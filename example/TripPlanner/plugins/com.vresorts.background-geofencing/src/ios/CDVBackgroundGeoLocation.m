////
//  CDVBackgroundGeoLocation
//
//  Created by Chris Scott <chris@transistorsoft.com> on 2013-06-15
//
#import "CDVLocation.h"
#import "CDVBackgroundGeoLocation.h"
#import <Cordova/CDVJSON.h>

// Debug sounds for bg-geolocation life-cycle events.
// http://iphonedevwiki.net/index.php/AudioServices
//#define exitRegionSound         1005
//#define locationSyncSound       1004
//#define paceChangeYesSound      1110
//#define paceChangeNoSound       1112
//#define acquiringLocationSound  1103
//#define acquiredLocationSound   1052
//#define locationErrorSound      1073


@implementation CDVBackgroundGeoLocation {

    BOOL isUpdatingLocation;

    UIBackgroundTaskIdentifier bgTask;
    NSDate *lastBgTaskAt;

    NSError *locationError;
    CLLocationManager *locationManager;
    UILocalNotification *localNotification;

    CDVLocationData *locationData;
    CLLocation *lastLocation;
    NSInteger locationAcquisitionAttempts;

    CLActivityType activityType;
    //JSON
    NSMutableData *responseData;
}

@synthesize syncCallbackId;
@synthesize stationaryRegionListeners;
@synthesize responseData = _responseData;
@synthesize enterTime;

- (void)pluginInitialize
{
    locationManager = [[CLLocationManager alloc] init];
    locationManager.delegate = self;
    locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters;
    [locationManager requestAlwaysAuthorization];
    [locationManager requestWhenInUseAuthorization];
    localNotification = [[UILocalNotification alloc] init];
    localNotification.timeZone = [NSTimeZone defaultTimeZone];

    isUpdatingLocation = NO;


    bgTask = UIBackgroundTaskInvalid;
    
}

- (void) configure:(CDVInvokedUrlCommand*)command
{

    self.syncCallbackId = command.callbackId;

    locationManager.activityType = activityType;
    locationManager.pausesLocationUpdatesAutomatically = YES;
    locationManager.distanceFilter = 10; // meters
    locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters;//can be changed later
    
    //remove the previous monitored regions when reconfigure
    for (CLRegion *monitored in [locationManager monitoredRegions])
    {
        NSLog(@"delete %@",monitored.identifier);
        [locationManager stopMonitoringForRegion:monitored];
    }
    
    // ios 8 requires permissions to send local-notifications
    
    UIApplication *app = [UIApplication sharedApplication];
    if ([app respondsToSelector:@selector(registerUserNotificationSettings:)])
    {
        [app registerUserNotificationSettings:[UIUserNotificationSettings settingsForTypes:UIUserNotificationTypeAlert|UIUserNotificationTypeBadge|UIUserNotificationTypeSound categories:nil]];
    }
    
    
    //retrieve json data
    self.responseData = [NSMutableData data];
    NSURLRequest *request = [NSURLRequest requestWithURL:
                             [NSURL URLWithString:@"http://xixixhalu-test.apigee.net/proxy/tripPlanner/getPlaces?trip_plan_uuid=3111e994-20e3-11e4-86d4-239193270808"]];
    [[NSURLConnection alloc] initWithRequest:request delegate:self];
    
}


-(void) addplace:(CDVInvokedUrlCommand *)command
{
    
    CLLocationDegrees lattitude = [[command argumentAtIndex:(0)] doubleValue];
    CLLocationDegrees longitude = [[command argumentAtIndex:(1)] doubleValue];
    NSString *name = [[command argumentAtIndex:(2)] stringValue];
    double radius = [[command argumentAtIndex:(3)] doubleValue];
    
    CLLocationCoordinate2D coor = CLLocationCoordinate2DMake(lattitude, longitude);
    CLCircularRegion *region = [[CLCircularRegion alloc] initWithCenter:coor radius:radius identifier:name];
    [locationManager startMonitoringForRegion:region];
    [self notify:[NSString stringWithFormat:@"Place %@ is added to monitoring",name]];
}

-(void) disableplace:(CDVInvokedUrlCommand *)command
{
    CLLocationDegrees lattitude = [[command argumentAtIndex:(0)] doubleValue];
    CLLocationDegrees longitude = [[command argumentAtIndex:(1)] doubleValue];
    NSString *name = [[command argumentAtIndex:(2)] stringValue];
    double radius = [[command argumentAtIndex:(3)] doubleValue];
    
    CLLocationCoordinate2D coor = CLLocationCoordinate2DMake(lattitude, longitude);
    CLCircularRegion *region = [[CLCircularRegion alloc] initWithCenter:coor radius:radius identifier:name];
    [locationManager stopMonitoringForRegion:region];
    [self notify:[NSString stringWithFormat:@"Place %@ is deleted from monitoring",name]];
}


- (void) getCurrentLocation:(CDVInvokedUrlCommand *)command
{
    //TO DO: find out what format to return the current user location
    CDVPluginResult* result = nil;
    NSMutableDictionary *returnInfo = [self locationToHash:lastLocation];
    result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnInfo];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

/**
 *  Start geofencing
 */
- (void) start:(CDVInvokedUrlCommand*)command
{
    [self stopUpdatingLocation];
    CDVPluginResult* result = nil;
    result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}
/**
 *  Stop geofencing
 */
- (void) stop:(CDVInvokedUrlCommand*)command
{
    [self stopUpdatingLocation];
    CDVPluginResult* result = nil;
    result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}



/**********************************************/

-(NSMutableDictionary*) locationToHash:(CLLocation*)location
{
    NSMutableDictionary *returnInfo;
    returnInfo = [NSMutableDictionary dictionaryWithCapacity:10];
    
    NSNumber* timestamp = [NSNumber numberWithDouble:([location.timestamp timeIntervalSince1970] * 1000)];
    [returnInfo setObject:timestamp forKey:@"timestamp"];
    [returnInfo setObject:[NSNumber numberWithDouble:location.coordinate.latitude] forKey:@"latitude"];
    [returnInfo setObject:[NSNumber numberWithDouble:location.coordinate.longitude] forKey:@"longitude"];
    
    return returnInfo;
}


-(void) addPlace:(CLLocationCoordinate2D) coordinate
                    name:(NSString *)name radius:(NSNumber *) radius{

    CLCircularRegion *region = [[CLCircularRegion alloc] initWithCenter:coordinate radius:[radius doubleValue] identifier:name];
    [locationManager startMonitoringForRegion:region];
    [self notify:[NSString stringWithFormat:@"Place %@ is added to monitoring",name]];
}

-(void) disablePlace:(CLLocationCoordinate2D) coordinate
                name:(NSString *)name radius:(NSNumber *) radius{
    CLCircularRegion *region = [[CLCircularRegion alloc] initWithCenter:coordinate radius:[radius doubleValue] identifier:name];
    [locationManager stopMonitoringForRegion:region];
    [self notify:[NSString stringWithFormat:@"Place %@ is deleted from monitoring",name]];
}

-(void) locationManager:(CLLocationManager *)manager didEnterRegion:(CLRegion *)region{
    //AudioServicesPlaySystemSound (exitRegionSound);
    [self notify:[NSString stringWithFormat:@"Enter %@",region.identifier]];
    enterTime = [NSDate date];
}

- (void)locationManager:(CLLocationManager *)manager didExitRegion:(CLRegion *)region
{
    //NSLog(@"- CDVBackgroundGeoLocation exit region");
    NSDate *leaveTime = [NSDate date];
    NSTimeInterval stayingTime = [leaveTime timeIntervalSinceDate:enterTime];
    [self notify:[NSString stringWithFormat:@"Exit %@, stay for %f",region.identifier,stayingTime]];
}

/**** Handle Connection to fetch json data *****/

- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response {
    NSLog(@"didReceiveResponse");
    [self.responseData setLength:0];
}

- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data {
    [self.responseData appendData:data];
}

- (void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error {
    NSLog(@"didFailWithError");
    NSLog([NSString stringWithFormat:@"Connection failed: %@", [error description]]);
}

- (void)connectionDidFinishLoading:(NSURLConnection *)connection {
    NSLog(@"connectionDidFinishLoading");
    NSLog(@"Succeeded! Received %lu bytes of data",(unsigned long)[self.responseData length]);
    
    NSError *myError = nil;
    NSDictionary *result = [NSJSONSerialization JSONObjectWithData:self.responseData options:NSJSONReadingMutableLeaves error:&myError];
    
    NSArray *places = [result objectForKey:@"places"];
    
    for(NSDictionary *results in places)
    {
        NSDictionary *geofence = [results objectForKey:@"geofence"];
        //if geofence is null then ignore
        if (geofence == NULL) {continue;}
        NSString *place_name = [results objectForKey:@"place_name"];
        NSNumber *latitude = [geofence objectForKey:@"latitude"];
        NSNumber *longitude = [geofence objectForKey:@"longitude"];
        NSString *is_subscribed = [results objectForKey:@"is_subscribed"];
        NSNumber *radius = [geofence objectForKey:@"radius"];
        
        CLLocationCoordinate2D coord;
        coord.longitude = (CLLocationDegrees)[latitude doubleValue];
        coord.latitude = (CLLocationDegrees)[longitude doubleValue];
        if ([is_subscribed containsString:@"true"])
        {
            
            [self addPlace:coord name:place_name radius:radius];
        }
        else
        {
            [self disablePlace:coord name:place_name radius:radius];
            
        }
    }
    
}
/**********************************************/

-(void) locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray *)locations
{
    lastLocation = [locations lastObject];
    NSLog(@"User Location Updated: @%", lastLocation);
}

- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error
{
    NSLog(@"- CDVBackgroundGeoLocation locationManager failed:  %@", error);
    
    [self notify:[NSString stringWithFormat:@"Location error: %@", error.localizedDescription]];
    

    locationError = error;

    switch(error.code) {
        case kCLErrorLocationUnknown:
        case kCLErrorNetwork:
        case kCLErrorRegionMonitoringDenied:
        case kCLErrorRegionMonitoringSetupDelayed:
        case kCLErrorRegionMonitoringResponseDelayed:
        case kCLErrorGeocodeFoundNoResult:
        case kCLErrorGeocodeFoundPartialResult:
        case kCLErrorGeocodeCanceled:
            break;
        case kCLErrorDenied:
            [self stopUpdatingLocation];
            break;
        default:
            [self stopUpdatingLocation];
    }
}

- (void) stopUpdatingLocation
{
    [locationManager stopUpdatingLocation];
    isUpdatingLocation = NO;
}

- (void) startUpdatingLocation
{
    [locationManager startUpdatingLocation];
    isUpdatingLocation = YES;
}

- (NSTimeInterval) locationAge:(CLLocation*)location
{
    return -[location.timestamp timeIntervalSinceNow];
}

- (void) notify:(NSString*)message
{
    localNotification.fireDate = [NSDate date];
    localNotification.alertBody = message;
    [[UIApplication sharedApplication] scheduleLocalNotification:localNotification];
}
/**
 * If you don't stopMonitoring when application terminates, the app will be awoken still when a
 * new location arrives, essentially monitoring the user's location even when they've killed the app.
 * Might be desirable in certain apps.
 */
- (void)applicationWillTerminate:(UIApplication *)application {
    [locationManager stopMonitoringSignificantLocationChanges];
    [locationManager stopUpdatingLocation];

}

- (void)dealloc
{
    locationManager.delegate = nil;
}

- (void) onAppTerminate
{
    [self stopUpdatingLocation];
}

@end

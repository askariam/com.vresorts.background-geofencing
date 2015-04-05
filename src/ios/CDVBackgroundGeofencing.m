////
//  CDVBackgroundGeoLocation
//
//  Created by Chris Scott <chris@transistorsoft.com> on 2013-06-15
//
#import "CDVLocation.h"
#import "CDVBackgroundGeofencing.h"
#import <Cordova/CDVJSON.h>

// Debug sounds for bg-geolocation life-cycle events.
// http://iphonedevwiki.net/index.php/AudioServices
#define exitRegionSound         1005
#define locationSyncSound       1004
#define paceChangeYesSound      1110
#define paceChangeNoSound       1112
#define acquiringLocationSound  1103
#define acquiredLocationSound   1052
#define locationErrorSound      1073
#define msgreceived            1307


@implementation CDVBackgroundGeofencing {
    
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
    NSMutableDictionary *place_name_map;
    NSMutableDictionary *offer_uuid_map;
    NSMutableDictionary *lat_map;
    NSMutableDictionary *lon_map;
    NSMutableDictionary *raduis_map;
    NSMutableDictionary *notification_map;
    NSString *callback_id;
}

@synthesize syncCallbackId;
@synthesize stationaryRegionListeners;
@synthesize enterTime;
@synthesize offer_uuid_map = offer_uuid_map;
@synthesize lat_map = lat_map;
@synthesize lon_map = lon_map;
@synthesize radius_map = raduis_map;
@synthesize place_name_map = place_name_map;
@synthesize notification_map = notification_map;
- (void)pluginInitialize
{
    [super pluginInitialize];
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
    self.place_name_map = [NSMutableDictionary dictionaryWithCapacity:100];
    self.offer_uuid_map = [NSMutableDictionary dictionaryWithCapacity:100];
    self.lat_map = [NSMutableDictionary dictionaryWithCapacity:100];
    self.lon_map = [NSMutableDictionary dictionaryWithCapacity:100];
    self.radius_map = [NSMutableDictionary dictionaryWithCapacity:100];
    self.notification_map = [NSMutableDictionary dictionaryWithCapacity:100];
    //fetch json and add them into geofence data
    NSArray *array = [command arguments];
    NSDictionary *result = [array objectAtIndex:0];
    NSArray *places = [result objectForKey:@"places"];
    for(NSDictionary *results in places)
    {
        NSDictionary *geofence = [results objectForKey:@"geofence"];
        //if geofence is null then ignore
        if (geofence == NULL) {continue;}
        NSString *uuid = [results objectForKey:@"uuid"];
        NSString *name = [results objectForKey:@"place_name"];
        NSNumber *latitude = [geofence objectForKey:@"latitude"];
        NSNumber *longitude = [geofence objectForKey:@"longitude"];
        NSString *is_subscribed = [results objectForKey:@"is_subscribed"];
        NSNumber *radius = [geofence objectForKey:@"radius"];
        NSString *offer_uuid = [geofence objectForKey:@"offer_uuid"];
        CLLocationCoordinate2D coord;
        coord.longitude = (CLLocationDegrees)[latitude doubleValue];
        coord.latitude = (CLLocationDegrees)[longitude doubleValue];
        //add the place information in map for later use
        //such as ufferuuid, raduis, lat_lon...
        [place_name_map setObject:name forKey:uuid];
        [offer_uuid_map setObject:offer_uuid forKey:uuid];
        [lat_map setObject:latitude forKey:uuid];
        [lon_map setObject:longitude forKey:uuid];
        [raduis_map setObject:radius forKey:uuid];
        
        if ([is_subscribed containsString:@"true"])
        {
            [self addPlace:coord name:uuid radius:radius];
        }
        else
        {
            [self disablePlace:coord name:uuid radius:radius];
        }
    }

    self.syncCallbackId = command.callbackId;
    
    locationManager.activityType = activityType;
    locationManager.pausesLocationUpdatesAutomatically = YES;
    locationManager.distanceFilter = 10; // meters
    locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters;//can be changed later
    
    // ios 8 requires permissions to send local-notifications
    
    UIApplication *app = [UIApplication sharedApplication];
    if ([app respondsToSelector:@selector(registerUserNotificationSettings:)])
    {
        [app registerUserNotificationSettings:[UIUserNotificationSettings settingsForTypes:UIUserNotificationTypeAlert|UIUserNotificationTypeBadge|UIUserNotificationTypeSound categories:nil]];
    }
    


}


//-(void) addPlace:(CDVInvokedUrlCommand *)command
//{
//
//    CLLocationDegrees lattitude = [[command argumentAtIndex:(0)] doubleValue];
//    CLLocationDegrees longitude = [[command argumentAtIndex:(1)] doubleValue];
//    NSString *name = [[command argumentAtIndex:(2)] stringValue];
//    double radius = [[command argumentAtIndex:(3)] doubleValue];
//
//    CLLocationCoordinate2D coor = CLLocationCoordinate2DMake(lattitude, longitude);
//    CLCircularRegion *region = [[CLCircularRegion alloc] initWithCenter:coor radius:radius identifier:name];
//    [locationManager startMonitoringForRegion:region];
//    [self notify:[NSString stringWithFormat:@"Place %@ is added to monitoring",name]];
//}
//
//-(void) deletePlace:(CDVInvokedUrlCommand *)command
//{
//    CLLocationDegrees lattitude = [[command argumentAtIndex:(0)] doubleValue];
//    CLLocationDegrees longitude = [[command argumentAtIndex:(1)] doubleValue];
//    NSString *name = [[command argumentAtIndex:(2)] stringValue];
//    double radius = [[command argumentAtIndex:(3)] doubleValue];
//
//    CLLocationCoordinate2D coor = CLLocationCoordinate2DMake(lattitude, longitude);
//    CLCircularRegion *region = [[CLCircularRegion alloc] initWithCenter:coor radius:radius identifier:name];
//    [locationManager stopMonitoringForRegion:region];
//    [self notify:[NSString stringWithFormat:@"Place %@ is deleted from monitoring",name]];
//}

- (void) enablePlace:(CDVInvokedUrlCommand*)command
{
    NSArray *array = [command arguments];
    NSDictionary *place = [array objectAtIndex:0];
    NSString *key = [place objectForKey:@"place_uuid"];
    CLLocationDegrees lattitude = [[lat_map objectForKey:key]doubleValue];
    CLLocationDegrees longitude = [[lon_map objectForKey:key]doubleValue];
    NSString *name = key;
    NSNumber *radius = [raduis_map objectForKey:key];
    CLLocationCoordinate2D coor = CLLocationCoordinate2DMake(lattitude, longitude);

    [self addPlace:coor name:name radius:radius];
    [self notify:name];
    CDVPluginResult* result = nil;
    result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    
}

- (void) disablePlace:(CDVInvokedUrlCommand*)command
{
    NSArray *array = [command arguments];
    NSDictionary *place = [array objectAtIndex:0];
    NSString *key = [place objectForKey:@"place_uuid"];
    CLLocationDegrees lattitude = [[lat_map objectForKey:key]doubleValue];
    CLLocationDegrees longitude = [[lon_map objectForKey:key]doubleValue];
    NSString *name = key;
    NSNumber *radius = [raduis_map objectForKey:key];
    CLLocationCoordinate2D coor = CLLocationCoordinate2DMake(lattitude, longitude);
    
    [self disablePlace:coor name:name radius:radius];
    CDVPluginResult* result = nil;
    result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}


- (void) getCurrentLocation:(CDVInvokedUrlCommand *)command
{
    //
    //TO DO: transfer format to json  {lat:, lng:}
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
    [self startUpdatingLocation];
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


- (void) setOnNotificationClickedCallback:(CDVInvokedUrlCommand *)command
{
    callback_id = command.callbackId;
    //NSLog(@"Notification callback is set");
//    
//    
//    CDVPluginResult* result = nil;
//    result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
//    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];

}


/**********************************************/

- (void) didReceiveLocalNotification:(NSNotification *)notification
{

    NSString *key = [self regularexpforkey:notification];
    NSString *message = [notification_map objectForKey:key];
    
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:message];
    [pluginResult setKeepCallbackAsBool:YES]; // here we tell Cordova not to cleanup the callback id after sendPluginResult()
    [self.commandDelegate sendPluginResult:pluginResult callbackId:callback_id];
    
}

- (NSString*) regularexpforkey:(NSNotification *)notification
{
    //NSLog(@"%@",notification.description);
    NSString *datestring = (NSString *)notification.description;
    NSRange start_date;
    NSRange end_date;
    //NSLog(@"datestring is = %@", datestring);
    //capture the string part of date
    //NSLog(@"datestring is =%@",datestring);
    start_date = [datestring rangeOfString:@" at "];
    end_date = [datestring rangeOfString:@", time zone"];
    NSRange new_range;
    new_range.length = end_date.location-start_date.location;
    new_range.location = start_date.location;
    NSString *key = [datestring substringWithRange:new_range];
    //NSLog(@"key is = %@", key);
    key = [key substringFromIndex:6];
    //NSLog(@"key is = %@", key);
    //strip all other character except digits
    NSString *newString = [[key componentsSeparatedByCharactersInSet:
                            [[NSCharacterSet decimalDigitCharacterSet] invertedSet]]
                           componentsJoinedByString:@""];
    //NSLog(@"%@",newString);
    return newString;
}



-(NSMutableDictionary*) locationToHash:(CLLocation*)location
{
    NSMutableDictionary *returnInfo;
    returnInfo = [NSMutableDictionary dictionaryWithCapacity:10];
    [returnInfo setObject:[NSNumber numberWithDouble:location.coordinate.latitude] forKey:@"latitude"];
    [returnInfo setObject:[NSNumber numberWithDouble:location.coordinate.longitude] forKey:@"longitude"];
    
    return returnInfo;
}


-(void) addPlace:(CLLocationCoordinate2D) coordinate
            name:(NSString *)name radius:(NSNumber *) radius{
    
    CLCircularRegion *region = [[CLCircularRegion alloc] initWithCenter:coordinate radius:[radius doubleValue] identifier:name];
    [locationManager startMonitoringForRegion:region];
    //[self notify: name];
}

-(void) disablePlace:(CLLocationCoordinate2D) coordinate
                name:(NSString *)name radius:(NSNumber *) radius{
    CLCircularRegion *region = [[CLCircularRegion alloc] initWithCenter:coordinate radius:[radius doubleValue] identifier:name];
    [locationManager stopMonitoringForRegion:region];
    //[self notify:[NSString stringWithFormat:@"Place %@ is deleted from monitoring",name]];
}

-(void) locationManager:(CLLocationManager *)manager didEnterRegion:(CLRegion *)region{
    //AudioServicesPlaySystemSound (exitRegionSound);
    NSString *name = [place_name_map objectForKey:region.identifier];
    [self notify:[NSString stringWithFormat:@"%@",name]];
    enterTime = [NSDate date];
}

- (void)locationManager:(CLLocationManager *)manager didExitRegion:(CLRegion *)region
{
    //NSLog(@"- CDVBackgroundGeoLocation exit region");
    NSDate *leaveTime = [NSDate date];
    NSString *name = [place_name_map objectForKey:region.identifier];
    NSTimeInterval stayingTime = [leaveTime timeIntervalSinceDate:enterTime];
    //[self notify:[NSString stringWithFormat:@"Leaves %@, stays for %f",name,stayingTime]];
}

/**** Handle Connection to fetch json data *****/
//
//- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response {
//    NSLog(@"didReceiveResponse");
//    [self.responseData setLength:0];
//}
//
//- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data {
//    [self.responseData appendData:data];
//}
//
//- (void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error {
//    NSLog(@"didFailWithError");
//    NSLog([NSString stringWithFormat:@"Connection failed: %@", [error description]]);
//}


/**********************************************/

-(void) locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray *)locations
{
    lastLocation = [locations lastObject];
    //NSLog(@"User Location Updated: @%", lastLocation);
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
    AudioServicesPlaySystemSound(msgreceived);
    NSString *name = [place_name_map objectForKey:message];
    localNotification.alertTitle = [NSString stringWithFormat:@"You just got an offer from %@",name];
    localNotification.alertBody = @"Click to see more detail!";
    [[UIApplication sharedApplication] scheduleLocalNotification:localNotification];
    [self setupcallbacknotification:message];
}

-(void) setupcallbacknotification:(NSString*)message
{
    NSMutableDictionary *msg = [[NSMutableDictionary alloc]init];
    NSString *offer_id =[offer_uuid_map objectForKey:message];
    NSString *place_name = [place_name_map objectForKey:message];
    [msg setObject:offer_id forKey:@"offerUuid"];
    [msg setObject:place_name forKey:@"placeName"];
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:msg options:NSJSONWritingPrettyPrinted error:&error];
    NSString *json_result = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    NSDateFormatter *dateformater = [[NSDateFormatter alloc]init];
    [dateformater setDateFormat:@"mmss"];
    NSString *stringDate = [dateformater stringFromDate:[NSDate date]];
    NSLog(@"date is %@",stringDate);
    
    [notification_map setObject:json_result forKey:stringDate];
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

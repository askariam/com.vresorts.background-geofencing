////
//  CDVBackgroundGeoLocation
//
//  Created by Chein-Hsing Lu <dreadlord1110@gmail.com> on 2015-04-06
//

#import "CDVBackgroundGeofencing.h"
#import <Cordova/CDVJSON.h>

// Debug sounds for bg-geolocation life-cycle events.
// http://iphonedevwiki.net/index.php/AudioServices
#define msgreceived            1307

@implementation NSString (Contains)

- (BOOL)myContainsString:(NSString*)other {
    NSRange range = [self rangeOfString:other];
    return range.length != 0;
}

@end

@implementation CDVBackgroundGeofencing {
    
    UIBackgroundTaskIdentifier bgTask;
    NSDate *lastBgTaskAt;
    NSError *locationError;
    UILocalNotification *localNotification;
    NSNotificationCenter *notification_center;
    CDVLocationData *locationData;
    CLLocation *lastLocation;
    NSInteger locationAcquisitionAttempts;
    
    CLActivityType activityType;
    NSMutableDictionary *uuid_map;
    NSMutableDictionary *offer_uuid_map;
    NSMutableDictionary *lat_map;
    NSMutableDictionary *lon_map;
    NSMutableDictionary *raduis_map;
    NSMutableDictionary *notification_map;
    NSString *callback_id;
}
@synthesize geofences;
@synthesize locationManager;
@synthesize syncCallbackId;
@synthesize enterTime;
@synthesize offer_uuid_map = offer_uuid_map;
@synthesize lat_map = lat_map;
@synthesize lon_map = lon_map;
@synthesize radius_map = raduis_map;
@synthesize uuid_map = uuid_map;
@synthesize notification_map = notification_map;
- (void)pluginInitialize
{
    [self locationManagerSetup];
    
    [super pluginInitialize];
    
    bgTask = UIBackgroundTaskInvalid;
    
    notification_center = [NSNotificationCenter defaultCenter];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onSuspend:) name:UIApplicationDidEnterBackgroundNotification object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onResume:) name:UIApplicationWillEnterForegroundNotification object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onPause:) name:UIApplicationWillEnterForegroundNotification object:nil];
}

-(void)locationManagerSetup
{
    if(!self.locationManager) self.locationManager = [[CLLocationManager alloc] init];
    self.locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters;
    self.locationManager.distanceFilter = 10; // meters
    self.locationManager.delegate = self;
    
    geofences = [NSMutableArray arrayWithArray:[[self.locationManager monitoredRegions] allObjects]];
    
    NSString *version = [[UIDevice currentDevice] systemVersion];
    if ([version floatValue] >= 8.0f) //for iOS8
    {
        if ([self.locationManager respondsToSelector:@selector(requestAlwaysAuthorization)]) {
            [self.locationManager requestAlwaysAuthorization];
        }
        [self.locationManager requestWhenInUseAuthorization];
    }
    
    localNotification = [[UILocalNotification alloc] init];
    localNotification.timeZone = [NSTimeZone defaultTimeZone];
}

- (void) configure:(CDVInvokedUrlCommand*)command
{
    self.uuid_map = [NSMutableDictionary dictionaryWithCapacity:100];
    self.offer_uuid_map = [NSMutableDictionary dictionaryWithCapacity:100];
    self.lat_map = [NSMutableDictionary dictionaryWithCapacity:100];
    self.lon_map = [NSMutableDictionary dictionaryWithCapacity:100];
    self.radius_map = [NSMutableDictionary dictionaryWithCapacity:100];
    self.notification_map = [NSMutableDictionary dictionaryWithCapacity:100];
    
    //delete the previous geofence point
    for (CLRegion *monitored in [self.locationManager monitoredRegions])
        [self.locationManager stopMonitoringForRegion:monitored];
    
    
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
        NSString *latitude = [geofence objectForKey:@"latitude"];
        NSString *longitude = [geofence objectForKey:@"longitude"];
        NSString *is_subscribed = [results objectForKey:@"is_subscribed"];
        NSString *radius = [geofence objectForKey:@"radius"];
        NSString *offer_uuid = [results objectForKey:@"offer_uuid"];
        //add the place information in map for later use
        //such as ufferuuid, raduis, lat_lon...
        if(uuid !=nil && offer_uuid != nil && longitude != nil && latitude != nil && radius != nil && name != nil)
        {
            NSNumberFormatter *f = [[NSNumberFormatter alloc] init];
            f.numberStyle = NSNumberFormatterDecimalStyle;
            NSNumber *lat = [f numberFromString:latitude];
            NSNumber *lon = [f numberFromString:longitude];
            NSNumber *rad = [f numberFromString:radius];

            CLLocationCoordinate2D coord;
            coord.longitude = (CLLocationDegrees)[lon doubleValue];
            coord.latitude = (CLLocationDegrees)[lat doubleValue];
            
            [uuid_map setObject:uuid forKey:name];
            [offer_uuid_map setObject:offer_uuid forKey:name];
            [lat_map setObject:lat forKey:name];
            [lon_map setObject:lon forKey:name];
            [raduis_map setObject:rad forKey:name];
            
            if ([is_subscribed myContainsString:@"true"])
            {
                [self addPlace:coord name:name radius:rad];
            }
            else
            {
                [self disablePlace:coord name:name radius:rad];
            }
        }
        
    }
    
    self.syncCallbackId = command.callbackId;
    
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
    //[self notify:name];
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
-(void) locationManager:(CLLocationManager *)manager didStartMonitoringForRegion:(CLRegion *)region
{
    
}


- (void) didReceiveLocalNotification:(NSNotification *)notification
{
    
    NSDictionary *dict = [[notification object]userInfo];
    
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dict options:NSJSONWritingPrettyPrinted error:&error];
    NSString *json_result = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
//    NSString *key = [self regularexpforkey:notification];
//    NSString *message = [notification_map objectForKey:key];
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:json_result];
    [pluginResult setKeepCallbackAsBool:YES]; // here we tell Cordova not to cleanup the callback id after sendPluginResult()
    [self.commandDelegate sendPluginResult:pluginResult callbackId:callback_id];
    
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
    
    CLRegion *region = [[CLRegion alloc]initCircularRegionWithCenter:coordinate radius:[radius doubleValue] identifier:name];
    //NSLog(@"lat is %f and lon is %f and radius is %@ and name is %@",coordinate.latitude,coordinate.longitude,radius,name);
    [self.locationManager startMonitoringForRegion:region];
    
    //if([[self.locationManager monitoredRegions] count]<1)NSLog(@"No Monitored added!!!");
    
    //[self notify: name];
}

-(void) disablePlace:(CLLocationCoordinate2D) coordinate
                name:(NSString *)name radius:(NSNumber *) radius{
    CLCircularRegion *region = [[CLCircularRegion alloc] initWithCenter:coordinate radius:[radius doubleValue] identifier:name];
    [self.locationManager stopMonitoringForRegion:region];
    //[self notify:[NSString stringWithFormat:@"Place %@ is deleted from monitoring",name]];
}

-(void) locationManager:(CLLocationManager *)manager didEnterRegion:(CLRegion *)region
{
    //AudioServicesPlaySystemSound (exitRegionSound);
    [self notify:region.identifier];
    enterTime = [NSDate date];
}

- (void)locationManager:(CLLocationManager *)manager didExitRegion:(CLRegion *)region
{
    //NSLog(@"- CDVBackgroundGeoLocation exit region");
    NSDate *leaveTime = [NSDate date];
    //NSString *name = [place_name_map objectForKey:region.identifier];
    NSTimeInterval stayingTime = [leaveTime timeIntervalSinceDate:enterTime];
    //[self notify:[NSString stringWithFormat:@"Leaves %@, stays for %f",name,stayingTime]];
}


/**********************************************/

- (NSNumber*)calculateDistanceInMetersBetweenCoord:(CLLocationCoordinate2D)coord1 coord:(CLLocationCoordinate2D)coord2 {
    NSInteger nRadius = 6371; // Earth's radius in Kilometers
    double latDiff = (coord2.latitude - coord1.latitude) * (M_PI/180);
    double lonDiff = (coord2.longitude - coord1.longitude) * (M_PI/180);
    double lat1InRadians = coord1.latitude * (M_PI/180);
    double lat2InRadians = coord2.latitude * (M_PI/180);
    double nA = pow ( sin(latDiff/2), 2 ) + cos(lat1InRadians) * cos(lat2InRadians) * pow ( sin(lonDiff/2), 2 );
    double nC = 2 * atan2( sqrt(nA), sqrt( 1 - nA ));
    double nD = nRadius * nC;
    // convert to meters
    return @(nD*1000);
}

-(void) locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray *)locations
{
    lastLocation = [locations lastObject];
    
    BOOL static first_time = TRUE;
    if(first_time)//enter if user is already in the monitored region, then enter didEnterMonitoredRegion manually once
    {
        first_time = FALSE;
        NSSet *monitoredRegions = self.locationManager.monitoredRegions;
        
        if(monitoredRegions)
        {
            [monitoredRegions enumerateObjectsUsingBlock:^(CLRegion *region,BOOL *stop)
             {
                 CLLocationCoordinate2D centerCoords =region.center;
                 CLLocationCoordinate2D currentCoords= CLLocationCoordinate2DMake(lastLocation.coordinate.latitude,lastLocation.coordinate.longitude);
                 CLLocationDistance radius = region.radius;
                 
                 NSNumber * currentLocationDistance =[self calculateDistanceInMetersBetweenCoord:currentCoords coord:centerCoords];
                 if([currentLocationDistance floatValue] < radius)
                 {
                     //stop Monitoring Region temporarily
                     [self.locationManager stopMonitoringForRegion:region];
                     
                     [self locationManager:locationManager didEnterRegion:region];
                     //start Monitoing Region again.
                     [self.locationManager startMonitoringForRegion:region];
                 }
             }];
        }
    }
}

- (void)locationManager:(CLLocationManager *)manager monitoringDidFailForRegion:(CLRegion *)region
              withError:(NSError *)error
{
    NSLog(@"Monitored regions are %lu",(unsigned long)[[self.locationManager monitoredRegions]count]);
    NSLog(@"Encounter error when start monitoring for region %@",region.identifier);
    NSLog(@"%@ happens.",error.description);
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
    [self.locationManager stopUpdatingLocation];
}

- (void) startUpdatingLocation
{
    [self.locationManager startUpdatingLocation];
}

- (NSTimeInterval) locationAge:(CLLocation*)location
{
    return -[location.timestamp timeIntervalSinceNow];
}

- (void) notify:(NSString*)message
{
    NSMutableDictionary *msg = [[NSMutableDictionary alloc]init];
    NSString *offer_id =[offer_uuid_map objectForKey:message];
    //NSString *place_name = [place_name_map objectForKey:message];
    [msg setObject:offer_id forKey:@"offerUuid"];
    [msg setObject:message forKey:@"placeName"];
    
    localNotification.fireDate = [NSDate date];
    localNotification.userInfo = msg;
    localNotification.alertBody = [NSString stringWithFormat:@"You just got an offer from %@ !",message];
    
    [[UIApplication sharedApplication] scheduleLocalNotification:localNotification];
    AudioServicesPlaySystemSound(msgreceived);

}


/**
 * If you don't stopMonitoring when application terminates, the app will be awoken still when a
 * new location arrives, essentially monitoring the user's location even when they've killed the app.
 * Might be desirable in certain apps.
 */
- (void)applicationWillTerminate:(UIApplication *)application
{
    [self.locationManager stopUpdatingLocation];
}

- (void)dealloc
{
    self.locationManager.delegate = nil;
}

-(UIBackgroundTaskIdentifier) createBackgroundTask
{
    lastBgTaskAt = [NSDate date];
    return [[UIApplication sharedApplication] beginBackgroundTaskWithExpirationHandler:^{
        [self stopBackgroundTask];
    }];
}

- (void) stopBackgroundTask
{
    UIApplication *app = [UIApplication sharedApplication];
    NSLog(@"- CDVBackgroundGeoLocation stopBackgroundTask (remaining t: %f)", app.backgroundTimeRemaining);
    if (bgTask != UIBackgroundTaskInvalid)
    {
        [app endBackgroundTask:bgTask];
        bgTask = UIBackgroundTaskInvalid;
    }
}



///**
// * Suspend.  Turn on passive location services
// */
-(void) onSuspend:(NSNotification *) notification
{
    NSLog(@"- CDVBackgroundGeoLocation suspend");
}
///**@
// * Resume
// */
-(void) onResume:(NSNotification *) notification
{
    NSLog(@"- CDVBackgroundGeoLocation resume");
}

-(void) onPause:(NSNotification *) notification
{
    NSLog(@"- CDVBackgroundGeoLocation onpause");
}

-(void) onPause
{
    
}

-(void) onResume
{
    
}

-(void) onReset
{
    
}


- (void) onAppTerminate
{
    [self stopUpdatingLocation];
}

@end

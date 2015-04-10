////
//  CDVBackgroundGeoLocation
//
//  Created by Chein-Hsing Lu <dreadlord1110@gmail.com> on 2015-04-06
//

#import <Cordova/CDVPlugin.h>
#import "CDVLocation.h"
#import <AudioToolbox/AudioToolbox.h>

@interface CDVBackgroundGeofencing : CDVPlugin <CLLocationManagerDelegate>
@property (nonatomic, strong) CLLocationManager* locationManager;
@property (strong, nonatomic) NSMutableArray *geofences;
@property (nonatomic, strong) NSString* syncCallbackId;
@property (nonatomic, strong) NSMutableDictionary *uuid_map;
@property (nonatomic, strong) NSMutableDictionary *offer_uuid_map;
@property (nonatomic, strong) NSMutableDictionary *lat_map;
@property (nonatomic, strong) NSMutableDictionary *lon_map;
@property (nonatomic, strong) NSMutableDictionary *radius_map;
@property (nonatomic, strong) NSMutableDictionary *notification_map;
@property (nonatomic, strong) NSDate *enterTime;
@property (nonatomic, strong) NSString *callback_id;
- (void) configure:(CDVInvokedUrlCommand*)command;
- (void) start:(CDVInvokedUrlCommand*)command;
- (void) stop:(CDVInvokedUrlCommand*)command;
- (void) addPlace:(CDVInvokedUrlCommand*)command;//not implemented yet
- (void) deletePlace:(CDVInvokedUrlCommand*)command;//not implemented yet
- (void) enablePlace:(CDVInvokedUrlCommand*)command;
- (void) disablePlace:(CDVInvokedUrlCommand*)command;
- (void) getCurrentLocation:(CDVInvokedUrlCommand *)command;
- (void) setOnNotificationClickedCallback:(CDVInvokedUrlCommand *)command;
- (NSNumber*)calculateDistanceInMetersBetweenCoord:(CLLocationCoordinate2D)coord1 coord:(CLLocationCoordinate2D)coord2;
- (void) onAppTerminate;
- (void) didReceiveLocalNotification:(NSNotification *)notification;
//- (NSString*) regularexpforkey:(NSNotification *)notification;
@end

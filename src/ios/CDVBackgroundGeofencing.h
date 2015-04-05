//
//  CDVBackgroundGeoLocation.h
//
//  Created by Chris Scott <chris@transistorsoft.com>
//

#import <Cordova/CDVPlugin.h>
#import "CDVLocation.h"
#import <AudioToolbox/AudioToolbox.h>

@interface CDVBackgroundGeofencing : CDVPlugin <CLLocationManagerDelegate>

@property (nonatomic, strong) NSString* syncCallbackId;
@property (nonatomic, strong) NSMutableArray* stationaryRegionListeners;
@property (nonatomic, strong) NSMutableDictionary *place_name_map;
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

- (void) onAppTerminate;
- (void) didReceiveLocalNotification:(NSNotification *)notification;
-(void) setupcallbacknotification:(NSString*)message;
- (NSString*) regularexpforkey:(NSNotification *)notification;
@end

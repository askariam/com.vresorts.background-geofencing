//
//  CDVBackgroundGeoLocation.h
//
//  Created by Chris Scott <chris@transistorsoft.com>
//

#import <Cordova/CDVPlugin.h>
#import "CDVLocation.h"
#import <AudioToolbox/AudioToolbox.h>

@interface CDVBackgroundGeoLocation : CDVPlugin <CLLocationManagerDelegate>

@property (nonatomic, strong) NSString* syncCallbackId;
@property (nonatomic, strong) NSMutableArray* stationaryRegionListeners;
@property (nonatomic, strong) NSMutableData *responseData;
@property (nonatomic, strong) NSDate *enterTime;
- (void) configure:(CDVInvokedUrlCommand*)command;
- (void) start:(CDVInvokedUrlCommand*)command;
- (void) stop:(CDVInvokedUrlCommand*)command;
- (void) addplace:(CDVInvokedUrlCommand*)command;
- (void) disableplace:(CDVInvokedUrlCommand*)command;
- (void) getCurrentLocation:(CDVInvokedUrlCommand *)command;
- (void) onAppTerminate;

@end


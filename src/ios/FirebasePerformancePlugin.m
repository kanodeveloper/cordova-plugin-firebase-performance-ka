#import "FirebasePerformancePlugin.h"

@import Firebase;

@implementation FirebasePerformancePlugin

@synthesize traces;

- (void)pluginInitialize {
    NSLog(@"Starting Firebase Performance plugin");

    if(![FIRApp defaultApp]) {
        [FIRApp configure];
    }
}

- (void)startTrace:(CDVInvokedUrlCommand *)command {

    [self.commandDelegate runInBackground:^{
        NSString* name = [command.arguments objectAtIndex:0];
        FIRTrace *trace = [self.traces objectForKey:name];

        if ( self.traces == nil) {
            self.traces = [NSMutableDictionary new];
        }

        if (trace == nil) {
            trace = [FIRPerformance startTraceWithName:name];
            [self.traces setObject:trace forKey:name ];
        }

        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)stopTrace:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        NSString* name = [command.arguments objectAtIndex:0];
        FIRTrace *trace = [self.traces objectForKey:name];
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];

        if (trace != nil) {
            [trace stop];
            [self.traces removeObjectForKey:name];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Trace not found"];
        }

        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

@end

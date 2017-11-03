#import "FirebasePerformancePlugin.h"
@import FirebasePerformance;
@import Firebase;

@implementation FirebasePerformancePlugin

- (void)pluginInitialize
{
    NSLog(@"Starting Firebase Performance plugin");

    if(![FIRApp defaultApp]) {
        [FIRApp configure];
    }
}

- (void)startTrace:(CDVInvokedUrlCommand *)command {
    //FIRTrace *trace = [FIRPerformance startTraceWithName:@"test trace"];
}

- (void)stopTrace:(CDVInvokedUrlCommand *)command {
    //[trace stop];
}

@end

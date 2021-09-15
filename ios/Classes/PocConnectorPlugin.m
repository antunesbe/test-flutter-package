#import "PocConnectorPlugin.h"
#if __has_include(<poc_connector/poc_connector-Swift.h>)
#import <poc_connector/poc_connector-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "poc_connector-Swift.h"
#endif

@implementation PocConnectorPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftPocConnectorPlugin registerWithRegistrar:registrar];
}
@end

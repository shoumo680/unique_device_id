#import "UniqueDeviceIdPlugin.h"
#if __has_include(<unique_device_id/unique_device_id-Swift.h>)
#import <unique_device_id/unique_device_id-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "unique_device_id-Swift.h"
#endif

@implementation UniqueDeviceIdPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftUniqueDeviceIdPlugin registerWithRegistrar:registrar];
}
@end

// The push demo's native stack, iOS: React Navigation's `direction` mirrors it only through
// semanticContentAttribute (react-native-screens), and UIKit's own layout direction stays the
// system's. The glass back button (iOS 26+) is laid out by the trait, its chevron by the forced
// attribute; when they disagree the chevron collapses to zero width. The example switches
// direction at run time, so the scene's trait follows it too.
#import <React/RCTBridgeModule.h>
#import <UIKit/UIKit.h>

@interface ExampleLayoutDirection : NSObject <RCTBridgeModule>
@end

@implementation ExampleLayoutDirection

RCT_EXPORT_MODULE()

- (dispatch_queue_t)methodQueue
{
  return dispatch_get_main_queue();
}

RCT_EXPORT_METHOD(setRTL : (BOOL)rtl)
{
  for (UIScene *scene in UIApplication.sharedApplication.connectedScenes) {
    if ([scene isKindOfClass:UIWindowScene.class]) {
      ((UIWindowScene *)scene).traitOverrides.layoutDirection =
          rtl ? UITraitEnvironmentLayoutDirectionRightToLeft : UITraitEnvironmentLayoutDirectionLeftToRight;
    }
  }
}

@end

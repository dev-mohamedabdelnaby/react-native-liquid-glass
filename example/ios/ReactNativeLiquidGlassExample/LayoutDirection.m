// The push demo's native stack, iOS: React Navigation's `direction` mirrors it only through
// semanticContentAttribute (react-native-screens), and UIKit's own layout direction stays the
// system's. The glass back button (iOS 26+) is laid out by the trait, its chevron by the forced
// attribute; when they disagree the chevron collapses to zero width. The example switches
// direction at run time, so the scene's trait follows it too.
#import <React/RCTBridgeModule.h>
#import <UIKit/UIKit.h>

@interface ExampleLayoutDirection : NSObject <RCTBridgeModule>
@end

// The direction asked for: "rtl", "ltr", or nil for the system's. Scenes that connect later get it too.
static NSString *requested;

static void apply(UIScene *scene)
{
  if (![scene isKindOfClass:UIWindowScene.class]) return;
  id<UITraitOverrides> overrides = ((UIWindowScene *)scene).traitOverrides;
  if ([requested isEqualToString:@"rtl"]) {
    overrides.layoutDirection = UITraitEnvironmentLayoutDirectionRightToLeft;
  } else if ([requested isEqualToString:@"ltr"]) {
    overrides.layoutDirection = UITraitEnvironmentLayoutDirectionLeftToRight;
  } else {
    [overrides removeTrait:UITraitLayoutDirection.class];
  }
}

@implementation ExampleLayoutDirection

RCT_EXPORT_MODULE()

- (dispatch_queue_t)methodQueue
{
  return dispatch_get_main_queue();
}

// "rtl", "ltr" or "system" (no override).
RCT_EXPORT_METHOD(setDirection : (NSString *)direction)
{
  static dispatch_once_t once;
  dispatch_once(&once, ^{
    [NSNotificationCenter.defaultCenter addObserverForName:UISceneWillConnectNotification
                                                    object:nil
                                                     queue:NSOperationQueue.mainQueue
                                                usingBlock:^(NSNotification *note) { apply(note.object); }];
  });
  requested = [direction isEqualToString:@"system"] ? nil : direction;
  for (UIScene *scene in UIApplication.sharedApplication.connectedScenes) apply(scene);
}

@end

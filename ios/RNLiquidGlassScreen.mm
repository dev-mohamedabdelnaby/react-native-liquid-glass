#import "RNLiquidGlassScreen.h"

#import <React/RCTConversions.h>
#import <React/RCTMountingTransactionObserving.h>

#import <react/renderer/components/RNLiquidGlassViewSpec/ComponentDescriptors.h>
#import <react/renderer/components/RNLiquidGlassViewSpec/EventEmitters.h>
#import <react/renderer/components/RNLiquidGlassViewSpec/Props.h>

#if __has_include(<ReactNativeLiquidGlass/ReactNativeLiquidGlass-Swift.h>)
#import <ReactNativeLiquidGlass/ReactNativeLiquidGlass-Swift.h>
#else
#import "ReactNativeLiquidGlass-Swift.h"
#endif

using namespace facebook::react;

static NSString *RNLiquidGlassScreenString(const std::string &value)
{
  return [NSString stringWithUTF8String:value.c_str()];
}

static UIColor *_Nullable RNLiquidGlassScreenColor(const SharedColor &color)
{
  return color ? RCTUIColorFromSharedColor(color) : nil;
}

@interface RNLiquidGlassScreen () <RCTMountingTransactionObserving>
@end

@implementation RNLiquidGlassScreen {
  LiquidGlassScreenHost *_host;
}

+ (ComponentDescriptorProvider)componentDescriptorProvider
{
  return concreteComponentDescriptorProvider<LiquidGlassScreenComponentDescriptor>();
}

// A recycled view would show the previous screen's native state (scroll position, open sheets).
+ (BOOL)shouldBeRecycled
{
  return NO;
}

- (instancetype)initWithFrame:(CGRect)frame
{
  if (self = [super initWithFrame:frame]) {
    static const auto defaultProps = std::make_shared<const LiquidGlassScreenProps>();
    _props = defaultProps;

    _host = [[LiquidGlassScreenHost alloc] initWithFrame:self.bounds];
    _host.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    __weak RNLiquidGlassScreen *weakSelf = self;
    _host.onBack = ^{
      RNLiquidGlassScreen *strongSelf = weakSelf;
      if (strongSelf && strongSelf->_eventEmitter) {
        std::static_pointer_cast<const LiquidGlassScreenEventEmitter>(strongSelf->_eventEmitter)
            ->onBack(LiquidGlassScreenEventEmitter::OnBack{});
      }
    };
    _host.onOpen = ^(NSString *screen) {
      RNLiquidGlassScreen *strongSelf = weakSelf;
      if (strongSelf && strongSelf->_eventEmitter) {
        std::static_pointer_cast<const LiquidGlassScreenEventEmitter>(strongSelf->_eventEmitter)
            ->onOpen({.screen = screen.UTF8String});
      }
    };
    _host.onSettingsChange = ^(NSDictionary<NSString *, NSString *> *s) {
      RNLiquidGlassScreen *strongSelf = weakSelf;
      if (!strongSelf || !strongSelf->_eventEmitter) {
        return;
      }
      // iOS has no controls for the Android-only settings: they go back as the props have them.
      const auto &p = *std::static_pointer_cast<const LiquidGlassScreenProps>(strongSelf->_props);
      std::static_pointer_cast<const LiquidGlassScreenEventEmitter>(strongSelf->_eventEmitter)
          ->onSettingsChange({
              .appearance = s[@"appearance"].UTF8String,
              .layoutDirection = s[@"layoutDirection"].UTF8String,
              .androidStyle = p.androidStyle,
              .androidSegmentedCheck = p.androidSegmentedCheck,
              .tint = s[@"tint"].UTF8String,
              .clear = p.clear,
              .blur = p.blur,
              .white = p.white,
              .black = p.black,
              .edgeLight = p.edgeLight,
              .refractionHeight = p.refractionHeight,
              .refractionAmount = p.refractionAmount,
              .opaque = p.opaque,
              .liquid = p.liquid,
              .dropColor = [LiquidGlassScreenHost rgba:RNLiquidGlassScreenColor(p.dropColor)].UTF8String,
              .sliderLiquid = p.sliderLiquid,
              .switchLiquid = p.switchLiquid,
              .segmentedLiquid = p.segmentedLiquid,
              .tabBarLiquid = p.tabBarLiquid,
              .tabBarDrop = p.tabBarDrop,
          });
    };
    [self addSubview:_host];
  }
  return self;
}

// iOS reads the settings its app has; the Android-only props are only echoed in onSettingsChange.
- (void)updateProps:(Props::Shared const &)props oldProps:(Props::Shared const &)oldProps
{
  const auto &p = *std::static_pointer_cast<LiquidGlassScreenProps const>(props);

  _host.screen = RNLiquidGlassScreenString(p.screen);
  _host.appearance = RNLiquidGlassScreenString(p.appearance);
  _host.layoutDirection = RNLiquidGlassScreenString(p.layoutDirection);
  _host.glassTint = RNLiquidGlassScreenColor(p.tintColor);
  [_host setFontsWithRegular:RNLiquidGlassScreenString(p.fontRegular)
                      medium:RNLiquidGlassScreenString(p.fontMedium)
                    semibold:RNLiquidGlassScreenString(p.fontSemibold)
                        bold:RNLiquidGlassScreenString(p.fontBold)
                       heavy:RNLiquidGlassScreenString(p.fontHeavy)];

  [super updateProps:props oldProps:oldProps];
}

// The window style is the screen's while it is in the React tree, not only while it is in the
// window: unmounting removes this view from its superview, then invalidates it (no recycling).
- (void)willMoveToSuperview:(UIView *)newSuperview
{
  [super willMoveToSuperview:newSuperview];
  if (!newSuperview) {
    [_host releaseWindowStyle];
    [_host detachFromViewController];
  }
}

// Every transaction: native-stack pushes a screen asynchronously after the transaction that
// mounted it, so the hosting controller becomes a child of the screen's view controller first.
- (void)mountingTransactionDidMount:(const MountingTransaction &)transaction
               withSurfaceTelemetry:(const SurfaceTelemetry &)surfaceTelemetry
{
  [_host attachToViewController];
}

- (void)invalidate
{
  [_host releaseWindowStyle];
  [super invalidate];
}

@end

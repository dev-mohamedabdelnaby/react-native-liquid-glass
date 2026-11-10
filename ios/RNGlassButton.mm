#import "RNGlassButton.h"

#import <React/RCTConversions.h>

#import <react/renderer/components/RNLiquidGlassViewSpec/ComponentDescriptors.h>
#import <react/renderer/components/RNLiquidGlassViewSpec/EventEmitters.h>
#import <react/renderer/components/RNLiquidGlassViewSpec/Props.h>

#if __has_include(<ReactNativeLiquidGlass/ReactNativeLiquidGlass-Swift.h>)
#import <ReactNativeLiquidGlass/ReactNativeLiquidGlass-Swift.h>
#else
#import "ReactNativeLiquidGlass-Swift.h"
#endif

using namespace facebook::react;

static NSString *_Nullable RNGlassButtonString(const std::string &value)
{
  return value.empty() ? nil : [NSString stringWithUTF8String:value.c_str()];
}

@implementation RNGlassButton {
  LiquidGlassButton *_button;
}

+ (ComponentDescriptorProvider)componentDescriptorProvider
{
  return concreteComponentDescriptorProvider<RNGlassButtonComponentDescriptor>();
}

- (instancetype)initWithFrame:(CGRect)frame
{
  if (self = [super initWithFrame:frame]) {
    static const auto defaultProps = std::make_shared<const RNGlassButtonProps>();
    _props = defaultProps;

    _button = [[LiquidGlassButton alloc] initWithFrame:self.bounds];
    _button.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    __weak RNGlassButton *weakSelf = self;
    _button.onPress = ^{
      RNGlassButton *strongSelf = weakSelf;
      if (strongSelf && strongSelf->_eventEmitter) {
        std::static_pointer_cast<const RNGlassButtonEventEmitter>(strongSelf->_eventEmitter)
            ->onButtonPress(RNGlassButtonEventEmitter::OnButtonPress{});
      }
    };
    [self addSubview:_button];
  }
  return self;
}

// Every prop is applied on every update, so a recycled view never keeps the previous owner's look.
- (void)updateProps:(Props::Shared const &)props oldProps:(Props::Shared const &)oldProps
{
  const auto &p = *std::static_pointer_cast<RNGlassButtonProps const>(props);

  _button.title = RNGlassButtonString(p.title);
  _button.systemImage = RNGlassButtonString(p.systemImage);
  _button.prominent = p.prominent;
  switch (p.size) {
    case RNGlassButtonSize::Small: _button.size = @"small"; break;
    case RNGlassButtonSize::Large: _button.size = @"large"; break;
    default: _button.size = @"regular";
  }
  _button.disabled = p.disabled;
  _button.tint = p.tintColor ? RCTUIColorFromSharedColor(p.tintColor) : nil;
  _button.opaqueFill = p.opaque;
  _button.glassStyle = p.glassStyle == RNGlassButtonGlassStyle::Clear ? @"clear" : @"regular";
  // The UIButton is the accessible element, so the label goes there.
  _button.label = RNGlassButtonString(p.accessibilityLabel);
  [_button apply];

  [super updateProps:props oldProps:oldProps];
}

@end

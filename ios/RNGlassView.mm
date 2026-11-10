#import "RNGlassView.h"

#import <React/RCTConversions.h>

#import <react/renderer/components/RNLiquidGlassViewSpec/ComponentDescriptors.h>
#import <react/renderer/components/RNLiquidGlassViewSpec/Props.h>
#import <react/renderer/components/RNLiquidGlassViewSpec/RCTComponentViewHelpers.h>

#if __has_include(<ReactNativeLiquidGlass/ReactNativeLiquidGlass-Swift.h>)
#import <ReactNativeLiquidGlass/ReactNativeLiquidGlass-Swift.h>
#else
#import "ReactNativeLiquidGlass-Swift.h"
#endif

using namespace facebook::react;

@implementation RNGlassView {
  LiquidGlassEffectView *_glass;
}

+ (ComponentDescriptorProvider)componentDescriptorProvider
{
  return concreteComponentDescriptorProvider<RNGlassViewComponentDescriptor>();
}

- (instancetype)initWithFrame:(CGRect)frame
{
  if (self = [super initWithFrame:frame]) {
    static const auto defaultProps = std::make_shared<const RNGlassViewProps>();
    _props = defaultProps;

    _glass = [[LiquidGlassEffectView alloc] initWithFrame:self.bounds];
    _glass.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    [self addSubview:_glass];
  }
  return self;
}

// Children live inside the glass `contentView`, after the opaque fill at index 0.
- (void)mountChildComponentView:(UIView<RCTComponentViewProtocol> *)childComponentView index:(NSInteger)index
{
  [_glass.contentView insertSubview:childComponentView atIndex:index + 1];
}

- (void)unmountChildComponentView:(UIView<RCTComponentViewProtocol> *)childComponentView index:(NSInteger)index
{
  [childComponentView removeFromSuperview];
}

// Every prop is applied on every update (the Swift setters ignore unchanged values), so a
// recycled view never keeps the previous owner's look.
- (void)updateProps:(Props::Shared const &)props oldProps:(Props::Shared const &)oldProps
{
  const auto &p = *std::static_pointer_cast<RNGlassViewProps const>(props);

  _glass.glassStyle = p.glassStyle == RNGlassViewGlassStyle::Clear ? @"clear" : @"regular";
  _glass.tint = p.tintColor ? RCTUIColorFromSharedColor(p.tintColor) : nil;
  _glass.interactive = p.interactive;
  _glass.opaqueFill = p.opaque;
  switch (p.shapeKind) {
    case RNGlassViewShapeKind::Circle: _glass.shapeKind = @"circle"; break;
    case RNGlassViewShapeKind::Rect: _glass.shapeKind = @"rect"; break;
    default: _glass.shapeKind = @"capsule";
  }
  _glass.cornerRadius = p.cornerRadius;

  [super updateProps:props oldProps:oldProps];
}

@end

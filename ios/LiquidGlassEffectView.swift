import UIKit

/// A `UIVisualEffectView` with `UIGlassEffect`; React Native children mount into `contentView`.
@objc(LiquidGlassEffectView)
@MainActor
public final class LiquidGlassEffectView: UIView {
  let effectView = UIVisualEffectView()
  // Index 0 of `contentView`; shown only when `opaque`.
  private let fill = UIView()

  /// Where React Native children mount (after the fill).
  @objc public var contentView: UIView { effectView.contentView }

  @objc public var glassStyle = "regular" { didSet { if glassStyle != oldValue { rebuildEffect() } } }
  @objc public var tint: UIColor? { didSet { if tint != oldValue { rebuildEffect(); updateFill() } } }
  @objc public var interactive = false { didSet { if interactive != oldValue { rebuildEffect() } } }
  // Not named `opaque`: that is `UIView.opaque` (an unavailable-renamed UIKit property).
  @objc public var opaqueFill = false { didSet { updateFill() } }
  @objc public var shapeKind = "capsule" { didSet { updateCorners() } }
  @objc public var cornerRadius: CGFloat = 0 { didSet { updateCorners() } }

  public override init(frame: CGRect) {
    super.init(frame: frame)
    effectView.frame = bounds
    effectView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
    fill.frame = effectView.contentView.bounds
    fill.autoresizingMask = [.flexibleWidth, .flexibleHeight]
    effectView.contentView.addSubview(fill)
    addSubview(effectView)
    rebuildEffect()
    updateFill()
    updateCorners()
    registerForTraitChanges([UITraitUserInterfaceStyle.self]) { (view: LiquidGlassEffectView, _) in view.updateFill() }
  }

  @available(*, unavailable)
  required init?(coder: NSCoder) { fatalError("init(coder:) is not supported") }

  private func rebuildEffect() {
    effectView.effect = GlassMapping.effect(style: glassStyle, tint: tint, interactive: interactive)
  }

  private func updateFill() {
    fill.isHidden = !opaqueFill
    // Resolved here: a dynamic color inside glass content renders translucent and tinted by what is behind.
    fill.backgroundColor = tint ?? UIColor.systemBackground.resolvedColor(with: traitCollection)
  }

  private func updateCorners() {
    let corners = GlassMapping.corners(kind: shapeKind, radius: cornerRadius)
    effectView.cornerConfiguration = corners
    fill.cornerConfiguration = corners
  }
}

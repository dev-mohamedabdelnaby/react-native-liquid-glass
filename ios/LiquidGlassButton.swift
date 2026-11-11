import UIKit

/// A system glass `UIButton` filling the view. Set the props, then call `apply()`.
@objc(LiquidGlassButton)
@MainActor
public final class LiquidGlassButton: UIView {
  @objc public let button = UIButton(type: .system)

  @objc public var title: String?
  @objc public var systemImage: String?
  @objc public var prominent = false
  /// "small", "regular" or "large".
  @objc public var size = "regular"
  @objc public var disabled = false
  @objc public var tint: UIColor?
  /// "clear" picks the clear glass variant; prominent buttons are always regular glass (as on Android).
  @objc public var glassStyle = "regular"
  /// A solid fill instead of glass: the tint, or the system background. Not named `opaque`
  /// (that is `UIView.opaque`).
  @objc public var opaqueFill = false
  /// Falls back to the title when nil.
  @objc public var label: String?
  @objc public var onPress: (() -> Void)?

  public override init(frame: CGRect) {
    super.init(frame: frame)
    button.frame = bounds
    button.autoresizingMask = [.flexibleWidth, .flexibleHeight]
    button.addAction(UIAction { [weak self] _ in self?.onPress?() }, for: .primaryActionTriggered)
    addSubview(button)
    apply()
  }

  @available(*, unavailable)
  required init?(coder: NSCoder) { fatalError("init(coder:) is not supported") }

  /// Rebuilds the button configuration from the props.
  @objc public func apply() {
    var config: UIButton.Configuration
    if opaqueFill {
      config = .filled()
      // The glass configurations are capsules; keep that shape when the fill is solid.
      config.cornerStyle = .capsule
    } else if prominent {
      config = .prominentGlass()
    } else {
      config = glassStyle == "clear" ? .clearGlass() : .glass()
    }
    config.title = title
    config.image = systemImage.flatMap { UIImage(systemName: $0) }
    config.imagePadding = 8
    config.buttonSize = switch size {
    case "small": .small
    case "large": .large
    default: .medium
    }
    if let tint {
      config.baseBackgroundColor = tint
      if !prominent && !opaqueFill {
        // The plain glass configurations ignore baseBackgroundColor; tint their glass effect instead.
        config.background.visualEffect = GlassMapping.effect(style: glassStyle, tint: tint, interactive: true)
      }
    } else if opaqueFill && !prominent {
      // A prominent button keeps `.filled()`'s accent fill and white label.
      config.baseBackgroundColor = .systemBackground
      config.baseForegroundColor = .label
    }
    button.configuration = config
    button.isEnabled = !disabled
    button.accessibilityLabel = label
  }
}

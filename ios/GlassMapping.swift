import UIKit

/// Pure mapping from the JS-resolved glass props to UIKit values.
@MainActor
enum GlassMapping {
  static func effect(style: String, tint: UIColor?, interactive: Bool) -> UIGlassEffect {
    let effect = UIGlassEffect(style: style == "clear" ? .clear : .regular)
    effect.tintColor = tint
    effect.isInteractive = interactive
    return effect
  }

  /// A circle is a capsule: on a square view the two are the same shape.
  static func corners(kind: String, radius: CGFloat) -> UICornerConfiguration {
    kind == "rect" ? .uniformCorners(radius: .fixed(Double(radius))) : .capsule()
  }
}

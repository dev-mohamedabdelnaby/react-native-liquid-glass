import SwiftUI
import UIKit

/// The `font*` props: each weight's face, as React Native's `fontFamily` names it; `""` is not set.
struct GlassFontSpec: Equatable {
  var regular = ""
  var medium = ""
  var semibold = ""
  var bold = ""
  var heavy = ""

  /// The face for `weight`; a missing weight takes the regular face (or, without one, a face that
  /// is set). Nil when no face is set: the system font.
  func face(for weight: Font.Weight) -> String? {
    let own = switch weight {
    case .medium: medium
    case .semibold: semibold
    case .bold: bold
    case .heavy, .black: heavy
    default: regular
    }
    return [own, regular, medium, semibold, bold, heavy].first { !$0.isEmpty }
  }

  /// One name for every weight is a family, whose member iOS picks by weight (`fontFamily` +
  /// `fontWeight` in React Native); a weight map's faces are fonts of their own.
  var isFamily: Bool { Set([regular, medium, semibold, bold, heavy]).count == 1 }

  /// `style` (at its own weight unless `weight` is given) in the custom face, scaled by Dynamic Type
  /// as the style is. Nil without a face.
  func font(_ style: Font.TextStyle, weight: Font.Weight? = nil) -> Font? {
    let weight = weight ?? Self.weight(of: style)
    guard let face = face(for: weight) else { return nil }
    let font = Font.custom(face, size: Self.size(of: style), relativeTo: style)
    return isFamily ? font.weight(weight) : font
  }

  /// A fixed size, as `.system(size:weight:)` is (no Dynamic Type). Nil without a face.
  func font(size: CGFloat, weight: Font.Weight) -> Font? {
    guard let face = face(for: weight) else { return nil }
    let font = Font.custom(face, fixedSize: size)
    return isFamily ? font.weight(weight) : font
  }

  /// The UIKit font for the appearance proxies: fixed size, as UIKit's own bar fonts are, or scaled
  /// by Dynamic Type as `style` is. Nil without a face.
  func uiFont(size: CGFloat, weight: Font.Weight, relativeTo style: UIFont.TextStyle? = nil) -> UIFont? {
    guard let face = face(for: weight) else { return nil }
    let font = isFamily
      ? UIFont(descriptor: UIFontDescriptor(fontAttributes: [.family: face, .traits: [UIFontDescriptor.TraitKey.weight: Self.uiWeight(weight)]]), size: size)
      : UIFont(name: face, size: size)
    guard let font, let style else { return font }
    return UIFontMetrics(forTextStyle: style).scaledFont(for: font)
  }

  /// A text style's size at the default content size (Dynamic Type scales from there).
  static func size(of style: Font.TextStyle) -> CGFloat {
    UIFont.preferredFont(forTextStyle: uiStyle(style), compatibleWith: UITraitCollection(preferredContentSizeCategory: .large)).pointSize
  }

  /// A text style's own weight: headline is semibold, the others regular.
  static func weight(of style: Font.TextStyle) -> Font.Weight { style == .headline ? .semibold : .regular }

  private static func uiStyle(_ style: Font.TextStyle) -> UIFont.TextStyle {
    switch style {
    case .largeTitle: .largeTitle
    case .title: .title1
    case .title2: .title2
    case .title3: .title3
    case .headline: .headline
    case .subheadline: .subheadline
    case .callout: .callout
    case .footnote: .footnote
    case .caption: .caption1
    case .caption2: .caption2
    default: .body
    }
  }

  private static func uiWeight(_ weight: Font.Weight) -> UIFont.Weight {
    switch weight {
    case .medium: .medium
    case .semibold: .semibold
    case .bold: .bold
    case .heavy: .heavy
    case .black: .black
    default: .regular
    }
  }
}

extension EnvironmentValues {
  /// The screens' custom font; empty is the system font. Set by the host.
  @Entry var glassFont = GlassFontSpec()
}

extension View {
  /// `.font(.style)` (or `.style.weight(weight)`) in the custom font when one is set: same size,
  /// weight and Dynamic Type. Without one, exactly the system font as before.
  func glassFont(_ style: Font.TextStyle, weight: Font.Weight? = nil) -> some View {
    // `.bold()` is the catalog's own spelling of bold, and it draws lighter than `.weight(.bold)`.
    let system: Font = weight == .bold ? .system(style).bold() : weight.map { .system(style).weight($0) } ?? .system(style)
    return modifier(GlassFontModifier(system: system) {
      $0.font(style, weight: weight)
    })
  }

  /// `.font(.system(size:weight:design:))` in the custom font when one is set (the design is the system font's).
  func glassFont(size: CGFloat, weight: Font.Weight = .regular, design: Font.Design? = nil) -> some View {
    modifier(GlassFontModifier(system: .system(size: size, weight: weight, design: design)) {
      $0.font(size: size, weight: weight)
    })
  }

  /// The custom font in `style` when one is set; without one nothing changes, so the text keeps
  /// the style the system gives that place (list section headers and footers).
  func glassFontOnly(_ style: Font.TextStyle) -> some View {
    modifier(GlassFontModifier(system: nil) { $0.font(style) })
  }
}

private struct GlassFontModifier: ViewModifier {
  let system: Font?
  let custom: (GlassFontSpec) -> Font?
  @Environment(\.glassFont) private var spec

  func body(content: Content) -> some View { content.font(custom(spec) ?? system) }
}

/// The bars and controls UIKit draws for the screens (navigation and large titles, bar buttons, tab
/// bar items, segmented controls, search fields), through appearance proxies scoped to
/// `GlassFontHostingController`: a screen's controller is one only while the screen has a font, so
/// a screen without one (even one mounted after a screen with one) keeps UIKit's own look, untouched.
/// shortcut: the bars of sheets and covers a screen presents are outside its controller and keep the
/// system font (styling them would need each presented bar styled directly).
/// shortcut: proxies are per class, so screens with fonts mounted together share the last font set,
/// and bars already on screen keep theirs until they are created again; per-screen fonts would need
/// each bar styled directly.
enum GlassFontAppearance {
  private static var applied = GlassFontSpec()

  /// Gives `controller` the font's bars, or (no face) UIKit's own.
  static func apply(_ spec: GlassFontSpec, to controller: GlassHostingController) {
    let hasFont = spec.face(for: .regular) != nil
    object_setClass(controller, hasFont ? GlassFontHostingController.self : GlassHostingController.self)
    guard hasFont, spec != applied else { return }
    applied = spec
    let inHost = [GlassFontHostingController.self]
    // UIKit's own size and weight for each (iOS 27); only the large title and the search field scale with Dynamic Type.
    func attributes(_ size: CGFloat, _ weight: Font.Weight, _ style: UIFont.TextStyle? = nil) -> [NSAttributedString.Key: Any]? {
      spec.uiFont(size: size, weight: weight, relativeTo: style).map { [.font: $0] }
    }
    let navigationBar = UINavigationBar.appearance(whenContainedInInstancesOf: inHost)
    navigationBar.titleTextAttributes = attributes(17, .semibold)
    navigationBar.largeTitleTextAttributes = attributes(34, .bold, .largeTitle)
    let barButton = UIBarButtonItem.appearance(whenContainedInInstancesOf: inHost)
    for state in [UIControl.State.normal, .highlighted, .disabled] {
      barButton.setTitleTextAttributes(attributes(17, .medium), for: state)
    }
    // Tab bars take item fonts only from their appearance: UIKit's default one, with the fonts set.
    let tabs = UITabBar().standardAppearance
    for item in [tabs.stackedLayoutAppearance, tabs.inlineLayoutAppearance, tabs.compactInlineLayoutAppearance] {
      item.normal.titleTextAttributes = attributes(10, .medium) ?? [:]
      item.selected.titleTextAttributes = attributes(10, .semibold) ?? [:]
    }
    UITabBar.appearance(whenContainedInInstancesOf: inHost).standardAppearance = tabs
    let segmented = UISegmentedControl.appearance(whenContainedInInstancesOf: inHost)
    segmented.setTitleTextAttributes(attributes(13, .regular), for: .normal)
    segmented.setTitleTextAttributes(attributes(13, .medium), for: .selected)
    // UIKit's own text attributes (colour, paragraph style) with the font.
    if let font = spec.uiFont(size: 16, weight: .medium, relativeTo: .body) {
      UISearchTextField.appearance(whenContainedInInstancesOf: inHost).defaultTextAttributes =
        UISearchTextField().defaultTextAttributes.merging([.font: font]) { $1 }
    }
  }
}

/// The screens' hosting controller: its own class, so the library's settings reach only its screens.
class GlassHostingController: UIHostingController<LiquidGlassScreenRoot> {}

/// The hosting controller of a screen with a font (the same object, its class swapped while the
/// font is set): the appearance proxies reach only these.
final class GlassFontHostingController: GlassHostingController {}

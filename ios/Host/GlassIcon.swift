import CoreText
import SwiftDraw
import SwiftUI
import UIKit

/// One entry of the `icons` prop.
enum GlassIconSource: Hashable {
  case glyph(String, fontFamily: String)
  case svg(String)
  case uri(String)

  /// The `icons` prop: JSON of SF Symbol name → `{glyph, fontFamily}` | `{svg}` | `{uri}`. Bad JSON
  /// or entries are left out.
  static func parse(_ json: String) -> [String: GlassIconSource] {
    guard let data = json.data(using: .utf8),
          let all = (try? JSONSerialization.jsonObject(with: data)) as? [String: Any]
    else { return [:] }
    return all.compactMapValues { value in
      guard let o = value as? [String: String] else { return nil }
      if let glyph = o["glyph"], let family = o["fontFamily"] { return .glyph(glyph, fontFamily: family) }
      if let svg = o["svg"] { return .svg(svg) }
      if let uri = o["uri"] { return .uri(uri) }
      return nil
    }
  }

  /// Glyphs and SVGs are drawn at this size (points, at the screen's scale) and scaled down to fit.
  static let drawSize: CGFloat = 64

  /// The icon's ink, tight, as a template image: drawn at once for a glyph or an SVG; nil for an
  /// image (it loads) or when it cannot be drawn (a font the app did not add, bad SVG).
  func drawn() -> UIImage? {
    switch self {
    case let .glyph(glyph, family): Self.glyphImage(glyph, fontFamily: family)
    case let .svg(xml): Self.svgImage(xml)
    case .uri: nil
    }
  }

  private static func svgImage(_ xml: String) -> UIImage? {
    guard let svg = SVG(xml: xml), svg.size.width > 0, svg.size.height > 0 else { return nil }
    let scale = drawSize / max(svg.size.width, svg.size.height)
    return svg.rasterize(size: CGSize(width: svg.size.width * scale, height: svg.size.height * scale))
      .withRenderingMode(.alwaysTemplate)
  }

  private static func glyphImage(_ glyph: String, fontFamily: String) -> UIImage? {
    guard let font = UIFont(name: fontFamily, size: drawSize) else { return nil }
    let ctFont = font as CTFont
    let chars = Array(glyph.utf16)
    var glyphs = [CGGlyph](repeating: 0, count: chars.count)
    guard CTFontGetGlyphsForCharacters(ctFont, chars, &glyphs, chars.count), let id = glyphs.first,
          let path = CTFontCreatePathForGlyph(ctFont, id, nil)
    else { return nil }
    let ink = path.boundingBoxOfPath
    guard ink.width > 0, ink.height > 0 else { return nil }
    return UIGraphicsImageRenderer(size: ink.size).image { context in
      let cg = context.cgContext
      // Glyph space is y-up from the baseline.
      cg.translateBy(x: -ink.minX, y: ink.maxY)
      cg.scaleBy(x: 1, y: -1)
      cg.addPath(path)
      cg.fillPath()
    }.withRenderingMode(.alwaysTemplate)
  }

  /// An image `uri` as React Native resolves it: a URL (Metro, remote), a file, or a bundle path.
  static func loadImage(_ uri: String) async -> UIImage? {
    guard let url = URL(string: uri), url.scheme != nil else { return UIImage(contentsOfFile: uri) }
    if url.isFileURL { return UIImage(contentsOfFile: url.path) }
    guard let (data, _) = try? await URLSession.shared.data(from: url) else { return nil }
    return UIImage(data: data)
  }
}

/// The screen's custom icons, by SF Symbol name, as template images.
struct GlassIcons: Equatable {
  var images: [String: UIImage] = [:]

  subscript(name: String) -> UIImage? { images[name] }

  /// For places UIKit draws (menus, toolbar and tab bar items), which take a plain image: the icon at
  /// the size UIKit gives the SF Symbol `name` at the body text style, and no smaller than a square
  /// symbol (an icon fitted to a flat one, such as an ellipsis, would be a dot). Nil when the app has none.
  func glassUIImage(_ name: String) -> UIImage? {
    guard let image = images[name] else { return nil }
    let body = UIImage.SymbolConfiguration(textStyle: .body)
    let symbol = UIImage(systemName: name, withConfiguration: body)?.size ?? .zero
    let square = UIImage(systemName: "square", withConfiguration: body)?.size ?? CGSize(width: 22, height: 22)
    let box = CGSize(width: max(symbol.width, square.width), height: max(symbol.height, square.height))
    return UIGraphicsImageRenderer(size: box).image { _ in
      image.draw(in: Self.fit(image.size, in: box))
    }.withRenderingMode(.alwaysTemplate)
  }

  /// [size] scaled to fit [box], centred.
  static func fit(_ size: CGSize, in box: CGSize) -> CGRect {
    let scale = min(box.width / size.width, box.height / size.height)
    let fitted = CGSize(width: size.width * scale, height: size.height * scale)
    return CGRect(x: (box.width - fitted.width) / 2, y: (box.height - fitted.height) / 2, width: fitted.width, height: fitted.height)
  }
}

extension EnvironmentValues {
  /// The screen's custom icons. Set by the host.
  @Entry var glassIcons = GlassIcons()
}

/// `Image(systemName:)`, or the app's icon for that SF Symbol: in the symbol's place and size (its
/// frame under the current font, no smaller than a square symbol's) and tint, drawn as a template. With `uiKit`, a plain image for
/// places UIKit draws (menus, toolbar and tab bar items), which read only an `Image`.
struct GlassImage: View {
  let name: String
  var uiKit = false
  @Environment(\.glassIcons) private var icons

  init(_ name: String, uiKit: Bool = false) {
    self.name = name
    self.uiKit = uiKit
  }

  var body: some View {
    if uiKit, let image = icons.glassUIImage(name) {
      Image(uiImage: image)
    } else if !uiKit, let image = icons[name] {
      // The square keeps an icon in a flat symbol's place (an ellipsis) from shrinking to a dot.
      ZStack { Image(systemName: name); Image(systemName: "square") }.hidden().overlay {
        Image(uiImage: image).renderingMode(.template).resizable().scaledToFit()
      }
    } else {
      Image(systemName: name)
    }
  }
}

// `Button("Title", systemImage:)` and friends with the app's icon for that symbol.
extension Label where Title == Text, Icon == GlassImage {
  init(_ titleKey: LocalizedStringKey, glassImage name: String, uiKit: Bool = false) {
    self.init { Text(titleKey) } icon: { GlassImage(name, uiKit: uiKit) }
  }

  @_disfavoredOverload
  init(_ title: some StringProtocol, glassImage name: String, uiKit: Bool = false) {
    self.init { Text(title) } icon: { GlassImage(name, uiKit: uiKit) }
  }
}

extension Button where Label == SwiftUI.Label<Text, GlassImage> {
  init(_ titleKey: LocalizedStringKey, glassImage name: String, uiKit: Bool = false, role: ButtonRole? = nil, action: @escaping () -> Void) {
    self.init(role: role, action: action) { SwiftUI.Label(titleKey, glassImage: name, uiKit: uiKit) }
  }

  @_disfavoredOverload
  init(_ title: some StringProtocol, glassImage name: String, uiKit: Bool = false, role: ButtonRole? = nil, action: @escaping () -> Void) {
    self.init(role: role, action: action) { SwiftUI.Label(title, glassImage: name, uiKit: uiKit) }
  }
}

extension Toggle where Label == SwiftUI.Label<Text, GlassImage> {
  init(_ titleKey: LocalizedStringKey, glassImage name: String, uiKit: Bool = false, isOn: Binding<Bool>) {
    self.init(isOn: isOn) { SwiftUI.Label(titleKey, glassImage: name, uiKit: uiKit) }
  }
}

extension Menu where Label == SwiftUI.Label<Text, GlassImage> {
  init(_ titleKey: LocalizedStringKey, glassImage name: String, @ViewBuilder content: () -> Content) {
    self.init(content: content) { SwiftUI.Label(titleKey, glassImage: name, uiKit: true) }
  }
}

import SwiftUI
import XCTest

@testable import ReactNativeLiquidGlass

/// The `icons` prop as icons, drawn in the place and size of the SF Symbol they replace.
@MainActor
final class GlassIconTests: XCTestCase {
  func testJSONGivesEachKind() {
    let sources = GlassIconSource.parse(
      #"{"heart":{"glyph":"A","fontFamily":"Helvetica"},"magnifyingglass":{"svg":"<svg/>"},"plus":{"uri":"file:///plus.png"}}"#)
    XCTAssertEqual(sources, [
      "heart": .glyph("A", fontFamily: "Helvetica"),
      "magnifyingglass": .svg("<svg/>"),
      "plus": .uri("file:///plus.png"),
    ])
  }

  func testBadJSONAndEntriesHaveNoIcon() {
    XCTAssertEqual(GlassIconSource.parse(""), [:])
    XCTAssertEqual(GlassIconSource.parse("not json"), [:])
    XCTAssertEqual(GlassIconSource.parse(#"{"a":{"size":3},"b":4}"#), [:])
  }

  func testGlyphsAndSVGsDrawAsTemplates() throws {
    let glyph = try XCTUnwrap(GlassIconSource.glyph("A", fontFamily: "Helvetica").drawn())
    XCTAssertEqual(glyph.renderingMode, .alwaysTemplate)
    let svg = try XCTUnwrap(GlassIconSource.svg(#"<svg viewBox="0 0 10 20" xmlns="http://www.w3.org/2000/svg"><rect width="10" height="20"/></svg>"#).drawn())
    XCTAssertEqual(svg.renderingMode, .alwaysTemplate)
    // Fitted to the draw size, its shape kept.
    XCTAssertEqual(svg.size.height, GlassIconSource.drawSize, accuracy: 0.5)
    XCTAssertEqual(svg.size.width, GlassIconSource.drawSize / 2, accuracy: 0.5)
  }

  // A font the app did not add, or a glyph it lacks, leaves the native icon.
  func testUndrawableIconsHaveNoImage() {
    XCTAssertNil(GlassIconSource.glyph("A", fontFamily: "NoSuchIconFont").drawn())
    XCTAssertNil(GlassIconSource.svg("not svg").drawn())
    XCTAssertNil(GlassIconSource.uri("file:///plus.png").drawn())
  }

  func testAGlyphTakesTheSymbolsSize() throws {
    let glyph = try XCTUnwrap(GlassIconSource.glyph("A", fontFamily: "Helvetica").drawn())
    let icons = GlassIcons(images: ["heart": glyph])
    // UIKit places: the size UIKit gives the symbol, or a square symbol where that is bigger.
    let body = UIImage.SymbolConfiguration(textStyle: .body)
    let symbol = try XCTUnwrap(UIImage(systemName: "heart", withConfiguration: body)).size
    let square = try XCTUnwrap(UIImage(systemName: "square", withConfiguration: body)).size
    XCTAssertEqual(try XCTUnwrap(icons.glassUIImage("heart")).size, CGSize(width: max(symbol.width, square.width), height: max(symbol.height, square.height)))
    // SwiftUI places: the symbol's frame under the place's font, the same way.
    for font in [Font.body, .largeTitle, .system(size: 48)] {
      let expected = ZStack { Image(systemName: "heart"); Image(systemName: "square") }
      XCTAssertEqual(size(of: GlassImage("heart"), font: font, icons: icons), size(of: expected, font: font, icons: icons))
    }
  }

  // A flat symbol (an ellipsis) leaves an icon no shorter than a square symbol: an icon fitted to
  // the ellipsis alone would be a dot.
  func testAnIconInAFlatSymbolsPlaceIsNotADot() throws {
    let glyph = try XCTUnwrap(GlassIconSource.glyph("A", fontFamily: "Helvetica").drawn())
    let icons = GlassIcons(images: ["ellipsis": glyph])
    let square = try XCTUnwrap(UIImage(systemName: "square", withConfiguration: UIImage.SymbolConfiguration(textStyle: .body)))
    XCTAssertGreaterThanOrEqual(try XCTUnwrap(icons.glassUIImage("ellipsis")).size.height, square.size.height)
    XCTAssertGreaterThanOrEqual(
      size(of: GlassImage("ellipsis"), font: .body, icons: icons).height,
      size(of: Image(systemName: "square"), font: .body, icons: icons).height)
  }

  func testAnUnknownNameIsTheNativeSymbol() {
    let icons = GlassIcons(images: ["heart": UIImage()])
    XCTAssertNil(icons["trash"])
    XCTAssertNil(icons.glassUIImage("trash"))
    XCTAssertEqual(size(of: GlassImage("trash"), font: .title, icons: icons), size(of: Image(systemName: "trash"), font: .title, icons: icons))
  }

  // MARK: RTL

  /// Opaque only on its left half: drawn, its left side is ink; mirrored, its right side.
  private func leftHalf() -> UIImage {
    UIGraphicsImageRenderer(size: CGSize(width: 20, height: 20)).image { _ in
      UIRectFill(CGRect(x: 0, y: 0, width: 10, height: 20))
    }.withRenderingMode(.alwaysTemplate)
  }

  /// Whether the left (or right) quarter of the rendered view has ink.
  private func ink(of view: some View, icons: GlassIcons, rtl: Bool) throws -> (left: Bool, right: Bool) {
    let renderer = ImageRenderer(content: view.font(.system(size: 48)).foregroundStyle(.red)
      .environment(\.glassIcons, icons).environment(\.layoutDirection, rtl ? .rightToLeft : .leftToRight))
    renderer.scale = 1
    let cg = try XCTUnwrap(renderer.cgImage)
    let w = cg.width, h = cg.height
    var px = [UInt8](repeating: 0, count: w * h * 4)
    let ctx = CGContext(data: &px, width: w, height: h, bitsPerComponent: 8, bytesPerRow: w * 4,
                        space: CGColorSpaceCreateDeviceRGB(), bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue)!
    ctx.draw(cg, in: CGRect(x: 0, y: 0, width: w, height: h))
    func alpha(_ x: Int) -> UInt8 { px[((h / 2) * w + x) * 4 + 3] }
    return (alpha(w / 4) > 128, alpha(w * 3 / 4) > 128)
  }

  // A custom icon for a symbol the system mirrors in RTL (chevron.backward) mirrors too; one for a
  // symbol it does not (heart) keeps its side. In SwiftUI places and UIKit places alike.
  func testACustomIconMirrorsInRTLWhereTheSymbolDoes() throws {
    let icons = GlassIcons(images: ["chevron.backward": leftHalf(), "heart": leftHalf()])
    for uiKit in [false, true] {
      let ltr = try ink(of: GlassImage("chevron.backward", uiKit: uiKit), icons: icons, rtl: false)
      XCTAssertTrue(ltr.left && !ltr.right, "LTR uiKit \(uiKit)")
      let rtl = try ink(of: GlassImage("chevron.backward", uiKit: uiKit), icons: icons, rtl: true)
      XCTAssertTrue(!rtl.left && rtl.right, "RTL uiKit \(uiKit)")
      let heart = try ink(of: GlassImage("heart", uiKit: uiKit), icons: icons, rtl: true)
      XCTAssertTrue(heart.left && !heart.right, "heart RTL uiKit \(uiKit)")
    }
    // UIKit places (bar button items) flip a flagged image themselves.
    XCTAssertTrue(try XCTUnwrap(icons.glassUIImage("chevron.backward")).flipsForRightToLeftLayoutDirection)
    XCTAssertFalse(try XCTUnwrap(icons.glassUIImage("heart")).flipsForRightToLeftLayoutDirection)
  }

  // MARK: VoiceOver

  /// The VoiceOver labels in the window's accessibility tree.
  private func labels(_ view: some View, icons: GlassIcons) -> [String] {
    // SwiftUI builds its accessibility tree only for an assistive client: switch on the one UI tests use.
    if let lib = dlopen("/usr/lib/libAccessibility.dylib", RTLD_NOW), let set = dlsym(lib, "_AXSSetAutomationEnabled") {
      unsafeBitCast(set, to: (@convention(c) (Int32) -> Void).self)(1)
    }
    let window = UIWindow(frame: CGRect(x: 0, y: 0, width: 400, height: 800))
    window.rootViewController = UIHostingController(rootView: view.environment(\.glassIcons, icons))
    window.makeKeyAndVisible()
    window.layoutIfNeeded()
    RunLoop.main.run(until: Date().addingTimeInterval(0.5))
    var out: [String] = []
    func walk(_ e: NSObject) {
      if e.isAccessibilityElement, let label = e.accessibilityLabel, !label.isEmpty { out.append(label) }
      if let elements = e.accessibilityElements as? [NSObject] { elements.forEach(walk) }
      else if let view = e as? UIView { view.subviews.forEach(walk) }
    }
    walk(window)
    window.isHidden = true
    return out
  }

  // An icon-only button keeps the label its SF Symbol gave it once the icon is custom, in a
  // SwiftUI place and in a toolbar (a UIKit place).
  func testACustomIconKeepsTheSymbolsVoiceOverLabel() throws {
    let custom = GlassIcons(images: ["play.fill": leftHalf(), "heart": leftHalf()])
    let button = Button {} label: { GlassImage("play.fill") }
    let native = labels(button, icons: GlassIcons())
    XCTAssertFalse(native.isEmpty)
    XCTAssertEqual(labels(button, icons: custom), native)
    let toolbar = NavigationStack {
      Text("Body").toolbar { ToolbarItem(placement: .topBarTrailing) { Button {} label: { GlassImage("heart", uiKit: true) } } }
    }
    let nativeBar = labels(toolbar, icons: GlassIcons())
    XCTAssertTrue(nativeBar.count > 1, "\(nativeBar)")
    XCTAssertEqual(labels(toolbar, icons: custom), nativeBar)
  }

  private func size(of view: some View, font: Font, icons: GlassIcons) -> CGSize {
    UIHostingController(rootView: view.font(font).environment(\.glassIcons, icons)).sizeThatFits(in: CGSize(width: 1000, height: 1000))
  }
}

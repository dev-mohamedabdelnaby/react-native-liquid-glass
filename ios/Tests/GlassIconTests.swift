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

  private func size(of view: some View, font: Font, icons: GlassIcons) -> CGSize {
    UIHostingController(rootView: view.font(font).environment(\.glassIcons, icons)).sizeThatFits(in: CGSize(width: 1000, height: 1000))
  }
}

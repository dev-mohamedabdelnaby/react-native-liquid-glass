import UIKit
import XCTest

@testable import ReactNativeLiquidGlass

@MainActor
final class GlassMappingTests: XCTestCase {
  func testEffect() {
    let e = GlassMapping.effect(style: "regular", tint: .red, interactive: true)
    XCTAssertTrue(e.isInteractive)
    XCTAssertEqual(e.tintColor, .red)
  }

  func testClearStyleHasNoTint() {
    let e = GlassMapping.effect(style: "clear", tint: nil, interactive: false)
    XCTAssertFalse(e.isInteractive)
    XCTAssertNil(e.tintColor)
  }

  func testCapsuleAndRect() {
    let v = UIView(frame: CGRect(x: 0, y: 0, width: 200, height: 100))
    v.cornerConfiguration = GlassMapping.corners(kind: "capsule", radius: 0)
    XCTAssertEqual(v.effectiveRadius(corner: .topLeft), 50, accuracy: 0.5)
    v.cornerConfiguration = GlassMapping.corners(kind: "rect", radius: 12)
    XCTAssertEqual(v.effectiveRadius(corner: .topLeft), 12, accuracy: 0.5)
  }

  func testCircleOnSquareIsFullyRound() {
    let v = UIView(frame: CGRect(x: 0, y: 0, width: 80, height: 80))
    v.cornerConfiguration = GlassMapping.corners(kind: "circle", radius: 0)
    XCTAssertEqual(v.effectiveRadius(corner: .topLeft), 40, accuracy: 0.5)
  }

  func testOpaqueFillsWithTint() throws {
    let g = LiquidGlassEffectView()
    g.tint = .red
    let fill = try XCTUnwrap(g.contentView.subviews.first)
    XCTAssertTrue(fill.isHidden)
    g.opaqueFill = true
    XCTAssertFalse(fill.isHidden)
    XCTAssertEqual(fill.backgroundColor, .red)
    g.tint = nil
    XCTAssertEqual(fill.backgroundColor, UIColor.systemBackground.resolvedColor(with: g.traitCollection))
    g.opaqueFill = false
    XCTAssertTrue(fill.isHidden)
  }

  func testEffectIsRebuiltWhenPropsChange() throws {
    let g = LiquidGlassEffectView()
    g.glassStyle = "clear"
    g.tint = .blue
    g.interactive = true
    let effect = try XCTUnwrap(g.effectView.effect as? UIGlassEffect)
    XCTAssertTrue(effect.isInteractive)
    XCTAssertEqual(effect.tintColor, .blue)
  }
}

import UIKit
import XCTest

@testable import ReactNativeLiquidGlass

@MainActor
final class GlassButtonTests: XCTestCase {
  func testConfiguration() throws {
    let b = LiquidGlassButton()
    b.prominent = true
    b.title = "Done"
    b.systemImage = "checkmark"
    b.tint = .red
    b.size = "large"
    b.apply()
    let config = try XCTUnwrap(b.button.configuration)
    XCTAssertEqual(config.title, "Done")
    XCTAssertNotNil(config.image)
    XCTAssertEqual(config.baseBackgroundColor, .red)
    XCTAssertEqual(config.buttonSize, .large)
  }

  func testDisabledAndLabel() {
    let b = LiquidGlassButton()
    b.systemImage = "xmark"
    b.disabled = true
    b.label = "Close"
    b.apply()
    XCTAssertFalse(b.button.isEnabled)
    XCTAssertEqual(b.button.accessibilityLabel, "Close")
    b.disabled = false
    b.label = nil
    b.title = "Go"
    b.apply()
    XCTAssertTrue(b.button.isEnabled)
    // nil hands the label back to UIKit, which reads the title.
    XCTAssertNil(b.button.accessibilityLabel)
  }

  func testPrimaryActionCallsOnPress() {
    let b = LiquidGlassButton()
    var presses = 0
    b.onPress = { presses += 1 }
    b.button.sendActions(for: .primaryActionTriggered)
    XCTAssertEqual(presses, 1)
  }

  func testNonProminentTint() throws {
    let b = LiquidGlassButton()
    b.glassStyle = "clear"
    b.tint = .green
    b.apply()
    let effect = try XCTUnwrap(b.button.configuration?.background.visualEffect as? UIGlassEffect)
    XCTAssertEqual(effect.tintColor, .green)
  }

  func testOpaqueIsASolidFill() throws {
    let b = LiquidGlassButton()
    b.opaqueFill = true
    b.title = "Op"
    b.tint = .red
    b.apply()
    var config = try XCTUnwrap(b.button.configuration)
    XCTAssertEqual(config.baseBackgroundColor, .red)
    XCTAssertEqual(config.title, "Op")
    b.tint = nil
    b.apply()
    config = try XCTUnwrap(b.button.configuration)
    XCTAssertEqual(config.baseBackgroundColor, .systemBackground)
    XCTAssertEqual(config.baseForegroundColor, .label)
    XCTAssertEqual(config.cornerStyle, .capsule)
  }

  func testOpaqueProminentKeepsTheAccent() throws {
    let b = LiquidGlassButton()
    b.opaqueFill = true
    b.prominent = true
    b.title = "Go"
    b.apply()
    let config = try XCTUnwrap(b.button.configuration)
    // nil leaves `.filled()` on the accent (tint colour), not the plain background.
    XCTAssertNil(config.baseBackgroundColor)
    XCTAssertNil(config.baseForegroundColor)
    XCTAssertEqual(config.cornerStyle, .capsule)
  }
}

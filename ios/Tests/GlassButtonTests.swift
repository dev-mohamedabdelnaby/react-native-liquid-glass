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
}

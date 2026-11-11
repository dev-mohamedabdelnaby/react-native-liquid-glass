import SwiftUI
import UIKit
import XCTest

@testable import ReactNativeLiquidGlass

@MainActor
final class LiquidGlassScreenHostTests: XCTestCase {
  func testRgba() {
    XCTAssertEqual(LiquidGlassScreenHost.rgba(UIColor(red: 1, green: 0, blue: 0.5, alpha: 0.2)), "#FF008033")
    XCTAssertEqual(LiquidGlassScreenHost.rgba(nil), "")
  }

  // A picked choice reaches JS as a fixed colour and comes back as the prop: Home must show the
  // same choice, in light and in dark mode.
  func testTintChoiceSurvivesTheRoundTrip() {
    let model = LiquidGlassScreenModel()
    for choice in GlassTintChoice.allCases {
      for style in [UIUserInterfaceStyle.light, .dark] {
        model.tint = choice.color.map { UIColor($0).resolvedColor(with: UITraitCollection(userInterfaceStyle: style)) }
        XCTAssertEqual(model.tintChoice, choice, "\(choice) in \(style.rawValue)")
      }
    }
  }

  func testOtherTintsShowNoneAndStillTint() {
    let model = LiquidGlassScreenModel()
    model.tint = UIColor(red: 0.1, green: 0.2, blue: 0.3, alpha: 1)
    XCTAssertEqual(model.tintChoice, .none)
    XCTAssertNotNil(model.glassTint)
  }

  func testPropsMapAndNeverSendSettings() {
    let host = LiquidGlassScreenHost(frame: .zero)
    host.onSettingsChange = { _ in XCTFail("a prop must not send onSettingsChange") }
    host.layoutDirection = "rtl"
    XCTAssertEqual(host.layoutDirection, "rtl")
    host.layoutDirection = "nonsense"
    XCTAssertEqual(host.layoutDirection, "system")
    host.appearance = "dark"
    XCTAssertEqual(host.appearance, "dark")
    host.glassTint = .red
    host.screen = "home"
  }
}

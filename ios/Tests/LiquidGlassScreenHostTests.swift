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

  func testPickingNoneClearsAnyTint() {
    let model = LiquidGlassScreenModel()
    model.tint = UIColor(red: 0.1, green: 0.2, blue: 0.3, alpha: 1)
    XCTAssertTrue(model.pickTint(.none))
    XCTAssertNil(model.tint)
    XCTAssertFalse(model.pickTint(.none))
    XCTAssertTrue(model.pickTint(.blue))
    XCTAssertFalse(model.pickTint(.blue))
  }

  // Only the setters: the bindings that send settings live in the rendered SwiftUI root.
  func testPropSettersMapValuesWithoutSendingSettings() {
    let host = LiquidGlassScreenHost(frame: .zero)
    host.onSettingsChange = { _ in XCTFail("a prop setter must not send onSettingsChange") }
    host.layoutDirection = "rtl"
    XCTAssertEqual(host.layoutDirection, "rtl")
    host.layoutDirection = "nonsense"
    XCTAssertEqual(host.layoutDirection, "system")
    host.appearance = "dark"
    XCTAssertEqual(host.appearance, "dark")
    host.appearance = "nonsense"
    XCTAssertEqual(host.appearance, "system")
    host.glassTint = .red
    XCTAssertEqual(host.glassTint, .red)
    host.screen = "home"
    XCTAssertEqual(host.screen, "home")
  }

  // A logic test has no window scene, so it needs the deprecated UIWindow(frame:); marked
  // deprecated itself so that call does not warn.
  @available(iOS, deprecated: 26.0)
  func testWindowStyleIsSharedAndRestored() {
    let window = UIWindow(frame: CGRect(x: 0, y: 0, width: 100, height: 100))
    window.overrideUserInterfaceStyle = .light  // the app's own choice
    let first = LiquidGlassScreenHost(frame: .zero)
    let second = LiquidGlassScreenHost(frame: .zero)

    window.addSubview(first)
    XCTAssertEqual(window.overrideUserInterfaceStyle, .light, "'system' leaves the window alone")

    first.appearance = "dark"
    second.appearance = "dark"
    window.addSubview(second)
    XCTAssertEqual(window.overrideUserInterfaceStyle, .dark)

    first.removeFromSuperview()
    XCTAssertEqual(window.overrideUserInterfaceStyle, .dark, "the other screen still asks for dark")
    second.removeFromSuperview()
    XCTAssertEqual(window.overrideUserInterfaceStyle, .light, "the last screen gives the style back")

    window.addSubview(first)
    XCTAssertEqual(window.overrideUserInterfaceStyle, .dark)
    first.appearance = "system"
    XCTAssertEqual(window.overrideUserInterfaceStyle, .light, "back to 'system' gives the style back")
    first.removeFromSuperview()
    XCTAssertEqual(window.overrideUserInterfaceStyle, .light)
  }
}

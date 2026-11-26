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

  // Native-stack pushes a screen after mounting it: the controller must be a child by then.
  func testChildFromMountUntilUnmount() {
    let parent = UIViewController()
    let container = UIView()
    let host = LiquidGlassScreenHost(frame: .zero)
    container.addSubview(host)
    host.attachToViewController()
    XCTAssertTrue(parent.children.isEmpty, "no view controller reachable yet")

    parent.view.addSubview(container)
    host.attachToViewController()
    host.attachToViewController()
    XCTAssertEqual(parent.children.count, 1, "a child once mounted, outside any window")

    container.removeFromSuperview()
    host.detachFromViewController()
    XCTAssertTrue(parent.children.isEmpty, "unmounted, the child leaves")
  }

  // The window-style tests need a window, and a logic test has no window scene: they use the
  // deprecated UIWindow(frame:) and are marked deprecated themselves so that call does not warn.

  @available(iOS, deprecated: 26.0)
  func testWindowStyleIsSharedAndRestored() {
    let window = makeWindow(.light)  // the app's own choice
    let first = LiquidGlassScreenHost(frame: .zero)
    let second = LiquidGlassScreenHost(frame: .zero)

    window.addSubview(first)
    XCTAssertEqual(window.overrideUserInterfaceStyle, .light, "'system' leaves the window alone")

    first.appearance = "dark"
    second.appearance = "dark"
    window.addSubview(second)
    XCTAssertEqual(window.overrideUserInterfaceStyle, .dark)

    unmount(first)
    XCTAssertEqual(window.overrideUserInterfaceStyle, .dark, "the other screen still asks for dark")
    unmount(second)
    XCTAssertEqual(window.overrideUserInterfaceStyle, .light, "the last screen gives the style back")

    window.addSubview(first)
    XCTAssertEqual(window.overrideUserInterfaceStyle, .dark)
    first.appearance = "system"
    XCTAssertEqual(window.overrideUserInterfaceStyle, .light, "back to 'system' gives the style back")
    unmount(first)
    XCTAssertEqual(window.overrideUserInterfaceStyle, .light)
  }

  @available(iOS, deprecated: 26.0)
  func testLatestStyleWinsAndReleasesInAnyOrder() {
    let window = makeWindow(.unspecified)
    let dark = LiquidGlassScreenHost(frame: .zero)
    let light = LiquidGlassScreenHost(frame: .zero)
    dark.appearance = "dark"
    light.appearance = "light"

    window.addSubview(dark)
    window.addSubview(light)
    XCTAssertEqual(window.overrideUserInterfaceStyle, .light, "the latest request wins")
    dark.appearance = "dark"
    XCTAssertEqual(window.overrideUserInterfaceStyle, .light, "the same style again keeps its place")
    unmount(light)
    XCTAssertEqual(window.overrideUserInterfaceStyle, .dark)
    unmount(dark)
    XCTAssertEqual(window.overrideUserInterfaceStyle, .unspecified)

    window.addSubview(dark)
    window.addSubview(light)
    unmount(dark)
    XCTAssertEqual(window.overrideUserInterfaceStyle, .light, "releasing the lower request changes nothing")
    unmount(light)
    XCTAssertEqual(window.overrideUserInterfaceStyle, .unspecified)
  }

  // A full-screen cover or a native-stack push takes a mounted screen out of the window.
  @available(iOS, deprecated: 26.0)
  func testLeavingTheWindowKeepsTheRequest() {
    let window = makeWindow(.light)
    let container = UIView()
    let host = LiquidGlassScreenHost(frame: .zero)
    host.appearance = "dark"
    container.addSubview(host)
    window.addSubview(container)
    XCTAssertEqual(window.overrideUserInterfaceStyle, .dark)

    container.removeFromSuperview()
    XCTAssertNil(host.window)
    XCTAssertEqual(window.overrideUserInterfaceStyle, .dark, "still mounted, still dark")
    window.addSubview(container)
    XCTAssertEqual(window.overrideUserInterfaceStyle, .dark)
    unmount(host)
    XCTAssertEqual(window.overrideUserInterfaceStyle, .light)
  }

  @available(iOS, deprecated: 26.0)
  func testMovingToAnotherWindowMovesTheRequest() {
    let first = makeWindow(.light)
    let second = makeWindow(.unspecified)
    let host = LiquidGlassScreenHost(frame: .zero)
    host.appearance = "dark"
    first.addSubview(host)
    XCTAssertEqual(first.overrideUserInterfaceStyle, .dark)

    second.addSubview(host)
    XCTAssertEqual(first.overrideUserInterfaceStyle, .light)
    XCTAssertEqual(second.overrideUserInterfaceStyle, .dark)
    unmount(host)
    XCTAssertEqual(second.overrideUserInterfaceStyle, .unspecified)
  }

  // The window scene takes the style too (React Native Screens reads the status bar style from
  // it) and gets its own back. A logic test has no window scene; a view's overrides are the same
  // type, so the save and restore are checked there (the scene itself on the simulator).
  func testStyleOverrideIsSavedAndRemoved() {
    let view = UIView()
    XCTAssertNil(view.traitOverrides.userInterfaceStyleOverride, "none set")
    view.traitOverrides.userInterfaceStyleOverride = .dark
    XCTAssertEqual(view.traitOverrides.userInterfaceStyleOverride, .dark)
    view.updateTraitsIfNeeded()
    XCTAssertEqual(view.traitCollection.userInterfaceStyle, .dark)
    view.traitOverrides.userInterfaceStyleOverride = .unspecified
    XCTAssertEqual(view.traitOverrides.userInterfaceStyleOverride, .unspecified, "an explicit unspecified is kept")
    view.traitOverrides.userInterfaceStyleOverride = nil
    XCTAssertFalse(view.traitOverrides.contains(UITraitUserInterfaceStyle.self), "nil removes the override")
  }

  @available(iOS, deprecated: 26.0)
  private func makeWindow(_ style: UIUserInterfaceStyle) -> UIWindow {
    let window = UIWindow(frame: CGRect(x: 0, y: 0, width: 100, height: 100))
    window.overrideUserInterfaceStyle = style
    return window
  }

  /// What RNLiquidGlassScreen does when React unmounts it.
  private func unmount(_ host: LiquidGlassScreenHost) {
    host.removeFromSuperview()
    host.releaseWindowStyle()
  }
}

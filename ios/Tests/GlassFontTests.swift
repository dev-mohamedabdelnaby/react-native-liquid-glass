import SwiftUI
import XCTest

@testable import ReactNativeLiquidGlass

/// The `font*` props as the face for each weight; no face is the system font.
@MainActor
final class GlassFontTests: XCTestCase {
  func testEachWeightTakesItsFace() {
    let spec = GlassFontSpec(regular: "R", medium: "M", semibold: "S", bold: "B", heavy: "H")
    XCTAssertEqual(spec.face(for: .regular), "R")
    XCTAssertEqual(spec.face(for: .medium), "M")
    XCTAssertEqual(spec.face(for: .semibold), "S")
    XCTAssertEqual(spec.face(for: .bold), "B")
    XCTAssertEqual(spec.face(for: .heavy), "H")
    XCTAssertEqual(spec.face(for: .black), "H")
    XCTAssertEqual(spec.face(for: .light), "R")
  }

  func testMissingWeightsFallBackToRegular() {
    let spec = GlassFontSpec(regular: "R", bold: "B")
    XCTAssertEqual(spec.face(for: .medium), "R")
    XCTAssertEqual(spec.face(for: .semibold), "R")
    XCTAssertEqual(spec.face(for: .heavy), "R")
    XCTAssertEqual(spec.face(for: .bold), "B")
    // Without a regular face, a face that is set.
    XCTAssertEqual(GlassFontSpec(bold: "B").face(for: .regular), "B")
  }

  func testNoFaceIsTheSystemFont() {
    let spec = GlassFontSpec()
    XCTAssertNil(spec.face(for: .regular))
    XCTAssertNil(spec.font(.body))
    XCTAssertNil(spec.font(size: 20, weight: .bold))
    XCTAssertNil(spec.uiFont(size: 17, weight: .semibold, relativeTo: .headline))
    XCTAssertNotNil(GlassFontSpec(regular: "R").font(.body))
  }

  // One name for every weight is a family (iOS picks its member by weight), as in React Native.
  func testOneNameForEveryWeightIsAFamily() {
    XCTAssertTrue(GlassFontSpec(regular: "C", medium: "C", semibold: "C", bold: "C", heavy: "C").isFamily)
    XCTAssertFalse(GlassFontSpec(regular: "C-Regular", bold: "C-Bold").isFamily)
  }

  // Custom faces keep the text style's own size, which Dynamic Type then scales.
  func testTextStylesKeepTheirDefaultSize() {
    XCTAssertEqual(GlassFontSpec.size(of: .largeTitle), 34)
    XCTAssertEqual(GlassFontSpec.size(of: .body), 17)
    XCTAssertEqual(GlassFontSpec.size(of: .footnote), 13)
    XCTAssertEqual(GlassFontSpec.weight(of: .headline), .semibold)
    XCTAssertEqual(GlassFontSpec.weight(of: .body), .regular)
  }

  func testTheHostSetsTheFacesWithoutSendingSettings() {
    let host = LiquidGlassScreenHost(frame: .zero)
    host.onSettingsChange = { _ in XCTFail("a prop setter must not send onSettingsChange") }
    host.setFonts(regular: "R", medium: "", semibold: "", bold: "B", heavy: "")
    XCTAssertEqual(host.font, GlassFontSpec(regular: "R", bold: "B"))
    host.setFonts(regular: "", medium: "", semibold: "", bold: "", heavy: "")
    XCTAssertEqual(host.font, GlassFontSpec())
  }

  // The bars and controls UIKit draws inside the library's hosting controller take the font.
  func testUIKitBarsAndControlsInTheHostTakeTheFont() {
    GlassFontAppearance.apply(GlassFontSpec(regular: "Georgia", medium: "Georgia", semibold: "Georgia-Bold", bold: "Georgia-Bold", heavy: "Georgia-Bold"))
    defer { GlassFontAppearance.apply(GlassFontSpec()) }
    let screen = UIViewController()
    screen.title = "Title"
    screen.navigationItem.rightBarButtonItem = UIBarButtonItem(title: "Edit", style: .plain, target: nil, action: nil)
    let segmented = UISegmentedControl(items: ["Day", "Week"])
    segmented.selectedSegmentIndex = 0
    segmented.frame = CGRect(x: 0, y: 100, width: 200, height: 32)
    screen.view.addSubview(segmented)
    let navigation = UINavigationController(rootViewController: screen)
    navigation.tabBarItem = UITabBarItem(title: "Library", image: UIImage(systemName: "star"), tag: 0)
    let tabs = UITabBarController()
    tabs.viewControllers = [navigation]
    let host = GlassHostingController(rootView: LiquidGlassScreenRoot(model: LiquidGlassScreenModel()))
    host.addChild(tabs)
    host.view.addSubview(tabs.view)
    tabs.didMove(toParent: host)
    let window = UIWindow(frame: CGRect(x: 0, y: 0, width: 402, height: 874))
    window.rootViewController = host
    window.makeKeyAndVisible()
    tabs.view.frame = window.bounds
    window.layoutIfNeeded()
    func labels(_ view: UIView) -> [UILabel] { ((view as? UILabel).map { [$0] } ?? []) + view.subviews.flatMap(labels) }
    let fonts = Dictionary(labels(window).map { ($0.text ?? "", $0.font.fontName) }, uniquingKeysWith: { a, _ in a })
    for text in ["Title", "Edit", "Library", "Day", "Week"] {
      XCTAssertTrue(fonts[text]?.hasPrefix("Georgia") == true, "\(text): \(fonts[text] ?? "no label")")
    }
  }
}

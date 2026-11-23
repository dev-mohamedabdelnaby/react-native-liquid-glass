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

  private let georgia = GlassFontSpec(regular: "Georgia", medium: "Georgia", semibold: "Georgia-Bold", bold: "Georgia-Bold", heavy: "Georgia-Bold")
  private var windows: [UIWindow] = []

  /// A screen's controller with `spec`, showing a tab bar with a navigation bar (title, bar button,
  /// search field) and a segmented control. Font name per text. (A bare test window does not finish
  /// UIKit's presentations, so presented bars are checked in the example app.)
  private func showBars(_ spec: GlassFontSpec) -> (fonts: [String: String], search: UISearchTextField) {
    let screen = UIViewController()
    screen.title = "Title"
    screen.navigationItem.rightBarButtonItem = UIBarButtonItem(title: "Edit", style: .plain, target: nil, action: nil)
    let search = UISearchController()
    screen.navigationItem.searchController = search
    screen.navigationItem.preferredSearchBarPlacement = .stacked
    let segmented = UISegmentedControl(items: ["Day", "Week"])
    segmented.selectedSegmentIndex = 0
    segmented.frame = CGRect(x: 0, y: 300, width: 200, height: 32)
    screen.view.addSubview(segmented)
    let navigation = UINavigationController(rootViewController: screen)
    navigation.tabBarItem = UITabBarItem(title: "Library", image: UIImage(systemName: "star"), tag: 0)
    let tabs = UITabBarController()
    tabs.viewControllers = [navigation]
    let host = UIViewController()
    GlassFontAppearance.apply(spec, to: host)
    let window = UIWindow(frame: CGRect(x: 0, y: 0, width: 402, height: 874))
    windows.append(window)
    window.rootViewController = host
    window.makeKeyAndVisible()
    host.addChild(tabs)
    host.view.addSubview(tabs.view)
    tabs.didMove(toParent: host)
    tabs.view.frame = window.bounds
    window.layoutIfNeeded()
    func labels(_ view: UIView) -> [UILabel] { ((view as? UILabel).map { [$0] } ?? []) + view.subviews.flatMap(labels) }
    var fonts = Dictionary(labels(tabs.view).map { ($0.text ?? "", $0.font.fontName) }, uniquingKeysWith: { a, _ in a })
    fonts["search"] = search.searchBar.searchTextField.font?.fontName
    return (fonts, search.searchBar.searchTextField)
  }

  override func tearDown() {
    windows.forEach { $0.isHidden = true }
    windows = []
    super.tearDown()
  }

  // The bars and controls UIKit draws for a screen with a font take the font.
  func testUIKitBarsAndControlsOfAScreenWithAFontTakeIt() {
    let (fonts, search) = showBars(georgia)
    for text in ["Title", "Edit", "Library", "Day", "Week", "search"] {
      XCTAssertTrue(fonts[text]?.hasPrefix("Georgia") == true, "\(text): \(fonts[text] ?? "no label")")
    }
    // The search field keeps its own text colour.
    XCTAssertEqual(search.textColor, UISearchTextField().textColor)
  }

  // A screen without a font, mounted after one with a font, keeps UIKit's own bars.
  func testAScreenWithoutAFontKeepsUIKitsBars() {
    _ = showBars(georgia)
    let system = showBars(GlassFontSpec()).fonts
    for text in ["Title", "Edit", "Library", "Day", "Week", "search"] {
      XCTAssertFalse(system[text]?.hasPrefix("Georgia") ?? true, "\(text): \(system[text] ?? "no label")")
    }
  }

  // Each screen's bars take its own font.
  func testScreensWithDifferentFontsKeepTheirOwn() {
    let courier = GlassFontSpec(regular: "Courier", medium: "Courier", semibold: "Courier-Bold", bold: "Courier-Bold", heavy: "Courier-Bold")
    let first = showBars(georgia).fonts
    let second = showBars(courier).fonts
    XCTAssertTrue(first["Title"]?.hasPrefix("Georgia") == true, first["Title"] ?? "no label")
    XCTAssertTrue(second["Title"]?.hasPrefix("Courier") == true, second["Title"] ?? "no label")
  }

  // Sheets and covers do not inherit the presenter's traits; the screen hands them its font.
  func testWhatAScreenPresentsTakesItsFont() {
    let host = GlassHostingController(rootView: LiquidGlassScreenRoot(model: LiquidGlassScreenModel()))
    GlassFontAppearance.apply(georgia, to: host)
    let cover = UIViewController()
    host.present(cover, animated: false)
    XCTAssertEqual(cover.traitOverrides[GlassFontTrait.self], georgia)
  }

  func testUnsettingTheFontRemovesTheTrait() {
    let host = UIViewController()
    GlassFontAppearance.apply(georgia, to: host)
    host.updateTraitsIfNeeded()
    XCTAssertEqual(host.traitCollection[GlassFontTrait.self], georgia)
    GlassFontAppearance.apply(GlassFontSpec(), to: host)
    host.updateTraitsIfNeeded()
    XCTAssertNil(host.traitCollection[GlassFontTrait.self])
  }

  // Bars keep UIKit's fixed sizes; only the large title and the search field scale (as UIKit's do).
  func testBarFontsHaveFixedSizes() {
    XCTAssertEqual(georgia.uiFont(size: 10, weight: .medium), UIFont(name: "Georgia", size: 10))
    XCTAssertEqual(georgia.uiFont(size: 17, weight: .semibold), UIFont(name: "Georgia-Bold", size: 17))
  }
}

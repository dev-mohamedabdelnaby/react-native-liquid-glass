import XCTest

@testable import ReactNativeLiquidGlass

@MainActor
final class ScreenIdsTests: XCTestCase {
  // The JS screen ids (src/screens.tsx), in home order.
  func testEveryScreenIdIsAGlassComponent() {
    let ids = ["glassEffect", "glassView", "interactive", "buttons", "container", "morphing", "union", "toolbar", "floatingHeader", "tabBar", "sheets", "controls", "text", "transitions", "search", "editSearch", "scrollEdge", "splitView", "zoomTransitions", "alerts", "recipes"]
    XCTAssertEqual(GlassComponent.allCases.map(\.rawValue), ids)
  }
}

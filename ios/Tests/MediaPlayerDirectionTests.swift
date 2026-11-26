import SwiftUI
import XCTest

@testable import ReactNativeLiquidGlass

/// Media players follow time, not reading direction: in an RTL screen they keep their LTR layout (HIG).
@MainActor
final class MediaPlayerDirectionTests: XCTestCase {
  /// Each VoiceOver label's frame on screen, for the view hosted in an RTL window.
  private func frames(_ view: some View) -> [String: CGRect] {
    // SwiftUI builds its accessibility tree only for an assistive client: switch on the one UI tests use.
    if let lib = dlopen("/usr/lib/libAccessibility.dylib", RTLD_NOW), let set = dlsym(lib, "_AXSSetAutomationEnabled") {
      unsafeBitCast(set, to: (@convention(c) (Int32) -> Void).self)(1)
    }
    let window = UIWindow(frame: CGRect(x: 0, y: 0, width: 400, height: 2000))
    window.rootViewController = UIHostingController(rootView: view.environment(\.layoutDirection, .rightToLeft))
    window.makeKeyAndVisible()
    window.layoutIfNeeded()
    RunLoop.main.run(until: Date().addingTimeInterval(0.5))
    var out: [String: CGRect] = [:]
    func walk(_ e: NSObject) {
      if e.isAccessibilityElement, let label = e.accessibilityLabel, !label.isEmpty, out[label] == nil { out[label] = e.accessibilityFrame }
      if let elements = e.accessibilityElements as? [NSObject] { elements.forEach(walk) }
      else if let view = e as? UIView { view.subviews.forEach(walk) }
    }
    walk(window)
    window.isHidden = true
    return out
  }

  private func assertLeft(_ a: String, of b: String, in frames: [String: CGRect], file: StaticString = #filePath, line: UInt = #line) {
    guard let left = frames[a], let right = frames[b] else { return XCTFail("no \(a) or \(b) in \(frames.keys.sorted())", file: file, line: line) }
    XCTAssertLessThan(left.minX, right.minX, "\(a) should sit left of \(b)", file: file, line: line)
  }

  func testTheButtonsMediaRowKeepsItsOrderInRTL() {
    let frames = frames(GlassButtonsScreen())
    assertLeft("Play", of: "Pause", in: frames)
    assertLeft("Pause", of: "Forward", in: frames)
  }

  func testTheRecipesMiniPlayerKeepsItsLayoutInRTL() {
    // The player alone: the screen's scroll view keeps its content out of this accessibility walk.
    let frames = frames(MiniPlayer())
    assertLeft("Glass Animals", of: "Play", in: frames)
  }
}

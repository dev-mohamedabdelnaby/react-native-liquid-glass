import SwiftUI
import UIKit

/// One catalog screen (or Home, `"home"`) in a `UIHostingController`, with the environment the
/// iOS app's `ContentView` gives its screens. The props are observable state, so a prop change
/// updates the mounted screen. `RNLiquidGlassScreen.mm` sets the props and forwards the events.
@objc public final class LiquidGlassScreenHost: UIView {
  @objc public var onBack: (() -> Void)?
  @objc public var onOpen: ((String) -> Void)?
  /// A setting changed on Home: `appearance`, `layoutDirection` and `tint` (`'#RRGGBBAA'`, `''` for none).
  @objc public var onSettingsChange: (([String: String]) -> Void)?

  private let model = LiquidGlassScreenModel()
  private lazy var controller = UIHostingController(rootView: LiquidGlassScreenRoot(model: model, host: self))

  /// A `GlassComponent` raw value, or `"home"`.
  @objc public var screen: String {
    get { model.screen }
    set { model.screen = newValue }
  }

  /// `'system' | 'light' | 'dark'`.
  @objc public var appearance: String {
    get { model.appearance.rawValue }
    set {
      model.appearance = AppearanceMode(rawValue: newValue) ?? .system
      applyAppearance()
    }
  }

  /// `'system' | 'ltr' | 'rtl'`.
  @objc public var layoutDirection: String {
    get { model.directionMode.jsValue }
    set { model.directionMode = LayoutDirectionMode.allCases.first { $0.jsValue == newValue } ?? .system }
  }

  /// The general glass tint (not `tintColor`: that is `UIView.tintColor`).
  @objc public var glassTint: UIColor? {
    get { model.tint }
    set { model.tint = newValue }
  }

  public override init(frame: CGRect) {
    super.init(frame: frame)
    controller.view.frame = bounds
    controller.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
    addSubview(controller.view)
  }

  @available(*, unavailable)
  required init?(coder: NSCoder) { fatalError("init(coder:) is not supported") }

  // A child of the React view controller while on screen, so the screen can present
  // full-screen covers, sheets, popovers and alerts. Added before the view enters the window
  // when the view controller is already reachable, so the controller gets its appear callbacks.
  public override func willMove(toWindow newWindow: UIWindow?) {
    super.willMove(toWindow: newWindow)
    guard newWindow !== window else { return }
    if let window { WindowStyle.release(self, in: window) }
    if newWindow == nil {
      if controller.parent != nil { controller.willMove(toParent: nil) }
    } else {
      addControllerToParent()
    }
  }

  public override func didMoveToWindow() {
    super.didMoveToWindow()
    guard window != nil else {
      if controller.parent != nil { controller.removeFromParent() }
      return
    }
    applyAppearance()
    // When this view itself was just added, its superview (and so the view controller) was not
    // reachable yet in willMove(toWindow:).
    addControllerToParent()
    if let parent = controller.parent { controller.didMove(toParent: parent) }
  }

  private func addControllerToParent() {
    guard controller.parent == nil,
          let parent = sequence(first: self as UIResponder, next: { $0.next }).first(where: { $0 is UIViewController }) as? UIViewController
    else { return }
    parent.addChild(controller)
  }

  // `preferredColorScheme` does not reach the window from a child hosting controller. In the iOS
  // app it sets the window's style (the choice is for the whole app), so the host does that;
  // `'system'` leaves the window to the app.
  fileprivate func applyAppearance() {
    guard let window else { return }
    if let scheme = model.appearance.colorScheme {
      WindowStyle.claim(self, UIUserInterfaceStyle(scheme), in: window)
    } else {
      WindowStyle.release(self, in: window)
    }
  }

  /// `'#RRGGBBAA'` (a dynamic colour as in light mode), as the events send colours; `''` for none.
  @objc public static func rgba(_ color: UIColor?) -> String { rgba(color, in: .light) }

  /// `'#RRGGBBAA'` of the colour as in `style`; `''` for none.
  static func rgba(_ color: UIColor?, in style: UIUserInterfaceStyle) -> String {
    guard let color = color?.resolvedColor(with: UITraitCollection(userInterfaceStyle: style)) else { return "" }
    var r: CGFloat = 0, g: CGFloat = 0, b: CGFloat = 0, a: CGFloat = 0
    color.getRed(&r, green: &g, blue: &b, alpha: &a)
    return "#" + [r, g, b, a].map { String(format: "%02X", Int((min(max($0, 0), 1) * 255).rounded())) }.joined()
  }

  // Only Home's controls call this, never the prop setters: JS state and props would echo.
  fileprivate func homeChanged() {
    applyAppearance()
    onSettingsChange?([
      "appearance": model.appearance.rawValue,
      "layoutDirection": model.directionMode.jsValue,
      "tint": Self.rgba(model.tint),
    ])
  }
}

/// The props, as SwiftUI state.
@Observable final class LiquidGlassScreenModel {
  var screen = ""
  var appearance = AppearanceMode.system
  var directionMode = LayoutDirectionMode.system
  var tint: UIColor?

  /// Home's picker: the choice whose colour is the tint (in light or dark mode), else None.
  var tintChoice: GlassTintChoice {
    guard let tint else { return .none }
    return GlassTintChoice.allCases.first { choice in
      guard let color = choice.color.map({ UIColor($0) }) else { return false }
      return [UIUserInterfaceStyle.light, .dark].contains {
        LiquidGlassScreenHost.rgba(color, in: $0) == LiquidGlassScreenHost.rgba(tint, in: $0)
      }
    } ?? .none
  }

  /// What untinted glass takes: the picked choice's own colour (it follows light and dark mode), else the tint.
  var glassTint: Color? { tintChoice.color ?? tint.map { Color(uiColor: $0) } }

  /// Home picked a tint; false when nothing changes. None also clears a tint no choice matches
  /// (which shows as None).
  func pickTint(_ choice: GlassTintChoice) -> Bool {
    guard choice != tintChoice || (choice == .none && tint != nil) else { return false }
    tint = choice.color.map { UIColor($0) }
    return true
  }
}

/// The window styles mounted screens ask for. The latest request wins; when no screen asks any
/// more, the window gets back the style it had before (an app's own `Appearance.setColorScheme`).
enum WindowStyle {
  private static var requests: [ObjectIdentifier: (original: UIUserInterfaceStyle, stack: [(host: ObjectIdentifier, style: UIUserInterfaceStyle)])] = [:]

  static func claim(_ host: AnyObject, _ style: UIUserInterfaceStyle, in window: UIWindow) {
    var entry = requests[ObjectIdentifier(window)] ?? (window.overrideUserInterfaceStyle, [])
    entry.stack.removeAll { $0.host == ObjectIdentifier(host) }
    entry.stack.append((ObjectIdentifier(host), style))
    requests[ObjectIdentifier(window)] = entry
    window.overrideUserInterfaceStyle = style
  }

  static func release(_ host: AnyObject, in window: UIWindow) {
    guard var entry = requests[ObjectIdentifier(window)],
          entry.stack.contains(where: { $0.host == ObjectIdentifier(host) })
    else { return }
    entry.stack.removeAll { $0.host == ObjectIdentifier(host) }
    requests[ObjectIdentifier(window)] = entry.stack.isEmpty ? nil : entry
    window.overrideUserInterfaceStyle = entry.stack.last?.style ?? entry.original
  }
}

extension LayoutDirectionMode {
  /// The JS name: `'system' | 'ltr' | 'rtl'`.
  var jsValue: String {
    switch self {
    case .system: "system"
    case .leftToRight: "ltr"
    case .rightToLeft: "rtl"
    }
  }
}

extension ContentView {
  /// Home with fixed settings, for the copied `#Preview`s.
  init() {
    self.init(
      appearance: .constant(.system), directionMode: .constant(.system), glassTint: .constant(.none),
      systemLayoutDirection: .leftToRight, onOpen: { _ in })
  }
}

/// The hosted screen with the environment the iOS app's `ContentView` sets: `\.glassTint`,
/// `preferredColorScheme` and `\.layoutDirection`.
struct LiquidGlassScreenRoot: View {
  let model: LiquidGlassScreenModel
  weak var host: LiquidGlassScreenHost?
  /// The direction from the system language (above the override below), used when the mode is `.system`.
  @Environment(\.layoutDirection) private var systemLayoutDirection

  var body: some View {
    Group {
      if model.screen == "home" {
        ContentView(
          appearance: setting(\.appearance),
          directionMode: setting(\.directionMode),
          glassTint: Binding {
            model.tintChoice
          } set: {
            if model.pickTint($0) { host?.homeChanged() }
          },
          systemLayoutDirection: systemLayoutDirection,
          onOpen: { host?.onOpen?($0) })
      } else if let component = GlassComponent(rawValue: model.screen) {
        NavigationStack {
          component.screen
            .toolbar {
              ToolbarItem(placement: .topBarLeading) {
                Button(action: { host?.onBack?() }) { Image(systemName: "chevron.backward") }
                  .accessibilityLabel("Back")
              }
            }
        }
      }
    }
    .environment(\.glassTint, model.glassTint)
    .preferredColorScheme(model.appearance.colorScheme)
    .environment(\.layoutDirection, model.directionMode.layoutDirection ?? systemLayoutDirection)
  }

  /// A Home setting: the user's change shows at once and goes to JS.
  private func setting<T: Equatable>(_ keyPath: ReferenceWritableKeyPath<LiquidGlassScreenModel, T>) -> Binding<T> {
    Binding {
      model[keyPath: keyPath]
    } set: { value in
      guard value != model[keyPath: keyPath] else { return }
      model[keyPath: keyPath] = value
      host?.homeChanged()
    }
  }
}

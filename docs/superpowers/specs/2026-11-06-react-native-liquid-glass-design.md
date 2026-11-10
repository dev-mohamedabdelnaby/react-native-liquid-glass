# react-native-liquid-glass — design

Date: 2026-11-06. Status: awaiting review.

## Goal

A React Native library that brings the Liquid Glass work of this session to JavaScript with a
SwiftUI-style API: one component tree gives Apple's real glass on iOS and this session's Compose
glass on Android, with every Android and iOS edit kept.

Success: a JS developer writes the same tree for both platforms and gets the look and behavior
of the iOS catalog app (`Developer/ios/liquidGlass`) on iOS and of the Android app
(`Developer/android`) on Android.

## Decisions

| Topic | Decision |
|---|---|
| Relation to react-native-liquid | New, separate package; react-native-liquid stays as it is. |
| Package | `@mohamed_abd_elnaby/react-native-liquid-glass` (scoped: the bare name may be taken on npm). |
| Location | `/Users/mohamedabdelnaby/Developer/libs/liquid-glass`, with an `example/` app. |
| API style | Mirrors SwiftUI: `glassEffect`, `GlassEffectContainer`, IDs, unions, transitions. |
| Minimum OS | iOS 26 (real glass only, no fallback look); Android 8 (API 26). |
| Android tiers | Lens on API 33+, blur on 31–32, frost on 26–30 (as in the Android app). |
| React Native | New Architecture only, Fabric native views, React Native 0.80+. |
| Distribution | Bare React Native and Expo through a development build (Expo Go can't load native code). |
| Scaffold | create-react-native-library, Fabric view template, Kotlin + Swift. |
| Approach | iOS: UIKit glass with RN children as real subviews. Android: the Compose glass package hosted in Fabric views. |

## Sub-projects

v1 covers four areas. Each is its own spec, plan and build, done in this order; this spec covers
the shared architecture and sub-project 1 in detail.

1. **Foundation and glass views** — package, settings, `GlassView`, `GlassButton`, example app.
2. **Containers and morphing** — `GlassEffectContainer` (spacing merge), glass IDs and morphs,
   unions, `matchedGeometry` / `materialize` / `identity` transitions.
3. **Menus, popover and dialogs** — menus that grow out of their button, context menu with
   preview, popover with arrow, confirmation popover, alert, sheet.
4. **Controls and bars** — tab bar (minimize, accessory, search), segmented, slider, toggle with
   the liquid drop and drop color, search field, toolbar buttons.

## Architecture

Three layers.

### JS (`src/`)

- Components named after SwiftUI: `GlassView`, `GlassButton` now; `GlassEffectContainer`,
  `GlassMenu`, `GlassPopover`, `GlassTabBar` and the rest in later sub-projects.
- `GlassSettingsProvider` holds the general settings. A component merges its own `glass` prop
  over the provider over the defaults (the Android app's `Glass.styled()` rule) and passes one
  flat set of props to its native view. Native code never reads React context.
- Codegen specs (`src/*NativeComponent.ts`) type every native view's props.

### iOS (`ios/`)

- One Fabric component per JS component: Swift view, thin Objective-C++ component shim.
- `GlassView` → `UIVisualEffectView(effect: UIGlassEffect)`. `UIGlassEffect.Style` from
  `style`, `tintColor` from `tint`, `isInteractive` from `interactive`. RN children mount into
  the effect view's `contentView`. Shapes map to `cornerConfiguration`.
- `GlassEffectContainer` (sub-project 2) → `UIVisualEffectView(effect: UIGlassContainerEffect)`
  with `spacing`; member glass views live in its `contentView` so UIKit merges and morphs them.
- Menus, popovers, alerts and bars (sub-projects 3–4) use the system components, which are glass
  on iOS 26.
- Settings with no iOS meaning are ignored there and documented as Android only: `blur`,
  `white`, `black`, `edgeLight`, `refraction`, `liquid`, `dropColor` and the per-control liquid
  switches. `tint` and `opaque` apply on both; `UIGlassEffect` has no opaque option, so iOS draws
  opaque glass as a solid fill (the tint, or the system background) inside the effect view's
  rounded shape.

### Android (`android/`)

- The Android app's `glass` and `liquid` packages move into the library as its glass engine,
  unchanged in behavior: settings, frosted (non-liquid) glass, opaque, the weaker merge rule,
  menus that become their button, popover, context menu and the rest.
- The engine has no React Native imports, so it stays testable on its own.
- Each Fabric view hosts a `ComposeView` that draws the glass. RN children stay ordinary Android
  views above it, laid out by Fabric (react-native-liquid's `LiquidComposeHost` pattern).
- The glass refracts what's behind it through a backdrop recorded from the root React view.

## Sub-project 1: API

```tsx
type Glass = {
  style?: 'regular' | 'clear';          // default 'regular'
  tint?: ColorValue;                     // own tint, wins over the general tint
  interactive?: boolean;                 // press swell and light
  opaque?: boolean;                      // solid fill
  liquid?: boolean;                      // Android: false = frosted blur, no lens
  blur?: number;                         // Android only; unset = follow settings
  white?: number;                        // Android only
  black?: number;                        // Android only
  edgeLight?: number;                    // Android only
  refraction?: { height: number; amount: number }; // Android only
};

type GlassShape = 'capsule' | 'circle' | { cornerRadius: number };

<GlassView glass={glass} shape="capsule" style={style}>{children}</GlassView>

<GlassButton title="Done" icon="checkmark" androidIcon={...} prominent tint={color}
  size="small" | "regular" | "large" onPress={fn} disabled={bool} />

<GlassSettingsProvider value={{
  tint, clear, blur, white, black, edgeLight, refraction, opaque, liquid,
  dropColor, sliderLiquid, switchLiquid, segmentedLiquid, tabBarLiquid, tabBarDrop,
}}>
```

- Defaults match the Android app: clear, blur 3, white 25%, black 5%, edge light 15%,
  refraction 14 / 24, no tint, not opaque, liquid on, every control liquid, tab bar drop on.
- `dropColor` and the per-control liquid switches are accepted from sub-project 1 so the
  settings shape is stable, but only take effect once the controls ship in sub-project 4.
- Icons take SF Symbol names. iOS renders them natively; Android maps common names
  (`checkmark`, `xmark`, `plus`, `minus`, `magnifyingglass`, `heart`, `square.and.arrow.up`,
  `ellipsis`, `trash`) to Material icons; `androidIcon` overrides any of them.

## Errors and fallbacks

- Invalid prop values fall back to the defaults with a dev-only warning; they never throw.
- On an unsupported device (iOS below 26), `GlassView` renders a plain `View` and warns once.
- Android below API 33 uses the engine's existing blur or frost tiers; that is supported, not
  an error.

## Testing

- Jest: settings merging (own > provider > defaults), shape parsing, the icon map, the
  unsupported-device fallback.
- Android JVM tests: the app's `GlassStyleTest`, `GlassSettingsTest` and `GlassContainerTest`
  move with the engine and keep passing.
- iOS XCTest: each glass prop maps to the right `UIGlassEffect` setting and corner
  configuration.
- Example app: a Glass Lab screen (every setting live) and a Glass View screen matching the
  catalog's, run on a phone and the iPhone simulator.
- CI: lint, typecheck, Jest and the Android unit tests (template workflow).

Sub-project 1 is done when the example builds and runs on both platforms, glass refracts the
backdrop (real glass on iOS, the lens on Android 33+), RN children render inside glass and stay
tappable, settings changes apply live, and screenshots match the catalog apps' Glass View screen.

## Out of scope for v1

Web support; custom shader parameters beyond those listed; Expo Go.

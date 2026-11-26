# CONTEXT: @native-dev-kit/react-native-liquid-glass (for agents)

Agent-only reference, version 1.0.0. It covers how the library works inside, where each piece lives, the tricks it relies on and the gotchas. Read it before you use the library in an app or change it. The user guide is [USAGE.md](USAGE.md); the human docs are [README.md](README.md) and `docs/`.

Every name below is exact. When this file and the code disagree, the code wins: fix this file in the same change.

---

## 1. What the library is

- **Whole native screens, not building blocks.** 22 React Native components: Home plus 21 catalog screens. On iOS each one is a SwiftUI screen of a native catalog app with Apple's real Liquid Glass. On Android it is the same screen of a Jetpack Compose catalog app, drawn either with a Compose glass engine (`androidStyle: 'glass'`, the default) or with Material 3 (`androidStyle: 'material'`).
- A screen draws everything itself: bar, back button, content, sheets, menus, alerts, covers. **It takes no children.** You cannot put React Native views inside it or restyle its parts.
- There is **no** `GlassView`, `GlassButton` or other primitive. Do not invent them. If a user wants glass inside their own React Native screen, say this library does not do that.
- All 22 components render one Fabric component, `LiquidGlassScreen`, with a `screen` prop (`'home'` or a `GlassScreenId`). Use the named components, never the native view.
- Settings (appearance, tint, layout direction, Android style and glass options, fonts, icons) come from `GlassSettingsProvider` through React context. A few can also be screen props (section 3).
- Requirements: React Native 0.88 (verified), **New Architecture only**, bare React Native CLI app (**no Expo**), iOS 27.0+, Android minSdk 26 with **compileSdk 37 and AGP 9.1** (Compose BOM `2026.09.00`). Debug builds load JS from Metro on port 8081.

## 2. Repository map

| Path | What it is |
|---|---|
| `src/index.tsx` | Public exports (section 3). Nothing else is public. |
| `src/screens.tsx` | `GLASS_SCREENS`, `GlassScreenId`, `GlassScreenProps`, the 22 components (`screen(id)` factory), props/settings → native props, native events → JS callbacks. |
| `src/settings.tsx` | `DEFAULT_SETTINGS`, `GlassSettingsProvider`, `useGlassSettings`. |
| `src/types.ts` | `GlassSettings`, `GlassIcon`. |
| `src/icons.ts` | `vectorIcon()` (public), `iconsJson()` (internal: the `icons` native prop). |
| `src/LiquidGlassScreenNativeComponent.ts` | Codegen spec: every native prop, its native default, the three events. |
| `src/__tests__/` | Jest: `screens.test.tsx`, `settings.test.tsx`. |
| `ios/RNLiquidGlassScreen.mm` | Fabric component view (Objective-C++). Props → host, events → emitter, mount/unmount hooks. |
| `ios/Host/LiquidGlassScreenHost.swift` | `LiquidGlassScreenHost` (UIView holding a `UIHostingController`), `LiquidGlassScreenModel` (`@Observable` props), `WindowStyle` (appearance requests), `LiquidGlassScreenRoot` (environment + back button). |
| `ios/Host/GlassFont.swift` | `GlassFontSpec`, `.glassFont` modifiers, `GlassFontTrait`, `GlassFontAppearance` (UIKit proxies), `GlassHostingController`. |
| `ios/Host/GlassIcon.swift` | `GlassIconSource` (parse/draw/load), `GlassIcons`, `GlassImage`, `Label/Button/Toggle/Menu(glassImage:)`. |
| `ios/Catalog/` | The iOS app's code: `ContentView.swift` (Home, `GlassComponent`, `AppearanceMode`, `LayoutDirectionMode`), `Screens/*.swift`, `Shared/*.swift` (`GlassTint`, `GlassBackdrop`, `LiquidGlassView`). Edited only where hosting, fonts, icons, RTL and QA fixes needed it. |
| `ios/Tests/` | XCTest (`ScreenIdsTests`, `LiquidGlassScreenHostTests`, `GlassFontTests`, `GlassIconTests`, `MediaPlayerDirectionTests`). Excluded from the pod sources; run as the pod's test spec. |
| `ReactNativeLiquidGlass.podspec` | Pod `ReactNativeLiquidGlass`, iOS 27.0, Swift 5 mode, `SWIFT_DEFAULT_ACTOR_ISOLATION=MainActor`, dependency `SwiftDraw 0.30.0`. |
| `android/build.gradle` | Library build: minSdk 26, compileSdk 37, Compose BOM `2026.09.00`, material-icons-extended 1.7.8, activity-compose 1.13.0, navigationevent 1.0.0, lifecycle 2.11.0, `com.caverock:androidsvg-aar:1.4`. Each version can be overridden from the app's root `ext` (`getExtOrDefault`). Applies the Compose compiler plugin at the app's `kotlinVersion`. Adds `-Xcontext-parameters` (the backdrop code needs it). |
| `android/src/main/java/com/mohamedabdelnaby/liquidglass/` | Host: `LiquidGlassScreenView.kt` (the view, CompositionLocals, events), `LiquidGlassScreenManager.kt` (prop setters), `FontFamily.kt`, `GlassIcons.kt`, `ReactNativeLiquidGlassViewPackage.kt`. |
| `…/liquidglass/host/` | `ComposeHost.kt` (React view holding one `ComposeView`), `GlassLifecycleOwner.kt`, `BackRouter.kt`, `BackKey.kt` (+ `WindowHooks`), `SystemBarIcons.kt` (+ `SharedRequests`, `BarsRequest`, `WindowBarIcons`). |
| `…/liquidglass/catalog/` | The Android app's code: `HomeScreen.kt`, `GlassComponent.kt`, `glass/` (building blocks and their Material counterparts), `liquid/` (liquid controls; `liquid/backdrop/` is the glass engine under the `LICENSE-*` files), `shapes/` (Material control shapes), `screens/` (21 screens), `ui/theme/`. |
| `android/src/test/` | JVM and Robolectric tests (host and catalog). `ScreenIdsTest` reads `src/screens.tsx` with a regex. |
| `example/` | React Native CLI app (New Architecture). `src/App.tsx` (the recipe), `src/customize.ts` (`CUSTOMIZE` flag: Cairo font and sample icons), `src/PushDemo.tsx` (plain RN screens to compare back buttons), `ios/ReactNativeLiquidGlassExample/LayoutDirection.m` (UIKit trait workaround, section 11). |
| `docs/` | Human guides and `docs/assets/` media (`docs/assets/INDEX.md` lists every file). **Not in the npm package.** `docs/superpowers/` holds specs and plans. |
| `README.md`, `USAGE.md`, `CONTEXT.md`, `AGENTS.md`, `llms.txt`, `skills/` | Ship in the npm package; `docs/` does not. |

## 3. Public API (exact)

```ts
import {
  // 22 screens
  GlassHomeScreen, GlassEffectScreen, GlassViewScreen, InteractiveGlassScreen,
  GlassButtonsScreen, GlassContainerScreen, GlassMorphingScreen, GlassUnionScreen,
  GlassToolbarScreen, GlassFloatingHeaderScreen, GlassTabBarScreen, GlassSheetsScreen,
  GlassControlsScreen, GlassTextScreen, GlassTransitionsScreen, GlassSearchScreen,
  GlassEditSearchScreen, GlassScrollEdgeScreen, GlassSplitViewScreen,
  GlassZoomTransitionsScreen, GlassAlertsScreen, GlassRecipesScreen,
  // settings
  GlassSettingsProvider, useGlassSettings, DEFAULT_SETTINGS,
  // icons
  vectorIcon,
  // list and types
  GLASS_SCREENS, type GlassScreenId, type GlassScreenProps, type GlassSettings, type GlassIcon,
} from '@native-dev-kit/react-native-liquid-glass';
```

Nothing else is exported.

### Screens

`GLASS_SCREENS` is `readonly { id, title }[]`: the 21 catalog screens in Home's order. **It does not include Home.** `GlassScreenId` is the union of those ids (no `'home'`). Keep each entry on one line as `{ id: '…', title: '…' }`: `ScreenIdsTest.kt` reads the ids with ``Regex("""\{ id: '(\w+)'""")``.

`title` is Home's row title. Several screens show another title in their own bar (from the native apps):

| id | Component | Home row title | Bar title |
|---|---|---|---|
| (`home`) | `GlassHomeScreen` | — | Liquid Glass |
| `glassEffect` | `GlassEffectScreen` | Glass Effect | Glass Effect |
| `glassView` | `GlassViewScreen` | Glass View | Glass View |
| `interactive` | `InteractiveGlassScreen` | Interactive Glass | **Interactive** |
| `buttons` | `GlassButtonsScreen` | Buttons | Buttons |
| `container` | `GlassContainerScreen` | Glass Container | **Container** |
| `morphing` | `GlassMorphingScreen` | Morphing | Morphing |
| `union` | `GlassUnionScreen` | Union | Union |
| `toolbar` | `GlassToolbarScreen` | Toolbar | Toolbar |
| `floatingHeader` | `GlassFloatingHeaderScreen` | Floating Header | Floating Header |
| `tabBar` | `GlassTabBarScreen` | Tab Bar | Tab Bar |
| `sheets` | `GlassSheetsScreen` | Sheets & Menus | Sheets & Menus |
| `controls` | `GlassControlsScreen` | Controls | Controls |
| `text` | `GlassTextScreen` | Text | Text |
| `transitions` | `GlassTransitionsScreen` | Glass Transitions | **Transitions** |
| `search` | `GlassSearchScreen` | Search | Search |
| `editSearch` | `GlassEditSearchScreen` | Edit & Search Buttons | **Notes** |
| `scrollEdge` | `GlassScrollEdgeScreen` | Scroll Edge & Bars | **Scroll Edge** |
| `splitView` | `GlassSplitViewScreen` | Sidebar & Inspector | **Split View** |
| `zoomTransitions` | `GlassZoomTransitionsScreen` | Zoom Transitions | **none** (a status pill) |
| `alerts` | `GlassAlertsScreen` | Alerts & Dialogs | Alerts & Dialogs |
| `recipes` | `GlassRecipesScreen` | Recipes | Recipes |

### `GlassScreenProps` (the same for all 22)

```ts
type GlassScreenProps = {
  androidStyle?: 'glass' | 'material';               // wins over the provider
  androidControlShape?: 'pill' | 'labeled' | 'glow'; // wins over the provider
  androidGlowIntensity?: number;                     // wins over the provider
  fontFamily?: GlassSettings['fontFamily'];          // wins over the provider
  icons?: GlassSettings['icons'];                    // merged over the provider's, key by key
  onBack?: () => void;                               // the screen's back / close button (not Home)
  onOpen?: (screen: GlassScreenId) => void;          // Home only: a row was tapped
  onSettingsChange?: (settings: GlassSettings) => void; // Home only: a Home control changed
  style?: StyleProp<ViewStyle>;                      // REQUIRED in practice: no default size
  testID?: string;
};
```

No `children`, no `title`, no other settings props.

### `GlassSettings`, `DEFAULT_SETTINGS`

```ts
type GlassIcon = { glyph: string; fontFamily: string } | { svg: string } | ImageSourcePropType;

type GlassSettings = {
  appearance?: 'system' | 'light' | 'dark';
  tint?: ColorValue | null;
  layoutDirection?: 'system' | 'ltr' | 'rtl';
  androidStyle?: 'glass' | 'material';
  androidControlShape?: 'pill' | 'labeled' | 'glow';
  androidGlowIntensity?: number;
  clear?: boolean; blur?: number; white?: number; black?: number; edgeLight?: number;
  refraction?: { height: number; amount: number };
  opaque?: boolean; liquid?: boolean; dropColor?: ColorValue | null;
  sliderLiquid?: boolean; switchLiquid?: boolean; segmentedLiquid?: boolean;
  tabBarLiquid?: boolean; tabBarDrop?: boolean;
  fontFamily?: string | { regular?: string; medium?: string; semibold?: string; bold?: string; heavy?: string };
  icons?: Record<string, GlassIcon | undefined>; // key: SF Symbol name
};

DEFAULT_SETTINGS = {
  appearance: 'system', tint: null, layoutDirection: 'system',
  androidStyle: 'glass', androidControlShape: 'pill', androidGlowIntensity: 0.35,
  clear: true, blur: 3, white: 0.25, black: 0.05, edgeLight: 0.15,
  refraction: { height: 14, amount: 24 }, opaque: false, liquid: true, dropColor: null,
  sliderLiquid: true, switchLiquid: true, segmentedLiquid: true, tabBarLiquid: true,
  tabBarDrop: true, fontFamily: '', icons: {},
}; // Required<GlassSettings>
```

`vectorIcon(iconSet, name)` returns `GlassIcon | undefined` (`undefined`, plus a `__DEV__` warning, when the set has no such name; the native icon stays). It needs only `getRawGlyphMap()` and `getFontFamily()`; the library does not depend on react-native-vector-icons.

## 4. Data flow: JS → native → JS

### Props

1. `GlassSettingsProvider` (`settings.tsx`) merges `value` over the parent context (or `DEFAULT_SETTINGS`) as `{...parent, ...value-without-undefined}` in a `useMemo([parent, value])`. **Top-level keys replace, they do not deep-merge:** an inner provider's `icons` or `fontFamily` replaces the outer one's whole value.
2. `GlassScreen` (`screens.tsx`) reads the merged settings and renders `<LiquidGlassScreen>` with:
   - every setting spread as a prop, except: `tint` → `tintColor`; `refraction` → `refractionHeight` + `refractionAmount`; `fontFamily` → `fontRegular|Medium|Semibold|Bold|Heavy` (`fontFaces()`: a string fills all five, a map leaves missing weights `''`); `icons` → one JSON string (`iconsJson()`).
   - screen props win: `androidStyle ?? settings.androidStyle`, the same for `androidControlShape`, `androidGlowIntensity` and `fontFamily`; `icons = {...providerIcons, ...screenIcons}` (a key set to `undefined` in the screen's map removes the provider's icon for that screen).
3. Codegen (`LiquidGlassScreenNativeComponent.ts`; codegen name `RNLiquidGlassViewSpec`, Android package `com.mohamedabdelnaby.liquidglass`, iOS class `RNLiquidGlassScreen`) carries them. Native defaults equal `DEFAULT_SETTINGS`.
4. **iOS** (`RNLiquidGlassScreen.mm` `updateProps`) reads only `screen`, `appearance`, `layoutDirection`, `tintColor`, the five font faces and `icons`, and sets them on `LiquidGlassScreenHost`. The host keeps them in `LiquidGlassScreenModel` (`@Observable`), so a prop change updates the mounted SwiftUI screen. The Android-only props are not read on iOS.
5. **Android** (`LiquidGlassScreenManager.kt`) sets `screen`, `mode` (appearance), `jsLayoutDirection`, `androidStyle` (`androidStyleOf`: anything but `'material'` is Glass), `controlShape` (`controlShapeOf`: unknown, including the old `'default'`/`'squared'`, is Pill), `glowIntensity` (`glowIntensityOf`: NaN → 0.35, clamped to 0..1), `settings` (the catalog's `GlassSettings` data class; `blur` and refraction become dp), `faces` and `icons` (`glassIconsOf`). Each is Compose state on `LiquidGlassScreenView`, so a change recomposes.

### Events

| Event | iOS source | Android source | JS callback |
|---|---|---|---|
| `onBack` | the host's leading toolbar button (`LiquidGlassScreenRoot`) | `LocalOnBack` (the catalog's back arrow or close button) | `onBack()` |
| `onOpen` | Home row → `ContentView.onOpen` | `HomeScreen.onOpen` (`GlassComponent.name` with the first letter lowercased) | `onOpen(id as GlassScreenId)` |
| `onSettingsChange` | Home's bindings → `homeChanged()` | `emitSettings()` from each Home control | `onSettingsChange(GlassSettings)` |

`onSettingsChange` in detail:

- Only Home's own controls emit it, never the prop setters (otherwise JS state → props → event would loop).
- **iOS** sends `appearance`, `layoutDirection` and `tint` from Home; `RNLiquidGlassScreen.mm` fills every Android-only field from the current props (an echo), so the round trip does not reset them.
- **Android** sends every field Home can change (`putSettings`).
- Colours arrive as `'#RRGGBBAA'`; `''` becomes `null` in JS. Refraction arrives flat and is rebuilt as `{ height, amount }`.
- JS adds `fontFamily` and `icons` **from the provider** (`providerFont`, `providerIcons`), because Home has no font or icon control; feeding the event back into the provider keeps them. A `fontFamily`/`icons` passed as a prop to Home is not included (it stays a prop).
- Home does not keep a change by itself: the app must store the event and pass it back into the provider, or the control snaps back and other screens never see the change.

## 5. Settings: who reads what

| Setting | Native prop | iOS | Android glass | Android material | Home control |
|---|---|---|---|---|---|
| `appearance` | `appearance` | window style (6.3) | Compose theme + bar icons | same | both: System / Light / Dark |
| `tint` | `tintColor` | `\.glassTint` (applied at 0.35 opacity to glass without its own tint) | tint of every glass without its own | the scheme's primary roles, contrast-adjusted | iOS: None / Blue / Purple / Pink / Orange / Green / Teal; Android: colour choices |
| `layoutDirection` | `layoutDirection` | `\.layoutDirection` | `LocalLayoutDirection` | same | both: System / LTR / RTL |
| `androidStyle` | `androidStyle` | ignored | — | — | Android: Glass / Material |
| `androidControlShape` | `androidControlShape` | ignored | ignored (liquid controls stay) | switch, slider, top tabs, bottom tabs | Android Material: Pill / Labeled / Glow |
| `androidGlowIntensity` | `androidGlowIntensity` | ignored | ignored | `'glow'` halo strength | Android Material, while Glow is picked |
| `clear`, `blur`, `white`, `black`, `edgeLight`, `refraction`, `opaque`, `liquid` | same; `refractionHeight`, `refractionAmount` | ignored | glass engine | ignored | Android Glass section |
| `dropColor`, `sliderLiquid`, `switchLiquid`, `segmentedLiquid`, `tabBarLiquid`, `tabBarDrop` | same | ignored | liquid controls | ignored | Android Glass section |
| `fontFamily` | `font*` | yes (section 9) | yes | yes (Material type scale) | none |
| `icons` | `icons` | yes (section 10) | yes | yes | none |

Units (Android): `blur` and `refraction` in dp; `white`, `black`, `edgeLight` from 0 to 1. Unknown string values fall back to the default (`'system'`, `'glass'`, `'pill'`). `androidSegmentedCheck` existed during development and was **removed**; do not use it.

## 6. iOS host

### 6.1 Mounting

- `RNLiquidGlassScreen` creates one `LiquidGlassScreenHost` that fills it. `+shouldBeRecycled` returns `NO`: a recycled view would show the previous screen's state.
- The host holds a `GlassHostingController` (a `UIHostingController<LiquidGlassScreenRoot>`) and adds it as a **child of the nearest React view controller** (found through the responder chain), so the screen can present sheets, popovers, alerts and `fullScreenCover`s (the Tab Bar demos, Split View).
- `mountingTransactionDidMount` calls `attachToViewController()` on every transaction: native-stack pushes asynchronously after the mount, and a controller added only when the view enters the window misses the safe area the push hands out first (its bars sit under the status bar until the push ends).
- `willMove(toWindow:)` / `didMoveToWindow()` handle `willMove`/`didMove`/`removeFromParent`; `detachFromViewController()` runs on unmount outside a window.
- `LiquidGlassScreenRoot`: `"home"` → `ContentView` (the iOS Home) with bindings that call `homeChanged()`; any `GlassComponent` id → `NavigationStack { component.screen.toolbar { leading Button → onBack } }` with `GlassImage("chevron.backward", uiKit: true)` and the accessibility label "Back"; an unknown id renders nothing. Each screen has its own navigation bar, so the bar slides with its screen on push instead of cross-fading.
- The root sets: `.font` (body in the custom font), `\.glassFont`, `\.glassIcons`, `\.glassTint`, `.preferredColorScheme`, `\.layoutDirection` (the mode's direction, else the system direction read above it).

### 6.2 Tint

`LiquidGlassScreenModel.tintChoice` maps the tint to the Home choice whose colour matches in light or dark (`rgba` comparison). No match shows None but still tints (`glassTint` falls back to the raw colour). Picking None also clears an unmatched tint. Events send `'#RRGGBBAA'` resolved in light mode.

### 6.3 Appearance is window-wide

- `preferredColorScheme` from a child hosting controller does not reach the window, so the host sets `window.overrideUserInterfaceStyle` itself through `WindowStyle` (`LiquidGlassScreenHost.swift`). That changes **the whole window**: the app's React Native screens and the status bar too.
- Requests: per window, a stack of `(host, style)`. **The latest mounted screen asking for `'light'` or `'dark'` wins.** The same style again keeps its place. The window's own style is saved at the first request and restored when the last request ends.
- `'system'` never writes the window (it only ends this screen's request).
- A request lasts while the screen is **in the React tree**, not only while it is in the window: leaving the window (a native-stack push, a cover) keeps it. It ends in `willMoveToSuperview:nil` / `invalidate` (`releaseWindowStyle`). So an off-screen mounted screen (under a pushed one, in an inactive tab) still holds its style.
- An app `Appearance.setColorScheme` made while a glass screen holds a request is **lost** on release: the window gets back the style saved at the first request.
- Windows are held weakly; a closed window's requests are dropped.
- **Known issue (to be fixed before release; delete this note when fixed):** with the system in Light and Home set to Dark, the status bar content stays dark (the time and signal are unreadable on the dark screen). With the system in Dark it is right.

### 6.4 Home redraw on a direction switch

`ContentView`'s `List` has `.id(layoutDirection)`: a list switched from RTL to LTR kept UIKit's RTL layout and drew mirrored. The cost: after a switch that changes the resolved direction, Home's list opens at the top.

### 6.5 Tab bar minimize

`GlassTabBarScreen.swift` uses `.tabBarMinimizeBehavior(.onScrollDown)` and lets **iOS drive** minimize and expand with the system animation. An earlier workaround that switched the behaviour to `.never` while scrolling up snapped the bar and broke the Now Playing accessory's morph; it was removed. Never switch the minimize behaviour mid-scroll. The bar expands on a released scroll back toward the top (system behaviour).

## 7. Android host

### 7.1 View, composition and lifecycle

- `LiquidGlassScreenView` extends `ComposeHost`, a `ReactViewGroup` with one `ComposeView`. `LiquidGlassScreenManager` is a `SimpleViewManager` (no React children).
- `GlassLifecycleOwner`: without it Compose binds to react-native-screens' Fragment view lifecycle, which is destroyed on every detach (push, tab switch) and would dispose the glass for good. This owner lives as long as the React view: RESUMED while attached, CREATED while detached, DESTROYED when React drops the view.
- Layout: React views ignore `requestLayout`, but Compose requests layouts while it animates, so `ComposeHost.requestLayout` posts a measure and layout of the `ComposeView` on the next animation frame. A `ComposeView` measured outside a window throws (no recomposer yet), so layout runs only while attached. `onAttachedToWindow` requests a layout (native-stack lays out a pushed screen before it has a window).
- `dispose()` (from `onDropViewInstance`): React drops a popped screen as its exit animation starts. If the view is still attached it is only marked `dropped` and destroyed on detach, so it keeps drawing during the pop.
- CompositionLocals the view provides: `LocalAndroidStyle`, `LocalGlassSettings` (with the font family), `LocalControlShape`, `LocalGlowIntensity` (not static, so a slider tick recomposes only the glows), `LocalGlassIcons`, then inside `LiquidGlassTheme(mode)`: `LocalOnBack`, `LocalGlassWindowBackdrop`, `LocalLayoutDirection`.
- Screen ids map to `GlassComponent` entries by capitalising the first letter (`screenComponent`); an unknown id renders nothing.

### 7.2 Back key routing

- React Native's activity takes every back press to pop native-stack, and its callback can sit above handlers a glass screen adds. So each host has a `BackRouter`: the screen's Compose `BackHandler`s register on private dispatchers (`LocalNavigationEventDispatcherOwner`, `LocalOnBackPressedDispatcherOwner`), and one `PRIORITY_OVERLAY` handler on the activity's `NavigationEventDispatcher` forwards back to them while any is enabled. With none enabled, back goes on to React Native and React Navigation pops.
- API 36+ and predictive back use the activity's dispatcher, so **the activity must be a `NavigationEventDispatcherOwner`** (`ReactActivity` / `ComponentActivity` is).
- Key events (before Android 16, or apps not on predictive back) go to `Activity.onBackPressed`, which React Native answers without asking the dispatcher. So `BackKey` wraps the activity **window's `Window.Callback`** and gives a back key to the latest attached router that wants it (DOWN and UP handled as one press). `WindowHooks` reinstalls the wrapper if something replaced the callback and holds both sides weakly. A library that replaces the callback without delegating breaks this until a glass screen enters the window again.
- Two glass screens visible at once (a transparent modal, a form sheet): back goes to the one that entered the window last.
- Do not add a JS `BackHandler` for glass screens.

### 7.3 Appearance and bar icons

- The screen's Compose theme follows `appearance` (`LiquidGlassTheme(mode)`; System is `isSystemInDarkTheme()`). React Native views are not changed.
- The screen also sets the status and navigation bar icon colours (`SystemBarIcons`), as the Android app's `MainActivity` did: dark icons in light. The request model is the iOS one (`SharedRequests` per flag, `BarsRequest` per screen): the latest mounted request wins, it lasts while the screen is in the React tree, it ends on drop. If the app wrote a value itself since (React Native `<StatusBar>`), that value is kept as the one to restore.
- Xiaomi/MIUI in system dark mode draws status icons from the content under them, so the Floating Header in Light may show white icons. Platform behaviour.

## 8. Android: Glass vs Material, control shapes

- `LocalAndroidStyle` / `isMaterial` (`catalog/glass/AndroidStyle.kt`). Each shared building block in `glass/` switches to its Material counterpart in one place (`MaterialBars.kt`, `MaterialControls.kt`, `MaterialDialogs.kt`, `MaterialLists.kt`); screens are not rewritten. `platformTextStyle` gives the iOS size in glass and a Material type role in Material; `platformCopy` swaps glass/iOS wording for Android wording.
- Material: `TopAppBar` / `LargeTopAppBar`, `Button` / `FilledTonalButton` / `OutlinedButton` / `TextButton`, FAB, `BottomAppBar`, `DropdownMenu`, `AlertDialog`, `ModalBottomSheet`, a top search bar, Material icons (`nativeIcon()` / `androidIcon()` map the iOS-style vectors), dynamic colour (Android 12+) else the baseline scheme, tint → primary roles. Screens about glass itself (Glass Effect, Glass View, Union, Morphing, Container) show tonal surfaces on the plain page. Material Home shows only Appearance, Layout direction, Android style, the control shape (+ glow intensity) and Colors (tint).
- The Material bottom tab bar does **not** minimize on scroll (the Tab Bar screen hides its "Minimize on scroll" switch); it floats above the navigation bar in its own container colour.
- **Control shapes** (`catalog/shapes/`: `ControlShape.kt`, `ShapedSwitch.kt`, `ShapedSlider.kt`, `ShapedTabs.kt`, `ShapedTabBar.kt`): `pill | labeled | glow`, **Material only**, default `pill`. Glass keeps its liquid switch, slider, segmented control and tab bar with the drop, and `sliderLiquid` … `dropColor` work only there. Colours: the control's own colour, else `MaterialTheme.colorScheme.primary`; text on the accent is white or black by contrast. Glow halo: `Modifier.glow` (`dropShadow`, 14 dp radius) scaled by `LocalGlowIntensity`. Spring motion, 48 dp targets, ripples, D-pad focus, a slider haptic each tenth, TalkBack state and value. ON / OFF are fixed English at a fixed size.
- **The RTL border trick:** `Modifier.border`'s draw cache is not invalidated on a runtime layout-direction change (Compose ui 1.12.1), so an asymmetric bordered shape keeps a stale outline after the direction is switched at run time. The shapes avoid it by being symmetric capsules (`Capsule` in `ControlShape.kt`). If you add an asymmetric border, wrap it in `key(LocalLayoutDirection.current) { … }`.
- **Caption plates:** grey captions and section headers over a **photo** backdrop get a plate (`captionColors()` / `captionPlate()` in `glass/GlassLayout.kt`, 4.5:1 or more over any pixel). `LocalOnPhoto` comes from `GlassSurfaceHost(photo = …)`; plain-background screens pass `photo = false` (Home, Controls, Search, Edit & Search, Scroll Edge, Toolbar, Floating Header, Split View). A new plain screen must pass `photo = false` (it fails safe: the plates show). Material never shows plates.
- **Round badges:** tab bar badges are a fixed 18 dp box (at least 18 dp wide) with the count centred, no font padding and a fixed text size, so one digit is a circle and more digits a capsule. The Material M3 `Badge` is held at 16 dp.

## 9. Fonts

JS: a `fontFamily` string sends the same name for all five weights (a *family*); a map sends each weight's face, missing weights `''`.

- **iOS** (`GlassFont.swift`): `GlassFontSpec.face(for:)` takes the weight's face, else regular, else any set face; nil means the system font. `isFamily` (all five equal) → `Font.custom(name).weight(w)`, so iOS picks the family member; otherwise each face is a font of its own. SwiftUI texts use `.glassFont(style)` / `.glassFont(size:weight:)` / `.glassFontOnly(style)` and keep their size, weight and Dynamic Type; without a font they are exactly the system font.
- **iOS UIKit bars** (navigation and large titles, bar buttons, tab bar items, segmented controls, search fields) are styled by appearance proxies registered **per font trait**: `GlassFontTrait` (a custom `UITraitDefinition`) is set in the hosting controller's `traitOverrides`, and `GlassHostingController.present` copies it onto presented controllers (sheets, covers; first level only). Screens without a font carry no trait, so UIKit's own look and the app's own bars are untouched. Only the large title and the search field scale with Dynamic Type. Shortcut: bars already on screen keep the old font until they are created again after a font change.
- **Android** (`FontFamily.kt`): `fontFamilyOf(faces)` builds a Compose `FontFamily` of `NamedFont`s loaded through React Native's `ReactFontManager.getTypeface(name, weight, …, assets)`, so `assets/fonts` files (with React Native's `_bold` naming) and `ReactFontManager.addCustomFont` families work. A family loads with the weight; a map's faces load at normal weight. It reaches glass texts and the Material type scale.
- Adding fonts: Android `android/app/src/main/assets/fonts/` (the file name is the name); iOS the app target plus `UIAppFonts` (the PostScript name; for a family string, the family name). Naming files after their PostScript name gives one name on both.
- Texts the system draws keep the system font: on iOS context menus and the menus of menu buttons and pickers, alerts, action sheets (confirmation dialogs), the share sheet, the date, time and colour pickers, badge numbers and the keyboard; on Android the share sheet and the keyboard.

## 10. Icons

- Keys are **SF Symbol names on both platforms** (`'square.and.arrow.up'`). Android's call sites are `glassIcon("<SF name>", <Material vector>)`, so one map works everywhere. Home row keys: `GlassComponent.kt` (`symbol`) and `ContentView.swift` (`systemImage`).
- JS `iconsJson()`: `{glyph, fontFamily}` and `{svg}` pass through; an image source goes through `Image.resolveAssetSource` → `{uri}` (an array uses its first entry). Debug `require()` images are Metro `http://` URLs; in Release they are a bundle path (iOS) or a drawable name (Android).
- **iOS** (`GlassIcon.swift`): `GlassIconSource.parse` drops bad JSON and bad entries. Glyphs (the CoreText path of the glyph's ink) and SVGs (SwiftDraw) are drawn at once at 64 pt as template images; a font the app did not add or a bad SVG gives nil, and the native icon stays. Images load asynchronously through `URLSession.shared` (its default timeout) or from a file; a failed load leaves the native icon. Loaded images and the directional-symbol table are process-wide caches (shortcut: unbounded).
  - `GlassImage(name)` replaces `Image(systemName:)` everywhere in the catalog: the custom icon fills the symbol's frame (no smaller than a square symbol, so a flat symbol such as `ellipsis` does not shrink it to a dot), tinted as a template.
  - `uiKit: true` (toolbar, menu and tab items, the host's back button): UIKit reads only a plain image, so `glassUIImage` renders it at the body-style symbol size and **mirrors it in advance for RTL** (UIKit bars do not flip images for SwiftUI's direction).
  - **The RTL rule:** a custom icon mirrors exactly when the SF Symbol it replaces mirrors (`isDirectional`: the symbol's RTL image differs from its LTR one, as for `chevron.backward` and not `chevron.left`).
  - **VoiceOver:** `.accessibilityRepresentation { Image(systemName: name) }`, so the custom icon is read as the symbol was.
- **Android** (`GlassIcons.kt`): `glassIconsOf` takes strings only and drops bad entries. `glassIcon()` returns the native painter until the drawing is ready (the first frame is native), then a `GlassIconPainter` (a template through the `Icon` tint's colour filter, mirrored in RTL when the replaced vector is `autoMirror`). Drawings are made off the main thread and cached for the process (shortcut: unbounded). Glyphs need `paint.hasGlyph` (an unknown font stays native). SVG through AndroidSVG (the document size is used when there is no viewBox). Images: `http(s)` with **10 s connect and read timeouts**, `file`, `content`, or a release drawable name; downsampled to about 96 dp. Fitted inside Material's 2-of-24 dp padding. Failed loads are not cached (retried per call site).
- Left native on purpose (system-drawn, or Android-only): steppers, iOS `.searchable` and the Material search bar's icons, menu-picker chevrons, the Material menu checkmark, swipe delete, selection circles, drag handles, the Tab Bar colour swatch, previews, backdrop photos. On Android the glass Search field takes a custom `magnifyingglass` / `xmark`; iOS's `.searchable` cannot.

## 11. Layout direction

- **iOS `'system'`** is UIKit's direction from the system language, which is RTL **only if the app is localized for an RTL language** (an `ar.lproj` / `he.lproj`, or `CFBundleLocalizations` listing it). An English-only app stays LTR on an Arabic device, and so does React Native's `I18nManager.isRTL` (it intersects the app's localizations with the language).
- **Android `'system'`** is React Native's `I18nUtil.isRTL(context)` (device language, `allowRTL`, `forceRTL`, `supportsRtl`), so glass screens agree with RN screens. Unknown values are `'system'` on both platforms.
- Android text uses `TextDirection.Content` (`ui/theme/Type.kt`): English keeps its period last in RTL, as in SwiftUI. Trade-off: a wrapped English paragraph aligns left in an RTL layout (iOS keeps it right), and a wrapped Arabic paragraph aligns right in an LTR layout (iOS aligns to the layout's leading edge; not checked on a device). The catalog's own texts are English.
- Android engine code uses start/end everywhere; menus, popovers, drops, sliders, tab bars and sheets mirror.
- **Media players stay LTR in RTL** on both platforms and both Android styles (HIG and Material: media controls follow time). Forced in `GlassRecipesScreen.swift` (mini player, expanded slider and transport row), `GlassButtonsScreen.swift` (media row), `GlassTabBarScreen.swift` (Now Playing accessory), and in the Android `RecipesScreen.kt`, `ButtonsScreen.kt`, `TabBarScreen.kt` (`LocalLayoutDirection provides Ltr`). Tests: `MediaPlayerDirectionTests.swift`, `CatalogScreensTest.kt`.
- **iOS UIKit pieces** (menu rows, the open `.searchable` field) and `fullScreenCover`s (Tab Bar "With Now Playing Bar") follow UIKit's trait, not SwiftUI's environment: they stay LTR in a forced RTL unless the app overrides the window scene's layout-direction trait.
- **The iOS 26+ native-stack back chevron bug:** React Navigation's `direction` mirrors the stack only through `semanticContentAttribute` (react-native-screens); UIKit's trait stays the system's. The glass back button is laid out by the trait and its chevron by the attribute, so when they disagree the chevron collapses to zero width (an empty glass button). It hits the app's own native-stack headers when the direction is chosen at run time. Workaround: set `UIWindowScene.traitOverrides.layoutDirection` to match from a small native module (the example's `example/ios/ReactNativeLiquidGlassExample/LayoutDirection.m`, called from `App.tsx`; `'system'` removes the override; also applied on `UISceneWillConnectNotification`). With it, UIKit pieces and covers mirror too. The full code is in `docs/layout-direction.md`.
- The stack animation recipe: always `NavigationContainer direction={rtl ? 'rtl' : 'ltr'}` (it defaults to `I18nManager.isRTL`); iOS `animation: 'default'` (UIKit mirrors the push, the edge swipe and the content swipe; the edge swipe starts at the leading edge: left in LTR, right in RTL); Android `ios_from_right` / `ios_from_left` (absolute animations, picked by hand).

## 12. React Navigation recipe (verified: native-stack)

`@react-navigation/native` 7.5, `@react-navigation/native-stack` 7.21, `react-native-screens` 4.30. The full code is `docs/react-navigation.md` (it is `example/src/App.tsx` without the example-only push demo and trait module). The essentials:

```tsx
const [settings, setSettings] = useState<GlassSettings>(DEFAULT_SETTINGS);
const direction = settings.layoutDirection ?? 'system';
const rtl = direction === 'rtl' || (direction === 'system' && I18nManager.isRTL);

<GlassSettingsProvider value={settings}>
  <NavigationContainer direction={rtl ? 'rtl' : 'ltr'}>
    <Stack.Navigator
      screenOptions={{
        headerShown: false,
        animation: Platform.select({ android: rtl ? 'ios_from_left' : 'ios_from_right', default: 'default' }),
      }}
    >
      <Stack.Screen name="Home">
        {({ navigation }) => (
          <GlassHomeScreen style={{ flex: 1 }} onOpen={(id) => navigation.navigate(id)} onSettingsChange={setSettings} />
        )}
      </Stack.Screen>
      {/* one route per GLASS_SCREENS id, rendering SCREENS[id] with onBack={navigation.goBack} */}
    </Stack.Navigator>
  </NavigationContainer>
</GlassSettingsProvider>
```

Registering only some screens: guard `onOpen` (`if (id === 'buttons') navigation.navigate(id)`), or Home rows for missing routes do nothing and React Navigation logs an unhandled NAVIGATE.

Status bar: glass screens set their own icons. On iOS under native-stack React Native's `<StatusBar>` is inert (react-native-screens owns it); use the `statusBarStyle` screen option for your own screens. On Android `<StatusBar>` works on your screens, and a glass screen sets its icons again when it shows.

Only native-stack is verified. Bottom tabs, the JS stack and React Native `<Modal>` are untested.

## 13. Rules for apps (what agents get wrong)

1. `style={{ flex: 1 }}` (or another sizing style) on every screen. There is no default size: without it the screen is 0 × 0 and blank.
2. `headerShown: false`: each screen draws its own bar.
3. Feed `onSettingsChange` back into the provider's `value`.
4. Memoise the provider `value` (state or `useMemo`), never an inline object in a component that re-renders: a new object re-sends props to every mounted screen.
5. `onBack` → `navigation.goBack`. No JS `BackHandler` for glass screens.
6. Always pass `direction` to `NavigationContainer`, and pick the Android animation from the direction.
7. Expect window-wide appearance on iOS; use `'system'` or match the app if the app's other screens must not change.
8. Define `icons` and `fontFamily` maps outside components or memoise them.
9. Add font files the React Native way; key icons by SF Symbol name.
10. Do not pass children, titles or other settings as screen props; do not use `'home'` as a `GlassScreenId`.

## 14. Known limits and gotchas

- iOS appearance is window-wide; off-screen mounted screens keep their request; an `Appearance.setColorScheme` made during a request is lost on release (6.3).
- iOS: the status bar stays dark with Home in Dark on a Light system (6.3; to be fixed before release).
- iOS: each screen has its own navigation bar (it slides on push); the back chevron is a `chevron.backward` image, about 2 px (at 3x) taller than the system's.
- iOS: switching the direction on Home scrolls Home's list to the top (6.4).
- iOS: UIKit pieces and covers stay LTR in a forced RTL without the trait override (11).
- Android: back needs a `NavigationEventDispatcherOwner` activity on API 36+, and an intact `Window.Callback` chain below that (7.2). With two visible glass screens, back goes to the last one that entered the window.
- Android, MIUI in dark mode: status icons follow the content (7.3).
- Android, Union: the details card appears about 0.3 s after the menu closes.
- Android Material: the bottom tab bar does not minimize.
- `fontFamily` changed live: on iOS, bars already on screen keep the old font until they are created again.
- Icon and image caches are process-wide and unbounded on both platforms.
- An inner provider replaces, not merges, `icons` and `fontFamily` (4).
- Expo is not supported (no config plugin). The old architecture is not supported.
- `react-native-vector-icons` is only an example devDependency (10.3.0, deprecated upstream in favour of per-family packages); `vectorIcon` works with any set that has the two methods.

## 15. Build, test, verify

From the repository root (Yarn 4 workspace):

```sh
yarn                                   # install
yarn lint && yarn typecheck && yarn test
yarn example start                     # Metro on 8081: no other project's Metro may hold 8081
yarn example ios
yarn example android
cd example/android && ./gradlew :native-dev-kit_react-native-liquid-glass:testDebugUnitTest
```

- CI (`.github/workflows/ci.yml`): lint, typecheck, Jest, the library build (bob) and the Android unit tests. There is no iOS CI.
- iOS XCTest (the pod's test spec, scheme `ReactNativeLiquidGlass-Unit-Tests`) needs the prebuilt React / Hermes / RNDeps pods in the **Debug** flavour; after a Release build, swap them (commands in [CONTRIBUTING.md](CONTRIBUTING.md#ios-xctest)).
- Device checks: build **Release** (`--mode Release` / `--mode release`) so Metro plays no part. Check that every screen opens, back works (the button; on Android the key closes an open sheet, menu or search first; on iOS the edge swipe), Home settings change open screens live, RTL, both Android styles, and `CUSTOMIZE = true` in `example/src/customize.ts` for fonts and icons.
- "Unimplemented component" or a blank red box: the New Architecture is off, or pods/Gradle were not rebuilt. A stale `android/build/generated/autolinking/` after an upgrade: delete it.

## 16. Never do

- Never add React Native children, a JS header or a JS `BackHandler` to glass screens.
- Never invent primitives (`GlassView`, `GlassButton`) or props.
- Never emit `onSettingsChange` from prop setters (a JS ↔ native echo loop).
- Never switch the iOS tab bar minimize behaviour while scrolling (6.5).
- Never recycle the iOS component view, and never dispose the Android composition on detach.
- Never draw an asymmetric `Modifier.border` without the direction key (8).
- Never mirror media controls in RTL.
- Never break the one-line `GLASS_SCREENS` entry format (the Android test's regex).
- Never put `docs/` in the npm package (the media is large; the README uses absolute image URLs).
- Never change an API, default or floor without updating README.md, USAGE.md, docs/, this file, llms.txt and SKILL.md in the same change.

## 17. Docs map

- [USAGE.md](USAGE.md): every component for users, with screenshots and GIFs.
- [README.md](README.md): overview, install, quick start, known issues.
- `docs/` (on GitHub only): [getting-started](docs/getting-started.md), [ios](docs/ios.md), [android](docs/android.md), [screens](docs/screens.md), [settings](docs/settings.md), [react-navigation](docs/react-navigation.md), [layout-direction](docs/layout-direction.md), [android-style](docs/android-style.md), [control-shapes](docs/control-shapes.md), [fonts](docs/fonts.md), [icons](docs/icons.md), [troubleshooting](docs/troubleshooting.md).
- [AGENTS.md](AGENTS.md), [llms.txt](llms.txt), [skills/react-native-liquid-glass/SKILL.md](skills/react-native-liquid-glass/SKILL.md).

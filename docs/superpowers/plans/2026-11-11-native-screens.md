# Native Screens Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** The two native catalog apps' own code becomes the library; React Native shows each native screen and passes it props, in a React Native CLI example that matches the native apps.

**Architecture:** One Fabric component, `LiquidGlassScreen`, with a `screen` prop. iOS hosts the copied SwiftUI screen in a `UIHostingController`; Android hosts the copied Compose screen in a `ComposeView`. JS exports one named component per screen and a settings provider; the example pushes them with React Navigation native-stack.

**Tech Stack:** React Native 0.88.1 (New Architecture, codegen), TypeScript, Jest; Swift / SwiftUI (iOS 27); Kotlin / Jetpack Compose (Compose BOM 2026.02.00, API 26+); React Navigation native-stack (example only).

**Spec:** `docs/superpowers/specs/2026-11-11-native-screens-design.md`

## Global Constraints

- Library root: `/Users/mohamedabdelnaby/Developer/libs/liquid-glass`, branch `feat/native-screens`. Never push.
- iOS source: `/Users/mohamedabdelnaby/Developer/ios/liquidGlass/liquidGlass`. Android source: `/Users/mohamedabdelnaby/Developer/android/app/src/main/java/com/example/myapplication` (+ `app/src/main/res/drawable-nodpi`, `app/src/test`). Read only: never edit or commit in those two projects.
- Native code is copied unchanged. Allowed edits: Android package names (`com.example.myapplication` → `com.mohamedabdelnaby.liquidglass.catalog`, `R` import → `com.mohamedabdelnaby.liquidglass.R`); the live-tint fix from commit 53f95ae in `DrawBackdropModifier.kt`; on iOS only the `ContentView` struct's settings source and row action (Task 4); and the edits Tasks 6–8 make for direction, fonts and icons (spec section "Additions"). Any other edit needs a line in the task report saying why.
- Minimum OS: iOS 27.0; Android API 26. React Native 0.80+, New Architecture only (Fabric + codegen; no old-architecture view managers or bridge modules).
- The library has no dependency on React Navigation or react-native-screens; only the example has.
- Yarn only. Example: React Native CLI (no Expo), Metro on port 8081 only, the default (user, 2026-11-11: "close all ports and run 8081 only"; Task 1's 8084 settings were removed in f1016a7). Start no other dev server on any port; never touch `~/node_modules`.
- Commit subjects lower-case (commitlint); never `--no-verify`.
- iOS: `pod install` and `xcodebuild` need `LANG=en_US.UTF-8 LC_ALL=en_US.UTF-8`. Simulator: iPhone 18 Pro, UDID `459FB3AD-93EC-4C18-ADE6-2A0FC3C24DED`.
- Android device: phone `5ef3365c` only (adb at `~/Library/Android/sdk/platform-tools/adb`), no emulator. Allowed: install, launch, logcat, screencap, input. Before every tap or swipe, check `dumpsys activity activities | grep -m1 topResumedActivity` shows the app you mean. Never change settings, never unlock the phone, never bypass a MIUI install prompt (stop and report). `adb reverse tcp:8081 tcp:8081` drops when the phone is replugged; re-apply it.
- Screen ids (home order): `glassEffect, glassView, interactive, buttons, container, morphing, union, toolbar, floatingHeader, tabBar, sheets, controls, text, transitions, search, editSearch, scrollEdge, splitView, zoomTransitions, alerts, recipes`, plus `home`.

## Review Focus

1. Safe areas: hosted screens keep their bars below the status bar and above the home indicator / navigation bar, as in the native apps (Task 5 device check, both platforms).
2. Back with something open: Android's back button closes an open sheet, menu or dialog first, and only then pops the screen; iOS swipe-back from the left edge pops the React Native screen (Task 5 device check; host fix in Task 3 or 4 if it fails).
3. Returning to Home keeps Home's scroll position and settings (React Navigation detach and re-attach) (Task 3 and Task 4 device checks).
4. Appearance: the phone starts in dark mode, the simulator in light; Home's appearance choice changes Home and every screen opened after it, and the status bar icons stay readable (Task 5).
5. Presentations from hosted iOS screens: full-screen covers (Tab Bar, Sidebar & Inspector), sheets, popovers, alerts and menus present and dismiss (Task 4 device check).

---

### Task 1: React Native CLI example in place of the Expo example

**Files:**
- Delete: `example/` (the Expo app) except what step 1 says to keep
- Create: `example/` as a React Native CLI app (package `@native-dev-kit/react-native-liquid-glass-example`, the existing workspace name)

**Interfaces:**
- Produces: `yarn example start` (Metro on 8084), `yarn example android`, `yarn example ios`; Android applicationId and iOS bundle id `mohamedabdelnaby.reactnativeliquidglass.example`; `example/src/App.tsx` as the app root; `@react-navigation/native`, `@react-navigation/native-stack`, `react-native-screens`, `react-native-safe-area-context` installed.

- [ ] **Step 1: Generate the app.** `npx @react-native-community/cli@latest init ReactNativeLiquidGlassExample --version 0.88.1 --skip-install --skip-git-init` in a temp folder, then move it to `example/`. Keep from the old example: `assets/` is not needed (delete); keep the library linking from the old `metro.config.js`, `babel.config.js` and `react-native.config.js` (the create-react-native-library pattern that resolves the library from `../src` and links it with autolinking), adapted to the CLI app. `example/src/App.tsx` renders a `View` with the text "Liquid Glass".
- [ ] **Step 2: Port 8084.** `start` runs `react-native start --port 8084`; `android` runs `react-native run-android --port 8084`; `ios` runs `react-native run-ios --port 8084`. iOS Podfile `platform :ios, '27.0'`.
- [ ] **Step 3: Install and build.** `yarn` at the root; `cd example/ios && pod install`; `cd example/android && ./gradlew :app:assembleDebug`; `yarn lint && yarn typecheck && yarn test` at the root.
  Expected: all succeed (the library still contains the sub-project 1 views; the example does not use them).
- [ ] **Step 4: Run.** Start Metro on 8084 from `example/`. Install the debug APK on the phone (`adb -s 5ef3365c install -r`), `adb reverse tcp:8084 tcp:8084`, launch; run on the simulator. Expected: "Liquid Glass" on both, loaded from Metro (a change to App.tsx shows after reload). Screenshot both into the report's folder.
- [ ] **Step 5: Commit** — `git add -A example package.json yarn.lock && git commit -m "chore(example): react native cli example in place of expo"`

### Task 2: JS API for the native screens

**Files:**
- Create: `src/LiquidGlassScreenNativeComponent.ts`, `src/screens.tsx`, `src/__tests__/screens.test.tsx`
- Modify: `src/settings.tsx`, `src/types.ts`, `src/index.tsx`, `src/__tests__/settings.test.tsx`, `package.json` (`codegenConfig.ios.components`)
- Delete: `src/GlassView.tsx`, `src/GlassButton.tsx`, `src/resolveGlass.ts`, `src/icons.ts`, `src/support.ts` and their tests (keep `src/GlassViewNativeComponent.ts` and `src/GlassButtonNativeComponent.ts` until Task 4 so the native builds keep working)

**Interfaces:**
- Produces (JS): `GlassSettings` (the spec's type, exactly), `DEFAULT_SETTINGS` (appearance `'system'`, tint `null`, layoutDirection `'system'`, clear `true`, blur 3, white 0.25, black 0.05, edgeLight 0.15, refraction `{height: 14, amount: 24}`, opaque `false`, liquid `true`, dropColor `null`, the five control switches `true`), `GlassSettingsProvider({value, children})`, `useGlassSettings(): GlassSettings` (defaults merged under the provider value), `type GlassScreenId`, `GLASS_SCREENS: ReadonlyArray<{id: GlassScreenId; title: string}>` (the 21 catalog screens in home order with the iOS titles), the 22 named components from the spec, each `(props: GlassScreenProps) => JSX.Element`, where `GlassScreenProps = {onBack?: () => void; onOpen?: (screen: GlassScreenId) => void; onSettingsChange?: (settings: GlassSettings) => void; style?: StyleProp<ViewStyle>; testID?: string}`.
- Produces (native, codegen component `LiquidGlassScreen`, iOS class `RNLiquidGlassScreen`): props `screen: string`, `appearance: string`, `layoutDirection: string`, `tintColor: ColorValue`, `clear: boolean`, `blur: Float`, `white: Float`, `black: Float`, `edgeLight: Float`, `refractionHeight: Float`, `refractionAmount: Float`, `opaque: boolean`, `liquid: boolean`, `dropColor: ColorValue`, `sliderLiquid`, `switchLiquid`, `segmentedLiquid`, `tabBarLiquid`, `tabBarDrop: boolean`; events `onBack: DirectEventHandler<{}>`, `onOpen: DirectEventHandler<{screen: string}>`, `onSettingsChange: DirectEventHandler<{appearance: string; layoutDirection: string; tint: string; clear: boolean; blur: Float; white: Float; black: Float; edgeLight: Float; refractionHeight: Float; refractionAmount: Float; opaque: boolean; liquid: boolean; dropColor: string; sliderLiquid: boolean; switchLiquid: boolean; segmentedLiquid: boolean; tabBarLiquid: boolean; tabBarDrop: boolean}>`. In events, colours are `'#RRGGBBAA'` strings and `''` means none.

- [ ] **Step 1: Write the failing tests** (`src/__tests__/screens.test.tsx`)

```tsx
test('each named screen sends its id', () => {
  const r = render(<GlassButtonsScreen testID="s" />);
  expect(r.getByTestId('s').props.screen).toBe('buttons');
  expect(render(<GlassHomeScreen testID="h" />).getByTestId('h').props.screen).toBe('home');
});
test('GLASS_SCREENS lists the 21 screens in home order', () => {
  expect(GLASS_SCREENS.map((s) => s.id)).toEqual([
    'glassEffect', 'glassView', 'interactive', 'buttons', 'container', 'morphing', 'union',
    'toolbar', 'floatingHeader', 'tabBar', 'sheets', 'controls', 'text', 'transitions', 'search',
    'editSearch', 'scrollEdge', 'splitView', 'zoomTransitions', 'alerts', 'recipes',
  ]);
});
test('provider settings reach the native props and update live', () => {
  const r = render(<GlassSettingsProvider value={{ tint: 'purple', blur: 5 }}><GlassTextScreen testID="s" /></GlassSettingsProvider>);
  expect(r.getByTestId('s').props).toMatchObject({ tintColor: 'purple', blur: 5, white: 0.25, appearance: 'system' });
  r.rerender(<GlassSettingsProvider value={{ tint: null }}><GlassTextScreen testID="s" /></GlassSettingsProvider>);
  expect(r.getByTestId('s').props.tintColor).toBeNull();
});
test('events reach the callbacks', () => {
  const onBack = jest.fn(), onOpen = jest.fn(), onSettingsChange = jest.fn();
  const r = render(<GlassHomeScreen testID="h" onBack={onBack} onOpen={onOpen} onSettingsChange={onSettingsChange} />);
  fireEvent(r.getByTestId('h'), 'back');
  fireEvent(r.getByTestId('h'), 'open', { nativeEvent: { screen: 'controls' } });
  fireEvent(r.getByTestId('h'), 'settingsChange', { nativeEvent: { ...nativeDefaults, tint: '#AF52DEFF', blur: 7 } });
  expect(onBack).toHaveBeenCalledTimes(1);
  expect(onOpen).toHaveBeenCalledWith('controls');
  expect(onSettingsChange).toHaveBeenCalledWith(expect.objectContaining({ tint: '#AF52DEFF', blur: 7, refraction: { height: 14, amount: 24 } }));
});
```

`nativeDefaults` is the event payload built from `DEFAULT_SETTINGS` (colours `''`). An event colour `''` becomes `null` in the `GlassSettings` passed to `onSettingsChange`.

- [ ] **Step 2: Run, see them fail** — `yarn test src/__tests__/screens.test.tsx` → FAIL (module not found).
- [ ] **Step 3: Implement.** `screens.tsx`: one internal `GlassScreen` that reads `useGlassSettings()`, flattens it to the native props (`refraction` → `refractionHeight`/`refractionAmount`, `tint` → `tintColor`), maps the three events to the callbacks; 22 one-line named exports. Update `settings.test.tsx` for the new `DEFAULT_SETTINGS`.
- [ ] **Step 4: Run** — `yarn lint && yarn typecheck && yarn test` → PASS. (The old engine's `IconsTest.kt` reads `src/icons.ts`, so the Gradle unit tests fail from here until Task 3 deletes the engine; the example still builds.)
- [ ] **Step 5: Commit** — `git add -A src package.json && git commit -m "feat: js api for the native screens"`

### Task 3: Android — the Android app's code as the library

**Files:**
- Delete: `android/src/main/java/com/mohamedabdelnaby/liquidglass/{GlassView,GlassViewProps,GlassButtonView,GlassViewManager,GlassButtonManager}.kt`, `host/ViewTreeBackdrop.kt`, `engine/` (all), `android/src/test/.../GlassViewPropsTest.kt` and the engine tests
- Create: `android/src/main/java/com/mohamedabdelnaby/liquidglass/catalog/**` (copy of the app's `glass/`, `liquid/`, `screens/`, `ui/`, `HomeScreen.kt`, `GlassComponent.kt`), `android/src/main/res/drawable-nodpi/backdrop_{fjord,jellyfish,snow,valley}.jpg`, `android/src/test/java/com/mohamedabdelnaby/liquidglass/catalog/glass/{GlassSettingsTest,GlassStyleTest,GlassContainerTest}.kt` (copied), `android/src/main/java/com/mohamedabdelnaby/liquidglass/LiquidGlassScreenView.kt`, `LiquidGlassScreenManager.kt`, `android/src/test/java/com/mohamedabdelnaby/liquidglass/ScreenIdsTest.kt`
- Modify: `host/ComposeHost.kt` (keep only what one hosted screen needs), `ReactNativeLiquidGlassViewPackage.kt`, `android/build.gradle` (dependencies the copied code imports, versions from the app's `gradle/libs.versions.toml`)

**Interfaces:**
- Consumes: the codegen component from Task 2 (`LiquidGlassScreenManagerInterface` / `Delegate`).
- Produces: `LiquidGlassScreenManager` registered in the package; `fun screenComponent(id: String): GlassComponent?` (`id.replaceFirstChar(Char::uppercase)` → `GlassComponent.valueOf`, null when unknown) in `LiquidGlassScreenView.kt`.
- Mappings: `appearance` `'system'|'light'|'dark'` ↔ `AppearanceMode.System|Light|Dark`; `blur`, `refraction*` in dp. Android ignores `layoutDirection` and sends it unchanged from its current prop in `onSettingsChange`.

- [ ] **Step 1: Write the failing test** (`ScreenIdsTest.kt`)

```kotlin
@Test fun everyJsScreenIdMapsToAGlassComponent() {
  val ids = Regex("""\{ id: '(\w+)'""").findAll(File("../src/screens.tsx").readText()).map { it.groupValues[1] }.toList()
  assertEquals(21, ids.size)
  ids.forEach { assertNotNull(it, screenComponent(it)) }
  assertEquals(GlassComponent.entries.map { it.name.replaceFirstChar(Char::lowercase) }, ids)
}
```

- [ ] **Step 2: Run, see it fail** — `cd example/android && ./gradlew :native-dev-kit_react-native-liquid-glass:testDebugUnitTest` → FAIL (does not compile).
- [ ] **Step 3: Copy the app's code.** Copy as listed; rename packages and the `R` import only. Re-apply 53f95ae's `DrawBackdropModifier.kt` change (`git show 53f95ae`) to the copied file. Add the app's dependencies the copied code imports (not navigation-compose, datastore or serialization: only the app's `MainActivity` uses those).
- [ ] **Step 4: Host.** `LiquidGlassScreenView` (a `ComposeHost`) renders, as the app's `LiquidGlassApp` does: `LiquidGlassTheme(mode)` + `CompositionLocalProvider(LocalOnBack provides { onBack event }, LocalGlassWindowBackdrop provides remember { GlassWindowBackdrop() }, LocalGlassSettings provides settingsFromProps)`, then `HomeScreen(mode, onModeChange → onSettingsChange event, onOpen → onOpen event with the screen id, settings, onSettingsChange → onSettingsChange event)` for `home`, else `screenComponent(id)?.screen()`. Settings live in Compose state set by the prop setters, so a prop change recomposes without recreating the view. `ComposeHost` keeps the lifecycle owner and the manual layout; it drops the backdrop recording, `GlassLayerFrame`, `GlassTheme` and the re-keying on attach, so the screen's state survives detach and re-attach.
- [ ] **Step 5: Run** — the Gradle unit tests → PASS (copied app tests + `ScreenIdsTest`); `./gradlew :app:assembleDebug` → BUILD SUCCESSFUL; `yarn lint && yarn typecheck && yarn test` → PASS.
- [ ] **Step 6: Device check.** In `example/src/App.tsx` (temporary until Task 5 wires navigation, keep it in the commit): show `GlassHomeScreen` full screen and, on `onOpen`, the chosen screen with `onBack` returning to Home. On the phone: Home looks like the native app's Home (`com.example.myapplication`, open it with `am start` for reference screenshots); open five screens (Glass Effect, Controls, Tab Bar, Sheets & Menus, Recipes) and go back; change the tint and blur on Home and see Home repaint at once; 0 `APP_SCOUT_` lines in logcat.
- [ ] **Step 7: Commit** — `git add -A android example/src && git commit -m "feat(android): host the android app's screens"`

### Task 4: iOS — the iOS app's code as the library

**Files:**
- Delete: `ios/GlassMapping.swift`, `ios/LiquidGlassButton.swift`, `ios/LiquidGlassEffectView.swift`, `ios/RNGlassButton.{h,mm}`, `ios/RNGlassView.{h,mm}`, `ios/Tests/*`, `src/GlassViewNativeComponent.ts`, `src/GlassButtonNativeComponent.ts` (and their `codegenConfig` entries)
- Create: `ios/Catalog/` (copy of `ContentView.swift`, `Screens/*.swift`, `Shared/*.swift`; not `liquidGlassApp.swift`), `ios/Host/LiquidGlassScreenHost.swift`, `ios/RNLiquidGlassScreen.{h,mm}`, `ios/Tests/ScreenIdsTests.swift`
- Modify: `ReactNativeLiquidGlass.podspec` (`platforms` iOS `27.0`; `swift_version` `5.0`; `pod_target_xcconfig` with `SWIFT_DEFAULT_ACTOR_ISOLATION = MainActor`, `SWIFT_APPROACHABLE_CONCURRENCY = YES`, `SWIFT_UPCOMING_FEATURE_MEMBER_IMPORT_VISIBILITY = YES`, as the app's project), `example/ios/Podfile` (`:testspecs => ['Tests']`)

**Interfaces:**
- Consumes: the codegen component from Task 2 (props and events by name).
- Produces: `@objc public final class LiquidGlassScreenHost: UIView` with `@objc` setters for every prop and `@objc` closures `onBack`, `onOpen(String)`, `onSettingsChange([String: Any])` that `RNLiquidGlassScreen.mm` forwards to the event emitter.
- Mappings: `appearance` `'system'|'light'|'dark'` ↔ `AppearanceMode.system|light|dark`; `layoutDirection` `'system'|'ltr'|'rtl'` ↔ `LayoutDirectionMode.system|leftToRight|rightToLeft`. iOS ignores the Android-only props and sends the Android-only fields of `onSettingsChange` unchanged from its current props.

- [ ] **Step 1: Write the failing test** (`ios/Tests/ScreenIdsTests.swift`)

```swift
func testEveryScreenIdIsAGlassComponent() {
  let ids = ["glassEffect", "glassView", "interactive", "buttons", "container", "morphing", "union", "toolbar", "floatingHeader", "tabBar", "sheets", "controls", "text", "transitions", "search", "editSearch", "scrollEdge", "splitView", "zoomTransitions", "alerts", "recipes"]
  XCTAssertEqual(GlassComponent.allCases.map(\.rawValue), ids)
}
```

(`GlassComponent` is `String`-backed in the copied `ContentView.swift`; if its raw values are not these names, the test fails and the report says so; do not change the enum.)

- [ ] **Step 2: Run, see it fail** — `xcodebuild test` for the pod's test spec in the example workspace on the simulator → FAIL (no such type).
- [ ] **Step 3: Copy the app's code** unchanged into `ios/Catalog/`. The only edit, in the copied `ContentView` struct: its three `@AppStorage` settings and the system layout direction come from the host (bindings that send `onSettingsChange`), and each `NavigationLink(value:)` row becomes a row with the same look that calls `onOpen(component.rawValue)` instead of pushing.
- [ ] **Step 4: Host.** `LiquidGlassScreenHost` owns a `UIHostingController` whose root reads an `@Observable` model filled by the prop setters (so prop changes update without a new controller). Root view: `NavigationStack { screen }`, with the environment the copied `ContentView` sets (`\.glassTint`, `preferredColorScheme`, `\.layoutDirection`). Screens other than Home get `ToolbarItem(placement: .topBarLeading) { Button(action: onBack) { Image(systemName: "chevron.backward") } }`. Tint: a colour from JS tints the glass; Home's picker shows the `GlassTintChoice` whose colour matches (else None); picking a choice sends its colour as `'#RRGGBBAA'` and Home then shows that choice. The hosting controller is added as a child of the React view controller when the view enters a window, and removed when it leaves.
- [ ] **Step 5: Run** — `pod install`; the XCTest → PASS; the example builds for the simulator; `yarn lint && yarn typecheck && yarn test` → PASS; Android unit tests and `assembleDebug` still PASS (codegen changed).
- [ ] **Step 6: Device check (simulator).** Build the native iOS app (`/Users/mohamedabdelnaby/Developer/ios/liquidGlass`, scheme `liquidGlass`, into DerivedData) for reference screenshots. In the example: Home matches the native Home; open every screen, compare with the native app's same screen, go back; Tab Bar and Sidebar & Inspector covers open and close; Sheets & Menus sheet, popover and menu; Alerts & Dialogs alert, confirmation dialog and context menu; the tint picker repaints Home at once; after going back, Home keeps its scroll position. Note every visible difference in the report.
- [ ] **Step 7: Commit** — `git add -A ios src package.json ReactNativeLiquidGlass.podspec example && git commit -m "feat(ios): host the ios app's screens"`

### Task 10: Android behaves like iOS (the user's list; runs after Task 4, before Task 5)

Spec section "Additions → Android like iOS". Edits to copied Android code are allowed; list each in the report.

**Files:**
- Modify: `android/src/main/java/com/mohamedabdelnaby/liquidglass/catalog/**` and the host files, in the library only. The native Android app (`/Users/mohamedabdelnaby/Developer/android`) stays as it is (the user's choice); use it only for comparison.

**Inputs:** iOS drop measurements in `ios-drop-measurements.md`, the phone test in `android-vs-ios-test.md` (both in the plan workspace), the iOS screens in `ios/Catalog/Screens/*.swift`, and the iOS shots `task-4-shots/native-*.png`.

- [ ] **Step 1: Items.** Each item is a difference from iOS that the user saw on Android:
  1. Top tabs (segmented) drop: "more bouncy on x axis… make it like ios". Its travel, scaleX/scaleY peaks and wobble count match the iOS measurements.
  2. Glass View screen: the Regular/Clear tabs get the same liquid drop as the top tabs.
  3. Toolbar: pressing Share must not make the header flicker upward, while the share sheet opens or when it closes.
  4. Floating Header: on the status bar option, the status bar area shows a blurred background, not a linear gradient. Share has no header flicker, as in item 3. In light appearance, the status bar icons are dark.
  5. Union: showing details must not make the buttons flicker. The alert it adds has top padding, so it sits clear of the status bar as on iOS.
  6. Morphing: the button press animates like iOS (`GlassMorphingScreen.swift`).
  7. Every `hosting` or `port` difference in `android-vs-ios-test.md`, except the ones marked `planned` (Tasks 5–8) or `platform`.
- [ ] **Step 2: Cause first.** Reproduce each item on the phone (screencap bursts, or `screenrecord` for motion), read the iOS and Android code, and name the cause before you change anything.
- [ ] **Step 3: Fix** in the library copy, then mirror it into the native app. Wherever the logic can run without Compose (for example the drop springs), add a JVM test. Run the library unit tests and `assembleDebug` for the example and for the native app → PASS.
- [ ] **Step 4: Device check** on the phone, in the example and in the native app: each item before and after, with frames for every animation, compared against the iOS numbers and shots.
- [ ] **Step 5: Commit** — `git add -A android && git commit -m "fix(android): match ios on drops, headers, union and morphing"` (more commits are fine; the native app has no git).

### Task 11: Android behaves like iOS, second list (runs after Task 5)

Same rules as Task 10: library only; the native Android app is used only for reading. List each edit in the report.

- [ ] **Step 1: Items** (the user's words, 2026-11-19):
  1. Glass View top tabs: "when it convert from liquid to active background color its become rectangular flicker then retrun to normal… make it like scroll edge screen". Settling from the lens to the pill must not show a rectangle in any frame; it should match the Scroll Edge screen's segmented control.
  2. "when i back from another screen to main it flash whit background between them": no white frame while going back, or pushing, in light or dark.
     Also, in the user's words: "when i pop to screen sliding to back i frrl it flash no slide". Push and pop both slide like iOS: the new screen comes in from the trailing edge, and on pop it slides back out while the screen below slides in from a third of the way. On Android that is native-stack `animation: 'ios_from_right'` (Task 6 adds the RTL variant). This part of Task 6, Step 4 moves here.
  3. Container: "the item is bouncing at first loading". No bounce on first appearance.
  4. Floating Header, Soft: the gradient becomes a blur like iOS (the Status Bar option already is).
  5. Scroll Edge: switching Auto / Soft / Hard animates like iOS, not "so ugly".
  6. Zoom Transitions: the icon in the back button's row has a "3" badge that is clipped while the zoom opens and closes. It must never be clipped.
  7. Morphing: more padding, and a smoother animation.
  8. Glass Transitions: showing and hiding the badge animates more smoothly, like iOS.
- [ ] **Step 2: Cause first.** Reproduce each item with frames on the phone (Release build; port 8081 belongs to the user's other app), compare with the iOS app and shots, and name the cause before you change anything.
- [ ] **Step 3: Fix and test.** Add JVM tests where the logic runs without Compose. Library unit tests and `assembleRelease` → PASS.
- [ ] **Step 4: Device check:** before and after frames for every item.
- [ ] **Step 5: Commit** — `git commit -m "fix(android): smoother tabs, transitions and edges like ios"` (more commits are fine).

### Task 12: Native Android style (runs after Task 6, before Task 7)

Spec section "Additions → Native Android style".

**Files:**
- Modify: `src/types.ts`, `src/settings.tsx`, `src/screens.tsx`, `src/LiquidGlassScreenNativeComponent.ts` (prop `androidStyle: string`, `'glass'` default), Jest tests; the Android host (reads the prop into `LocalAndroidStyle`); the shared building blocks under `catalog/glass/` and `catalog/liquid/` (each renders its Material 3 counterpart when the style is `material`); `example/` (a Glass/Material switch on Home's example header or in `customize.ts`).
- Test: Jest; `android/src/test/.../AndroidStyleTest.kt`

**Interfaces:**
- Produces (JS): `GlassSettings.androidStyle?: 'glass' | 'material'`, also a prop on every screen (wins over the provider).
- Produces (native): `val LocalAndroidStyle = staticCompositionLocalOf { AndroidStyle.Glass }`; `enum class AndroidStyle { Glass, Material }`; `fun androidStyleOf(value: String): AndroidStyle` (unknown → Glass).

- [ ] **Step 1: Failing tests:** Jest: provider `androidStyle: 'material'` reaches every screen's prop; a screen's own prop wins; the default is `'glass'`. Kotlin: `androidStyleOf("material") == Material`, `("glass")` and `("bogus")` → Glass.
- [ ] **Step 2: Implement** the prop path and `LocalAndroidStyle`. Then, one building block at a time, the Material counterpart: top bar → `TopAppBar` with `Icons.AutoMirrored.Filled.ArrowBack`; glass buttons → `Button`/`FilledTonalButton`/`IconButton`; switch → `Switch`; slider → `Slider`; segmented → `SingleChoiceSegmentedButtonRow`; bottom tab bar → `NavigationBar`; menus → `DropdownMenu`; dialogs → `AlertDialog`; sheets → `ModalBottomSheet`; search → `SearchBar`; glass surfaces → `Surface(tonalElevation)`. Text uses `MaterialTheme.typography`. → PASS.
- [ ] **Step 3: Device check** on the phone (Release build) with `material`: all 21 screens open, controls work, back works (including Task 5's back-key routing), light/dark and RTL work; with `glass` nothing changed (compare with Task 11's shots).
- [ ] **Step 4: Commit** — `git commit -m "feat(android): native material style option"`

### Task 13: iOS header flicker on push (runs now, alongside Task 5's Android fix)

- [ ] **Step 1: Reproduce.** In the user's words: "when i open screen on ios the header is flicker fix it please". On the iPhone simulator (Release build; port 8081 belongs to the user's other app), push several screens from Home through native-stack. Record video (`simctl io recordVideo`), extract frames, and name what the header does: a title or large-title jump, safe-area inset arriving late, the bar background or back button popping in, or the appearance changing. Compare with the native iOS app pushing the same screen.
- [ ] **Step 2: Fix in the iOS host** (`ios/Host/`, `ios/RNLiquidGlassScreen.mm`), not in the copied SwiftUI screens. For example: give the hosting controller its final size and safe area before the push animation starts, or lay it out synchronously when it is first attached. Add an XCTest if the logic can run without the screen.
- [ ] **Step 3: Device check:** frames before and after for at least 5 screens (one with a large title, one inline, Search, Tab Bar, Floating Header). No header change across the push in any frame, and still 0.01% against the native app.
- [ ] **Step 4: Commit** — `git commit -m "fix(ios): steady header when a screen is pushed"`

### Task 14: Docs for people and agents (runs now, alongside Task 6; Task 9 becomes the final docs pass + CI)

The user's choices (2026-11-19): npm name `@native-dev-kit/react-native-liquid-glass` (npm account `native-dev-kit`); a README plus `docs/` guides; agent files `CONTEXT.md`, `AGENTS.md`, an agent skill and `llms.txt`; written now and updated by each later feature task.

**Files:**
- Create: `README.md` (replace), `docs/` guides (`getting-started.md`, `screens.md`, `settings.md`, `react-navigation.md`, `layout-direction.md`, `android-style.md`, `fonts.md`, `icons.md`, `troubleshooting.md`), `CONTEXT.md`, `AGENTS.md`, `skills/react-native-liquid-glass/SKILL.md`, `llms.txt`
- Modify (last step, only after Task 6 is committed): `package.json` (`name`, `files` to ship `CONTEXT.md`, `AGENTS.md`, `llms.txt`, `skills`, `docs`), and every import of the old name (example, tests, configs).

- [ ] **Step 1: Facts first.** Read `src/`, the spec, and the task reports for what exists and how it behaves on each platform. Every API, prop, default, floor and limit in the docs comes from code or a report, never from memory.
- [ ] **Step 2: README:** what it is (the native apps' own screens), screenshots or a pointer to them, install (bare RN CLI, New Architecture, iOS 27 / Android 26, compileSdk 37 / AGP 9.1, RN 0.88 verified, `pod install`), a 15-line quick start with React Navigation, the 22 components, links to the guides, the Expo status, and the license.
- [ ] **Step 3: Guides:** one per topic. Planned features (Material style, fonts, icons) get their guide now, marked "coming in the next version", with the API from the spec.
- [ ] **Step 4: Agent files.**
  - `CONTEXT.md`: what an agent must know to use the library correctly. That covers the mental model (native screens, not building blocks), the exact API, settings per platform, the React Navigation recipe, rules (memoise the provider value; feed `onSettingsChange` back; header hidden; Release vs Debug and Metro), common mistakes, and how to verify on a device.
  - `AGENTS.md`: short; it points to `CONTEXT.md`, plus repo commands for contributors.
  - `SKILL.md`: front matter with `name` and `description` that trigger on "liquid glass" or "glass screens" in React Native. The body is the install and use steps and points to `CONTEXT.md`.
  - `llms.txt`: in the llms.txt format, linking the docs.
- [ ] **Step 5: Check:** every code sample type-checks against `src/`, by pasting them into a scratch `.tsx` under `example/` and running `yarn typecheck`, then deleting it. `npm pack --dry-run` lists the agent files. Links resolve.
- [ ] **Step 6: Commit** — `git commit -m "docs: readme, guides and agent context"`; then, after Task 6, the rename — `git commit -m "chore: publish as @native-dev-kit/react-native-liquid-glass"`.

### Task 15: Material segmented buttons in RTL (bug, runs after Task 7)

User, 2026-11-21, with a screenshot (`task-12-shots/round1/material-rtl/home-top.png`): in RTL the Material segmented buttons keep their order mirrored correctly but put the rounded ends on the inner sides (the first segment, at the right, is rounded on its left).

**Files:**
- Modify: the Material segmented row (`catalog/glass/MaterialBars.kt` `MaterialTabs`) or whatever the root cause turns out to be (find it with superpowers:systematic-debugging; fix it once in shared code).
- Test: a Compose/JVM test if the cause is a pure function; otherwise the device check is the proof.

- [ ] **Step 1: Reproduce** on the phone (Release build): Home in Material, direction RTL (set at runtime and at launch). Screenshot.
- [ ] **Step 2: Root cause and fix** in the shared building block; every Material segmented row (Home, Controls, Recipes, Floating Header, Scroll Edge…) gets it. Check other start/end shapes in Material for the same cause (list segments, chips, sheets).
- [ ] **Step 3: Device check** in RTL and LTR, runtime switch both ways; glass unchanged.
- [ ] **Step 4: Commit** — `git commit -m "fix(android): material segmented ends in rtl"`

### Task 16: Android control shapes (runs after Task 15, before Task 8)

Spec section "Additions → Android control shapes".

**Files:**
- Modify: `src/types.ts`, `src/settings.tsx`, `src/screens.tsx`, `src/LiquidGlassScreenNativeComponent.ts` (prop `androidControlShape: string`, `'default'` default), Jest tests; the Android host (reads the prop into `LocalControlShape`, echoes it in `onSettingsChange`); iOS host accepts and ignores the prop; the building blocks for switch, slider, segmented control and bottom tab bar (each renders the shaped control first when the shape is not `default`, in both styles); Home (a shape picker, Android only); `example/`; docs (`docs/android-style.md` or a new `docs/control-shapes.md`, README/CONTEXT/AGENTS/SKILL/llms.txt lines).
- Create: `android/src/main/java/com/mohamedabdelnaby/liquidglass/catalog/shapes/` (one file per control: `ShapedSwitch.kt`, `ShapedSlider.kt`, `ShapedTabs.kt`, `ShapedTabBar.kt`, plus `ControlShape.kt`).
- Test: Jest; `android/src/test/.../ControlShapeTest.kt`

**Interfaces:**
- Produces (JS): `GlassSettings.androidControlShape?: 'default' | 'pill' | 'labeled' | 'squared' | 'glow'`, also a prop on every screen (wins over the provider).
- Produces (native): `enum class ControlShape { Default, Pill, Labeled, Squared, Glow }`; `fun controlShapeOf(value: String): ControlShape` (unknown → Default); `val LocalControlShape = staticCompositionLocalOf { ControlShape.Default }`.

- [ ] **Step 1: Failing tests:** Jest: provider `androidControlShape: 'glow'` reaches every screen's prop; a screen's own prop wins; default `'default'`; Home's `onSettingsChange` carries it. Kotlin: `controlShapeOf` for each value and `"bogus"` → Default.
- [ ] **Step 2: Implement** the prop path, then each shaped control per the spec (all four shapes × four controls), keeping the default path byte-identical. Semantics: switch `toggleableState` + label; slider one node with label, value and `setProgress`; tabs `selected` + role Tab. → PASS.
- [ ] **Step 3: Device check** on the phone: every shape × {glass, material} × {light, dark} × {LTR, RTL} on Controls, Home, Tab Bar, Recipes; `default` pixel-identical to Task 15's head; iOS Release build still passes and looks unchanged.
- [ ] **Step 4: Commit** — `git commit -m "feat(android): control shapes option"`

### Task 17: iOS bottom tab bar minimize follows the scroll (bug, runs now)

User, 2026-11-21: on iPhone, the first scroll down minimizes the bottom tab bar, but after that it does not restore, or restores without animation. Minimize and expand must follow the scroll, animated, including when the finger stays down and scrolls back. Fix in BOTH places (user: "Both"): the library's `ios/Catalog/Screens/GlassTabBarScreen.swift` and the native app's `/Users/mohamedabdelnaby/Developer/ios/liquidGlass/liquidGlass/Screens/GlassTabBarScreen.swift` get the same change (native app: edit only, no commit).

**Files:**
- Modify: both `GlassTabBarScreen.swift` copies (`TabBarDemo` / `DemoTabContent`: the `isScrollingUp` switch between `.onScrollDown` and `.never` is the suspect); the library's host only if the root cause is in hosting.

- [ ] **Step 1: Reproduce** on the simulator in the native app and in the example (Release): every tab bar variant, scroll down, up, down; finger held while reversing.
- [ ] **Step 2: Root cause** (superpowers:systematic-debugging) and fix so the system's own interactive minimize/expand drives the bar, animated both ways, every time, in all four variants.
- [ ] **Step 3: Check** both apps on the simulator (screen recordings or frame sequences of minimize → expand → minimize, and of a held-finger reversal), LTR and RTL; Android untouched.
- [ ] **Step 4: Commit** (library only) — `git commit -m "fix(ios): tab bar minimize follows the scroll"`

### Task 18: iOS Home drawn mirrored after a direction switch (bug, runs after Task 7)

User, 2026-11-23, with a simulator recording (Home in the RN example): switching Direction RTL → LTR / System leaves Home drawn as a mirror image (flipped glyphs: "ʞɿɒᗡ", reversed labels) until relayout; it flips back and forth as the setting changes. Task 6 had ruled this native SwiftUI behaviour; the user now wants it fixed. Fix in BOTH copies of the iOS Home (`ios/Catalog/ContentView.swift` in the library; `/Users/mohamedabdelnaby/Developer/ios/liquidGlass/liquidGlass/ContentView.swift` in the native app, edit only, keeping the two copies' existing differences) and in the library's iOS host if the RN path adds to it. Swift only, no JS changes.

- [ ] **Step 1: Reproduce** on the simulator in both apps: RTL → LTR → System → RTL, at the top and scrolled; recording.
- [ ] **Step 2: Root cause** (superpowers:systematic-debugging; suspect the UIKit-backed List keeping the old direction's flip after the `layoutDirection` environment changes) and fix so every switch redraws Home correctly at once, keeping the scroll position if possible.
- [ ] **Step 3: Check** every switch in both apps (recording), plus a pushed screen and a full-screen demo after switching; Android untouched.
- [ ] **Step 4: Commit** (library only) — `git commit -m "fix(ios): home redraws after a direction switch"`

### Task 5: Every native screen through React Navigation

**Files:**
- Modify: `example/src/App.tsx`; `example/package.json` (only if a dependency is missing)

**Interfaces:**
- Consumes: Task 2's exports.

- [ ] **Step 1: Wire the example.** `NavigationContainer` > native-stack with `headerShown: false`; `useState<GlassSettings>(DEFAULT_SETTINGS)` in `App` feeding one `GlassSettingsProvider` above the `NavigationContainer`; route `Home` renders `GlassHomeScreen` with `onOpen={(id) => navigation.navigate(id)}` and `onSettingsChange={setSettings}`; one route per entry of `GLASS_SCREENS` (route name = id) rendering the matching named component with `onBack={navigation.goBack}`; every screen `style={{ flex: 1 }}`. The React Native status bar style follows the settings' appearance (system → `useColorScheme()`). Remove the temporary Task 3/4 switcher.
- [ ] **Step 2: Run** — `yarn lint && yarn typecheck && yarn test` → PASS; `:app:assembleDebug` and the iOS simulator build → succeed.
- [ ] **Step 3: Device checks, phone and simulator, each screen against the native app on the same device** (screenshots `rn-*.png` next to `native-*.png` in the report folder): all 21 screens open from Home and match; the back button and Android's back key return to Home; Android back with a sheet, menu or dialog open closes it first; iOS swipe-back from the left edge pops; Home keeps scroll position and settings after back; appearance light/dark from Home applies to Home and to screens opened after; tint and (Android) blur change Home at once; bars sit below the status bar and above the navigation bar / home indicator; 0 `APP_SCOUT_` lines on Android. Fix failures in the library (host files), not by changing copied native code.
- [ ] **Step 4: Commit** — `git add -A example android ios src && git commit -m "feat(example): every native screen through react navigation"`

### Task 6: Direction on Android, like iOS

Spec section "Additions → Direction". Edits to copied code are allowed for this; list each in the report.

**Files:**
- Modify: `android/.../catalog/HomeScreen.kt` (the "Layout direction" picker, as the iOS Home's: System / LTR / RTL, same footer text, placed as on iOS), `android/.../LiquidGlassScreenView.kt` (Compose layout direction from the prop), copied engine files that use left/right where iOS follows the reading direction, `example/src/App.tsx` (screen animation per direction)
- Test: `android/src/test/java/com/mohamedabdelnaby/liquidglass/LayoutDirectionTest.kt`

**Interfaces:**
- Produces: `fun layoutDirectionOf(value: String, systemIsRtl: Boolean): LayoutDirection` in `LiquidGlassScreenView.kt`; Android Home's picker sends `onSettingsChange` with `layoutDirection`.

- [ ] **Step 1: Failing test** — `layoutDirectionOf("rtl", false) == Rtl`, `("ltr", true) == Ltr`, `("system", true) == Rtl`, `("system", false) == Ltr`, `("bogus", false) == Ltr`. Run the Gradle unit tests → FAIL.
- [ ] **Step 2: Implement** the function, the host wiring (`system` = the device locale's direction) and the Home picker → PASS.
- [ ] **Step 3: Controls in RTL.** On the phone in RTL, compare with the iOS simulator in RTL (iOS Home → RTL): Glass View, Controls, Tab Bar, Sheets & Menus, Toolbar, Search, Recipes. Slider fill and thumb, toggle thumb and drop, segmented pill and drop, tab bar drop, menus and popovers growing from their button, sheets, the back chevron, press effects. Fix each mismatch in the copied engine code (start/end instead of left/right).
- [ ] **Step 4: Transitions.** `example/src/App.tsx`: native-stack `animation` is `'ios_from_right'` on Android (`'ios_from_left'` when the settings' direction is RTL) and the default on iOS (`'slide_from_left'` in RTL). Check push and back on both platforms in LTR and RTL.
- [ ] **Step 5: Run** all tests and both builds → PASS. **Commit** — `git add -A android example/src && git commit -m "feat(android): layout direction like ios"`

### Task 7: Custom font

Spec section "Additions → Fonts". Edits to copied code are allowed for this; list each in the report.

**Files:**
- Modify: `src/types.ts`, `src/settings.tsx`, `src/screens.tsx`, `src/LiquidGlassScreenNativeComponent.ts` (props `fontRegular`, `fontMedium`, `fontSemibold`, `fontBold`, `fontHeavy: string`, `''` = not set; a string `fontFamily` fills all five), `src/__tests__/screens.test.tsx`; Android host + a `fontFamilyOf(...)` helper; iOS host + `ios/Host/GlassFont.swift` and the copied SwiftUI files' `.font(...)` calls; `example/` (a sample font, `CUSTOMIZE` flag)
- Test: Jest; `android/src/test/.../FontFamilyTest.kt`; `ios/Tests/GlassFontTests.swift`

**Interfaces:**
- Produces (JS): `GlassSettings.fontFamily?: string | { regular?: string; medium?: string; semibold?: string; bold?: string; heavy?: string }`; also a prop on every screen (wins over the provider).
- Produces (native): Android `fun fontFamilyOf(faces: FontFaces, load: (name: String, weight: FontWeight) -> Typeface?): FontFamily?` (null when no face is set); iOS `struct GlassFontSpec` in the environment and `View.glassFont(_ style: Font.TextStyle)` / `glassFont(size:weight:)` that keep size, weight and Dynamic Type.

- [ ] **Step 1: Failing tests** — Jest: `fontFamily: 'Cairo'` → all five props `'Cairo'`; `{ regular: 'Cairo-Regular', bold: 'Cairo-Bold' }` → those two set, others `''`; a screen's own `fontFamily` prop wins; `onSettingsChange` from Home keeps the current `fontFamily` and `icons` (the event does not carry them). Kotlin: weight → face, missing weight falls back to regular, none set → null. XCTest: the face for each weight, fallback to regular, nil → system font.
- [ ] **Step 2: Implement.** Android: React Native's font manager (`ReactFontManager.getTypeface`) loads faces; the host provides `MaterialTheme(typography = …with fontFamily)` and `LocalTextStyle` around the copied screens; copied texts that build a `TextStyle` without the theme get the family too. iOS: each copied `.font(.style)` / `.font(.system(size:weight:))` becomes `.glassFont(…)`; navigation and large titles, tab bar items, segmented controls, bar buttons and search fields get the font through appearance proxies scoped to the library's hosting controller class. → PASS.
- [ ] **Step 3: Example and device check.** Add an OFL font to the example (e.g. the `@expo-google-fonts/cairo` package's TTFs; register them the React Native way) and `example/src/customize.ts` with `CUSTOMIZE = false` (true passes the font and Task 8's icons to the provider). With it true, on the phone and the simulator every screen's text uses the font, sizes and weights as before; with it false, screens look native. List the texts that stay in the system font.
- [ ] **Step 4: Run** all tests and both builds → PASS. **Commit** — `git add -A src android ios example && git commit -m "feat: custom font for every screen"`

### Task 8: Custom icons

Spec section "Additions → Icons". Edits to copied code are allowed for this; list each in the report.

**Files:**
- Modify: `src/types.ts`, `src/settings.tsx`, `src/screens.tsx`, `src/index.tsx` (`vectorIcon`), `src/LiquidGlassScreenNativeComponent.ts` (prop `icons: string`, JSON of name → `{glyph, fontFamily}` | `{svg}` | `{uri}`; image sources resolved with `Image.resolveAssetSource`), tests; Android host + `GlassIcons.kt` and the copied Compose icon call sites; `android/build.gradle` (`com.caverock:androidsvg-aar`); iOS host + `ios/Host/GlassIcon.swift` and the copied SwiftUI icon call sites; podspec (`SwiftDraw`); `example/` (`react-native-vector-icons` and an SVG and PNG sample in `customize.ts`)
- Test: Jest; `android/src/test/.../GlassIconsTest.kt`; `ios/Tests/GlassIconTests.swift`

**Interfaces:**
- Produces (JS): `GlassSettings.icons?: Record<string, GlassIcon>`, `type GlassIcon` and `vectorIcon(iconSet, name)` as in the spec; `icons` also a prop on every screen (merged over the provider's, key by key).
- Produces (native): Android `@Composable fun glassIcon(sfName: String, fallback: ImageVector): Painter`; iOS `struct GlassImage: View` (`GlassImage("heart")`) and `func glassUIImage(_ sfName: String) -> UIImage?` for UIKit-backed places (tab items, menus).
- Call sites: every Android Material icon in the copied screens names the SF Symbol the iOS catalog uses in the same place (`glassIcon("heart.fill", Icons.Filled.Favorite)`); no separate table.

- [ ] **Step 1: Failing tests** — Jest: `vectorIcon({ getRawGlyphMap: () => ({ heart: 0xf443 }), getFontFamily: () => 'Ionicons' }, 'heart')` → `{ glyph: '', fontFamily: 'Ionicons' }`; an unknown name → dev warning, `undefined`; `icons` JSON has `uri` for `require`d images and passes `svg`/`glyph` through; a screen's own icons merge over the provider's. Kotlin and XCTest: JSON → icon kinds; a glyph icon renders at the requested size; an unknown name → fallback.
- [ ] **Step 2: Implement** native drawing (glyph from the registered font; SVG via AndroidSVG / SwiftDraw; images loaded from the uri), template tinting, the original icon's size; replace the copied call sites → PASS.
- [ ] **Step 3: Device check** with `CUSTOMIZE = true`: a vector icon, an SVG and a PNG replace their icons on Home, Buttons, Toolbar, Tab Bar and in a menu, on the phone and the simulator; icons not in the map stay native.
- [ ] **Step 4: Run** all tests and both builds → PASS. **Commit** — `git add -A src android ios example ReactNativeLiquidGlass.podspec && git commit -m "feat: custom icons for every screen"`

### Task 9: Final docs pass and CI (README and guides are written in Task 14)

**Files:**
- Create: `README.md` (replace the template's), `.github/workflows/ci.yml` (or extend the template's; remove the Expo `prebuild` and `build:web` steps)
- Modify: `package.json` (`files` includes `ios`, `android` without build outputs, `src`, `lib`, the podspec; `keywords`)

- [ ] **Step 1: README** — install (bare React Native CLI; `pod install` with `LANG`/`LC_ALL`), floors (iOS 27; Android 26 with compileSdk 37 and AGP 9.1, from the copied Compose; RN 0.88 verified, New Architecture), the 22 components and `GLASS_SCREENS`, `GlassSettingsProvider` and which settings each platform reads (the spec's table), events, the React Navigation recipe from Task 5 (header hidden, `onOpen` → `navigate`, `onBack` → `goBack`, the per-direction animations), layout direction, fonts (adding fonts, the weight map, the texts iOS keeps in the system font), icons (`vectorIcon`, SVG, images, SF Symbol keys), "memoise the provider `value`", Metro on 8081, Expo not yet supported, licenses of the backdrop code (`LICENSE-*` files kept).
- [ ] **Step 2: CI** — lint, typecheck, Jest, and the Android unit tests (`./gradlew :native-dev-kit_react-native-liquid-glass:testDebugUnitTest` from `example/android`).
- [ ] **Step 3: Run** — `yarn lint && yarn typecheck && yarn test` → PASS; `npm pack --dry-run` lists no build folders.
- [ ] **Step 4: Commit** — `git add README.md .github package.json && git commit -m "docs: readme and ci for the native screens"`

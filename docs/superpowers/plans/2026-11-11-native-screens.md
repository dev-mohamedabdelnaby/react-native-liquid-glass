# Native Screens Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** The two native catalog apps' own code becomes the library; React Native shows each native screen and passes it props, in a React Native CLI example that matches the native apps.

**Architecture:** One Fabric component, `LiquidGlassScreen`, with a `screen` prop. iOS hosts the copied SwiftUI screen in a `UIHostingController`; Android hosts the copied Compose screen in a `ComposeView`. JS exports one named component per screen and a settings provider; the example pushes them with React Navigation native-stack.

**Tech Stack:** React Native 0.88.1 (New Architecture, codegen), TypeScript, Jest; Swift / SwiftUI (iOS 27); Kotlin / Jetpack Compose (Compose BOM 2026.02.00, API 26+); React Navigation native-stack (example only).

**Spec:** `docs/superpowers/specs/2026-11-11-native-screens-design.md`

## Global Constraints

- Library root: `/Users/mohamedabdelnaby/Developer/libs/liquid-glass`, branch `feat/native-screens`. Never push.
- iOS source: `/Users/mohamedabdelnaby/Developer/ios/liquidGlass/liquidGlass`. Android source: `/Users/mohamedabdelnaby/Developer/android/app/src/main/java/com/example/myapplication` (+ `app/src/main/res/drawable-nodpi`, `app/src/test`). Read only: never edit or commit in those two projects.
- Native code is copied unchanged. Allowed edits: Android package names (`com.example.myapplication` → `com.mohamedabdelnaby.liquidglass.catalog`, `R` import → `com.mohamedabdelnaby.liquidglass.R`); the live-tint fix from commit 53f95ae in `DrawBackdropModifier.kt`; on iOS only the `ContentView` struct's settings source and row action (Task 4). Any other edit needs a line in the task report saying why.
- Minimum OS: iOS 27.0; Android API 26. React Native 0.80+, New Architecture only (Fabric + codegen; no old-architecture view managers or bridge modules).
- The library has no dependency on React Navigation or react-native-screens; only the example has.
- Yarn only. Example: React Native CLI (no Expo), Metro on port 8084 only. Never use ports 8081, 8082, 8083; never touch `~/node_modules`.
- Commit subjects lower-case (commitlint); never `--no-verify`.
- iOS: `pod install` and `xcodebuild` need `LANG=en_US.UTF-8 LC_ALL=en_US.UTF-8`. Simulator: iPhone 18 Pro, UDID `459FB3AD-93EC-4C18-ADE6-2A0FC3C24DED`.
- Android device: phone `5ef3365c` only (adb at `~/Library/Android/sdk/platform-tools/adb`), no emulator. Allowed: install, launch, logcat, screencap, input. Before every tap or swipe, check `dumpsys activity activities | grep -m1 topResumedActivity` shows the app you mean. Never change settings, never unlock the phone, never bypass a MIUI install prompt (stop and report). `adb reverse tcp:8084 tcp:8084` drops when the phone is replugged; re-apply it.
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
- Create: `example/` as a React Native CLI app (package `@mohamed_abd_elnaby/react-native-liquid-glass-example`, the existing workspace name)

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

- [ ] **Step 2: Run, see it fail** — `cd example/android && ./gradlew :mohamed_abd_elnaby_react-native-liquid-glass:testDebugUnitTest` → FAIL (does not compile).
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

### Task 5: Every native screen through React Navigation

**Files:**
- Modify: `example/src/App.tsx`; `example/package.json` (only if a dependency is missing)

**Interfaces:**
- Consumes: Task 2's exports.

- [ ] **Step 1: Wire the example.** `NavigationContainer` > native-stack with `headerShown: false`; `useState<GlassSettings>(DEFAULT_SETTINGS)` in `App` feeding one `GlassSettingsProvider` above the `NavigationContainer`; route `Home` renders `GlassHomeScreen` with `onOpen={(id) => navigation.navigate(id)}` and `onSettingsChange={setSettings}`; one route per entry of `GLASS_SCREENS` (route name = id) rendering the matching named component with `onBack={navigation.goBack}`; every screen `style={{ flex: 1 }}`. The React Native status bar style follows the settings' appearance (system → `useColorScheme()`). Remove the temporary Task 3/4 switcher.
- [ ] **Step 2: Run** — `yarn lint && yarn typecheck && yarn test` → PASS; `:app:assembleDebug` and the iOS simulator build → succeed.
- [ ] **Step 3: Device checks, phone and simulator, each screen against the native app on the same device** (screenshots `rn-*.png` next to `native-*.png` in the report folder): all 21 screens open from Home and match; the back button and Android's back key return to Home; Android back with a sheet, menu or dialog open closes it first; iOS swipe-back from the left edge pops; Home keeps scroll position and settings after back; appearance light/dark from Home applies to Home and to screens opened after; tint and (Android) blur change Home at once; bars sit below the status bar and above the navigation bar / home indicator; 0 `APP_SCOUT_` lines on Android. Fix failures in the library (host files), not by changing copied native code.
- [ ] **Step 4: Commit** — `git add -A example android ios src && git commit -m "feat(example): every native screen through react navigation"`

### Task 6: README and CI

**Files:**
- Create: `README.md` (replace the template's), `.github/workflows/ci.yml` (or extend the template's)
- Modify: `package.json` (`files` includes `ios`, `android` without build outputs, `src`, `lib`, the podspec; `keywords`)

- [ ] **Step 1: README** — install (bare React Native CLI; `pod install` with `LANG`/`LC_ALL`), floors (iOS 27, Android 26, RN 0.80+ New Architecture), the 22 components and `GLASS_SCREENS`, `GlassSettingsProvider` and which settings each platform reads (the spec's table), events, the React Navigation recipe from Task 5 (header hidden, `onOpen` → `navigate`, `onBack` → `goBack`), "memoise the provider `value`", Expo not yet supported, licenses of the backdrop code (`LICENSE-*` files kept).
- [ ] **Step 2: CI** — lint, typecheck, Jest, and the Android unit tests (`./gradlew :mohamed_abd_elnaby_react-native-liquid-glass:testDebugUnitTest` from `example/android`).
- [ ] **Step 3: Run** — `yarn lint && yarn typecheck && yarn test` → PASS; `npm pack --dry-run` lists no build folders.
- [ ] **Step 4: Commit** — `git add README.md .github package.json && git commit -m "docs: readme and ci for the native screens"`

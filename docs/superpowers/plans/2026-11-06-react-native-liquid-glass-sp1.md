# react-native-liquid-glass — Sub-project 1 (foundation and glass views) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship the library's foundation — package, settings, `GlassView` and `GlassButton` — on iOS (UIKit glass) and Android (the Compose glass engine), with an example app.

**Architecture:** JS components merge their own `glass` prop over a `GlassSettingsProvider` over defaults and pass one flat prop set to a Fabric view. iOS wraps `UIVisualEffectView(UIGlassEffect)` with React children in its `contentView`. Android hosts a `ComposeView` drawing the engine's `glassEffect` behind ordinary React child views, refracting a backdrop recorded from the view tree.

**Tech Stack:** TypeScript, React Native 0.80+ New Architecture (Fabric codegen), Swift + Objective-C++ (UIKit, iOS 26), Kotlin + Jetpack Compose, Jest, JUnit, XCTest, create-react-native-library, Yarn workspaces, Expo example app.

**Spec:** `/Users/mohamedabdelnaby/Developer/android/docs/superpowers/specs/2026-11-06-react-native-liquid-glass-design.md` (moved into the library's `docs/` in Task 1).

## Global Constraints

- Package: `@mohamed_abd_elnaby/react-native-liquid-glass`, at `/Users/mohamedabdelnaby/Developer/libs/liquid-glass`.
- Minimum OS: iOS 26 (real glass only, no fallback look); Android 8 (API 26).
- Android tiers: lens on API 33+, blur on 31–32, frost on 26–30.
- React Native 0.80+, New Architecture only, Fabric native views.
- Bare React Native and Expo through a development build; Expo Go is not supported.
- Settings defaults: clear, blur 3, white 25%, black 5%, edge light 15%, refraction 14 / 24, no tint, not opaque, liquid on, every control liquid, tab bar drop on.
- Precedence: an item's own value > `GlassSettingsProvider` > defaults.
- Android only (ignored on iOS, documented): `blur`, `white`, `black`, `edgeLight`, `refraction`, `liquid`, `dropColor`, per-control liquid switches. `tint` and `opaque` apply on both.
- Invalid prop values fall back to defaults with a dev-only warning; never throw.
- The Android engine has no React Native imports.
- Yarn only (the template is a Yarn workspace).

## Review Focus

- React children inside a `GlassView` on Android must stay tappable and draw above the glass — a Pressable inside glass fires `onPress` (Task 5 check).
- A `GlassView` over scrolling content must refract what is behind it *now*, not a stale frame (Task 5 and Task 8 checks).
- Glass inside glass (a `GlassButton` in a `GlassView`) must not sample itself or crash on Android (Task 5 check).
- Changing a `GlassSettingsProvider` value re-renders the glass under it live (Task 2 test).
- A tint string that isn't a color (`'notacolor'`) means "no tint", with one dev warning (Task 2 test).

---

### Task 1: Scaffold the package

**Files:**
- Create: `/Users/mohamedabdelnaby/Developer/libs/liquid-glass/` (whole template)
- Modify: `ReactNativeLiquidGlass.podspec` (platform), `android/build.gradle` (minSdk, Compose), `package.json` (name, description, keywords)
- Move: the spec and this plan into `docs/superpowers/specs/` and `docs/superpowers/plans/`

**Interfaces:**
- Produces: a Yarn workspace with `src/`, `ios/`, `android/`, `example/` (Expo); codegen config name `RNLiquidGlassViewSpec`; Android namespace `com.mohamedabdelnaby.liquidglass`.

- [ ] **Step 1: Scaffold**

```bash
cd /Users/mohamedabdelnaby/Developer/libs
npx create-react-native-library@latest liquid-glass --no-interactive --yes \
  --slug @mohamed_abd_elnaby/react-native-liquid-glass \
  --description "SwiftUI-style Liquid Glass for React Native: real glass on iOS, the Compose glass engine on Android" \
  --type fabric-view --languages kotlin-objc --example expo
```

- [ ] **Step 2: Set the platform floors**

Podspec `s.platforms = { :ios => "26.0" }` and `s.swift_version = "6.0"` (Swift sources are added in Task 6). Android `minSdkVersion 26`. Add the Compose setup copied from `/Users/mohamedabdelnaby/Developer/libs/liquid/android/build.gradle` (Kotlin Compose plugin, Compose BOM, `ui`, `foundation`, `material3`, `material-icons-extended`, lifecycle runtime) — same versions as that file.

- [ ] **Step 3: Install and build the untouched template on both platforms**

Run: `yarn && yarn example expo prebuild && (cd example && npx pod-install) && yarn example android && yarn example ios`
Expected: the template's sample view shows on the phone (`ANDROID_SERIAL=5ef3365c`) and the iPhone simulator.

- [ ] **Step 4: Move the spec and plan in, then commit**

```bash
git add -A && git commit -m "chore: scaffold react-native-liquid-glass (fabric view, iOS 26, Android 26)"
```

---

### Task 2: Settings, glass resolution and shapes (JS)

**Files:**
- Create: `src/types.ts`, `src/settings.tsx`, `src/resolveGlass.ts`
- Test: `src/__tests__/resolveGlass.test.ts`, `src/__tests__/settings.test.tsx`

**Interfaces:**
- Produces:
  - `type GlassStyle = 'regular' | 'clear'`
  - `type Glass = { style?: GlassStyle; tint?: ColorValue; interactive?: boolean; opaque?: boolean; liquid?: boolean; blur?: number; white?: number; black?: number; edgeLight?: number; refraction?: { height: number; amount: number } }`
  - `type GlassShape = 'capsule' | 'circle' | { cornerRadius: number }`
  - `type GlassSettings = { tint?: ColorValue; clear: boolean; blur: number; white: number; black: number; edgeLight: number; refraction: { height: number; amount: number }; opaque: boolean; liquid: boolean; dropColor?: ColorValue; sliderLiquid: boolean; switchLiquid: boolean; segmentedLiquid: boolean; tabBarLiquid: boolean; tabBarDrop: boolean }`
  - `const DEFAULT_SETTINGS: GlassSettings`
  - `function GlassSettingsProvider(props: { value: Partial<GlassSettings>; children: ReactNode }): JSX.Element` (nested providers merge over their parent)
  - `function useGlassSettings(): GlassSettings`
  - `type NativeGlassProps = { glassStyle: GlassStyle; tintColor: ProcessedColorValue | null; interactive: boolean; opaque: boolean; liquid: boolean; blur: number; white: number; black: number; edgeLight: number; refractionHeight: number; refractionAmount: number; shapeKind: 'capsule' | 'circle' | 'rect'; cornerRadius: number }`
  - `function resolveGlass(own: Glass | undefined, settings: GlassSettings, shape: GlassShape | undefined): NativeGlassProps`

- [ ] **Step 1: Write the failing tests**

```ts
test('defaults', () => {
  expect(resolveGlass(undefined, DEFAULT_SETTINGS, undefined)).toEqual({
    glassStyle: 'clear', tintColor: null, interactive: false, opaque: false, liquid: true,
    blur: 3, white: 0.25, black: 0.05, edgeLight: 0.15,
    refractionHeight: 14, refractionAmount: 24, shapeKind: 'capsule', cornerRadius: 0,
  });
});
test('own value beats provider beats default', () => {
  const s = { ...DEFAULT_SETTINGS, blur: 8, white: 0.4 };
  const r = resolveGlass({ blur: 1 }, s, undefined);
  expect(r.blur).toBe(1); expect(r.white).toBe(0.4); expect(r.black).toBe(0.05);
});
test('a tinted glass stays regular when the setting says clear', () => {
  expect(resolveGlass({ tint: 'red' }, DEFAULT_SETTINGS, undefined).glassStyle).toBe('regular');
  expect(resolveGlass({ style: 'clear', tint: 'red' }, DEFAULT_SETTINGS, undefined).glassStyle).toBe('clear');
});
test('the general tint applies only to glass without its own', () => {
  const s = { ...DEFAULT_SETTINGS, tint: 'blue' };
  expect(resolveGlass(undefined, s, undefined).tintColor).toBe(processColor('blue'));
  expect(resolveGlass({ tint: 'red' }, s, undefined).tintColor).toBe(processColor('red'));
});
test('liquid is off when either the item or the setting turns it off', () => {
  expect(resolveGlass({ liquid: false }, DEFAULT_SETTINGS, undefined).liquid).toBe(false);
  expect(resolveGlass(undefined, { ...DEFAULT_SETTINGS, liquid: false }, undefined).liquid).toBe(false);
});
test('shapes', () => {
  expect(resolveGlass(undefined, DEFAULT_SETTINGS, 'circle').shapeKind).toBe('circle');
  expect(resolveGlass(undefined, DEFAULT_SETTINGS, { cornerRadius: 12 })).toMatchObject({ shapeKind: 'rect', cornerRadius: 12 });
});
test('invalid values fall back with one dev warning', () => {
  const warn = jest.spyOn(console, 'warn').mockImplementation(() => {});
  expect(resolveGlass({ tint: 'notacolor' }, DEFAULT_SETTINGS, undefined).tintColor).toBeNull();
  expect(resolveGlass(undefined, DEFAULT_SETTINGS, { cornerRadius: -4 }).cornerRadius).toBe(0);
  expect(resolveGlass(undefined, DEFAULT_SETTINGS, 'oval' as any).shapeKind).toBe('capsule');
  expect(resolveGlass({ white: 3 }, DEFAULT_SETTINGS, undefined).white).toBe(1);
  expect(warn).toHaveBeenCalledTimes(4);
});
```

```tsx
// settings.test.tsx (uses @testing-library/react-native, added in Step 3)
const Probe = () => <Text testID="s">{JSON.stringify(useGlassSettings())}</Text>;
const tree = (blur: number) => (
  <GlassSettingsProvider value={{ blur }}>
    <GlassSettingsProvider value={{ white: 0.5 }}><Probe /></GlassSettingsProvider>
  </GlassSettingsProvider>
);
test('nested providers merge over the parent and re-render live', () => {
  const r = render(tree(8));
  const read = () => JSON.parse(r.getByTestId('s').props.children);
  expect(read()).toMatchObject({ blur: 8, white: 0.5, black: 0.05 });
  r.rerender(tree(2));
  expect(read().blur).toBe(2);
});
```

- [ ] **Step 2: Run them to see them fail**

Run: `yarn test src/__tests__`
Expected: FAIL (modules not found).

- [ ] **Step 3: Implement `types.ts`, `settings.tsx`, `resolveGlass.ts`**

First `yarn add -D @testing-library/react-native`. Ratios (`white`, `black`, `edgeLight`) clamp to 0–1; lengths (`blur`, refraction, `cornerRadius`) clamp to ≥ 0; every clamp or rejected value calls a `warnOnce(key, message)` that is silent unless `__DEV__`. Style rule: `own.style ?? (settings.clear && own.tint == null ? 'clear' : 'regular')`.

- [ ] **Step 4: Run them to see them pass**

Run: `yarn test src/__tests__ && yarn typecheck`
Expected: PASS, no type errors.

- [ ] **Step 5: Commit**

```bash
git add src && git commit -m "feat: glass settings, resolution and shapes"
```

---

### Task 3: Support check and icon map (JS)

**Files:**
- Create: `src/support.ts`, `src/icons.ts`
- Test: `src/__tests__/support.test.ts`, `src/__tests__/icons.test.ts`

**Interfaces:**
- Produces:
  - `function isGlassSupported(): boolean` — iOS: `parseInt(Platform.Version, 10) >= 26`; Android: always true.
  - `const ANDROID_ICONS: Record<string, string>` — SF Symbol name → engine icon key.
  - `function androidIconName(systemImage: string | undefined, override?: string): string | null`

- [ ] **Step 1: Write the failing tests**

```ts
const withPlatform = (OS: string, Version: string | number) => {
  jest.resetModules();
  jest.doMock('react-native', () => ({ Platform: { OS, Version } }));
  return require('../support').isGlassSupported as () => boolean;
};
test('iOS 26 and later are supported, earlier are not', () => {
  expect(withPlatform('ios', '26.0')()).toBe(true);
  expect(withPlatform('ios', '18.4')()).toBe(false);
  expect(withPlatform('android', 26)()).toBe(true);
});
test('the spec\'s nine symbols map', () => {
  expect(androidIconName('checkmark')).toBe('check');
  expect(androidIconName('xmark')).toBe('close');
  expect(androidIconName('plus')).toBe('add');
  expect(androidIconName('minus')).toBe('remove');
  expect(androidIconName('magnifyingglass')).toBe('search');
  expect(androidIconName('heart')).toBe('favorite_border');
  expect(androidIconName('square.and.arrow.up')).toBe('share');
  expect(androidIconName('ellipsis')).toBe('more_horiz');
  expect(androidIconName('trash')).toBe('delete');
});
test('override wins, unknown is null', () => {
  expect(androidIconName('checkmark', 'done')).toBe('done');
  expect(androidIconName('sparkles')).toBeNull();
});
```

- [ ] **Step 2: Run, see them fail** — `yarn test src/__tests__/support.test.ts src/__tests__/icons.test.ts` → FAIL.
- [ ] **Step 3: Implement `support.ts` and `icons.ts`.**
- [ ] **Step 4: Run, see them pass** — same command → PASS.
- [ ] **Step 5: Commit** — `git add src && git commit -m "feat: iOS support check and Android icon map"`

---

### Task 4: Android glass engine

**Files:**
- Create: `android/src/main/java/com/mohamedabdelnaby/liquidglass/engine/` — copies of `/Users/mohamedabdelnaby/Developer/android/app/src/main/java/com/example/myapplication/glass/` (as `engine/glass/`), `.../liquid/` (as `engine/liquid/`) and `.../ui/theme/SystemColors` (as `engine/SystemColors.kt`); `engine/Icons.kt`
- Test: `android/src/test/java/com/mohamedabdelnaby/liquidglass/engine/` — copies of the app's `GlassStyleTest`, `GlassSettingsTest`, `GlassContainerTest`; new `IconsTest.kt`
- Copy: `LICENSE-AndroidLiquidGlass`, `LICENSE-Liquid-Glass-Android` from the Android app to the package root, listed in `package.json` `files`

**Interfaces:**
- Produces (package `com.mohamedabdelnaby.liquidglass.engine.glass`): unchanged from the app — `data class Glass`, `data class GlassSettings`, `LocalGlassSettings`, `LocalGlassBackdrop`, `Modifier.glassEffect(glass, shape, transition, unionId, present)`, `Modifier.frostedGlass(glass, shape, shadow)`, `GlassButton`, `GlassProminentButton`, `GlassControlSize`, `CapsuleShape`, `GlassCircle`.
- Produces: `fun materialIcon(key: String): ImageVector?` in `engine/Icons.kt` covering every value of the JS `ANDROID_ICONS` map plus `done`.
- Excludes: the app's screen scaffolding (`GlassScreen`, `GlassSurfaceHost`, `GlassBackdrop` images) — screens are app code; keep only what components need. Anything that then fails to compile is either needed (copy it) or app-only (drop the reference).

- [ ] **Step 1: Copy the files, rename packages to `com.mohamedabdelnaby.liquidglass.engine.*`, and add `IconsTest`**

```kotlin
@Test fun everyMappedKeyResolves() {
  listOf("check","close","add","remove","search","favorite_border","share","more_horiz","delete","done")
    .forEach { assertNotNull(it, materialIcon(it)) }
}
@Test fun unknownKeyIsNull() { assertNull(materialIcon("nope")) }
```

- [ ] **Step 2: Run the engine tests**

Run: `cd example/android && ./gradlew :mohamed_abd_elnaby_react-native-liquid-glass:testDebugUnitTest`
Expected: `IconsTest` fails (`materialIcon` missing); the three copied test classes pass.

- [ ] **Step 3: Implement `materialIcon` with a `when` over the keys.**
- [ ] **Step 4: Run the same command** → all PASS. Also `grep -r "com.facebook.react" android/src/main/java/com/mohamedabdelnaby/liquidglass/engine` → no output.
- [ ] **Step 5: Commit** — `git add android LICENSE-* package.json && git commit -m "feat(android): bring in the glass engine"`

---

### Task 5: Android `GlassView`

**Files:**
- Create: `src/GlassViewNativeComponent.ts` (codegen spec, props = `NativeGlassProps` with `WithDefault` types, `tintColor: ColorValue`), `src/GlassView.tsx`
- Create: `android/.../liquidglass/host/ComposeHost.kt`, `host/ViewTreeBackdrop.kt`, `host/GlassLifecycleOwner.kt` (adapted from `/Users/mohamedabdelnaby/Developer/libs/liquid/android/src/main/java/com/mohamedabdelnaby/reactnativeliquid/LiquidComposeHost.kt`, `ViewTreeBackdrop.kt`, `GlassLifecycleOwner.kt` to the engine's `Backdrop`), `android/.../liquidglass/GlassViewProps.kt`, `GlassView.kt`, `GlassViewManager.kt`
- Modify: the template's package registration to export `GlassViewManager`; delete the template's sample view
- Test: `android/src/test/.../GlassViewPropsTest.kt`, `src/__tests__/GlassView.test.tsx`

**Interfaces:**
- Consumes: `resolveGlass`, `useGlassSettings`, `isGlassSupported` (Tasks 2–3); engine `Glass`, `GlassSettings`, `glassEffect`, `LocalGlassBackdrop`, `LocalGlassSettings` (Task 4).
- Produces:
  - JS: `function GlassView(props: ViewProps & { glass?: Glass; shape?: GlassShape }): JSX.Element`
  - Kotlin: `data class GlassViewProps(glassStyle: String = "regular", tintColor: Int? = null, interactive: Boolean = false, opaque: Boolean = false, liquid: Boolean = true, blur: Float = 3f, white: Float = 0.25f, black: Float = 0.05f, edgeLight: Float = 0.15f, refractionHeight: Float = 14f, refractionAmount: Float = 24f, shapeKind: String = "capsule", cornerRadius: Float = 0f)` with `fun toEngine(): Pair<Glass, GlassSettings>` and `fun shape(): RoundedCornerShape`.

- [ ] **Step 1: Write the failing tests**

```kotlin
@Test fun styleIsTakenAsGiven() {
  val (glass, settings) = GlassViewProps(glassStyle = "regular").toEngine()
  assertEquals(Glass.Variant.Regular, glass.styled(settings).variant)   // ownVariant = true
}
@Test fun valuesReachTheEngine() {
  val (glass, settings) = GlassViewProps(blur = 8f, white = 0.4f, opaque = true, liquid = false, tintColor = 0xFFFF0000.toInt()).toEngine()
  val s = glass.styled(settings)
  assertEquals(8.dp, s.blur); assertTrue(s.opaque); assertFalse(s.liquid); assertEquals(Color.Red, s.tint)
}
@Test fun shapes() {
  assertEquals(CapsuleShape, GlassViewProps(shapeKind = "capsule").shape())
  assertEquals(RoundedCornerShape(12.dp), GlassViewProps(shapeKind = "rect", cornerRadius = 12f).shape())
}
```

```tsx
jest.mock('../support', () => ({ isGlassSupported: jest.fn() }));
const supported = require('../support').isGlassSupported as jest.Mock;
test('renders the native view where glass is supported', () => {
  supported.mockReturnValue(true);
  const r = render(<GlassView testID="g" glass={{ tint: 'red' }} />);
  expect(r.getByTestId('g').props.glassStyle).toBe('regular');
});
test('falls back to a plain View where glass is unsupported', () => {
  supported.mockReturnValue(false);
  const r = render(<GlassView testID="g" />);
  expect(r.getByTestId('g').props.glassStyle).toBeUndefined();
});
```

- [ ] **Step 2: Run, see them fail** — `yarn test src/__tests__/GlassView.test.tsx` and the Gradle test command from Task 4 → FAIL.
- [ ] **Step 3: Implement.** `GlassView` (JS) renders `GlassViewNativeComponent` with `resolveGlass(glass, useGlassSettings(), shape)` and its `children`; when unsupported it renders `View` and warns once. `GlassView` (Kotlin) extends `ComposeHost`, which keeps one leading `ComposeView` below the React children (child indices offset, as in react-native-liquid), drawing `Box(Modifier.fillMaxSize().glassEffect(glass, shape))` under `LocalGlassBackdrop provides viewTreeBackdrop` and `LocalGlassSettings provides settings`. On API < 29 the backdrop is `emptyBackdrop()` (frost tier).
- [ ] **Step 4: Run the tests** → PASS.
- [ ] **Step 5: Device checks (example app, phone `5ef3365c`)** — put a temporary screen in the example and confirm each:
  - a `Pressable` inside `<GlassView>` fires `onPress` and draws above the glass;
  - a `GlassView` over a scrolling `ScrollView` of colored rows refracts the rows under it while scrolling (no frozen frame);
  - a `GlassView` inside a `GlassView` renders both without crash or self-sampling;
  - a `GlassView` with zero width doesn't crash.
- [ ] **Step 6: Commit** — `git add src android && git commit -m "feat(android): GlassView"`

---

### Task 6: iOS `GlassView`

**Files:**
- Create: `ios/LiquidGlassEffectView.swift` (`@objc public class LiquidGlassEffectView: UIView`), `ios/GlassMapping.swift`, `ios/RNLiquidGlassView.mm` + `.h` (Fabric component view)
- Modify: `ReactNativeLiquidGlass.podspec` (`source_files` include `ios/**/*.{h,m,mm,swift}`; add `s.test_spec 'Tests'` with `ios/Tests/*.swift`); delete the template's sample view
- Test: `ios/Tests/GlassMappingTests.swift`

**Interfaces:**
- Consumes: the codegen props from Task 5 (`GlassViewNativeComponent.ts`).
- Produces:
  - `enum GlassMapping { static func effect(style: String, tint: UIColor?, interactive: Bool) -> UIGlassEffect; static func corners(kind: String, radius: CGFloat) -> UICornerConfiguration }`
  - `LiquidGlassEffectView` with `@objc` setters `glassStyle`, `tint`, `interactive`, `opaque`, `shapeKind`, `cornerRadius`, and `contentView: UIView` (where children mount).

- [ ] **Step 1: Write the failing tests**

```swift
func testEffect() {
  let e = GlassMapping.effect(style: "regular", tint: .red, interactive: true)
  XCTAssertTrue(e.isInteractive); XCTAssertEqual(e.tintColor, .red)
}
func testCapsuleAndRect() {
  let v = UIView(frame: CGRect(x: 0, y: 0, width: 200, height: 100))
  v.cornerConfiguration = GlassMapping.corners(kind: "capsule", radius: 0)
  XCTAssertEqual(v.effectiveRadius(corner: .topLeft), 50, accuracy: 0.5)
  v.cornerConfiguration = GlassMapping.corners(kind: "rect", radius: 12)
  XCTAssertEqual(v.effectiveRadius(corner: .topLeft), 12, accuracy: 0.5)
}
func testOpaqueFillsWithTint() {
  let g = LiquidGlassEffectView(); g.tint = .red; g.opaque = true
  // the fill subview at index 0 of contentView is visible with backgroundColor .red;
  // with opaque = false it is hidden.
}
```

- [ ] **Step 2: Run, see them fail**

Run: `xcodebuild test -workspace example/ios/*.xcworkspace -scheme ReactNativeLiquidGlass-Unit-Tests -destination 'id=459FB3AD-93EC-4C18-ADE6-2A0FC3C24DED'`
Expected: FAIL (types missing).

- [ ] **Step 3: Implement.** `LiquidGlassEffectView` holds a `UIVisualEffectView(effect: GlassMapping.effect(...))`, rebuilding the effect when style, tint or interactive change; the opaque fill is a subview of `contentView` at index 0 with `backgroundColor = tint ?? .systemBackground`, shown only when `opaque`. `RNLiquidGlassView.mm` updates props in `updateProps:oldProps:` and mounts children into `contentView` in `mountChildComponentView:index:` (index + 1, after the fill).
- [ ] **Step 4: Run the tests** → PASS.
- [ ] **Step 5: Simulator checks** — the same four checks as Task 5 Step 5, on the iPhone simulator.
- [ ] **Step 6: Commit** — `git add ios ReactNativeLiquidGlass.podspec && git commit -m "feat(ios): GlassView on UIGlassEffect"`

---

### Task 7: `GlassButton`

**Files:**
- Create: `src/GlassButtonNativeComponent.ts`, `src/GlassButton.tsx`, `ios/LiquidGlassButton.swift`, `ios/RNLiquidGlassButton.mm` + `.h`, `android/.../liquidglass/GlassButtonView.kt`, `GlassButtonManager.kt`
- Test: `src/__tests__/GlassButton.test.tsx`, `ios/Tests/GlassButtonTests.swift`

**Interfaces:**
- Consumes: `resolveGlass`, `useGlassSettings`, `isGlassSupported`, `androidIconName` (Tasks 2–3); engine `GlassButton`, `GlassProminentButton`, `GlassControlSize`, `materialIcon` (Task 4).
- Produces:
  - JS: `function GlassButton(props: { title?: string; icon?: string; androidIcon?: string; prominent?: boolean; tint?: ColorValue; size?: 'small' | 'regular' | 'large'; disabled?: boolean; onPress?: () => void; glass?: Glass; style?: StyleProp<ViewStyle> }): JSX.Element`
  - Native props: `title`, `systemImage`, `androidIcon` (already mapped via `androidIconName`), `prominent`, `size`, `disabled`, the `NativeGlassProps`, and `onPress: DirectEventHandler<{}>`. The JS wrapper maps the native `onPress` event to the `onPress` prop and drops it when `disabled`.
  - Swift: `@objc public class LiquidGlassButton: UIView` with `button: UIButton`, `@objc` setters `title`, `systemImage`, `prominent`, `size`, `disabled`, `tint`, and `func apply()` that rebuilds the configuration.

- [ ] **Step 1: Write the failing tests**

```tsx
test('passes the mapped Android icon and the resolved glass', () => {
  const r = render(<GlassButton testID="b" icon="checkmark" title="Done" />);
  expect(r.getByTestId('b').props).toMatchObject({
    systemImage: 'checkmark', androidIcon: 'check', glassStyle: 'clear', size: 'regular', prominent: false,
  });
});
test('onPress fires, disabled blocks it', () => {
  const onPress = jest.fn();
  const r = render(<GlassButton testID="b" title="Go" onPress={onPress} />);
  fireEvent(r.getByTestId('b'), 'press');
  expect(onPress).toHaveBeenCalledTimes(1);
  r.rerender(<GlassButton testID="b" title="Go" onPress={onPress} disabled />);
  fireEvent(r.getByTestId('b'), 'press');
  expect(onPress).toHaveBeenCalledTimes(1);
});
```

```swift
func testConfiguration() {
  let b = LiquidGlassButton(); b.prominent = true; b.title = "Done"; b.systemImage = "checkmark"; b.apply()
  XCTAssertEqual(b.button.configuration?.title, "Done")
  XCTAssertNotNil(b.button.configuration?.image)
  // prominent uses UIButton.Configuration.prominentGlass(), otherwise .glass()
}
```

- [ ] **Step 2: Run, see them fail** — `yarn test src/__tests__/GlassButton.test.tsx` and the `xcodebuild test` command from Task 6 → FAIL.
- [ ] **Step 3: Implement.** iOS: `UIButton` with `UIButton.Configuration.glass()` / `.prominentGlass()`, `image = UIImage(systemName:)`, `baseBackgroundColor = tint` for prominent, `controlSize` from `size`, `isEnabled = !disabled`, sending `onPress` on `.primaryActionTriggered`. Android: the engine's `GlassButton` / `GlassProminentButton` with `GlassControlSize` from `size`, `materialIcon(androidIcon)` and the title, in a `ComposeHost` with no React children, emitting `onPress` through the event dispatcher.
- [ ] **Step 4: Run the tests** → PASS.
- [ ] **Step 5: Commit** — `git add src ios android && git commit -m "feat: GlassButton"`

---

### Task 8: Example app — Glass Lab and Glass View screens

**Files:**
- Create: `example/src/screens/GlassLab.tsx`, `example/src/screens/GlassViewScreen.tsx`, `example/src/backdrop.tsx` (a colorful image or gradient backdrop, like the catalog's), `example/src/App.tsx` (two-screen switcher)

**Interfaces:**
- Consumes: `GlassView`, `GlassButton`, `GlassSettingsProvider`, `DEFAULT_SETTINGS`, `Glass`, `GlassShape` exported from `src/index.tsx`.

- [ ] **Step 1: Export the public API from `src/index.tsx`** — `GlassView`, `GlassButton`, `GlassSettingsProvider`, `useGlassSettings`, `DEFAULT_SETTINGS`, `isGlassSupported`, and the types `Glass`, `GlassShape`, `GlassSettings`, `GlassStyle`.
- [ ] **Step 2: Build Glass Lab** — every `GlassSettings` field from sub-project 1 editable live (clear, tint swatches, blur, white, black, edge light, refraction height and amount, opaque, liquid), wrapping a preview of `GlassView`s and `GlassButton`s over the backdrop.
- [ ] **Step 3: Build Glass View** — matching the catalog's Glass View screen: a scrolling backdrop, a `GlassView` with shape (capsule, circle, rect 24) and Regular/Clear choices, and a `GlassButton` row (plain, prominent, tinted, icon-only).
- [ ] **Step 4: Run on both platforms and compare**

Run: `yarn example android` (phone) and `yarn example ios` (simulator).
Expected: settings changes apply live; glass refracts the backdrop while it scrolls; screenshots match the Glass View screens of `/Users/mohamedabdelnaby/Developer/android` and `/Users/mohamedabdelnaby/Developer/ios/liquidGlass`.

- [ ] **Step 5: Commit** — `git add example src/index.tsx && git commit -m "feat(example): Glass Lab and Glass View screens"`

---

### Task 9: Docs and CI

**Files:**
- Modify: `README.md`, `.github/workflows/ci.yml`, `package.json` (`files`, `keywords`)

- [ ] **Step 1: README** — install (bare and Expo dev build, iOS 26 / Android 26 floors), the `GlassView` / `GlassButton` / `GlassSettingsProvider` API with the precedence rule, an "Android only" table listing exactly the Global Constraints' Android-only settings, the Android tiers, and license notes for the two copied licenses.
- [ ] **Step 2: CI** — keep the template's lint, typecheck and Jest jobs; add a job running `./gradlew :mohamed_abd_elnaby_react-native-liquid-glass:testDebugUnitTest` from `example/android`.
- [ ] **Step 3: Verify locally**

Run: `yarn lint && yarn typecheck && yarn test && (cd example/android && ./gradlew :mohamed_abd_elnaby_react-native-liquid-glass:testDebugUnitTest)`
Expected: all pass.

- [ ] **Step 4: Commit** — `git add -A && git commit -m "docs: README and CI for sub-project 1"`

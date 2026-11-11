# react-native-liquid-glass — the native apps as the library

Date: 2026-11-11. Status: awaiting review.
Replaces the approach of `2026-11-06-react-native-liquid-glass-design.md` (UIKit views written for the
library, example screens rebuilt in JavaScript). That work stays on branch `feat/sp1-foundation`.

## Goal

The library is the two native catalog apps' own code: the SwiftUI app
(`Developer/ios/liquidGlass`) on iOS and the Compose app (`Developer/android`) on Android. React
Native shows their screens and passes them props. Nothing is rewritten.

Success: the React Native example app, opened on the phone and the iPhone simulator, looks and
behaves exactly like the native app on the same platform, on every screen, and the Glass
settings chosen on its Home change every screen live.

## Decisions

| Topic | Decision |
|---|---|
| What React Native gets | One React Native component per native screen: Home plus the 21 catalog screens. |
| iOS code | The iOS app's `Screens/*.swift`, `Shared/*.swift` and the `GlassComponent`, `AppearanceMode`, `LayoutDirectionMode` types from `ContentView.swift`, copied unchanged into `ios/Catalog/`. |
| Android code | The Android app's `glass`, `liquid`, `screens`, `ui` packages, `HomeScreen.kt`, `GlassComponent.kt` and the four backdrop photos, copied with only the package name changed. The engine fix for live tint changes (commit 53f95ae) is kept. |
| Allowed edits to native code | Only what hosting needs: iOS Home rows call back instead of pushing, and iOS Home reads its settings from props instead of `@AppStorage`. Every edit is listed in the plan. |
| Hosting | iOS: a Fabric view holding a `UIHostingController`, added as a child of the React view controller. Android: a Fabric view holding a `ComposeView`. |
| Minimum OS | iOS 27.0 (the iOS app's deployment target; the copied code is unchanged). Android 8 (API 26), as the Android app. |
| React Native | 0.80+, New Architecture only (Fabric + codegen). React Native CLI first; Expo later. |
| Example app | A React Native CLI app (replaces the Expo example), Metro on port 8084. |
| Branch | `feat/native-screens`, cut from `feat/sp1-foundation` at 53f95ae. |

## JS API

```tsx
import {
  GlassSettingsProvider, GlassHomeScreen, GlassButtonsScreen, GlassControlsScreen, /* … */
} from '@mohamed_abd_elnaby/react-native-liquid-glass';

<GlassSettingsProvider value={settings}>
  <GlassHomeScreen onOpen={(screen) => navigation.navigate(screen)} onSettingsChange={setSettings} />
  <GlassButtonsScreen onBack={() => navigation.goBack()} style={{ flex: 1 }} />
</GlassSettingsProvider>
```

- One native component, `LiquidGlassScreen`, with a `screen` prop. The library exports one named
  component per screen (`GlassHomeScreen`, `GlassEffectScreen`, `GlassViewScreen`,
  `InteractiveGlassScreen`, `GlassButtonsScreen`, `GlassContainerScreen`, `GlassMorphingScreen`,
  `GlassUnionScreen`, `GlassToolbarScreen`, `GlassFloatingHeaderScreen`, `GlassTabBarScreen`,
  `GlassSheetsScreen`, `GlassControlsScreen`, `GlassTextScreen`, `GlassTransitionsScreen`,
  `GlassSearchScreen`, `GlassEditSearchScreen`, `GlassScrollEdgeScreen`, `GlassSplitViewScreen`,
  `GlassZoomTransitionsScreen`, `GlassAlertsScreen`, `GlassRecipesScreen`) and `GLASS_SCREENS`, the
  list of screen ids with their titles in home order.
- Screen ids are the iOS `GlassComponent` case names (`glassEffect` … `recipes`); Android maps
  them to its `GlassComponent` entries by capitalising the first letter (they match today).
- Events: `onBack()` (the screen's back or close button), `onOpen(screen)` (Home row tapped),
  `onSettingsChange(settings)` (Home changed a setting).
- `GlassSettingsProvider` passes the settings to every screen as props, so a change made on Home
  reaches screens that are already mounted.

## Settings

```ts
type GlassSettings = {
  appearance?: 'system' | 'light' | 'dark';           // both
  tint?: ColorValue | null;                           // both: the general glass tint
  layoutDirection?: 'system' | 'ltr' | 'rtl';         // iOS (iOS Home has this picker)
  // Android only, as the Android app's GlassSettings:
  clear?: boolean; blur?: number; white?: number; black?: number; edgeLight?: number;
  refraction?: { height: number; amount: number }; opaque?: boolean; liquid?: boolean;
  dropColor?: ColorValue | null; sliderLiquid?: boolean; switchLiquid?: boolean;
  segmentedLiquid?: boolean; tabBarLiquid?: boolean; tabBarDrop?: boolean;
};
```

- Defaults are the native apps' defaults. Each platform reads what its app has and ignores the rest.
- iOS: settings become the environment the iOS `ContentView` sets (`\.glassTint`,
  `preferredColorScheme`, `\.layoutDirection`). Android: they become `LocalGlassSettings` and the
  appearance mode given to `LiquidGlassTheme`, as the Android `MainActivity` does.
- Settings live in the React Native app's state (memory only, as in the Android app).

## Hosting details

- iOS: each hosted screen sits in its own `NavigationStack`, so its title, toolbar, search field,
  sheets and full-screen covers work as in the app. Screens other than Home get a leading back
  button (`chevron.backward`) that sends `onBack`. The hosting controller is a child of the React
  view controller, so `fullScreenCover` (Tab Bar, Sidebar & Inspector) can present.
- Android: the `ComposeView` provides `LocalOnBack` (sends `onBack`), `LocalGlassWindowBackdrop`,
  `LocalGlassSettings` and `LiquidGlassTheme(mode)`, exactly as the Android `MainActivity`.
  Home is the app's `HomeScreen` with `onOpen`, `onModeChange` and `onSettingsChange` sending events.
- The view keeps its native state across React Navigation detach and re-attach (going back to a
  screen below, switching tabs).

## React Navigation

- The library does not depend on React Navigation; it only sends `onBack` and `onOpen`.
- The example uses native-stack with the header hidden (each native screen draws its own bar):
  Home → `navigation.navigate(screen)`, `onBack` → `navigation.goBack()`. The iOS swipe-back
  gesture and the Android back button go through native-stack.
- Navigator components for apps' own screens (a glass tab bar for bottom-tabs, a glass header for
  native-stack, like react-native-liquid) are not in this version.

## Removed on this branch

`GlassView`, `GlassButton`, the UIKit views written for them, their Android Fabric wrappers, the
React-view backdrop recorder (`ViewTreeBackdrop`), the icon map, and the Expo example. They stay on
`feat/sp1-foundation`.

## Testing

- Jest: every exported screen sends its screen id; settings from the provider reach the native props;
  `onBack` / `onOpen` / `onSettingsChange` fire.
- Android JVM: the engine tests that came with the app keep passing; a test that every JS screen id
  maps to a `GlassComponent` entry.
- iOS: the library builds in the example; a check that every JS screen id maps to a
  `GlassComponent` case.
- Devices: the example on the phone (5ef3365c) and the iPhone simulator: every screen opens, back
  works, Home settings change open screens live, no hang lines on Android, and screenshots match the
  native app on the same device.
- CI: lint, typecheck, Jest, Android unit tests.

Done when every screen of both native apps opens from the React Native CLI example and matches the
native app on the same platform, settings apply live, and back navigation works.

## Out of scope for this version

Expo (development builds); building blocks (`GlassView`, buttons, controls) as separate React Native
components; React Navigation tab bar and header components; saving settings across restarts.

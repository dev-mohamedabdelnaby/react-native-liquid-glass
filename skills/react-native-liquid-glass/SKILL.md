---
name: react-native-liquid-glass
description: Use when adding Liquid Glass or glass screens to a React Native app, or when working with @native-dev-kit/react-native-liquid-glass (GlassHomeScreen, GlassButtonsScreen, GlassSettingsProvider, GLASS_SCREENS). Covers install, iOS 27 / Android compileSdk 37 setup, the React Navigation native-stack recipe and the rules that avoid blank screens and dead settings.
---

# React Native Liquid Glass screens

`@native-dev-kit/react-native-liquid-glass` ships 22 **whole native screens** (SwiftUI on iOS, Jetpack Compose on Android), not glass building blocks. There is no `GlassView` or `GlassButton`. Screens take no children.

The full reference is `CONTEXT.md` in the package (`node_modules/@native-dev-kit/react-native-liquid-glass/CONTEXT.md`). Read it before writing code.

## Check first

- Bare React Native CLI app, React Native 0.88+, New Architecture on. Expo is not supported: stop and tell the user.
- iOS 27.0+, Android minSdk 26 with compileSdk 37 and Android Gradle Plugin 9.1.

## Install

1. `npm install @native-dev-kit/react-native-liquid-glass` (or `yarn add`).
2. iOS: `platform :ios, '27.0'` in `ios/Podfile`; then `cd ios && LANG=en_US.UTF-8 LC_ALL=en_US.UTF-8 pod install`.
3. Android: in `android/build.gradle` `ext`, `compileSdkVersion = 37`, `minSdkVersion = 26` or higher, and `kotlinVersion` set.
4. For navigation: `@react-navigation/native`, `@react-navigation/native-stack`, `react-native-screens`, `react-native-safe-area-context`.

## Use

1. Keep settings in state: `const [settings, setSettings] = useState<GlassSettings>(DEFAULT_SETTINGS)`.
2. Wrap the navigator in `<GlassSettingsProvider value={settings}>`.
3. Native-stack with `screenOptions={{ headerShown: false, animation: 'ios_from_right' }}`.
4. Route `Home`: `<GlassHomeScreen style={{ flex: 1 }} onOpen={(id) => navigation.navigate(id)} onSettingsChange={setSettings} />`.
5. One route per `GLASS_SCREENS` entry, named by its `id`: `<Screen style={{ flex: 1 }} onBack={navigation.goBack} />`. Copy the `SCREENS` map and the full recipe from `CONTEXT.md` section 4.

## Rules

- Every screen needs `style={{ flex: 1 }}`; there is no default size.
- Feed `onSettingsChange` back into the provider; memoise the provider `value`.
- Do not add a header, a `BackHandler` or a `<StatusBar>` for glass screens.
- `appearance` on iOS changes the whole window.
- `androidStyle: 'material'` (provider or screen prop) draws the Android screens with Material 3; iOS ignores it.
- `androidControlShape: 'pill' | 'labeled' | 'glow'` (default `'pill'`; provider or screen prop) shapes the Android switch, slider, segmented control and bottom tab bar in both styles; `androidGlowIntensity` (0..1, default 0.35) sets how strongly `'glow'` glows. iOS ignores both. See `docs/control-shapes.md`.
- `fontFamily` (provider or screen prop): one family name, or `{ regular, medium, semibold, bold, heavy }` font names as in React Native's `fontFamily`. Fonts are added the React Native way (Android `assets/fonts`, iOS `UIAppFonts`). See `docs/fonts.md`.
- `icons` and `vectorIcon` are planned, not shipped. Do not use them.
- Right-to-left: `layoutDirection` in the provider; for the stack animations copy the recipe in `CONTEXT.md` section 4.

## Verify

Run the app; Home lists 21 screens; a row pushes its screen; the back button and (Android) the back key return; changing Appearance on Home changes the next screen opened. See `CONTEXT.md` section 10.

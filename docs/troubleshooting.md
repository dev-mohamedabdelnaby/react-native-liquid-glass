# Troubleshooting

## The screen is blank

The components have no default size. Give every screen a style that sizes it, usually `style={{ flex: 1 }}`, and make sure its parent has a size too.

## Home's controls do nothing, or other screens ignore them

Home sends every change through `onSettingsChange`; it does not keep it on its own. Store the settings and pass them back:

```tsx
import { useState } from 'react';
import {
  DEFAULT_SETTINGS,
  GlassHomeScreen,
  GlassSettingsProvider,
  type GlassSettings,
} from '@native-dev-kit/react-native-liquid-glass';

export function Root() {
  const [settings, setSettings] = useState<GlassSettings>(DEFAULT_SETTINGS);
  return (
    <GlassSettingsProvider value={settings}>
      <GlassHomeScreen style={{ flex: 1 }} onSettingsChange={setSettings} />
    </GlassSettingsProvider>
  );
}
```

## Every screen updates when an unrelated parent re-renders

`GlassSettingsProvider` re-merges its `value` whenever the object changes identity, and every screen then gets new native props. Pass state or a `useMemo` result, never an inline object such as `value={{ appearance: 'dark' }}` in a component that re-renders often.

## Two title bars at the top

Hide the navigator's header: `screenOptions={{ headerShown: false }}`. Each glass screen draws its own bar.

## iOS: the whole app turned dark (or light)

On iOS, `appearance: 'light'` or `'dark'` sets the window's style, so your React Native screens change too. This is how SwiftUI's dark mode works.

- The latest mounted glass screen that asks for light or dark wins.
- **A mounted screen keeps its request even when it is off screen** (under a pushed screen, in another tab).
- When the last such screen unmounts, the window gets back the style it had before.
- A call to `Appearance.setColorScheme` made while a glass screen holds a request is lost when the request ends.

To keep your own screens in one style, set `appearance` in the provider to match your app, or use `'system'`, which never writes the window.

## iOS: `<StatusBar>` does nothing

Under native-stack, react-native-screens owns the status bar on iOS, so React Native's `<StatusBar>` component has no effect. Use the screen option instead: `options={{ statusBarStyle: 'light' }}`. Glass screens set their own icons; you need this only for your own screens.

## Android, MIUI: white status bar icons on the Floating Header in Light

On Xiaomi phones (MIUI) in system dark mode, the status bar icons follow the colour of the content under them, not the app's request. The Floating Header screen in Light asks for dark icons, but its header is blurred and coloured at the top, so MIUI may show white icons. Other Android phones honour the request. This is the phone's behaviour; the library leaves it as is.

## Android: the back key leaves the screen instead of closing a sheet

The library sends the back key to the screen's open sheet, menu, search field or cover first. Check:

- **Your activity is React Native's `ReactActivity`** (or another `androidx.activity.ComponentActivity`). On Android 16 (API 36) and later, back events come through the activity's navigation event dispatcher, so the activity must be a `NavigationEventDispatcherOwner`. `ReactActivity` is.
- **No other library replaced the activity window's `Window.Callback` without passing events on.** Below API 36 the library reads the back key there. It installs itself again the next time a glass screen enters the window.
- **Only one glass screen is visible at a time.** With two glass screens on screen together (a transparent modal or a form sheet over another glass screen), the back key goes to the one that entered the window last, which may be the lower one.

## iOS: the system direction stays left-to-right on an Arabic iPhone

iOS reports right-to-left only for apps localized for a right-to-left language. Add the localization (for example an `ar.lproj` folder, or `ar` in `CFBundleLocalizations`), or set `layoutDirection: 'rtl'`. See [layout-direction.md](layout-direction.md).

## iOS: my own header's back button is an empty glass circle in RTL

On iOS 26 and later, a native-stack header in a direction chosen at run time loses its chevron. Set the window scene's layout-direction trait from a small native module: [layout-direction.md](layout-direction.md#ios-26-the-back-button-loses-its-chevron-in-run-time-rtl).

## A custom icon does not show

- The key must be the SF Symbol name the screen uses at that place (see [icons.md](icons.md#which-keys-exist)).
- `vectorIcon` returned `undefined` (a development warning names the missing glyph), or the icon font is not added to the app.
- The SVG is invalid or has neither a `viewBox` nor a size.
- An image failed to load (on Android a remote image waits at most 10 seconds). The native icon stays in all these cases.
- Some system-drawn icons cannot be replaced: steppers, the iOS search field, some menu marks.

## A custom font does not show

Check the name: on Android the file name in `assets/fonts` (or the registered family), on iOS the PostScript name, with the file listed in `UIAppFonts`. iOS alerts, menus, pickers, the share sheet and the keyboard always keep the system font.

## Debug build: red box from another app's code

Debug builds load JavaScript from Metro on port 8081. If another project's Metro runs on 8081, the app loads that project's bundle and fails (for example `Cannot read property 'EventEmitter' of undefined`). Stop the other Metro and start yours with `npx react-native start`, or build Release, which embeds the bundle:

```sh
npx react-native run-ios --mode Release
npx react-native run-android --mode release
```

## Android build fails

- **"compileSdk 37" or Compose version errors.** The Android screens need compileSdk 37 and Android Gradle Plugin 9.1. See [getting-started.md](getting-started.md#3-android).
- **`autolinking.cpp` lists components that no longer exist** after upgrading the library. The React Native Gradle plugin's autolinking cache is stale. Delete `android/build/generated/autolinking/` (or clean the build) and build again.
- **Old architecture.** The library is a Fabric component. Keep `newArchEnabled=true`.

## `pod install` fails with an encoding error

CocoaPods needs a UTF-8 locale: `LANG=en_US.UTF-8 LC_ALL=en_US.UTF-8 pod install`.

## iOS: the build fails on the deployment target

The pod needs iOS 27.0. Set `platform :ios, '27.0'` in the Podfile and the app target's deployment target to 27.0.

## Expo

Not supported yet. Use a bare React Native CLI app.

## Known differences from the native apps

- **iOS:** each screen has its own navigation bar, so on push the new bar slides in with its screen. In the native app, one shared bar cross-fades its items.
- **iOS:** the back chevron is a `chevron.backward` image, about 2 px (at 3x) taller than the system back chevron.
- **Android, Union:** after "Show Details" in the menu, the details card appears once the menu has closed, about 0.3 s later.
- **Both:** some bar titles are shorter than the Home row titles (Interactive, Container, Transitions, Notes, Scroll Edge, Split View; Zoom Transitions shows a status pill instead). They are the native apps' titles; see [screens.md](screens.md).
- **iOS:** after the layout direction is switched on Home, Home's list starts again at the top.
- **Android Material:** the bottom tab bar does not minimize on scroll.

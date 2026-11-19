# @native-dev-kit/react-native-liquid-glass

Liquid Glass screens for React Native. On iOS, they are a SwiftUI app's own screens, with Apple's real glass. On Android, they are a Jetpack Compose app's own screens, with a Compose glass engine.

This library gives you **whole native screens**, not building blocks. Each screen draws its own title bar, back button, glass, controls, sheets and menus. You put a screen in your navigator and handle two events: open and back.

- 22 screens: Home plus 21 catalog screens (buttons, controls, tab bars, search, sheets and more).
- One settings provider: appearance, glass tint and the Android glass options reach every screen, live.
- Works with React Navigation's native-stack. The library does not depend on it.

To see every screen, run the example app in [`example/`](example) (commands in [AGENTS.md](AGENTS.md)).

## Requirements

| | Minimum |
|---|---|
| React Native | 0.88 (verified). New Architecture only. |
| iOS | 27.0 |
| Android | minSdk 26 (Android 8), compileSdk 37, Android Gradle Plugin 9.1 |
| Tooling | React Native CLI (bare app). **Expo is not supported yet.** |

The Android floor comes from the Compose BOM `2026.09.00` that the Android screens are built with.

## Install

```sh
npm install @native-dev-kit/react-native-liquid-glass
```

iOS: set `platform :ios, '27.0'` in `ios/Podfile`, then run:

```sh
cd ios && LANG=en_US.UTF-8 LC_ALL=en_US.UTF-8 pod install
```

Android: set `compileSdkVersion = 37` and `minSdkVersion = 26` (or higher) in `android/build.gradle`.

All steps: [docs/getting-started.md](docs/getting-started.md).

## Quick start (React Navigation)

```tsx
import { useState } from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import {
  DEFAULT_SETTINGS, GlassButtonsScreen, GlassHomeScreen, GlassSettingsProvider, type GlassSettings,
} from '@native-dev-kit/react-native-liquid-glass';

const Stack = createNativeStackNavigator();

export default function App() {
  const [settings, setSettings] = useState<GlassSettings>(DEFAULT_SETTINGS);
  return (
    <GlassSettingsProvider value={settings}>
      <NavigationContainer>
        <Stack.Navigator screenOptions={{ headerShown: false, animation: 'ios_from_right' }}>
          <Stack.Screen name="Home">
            {({ navigation }) => (
              <GlassHomeScreen style={{ flex: 1 }} onOpen={(id) => navigation.navigate(id)} onSettingsChange={setSettings} />
            )}
          </Stack.Screen>
          <Stack.Screen name="buttons">
            {({ navigation }) => <GlassButtonsScreen style={{ flex: 1 }} onBack={navigation.goBack} />}
          </Stack.Screen>
        </Stack.Navigator>
      </NavigationContainer>
    </GlassSettingsProvider>
  );
}
```

Three rules: hide the navigator's header (each screen draws its own), give every screen `style={{ flex: 1 }}`, and feed `onSettingsChange` back into the provider. This sample only registers `buttons`; the recipe with all 22 screens is in [docs/react-navigation.md](docs/react-navigation.md).

## The screens

| Component | Screen id | Title |
|---|---|---|
| `GlassHomeScreen` | `home` | Liquid Glass: the list of screens and the settings |
| `GlassEffectScreen` | `glassEffect` | Glass Effect |
| `GlassViewScreen` | `glassView` | Glass View |
| `InteractiveGlassScreen` | `interactive` | Interactive Glass |
| `GlassButtonsScreen` | `buttons` | Buttons |
| `GlassContainerScreen` | `container` | Glass Container |
| `GlassMorphingScreen` | `morphing` | Morphing |
| `GlassUnionScreen` | `union` | Union |
| `GlassToolbarScreen` | `toolbar` | Toolbar |
| `GlassFloatingHeaderScreen` | `floatingHeader` | Floating Header |
| `GlassTabBarScreen` | `tabBar` | Tab Bar |
| `GlassSheetsScreen` | `sheets` | Sheets & Menus |
| `GlassControlsScreen` | `controls` | Controls |
| `GlassTextScreen` | `text` | Text |
| `GlassTransitionsScreen` | `transitions` | Glass Transitions |
| `GlassSearchScreen` | `search` | Search |
| `GlassEditSearchScreen` | `editSearch` | Edit & Search Buttons |
| `GlassScrollEdgeScreen` | `scrollEdge` | Scroll Edge & Bars |
| `GlassSplitViewScreen` | `splitView` | Sidebar & Inspector |
| `GlassZoomTransitionsScreen` | `zoomTransitions` | Zoom Transitions |
| `GlassAlertsScreen` | `alerts` | Alerts & Dialogs |
| `GlassRecipesScreen` | `recipes` | Recipes |

`GLASS_SCREENS` lists the 21 catalog screens (not Home) as `{ id, title }`, in Home's order. Details: [docs/screens.md](docs/screens.md).

## Guides

- [Getting started](docs/getting-started.md): install, platform setup, first screen.
- [Screens](docs/screens.md): every component, its props and events.
- [Settings](docs/settings.md): `GlassSettingsProvider`, every setting, and which platform reads it.
- [React Navigation](docs/react-navigation.md): the full native-stack recipe.
- [Layout direction](docs/layout-direction.md): right-to-left on both platforms.
- [Android style](docs/android-style.md): Material 3 instead of glass on Android. Coming in the next version.
- [Fonts](docs/fonts.md): your app's fonts in the screens. Coming in the next version.
- [Icons](docs/icons.md): your own icons in the screens. Coming in the next version.
- [Troubleshooting](docs/troubleshooting.md): known limits and fixes.

## For coding agents

- [CONTEXT.md](CONTEXT.md): what an agent needs to use this library correctly.
- [skills/react-native-liquid-glass/SKILL.md](skills/react-native-liquid-glass/SKILL.md): an agent skill.
- [llms.txt](llms.txt): an index of the docs.
- [AGENTS.md](AGENTS.md): working on this repository.

These files ship in the npm package, in `node_modules/@native-dev-kit/react-native-liquid-glass/`.

## Expo

Not supported yet. The library needs a bare React Native CLI app with the New Architecture. Support for Expo development builds is planned.

## Contributing

- [Development workflow](CONTRIBUTING.md#development-workflow)
- [Sending a pull request](CONTRIBUTING.md#sending-a-pull-request)
- [Code of conduct](CODE_OF_CONDUCT.md)

## License

MIT, see [LICENSE](LICENSE). The Android glass engine includes code under its own licenses: Apache 2.0 ([LICENSE-AndroidLiquidGlass](LICENSE-AndroidLiquidGlass)) and MIT ([LICENSE-Liquid-Glass-Android](LICENSE-Liquid-Glass-Android)).

# CONTEXT: @native-dev-kit/react-native-liquid-glass

What a coding agent needs to use this library correctly. Read it all before writing code with it. Every name below is exact.

## 1. Mental model

- The library ships **whole native screens**, not building blocks. On iOS each component is a SwiftUI screen from a native catalog app (real iOS Liquid Glass). On Android it is the same screen from a Jetpack Compose app (a Compose glass engine).
- A screen draws everything itself: title bar, back button, content, sheets, menus, alerts, covers. **It takes no children.** You cannot put React Native views inside it, and you cannot restyle its parts.
- There are **no** `GlassView`, `GlassButton` or other primitives. Do not invent them. If the user wants glass buttons inside their own React Native screen, this library does not do that; say so.
- All 22 components render one Fabric view, `LiquidGlassScreen`, with a `screen` id. Use the named components, not the native view.
- Settings (appearance, tint, Android glass options) come from `GlassSettingsProvider` through React context, not from screen props.

## 2. Requirements

- React Native 0.88 (verified), **New Architecture only**.
- Bare React Native CLI app. **Expo is not supported** (no config plugin yet).
- iOS 27.0+: `platform :ios, '27.0'` in `ios/Podfile`, then `cd ios && LANG=en_US.UTF-8 LC_ALL=en_US.UTF-8 pod install`.
- Android minSdk 26, **compileSdk 37, Android Gradle Plugin 9.1** (from the Compose BOM `2026.09.00`). Set `kotlinVersion` in `android/build.gradle` `ext`: the library applies the Compose compiler plugin at that version.
- Install: `npm install @native-dev-kit/react-native-liquid-glass`. Autolinking; nothing to register.

## 3. The exact API

```ts
import {
  // 22 screen components
  GlassHomeScreen, GlassEffectScreen, GlassViewScreen, InteractiveGlassScreen,
  GlassButtonsScreen, GlassContainerScreen, GlassMorphingScreen, GlassUnionScreen,
  GlassToolbarScreen, GlassFloatingHeaderScreen, GlassTabBarScreen, GlassSheetsScreen,
  GlassControlsScreen, GlassTextScreen, GlassTransitionsScreen, GlassSearchScreen,
  GlassEditSearchScreen, GlassScrollEdgeScreen, GlassSplitViewScreen,
  GlassZoomTransitionsScreen, GlassAlertsScreen, GlassRecipesScreen,
  // settings
  GlassSettingsProvider, useGlassSettings, DEFAULT_SETTINGS,
  // list and types
  GLASS_SCREENS, type GlassScreenId, type GlassScreenProps, type GlassSettings,
} from '@native-dev-kit/react-native-liquid-glass';
```

Nothing else is exported.

### Screen ids (`GlassScreenId`) and components

| id | component | title |
|---|---|---|
| — (`home`) | `GlassHomeScreen` | Liquid Glass (list of screens + settings) |
| `glassEffect` | `GlassEffectScreen` | Glass Effect |
| `glassView` | `GlassViewScreen` | Glass View |
| `interactive` | `InteractiveGlassScreen` | Interactive Glass |
| `buttons` | `GlassButtonsScreen` | Buttons |
| `container` | `GlassContainerScreen` | Glass Container |
| `morphing` | `GlassMorphingScreen` | Morphing |
| `union` | `GlassUnionScreen` | Union |
| `toolbar` | `GlassToolbarScreen` | Toolbar |
| `floatingHeader` | `GlassFloatingHeaderScreen` | Floating Header |
| `tabBar` | `GlassTabBarScreen` | Tab Bar |
| `sheets` | `GlassSheetsScreen` | Sheets & Menus |
| `controls` | `GlassControlsScreen` | Controls |
| `text` | `GlassTextScreen` | Text |
| `transitions` | `GlassTransitionsScreen` | Glass Transitions |
| `search` | `GlassSearchScreen` | Search |
| `editSearch` | `GlassEditSearchScreen` | Edit & Search Buttons |
| `scrollEdge` | `GlassScrollEdgeScreen` | Scroll Edge & Bars |
| `splitView` | `GlassSplitViewScreen` | Sidebar & Inspector |
| `zoomTransitions` | `GlassZoomTransitionsScreen` | Zoom Transitions |
| `alerts` | `GlassAlertsScreen` | Alerts & Dialogs |
| `recipes` | `GlassRecipesScreen` | Recipes |

`GLASS_SCREENS` is `readonly { id, title }[]` with the 21 catalog screens in Home's order. **It does not include Home.** `GlassScreenId` is the union of those ids (no `'home'`).

### Props (`GlassScreenProps`, same for all 22)

```ts
type GlassScreenProps = {
  onBack?: () => void;                                   // the screen's back/close button
  onOpen?: (screen: GlassScreenId) => void;              // Home only: a row was tapped
  onSettingsChange?: (settings: GlassSettings) => void;  // Home only: user changed a setting
  androidStyle?: 'glass' | 'material';                   // Android only; wins over the provider
  androidSegmentedCheck?: boolean;                       // Android Material only; wins over the provider
  fontFamily?: GlassSettings['fontFamily'];              // wins over the provider
  style?: StyleProp<ViewStyle>;                          // REQUIRED in practice: no default size
  testID?: string;
};
```

There are no other props (no `children`, no other settings props, no `title`).

### Settings

```ts
type GlassSettings = {
  appearance?: 'system' | 'light' | 'dark';   // iOS + Android
  tint?: ColorValue | null;                   // iOS + Android
  layoutDirection?: 'system' | 'ltr' | 'rtl'; // iOS + Android
  // Android only (iOS ignores them):
  androidStyle?: 'glass' | 'material';        // 'material': Material 3 components, no glass
  androidSegmentedCheck?: boolean;            // Material segmented buttons: check on the selected one
  clear?: boolean; blur?: number; white?: number; black?: number; edgeLight?: number;
  refraction?: { height: number; amount: number }; opaque?: boolean; liquid?: boolean;
  dropColor?: ColorValue | null; sliderLiquid?: boolean; switchLiquid?: boolean;
  segmentedLiquid?: boolean; tabBarLiquid?: boolean; tabBarDrop?: boolean;
  // iOS + Android: every text the screens draw. One family name, or a font name per weight
  // (names as in React Native's `fontFamily`; a missing weight uses `regular`). '' = system font.
  fontFamily?: string | { regular?: string; medium?: string; semibold?: string; bold?: string; heavy?: string };
};

DEFAULT_SETTINGS = {
  appearance: 'system', tint: null, layoutDirection: 'system', androidStyle: 'glass', androidSegmentedCheck: true, clear: true, blur: 3,
  white: 0.25, black: 0.05, edgeLight: 0.15, refraction: { height: 14, amount: 24 },
  opaque: false, liquid: true, dropColor: null, sliderLiquid: true, switchLiquid: true,
  segmentedLiquid: true, tabBarLiquid: true, tabBarDrop: true, fontFamily: '',
}; // typed Required<GlassSettings>
```

- `<GlassSettingsProvider value={Partial<GlassSettings>}>`: merges over the parent provider (or `DEFAULT_SETTINGS`). `undefined` values are ignored. Providers nest.
- `useGlassSettings()`: the merged `Required<GlassSettings>` at that point in the tree.
- Units (Android): `blur` and `refraction` in dp; `white`, `black`, `edgeLight` from 0 to 1.
- Colours from `onSettingsChange` arrive as `'#RRGGBBAA'` strings; "none" arrives as `null`.

## 4. The React Navigation recipe

The library does not depend on React Navigation. The verified setup is native-stack (`@react-navigation/native` 7, `@react-navigation/native-stack` 7, `react-native-screens` 4):

```tsx
import { useState } from 'react';
import { I18nManager, Platform, StyleSheet } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import {
  DEFAULT_SETTINGS, GLASS_SCREENS, GlassAlertsScreen, GlassButtonsScreen, GlassContainerScreen,
  GlassControlsScreen, GlassEditSearchScreen, GlassEffectScreen, GlassFloatingHeaderScreen,
  GlassHomeScreen, GlassMorphingScreen, GlassRecipesScreen, GlassScrollEdgeScreen,
  GlassSearchScreen, GlassSettingsProvider, GlassSheetsScreen, GlassSplitViewScreen,
  GlassTabBarScreen, GlassTextScreen, GlassToolbarScreen, GlassTransitionsScreen,
  GlassUnionScreen, GlassViewScreen, GlassZoomTransitionsScreen, InteractiveGlassScreen,
  type GlassScreenId, type GlassScreenProps, type GlassSettings,
} from '@native-dev-kit/react-native-liquid-glass';

const SCREENS: Record<GlassScreenId, (props: GlassScreenProps) => React.JSX.Element> = {
  glassEffect: GlassEffectScreen, glassView: GlassViewScreen, interactive: InteractiveGlassScreen,
  buttons: GlassButtonsScreen, container: GlassContainerScreen, morphing: GlassMorphingScreen,
  union: GlassUnionScreen, toolbar: GlassToolbarScreen, floatingHeader: GlassFloatingHeaderScreen,
  tabBar: GlassTabBarScreen, sheets: GlassSheetsScreen, controls: GlassControlsScreen,
  text: GlassTextScreen, transitions: GlassTransitionsScreen, search: GlassSearchScreen,
  editSearch: GlassEditSearchScreen, scrollEdge: GlassScrollEdgeScreen,
  splitView: GlassSplitViewScreen, zoomTransitions: GlassZoomTransitionsScreen,
  alerts: GlassAlertsScreen, recipes: GlassRecipesScreen,
};

type Routes = { Home: undefined } & Record<GlassScreenId, undefined>;
const Stack = createNativeStackNavigator<Routes>();

export default function App() {
  const [settings, setSettings] = useState<GlassSettings>(DEFAULT_SETTINGS);
  const direction = settings.layoutDirection ?? 'system';
  const rtl =
    direction === 'rtl' || (direction === 'system' && I18nManager.isRTL);
  return (
    <GlassSettingsProvider value={settings}>
      {/* iOS: UIKit's stack direction; it mirrors its own push and swipes. */}
      <NavigationContainer direction={rtl ? 'rtl' : 'ltr'}>
        <Stack.Navigator
          screenOptions={{
            headerShown: false,
            // The new screen slides in from the trailing edge, as on iOS.
            animation: Platform.select({
              android: rtl ? 'ios_from_left' : 'ios_from_right',
              default: 'default',
            }),
          }}
        >
          <Stack.Screen name="Home">
            {({ navigation }) => (
              <GlassHomeScreen
                style={styles.fill}
                onOpen={(id) => navigation.navigate(id)}
                onSettingsChange={setSettings}
              />
            )}
          </Stack.Screen>
          {GLASS_SCREENS.map(({ id }) => {
            const Screen = SCREENS[id];
            return (
              <Stack.Screen key={id} name={id}>
                {({ navigation }) => <Screen style={styles.fill} onBack={navigation.goBack} />}
              </Stack.Screen>
            );
          })}
        </Stack.Navigator>
      </NavigationContainer>
    </GlassSettingsProvider>
  );
}

const styles = StyleSheet.create({ fill: { flex: 1 } });
```

If the user wants only a few screens, register only those routes and guard `onOpen` (`if (id === 'buttons') navigation.navigate(id)`), otherwise a Home row for an unregistered screen does nothing and React Navigation logs that the NAVIGATE action was not handled.

## 5. Rules

1. **`style={{ flex: 1 }}` (or another sizing style) on every screen.** No default size: without it the screen is 0 × 0 and blank.
2. **`headerShown: false`** on the navigator. Each screen draws its own bar; a header doubles it.
3. **Feed `onSettingsChange` back into the provider** (`onSettingsChange={setSettings}`, `value={settings}`). Home does not keep changes itself; without this, Home's controls do nothing lasting and other screens never see them.
4. **Memoise the provider `value`.** Pass state or `useMemo`, never an inline object in a component that re-renders. A new object re-sends props to every mounted screen.
5. **`onBack` → `navigation.goBack`.** Android's hardware back is handled for you: the library sends it to the screen's open sheet/menu/search/cover first, then React Navigation pops. Do not add a `BackHandler` for glass screens.
6. **Stack animation:** Android `ios_from_right` (`ios_from_left` in RTL) gives the iOS push slide; without it Android fades. iOS: `default` in both directions, with `NavigationContainer direction={rtl ? 'rtl' : 'ltr'}` so UIKit mirrors the push and the swipes. Always pass `direction`: it defaults to `I18nManager.isRTL`, which would push from the left in an Arabic app showing LTR screens. An LTR-only app can use `animation: 'ios_from_right'` alone.
7. **Status bar:** glass screens set their own bar icons. On iOS under native-stack, React Native's `<StatusBar>` is inert; use the native-stack option `statusBarStyle` for your own screens.
8. **Do not use planned APIs** (`icons`, `vectorIcon`): they are not in the package yet (section 8).

## 6. Settings per platform

| | iOS | Android |
|---|---|---|
| `appearance` | Window-wide: sets the window's `overrideUserInterfaceStyle`, so the whole app (RN views and status bar) follows. | Inside the screen (Compose theme) plus the status/navigation bar icons. RN views unchanged. |
| `tint` | Glass tint. Home's picker shows None/Blue/Purple/Pink/Orange/Green/Teal; other colours still tint but Home shows None. | Tint of every glass surface without its own. |
| `layoutDirection` | Mirrors the screens; Home has a picker. `system` = device direction. Full-screen covers (Tab Bar "With Now Playing Bar") and UIKit pieces (menu rows, the open search field) stay LTR. | Mirrors the screens and controls; Home has the same picker. `system` = React Native's `I18nManager.isRTL`. Text reads in its own script's direction (English keeps its period last), so a wrapped English paragraph aligns left in RTL where iOS aligns it right. |
| `androidStyle` | Ignored: always glass. | `'material'`: the same screens drawn with Material 3 (top app bar with the back arrow, buttons, switches, sliders, segmented buttons, navigation bar, sheets, dialogs, menus, search bar, Android colors, type and ripples). Home has a Glass / Material picker. A screen's `androidStyle` prop wins over the provider. `androidSegmentedCheck` (default `true`, Material only; Home's switch in Material): `false` hides the check on the selected segment; also a screen prop. |
| Other Android-only keys | Ignored. | The glass engine's options; Home has controls for them. They do nothing in `'material'`. |

Appearance requests on both platforms: the **latest mounted** screen asking for `'light'` or `'dark'` wins; the app's own value comes back when the last one **unmounts**. A screen keeps its request while mounted even if off screen (under a pushed screen, in an inactive tab). `'system'` never writes the iOS window. On iOS, an `Appearance.setColorScheme` call made while a glass screen holds a request is lost when the request ends.

## 7. Common mistakes

| Mistake | Fix |
|---|---|
| Blank screen | Add `style={{ flex: 1 }}`. |
| Two bars at the top | `headerShown: false`. |
| Home's controls have no lasting effect, other screens ignore them | Wire `onSettingsChange` to the state passed to `GlassSettingsProvider`. |
| `value={{ appearance: 'dark' }}` inline | `useMemo` or state. |
| Passing children or settings as props to a screen | Not supported; screens take no children, settings go in the provider. |
| Including `'home'` in `GLASS_SCREENS` loops or `GlassScreenId` | Home is separate: `GlassHomeScreen`. |
| Importing `GlassView` / `GlassButton` | They do not exist. |
| Adding the library to an Expo managed app | Not supported. |
| Old architecture app | New Architecture only. |
| Android `compileSdk` 35/36 | Needs 37 and AGP 9.1. |
| iOS deployment target below 27 | Needs 27.0. |
| `<StatusBar>` to style a glass screen | Not needed; and inert on iOS under native-stack. |
| Whole iOS app turns dark when a glass screen is mounted | Expected (window-wide). Use `'system'` or match the app's style. |

## 8. Coming in the next version (do not use yet)

- `icons?: Record<sfSymbolName, GlassIcon>` with `GlassIcon = { glyph, fontFamily } | { svg } | ImageSourcePropType`, and `vectorIcon(iconSet, name)`.

Details: `docs/icons.md`. Shipped: `docs/android-style.md`, `docs/layout-direction.md`, `docs/fonts.md`.

## 9. Known limits

- Android, Xiaomi/MIUI in system dark mode: status bar icons follow the content under them, so the Floating Header in Light may show white icons. Platform behaviour; left as is.
- Android back routing: API 36+ needs the activity to be a `NavigationEventDispatcherOwner` (`ReactActivity` / `ComponentActivity` is). Below API 36 the library wraps the activity window's `Window.Callback`; a library that replaces it without delegating breaks this until a glass screen re-enters the window.
- Two glass screens visible at once (transparent modal, form sheet): the back key goes to the one that entered the window last.
- Only native-stack is verified. Bottom tabs, JS stack and React Native `<Modal>` are untested.
- iOS: each screen has its own navigation bar, so the bar slides with its screen on push instead of cross-fading.

## 10. Verify on a device

1. Build and run: `npx react-native run-ios` / `npx react-native run-android`. Debug loads JS from Metro on port 8081; make sure that Metro is this app's. To test without Metro: `--mode Release` (iOS) / `--mode release` (Android).
2. Home appears with the list of 21 screens. Tap a row: the screen slides in, with its own title bar and back button.
3. Tap the screen's back button: back to Home, scroll position kept.
4. Android: open a sheet or the search field, press the system back key: it closes, the screen stays. Press again: back to Home.
5. iOS: swipe from the left edge: pops.
6. On Home change Appearance to Dark and the tint: Home changes at once; open a screen: it is dark and tinted.
7. No red box, no "Unimplemented component" (that means the New Architecture is off or pods/Gradle were not rebuilt).

## 11. More

- `README.md`, `docs/*.md` (getting-started, screens, settings, react-navigation, layout-direction, android-style, fonts, icons, troubleshooting), `llms.txt`.
- All of these ship inside the package: `node_modules/@native-dev-kit/react-native-liquid-glass/`.

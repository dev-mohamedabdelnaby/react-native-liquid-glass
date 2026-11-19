# React Navigation

The library does not depend on React Navigation. It only sends `onOpen` (Home row tapped) and `onBack` (back button tapped). This guide wires them to native-stack, the setup verified on both platforms.

## Install

```sh
npm install @react-navigation/native @react-navigation/native-stack react-native-screens react-native-safe-area-context
cd ios && LANG=en_US.UTF-8 LC_ALL=en_US.UTF-8 pod install
```

Verified with `@react-navigation/native` 7.5, `@react-navigation/native-stack` 7.21 and `react-native-screens` 4.30.

## The recipe: Home and all 21 screens

```tsx
import { useState } from 'react';
import { I18nManager, Platform, StyleSheet } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import {
  DEFAULT_SETTINGS,
  GLASS_SCREENS,
  GlassAlertsScreen,
  GlassButtonsScreen,
  GlassContainerScreen,
  GlassControlsScreen,
  GlassEditSearchScreen,
  GlassEffectScreen,
  GlassFloatingHeaderScreen,
  GlassHomeScreen,
  GlassMorphingScreen,
  GlassRecipesScreen,
  GlassScrollEdgeScreen,
  GlassSearchScreen,
  GlassSettingsProvider,
  GlassSheetsScreen,
  GlassSplitViewScreen,
  GlassTabBarScreen,
  GlassTextScreen,
  GlassToolbarScreen,
  GlassTransitionsScreen,
  GlassUnionScreen,
  GlassViewScreen,
  GlassZoomTransitionsScreen,
  InteractiveGlassScreen,
  type GlassScreenId,
  type GlassScreenProps,
  type GlassSettings,
} from '@native-dev-kit/react-native-liquid-glass';

const SCREENS: Record<GlassScreenId, (props: GlassScreenProps) => React.JSX.Element> = {
  glassEffect: GlassEffectScreen,
  glassView: GlassViewScreen,
  interactive: InteractiveGlassScreen,
  buttons: GlassButtonsScreen,
  container: GlassContainerScreen,
  morphing: GlassMorphingScreen,
  union: GlassUnionScreen,
  toolbar: GlassToolbarScreen,
  floatingHeader: GlassFloatingHeaderScreen,
  tabBar: GlassTabBarScreen,
  sheets: GlassSheetsScreen,
  controls: GlassControlsScreen,
  text: GlassTextScreen,
  transitions: GlassTransitionsScreen,
  search: GlassSearchScreen,
  editSearch: GlassEditSearchScreen,
  scrollEdge: GlassScrollEdgeScreen,
  splitView: GlassSplitViewScreen,
  zoomTransitions: GlassZoomTransitionsScreen,
  alerts: GlassAlertsScreen,
  recipes: GlassRecipesScreen,
};

type Routes = { Home: undefined } & Record<GlassScreenId, undefined>;

const Stack = createNativeStackNavigator<Routes>();

export default function App() {
  const [settings, setSettings] = useState<GlassSettings>(DEFAULT_SETTINGS);
  const rtl =
    settings.layoutDirection === 'rtl' ||
    (settings.layoutDirection === 'system' && I18nManager.isRTL);
  return (
    <GlassSettingsProvider value={settings}>
      <NavigationContainer>
        <Stack.Navigator
          // As UINavigationController: the new screen slides in from the trailing edge.
          screenOptions={{
            headerShown: false,
            animation: Platform.select({
              android: rtl ? 'ios_from_left' : 'ios_from_right',
              default: rtl ? 'slide_from_left' : 'default',
            }),
            // iOS: the swipe back starts at the leading (right) edge and drives the same slide.
            animationMatchesGesture: rtl,
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
                {({ navigation }) => (
                  <Screen style={styles.fill} onBack={navigation.goBack} />
                )}
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

This is the example app's `example/src/App.tsx`.

## Why each line is there

- **`headerShown: false`.** Every glass screen draws its own title bar and back button. A navigator header would add a second bar on top.
- **`animation`.** On Android, `ios_from_right` gives the iOS slide: the pushed screen comes in from the right and the one below moves a third of the way. Without it, Android shows a fade between screens. On iOS, `default` is the system's own push. In right-to-left, both come from the left (`ios_from_left`, `slide_from_left`), and `animationMatchesGesture` lets the iOS swipe from the right edge drive the pop. If your app is left-to-right only, `animation: 'ios_from_right'` alone is enough. See [layout-direction.md](layout-direction.md).
- **Route name = screen id.** Then `onOpen={(id) => navigation.navigate(id)}` needs no mapping.
- **`onBack={navigation.goBack}`.** The screen's own back button pops it. Passing `goBack` unbound is safe.
- **`onSettingsChange={setSettings}` on Home, `value={settings}` on the provider.** Home's controls change every screen, live. See [settings.md](settings.md).
- **`style={styles.fill}`.** The screens have no default size.

## Back on each platform

- **iOS.** The screen's back button sends `onBack`. The swipe from the left edge is native-stack's gesture and pops the screen.
- **Android.** The system back key or gesture first closes what is open inside the glass screen: a sheet, a menu, a popover, an alert, the search field, a panel, the Tab Bar cover. When nothing is open, React Navigation pops the screen. This is done by the library's host; you do not add a `BackHandler`.

## Status bar

- Glass screens set the status bar icons for their appearance on both platforms. You do not need a `<StatusBar>` for them.
- **iOS under native-stack, React Native's `<StatusBar>` component has no effect** (react-native-screens owns the status bar). For a non-glass screen of your own that needs light icons, use the native-stack option: `options={{ statusBarStyle: 'light' }}`.
- **Android:** `<StatusBar barStyle="…">` on your own screens works. When you go back to a glass screen, it sets its icons again.

## Mixing in your own screens

Glass screens and your React Native screens can live in the same stack. Push your screen from a glass screen's callback, or push a glass screen from your own button:

```tsx
navigation.navigate('buttons');
```

## Not covered yet

- Only native-stack is verified. Bottom tabs, the JS stack and React Native's `<Modal>` are untested.
- Glass tab bar and glass header components for your own navigators are not in this version.

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
import { CUSTOMIZE, CUSTOM_SETTINGS } from './customize';

const SCREENS: Record<
  GlassScreenId,
  (props: GlassScreenProps) => React.JSX.Element
> = {
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
  const [settings, setSettings] = useState<GlassSettings>(
    CUSTOMIZE ? { ...DEFAULT_SETTINGS, ...CUSTOM_SETTINGS } : DEFAULT_SETTINGS
  );
  const direction = settings.layoutDirection ?? 'system';
  const rtl =
    direction === 'rtl' || (direction === 'system' && I18nManager.isRTL);
  return (
    <GlassSettingsProvider value={settings}>
      {/* iOS: the stack's direction is UIKit's, which then mirrors its own push and swipes back. */}
      <NavigationContainer direction={rtl ? 'rtl' : 'ltr'}>
        <Stack.Navigator
          // As a UINavigationController: the new screen slides in from the trailing edge.
          screenOptions={{
            headerShown: false,
            // Android's slides are absolute, so they follow the direction by hand.
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

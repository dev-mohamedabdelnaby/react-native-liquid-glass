import {
  DarkTheme,
  DefaultTheme,
  NavigationContainer,
} from '@react-navigation/native';
import {
  createNativeStackNavigator,
  type NativeStackNavigationOptions,
} from '@react-navigation/native-stack';
import { StatusBar } from 'expo-status-bar';
import { useMemo, useState } from 'react';
import { Platform } from 'react-native';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import {
  DEFAULT_SETTINGS,
  GlassSettingsProvider,
  type GlassSettings,
} from '@mohamed_abd_elnaby/react-native-liquid-glass';
import { ButtonsScreen } from './screens/ButtonsScreen';
import { FloatingHeaderScreen } from './screens/FloatingHeaderScreen';
import { GlassEffectScreen } from './screens/GlassEffectScreen';
import { GlassViewScreen } from './screens/GlassViewScreen';
import { Home } from './screens/Home';
import { InteractiveGlassScreen } from './screens/InteractiveGlassScreen';
import { TabsScreen } from './screens/TabsScreen';
import { useTheme } from './theme';

export type Routes = {
  Home: undefined;
  GlassEffect: undefined;
  GlassView: undefined;
  Interactive: undefined;
  Buttons: undefined;
  FloatingHeader: undefined;
  TabBar: undefined;
};

const Stack = createNativeStackNavigator<Routes>();

// iOS: the system header, transparent, with its glass back button. Android draws the catalog's
// own glass top bar in GlassScreen.
const screenOptions: NativeStackNavigationOptions =
  Platform.OS === 'ios'
    ? { headerTransparent: true, headerBackButtonDisplayMode: 'minimal' }
    : { headerShown: false };

export default function App() {
  const { c, dark } = useTheme();
  // Home's Glass settings; the one provider above the navigator feeds every screen live.
  const [settings, setSettings] = useState<GlassSettings>(DEFAULT_SETTINGS);
  const theme = useMemo(() => {
    const base = dark ? DarkTheme : DefaultTheme;
    return {
      ...base,
      colors: {
        ...base.colors,
        primary: c.primary,
        background: c.background,
        card: c.surface,
        text: c.onSurface,
      },
    };
  }, [dark, c]);
  return (
    <SafeAreaProvider>
      <StatusBar style="auto" />
      <GlassSettingsProvider value={settings}>
        <NavigationContainer theme={theme}>
          <Stack.Navigator screenOptions={screenOptions}>
            <Stack.Screen
              name="Home"
              options={{ title: 'Liquid Glass', headerLargeTitleEnabled: true }}
            >
              {(props) => (
                <Home
                  {...props}
                  settings={settings}
                  onSettingsChange={setSettings}
                />
              )}
            </Stack.Screen>
            <Stack.Screen
              name="GlassEffect"
              component={GlassEffectScreen}
              options={{ title: 'Glass Effect' }}
            />
            <Stack.Screen
              name="GlassView"
              component={GlassViewScreen}
              options={{ title: 'Glass View' }}
            />
            <Stack.Screen
              name="Interactive"
              component={InteractiveGlassScreen}
              options={{ title: 'Interactive' }}
            />
            <Stack.Screen
              name="Buttons"
              component={ButtonsScreen}
              options={{ title: 'Buttons' }}
            />
            <Stack.Screen
              name="FloatingHeader"
              component={FloatingHeaderScreen}
              options={{ title: '' }}
            />
            <Stack.Screen
              name="TabBar"
              component={TabsScreen}
              // Each tab shows its own large title in its content.
              options={{ title: '' }}
            />
          </Stack.Navigator>
        </NavigationContainer>
      </GlassSettingsProvider>
    </SafeAreaProvider>
  );
}

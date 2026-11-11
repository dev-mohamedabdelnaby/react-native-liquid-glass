import { useState } from 'react';
import { StyleSheet } from 'react-native';
import {
  DEFAULT_SETTINGS,
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
} from '@mohamed_abd_elnaby/react-native-liquid-glass';

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

// Temporary switcher until React Navigation is wired: the open screen covers Home, which stays mounted.
export default function App() {
  const [settings, setSettings] = useState<GlassSettings>(DEFAULT_SETTINGS);
  const [open, setOpen] = useState<GlassScreenId | null>(null);
  const Screen = open && SCREENS[open];
  return (
    <GlassSettingsProvider value={settings}>
      <GlassHomeScreen
        style={styles.fill}
        onOpen={setOpen}
        onSettingsChange={setSettings}
      />
      {Screen && (
        <Screen style={StyleSheet.absoluteFill} onBack={() => setOpen(null)} />
      )}
    </GlassSettingsProvider>
  );
}

const styles = StyleSheet.create({ fill: { flex: 1 } });

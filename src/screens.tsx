import type { StyleProp, ViewStyle } from 'react-native';
import LiquidGlassScreen from './LiquidGlassScreenNativeComponent';
import { useGlassSettings } from './settings';
import type { GlassSettings } from './types';

// One line per screen, exactly this shape: the Android unit test reads the ids with a regex.
export const GLASS_SCREENS = [
  { id: 'glassEffect', title: 'Glass Effect' },
  { id: 'glassView', title: 'Glass View' },
  { id: 'interactive', title: 'Interactive Glass' },
  { id: 'buttons', title: 'Buttons' },
  { id: 'container', title: 'Glass Container' },
  { id: 'morphing', title: 'Morphing' },
  { id: 'union', title: 'Union' },
  { id: 'toolbar', title: 'Toolbar' },
  { id: 'floatingHeader', title: 'Floating Header' },
  { id: 'tabBar', title: 'Tab Bar' },
  { id: 'sheets', title: 'Sheets & Menus' },
  { id: 'controls', title: 'Controls' },
  { id: 'text', title: 'Text' },
  { id: 'transitions', title: 'Glass Transitions' },
  { id: 'search', title: 'Search' },
  { id: 'editSearch', title: 'Edit & Search Buttons' },
  { id: 'scrollEdge', title: 'Scroll Edge & Bars' },
  { id: 'splitView', title: 'Sidebar & Inspector' },
  { id: 'zoomTransitions', title: 'Zoom Transitions' },
  { id: 'alerts', title: 'Alerts & Dialogs' },
  { id: 'recipes', title: 'Recipes' },
] as const;

export type GlassScreenId = (typeof GLASS_SCREENS)[number]['id'];

export type GlassScreenProps = {
  // Wins over the provider's androidStyle for this screen.
  androidStyle?: GlassSettings['androidStyle'];
  androidSegmentedCheck?: boolean;
  onBack?: () => void;
  onOpen?: (screen: GlassScreenId) => void;
  onSettingsChange?: (settings: GlassSettings) => void;
  style?: StyleProp<ViewStyle>;
  testID?: string;
};

function GlassScreen({
  screen,
  androidStyle,
  androidSegmentedCheck,
  onBack,
  onOpen,
  onSettingsChange,
  style,
  testID,
}: GlassScreenProps & { screen: GlassScreenId | 'home' }) {
  const { tint, refraction, ...settings } = useGlassSettings();
  return (
    <LiquidGlassScreen
      {...settings}
      androidStyle={androidStyle ?? settings.androidStyle}
      androidSegmentedCheck={
        androidSegmentedCheck ?? settings.androidSegmentedCheck
      }
      screen={screen}
      tintColor={tint}
      refractionHeight={refraction.height}
      refractionAmount={refraction.amount}
      style={style}
      testID={testID}
      onBack={() => onBack?.()}
      onOpen={(e) => onOpen?.(e.nativeEvent.screen as GlassScreenId)}
      onSettingsChange={({ nativeEvent: n }) => {
        const {
          refractionHeight: height,
          refractionAmount: amount,
          ...rest
        } = n;
        onSettingsChange?.({
          ...rest,
          tint: n.tint || null,
          dropColor: n.dropColor || null,
          refraction: { height, amount },
        } as GlassSettings);
      }}
    />
  );
}

const screen = (id: GlassScreenId | 'home') => (props: GlassScreenProps) => (
  <GlassScreen screen={id} {...props} />
);

export const GlassHomeScreen = screen('home');
export const GlassEffectScreen = screen('glassEffect');
export const GlassViewScreen = screen('glassView');
export const InteractiveGlassScreen = screen('interactive');
export const GlassButtonsScreen = screen('buttons');
export const GlassContainerScreen = screen('container');
export const GlassMorphingScreen = screen('morphing');
export const GlassUnionScreen = screen('union');
export const GlassToolbarScreen = screen('toolbar');
export const GlassFloatingHeaderScreen = screen('floatingHeader');
export const GlassTabBarScreen = screen('tabBar');
export const GlassSheetsScreen = screen('sheets');
export const GlassControlsScreen = screen('controls');
export const GlassTextScreen = screen('text');
export const GlassTransitionsScreen = screen('transitions');
export const GlassSearchScreen = screen('search');
export const GlassEditSearchScreen = screen('editSearch');
export const GlassScrollEdgeScreen = screen('scrollEdge');
export const GlassSplitViewScreen = screen('splitView');
export const GlassZoomTransitionsScreen = screen('zoomTransitions');
export const GlassAlertsScreen = screen('alerts');
export const GlassRecipesScreen = screen('recipes');

import type { ColorValue } from 'react-native';

// Each platform reads the settings its native app has and ignores the rest.
export type GlassSettings = {
  appearance?: 'system' | 'light' | 'dark';
  tint?: ColorValue | null;
  layoutDirection?: 'system' | 'ltr' | 'rtl';
  // Android only: 'material' draws the screens with Material 3 instead of glass.
  androidStyle?: 'glass' | 'material';
  // Android Material only: the check mark on the selected segment of segmented buttons.
  androidSegmentedCheck?: boolean;
  clear?: boolean;
  blur?: number;
  white?: number;
  black?: number;
  edgeLight?: number;
  refraction?: { height: number; amount: number };
  opaque?: boolean;
  liquid?: boolean;
  dropColor?: ColorValue | null;
  sliderLiquid?: boolean;
  switchLiquid?: boolean;
  segmentedLiquid?: boolean;
  tabBarLiquid?: boolean;
  tabBarDrop?: boolean;
};

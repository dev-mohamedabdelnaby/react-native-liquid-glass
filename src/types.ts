import type { ColorValue } from 'react-native';

// Each platform reads the settings its native app has and ignores the rest.
export type GlassSettings = {
  appearance?: 'system' | 'light' | 'dark';
  tint?: ColorValue | null;
  layoutDirection?: 'system' | 'ltr' | 'rtl';
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

import type { ColorValue, ImageSourcePropType } from 'react-native';

// A custom icon: a glyph of an icon font (react-native-vector-icons: see vectorIcon), SVG markup,
// or an image. Drawn as a template, at the size and tint of the icon it replaces.
export type GlassIcon =
  { glyph: string; fontFamily: string } | { svg: string } | ImageSourcePropType;

// Each platform reads the settings its native app has and ignores the rest.
export type GlassSettings = {
  appearance?: 'system' | 'light' | 'dark';
  tint?: ColorValue | null;
  layoutDirection?: 'system' | 'ltr' | 'rtl';
  // Android only: 'material' draws the screens with Material 3 instead of glass.
  androidStyle?: 'glass' | 'material';
  // Android only: the shape of switches, sliders, top tabs and bottom tabs, in both styles.
  androidControlShape?: 'pill' | 'labeled' | 'glow';
  // Android only: how strongly the glow shape glows, 0 (none) to 1.
  androidGlowIntensity?: number;
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
  // Every text the screens draw: one family for all weights, or a font per weight. Names as in
  // React Native's `fontFamily`; '' (the default) keeps the system font.
  fontFamily?:
    | string
    | {
        regular?: string;
        medium?: string;
        semibold?: string;
        bold?: string;
        heavy?: string;
      };
  // Custom icons, keyed by the SF Symbol name the screens use for that icon on both platforms
  // ('square.and.arrow.up'). An icon not in the map (or undefined) stays native.
  icons?: Record<string, GlassIcon | undefined>;
};

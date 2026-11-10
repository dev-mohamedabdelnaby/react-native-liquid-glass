import type { ColorValue } from 'react-native';

export type GlassStyle = 'regular' | 'clear';

export type Glass = {
  style?: GlassStyle;
  tint?: ColorValue;
  interactive?: boolean;
  opaque?: boolean;
  liquid?: boolean;
  blur?: number;
  white?: number;
  black?: number;
  edgeLight?: number;
  refraction?: { height: number; amount: number };
};

export type GlassShape = 'capsule' | 'circle' | { cornerRadius: number };

export type GlassSettings = {
  tint?: ColorValue;
  clear: boolean;
  blur: number;
  white: number;
  black: number;
  edgeLight: number;
  refraction: { height: number; amount: number };
  opaque: boolean;
  liquid: boolean;
  dropColor?: ColorValue;
  sliderLiquid: boolean;
  switchLiquid: boolean;
  segmentedLiquid: boolean;
  tabBarLiquid: boolean;
  tabBarDrop: boolean;
};

export type NativeGlassProps = {
  glassStyle: GlassStyle;
  tintColor: ColorValue | null;
  interactive: boolean;
  opaque: boolean;
  liquid: boolean;
  blur: number;
  white: number;
  black: number;
  edgeLight: number;
  refractionHeight: number;
  refractionAmount: number;
  shapeKind: 'capsule' | 'circle' | 'rect';
  cornerRadius: number;
};

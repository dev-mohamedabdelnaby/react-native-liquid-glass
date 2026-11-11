import {
  codegenNativeComponent,
  type CodegenTypes,
  type ColorValue,
  type ViewProps,
} from 'react-native';

// Defaults are the native apps' defaults (see DEFAULT_SETTINGS in settings.tsx).
export interface NativeProps extends ViewProps {
  screen?: string;
  appearance?: CodegenTypes.WithDefault<string, 'system'>;
  layoutDirection?: CodegenTypes.WithDefault<string, 'system'>;
  tintColor?: ColorValue;
  clear?: CodegenTypes.WithDefault<boolean, true>;
  blur?: CodegenTypes.WithDefault<CodegenTypes.Float, 3>;
  white?: CodegenTypes.WithDefault<CodegenTypes.Float, 0.25>;
  black?: CodegenTypes.WithDefault<CodegenTypes.Float, 0.05>;
  edgeLight?: CodegenTypes.WithDefault<CodegenTypes.Float, 0.15>;
  refractionHeight?: CodegenTypes.WithDefault<CodegenTypes.Float, 14>;
  refractionAmount?: CodegenTypes.WithDefault<CodegenTypes.Float, 24>;
  opaque?: CodegenTypes.WithDefault<boolean, false>;
  liquid?: CodegenTypes.WithDefault<boolean, true>;
  dropColor?: ColorValue;
  sliderLiquid?: CodegenTypes.WithDefault<boolean, true>;
  switchLiquid?: CodegenTypes.WithDefault<boolean, true>;
  segmentedLiquid?: CodegenTypes.WithDefault<boolean, true>;
  tabBarLiquid?: CodegenTypes.WithDefault<boolean, true>;
  tabBarDrop?: CodegenTypes.WithDefault<boolean, true>;

  onBack?: CodegenTypes.DirectEventHandler<null>;
  onOpen?: CodegenTypes.DirectEventHandler<Readonly<{ screen: string }>>;
  // Colours are '#RRGGBBAA' strings, '' meaning none.
  onSettingsChange?: CodegenTypes.DirectEventHandler<
    Readonly<{
      appearance: string;
      layoutDirection: string;
      tint: string;
      clear: boolean;
      blur: CodegenTypes.Float;
      white: CodegenTypes.Float;
      black: CodegenTypes.Float;
      edgeLight: CodegenTypes.Float;
      refractionHeight: CodegenTypes.Float;
      refractionAmount: CodegenTypes.Float;
      opaque: boolean;
      liquid: boolean;
      dropColor: string;
      sliderLiquid: boolean;
      switchLiquid: boolean;
      segmentedLiquid: boolean;
      tabBarLiquid: boolean;
      tabBarDrop: boolean;
    }>
  >;
}

export default codegenNativeComponent<NativeProps>('LiquidGlassScreen');

import {
  codegenNativeComponent,
  type CodegenTypes,
  type ColorValue,
  type ViewProps,
} from 'react-native';

// The native contract: resolveGlass() produces every one of these (see NativeGlassProps).
export interface NativeProps extends ViewProps {
  glassStyle?: CodegenTypes.WithDefault<'regular' | 'clear', 'regular'>;
  tintColor?: ColorValue;
  interactive?: CodegenTypes.WithDefault<boolean, false>;
  opaque?: CodegenTypes.WithDefault<boolean, false>;
  liquid?: CodegenTypes.WithDefault<boolean, true>;
  blur?: CodegenTypes.WithDefault<CodegenTypes.Float, 3>;
  white?: CodegenTypes.WithDefault<CodegenTypes.Float, 0.25>;
  black?: CodegenTypes.WithDefault<CodegenTypes.Float, 0.05>;
  edgeLight?: CodegenTypes.WithDefault<CodegenTypes.Float, 0.15>;
  refractionHeight?: CodegenTypes.WithDefault<CodegenTypes.Float, 14>;
  refractionAmount?: CodegenTypes.WithDefault<CodegenTypes.Float, 24>;
  shapeKind?: CodegenTypes.WithDefault<
    'capsule' | 'circle' | 'rect',
    'capsule'
  >;
  cornerRadius?: CodegenTypes.WithDefault<CodegenTypes.Float, 0>;
}

export default codegenNativeComponent<NativeProps>('RNGlassView');

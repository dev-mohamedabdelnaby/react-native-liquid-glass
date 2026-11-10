import {
  codegenNativeComponent,
  type CodegenTypes,
  type ColorValue,
  type ViewProps,
} from 'react-native';

// Codegen cannot read props from another file, so the glass props repeat GlassViewNativeComponent's.
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

  title?: string;
  systemImage?: string;
  androidIcon?: string;
  prominent?: CodegenTypes.WithDefault<boolean, false>;
  size?: CodegenTypes.WithDefault<'small' | 'regular' | 'large', 'regular'>;
  disabled?: CodegenTypes.WithDefault<boolean, false>;
  // Not `onPress`: iOS's base view config already has `topPress` as a bubbling event, and an
  // event cannot be both direct and bubbling.
  onButtonPress?: CodegenTypes.DirectEventHandler<null>;
}

export default codegenNativeComponent<NativeProps>('RNGlassButton');

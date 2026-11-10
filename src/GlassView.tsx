import { View, type ViewProps } from 'react-native';
import NativeGlassView from './GlassViewNativeComponent';
import { resolveGlass, warnOnce } from './resolveGlass';
import { useGlassSettings } from './settings';
import { isGlassSupported } from './support';
import type { Glass, GlassShape } from './types';

export type GlassViewProps = ViewProps & {
  glass?: Glass;
  shape?: GlassShape;
};

export function GlassView({ glass, shape, children, ...rest }: GlassViewProps) {
  const settings = useGlassSettings();
  if (!isGlassSupported()) {
    warnOnce(
      'unsupported',
      'Liquid Glass is not supported here, GlassView renders a plain View.'
    );
    return <View {...rest}>{children}</View>;
  }
  return (
    <NativeGlassView {...rest} {...resolveGlass(glass, settings, shape)}>
      {children}
    </NativeGlassView>
  );
}

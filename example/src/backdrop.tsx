import { Image, StyleSheet, View } from 'react-native';
import { useTheme } from './theme';

const PHOTOS = {
  fjord: require('../assets/backdrop_fjord.jpg'),
  jellyfish: require('../assets/backdrop_jellyfish.jpg'),
};

export type Backdrop = keyof typeof PHOTOS | 'plain' | 'system';

/**
 * As the catalog's GlassBackdrop: a photo behind the glass, dimmed a little in dark mode so
 * labels stay legible. `plain` is the grouped-list gray, `system` the plain page colour.
 */
export function GlassBackdrop({ kind = 'fjord' }: { kind?: Backdrop }) {
  const { c, dark } = useTheme();
  if (kind === 'plain' || kind === 'system') {
    const color = kind === 'plain' ? c.background : c.systemBackground;
    return (
      <View style={[StyleSheet.absoluteFill, { backgroundColor: color }]} />
    );
  }
  return (
    <View style={[StyleSheet.absoluteFill, { backgroundColor: c.background }]}>
      <Image
        source={PHOTOS[kind]}
        resizeMode="cover"
        style={StyleSheet.absoluteFill}
      />
      {dark && <View style={[StyleSheet.absoluteFill, styles.dim]} />}
    </View>
  );
}

const styles = StyleSheet.create({
  dim: { backgroundColor: 'rgba(0,0,0,0.3)' },
});

import {
  Image,
  Platform,
  StyleSheet,
  View,
  useWindowDimensions,
} from 'react-native';
import { useTheme } from './theme';

const PHOTOS = {
  fjord: require('../assets/backdrop_fjord.jpg'),
  jellyfish: require('../assets/backdrop_jellyfish.jpg'),
};

export type Backdrop = keyof typeof PHOTOS | 'plain' | 'system';

/**
 * The catalog's backdrop. Android (GlassBackdrop.kt): a photo, dimmed a little in dark mode so
 * labels stay legible. iOS (GlassBackdrop.swift): a soft gradient with two blurred circles and a
 * faint sparkles glyph. `plain` is the grouped-list gray, `system` the plain page colour.
 */
export function GlassBackdrop({ kind = 'fjord' }: { kind?: Backdrop }) {
  const { c, dark } = useTheme();
  if (kind === 'plain' || kind === 'system') {
    const color = kind === 'plain' ? c.background : c.systemBackground;
    return (
      <View style={[StyleSheet.absoluteFill, { backgroundColor: color }]} />
    );
  }
  if (Platform.OS === 'ios') {
    return <IosBackdrop />;
  }
  return (
    <View style={[StyleSheet.absoluteFill, { backgroundColor: c.background }]}>
      {/* width/height 100%: without them the image keeps its intrinsic size (1080 x 2160 dp) and
          only its top-left corner shows, magnified and blurry. */}
      <Image source={PHOTOS[kind]} resizeMode="cover" style={styles.photo} />
      {dark && <View style={[StyleSheet.absoluteFill, styles.dim]} />}
    </View>
  );
}

// SwiftUI colours (light / dark variants) used by GlassBackdrop.swift.
const IOS = {
  light: {
    page: '#FFFFFF',
    gradient:
      'linear-gradient(to bottom right, rgba(50,173,230,0.35), rgba(255,45,85,0.35), rgba(255,149,0,0.35))',
    first: 'rgba(0,199,190,0.6)', // mint
    second: 'rgba(255,204,0,0.6)', // yellow
    glyph: '#000000',
  },
  dark: {
    page: '#000000',
    gradient:
      'linear-gradient(to bottom right, #000000, #5E5CE6, rgba(191,90,242,0.8))',
    first: 'rgba(10,132,255,0.6)', // blue
    second: 'rgba(255,55,95,0.5)', // pink
    glyph: '#FFFFFF',
  },
};

// SwiftUI's blur(radius: 30) on a circle: solid well inside, fading out about 60 pt past its edge.
const BLUR = 30;

function BlurredCircle({
  diameter,
  color,
  dx,
  dy,
}: {
  diameter: number;
  color: string;
  dx: number;
  dy: number;
}) {
  const { width, height } = useWindowDimensions();
  const r = diameter / 2;
  const outer = r + 2 * BLUR;
  const pct = (v: number) => `${Math.round((v / outer) * 100)}%`;
  // The same colour at half its alpha, at the circle's edge.
  const half = color.replace(/[\d.]+\)$/, (a) => `${parseFloat(a) / 2})`);
  return (
    <View
      style={{
        ...styles.absolute,
        width: 2 * outer,
        height: 2 * outer,
        left: width / 2 + dx - outer,
        top: height / 2 + dy - outer,
        backgroundImage: `radial-gradient(circle closest-side, ${color} 0%, ${color} ${pct(r - BLUR)}, ${half} ${pct(r)}, transparent 100%)`,
      }}
    />
  );
}

function IosBackdrop() {
  const { dark } = useTheme();
  const look = dark ? IOS.dark : IOS.light;
  return (
    <View
      style={[
        StyleSheet.absoluteFill,
        styles.center,
        { backgroundColor: look.page, backgroundImage: look.gradient },
      ]}
    >
      <BlurredCircle diameter={260} color={look.first} dx={-120} dy={-220} />
      <BlurredCircle diameter={200} color={look.second} dx={140} dy={240} />
      {/* .font(.system(size: 160)), .primary at 15%. */}
      <Image
        source={require('../assets/icons/sparkles.ios.png')}
        style={[styles.sparkles, { tintColor: look.glyph }]}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  photo: { width: '100%', height: '100%' },
  absolute: { position: 'absolute' },
  dim: { backgroundColor: 'rgba(0,0,0,0.3)' },
  center: { alignItems: 'center', justifyContent: 'center' },
  sparkles: { width: 206, height: 206, opacity: 0.15 },
});

import { useNavigation } from '@react-navigation/native';
import type { ReactNode } from 'react';
import { Platform, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import {
  GlassButton,
  type Glass,
} from '@mohamed_abd_elnaby/react-native-liquid-glass';
import { GlassBackdrop, type Backdrop } from './backdrop';
import { useTheme, withAlpha } from './theme';

/** How content fades where it meets the top bar (the catalog's EdgeEffect). */
export type TopEdge = 'soft' | 'statusBar' | 'hidden';

// GlassTopBar in glass/GlassScreen.kt: 4 dp above and below a 52 dp row.
export const BAR_HEIGHT = 4 + 52 + 4;

/**
 * The iOS 26 back button: regular glass whatever the general setting, milky white with a soft
 * blur and a visible hairline (BackButtonGlass).
 */
const BACK_GLASS: Glass = {
  style: 'regular',
  blur: 4,
  white: 0.3,
  edgeLight: 0.1,
};

const isAndroid = Platform.OS === 'android';

type Props = {
  /** The bar title on Android; iOS takes it from the navigator's options. */
  title: string;
  backdrop?: Backdrop;
  /** false: a centred column that does not scroll, as the native Buttons screen. */
  scroll?: boolean;
  /** Content runs under the bar, edge to edge, with no padding (Floating Header). */
  edgeToEdge?: boolean;
  spacing?: number;
  topEdge?: TopEdge;
  /** Android bar trailing items; iOS uses the navigator's header items. */
  actions?: ReactNode;
  /** A ✕ in place of the back chevron. */
  closeButton?: boolean;
  onBack?: () => void;
  /** Extra space under the content, for a floating tab bar. */
  bottomInset?: number;
  children: ReactNode;
};

/** A catalog screen: backdrop, scrolling content and, on Android, the glass top bar. */
export function GlassScreen({
  title,
  backdrop = 'fjord',
  scroll = true,
  edgeToEdge = false,
  spacing = 28,
  topEdge = 'soft',
  actions,
  closeButton = false,
  onBack,
  bottomInset = 0,
  children,
}: Props) {
  const insets = useSafeAreaInsets();
  const navigation = useNavigation();
  const { c, t } = useTheme();
  // DemoColumn: 16 dp around the content, below the bar on Android.
  const top = edgeToEdge ? 0 : isAndroid ? insets.top + BAR_HEIGHT + 16 : 16;
  const bottom = (isAndroid ? insets.bottom : 0) + bottomInset + 16;
  return (
    <View style={styles.flex}>
      <GlassBackdrop kind={backdrop} />
      <ScrollView
        style={styles.flex}
        scrollEnabled={scroll}
        // iOS: the transparent native header and the safe areas inset the content.
        contentInsetAdjustmentBehavior={edgeToEdge ? 'never' : 'automatic'}
        contentContainerStyle={[
          {
            paddingTop: top,
            paddingBottom: bottom,
            gap: spacing,
          },
          !edgeToEdge && styles.padded,
          !scroll && styles.centered,
        ]}
      >
        {children}
      </ScrollView>
      {isAndroid && (
        <View style={styles.bar} pointerEvents="box-none">
          <TopEdgeFade edge={topEdge} height={insets.top + BAR_HEIGHT} />
          <View
            style={[styles.barRow, { marginTop: insets.top + 4 }]}
            pointerEvents="box-none"
          >
            <GlassButton
              icon={closeButton ? 'xmark' : 'chevron.left'}
              accessibilityLabel={closeButton ? 'Close' : 'Back'}
              glass={BACK_GLASS}
              style={styles.back}
              onPress={onBack ?? (() => navigation.goBack())}
            />
            <Text
              style={[t.headline, styles.barTitle, { color: c.onSurface }]}
              numberOfLines={1}
              pointerEvents="none"
            >
              {title}
            </Text>
            <View style={styles.actions}>{actions}</View>
          </View>
        </View>
      )}
    </View>
  );
}

/**
 * The fade of the catalog's soft ScrollEdge: solid over the first 40% next to the bar, then
 * fading out. The native one also blurs what is under it; that is not built here.
 */
export function TopEdgeFade({
  edge,
  height,
}: {
  edge: TopEdge;
  height: number;
}) {
  const { c } = useTheme();
  const insets = useSafeAreaInsets();
  if (edge === 'hidden') return null;
  const near = withAlpha(c.background, 0.7);
  const far = withAlpha(c.background, 0);
  return (
    <View
      pointerEvents="none"
      style={[
        styles.fade,
        {
          height: edge === 'statusBar' ? insets.top : height,
          backgroundImage: `linear-gradient(to bottom, ${near} 0%, ${near} 40%, ${far} 100%)`,
        },
      ]}
    />
  );
}

/** A navigation stack's large title: `.largeTitle.bold()`, leading. */
export function LargeTitle({ children }: { children: string }) {
  const { t } = useTheme();
  return <Text style={[t.largeTitle, styles.largeTitle]}>{children}</Text>;
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  padded: { paddingHorizontal: 16 },
  centered: { flexGrow: 1, justifyContent: 'center', alignItems: 'center' },
  bar: { position: 'absolute', top: 0, left: 0, right: 0 },
  fade: { position: 'absolute', top: 0, left: 0, right: 0 },
  barRow: {
    height: 52,
    marginBottom: 4,
    paddingHorizontal: 16,
    justifyContent: 'center',
  },
  back: { position: 'absolute', left: 16, width: 44, height: 44 },
  barTitle: { textAlign: 'center', paddingHorizontal: 64 },
  actions: {
    position: 'absolute',
    right: 16,
    flexDirection: 'row',
    alignItems: 'center',
  },
  largeTitle: { marginBottom: 4 },
});

import { useNavigation } from '@react-navigation/native';
import type { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { useLayoutEffect, useState } from 'react';
import {
  Platform,
  Pressable,
  Share,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import {
  GlassButton,
  GlassView,
} from '@mohamed_abd_elnaby/react-native-liquid-glass';
import type { Routes } from '../App';
import { Choice, Icon } from '../controls';
import { GlassScreen, TopEdgeFade, type TopEdge } from '../GlassScreen';
import { SystemColors, useTheme } from '../theme';

const android = Platform.OS === 'android';

const EDGES: { title: string; edge: TopEdge; description: string }[] = [
  {
    title: 'Status Bar',
    edge: 'statusBar',
    description:
      'Only the status bar area gets a fade, so the clock and battery stay legible.',
  },
  {
    title: 'Soft',
    edge: 'soft',
    description: android
      ? 'A soft scroll edge effect fades content under the whole top bar.'
      : 'The system soft scroll edge effect fades content under the whole toolbar.',
  },
  {
    title: 'None',
    edge: 'hidden',
    description:
      'No edge effect at all: content scrolls fully visible under the glass buttons.',
  },
];

const share = () => Share.share({ message: 'Floating Header' });

export function FloatingHeaderScreen() {
  const { c, t } = useTheme();
  const navigation =
    useNavigation<NativeStackNavigationProp<Routes, 'FloatingHeader'>>();
  const [edgeIndex, setEdgeIndex] = useState(0);
  const [favorite, setFavorite] = useState(false);
  const edge = EDGES[edgeIndex]!;

  // iOS: the native bar items, grouped as the catalog's toolbar (heart, spacer, share + more).
  useLayoutEffect(() => {
    if (android) return;
    navigation.setOptions({
      scrollEdgeEffects: { top: edge.edge === 'soft' ? 'soft' : 'hidden' },
      unstable_headerRightItems: () => [
        {
          type: 'button',
          label: favorite ? 'Unfavorite' : 'Favorite',
          icon: { type: 'sfSymbol', name: favorite ? 'heart.fill' : 'heart' },
          onPress: () => setFavorite(!favorite),
          // Its own glass, apart from the share + more group.
          sharesBackground: false,
        },
        { type: 'spacing', spacing: 8 },
        {
          type: 'button',
          label: 'Share',
          icon: { type: 'sfSymbol', name: 'square.and.arrow.up' },
          onPress: share,
        },
        {
          type: 'button',
          label: 'More',
          icon: { type: 'sfSymbol', name: 'ellipsis' },
          onPress: () => {},
        },
      ],
    });
  }, [navigation, edge.edge, favorite]);

  return (
    <View style={styles.flex}>
      <GlassScreen
        title=""
        backdrop="system"
        edgeToEdge
        spacing={16}
        topEdge={edge.edge}
        actions={
          <>
            <GlassButton
              icon={favorite ? 'heart.fill' : 'heart'}
              accessibilityLabel={favorite ? 'Unfavorite' : 'Favorite'}
              style={styles.icon44}
              onPress={() => setFavorite(!favorite)}
            />
            <View style={styles.spacer} />
            <GlassView glass={{ interactive: true }} style={styles.group}>
              <BarIcon name="share" label="Share" onPress={share} />
              <BarIcon name="more" label="More" onPress={() => {}} />
            </GlassView>
          </>
        }
      >
        <View style={styles.header}>
          {/* .largeTitle.bold() and .subheadline. */}
          <Text style={[t.largeTitle, styles.white]}>Floating Header</Text>
          <Text style={[t.subheadline, styles.white]}>
            No bar background — just glass buttons
          </Text>
        </View>
        <View style={styles.inset}>
          <Choice
            options={EDGES.map((e) => e.title)}
            selected={edgeIndex}
            onSelect={setEdgeIndex}
          />
          <Text style={t.footnote}>{edge.description}</Text>
        </View>
        {Array.from({ length: 30 }, (_, i) => (
          <View
            key={i}
            style={[
              styles.rowCard,
              { backgroundColor: c.secondarySystemBackground },
            ]}
          >
            <Icon name="photo" size={26} color={c.primary} />
            <Text style={t.body}>Row {i + 1}</Text>
          </View>
        ))}
      </GlassScreen>
      {/* iOS: the status bar fade; Android's comes with the scaffold's top bar. */}
      {!android && edge.edge === 'statusBar' && (
        <TopEdgeFade edge="statusBar" height={0} />
      )}
    </View>
  );
}

/** An icon with no glass of its own, inside a shared glass group. */
function BarIcon({
  name,
  label,
  onPress,
}: {
  name: 'share' | 'more';
  label: string;
  onPress: () => void;
}) {
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityLabel={label}
      onPress={onPress}
      style={styles.icon44}
    >
      <Icon name={name} size={20} />
    </Pressable>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  icon44: {
    width: 44,
    height: 44,
    alignItems: 'center',
    justifyContent: 'center',
  },
  spacer: { width: 8 },
  group: { flexDirection: 'row' },
  header: {
    height: 320,
    justifyContent: 'flex-end',
    padding: 16,
    gap: 4,
    backgroundImage: `linear-gradient(to bottom right, ${SystemColors.Indigo}, ${SystemColors.Purple}, ${SystemColors.Pink}, ${SystemColors.Orange})`,
  },
  white: { color: '#FFFFFF' },
  inset: { paddingHorizontal: 16, gap: 16 },
  rowCard: {
    marginHorizontal: 16,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    padding: 16,
    borderRadius: 16,
  },
});

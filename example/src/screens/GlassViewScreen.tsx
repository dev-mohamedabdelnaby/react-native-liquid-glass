import { useState } from 'react';
import { Platform, Pressable, StyleSheet, Text, View } from 'react-native';
import {
  GlassView,
  useGlassSettings,
  type GlassShape,
} from '@mohamed_abd_elnaby/react-native-liquid-glass';
import { Choice, Icon, Stepper, SwitchRow } from '../controls';
import { GlassScreen } from '../GlassScreen';
import { SystemColors, useTheme, withAlpha } from '../theme';

const SHAPES = ['Capsule', 'Rounded', 'Circle', 'Rect'] as const;
type Shape = (typeof SHAPES)[number];

const TINTS = [
  SystemColors.Blue,
  SystemColors.Orange,
  SystemColors.Green,
  SystemColors.Pink,
  SystemColors.Purple,
  SystemColors.Teal,
];

const android = Platform.OS === 'android';
// The circle's padding around its icon.
const CIRCLE_PADDING = 36;
// The platform view's untinted layer.
const WHITE_WASH = 'rgba(255,255,255,0.35)';

export function GlassViewScreen() {
  const { c, t } = useTheme();
  // This view alone: its controls start from Home's general settings and then override them.
  const general = useGlassSettings();
  // Android starts from the general setting, the iOS catalog from Regular.
  const [isClear, setClear] = useState(android && general.clear);
  const [shape, setShape] = useState<Shape>('Rounded');
  const [cornerRadius, setCornerRadius] = useState(24);
  const [contentPadding, setContentPadding] = useState(24);
  const [blur, setBlur] = useState(general.blur);
  const [white, setWhite] = useState(general.white);
  const [black, setBlack] = useState(general.black);
  const [edge, setEdge] = useState(general.edgeLight);
  const [refractionHeight, setRefractionHeight] = useState(
    general.refraction.height
  );
  const [refractionAmount, setRefractionAmount] = useState(
    general.refraction.amount
  );
  const [interactive, setInteractive] = useState(true);
  const [tinted, setTinted] = useState(false);
  const [tint, setTint] = useState(SystemColors.Blue);
  const circle = shape === 'Circle';
  const glassShape: GlassShape =
    shape === 'Capsule'
      ? 'capsule'
      : circle
        ? 'circle'
        : { cornerRadius: shape === 'Rect' ? 0 : cornerRadius };
  const pct = (v: number) => `${Math.round(v * 100)}%`;
  return (
    <GlassScreen title="Glass View" backdrop="jellyfish">
      <View style={styles.preview}>
        <GlassView
          shape={glassShape}
          glass={{
            style: isClear ? 'clear' : 'regular',
            tint: tinted ? withAlpha(tint, 0.6) : undefined,
            interactive,
            blur,
            white,
            black,
            edgeLight: edge,
            refraction: { height: refractionHeight, amount: refractionAmount },
          }}
          style={[
            { padding: circle ? CIRCLE_PADDING : contentPadding },
            !circle && styles.fullWidth,
          ]}
        >
          {circle ? (
            <Icon name="drop_fill" size={48} />
          ) : (
            <View style={styles.gap8}>
              <View style={styles.label}>
                <Icon name="drop_fill" />
                <Text style={[t.title3, styles.bold]}>Liquid Glass View</Text>
              </View>
              <Text style={t.secondary}>
                Any content can sit on a glass surface. Adjust the options
                below.
              </Text>
            </View>
          )}
        </GlassView>
      </View>

      <GlassView shape={{ cornerRadius: 28 }} style={styles.controls}>
        <Choice
          options={['Regular', 'Clear']}
          selected={isClear ? 1 : 0}
          onSelect={(i) => setClear(i === 1)}
        />
        <Choice
          options={[...SHAPES]}
          selected={SHAPES.indexOf(shape)}
          onSelect={(i) => setShape(SHAPES[i]!)}
        />
        {shape === 'Rounded' && (
          <Stepper
            label={`Corner radius: ${cornerRadius}`}
            value={cornerRadius}
            min={0}
            max={60}
            step={2}
            onChange={setCornerRadius}
          />
        )}
        {/* The lens settings are Android only (iOS has no padding slider either). */}
        {android && (
          <>
            <Stepper
              label={`Padding: ${contentPadding} dp`}
              value={contentPadding}
              min={8}
              max={56}
              step={2}
              onChange={setContentPadding}
            />
            <Stepper
              label={`Blur: ${blur} dp`}
              value={blur}
              min={0}
              max={40}
              step={1}
              onChange={setBlur}
            />
            <Stepper
              label={`White: ${pct(white)}`}
              value={white}
              min={0}
              max={1}
              step={0.05}
              onChange={setWhite}
            />
            <Stepper
              label={`Black: ${pct(black)}`}
              value={black}
              min={0}
              max={1}
              step={0.05}
              onChange={setBlack}
            />
            <Stepper
              label={`Edge light: ${pct(edge)}`}
              value={edge}
              min={0}
              max={1}
              step={0.05}
              onChange={setEdge}
            />
            <Stepper
              label={`Refraction height: ${refractionHeight} dp`}
              value={refractionHeight}
              min={0}
              max={40}
              step={1}
              onChange={setRefractionHeight}
            />
            <Stepper
              label={`Refraction amount: ${refractionAmount} dp`}
              value={refractionAmount}
              min={0}
              max={60}
              step={1}
              onChange={setRefractionAmount}
            />
          </>
        )}
        <SwitchRow
          label="Interactive"
          value={interactive}
          onChange={setInteractive}
        />
        <SwitchRow label="Tint" value={tinted} onChange={setTinted} />
        {tinted && (
          <View style={styles.tintRow}>
            <Text style={[t.body, styles.flex]}>Tint color</Text>
            {TINTS.map((color) => (
              <Pressable
                key={color}
                accessibilityRole="radio"
                accessibilityState={{ selected: color === tint }}
                accessibilityLabel={`Tint ${color}`}
                onPress={() => setTint(color)}
                style={[
                  styles.tint,
                  { backgroundColor: color },
                  color === tint && [
                    styles.tintPicked,
                    { borderColor: c.onSurface },
                  ],
                ]}
              />
            ))}
          </View>
        )}
      </GlassView>

      <View style={styles.platform}>
        <Text style={t.headline}>
          {android ? 'Platform view · RenderEffect' : 'UIKit · UIGlassEffect'}
        </Text>
        <PlatformGlassView
          tint={tinted ? tint : undefined}
          interactive={interactive}
        />
      </View>
    </GlassScreen>
  );
}

/**
 * The comparison view under the playground. iOS: UIVisualEffectView + UIGlassEffect, which is
 * what GlassView is. Android: a framework view, a blurred gradient under a tint and a label.
 */
function PlatformGlassView({
  tint,
  interactive,
}: {
  tint: string | undefined;
  interactive: boolean;
}) {
  const { c } = useTheme();
  if (!android) {
    return (
      <GlassView
        shape={{ cornerRadius: 28 }}
        glass={{
          style: 'regular',
          tint: tint && withAlpha(tint, 0.6),
          interactive,
        }}
        style={styles.platformView}
      >
        <Text style={[styles.platformLabel, { color: c.onSurface }]}>
          UIVisualEffectView + UIGlassEffect
        </Text>
      </GlassView>
    );
  }
  return (
    <Pressable
      disabled={!interactive}
      style={({ pressed }) => [
        styles.platformView,
        styles.gradient,
        pressed && styles.pressed,
      ]}
    >
      <View
        style={[
          StyleSheet.absoluteFill,
          {
            backgroundColor: tint ? withAlpha(tint, 0.6) : WHITE_WASH,
          },
        ]}
      />
      <Text style={[styles.platformLabel, { color: c.onSurface }]}>
        android.view.View + RenderEffect
      </Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  preview: { height: 220, alignItems: 'center', justifyContent: 'center' },
  fullWidth: { alignSelf: 'stretch' },
  gap8: { gap: 8 },
  label: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  bold: { fontWeight: '700' },
  controls: { padding: 20, gap: 16 },
  tintRow: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  tint: { width: 28, height: 28, borderRadius: 14 },
  tintPicked: { borderWidth: 3 },
  platform: { alignItems: 'center', gap: 12 },
  platformView: {
    alignSelf: 'stretch',
    height: 100,
    borderRadius: 28,
    overflow: 'hidden',
    alignItems: 'center',
    justifyContent: 'center',
  },
  gradient: {
    backgroundImage: `linear-gradient(to bottom right, ${SystemColors.Cyan}, ${SystemColors.Pink}, ${SystemColors.Orange})`,
  },
  pressed: { transform: [{ scale: 1.03 }] },
  platformLabel: { fontSize: 16, fontWeight: '700' },
});

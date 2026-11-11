import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import {
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import type { GlassSettings } from '@mohamed_abd_elnaby/react-native-liquid-glass';
import type { Routes } from '../App';
import { GlassBackdrop } from '../backdrop';
import { Choice, Icon, ListSection, Stepper, SwitchRow } from '../controls';
import { TopEdgeFade } from '../GlassScreen';
import { SystemColors, useTheme, withAlpha, type IconName } from '../theme';

/** The ported catalog components, in the catalog's order (GlassComponent.kt). */
const COMPONENTS: { title: string; icon: IconName; route: keyof Routes }[] = [
  { title: 'Glass Effect', icon: 'drop_fill', route: 'GlassEffect' },
  { title: 'Glass View', icon: 'rectangle_on_rectangle', route: 'GlassView' },
  { title: 'Interactive Glass', icon: 'hand_tap', route: 'Interactive' },
  { title: 'Buttons', icon: 'button_horizontal', route: 'Buttons' },
  { title: 'Floating Header', icon: 'header', route: 'FloatingHeader' },
  { title: 'Tab Bar', icon: 'dock', route: 'TabBar' },
];

const DROP_COLORS = [
  SystemColors.Blue,
  SystemColors.Purple,
  SystemColors.Pink,
  SystemColors.Orange,
  SystemColors.Green,
  SystemColors.Teal,
];

// Strong enough to read as the color, still glass.
const GLASS_TINT_ALPHA = 0.35;

type Props = NativeStackScreenProps<Routes, 'Home'> & {
  settings: GlassSettings;
  onSettingsChange: (settings: GlassSettings) => void;
};

export function Home({ navigation, settings, onSettingsChange }: Props) {
  const insets = useSafeAreaInsets();
  const { c, t } = useTheme();
  const android = Platform.OS === 'android';
  return (
    <View style={styles.flex}>
      <GlassBackdrop kind="plain" />
      <ScrollView
        contentInsetAdjustmentBehavior="automatic"
        contentContainerStyle={[
          styles.content,
          android && { paddingTop: insets.top + 24 },
          { paddingBottom: (android ? insets.bottom : 0) + 24 },
        ]}
      >
        <View style={styles.top}>
          {/* iOS shows the navigator's large title. */}
          {android && (
            <Text style={[t.largeTitle, styles.title]}>Liquid Glass</Text>
          )}
          <SettingsPanel settings={settings} onChange={onSettingsChange} />
        </View>
        <Text style={[t.sectionHeader, styles.componentsHeader]}>
          Components
        </Text>
        {COMPONENTS.map((item, i) => {
          const last = i === COMPONENTS.length - 1;
          return (
            <Pressable
              key={item.route}
              accessibilityRole="button"
              onPress={() => navigation.navigate(item.route)}
              style={[
                { backgroundColor: c.surface },
                i === 0 && styles.firstRow,
                last && styles.lastRow,
              ]}
            >
              <View style={styles.row}>
                <Icon name={item.icon} size={22} color={c.primary} />
                <Text style={[t.body, styles.flex]}>{item.title}</Text>
                <Icon name="chevron_right" size={20} color={c.chevron} />
              </View>
              {!last && (
                <View
                  style={[
                    styles.divider,
                    { backgroundColor: c.outlineVariant },
                  ]}
                />
              )}
            </Pressable>
          );
        })}
      </ScrollView>
      {android && <TopEdgeFade edge="soft" height={insets.top + 16} />}
    </View>
  );
}

/** The Glass Lab controls every component in the catalog follows (HomeScreen.kt). */
function SettingsPanel({
  settings,
  onChange,
}: {
  settings: GlassSettings;
  onChange: (settings: GlassSettings) => void;
}) {
  const { t } = useTheme();
  const android = Platform.OS === 'android';
  const set = (patch: Partial<GlassSettings>) =>
    onChange({ ...settings, ...patch });
  const pct = (v: number) => `${Math.round(v * 100)}%`;
  return (
    <ListSection
      header="Glass"
      footer="Every component follows these, like the Glass Lab in LiquidPlayground."
    >
      <View style={styles.panel}>
        <Choice
          options={['Regular', 'Clear']}
          selected={settings.clear ? 1 : 0}
          onSelect={(i) => set({ clear: i === 1 })}
        />
        {/* Android only: iOS glass has no blur, wash, lens or liquid switch. */}
        {android && (
          <>
            {/* The blur label's 8 dp top padding. */}
            <View style={styles.blurTop} />
            <Stepper
              label={`Blur: ${settings.blur} dp`}
              value={settings.blur}
              min={0}
              max={40}
              step={1}
              onChange={(blur) => set({ blur })}
            />
            <Stepper
              label={`White: ${pct(settings.white)}`}
              value={settings.white}
              min={0}
              max={1}
              step={0.05}
              onChange={(white) => set({ white })}
            />
            <Stepper
              label={`Black: ${pct(settings.black)}`}
              value={settings.black}
              min={0}
              max={1}
              step={0.05}
              onChange={(black) => set({ black })}
            />
            <Stepper
              label={`Edge light: ${pct(settings.edgeLight)}`}
              value={settings.edgeLight}
              min={0}
              max={1}
              step={0.05}
              onChange={(edgeLight) => set({ edgeLight })}
            />
            <Stepper
              label={`Refraction height: ${settings.refraction.height} dp`}
              value={settings.refraction.height}
              min={0}
              max={40}
              step={1}
              onChange={(height) =>
                set({ refraction: { ...settings.refraction, height } })
              }
            />
            <Stepper
              label={`Refraction amount: ${settings.refraction.amount} dp`}
              value={settings.refraction.amount}
              min={0}
              max={60}
              step={1}
              onChange={(amount) =>
                set({ refraction: { ...settings.refraction, amount } })
              }
            />
            <SwitchRow
              label="Liquid glass (off: frosted blur)"
              value={settings.liquid}
              onChange={(liquid) => set({ liquid })}
            />
          </>
        )}
        <SwitchRow
          label="Opaque (no see-through)"
          value={settings.opaque}
          onChange={(opaque) => set({ opaque })}
        />
        <Text style={t.body}>Glass tint</Text>
        <TintChoices
          picked={settings.tint as string | undefined}
          onPick={(color) =>
            set({ tint: color && withAlpha(color, GLASS_TINT_ALPHA) })
          }
        />
      </View>
    </ListSection>
  );
}

/** None (the clear drop) or one of the drop colours; the picked one is ringed. */
function TintChoices({
  picked,
  onPick,
}: {
  picked: string | undefined;
  onPick: (color: string | undefined) => void;
}) {
  const { c } = useTheme();
  return (
    <View style={styles.swatches}>
      {[undefined, ...DROP_COLORS].map((color) => {
        const selected =
          picked === (color && withAlpha(color, GLASS_TINT_ALPHA));
        return (
          <Pressable
            key={color ?? 'none'}
            accessibilityRole="radio"
            accessibilityState={{ selected }}
            accessibilityLabel={color ? 'Glass tint' : 'No Glass tint'}
            onPress={() => onPick(color)}
            style={[styles.swatch, selected && { borderColor: c.primary }]}
          >
            <View
              style={[
                styles.swatchFill,
                color
                  ? { backgroundColor: color }
                  : [styles.swatchNone, { borderColor: c.outlineVariant }],
              ]}
            >
              {!color && (
                <Icon name="drop" size={14} color={c.onSurfaceVariant} />
              )}
            </View>
          </Pressable>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  // iOS: the large title header sits above; 8 pt to the first section.
  content: { paddingHorizontal: 16, paddingTop: 8 },
  top: { gap: 16, paddingBottom: 28 },
  title: { paddingLeft: 4 },
  panel: { padding: 16, gap: 4 },
  blurTop: { height: 4 },
  componentsHeader: { paddingLeft: 16, paddingBottom: 6 },
  firstRow: {
    borderTopLeftRadius: 26,
    borderTopRightRadius: 26,
    overflow: 'hidden',
  },
  lastRow: {
    borderBottomLeftRadius: 26,
    borderBottomRightRadius: 26,
    overflow: 'hidden',
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    minHeight: 52,
    paddingHorizontal: 16,
    paddingVertical: 8,
  },
  divider: { height: 0.5, marginHorizontal: 16 },
  swatches: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  // The ring is always there (transparent when not picked) so the fill stays 24 wide, as native.
  swatch: {
    width: 32,
    height: 32,
    borderRadius: 16,
    padding: 2,
    borderWidth: 2,
    borderColor: 'transparent',
  },
  swatchNone: { borderWidth: 1 },
  swatchFill: {
    flex: 1,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
  },
});

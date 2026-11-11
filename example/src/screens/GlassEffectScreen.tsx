import { useState, type ComponentProps, type ReactNode } from 'react';
import { Platform, Pressable, StyleSheet, Text, View } from 'react-native';
import { GlassView } from '@mohamed_abd_elnaby/react-native-liquid-glass';
import { Icon, MenuPanel, type MenuItem } from '../controls';
import { GlassScreen } from '../GlassScreen';
import { SystemColors, useTheme } from '../theme';

const PALETTE: [string, string][] = [
  ['Orange', SystemColors.Orange],
  ['Blue', SystemColors.Blue],
  ['Green', SystemColors.Green],
  ['Pink', SystemColors.Pink],
  ['Purple', SystemColors.Purple],
  ['Teal', SystemColors.Teal],
];
const GREETINGS = ['Hello, Liquid Glass!', 'Hi there 👋', 'Welcome!'];
const CHIPS = ['Orange', 'Blue', 'Green'];
// The native menu names the unit: dp on Android, pt on iOS.
const UNIT = Platform.OS === 'android' ? 'dp' : 'pt';

type MenuId = 'greeting' | 'clear' | 'star' | 'rounded' | 'rect' | string;

export function GlassEffectScreen() {
  const { t } = useTheme();
  const [greeting, setGreeting] = useState(GREETINGS[0]!);
  const [greetingClear, setGreetingClear] = useState(false);
  const [clearDimmed, setClearDimmed] = useState(false);
  const [chipTints, setChipTints] = useState<
    Record<string, string | undefined>
  >({
    Orange: SystemColors.Orange,
    Blue: SystemColors.Blue,
    Green: SystemColors.Green,
  });
  const [starFilled, setStarFilled] = useState(true);
  const [cornerRadius, setCornerRadius] = useState(16);
  const [rectTinted, setRectTinted] = useState(false);
  const [open, setOpen] = useState<MenuId | null>(null);
  const toggle = (id: MenuId) => setOpen(open === id ? null : id);
  const menu = (id: MenuId, items: MenuItem[]) =>
    open === id && <MenuPanel items={items} onClose={() => setOpen(null)} />;

  return (
    <GlassScreen title="Glass Effect">
      <Text style={[t.footnote, styles.center]}>
        Tap any glass item for its menu
      </Text>

      <Section title="Regular (default capsule)">
        <GlassItem
          label="Greeting menu"
          glass={{ style: greetingClear ? 'clear' : 'regular' }}
          onPress={() => toggle('greeting')}
          style={styles.pad16}
        >
          <Text style={t.title2}>{greeting}</Text>
        </GlassItem>
        {menu('greeting', [
          { header: 'Variant' },
          {
            label: 'Regular',
            checked: !greetingClear,
            onPress: () => setGreetingClear(false),
          },
          {
            label: 'Clear',
            checked: greetingClear,
            onPress: () => setGreetingClear(true),
          },
          { divider: true },
          { header: 'Greeting' },
          ...GREETINGS.map((text) => ({
            label: text,
            onPress: () => setGreeting(text),
          })),
        ])}
      </Section>

      <Section title="Clear variant">
        <GlassItem
          label="Clear glass menu"
          glass={{ style: 'clear' }}
          onPress={() => toggle('clear')}
        >
          {/* A dimming layer between the glass and the label keeps text legible. */}
          {clearDimmed && (
            <View style={[StyleSheet.absoluteFill, styles.dim]} />
          )}
          <View style={styles.clearLabel}>
            <Icon name="drop" size={20} />
            <Text style={t.title3}>Clear glass</Text>
          </View>
        </GlassItem>
        {menu('clear', [
          {
            label: 'Dimming Layer',
            checked: clearDimmed,
            onPress: () => setClearDimmed(!clearDimmed),
          },
        ])}
      </Section>

      <Section title="Tinted">
        <View style={styles.row16}>
          {CHIPS.map((chip) => (
            <GlassItem
              key={chip}
              label={`${chip} tint menu`}
              glass={{ tint: chipTints[chip] }}
              onPress={() => toggle(chip)}
              style={styles.pad16}
            >
              <Text style={t.body}>{chip}</Text>
            </GlassItem>
          ))}
        </View>
        {CHIPS.map((chip) =>
          menu(chip, [
            { header: 'Tint' },
            ...PALETTE.map(([name, color]) => ({
              label: name,
              onPress: () => setChipTints({ ...chipTints, [chip]: color }),
            })),
            { divider: true },
            {
              label: 'Remove Tint',
              onPress: () => setChipTints({ ...chipTints, [chip]: undefined }),
            },
          ])
        )}
      </Section>

      <Section title="Custom shapes">
        <View style={styles.row16}>
          <GlassItem
            label="Favorite menu"
            shape="circle"
            onPress={() => toggle('star')}
            style={styles.star}
          >
            <Icon name={starFilled ? 'star_fill' : 'star'} size={28} />
          </GlassItem>
          <GlassItem
            label="Corner radius menu"
            shape={{ cornerRadius }}
            onPress={() => toggle('rounded')}
            style={styles.pad16}
          >
            <Text style={t.body}>Rounded</Text>
          </GlassItem>
          <GlassItem
            label="Rect menu"
            shape={{ cornerRadius: 0 }}
            glass={{ tint: rectTinted ? SystemColors.Purple : undefined }}
            onPress={() => toggle('rect')}
            style={styles.pad16}
          >
            <Text style={t.body}>Rect</Text>
          </GlassItem>
        </View>
        {menu('star', [
          {
            label: starFilled ? 'Unfavorite' : 'Favorite',
            onPress: () => setStarFilled(!starFilled),
          },
        ])}
        {menu('rounded', [
          { header: 'Corner Radius' },
          ...[4, 16, 32].map((r) => ({
            label: `${r} ${UNIT}`,
            checked: cornerRadius === r,
            onPress: () => setCornerRadius(r),
          })),
        ])}
        {menu('rect', [
          {
            label: 'Tint',
            checked: rectTinted,
            onPress: () => setRectTinted(!rectTinted),
          },
        ])}
      </Section>
    </GlassScreen>
  );
}

/** An interactive glass item that opens its menu. */
function GlassItem({
  label,
  glass,
  shape = 'capsule',
  onPress,
  style,
  children,
}: {
  label: string;
  glass?: ComponentProps<typeof GlassView>['glass'];
  shape?: ComponentProps<typeof GlassView>['shape'];
  onPress: () => void;
  style?: ComponentProps<typeof View>['style'];
  children: ReactNode;
}) {
  return (
    <GlassView shape={shape} glass={{ ...glass, interactive: true }}>
      <Pressable
        accessibilityRole="button"
        accessibilityLabel={label}
        onPress={onPress}
        style={[styles.item, style]}
      >
        {children}
      </Pressable>
    </GlassView>
  );
}

function Section({ title, children }: { title: string; children: ReactNode }) {
  const { t } = useTheme();
  return (
    <View style={styles.section}>
      <Text style={t.headline}>{title}</Text>
      {children}
    </View>
  );
}

const styles = StyleSheet.create({
  center: { textAlign: 'center' },
  section: { alignItems: 'center', gap: 12 },
  item: { alignItems: 'center', justifyContent: 'center' },
  pad16: { padding: 16 },
  row16: { flexDirection: 'row', alignItems: 'center', gap: 16 },
  clearLabel: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    padding: 16,
  },
  dim: { backgroundColor: 'rgba(0,0,0,0.35)', borderRadius: 999 },
  star: { width: 70, height: 70 },
});

// Stand-ins for the controls that ship in sub-project 4, built only from GlassButton, GlassView
// and React Native core.
import { useState, type ReactNode } from 'react';
import {
  Image,
  Platform,
  Pressable,
  StyleSheet,
  Switch,
  Text,
  View,
  type ColorValue,
  type StyleProp,
  type ViewStyle,
} from 'react-native';
import {
  GlassButton,
  GlassView,
  type GlassButtonProps,
} from '@mohamed_abd_elnaby/react-native-liquid-glass';
import { ICONS, SystemColors, useTheme, type IconName } from './theme';

export function Icon({
  name,
  size = 24,
  color,
}: {
  name: IconName;
  size?: number;
  color?: ColorValue;
}) {
  const { c } = useTheme();
  return (
    <Image
      source={ICONS[name]}
      style={{ width: size, height: size, tintColor: color ?? c.onSurface }}
    />
  );
}

// GlassControlSize: label padding and font per size; the button has no native text measuring.
const SIZES = {
  small: { padding: 14, font: 15 },
  regular: { padding: 20, font: 17 },
  large: { padding: 24, font: 17 },
};

/** A GlassButton as wide as its label, measured with an invisible Text. */
export function FitButton(props: GlassButtonProps) {
  const [width, setWidth] = useState(0);
  const size = SIZES[props.size ?? 'regular'];
  const icon = props.icon ? 20 + (props.title ? 8 : 0) : 0;
  return (
    <View>
      <Text
        style={[styles.measure, { fontSize: size.font }]}
        onLayout={(e) => setWidth(e.nativeEvent.layout.width)}
      >
        {props.title}
      </Text>
      <GlassButton
        {...props}
        style={[
          { width: Math.ceil(width) + icon + 2 * size.padding },
          props.style,
        ]}
      />
    </View>
  );
}

/** The segmented control / picker stand-in: one button per option, the selected one prominent. */
export function Choice({
  options,
  selected,
  onSelect,
}: {
  options: string[];
  selected: number;
  onSelect: (index: number) => void;
}) {
  return (
    <View style={styles.choice}>
      {options.map((option, i) => (
        <GlassButton
          key={option}
          title={option}
          size="small"
          prominent={i === selected}
          style={styles.flex}
          onPress={() => onSelect(i)}
        />
      ))}
    </View>
  );
}

/** The slider stand-in: the label with its value, and a minus and a plus button. */
export function Stepper({
  label,
  value,
  min,
  max,
  step,
  onChange,
}: {
  label: string;
  value: number;
  min: number;
  max: number;
  step: number;
  onChange: (value: number) => void;
}) {
  const { t } = useTheme();
  // Rounded so repeated steps of 0.05 do not drift.
  const set = (v: number) =>
    onChange(Math.round(Math.min(max, Math.max(min, v)) * 100) / 100);
  return (
    <View style={styles.row}>
      <Text style={[t.body, styles.flex]}>{label}</Text>
      <GlassButton
        icon="minus"
        accessibilityLabel={`Decrease ${label}`}
        size="small"
        disabled={value <= min}
        onPress={() => set(value - step)}
      />
      <GlassButton
        icon="plus"
        accessibilityLabel={`Increase ${label}`}
        size="small"
        disabled={value >= max}
        onPress={() => set(value + step)}
      />
    </View>
  );
}

export function SwitchRow({
  label,
  value,
  onChange,
}: {
  label: string;
  value: boolean;
  onChange: (value: boolean) => void;
}) {
  const { t } = useTheme();
  return (
    <View style={styles.row}>
      <Text style={[t.body, styles.flex]}>{label}</Text>
      <Switch
        value={value}
        onValueChange={onChange}
        // The catalog's switches are iOS green.
        trackColor={{ true: SystemColors.Green }}
        thumbColor={Platform.OS === 'android' ? '#FFFFFF' : undefined}
        accessibilityLabel={label}
      />
    </View>
  );
}

export type MenuItem =
  | { header: string }
  | { divider: true }
  | { label: string; onPress: () => void; checked?: boolean };

/** The menu stand-in: a glass panel of rows that a tap on its item opens and any row closes. */
export function MenuPanel({
  items,
  onClose,
  style,
}: {
  items: MenuItem[];
  onClose: () => void;
  style?: StyleProp<ViewStyle>;
}) {
  const { c, t } = useTheme();
  return (
    <GlassView shape={{ cornerRadius: 22 }} style={[styles.menu, style]}>
      {items.map((item, i) => {
        if ('header' in item) {
          return (
            <Text key={i} style={[t.footnote, styles.menuHeader]}>
              {item.header}
            </Text>
          );
        }
        if ('divider' in item) {
          return (
            <View
              key={i}
              style={[styles.divider, { backgroundColor: c.outlineVariant }]}
            />
          );
        }
        return (
          <Pressable
            key={i}
            accessibilityRole="menuitem"
            style={styles.menuRow}
            onPress={() => {
              item.onPress();
              onClose();
            }}
          >
            <Text style={[t.body, styles.flex]}>{item.label}</Text>
            {item.checked && <Text style={t.body}>✓</Text>}
          </Pressable>
        );
      })}
    </GlassView>
  );
}

/** The catalog's GlassListSection: a header and a footer around rows on rounded glass. */
export function ListSection({
  header,
  footer,
  children,
}: {
  header?: string;
  footer?: string;
  children: ReactNode;
}) {
  const { t } = useTheme();
  return (
    <View style={styles.section}>
      {header && <Text style={[t.sectionHeader, styles.inset]}>{header}</Text>}
      <GlassView shape={{ cornerRadius: 26 }}>{children}</GlassView>
      {footer && <Text style={[t.footnote, styles.inset]}>{footer}</Text>}
    </View>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  measure: {
    position: 'absolute',
    opacity: 0,
    fontWeight: '600',
  },
  choice: { flexDirection: 'row', gap: 6 },
  row: { flexDirection: 'row', alignItems: 'center', gap: 8, minHeight: 34 },
  menu: { width: 250, alignSelf: 'center', paddingVertical: 6 },
  menuHeader: { paddingHorizontal: 16, paddingTop: 8, paddingBottom: 4 },
  menuRow: {
    flexDirection: 'row',
    alignItems: 'center',
    minHeight: 44,
    paddingHorizontal: 16,
  },
  divider: { height: StyleSheet.hairlineWidth, marginVertical: 4 },
  section: { gap: 6 },
  inset: { paddingHorizontal: 16 },
});

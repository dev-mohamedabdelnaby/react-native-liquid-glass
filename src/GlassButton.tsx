import {
  Pressable,
  Text,
  type ColorValue,
  type StyleProp,
  type ViewStyle,
} from 'react-native';
import NativeGlassButton from './GlassButtonNativeComponent';
import { androidIconName } from './icons';
import { resolveGlass, warnOnce } from './resolveGlass';
import { useGlassSettings } from './settings';
import { isGlassSupported } from './support';
import type { Glass } from './types';

export type GlassButtonSize = 'small' | 'regular' | 'large';

export type GlassButtonProps = {
  title?: string;
  /** SF Symbol name; mapped to a Material icon on Android. */
  icon?: string;
  /** Material icon key for Android, overriding the mapped one. */
  androidIcon?: string;
  prominent?: boolean;
  /** Wins over `glass.tint`. */
  tint?: ColorValue;
  size?: GlassButtonSize;
  disabled?: boolean;
  onPress?: () => void;
  glass?: Glass;
  style?: StyleProp<ViewStyle>;
  /** Needed for an icon-only button. */
  accessibilityLabel?: string;
  testID?: string;
};

// The engine's GlassControlSize heights, so both platforms agree. There is no native text
// measurement yet: the button gets a minimum size and the app sets the width through `style`.
const HEIGHTS: Record<GlassButtonSize, number> = {
  small: 34,
  regular: 44,
  large: 52,
};

function resolveSize(size: GlassButtonSize | undefined): GlassButtonSize {
  if (size === undefined) return 'regular';
  if (Object.hasOwn(HEIGHTS, size)) return size;
  warnOnce('size', `size ${String(size)} is not valid, using regular.`);
  return 'regular';
}

export function GlassButton({
  title,
  icon,
  androidIcon,
  prominent = false,
  tint,
  size,
  disabled = false,
  onPress,
  glass,
  style,
  accessibilityLabel,
  testID,
}: GlassButtonProps) {
  const settings = useGlassSettings();
  const s = resolveSize(size);
  const minSize = { minHeight: HEIGHTS[s], minWidth: HEIGHTS[s] };
  if (!isGlassSupported()) {
    warnOnce(
      'unsupported',
      'Liquid Glass is not supported here, GlassButton renders a plain Pressable.'
    );
    return (
      <Pressable
        testID={testID}
        style={[minSize, style]}
        accessibilityRole="button"
        accessibilityLabel={accessibilityLabel ?? title}
        disabled={disabled}
        onPress={onPress}
      >
        <Text>{title}</Text>
      </Pressable>
    );
  }
  return (
    <NativeGlassButton
      testID={testID}
      style={[minSize, style]}
      accessibilityLabel={accessibilityLabel}
      {...resolveGlass(
        { ...glass, tint: tint ?? glass?.tint },
        settings,
        undefined
      )}
      title={title}
      systemImage={icon}
      androidIcon={androidIconName(icon, androidIcon) ?? undefined}
      prominent={prominent}
      size={s}
      disabled={disabled}
      onButtonPress={disabled ? undefined : () => onPress?.()}
    />
  );
}

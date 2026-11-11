import { useMemo } from 'react';
import {
  Platform,
  useColorScheme,
  type ImageSourcePropType,
  type TextStyle,
} from 'react-native';

// The native catalog's colour schemes (Android ui/theme/Theme.kt, iOS system colours).
const light = {
  primary: '#007AFF',
  background: '#F2F2F7',
  surface: '#FFFFFF',
  systemBackground: '#FFFFFF',
  secondarySystemBackground: '#F2F2F7',
  onSurface: '#000000',
  onSurfaceVariant: 'rgba(60,60,67,0.6)',
  outlineVariant: 'rgba(60,60,67,0.2)',
  // onSurfaceVariant at 50%, the list rows' chevron.
  chevron: 'rgba(60,60,67,0.5)',
};

const dark: typeof light = {
  primary: '#0A84FF',
  background: '#000000',
  surface: '#1C1C1E',
  systemBackground: '#000000',
  secondarySystemBackground: '#1C1C1E',
  onSurface: '#FFFFFF',
  onSurfaceVariant: 'rgba(235,235,245,0.6)',
  outlineVariant: 'rgba(235,235,245,0.2)',
  chevron: 'rgba(235,235,245,0.5)',
};

export type Colors = typeof light;

/** Apple system colours, as the catalog's SystemColors. */
export const SystemColors = {
  Blue: '#007AFF',
  Orange: '#FF9500',
  Green: '#34C759',
  Pink: '#FF2D55',
  Purple: '#AF52DE',
  Teal: '#30B0C7',
  Indigo: '#5856D6',
  Cyan: '#32ADE6',
};

/** `#RRGGBB` plus an alpha in 0..1, as `#RRGGBBAA`. */
export const withAlpha = (hex: string, alpha: number) =>
  hex +
  Math.round(alpha * 255)
    .toString(16)
    .padStart(2, '0');

// iOS text styles (Android ui/theme/Type.kt). SF applies its own tracking on iOS, so the
// letter spacing is only set on Android, where the native app sets it on Roboto.
function ios(
  size: number,
  line: number,
  tracking: number,
  fontWeight: TextStyle['fontWeight'] = '400'
): TextStyle {
  return {
    fontSize: size,
    lineHeight: line,
    fontWeight,
    letterSpacing: Platform.OS === 'android' ? tracking : undefined,
  };
}

const type = {
  largeTitle: ios(34, 41, 0.4, '700'),
  title2: ios(22, 28, -0.26),
  title3: ios(20, 25, -0.45),
  headline: ios(17, 22, -0.43, '600'),
  body: ios(17, 22, -0.43),
  subheadline: ios(15, 20, -0.23),
  footnote: ios(13, 18, -0.08),
  titleSmall: ios(15, 20, -0.23, '600'),
};

export function useTheme() {
  const isDark = useColorScheme() === 'dark';
  return useMemo(() => {
    const c = isDark ? dark : light;
    const t = {
      largeTitle: { ...type.largeTitle, color: c.onSurface },
      title2: { ...type.title2, color: c.onSurface },
      title3: { ...type.title3, color: c.onSurface },
      headline: { ...type.headline, color: c.onSurface },
      body: { ...type.body, color: c.onSurface },
      secondary: { ...type.body, color: c.onSurfaceVariant },
      subheadline: { ...type.subheadline, color: c.onSurface },
      footnote: { ...type.footnote, color: c.onSurfaceVariant },
      sectionHeader: { ...type.headline, color: c.onSurfaceVariant },
      demoHeader: { ...type.titleSmall, color: c.onSurfaceVariant },
    };
    return { c, t, dark: isDark };
  }, [isDark]);
}

const onIos = Platform.OS === 'ios';

// SF Symbols on iOS, Material icons on Android, as the two native catalogs draw them.
export const ICONS = {
  drop_fill: onIos
    ? require('../assets/icons/drop_fill.ios.png')
    : require('../assets/icons/drop_fill.android.png'),
  drop: onIos
    ? require('../assets/icons/drop.ios.png')
    : require('../assets/icons/drop.android.png'),
  rectangle_on_rectangle: onIos
    ? require('../assets/icons/rectangle_on_rectangle.ios.png')
    : require('../assets/icons/rectangle_on_rectangle.android.png'),
  hand_tap: onIos
    ? require('../assets/icons/hand_tap.ios.png')
    : require('../assets/icons/hand_tap.android.png'),
  button_horizontal: onIos
    ? require('../assets/icons/button_horizontal.ios.png')
    : require('../assets/icons/button_horizontal.android.png'),
  header: onIos
    ? require('../assets/icons/header.ios.png')
    : require('../assets/icons/header.android.png'),
  dock: onIos
    ? require('../assets/icons/dock.ios.png')
    : require('../assets/icons/dock.android.png'),
  chevron_right: onIos
    ? require('../assets/icons/chevron_right.ios.png')
    : require('../assets/icons/chevron_right.android.png'),
  star_fill: onIos
    ? require('../assets/icons/star_fill.ios.png')
    : require('../assets/icons/star_fill.android.png'),
  star: onIos
    ? require('../assets/icons/star.ios.png')
    : require('../assets/icons/star.android.png'),
  photo: onIos
    ? require('../assets/icons/photo.ios.png')
    : require('../assets/icons/photo.android.png'),
  share: onIos
    ? require('../assets/icons/share.ios.png')
    : require('../assets/icons/share.android.png'),
  more: onIos
    ? require('../assets/icons/more.ios.png')
    : require('../assets/icons/more.android.png'),
  house: onIos
    ? require('../assets/icons/house.ios.png')
    : require('../assets/icons/house.android.png'),
  person: onIos
    ? require('../assets/icons/person.ios.png')
    : require('../assets/icons/person.android.png'),
} satisfies Record<string, ImageSourcePropType>;

export type IconName = keyof typeof ICONS;

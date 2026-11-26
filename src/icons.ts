import { Image, type ImageSourcePropType } from 'react-native';
import type { GlassIcon, GlassSettings } from './types';

/** A glyph icon from a react-native-vector-icons set (or any set with the same two methods). */
export function vectorIcon(
  iconSet: {
    getRawGlyphMap(): Record<string, number>;
    getFontFamily(): string;
  },
  name: string
): GlassIcon | undefined {
  const code = iconSet.getRawGlyphMap()[name];
  if (code === undefined) {
    if (__DEV__) {
      console.warn(
        `vectorIcon: no icon "${name}" in ${iconSet.getFontFamily()}; the native icon stays.`
      );
    }
    return undefined;
  }
  return {
    glyph: String.fromCodePoint(code),
    fontFamily: iconSet.getFontFamily(),
  };
}

/** The native `icons` prop: JSON of name → `{glyph, fontFamily}` | `{svg}` | `{uri}`. */
export function iconsJson(icons: GlassSettings['icons']): string {
  const out: Record<string, object> = {};
  for (const [name, icon] of Object.entries(icons ?? {})) {
    if (icon == null) continue;
    if (typeof icon === 'object' && ('glyph' in icon || 'svg' in icon)) {
      out[name] = icon;
      continue;
    }
    // shortcut: a list of sources uses its first; pick by scale if apps need it.
    const source = Image.resolveAssetSource(
      (Array.isArray(icon) ? icon[0] : icon) as ImageSourcePropType
    );
    if (source?.uri) out[name] = { uri: source.uri };
  }
  return JSON.stringify(out);
}

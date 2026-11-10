import { processColor } from 'react-native';
import { DEFAULT_SETTINGS } from './settings';
import type {
  Glass,
  GlassSettings,
  GlassShape,
  NativeGlassProps,
} from './types';

const warned = new Set<string>();

export function warnOnce(key: string, message: string) {
  if (!__DEV__ || warned.has(key)) return;
  warned.add(key);
  console.warn(`[react-native-liquid-glass] ${message}`);
}

function clamp(
  key: string,
  value: number | undefined,
  max: number,
  fallback: number
) {
  if (typeof value !== 'number' || !Number.isFinite(value)) {
    warnOnce(key, `${key} must be a finite number, using ${fallback}.`);
    return fallback;
  }
  const clamped = Math.min(max, Math.max(0, value));
  if (clamped !== value) {
    warnOnce(key, `${key} ${value} is out of range, using ${clamped}.`);
  }
  return clamped;
}

function resolveTint(tint: Glass['tint']) {
  if (tint == null) return null;
  const color = processColor(tint);
  if (color == null) {
    warnOnce('tint', `tint ${String(tint)} is not a valid color, ignoring it.`);
    return null;
  }
  return color;
}

function resolveStyle(style: Glass['style']) {
  if (style === undefined || style === 'regular' || style === 'clear') {
    return style;
  }
  warnOnce('style', `style ${String(style)} is not valid, ignoring it.`);
  return undefined;
}

function resolveShape(
  shape: GlassShape | undefined
): Pick<NativeGlassProps, 'shapeKind' | 'cornerRadius'> {
  if (shape === 'circle') return { shapeKind: 'circle', cornerRadius: 0 };
  if (typeof shape === 'object' && shape !== null) {
    return {
      shapeKind: 'rect',
      cornerRadius: clamp('cornerRadius', shape.cornerRadius, Infinity, 0),
    };
  }
  if (shape !== undefined && shape !== 'capsule') {
    warnOnce('shape', `shape ${String(shape)} is not valid, using capsule.`);
  }
  return { shapeKind: 'capsule', cornerRadius: 0 };
}

export function resolveGlass(
  own: Glass | undefined,
  settings: GlassSettings,
  shape: GlassShape | undefined
): NativeGlassProps {
  const g = own ?? {};
  const d = DEFAULT_SETTINGS;
  const refraction = g.refraction ?? settings.refraction;
  return {
    glassStyle:
      resolveStyle(g.style) ??
      (settings.clear && g.tint == null ? 'clear' : 'regular'),
    tintColor: resolveTint(g.tint ?? settings.tint),
    interactive: g.interactive ?? false,
    opaque: g.opaque ?? settings.opaque,
    liquid: (g.liquid ?? true) && settings.liquid,
    blur: clamp('blur', g.blur ?? settings.blur, Infinity, d.blur),
    white: clamp('white', g.white ?? settings.white, 1, d.white),
    black: clamp('black', g.black ?? settings.black, 1, d.black),
    edgeLight: clamp(
      'edgeLight',
      g.edgeLight ?? settings.edgeLight,
      1,
      d.edgeLight
    ),
    refractionHeight: clamp(
      'refraction.height',
      refraction.height,
      Infinity,
      d.refraction.height
    ),
    refractionAmount: clamp(
      'refraction.amount',
      refraction.amount,
      Infinity,
      d.refraction.amount
    ),
    ...resolveShape(shape),
  };
}

import { expect, jest, test } from '@jest/globals';
import { processColor } from 'react-native';
import { DEFAULT_SETTINGS } from '../settings';
import { resolveGlass } from '../resolveGlass';

test('defaults', () => {
  expect(resolveGlass(undefined, DEFAULT_SETTINGS, undefined)).toEqual({
    glassStyle: 'clear',
    tintColor: null,
    interactive: false,
    opaque: false,
    liquid: true,
    blur: 3,
    white: 0.25,
    black: 0.05,
    edgeLight: 0.15,
    refractionHeight: 14,
    refractionAmount: 24,
    shapeKind: 'capsule',
    cornerRadius: 0,
  });
});

test('own value beats provider beats default', () => {
  const s = { ...DEFAULT_SETTINGS, blur: 8, white: 0.4 };
  const r = resolveGlass({ blur: 1 }, s, undefined);
  expect(r.blur).toBe(1);
  expect(r.white).toBe(0.4);
  expect(r.black).toBe(0.05);
});

test('a tinted glass stays regular when the setting says clear', () => {
  expect(
    resolveGlass({ tint: 'red' }, DEFAULT_SETTINGS, undefined).glassStyle
  ).toBe('regular');
  expect(
    resolveGlass({ style: 'clear', tint: 'red' }, DEFAULT_SETTINGS, undefined)
      .glassStyle
  ).toBe('clear');
});

test('the general tint applies only to glass without its own', () => {
  const s = { ...DEFAULT_SETTINGS, tint: 'blue' };
  expect(resolveGlass(undefined, s, undefined).tintColor).toBe(
    processColor('blue')
  );
  expect(resolveGlass({ tint: 'red' }, s, undefined).tintColor).toBe(
    processColor('red')
  );
});

test('liquid is off when either the item or the setting turns it off', () => {
  expect(
    resolveGlass({ liquid: false }, DEFAULT_SETTINGS, undefined).liquid
  ).toBe(false);
  expect(
    resolveGlass(undefined, { ...DEFAULT_SETTINGS, liquid: false }, undefined)
      .liquid
  ).toBe(false);
});

test('shapes', () => {
  expect(resolveGlass(undefined, DEFAULT_SETTINGS, 'circle').shapeKind).toBe(
    'circle'
  );
  expect(
    resolveGlass(undefined, DEFAULT_SETTINGS, { cornerRadius: 12 })
  ).toMatchObject({ shapeKind: 'rect', cornerRadius: 12 });
});

test('invalid values fall back with one dev warning', () => {
  const warn = jest.spyOn(console, 'warn').mockImplementation(() => {});
  expect(
    resolveGlass({ tint: 'notacolor' }, DEFAULT_SETTINGS, undefined).tintColor
  ).toBeNull();
  expect(
    resolveGlass(undefined, DEFAULT_SETTINGS, { cornerRadius: -4 }).cornerRadius
  ).toBe(0);
  expect(
    resolveGlass(undefined, DEFAULT_SETTINGS, 'oval' as never).shapeKind
  ).toBe('capsule');
  expect(resolveGlass({ white: 3 }, DEFAULT_SETTINGS, undefined).white).toBe(1);
  expect(warn).toHaveBeenCalledTimes(4);
});

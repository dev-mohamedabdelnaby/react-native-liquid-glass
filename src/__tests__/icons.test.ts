import { expect, test } from '@jest/globals';
import { androidIconName } from '../icons';

test("the spec's nine symbols map", () => {
  expect(androidIconName('checkmark')).toBe('check');
  expect(androidIconName('xmark')).toBe('close');
  expect(androidIconName('plus')).toBe('add');
  expect(androidIconName('minus')).toBe('remove');
  expect(androidIconName('magnifyingglass')).toBe('search');
  expect(androidIconName('heart')).toBe('favorite_border');
  expect(androidIconName('square.and.arrow.up')).toBe('share');
  expect(androidIconName('ellipsis')).toBe('more_horiz');
  expect(androidIconName('trash')).toBe('delete');
});

test('override wins, unknown is null', () => {
  expect(androidIconName('checkmark', 'done')).toBe('done');
  expect(androidIconName('sparkles')).toBeNull();
});

test('a missing symbol or a prototype key is null, not an inherited value', () => {
  expect(androidIconName(undefined)).toBeNull();
  expect(androidIconName('constructor')).toBeNull();
  expect(androidIconName('toString', undefined)).toBeNull();
});

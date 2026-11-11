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

test('the catalog screens symbols map', () => {
  expect(androidIconName('chevron.left')).toBe('arrow_back_ios');
  expect(androidIconName('star')).toBe('star_border');
  expect(androidIconName('star.fill')).toBe('star');
  expect(androidIconName('gearshape')).toBe('settings');
  expect(androidIconName('bell')).toBe('notifications');
  expect(androidIconName('house')).toBe('home');
  expect(androidIconName('person')).toBe('person');
  expect(androidIconName('heart.fill')).toBe('favorite');
  expect(androidIconName('bolt.fill')).toBe('bolt');
  expect(androidIconName('play.fill')).toBe('play_arrow');
  expect(androidIconName('pause.fill')).toBe('pause');
  expect(androidIconName('forward.fill')).toBe('fast_forward');
});

test('override wins, unknown is null', () => {
  expect(androidIconName('checkmark', 'done')).toBe('done');
  expect(androidIconName('sparkles')).toBeNull();
});

test('an empty override is no override', () => {
  expect(androidIconName('checkmark', '')).toBe('check');
  expect(androidIconName('sparkles', '')).toBeNull();
});

test('a missing symbol or a prototype key is null, not an inherited value', () => {
  expect(androidIconName(undefined)).toBeNull();
  expect(androidIconName('constructor')).toBeNull();
  expect(androidIconName('toString', undefined)).toBeNull();
});

import { expect, jest, test } from '@jest/globals';
import { fireEvent, render } from '@testing-library/react-native';
import { useState } from 'react';
import { Image } from 'react-native';
import type { GlassSettings } from '../index';
import { DEFAULT_SETTINGS, GlassSettingsProvider, vectorIcon } from '../index';
import * as Screens from '../screens';
import {
  GLASS_SCREENS,
  GlassButtonsScreen,
  GlassHomeScreen,
  GlassTextScreen,
} from '../screens';

const { refraction, ...rest } = DEFAULT_SETTINGS;
const nativeDefaults = {
  ...rest,
  // The event carries what Home can change: not the font or the icons.
  fontFamily: undefined,
  icons: undefined,
  tint: '',
  dropColor: '',
  refractionHeight: refraction.height,
  refractionAmount: refraction.amount,
};

test('each named screen sends its id', async () => {
  const r = await render(<GlassButtonsScreen testID="s" />);
  expect(r.getByTestId('s').props.screen).toBe('buttons');
  const h = await render(<GlassHomeScreen testID="h" />);
  expect(h.getByTestId('h').props.screen).toBe('home');
});

test('GLASS_SCREENS lists the 21 screens in home order', () => {
  expect(GLASS_SCREENS.map((s) => s.id)).toEqual([
    'glassEffect',
    'glassView',
    'interactive',
    'buttons',
    'container',
    'morphing',
    'union',
    'toolbar',
    'floatingHeader',
    'tabBar',
    'sheets',
    'controls',
    'text',
    'transitions',
    'search',
    'editSearch',
    'scrollEdge',
    'splitView',
    'zoomTransitions',
    'alerts',
    'recipes',
  ]);
  expect(GLASS_SCREENS[0]).toEqual({
    id: 'glassEffect',
    title: 'Glass Effect',
  });
});

test('provider settings reach the native props and update live', async () => {
  const tree = (value: object) => (
    <GlassSettingsProvider value={value}>
      <GlassTextScreen testID="s" />
    </GlassSettingsProvider>
  );
  const r = await render(tree({ tint: 'purple', blur: 5 }));
  expect(r.getByTestId('s').props).toMatchObject({
    tintColor: 'purple',
    blur: 5,
    white: 0.25,
    appearance: 'system',
    refractionHeight: 14,
    refractionAmount: 24,
  });
  await r.rerender(tree({ tint: null }));
  expect(r.getByTestId('s').props.tintColor).toBeNull();
});

test('androidStyle: glass by default, the provider reaches every screen, a prop wins', async () => {
  const all = Object.entries(Screens).filter(([name]) =>
    /Screen$/.test(name)
  ) as [string, typeof GlassTextScreen][];
  expect(all).toHaveLength(22);
  for (const [name, Screen] of all) {
    const plain = await render(<Screen testID={name} />);
    expect(plain.getByTestId(name).props.androidStyle).toBe('glass');
    const r = await render(
      <GlassSettingsProvider value={{ androidStyle: 'material' }}>
        <Screen testID={name} />
        <Screen testID="own" androidStyle="glass" />
      </GlassSettingsProvider>
    );
    expect(r.getByTestId(name).props.androidStyle).toBe('material');
    expect(r.getByTestId('own').props.androidStyle).toBe('glass');
  }
});

test('androidControlShape and androidGlowIntensity: pill and 0.35 by default, the provider reaches every screen, a prop wins', async () => {
  const all = Object.entries(Screens).filter(([name]) =>
    /Screen$/.test(name)
  ) as [string, typeof GlassTextScreen][];
  for (const [name, Screen] of all) {
    const plain = await render(<Screen testID={name} />);
    expect(plain.getByTestId(name).props.androidControlShape).toBe('pill');
    expect(plain.getByTestId(name).props.androidGlowIntensity).toBe(0.35);
    const r = await render(
      <GlassSettingsProvider
        value={{ androidControlShape: 'glow', androidGlowIntensity: 1 }}
      >
        <Screen testID={name} />
        <Screen
          testID="own"
          androidControlShape="labeled"
          androidGlowIntensity={0}
        />
      </GlassSettingsProvider>
    );
    expect(r.getByTestId(name).props.androidControlShape).toBe('glow');
    expect(r.getByTestId(name).props.androidGlowIntensity).toBe(1);
    expect(r.getByTestId('own').props.androidControlShape).toBe('labeled');
    expect(r.getByTestId('own').props.androidGlowIntensity).toBe(0);
  }
});

// The app's loop: Home's event into state, state into the provider, back to Home's native props.
function SettingsLoop() {
  const [settings, setSettings] = useState<GlassSettings>(DEFAULT_SETTINGS);
  return (
    <GlassSettingsProvider value={settings}>
      <GlassHomeScreen testID="h" onSettingsChange={setSettings} />
    </GlassSettingsProvider>
  );
}

test("Home's control shape and glow intensity round-trip to its native props", async () => {
  const r = await render(<SettingsLoop />);
  expect(r.getByTestId('h').props.androidControlShape).toBe('pill');
  expect(r.getByTestId('h').props.androidGlowIntensity).toBe(0.35);
  await fireEvent(r.getByTestId('h'), 'settingsChange', {
    nativeEvent: {
      ...nativeDefaults,
      androidControlShape: 'glow',
      androidGlowIntensity: 0.6,
    },
  });
  expect(r.getByTestId('h').props.androidControlShape).toBe('glow');
  expect(r.getByTestId('h').props.androidGlowIntensity).toBe(0.6);
});

const fonts = (props: object) =>
  Object.fromEntries(
    Object.entries(props).filter(([key]) => /^font[A-Z]/.test(key))
  );

test('fontFamily: none by default, a string fills every weight, a map sets its own', async () => {
  const plain = await render(<GlassTextScreen testID="s" />);
  expect(fonts(plain.getByTestId('s').props)).toEqual({
    fontRegular: '',
    fontMedium: '',
    fontSemibold: '',
    fontBold: '',
    fontHeavy: '',
  });
  const one = await render(
    <GlassSettingsProvider value={{ fontFamily: 'Cairo' }}>
      <GlassTextScreen testID="s" />
    </GlassSettingsProvider>
  );
  expect(fonts(one.getByTestId('s').props)).toEqual({
    fontRegular: 'Cairo',
    fontMedium: 'Cairo',
    fontSemibold: 'Cairo',
    fontBold: 'Cairo',
    fontHeavy: 'Cairo',
  });
  const map = await render(
    <GlassSettingsProvider
      value={{ fontFamily: { regular: 'Cairo-Regular', bold: 'Cairo-Bold' } }}
    >
      <GlassTextScreen testID="s" />
    </GlassSettingsProvider>
  );
  expect(fonts(map.getByTestId('s').props)).toEqual({
    fontRegular: 'Cairo-Regular',
    fontMedium: '',
    fontSemibold: '',
    fontBold: 'Cairo-Bold',
    fontHeavy: '',
  });
  expect(map.getByTestId('s').props).not.toHaveProperty('fontFamily');
});

test("fontFamily: a screen's own prop wins over the provider", async () => {
  const all = Object.entries(Screens).filter(([name]) =>
    /Screen$/.test(name)
  ) as [string, typeof GlassTextScreen][];
  for (const [name, Screen] of all) {
    const r = await render(
      <GlassSettingsProvider value={{ fontFamily: 'Cairo' }}>
        <Screen testID={name} />
        <Screen testID="own" fontFamily={{ bold: 'Inter-Bold' }} />
      </GlassSettingsProvider>
    );
    expect(r.getByTestId(name).props.fontBold).toBe('Cairo');
    expect(r.getByTestId('own').props.fontBold).toBe('Inter-Bold');
    expect(r.getByTestId('own').props.fontRegular).toBe('');
  }
});

test('onSettingsChange from Home keeps the current fontFamily', async () => {
  const onSettingsChange = jest.fn();
  const fontFamily = { regular: 'Cairo-Regular', bold: 'Cairo-Bold' };
  const r = await render(
    <GlassSettingsProvider value={{ fontFamily }}>
      <GlassHomeScreen testID="h" onSettingsChange={onSettingsChange} />
    </GlassSettingsProvider>
  );
  await fireEvent(r.getByTestId('h'), 'settingsChange', {
    nativeEvent: { ...nativeDefaults, blur: 7 },
  });
  expect(onSettingsChange).toHaveBeenCalledWith(
    expect.objectContaining({ blur: 7, fontFamily })
  );
});

test('events reach the callbacks', async () => {
  const onBack = jest.fn();
  const onOpen = jest.fn();
  const onSettingsChange = jest.fn();
  const r = await render(
    <GlassHomeScreen
      testID="h"
      onBack={onBack}
      onOpen={onOpen}
      onSettingsChange={onSettingsChange}
    />
  );
  const home = r.getByTestId('h');
  await fireEvent(home, 'back');
  await fireEvent(home, 'open', { nativeEvent: { screen: 'controls' } });
  await fireEvent(home, 'settingsChange', {
    nativeEvent: { ...nativeDefaults, tint: '#AF52DEFF', blur: 7 },
  });
  expect(onBack).toHaveBeenCalledTimes(1);
  expect(onOpen).toHaveBeenCalledWith('controls');
  expect(onSettingsChange).toHaveBeenCalledTimes(1);
  expect(onSettingsChange).toHaveBeenCalledWith(
    expect.objectContaining({
      tint: '#AF52DEFF',
      dropColor: null,
      blur: 7,
      refraction: { height: 14, amount: 24 },
    })
  );
  expect(onSettingsChange.mock.calls[0]?.[0]).not.toHaveProperty(
    'refractionHeight'
  );
});

const Ionicons = {
  getRawGlyphMap: () => ({ heart: 0xf443 }),
  getFontFamily: () => 'Ionicons',
};

test('vectorIcon: a glyph from the set, undefined with a dev warning for an unknown name', () => {
  expect(vectorIcon(Ionicons, 'heart')).toEqual({
    glyph: '\uf443',
    fontFamily: 'Ionicons',
  });
  const warn = jest.spyOn(console, 'warn').mockImplementation(() => {});
  expect(vectorIcon(Ionicons, 'nope')).toBeUndefined();
  expect(warn).toHaveBeenCalledWith(expect.stringContaining('nope'));
  warn.mockRestore();
});

const icons = (props: Record<string, any>) => JSON.parse(props.icons);

test('icons: none by default; glyph and svg pass through, images become a uri', async () => {
  const plain = await render(<GlassTextScreen testID="s" />);
  expect(icons(plain.getByTestId('s').props)).toEqual({});
  // Jest's asset transform gives `require` a stand-in source; the app's Image resolves it to a uri.
  const png = require('./fixtures/share.png');
  const resolve = jest
    .spyOn(Image, 'resolveAssetSource')
    .mockImplementation((source) =>
      source === png
        ? { uri: 'file:///assets/share.png', width: 4, height: 4, scale: 1 }
        : (source as {
            uri: string;
            width: number;
            height: number;
            scale: number;
          })
    );
  const r = await render(
    <GlassSettingsProvider
      value={{
        icons: {
          'heart': vectorIcon(Ionicons, 'heart'),
          'magnifyingglass': { svg: '<svg/>' },
          'square.and.arrow.up': png,
          'plus': { uri: 'https://example.com/plus.png' },
          'trash': undefined,
        },
      }}
    >
      <GlassTextScreen testID="s" />
    </GlassSettingsProvider>
  );
  const sent = icons(r.getByTestId('s').props);
  expect(sent).toEqual({
    'heart': { glyph: '\uf443', fontFamily: 'Ionicons' },
    'magnifyingglass': { svg: '<svg/>' },
    'square.and.arrow.up': { uri: 'file:///assets/share.png' },
    'plus': { uri: 'https://example.com/plus.png' },
  });
  resolve.mockRestore();
});

test("icons: a screen's own map merges over the provider's, key by key", async () => {
  const all = Object.entries(Screens).filter(([name]) =>
    /Screen$/.test(name)
  ) as [string, typeof GlassTextScreen][];
  for (const [name, Screen] of all) {
    const r = await render(
      <GlassSettingsProvider
        value={{ icons: { heart: { svg: 'a' }, plus: { svg: 'b' } } }}
      >
        <Screen testID={name} />
        <Screen testID="own" icons={{ plus: { svg: 'c' } }} />
      </GlassSettingsProvider>
    );
    expect(icons(r.getByTestId(name).props)).toEqual({
      heart: { svg: 'a' },
      plus: { svg: 'b' },
    });
    expect(icons(r.getByTestId('own').props)).toEqual({
      heart: { svg: 'a' },
      plus: { svg: 'c' },
    });
  }
});

test('onSettingsChange from Home keeps the current icons', async () => {
  const onSettingsChange = jest.fn();
  const providerIcons = { heart: { svg: '<svg/>' } };
  const r = await render(
    <GlassSettingsProvider value={{ icons: providerIcons }}>
      <GlassHomeScreen testID="h" onSettingsChange={onSettingsChange} />
    </GlassSettingsProvider>
  );
  await fireEvent(r.getByTestId('h'), 'settingsChange', {
    nativeEvent: { ...nativeDefaults, blur: 7 },
  });
  expect(onSettingsChange).toHaveBeenCalledWith(
    expect.objectContaining({ blur: 7, icons: providerIcons })
  );
});

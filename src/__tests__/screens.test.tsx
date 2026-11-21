import { expect, jest, test } from '@jest/globals';
import { fireEvent, render } from '@testing-library/react-native';
import { DEFAULT_SETTINGS, GlassSettingsProvider } from '../index';
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
  // The event carries what Home can change: not the font.
  fontFamily: undefined,
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

test('androidSegmentedCheck: true by default, the provider reaches every screen, a prop wins', async () => {
  const all = Object.entries(Screens).filter(([name]) =>
    /Screen$/.test(name)
  ) as [string, typeof GlassTextScreen][];
  for (const [name, Screen] of all) {
    const plain = await render(<Screen testID={name} />);
    expect(plain.getByTestId(name).props.androidSegmentedCheck).toBe(true);
    const r = await render(
      <GlassSettingsProvider value={{ androidSegmentedCheck: false }}>
        <Screen testID={name} />
        <Screen testID="own" androidSegmentedCheck />
      </GlassSettingsProvider>
    );
    expect(r.getByTestId(name).props.androidSegmentedCheck).toBe(false);
    expect(r.getByTestId('own').props.androidSegmentedCheck).toBe(true);
  }
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

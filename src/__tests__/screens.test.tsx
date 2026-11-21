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

import { expect, test } from '@jest/globals';
import { Text } from 'react-native';
import { render } from '@testing-library/react-native';
import { GlassSettingsProvider, useGlassSettings } from '../settings';

const Probe = () => (
  <Text testID="s">{JSON.stringify(useGlassSettings())}</Text>
);
const tree = (blur: number) => (
  <GlassSettingsProvider value={{ blur }}>
    <GlassSettingsProvider value={{ white: 0.5 }}>
      <Probe />
    </GlassSettingsProvider>
  </GlassSettingsProvider>
);

test('nested providers merge over the parent and re-render live', async () => {
  const r = await render(tree(8));
  const read = () => JSON.parse(r.getByTestId('s').props.children);
  expect(read()).toMatchObject({ blur: 8, white: 0.5, black: 0.05 });
  await r.rerender(tree(2));
  expect(read().blur).toBe(2);
});

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

test('undefined provider entries follow the parent', async () => {
  const r = await render(
    <GlassSettingsProvider value={{ blur: 8 }}>
      <GlassSettingsProvider value={{ blur: undefined, liquid: undefined }}>
        <Probe />
      </GlassSettingsProvider>
    </GlassSettingsProvider>
  );
  expect(JSON.parse(r.getByTestId('s').props.children)).toMatchObject({
    blur: 8,
    liquid: true,
  });
});

import { beforeEach, expect, jest, test } from '@jest/globals';
import { fireEvent, render } from '@testing-library/react-native';
import { GlassButton } from '../GlassButton';
import { isGlassSupported } from '../support';

jest.mock('../support', () => ({ isGlassSupported: jest.fn() }));
const supported = isGlassSupported as jest.Mock<typeof isGlassSupported>;

beforeEach(() => {
  supported.mockReturnValue(true);
  jest.spyOn(console, 'warn').mockImplementation(() => {});
});

test('passes the mapped Android icon and the resolved glass', async () => {
  const r = await render(
    <GlassButton testID="b" icon="checkmark" title="Done" />
  );
  expect(r.getByTestId('b').props).toMatchObject({
    systemImage: 'checkmark',
    androidIcon: 'check',
    glassStyle: 'clear',
    size: 'regular',
    prominent: false,
    title: 'Done',
  });
});

test('onPress fires, disabled blocks it', async () => {
  const onPress = jest.fn();
  const r = await render(
    <GlassButton testID="b" title="Go" onPress={onPress} />
  );
  await fireEvent(r.getByTestId('b'), 'buttonPress');
  expect(onPress).toHaveBeenCalledTimes(1);
  await r.rerender(
    <GlassButton testID="b" title="Go" onPress={onPress} disabled />
  );
  await fireEvent(r.getByTestId('b'), 'buttonPress');
  expect(onPress).toHaveBeenCalledTimes(1);
  expect(r.getByTestId('b').props.disabled).toBe(true);
});

test('own tint wins over glass.tint and reaches native raw', async () => {
  const r = await render(
    <GlassButton testID="b" tint="red" glass={{ tint: 'blue' }} prominent />
  );
  expect(r.getByTestId('b').props).toMatchObject({
    tintColor: 'red',
    glassStyle: 'regular',
    prominent: true,
  });
});

test('size sets the minimum height and width, invalid size falls back', async () => {
  const r = await render(<GlassButton testID="b" size="small" />);
  expect(r.getByTestId('b')).toHaveStyle({ minHeight: 34, minWidth: 34 });
  await r.rerender(<GlassButton testID="b" size={'huge' as 'large'} />);
  expect(r.getByTestId('b').props.size).toBe('regular');
  expect(r.getByTestId('b')).toHaveStyle({ minHeight: 44 });
  expect(console.warn).toHaveBeenCalledTimes(1);
});

test('the accessibility label reaches native', async () => {
  const r = await render(
    <GlassButton testID="b" icon="xmark" accessibilityLabel="Close" />
  );
  expect(r.getByTestId('b').props.accessibilityLabel).toBe('Close');
});

test('falls back to a plain pressable where glass is unsupported', async () => {
  supported.mockReturnValue(false);
  const onPress = jest.fn();
  const r = await render(<GlassButton title="Go" onPress={onPress} />);
  await fireEvent.press(r.getByRole('button', { name: 'Go' }));
  expect(onPress).toHaveBeenCalledTimes(1);
});

import { beforeEach, expect, jest, test } from '@jest/globals';
import { render } from '@testing-library/react-native';
import { GlassView } from '../GlassView';
import { isGlassSupported } from '../support';

jest.mock('../support', () => ({ isGlassSupported: jest.fn() }));
const supported = isGlassSupported as jest.Mock<typeof isGlassSupported>;

beforeEach(() => {
  jest.spyOn(console, 'warn').mockImplementation(() => {});
});

test('renders the native view where glass is supported', async () => {
  supported.mockReturnValue(true);
  const r = await render(<GlassView testID="g" glass={{ tint: 'red' }} />);
  const props = r.getByTestId('g').props;
  expect(props.glassStyle).toBe('regular');
  expect(props.shapeKind).toBe('capsule');
  expect(props.tintColor).toBeDefined();
});

test('falls back to a plain View where glass is unsupported', async () => {
  supported.mockReturnValue(false);
  const r = await render(<GlassView testID="g" />);
  expect(r.getByTestId('g').props.glassStyle).toBeUndefined();
  expect(console.warn).toHaveBeenCalledTimes(1);
});

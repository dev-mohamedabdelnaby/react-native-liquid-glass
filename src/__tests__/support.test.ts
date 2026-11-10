import { expect, jest, test } from '@jest/globals';

const withPlatform = (OS: string, Version: string | number) => {
  jest.resetModules();
  jest.doMock('react-native', () => ({ Platform: { OS, Version } }));
  return require('../support').isGlassSupported as () => boolean;
};

test('iOS 26 and later are supported, earlier are not', () => {
  expect(withPlatform('ios', '26.0')()).toBe(true);
  expect(withPlatform('ios', '18.4')()).toBe(false);
  expect(withPlatform('android', 26)()).toBe(true);
});

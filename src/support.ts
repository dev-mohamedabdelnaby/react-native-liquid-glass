import { Platform } from 'react-native';

// Real Liquid Glass ships with iOS 26; Android always gets the Compose engine.
export function isGlassSupported(): boolean {
  if (Platform.OS === 'ios') {
    return parseInt(String(Platform.Version), 10) >= 26;
  }
  return true;
}

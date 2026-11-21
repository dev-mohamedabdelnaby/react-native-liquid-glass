import type { GlassSettings } from '@native-dev-kit/react-native-liquid-glass';

// true: every screen takes the settings below (the sample font, Cairo); false: the native look.
export const CUSTOMIZE = false;

// Cairo (SIL OFL 1.1, see android/app/src/main/assets/fonts/OFL.txt), added the React Native way:
// Android assets/fonts, iOS UIAppFonts. Each name is the font's file name and PostScript name.
export const CUSTOM_SETTINGS: GlassSettings = {
  fontFamily: {
    regular: 'Cairo-Regular',
    medium: 'Cairo-Medium',
    semibold: 'Cairo-SemiBold',
    bold: 'Cairo-Bold',
    heavy: 'Cairo-ExtraBold',
  },
};

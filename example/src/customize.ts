import type { GlassSettings } from '@native-dev-kit/react-native-liquid-glass';
import { vectorIcon } from '@native-dev-kit/react-native-liquid-glass';
import Ionicons from 'react-native-vector-icons/Ionicons';

// true: every screen takes the settings below (the sample font, Cairo, and the sample icons); false: the native look.
export const CUSTOMIZE = false;

// A four-pointed star; drawn as a template, so its own colour does not matter.
const STAR =
  '<svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path d="M12 1 L14.5 9.5 L23 12 L14.5 14.5 L12 23 L9.5 14.5 L1 12 L9.5 9.5 Z"/></svg>';
// A back chevron that points left: it mirrors in RTL where the native one does.
const BACK =
  '<svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path d="M16 2 L5 12 L16 22 L19 19 L11 12 L19 5 Z"/></svg>';
// A target (ring and dot), as a PNG.
const TARGET = require('./assets/target.png');

// Cairo (SIL OFL 1.1, see android/app/src/main/assets/fonts/OFL.txt), added the React Native way:
// Android assets/fonts, iOS UIAppFonts. Each name is the font's file name and PostScript name.
// Icons: keyed by the SF Symbol each screen shows there; Ionicons is added the react-native-vector-icons
// way (android/app/build.gradle, iOS UIAppFonts).
export const CUSTOM_SETTINGS: GlassSettings = {
  fontFamily: {
    regular: 'Cairo-Regular',
    medium: 'Cairo-Medium',
    semibold: 'Cairo-SemiBold',
    bold: 'Cairo-Bold',
    heavy: 'Cairo-ExtraBold',
  },
  icons: {
    // Home rows, Buttons, Toolbar, Tab Bar, menus: an icon font glyph, an SVG and a PNG.
    'drop.fill': vectorIcon(Ionicons, 'water'),
    'heart': vectorIcon(Ionicons, 'heart-outline'),
    'heart.fill': vectorIcon(Ionicons, 'heart'),
    'house': vectorIcon(Ionicons, 'home'),
    'play.fill': vectorIcon(Ionicons, 'play'),
    'checkmark.circle': vectorIcon(Ionicons, 'checkmark-circle-outline'),
    'sparkles': { svg: STAR },
    'bolt.fill': { svg: STAR },
    'wand.and.stars': { svg: STAR },
    'gearshape': { svg: STAR },
    'chevron.backward': { svg: BACK },
    'slider.horizontal.3': TARGET,
    'forward.fill': TARGET,
    'ellipsis': TARGET,
    'books.vertical': TARGET,
    'arrow.up.arrow.down': TARGET,
  },
};

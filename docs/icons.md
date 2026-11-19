# Icons (coming in the next version)

> **Not in the package yet.** This page describes the planned API. The code below does not compile with today's version.

```ts
icons?: Record<string, GlassIcon>; // key: the SF Symbol name the screens use for that icon

type GlassIcon =
  | { glyph: string; fontFamily: string } // icon fonts, for example react-native-vector-icons
  | { svg: string }                       // SVG markup
  | ImageSourcePropType;                  // require('./icon.png'), { uri }

function vectorIcon(
  iconSet: { getRawGlyphMap(): Record<string, number>; getFontFamily(): string },
  name: string
): GlassIcon;
```

- Set it in `GlassSettings` (the provider sets it for every screen) or as a prop on one screen.
- An icon that is not in the map stays native.
- **Keys are SF Symbol names on both platforms**, for example `'square.and.arrow.up'` or `'magnifyingglass'`. The library maps each Material icon the Android screens use to the SF Symbol the iOS screen uses in the same place, so one map works on both.
- Your icon takes the original icon's place, size and tint (it is drawn as a template): in buttons, toolbars, tab bars, menus, lists and controls.

## Sources

- **Icon fonts** (react-native-vector-icons and similar): use `vectorIcon(IconSet, 'name')`. The library does not depend on react-native-vector-icons; you pass the icon set.
- **SVG:** `{ svg: '<svg …>…</svg>' }`. Drawn natively (AndroidSVG on Android, SwiftDraw on iOS). The library does not depend on react-native-svg.
- **Images:** `require('./share.png')` or `{ uri: 'https://…' }`.

```tsx
// Planned API: not in the current package.
import Ionicons from 'react-native-vector-icons/Ionicons';
import { vectorIcon } from '@native-dev-kit/react-native-liquid-glass';

const icons = {
  'square.and.arrow.up': vectorIcon(Ionicons, 'share-outline'),
  'magnifyingglass': { svg: '<svg viewBox="0 0 24 24">…</svg>' },
  'heart': require('./assets/heart.png'),
};

<GlassSettingsProvider value={{ icons }}>…</GlassSettingsProvider>
```

Define the map outside the component or memoise it.

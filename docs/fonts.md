# Fonts

```ts
fontFamily?: string | {
  regular?: string;
  medium?: string;
  semibold?: string;
  bold?: string;
  heavy?: string;
};
```

- Set it in `GlassSettings` (the provider sets it for every screen) or as the `fontFamily` prop of one screen, which wins over the provider's.
- **A weight map** names the font for each weight, the way an app's own `AppText` component maps weights to font files. A weight you leave out uses `regular`.
- **A string** is one font family for every weight, as React Native's `fontFamily` with a `fontWeight`: iOS picks the family's member for each weight; Android loads it through React Native's font manager with the weight (a family registered with `ReactFontManager.addCustomFont`, or `assets/fonts` files with React Native's `_bold` naming). If you have one file per weight, use the map.
- `''` (the default) is the system font. With no font set, the screens look exactly as before.
- Every text the screens draw uses it and keeps its own size, weight and Dynamic Type or font scaling: titles, large and inline navigation titles, list rows, section headers and footers, buttons, segmented controls, tab bar items, search fields, the glass lettering, and on Android menus, dialogs, sheets and the Material type scale (both [Android styles](android-style.md)).

## Adding the fonts

Add the font files to your app the usual React Native way, and use the same names you use for React Native's `fontFamily`:

- **Android:** put the files in `android/app/src/main/assets/fonts/` (the file name without `.ttf` is the name), or register a font family with `ReactFontManager.getInstance().addCustomFont(…)`. The library loads them through React Native's font manager, so the names your `AppText` uses work unchanged.
- **iOS:** add the files to the app target and list them under `UIAppFonts` in `Info.plist`. The name is the font's PostScript name (for a family string, its family name).

Naming each file after its PostScript name (`Inter-SemiBold.ttf`) gives the same name on both platforms.

```tsx
import type { ReactNode } from 'react';
import {
  GlassSettingsProvider,
  type GlassSettings,
} from '@native-dev-kit/react-native-liquid-glass';

const settings: GlassSettings = {
  fontFamily: {
    regular: 'Inter-Regular',
    medium: 'Inter-Medium',
    semibold: 'Inter-SemiBold',
    bold: 'Inter-Bold',
    heavy: 'Inter-Black',
  },
};

export function Glass({ children }: { children: ReactNode }) {
  return (
    <GlassSettingsProvider value={settings}>{children}</GlassSettingsProvider>
  );
}
```

Define the settings outside the component (or memoise them), as for every provider value. Home's `onSettingsChange` keeps the provider's `fontFamily`, so feeding it back into the provider keeps the font.

The example app shows it with Cairo (SIL Open Font License): set `CUSTOMIZE = true` in `example/src/customize.ts`.

## Text that keeps the system font

The system draws this text itself and does not let apps restyle it:

- **iOS:** context menus and the menus of menu buttons and pickers, alerts, action sheets (confirmation dialogs), the share sheet, the date, time and colour pickers, badge numbers, and the keyboard.
- **Android:** the share sheet and the keyboard.

SF Symbols and Material icons stay as they are: they are icons, not text.

## Bars on iOS

Navigation titles, bar buttons, tab bars, segmented controls and search fields are UIKit views. The library styles them with appearance proxies scoped to its own screens, so the rest of your app is not touched. The proxies are per class: when glass screens with different fonts are mounted at the same time, their bars take the last font set.

## Why not a component?

The screens are native and draw their own text, so a React Native component such as your `AppText` cannot be rendered inside them. The weight map is how your fonts carry over.

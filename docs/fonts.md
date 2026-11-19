# Fonts (coming in the next version)

> **Not in the package yet.** This page describes the planned API. The code below does not compile with today's version.

```ts
fontFamily?: string | {
  regular?: string;
  medium?: string;
  semibold?: string;
  bold?: string;
  heavy?: string;
};
```

- Set it in `GlassSettings` (the provider sets it for every screen) or as a prop on one screen.
- A string is one font family for every weight. The object maps each weight to a font name, the way an app's own `AppText` component maps weights to font files.
- Every text the screens draw uses it and keeps its own size, weight and Dynamic Type or font scaling: titles, navigation and tab bar titles, buttons, segmented controls, search fields, and menus on Android.

## Adding the fonts

Add the font files to your app the usual React Native way, and use the same names you use for React Native's `fontFamily`:

- **Android:** put the files in `android/app/src/main/assets/fonts/`. The library looks them up through React Native's font manager, so the names your `AppText` uses work unchanged.
- **iOS:** add the files to the app target and list them under `UIAppFonts` in `Info.plist`.

```tsx
// Planned API: not in the current package.
const fonts = {
  regular: 'Inter-Regular',
  medium: 'Inter-Medium',
  semibold: 'Inter-SemiBold',
  bold: 'Inter-Bold',
  heavy: 'Inter-Black',
};

<GlassSettingsProvider value={{ fontFamily: fonts }}>…</GlassSettingsProvider>
```

Memoise that object, or define it outside the component, as for every provider value.

## Text that keeps the system font

On iOS, the system draws some text itself and does not let apps restyle it. That text keeps the system font: context menus, alerts, action sheets (confirmation dialogs) and the share sheet.

## Why not a component?

The screens are native and draw their own text, so a React Native component such as your `AppText` cannot be rendered inside them. The weight map is how your fonts carry over.

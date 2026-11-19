# Android style (coming in the next version)

> **Not in the package yet.** This page describes the planned API so you can plan for it. The code below does not compile with today's version.

```ts
androidStyle?: 'glass' | 'material'; // default 'glass'; iOS ignores it
```

- Set it in `GlassSettings`, so the provider applies it to every screen, or as a prop on one screen. The prop wins over the provider.
- `'glass'` (default): the Liquid Glass screens you get today.
- `'material'`: the same 21 screens with the same content, drawn with Material 3: a top app bar with the Android back arrow, Material buttons, switches, sliders, segmented buttons, navigation bar, bottom sheets, dialogs, menus and search bar, Android typography and ripples. No glass and no liquid drops.
- Screens about glass itself (Glass Effect, Glass View, Union, Morphing, Glass Container) show Material surfaces in its place.
- Custom [fonts](fonts.md) and [icons](icons.md) will apply to both styles.

```tsx
// Planned API: not in the current package.
<GlassSettingsProvider value={{ androidStyle: 'material' }}>
  <GlassHomeScreen style={{ flex: 1 }} />
</GlassSettingsProvider>

<GlassButtonsScreen androidStyle="glass" style={{ flex: 1 }} />
```

Use `Platform.OS` if you want Material only on some devices; iOS always shows glass.

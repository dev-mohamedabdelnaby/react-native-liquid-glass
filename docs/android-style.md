# Android style

```ts
androidStyle?: 'glass' | 'material'; // default 'glass'; iOS ignores it
```

- Set it in `GlassSettings`, so the provider applies it to every screen, or as a prop on one screen. The prop wins over the provider.
- `'glass'` (default): the Liquid Glass screens.
- `'material'`: the same 21 screens and Home, with the same content, drawn with Android's own Material 3 components. No glass and no liquid drops.
- Android Home has a Glass / Material picker. It sends `onSettingsChange` with the new `androidStyle`, like its other controls.

```tsx
import { useMemo } from 'react';
import {
  GlassButtonsScreen, GlassHomeScreen, GlassSettingsProvider,
} from '@native-dev-kit/react-native-liquid-glass';

export function MaterialHome() {
  const value = useMemo(() => ({ androidStyle: 'material' as const }), []);
  return (
    <GlassSettingsProvider value={value}>
      <GlassHomeScreen style={{ flex: 1 }} />
    </GlassSettingsProvider>
  );
}

// One screen in glass whatever the provider says.
export function GlassButtons() {
  return <GlassButtonsScreen androidStyle="glass" style={{ flex: 1 }} />;
}
```

Use `Platform.OS` if you want Material only on some devices; iOS always shows glass.

In Material, the switch, slider, segmented control and bottom tab bar are pill-shaped by default, or labeled or glowing: see [control-shapes.md](control-shapes.md) (`androidControlShape`). Glass keeps its liquid controls.

## What changes

| Glass | Material 3 |
|---|---|
| Glass bar with a glass back chevron | `TopAppBar` with the Android back arrow (✕ on full-screen demos); bar actions are plain icon buttons; it takes a tonal color once content scrolls under it |
| Home's large title | `LargeTopAppBar` that collapses as the list scrolls |
| A large title in the page that collapses into the bar (Toolbar, the Tab Bar demos) | The top app bar's title only |
| Grouped inset list with dividers, gray section headers, row chevrons | Android settings groups: a `titleSmall` header in the primary color, each row its own tonal segment (2 dp gaps, the group's ends rounded), no dividers, no chevrons |
| Prominent / glass / clear / frosted button | `Button` (in the prominent button's tint) / `FilledTonalButton` / `OutlinedButton` / `TextButton` |
| Floating glass compose / add button (Toolbar, Recipes) | `FloatingActionButton` |
| Glass icon button | `FilledTonalIconButton`; `IconButton` inside a bar |
| Liquid switch | The [control shape](control-shapes.md)'s switch (pill by default) |
| Liquid slider | The control shape's slider, with a light haptic each tenth of the way (not continuously) |
| Segmented control (and the Recipes selector) | The control shape's segmented control |
| Scroll Edge's filters and edge styles | `FilterChip` rows |
| Bottom tab bar | The control shape's tab bar with badges, floating above the navigation bar (content scrolls under it). It does not minimize on scroll, so the Tab Bar screen hides its "Minimize on scroll" switch |
| Glass bottom bar | `BottomAppBar` |
| Menu, context menu | `DropdownMenu` (a context menu opens at the long-pressed item, with no lifted preview) |
| Popover | `DropdownMenu` holding the same content |
| Confirmation popover (Alerts) | `AlertDialog` |
| Alert | `AlertDialog` with text buttons in a row, the confirming one at the end |
| Sheet | `ModalBottomSheet` with its drag handle |
| Search in the bottom toolbar (Search, Edit & Search) | A search bar at the top, under the top app bar; on Edit & Search it opens from the bar's search action, and editing turns the bar into a contextual one (Delete, Done). The iOS "Minimize search field" switch is hidden |
| Stepper (and the custom "Quick pick" stepper) | − and + tonal icon buttons |
| Any other glass surface (cards, pills, the glass demos) | A tonal surface (`surfaceContainerHigh`), or solid in the glass's own tint |
| iOS gray fills and page colors | `surface`, `surfaceContainerHigh`, `surfaceContainerHighest` |
| iOS-style symbols in bars and pickers (`···`, the share box, `‹ ›`, the up-down picker arrows) | `MoreVert`, `Share`, `ArrowBack` / `ArrowForward`, `ArrowDropDown` |

- Colors are the device's dynamic colors (Android 12+), else Material's baseline. `tint` becomes the scheme's primary roles: in light mode it is darkened until white reads on it, in dark mode lifted to a light tone with a dark on-color; the containers, on-colors and inverse primary are lighter or darker tones of it. It is a blend toward white or black, not a full tonal palette; secondary, tertiary and the surfaces stay the device's. Text uses Material's type scale, also for the screens' own texts. Presses show ripples.
- Android Home shows only the settings Material uses: Appearance, Layout direction, Android style, the control shape (and glow intensity) and `tint`.
- A slider's own tint wins over the primary. `dropColor` does nothing in `'material'`.
- TalkBack: a switch row reads its label with the switch's state; a slider reads its label and value ("Blur, 3", "White, 25%").
- Touch targets are at least 48 dp. Material components animate as their own; the control shapes slide with a spring, and show ripples and keyboard focus.
- Screens about glass itself (Glass Effect, Glass View, Union, Morphing, Glass Container) show Material surfaces in its place, on the plain page instead of the photo.
- The other Android glass settings (`clear`, `blur`, `liquid` and the rest) do nothing in `'material'`.
- `appearance`, `layoutDirection` and the back key work as in glass: back closes an open sheet, dialog, menu or search first.
- Custom [fonts](fonts.md) apply to both styles (glass texts and the Material type scale); custom [icons](icons.md) will too.

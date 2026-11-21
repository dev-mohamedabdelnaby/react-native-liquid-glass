# Android style

```ts
androidStyle?: 'glass' | 'material'; // default 'glass'; iOS ignores it
```

- Set it in `GlassSettings`, so the provider applies it to every screen, or as a prop on one screen. The prop wins over the provider.
- `'glass'` (default): the Liquid Glass screens.
- `'material'`: the same 21 screens and Home, with the same content, drawn with Android's own Material 3 components. No glass and no liquid drops.
- Android Home has a Glass / Material picker. It sends `onSettingsChange` with the new `androidStyle`, like its other controls.

```ts
androidSegmentedCheck?: boolean; // default true; Material only
```

- The check mark on the selected segment of Material segmented buttons. `false` hides it.
- In `GlassSettings` and as a screen prop (the prop wins), like `androidStyle`. In Material, Android Home shows a "Check on the selected segment" switch for it.

```tsx
import { useMemo } from 'react';
import {
  GlassButtonsScreen, GlassControlsScreen, GlassHomeScreen, GlassSettingsProvider,
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

// Material segmented buttons without the check mark on this screen.
export function NoCheckControls() {
  return <GlassControlsScreen androidStyle="material" androidSegmentedCheck={false} style={{ flex: 1 }} />;
}
```

Use `Platform.OS` if you want Material only on some devices; iOS always shows glass.

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
| Liquid switch | `Switch` with Material 3 Expressive's check / ✕ on the thumb |
| Liquid slider | `Slider` with the Expressive look: a 24 dp track, a 44 dp handle that narrows while pressed, a value indicator above it while held, and a light haptic each tenth of the way (not continuously) |
| Segmented control | `SingleChoiceSegmentedButtonRow` (check mark per `androidSegmentedCheck`) |
| Bottom tab bar | `NavigationBar` with badges |
| Glass bottom bar | `BottomAppBar` |
| Menu, context menu | `DropdownMenu` (a context menu opens at the long-pressed item, with no lifted preview) |
| Popover | `DropdownMenu` holding the same content |
| Confirmation popover (Alerts) | `AlertDialog` |
| Alert | `AlertDialog` with text buttons in a row, the confirming one at the end |
| Sheet | `ModalBottomSheet` with its drag handle |
| Expanding search | `DockedSearchBar` |
| Stepper | − and + tonal icon buttons |
| Any other glass surface (cards, pills, the glass demos) | A tonal surface (`surfaceContainerHigh`), or solid in the glass's own tint |
| iOS gray fills and page colors | `surface`, `surfaceContainerHigh`, `surfaceContainerHighest` |
| iOS-style symbols in bars and pickers (`···`, the share box, `‹ ›`, the up-down picker arrows) | `MoreVert`, `Share`, `ArrowBack` / `ArrowForward`, `ArrowDropDown` |

- Colors are the device's dynamic colors (Android 12+), else Material's baseline. `tint` becomes the scheme's primary, and tints the primary and secondary containers, with on-colors picked for contrast. Text uses Material's type scale. Presses show ripples.
- Sliders take `dropColor` for their handle; a slider's own tint wins over the primary.
- TalkBack: a switch row reads its label with the switch's state; a slider reads its label and value ("Blur, 3", "White, 25%").
- Touch targets are at least 48 dp. With "Remove animations" on, the slider's value indicator shows and hides without animating; the Material components' own animations are Compose's.
- Screens about glass itself (Glass Effect, Glass View, Union, Morphing, Glass Container) show Material surfaces in its place, on the plain page instead of the photo.
- The other Android glass settings (`clear`, `blur`, `liquid` and the rest) do nothing in `'material'`.
- `appearance`, `layoutDirection` and the back key work as in glass: back closes an open sheet, dialog, menu or search first.
- Custom [fonts](fonts.md) and [icons](icons.md) will apply to both styles.

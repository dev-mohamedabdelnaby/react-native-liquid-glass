# Android control shapes

```ts
androidControlShape?: 'pill' | 'labeled' | 'glow'; // default 'pill'; iOS ignores it
androidGlowIntensity?: number; // 0 (no glow) to 1; default 0.35; only 'glow' uses it; iOS ignores it
```

- Android only. iOS is unchanged.
- Set it in `GlassSettings` (the provider applies it to every screen) or as a prop on one screen. The prop wins.
- It changes four controls in both [`androidStyle`s](android-style.md): the switch, the slider, the segmented control (top tabs) and the bottom tab bar. Everything else keeps its style.
- `'pill'` is the default: an app that sets nothing gets pill controls. An unknown value (or the old `'default'` / `'squared'`) is `'pill'` too.
- `androidGlowIntensity` sets how strongly `'glow'` glows. Native code clamps it to 0..1; 0 draws the glow shape without a halo, 1 is the strongest. Same provider / screen-prop rules.
- Android Home has a picker for it (Pill / Labeled / Glow), plus a Glow intensity slider while Glow is picked, and sends `onSettingsChange` with the new values.

| Shape | Switch | Slider | Top tabs | Bottom tabs |
|---|---|---|---|---|
| `'pill'` | A solid pill in the accent with ON / OFF inside and a white thumb; gray when off | A thick rounded track with a white round handle | An accent pill; the selected tab is a white pill inside it | An accent bar with a white pill behind the selected tab |
| `'labeled'` | An outlined pill with ON / OFF and a filled thumb | A ring handle on a thin track | Outlined; the selected tab is filled | An outlined bar; the selected tab is filled |
| `'glow'` | Glows when on | A glowing track with a ring handle | The selected tab glows | The selected tab glows |

The four controls no longer have the liquid drop or Material's stock widgets, so `sliderLiquid`, `switchLiquid`, `segmentedLiquid`, `tabBarLiquid`, `tabBarDrop` and `androidSegmentedCheck` don't change them.

- Colours come from the accent: a control's own colour (a slider's tint, the Tab Bar screen's bar colour), else `tint`, else the theme's primary. Text and marks on the accent are white or black, whichever reads better. Light and dark both work.
- RTL mirrors every shape: the switch's on side, the slider's fill and the selected tab follow the direction.
- TalkBack reads them as Android's own controls: a switch row reads its label and state, a slider its label and value ("Volume, 50%") and you can adjust it, a tab its label and whether it is selected.
- Touch targets are at least 48 dp. The slider gives a light haptic each tenth of the way. The marker and thumb move with a spring; there is no liquid drop.
- ON / OFF are fixed English labels.

```tsx
import { useMemo } from 'react';
import {
  GlassControlsScreen, GlassHomeScreen, GlassSettingsProvider,
} from '@native-dev-kit/react-native-liquid-glass';

// Labeled controls everywhere, and a stronger glow for any screen that picks 'glow'.
export function LabeledHome() {
  const value = useMemo(
    () => ({ androidControlShape: 'labeled' as const, androidGlowIntensity: 0.6 }),
    []
  );
  return (
    <GlassSettingsProvider value={value}>
      <GlassHomeScreen style={{ flex: 1 }} />
    </GlassSettingsProvider>
  );
}

// Glowing controls on this screen, in Material, whatever the provider says.
export function GlowControls() {
  return (
    <GlassControlsScreen
      androidStyle="material"
      androidControlShape="glow"
      androidGlowIntensity={1}
      style={{ flex: 1 }}
    />
  );
}
```

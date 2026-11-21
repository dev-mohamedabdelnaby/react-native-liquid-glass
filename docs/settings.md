# Settings

Settings change how every screen looks: light or dark, the glass tint, and (on Android) the glass engine's options. They come from `GlassSettingsProvider`, not from screen props.

## The provider

```tsx
import { useState } from 'react';
import {
  DEFAULT_SETTINGS, GlassHomeScreen, GlassSettingsProvider, type GlassSettings,
} from '@native-dev-kit/react-native-liquid-glass';

export function Root() {
  const [settings, setSettings] = useState<GlassSettings>(DEFAULT_SETTINGS);
  return (
    <GlassSettingsProvider value={settings}>
      <GlassHomeScreen style={{ flex: 1 }} onSettingsChange={setSettings} />
    </GlassSettingsProvider>
  );
}
```

- `value` is a `Partial<GlassSettings>`. Missing keys and keys set to `undefined` keep the value from above: the parent provider, or `DEFAULT_SETTINGS` when there is none.
- Providers nest. An inner provider changes only the keys it sets, for the screens inside it.
- A change reaches every mounted screen at once, including screens under the one on top.
- Without any provider, screens use `DEFAULT_SETTINGS`.
- Settings live in your app's state. The library does not save them; they reset when the app restarts unless you save them yourself.

### Two rules

1. **Feed `onSettingsChange` back.** Home's controls show the settings they get from the provider. When the user changes one, Home sends `onSettingsChange` with the full new settings. If you do not store them and pass them back to the provider, the change does not stick and the other screens never see it.
2. **Keep `value` stable.** The provider merges `value` again whenever its identity changes, and then every screen gets new props. Pass state (as above) or a `useMemo` result, not an object literal written inline:

   ```tsx
   import { useMemo } from 'react';
   import { GlassSettingsProvider } from '@native-dev-kit/react-native-liquid-glass';

   export function Dark({ children }: { children: React.ReactNode }) {
     const value = useMemo(() => ({ appearance: 'dark' as const }), []);
     return <GlassSettingsProvider value={value}>{children}</GlassSettingsProvider>;
   }
   ```

### Reading settings

`useGlassSettings()` returns the merged settings for the component that calls it, with every key filled in (`Required<GlassSettings>`).

## All settings

```ts
type GlassSettings = {
  appearance?: 'system' | 'light' | 'dark';
  tint?: ColorValue | null;
  layoutDirection?: 'system' | 'ltr' | 'rtl';
  androidStyle?: 'glass' | 'material';
  androidSegmentedCheck?: boolean;
  clear?: boolean;
  blur?: number;
  white?: number;
  black?: number;
  edgeLight?: number;
  refraction?: { height: number; amount: number };
  opaque?: boolean;
  liquid?: boolean;
  dropColor?: ColorValue | null;
  sliderLiquid?: boolean;
  switchLiquid?: boolean;
  segmentedLiquid?: boolean;
  tabBarLiquid?: boolean;
  tabBarDrop?: boolean;
};
```

Each platform reads the settings its native app has and ignores the rest.

| Setting | Default | iOS | Android | Meaning |
|---|---|---|---|---|
| `appearance` | `'system'` | yes | yes | Light, dark, or follow the device. |
| `tint` | `null` | yes | yes | A colour every glass surface without its own tint takes. `null`: no tint. |
| `layoutDirection` | `'system'` | yes | yes | Left-to-right or right-to-left. See [layout-direction.md](layout-direction.md). |
| `androidStyle` | `'glass'` | no | yes | `'material'`: Material 3 components instead of glass. A screen's `androidStyle` prop wins. See [android-style.md](android-style.md). |
| `androidSegmentedCheck` | `true` | no | yes | Material only: the check mark on the selected segment of segmented buttons. `false` hides it. A screen's `androidSegmentedCheck` prop wins. |
| `clear` | `true` | no | yes | `true`: nearly clear glass. `false`: frosted (regular) glass. |
| `blur` | `3` | no | yes | Blur radius in dp. |
| `white` | `0.25` | no | yes | White wash over the glass, 0 to 1. |
| `black` | `0.05` | no | yes | Black wash over the glass, 0 to 1. |
| `edgeLight` | `0.15` | no | yes | Strength of the light on the glass edge. |
| `refraction` | `{ height: 14, amount: 24 }` | no | yes | Lens bend at the glass edge: band height and amount, in dp. |
| `opaque` | `false` | no | yes | Fill every glass surface solid. |
| `liquid` | `true` | no | yes | `false`: a frosted blur without the liquid lens, rim light, press swell or merging. |
| `dropColor` | `null` | no | yes | Tint of the liquid drop on tab bars, segmented controls, sliders and switches. `null`: clear. |
| `sliderLiquid` | `true` | no | yes | Liquid lift on slider thumbs. `false`: solid knobs. |
| `switchLiquid` | `true` | no | yes | Liquid lift on switch thumbs. `false`: solid knobs. |
| `segmentedLiquid` | `true` | no | yes | Liquid drop on segmented controls. `false`: the pill slides without the drop. |
| `tabBarLiquid` | `true` | no | yes | Glass on bottom tab bars. `false`: frosted. |
| `tabBarDrop` | `true` | no | yes | Liquid drop on bottom tab bars. |

`DEFAULT_SETTINGS` holds exactly these defaults. They are the native apps' defaults.

## What Home shows

- **iOS Home:** Appearance (System, Light, Dark), Glass tint (None, Blue, Purple, Pink, Orange, Green, Teal) and Layout direction (System, LTR, RTL).
- **Android Home:** Appearance, Layout direction (System, LTR, RTL), Android style (Glass, Material) and, in Material, a "Check on the selected segment" switch, plus a Glass section with the Android settings above (variant, blur, washes, edge light, refraction, tint, drop colour, opaque, liquid and the per-control switches).

## Appearance

`appearance` works differently on the two platforms. Both follow the same rule: **the latest mounted screen that asks for light or dark wins, and the app's own value comes back when the last one is unmounted.**

### iOS: window-wide

SwiftUI's dark mode is the window's style, so a screen with `'light'` or `'dark'` sets `overrideUserInterfaceStyle` on its window. That changes the **whole window**: your React Native screens and the status bar too.

- `'system'` never writes the window.
- The window's own style is saved at the first request and put back when the last request ends.
- A request lasts while the screen is mounted, **even when it is off screen** (under a pushed screen, in an inactive tab). So an off-screen glass screen still holds its light or dark.
- A request ends when the screen unmounts.
- If your app calls `Appearance.setColorScheme` while a glass screen holds a request, your value is lost when the request ends: the window gets back the style saved before the first request.

### Android: inside the screen, plus the bar icons

- The screen's Compose theme follows `appearance`. Your React Native views are not changed.
- The screen also sets the status and navigation bar icons (dark icons in light, light icons in dark), as the native app does. Same rule: latest mounted screen wins; when the last one is unmounted, the app's own icon setting comes back. If your app changed the bar icons itself (with `<StatusBar>`) while a glass screen was showing, your value is kept.

## Tint

`tint` takes any React Native colour (`'#AF52DE'`, `'purple'`, `'rgba(175, 82, 222, 0.35)'`), or `null` for none.

- **iOS:** the glass takes the colour. Home's tint picker shows the named choice that matches the colour (Blue, Purple, Pink, Orange, Green, Teal), or None when no choice matches. A colour that matches no choice still tints the glass. When the user picks a choice on Home, `onSettingsChange` sends the colour as `'#RRGGBBAA'`; None sends `null`.
- **Android:** every glass surface without its own tint takes the colour. With `androidStyle: 'material'` it is the Material scheme's primary (the accent) instead. Home's colour picker sends `'#RRGGBBAA'` too.

## Coming in the next version

These settings are planned and not in the package yet:

- `fontFamily`: see [fonts.md](fonts.md).
- `icons`: see [icons.md](icons.md).

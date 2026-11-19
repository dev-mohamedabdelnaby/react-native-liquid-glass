# Layout direction (right-to-left)

```ts
layoutDirection?: 'system' | 'ltr' | 'rtl'; // in GlassSettings, default 'system'
```

`'ltr'` and `'rtl'` force a direction for the glass screens. `'system'` follows the device:

- **iOS:** the system layout direction (the device language).
- **Android:** React Native's own direction, `I18nManager.isRTL` (device language, `allowRTL`, `forceRTL`), so the glass screens agree with your React Native screens.

It works on both platforms. Set it in the provider, or let the user pick it on Home: both Homes have the "Layout direction" picker (System, LTR, RTL) under Appearance, and Home sends the choice through `onSettingsChange`.

It changes only the glass screens. Your React Native views follow React Native's `I18nManager`.

```tsx
import { useMemo } from 'react';
import { GlassSettingsProvider } from '@native-dev-kit/react-native-liquid-glass';

export function ForceRtl({ children }: { children: React.ReactNode }) {
  const value = useMemo(() => ({ layoutDirection: 'rtl' as const }), []);
  return <GlassSettingsProvider value={value}>{children}</GlassSettingsProvider>;
}
```

## What mirrors

- **iOS:** the screens mirror the way SwiftUI mirrors any app: the layout flips and the back button moves to the right, its chevron pointing the other way.
- **Android:** the same as iOS. The back arrow moves to the right and points the other way. Sliders fill from the right, switch thumbs and liquid drops slide toward the reading direction, segmented controls, tab bars and sliding selectors follow the selected item, menus and popovers grow from their button's leading side, minimizing tab bars shrink into the leading circle, and sheets, dialogs and the context menu preview follow too.
- Icons that SF Symbols mirrors (back and forward chevrons, list and sidebar icons) mirror on both platforms. Media controls, checkmarks and similar symbols do not.

## Screen transitions in RTL

In RTL a pushed screen comes in from the left, as `UINavigationController` does. With native-stack, pick the animation from the direction:

```tsx
import { I18nManager, Platform } from 'react-native';
import type { GlassSettings } from '@native-dev-kit/react-native-liquid-glass';

export function stackOptions(settings: GlassSettings) {
  const rtl =
    settings.layoutDirection === 'rtl' ||
    (settings.layoutDirection === 'system' && I18nManager.isRTL);
  return {
    headerShown: false,
    animation: Platform.select({
      android: rtl ? ('ios_from_left' as const) : ('ios_from_right' as const),
      default: rtl ? ('slide_from_left' as const) : ('default' as const),
    }),
    // iOS: the swipe back starts at the leading (right) edge and drives the same slide.
    animationMatchesGesture: rtl,
  };
}

// <Stack.Navigator screenOptions={stackOptions(settings)}>
```

- **Android:** `ios_from_left` in RTL, `ios_from_right` in LTR. The back key slides the screen out to the left.
- **iOS:** `slide_from_left` in RTL, the system push (`default`) in LTR. `animationMatchesGesture: rtl` lets the swipe from the right edge drive the pop in RTL.

This is what the example app does (`example/src/App.tsx`).

## Known differences

- **iOS full-screen covers stay left-to-right.** The Tab Bar "With Now Playing Bar" cover (tabs, close button, title) is not mirrored on iOS, even though Home's footer says RTL mirrors full-screen demos. Android mirrors it.
- **iOS pieces drawn by UIKit stay left-to-right:** menu rows stay left-aligned, and the open search field keeps its magnifier on the left. Android mirrors both.
- **Android, English text in RTL:** punctuation at the end of an English sentence shows at its start (".presets"), because Compose sets an unspecified text direction from the layout direction. SwiftUI uses the text's own direction. Arabic and Hebrew text are not affected.
- **iOS, RTL back to System:** Home's list can stay drawn mirrored until it is laid out again. The native iOS app does the same.

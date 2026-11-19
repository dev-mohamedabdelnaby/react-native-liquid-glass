# Layout direction (right-to-left)

```ts
layoutDirection?: 'system' | 'ltr' | 'rtl'; // in GlassSettings, default 'system'
```

`'system'` follows the device language. `'ltr'` and `'rtl'` force a direction for the glass screens.

## iOS: available now

- Set it in the provider, or let the user pick it on iOS Home (Layout direction: System, LTR, RTL); Home sends it through `onSettingsChange`.
- In RTL the screens mirror the way SwiftUI mirrors any app: the layout flips and the back button moves to the right.
- It changes only the glass screens. Your React Native views follow React Native's own `I18nManager`.

```tsx
import { useMemo } from 'react';
import { GlassSettingsProvider } from '@native-dev-kit/react-native-liquid-glass';

export function ForceRtl({ children }: { children: React.ReactNode }) {
  const value = useMemo(() => ({ layoutDirection: 'rtl' as const }), []);
  return <GlassSettingsProvider value={value}>{children}</GlassSettingsProvider>;
}
```

Known: switching from RTL back to System can leave Home's list text drawn mirrored until it is laid out again. The native iOS app does the same.

## Android: coming in the next version

Today Android accepts `layoutDirection` and sends it back in `onSettingsChange`, but does not mirror the screens. The next version adds:

- The iOS Home's "Layout direction" picker (System, LTR, RTL) on Android Home.
- Mirroring the way iOS does it: sliders fill from the leading edge, switch thumbs and liquid drops slide toward the reading direction, the segmented pill and tab bar drop follow the selected item, menus and popovers grow from their button's leading side, and the back arrow points the other way.

## Screen transitions in RTL (next version)

In RTL, a pushed screen should come in from the left, as `UINavigationController` does. With native-stack, pick the animation from the direction. This needs the next version on Android; on iOS it works with today's package:

```tsx
import { I18nManager, Platform } from 'react-native';
import type { GlassSettings } from '@native-dev-kit/react-native-liquid-glass';

export function stackAnimation(settings: GlassSettings) {
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

// <Stack.Navigator screenOptions={stackAnimation(settings)}>
```

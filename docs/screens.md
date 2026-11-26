# Screens

Each component shows one whole native screen. All 22 share one native view, `LiquidGlassScreen`, which gets a `screen` id. You do not use that view directly.

## The components

| Component | Screen id | Home row title | Bar title | What it shows |
|---|---|---|---|---|
| `GlassHomeScreen` | `home` | — | Liquid Glass | The start screen: the settings (appearance, layout direction, tint; on Android also Glass / Material and the glass options) and the list of all 21 screens. |
| `GlassEffectScreen` | `glassEffect` | Glass Effect | Glass Effect | Glass capsules, the clear variant, tinted glass, custom shapes. |
| `GlassViewScreen` | `glassView` | Glass View | Glass View | A glass card over a photo with live options. |
| `InteractiveGlassScreen` | `interactive` | Interactive Glass | Interactive | Press-and-hold glass, a counter button, static vs interactive. |
| `GlassButtonsScreen` | `buttons` | Buttons | Buttons | Every button style, a media row, a disabled button. |
| `GlassContainerScreen` | `container` | Glass Container | Container | Two glass buttons that merge as they get closer. |
| `GlassMorphingScreen` | `morphing` | Morphing | Morphing | A + button that morphs a column of tools out of itself. |
| `GlassUnionScreen` | `union` | Union | Union | Symbols grouped into glass unions. |
| `GlassToolbarScreen` | `toolbar` | Toolbar | Toolbar | Large title, glass toolbar buttons, bottom toolbar, compose button. |
| `GlassFloatingHeaderScreen` | `floatingHeader` | Floating Header | Floating Header | A colourful header with glass buttons over a list, scroll-edge styles. |
| `GlassTabBarScreen` | `tabBar` | Tab Bar | Tab Bar | Floating glass tab bar demos, minimize on scroll. |
| `GlassSheetsScreen` | `sheets` | Sheets & Menus | Sheets & Menus | A sheet, a popover, a glass menu. |
| `GlassControlsScreen` | `controls` | Controls | Controls | Slider, toggle, segmented picker, steppers, pickers. |
| `GlassTextScreen` | `text` | Text | Text | Glass letterforms, titles, a paragraph card, tags. |
| `GlassTransitionsScreen` | `transitions` | Glass Transitions | Transitions | Matched / Materialize / Identity transitions with badges. |
| `GlassSearchScreen` | `search` | Search | Search | A list with a minimized glass search button. |
| `GlassEditSearchScreen` | `editSearch` | Edit & Search Buttons | Notes | A notes list with glass edit and search buttons. |
| `GlassScrollEdgeScreen` | `scrollEdge` | Scroll Edge & Bars | Scroll Edge | Filter chips, rows, scroll-edge effects. |
| `GlassSplitViewScreen` | `splitView` | Sidebar & Inspector | Split View | A sidebar / content / inspector demo. |
| `GlassZoomTransitionsScreen` | `zoomTransitions` | Zoom Transitions | (none: a status pill) | A sheet that zooms out of its button, badges. |
| `GlassAlertsScreen` | `alerts` | Alerts & Dialogs | Alerts & Dialogs | An alert, a confirmation dialog, a context menu. |
| `GlassRecipesScreen` | `recipes` | Recipes | Recipes | Sliding selector, map controls, mini player, action menu. |

The **bar title** is what the screen shows in its own bar; some screens use a shorter title there than their row on Home. `GLASS_SCREENS` holds the Home row titles. Screenshots of every screen on both platforms: [USAGE.md](../USAGE.md).

## Props

Every component takes the same props, `GlassScreenProps`:

```ts
type GlassScreenProps = {
  onBack?: () => void;
  onOpen?: (screen: GlassScreenId) => void;
  onSettingsChange?: (settings: GlassSettings) => void;
  androidStyle?: 'glass' | 'material';
  androidControlShape?: GlassSettings['androidControlShape'];
  androidGlowIntensity?: number;
  fontFamily?: GlassSettings['fontFamily'];
  icons?: GlassSettings['icons'];
  style?: StyleProp<ViewStyle>;
  testID?: string;
};
```

| Prop | When it fires / what it does |
|---|---|
| `style` | Size and place of the screen. **There is no default size**: pass `{ flex: 1 }` or the screen is 0 × 0. |
| `onBack` | The screen's own back (or close) button was tapped. Call `navigation.goBack()` here. Home has no back button. |
| `onOpen` | Home only: a row was tapped. You get the screen id, for example `'buttons'`. Push that screen. |
| `androidStyle` | Android only: `'material'` draws this screen with Material 3, `'glass'` with glass. Wins over the provider's `androidStyle`. See [android-style.md](android-style.md). |
| `androidControlShape` | Android Material only: the shape of this screen's switches, sliders, segmented controls and bottom tab bar. Wins over the provider's. See [control-shapes.md](control-shapes.md). |
| `androidGlowIntensity` | Android Material only: how strongly this screen's `'glow'` controls glow, 0 to 1. Wins over the provider's. |
| `fontFamily` | This screen's font: one family, or a font per weight. Wins over the provider's. See [fonts.md](fonts.md). |
| `icons` | This screen's custom icons, merged over the provider's key by key (a key set to `undefined` keeps the native icon on this screen). See [icons.md](icons.md). |
| `onSettingsChange` | Home only: the user changed a setting on Home. You get the full `GlassSettings`. Store it and pass it back to `GlassSettingsProvider`. |
| `testID` | Passed to the native view, for tests. |

Screens take no children. Settings do not come in as props (except `androidStyle`, `androidControlShape`, `androidGlowIntensity`, `fontFamily` and `icons`); they come from the nearest `GlassSettingsProvider` (see [settings.md](settings.md)).

## Back on each platform

- **iOS.** Every screen except Home shows a back button (a `chevron.backward` glass button at the top leading corner). It sends `onBack`. The swipe from the leading screen edge (left, or right in right-to-left) is native-stack's own gesture.
- **Android.** The screen's back arrow sends `onBack`. The system back key and gesture go to the screen's own open sheet, menu, search field, panel or cover first and close it. When nothing in the screen is open, the key goes to React Native as usual, and React Navigation goes back.

## `GLASS_SCREENS`, `GlassScreenId`

```ts
import { GLASS_SCREENS, type GlassScreenId } from '@native-dev-kit/react-native-liquid-glass';

GLASS_SCREENS; // readonly [{ id: 'glassEffect', title: 'Glass Effect' }, …] — 21 entries, Home's order
type Id = GlassScreenId; // 'glassEffect' | 'glassView' | … | 'recipes'
```

`GLASS_SCREENS` does not include Home. Use it to register one route per screen; the route name can be the id, so `onOpen={(id) => navigation.navigate(id)}` works as is.

A map from id to component, typed so a missing screen is a compile error:

```tsx
import {
  GlassAlertsScreen, GlassButtonsScreen, GlassContainerScreen, GlassControlsScreen,
  GlassEditSearchScreen, GlassEffectScreen, GlassFloatingHeaderScreen, GlassMorphingScreen,
  GlassRecipesScreen, GlassScrollEdgeScreen, GlassSearchScreen, GlassSheetsScreen,
  GlassSplitViewScreen, GlassTabBarScreen, GlassTextScreen, GlassToolbarScreen,
  GlassTransitionsScreen, GlassUnionScreen, GlassViewScreen, GlassZoomTransitionsScreen,
  InteractiveGlassScreen, type GlassScreenId, type GlassScreenProps,
} from '@native-dev-kit/react-native-liquid-glass';

export const SCREENS: Record<GlassScreenId, (props: GlassScreenProps) => React.JSX.Element> = {
  glassEffect: GlassEffectScreen,
  glassView: GlassViewScreen,
  interactive: InteractiveGlassScreen,
  buttons: GlassButtonsScreen,
  container: GlassContainerScreen,
  morphing: GlassMorphingScreen,
  union: GlassUnionScreen,
  toolbar: GlassToolbarScreen,
  floatingHeader: GlassFloatingHeaderScreen,
  tabBar: GlassTabBarScreen,
  sheets: GlassSheetsScreen,
  controls: GlassControlsScreen,
  text: GlassTextScreen,
  transitions: GlassTransitionsScreen,
  search: GlassSearchScreen,
  editSearch: GlassEditSearchScreen,
  scrollEdge: GlassScrollEdgeScreen,
  splitView: GlassSplitViewScreen,
  zoomTransitions: GlassZoomTransitionsScreen,
  alerts: GlassAlertsScreen,
  recipes: GlassRecipesScreen,
};
```

## State and lifetime

- A screen keeps its native state while it stays mounted: going back to Home keeps Home's scroll position, and a screen under a pushed one keeps its state.
- A screen's sheets, menus, popovers, alerts and full-screen covers are native. They open over the screen, not as React Native modals.
- Unmounting a screen ends it. Its appearance request (see [settings.md](settings.md#appearance)) ends too.

## Exports

```ts
import {
  // Components
  GlassHomeScreen, GlassEffectScreen, GlassViewScreen, InteractiveGlassScreen, GlassButtonsScreen,
  GlassContainerScreen, GlassMorphingScreen, GlassUnionScreen, GlassToolbarScreen,
  GlassFloatingHeaderScreen, GlassTabBarScreen, GlassSheetsScreen, GlassControlsScreen,
  GlassTextScreen, GlassTransitionsScreen, GlassSearchScreen, GlassEditSearchScreen,
  GlassScrollEdgeScreen, GlassSplitViewScreen, GlassZoomTransitionsScreen, GlassAlertsScreen,
  GlassRecipesScreen,
  // Settings and icons
  GlassSettingsProvider, useGlassSettings, DEFAULT_SETTINGS, vectorIcon,
  // Lists and types
  GLASS_SCREENS, type GlassScreenId, type GlassScreenProps, type GlassSettings, type GlassIcon,
} from '@native-dev-kit/react-native-liquid-glass';
```

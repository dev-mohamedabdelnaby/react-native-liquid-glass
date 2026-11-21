# Screens

Each component shows one whole native screen. All 22 share one native view, `LiquidGlassScreen`, which gets a `screen` id. You do not use that view directly.

## The components

| Component | Screen id | What it shows |
|---|---|---|
| `GlassHomeScreen` | `home` | The app's start screen: a list of all 21 screens and the settings (appearance, layout direction, tint, and the glass options on Android). |
| `GlassEffectScreen` | `glassEffect` | Glass Effect |
| `GlassViewScreen` | `glassView` | Glass View |
| `InteractiveGlassScreen` | `interactive` | Interactive Glass |
| `GlassButtonsScreen` | `buttons` | Buttons |
| `GlassContainerScreen` | `container` | Glass Container |
| `GlassMorphingScreen` | `morphing` | Morphing |
| `GlassUnionScreen` | `union` | Union |
| `GlassToolbarScreen` | `toolbar` | Toolbar |
| `GlassFloatingHeaderScreen` | `floatingHeader` | Floating Header |
| `GlassTabBarScreen` | `tabBar` | Tab Bar |
| `GlassSheetsScreen` | `sheets` | Sheets & Menus |
| `GlassControlsScreen` | `controls` | Controls |
| `GlassTextScreen` | `text` | Text |
| `GlassTransitionsScreen` | `transitions` | Glass Transitions |
| `GlassSearchScreen` | `search` | Search |
| `GlassEditSearchScreen` | `editSearch` | Edit & Search Buttons |
| `GlassScrollEdgeScreen` | `scrollEdge` | Scroll Edge & Bars |
| `GlassSplitViewScreen` | `splitView` | Sidebar & Inspector |
| `GlassZoomTransitionsScreen` | `zoomTransitions` | Zoom Transitions |
| `GlassAlertsScreen` | `alerts` | Alerts & Dialogs |
| `GlassRecipesScreen` | `recipes` | Recipes |

## Props

Every component takes the same props, `GlassScreenProps`:

```ts
type GlassScreenProps = {
  onBack?: () => void;
  onOpen?: (screen: GlassScreenId) => void;
  onSettingsChange?: (settings: GlassSettings) => void;
  androidStyle?: 'glass' | 'material';
  androidSegmentedCheck?: boolean;
  fontFamily?: GlassSettings['fontFamily'];
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
| `androidSegmentedCheck` | Android Material only: `false` hides the check mark on the selected segment of segmented buttons. Wins over the provider's. |
| `fontFamily` | This screen's font: one family, or a font per weight. Wins over the provider's. See [fonts.md](fonts.md). |
| `onSettingsChange` | Home only: the user changed a setting on Home. You get the full `GlassSettings`. Store it and pass it back to `GlassSettingsProvider`. |
| `testID` | Passed to the native view, for tests. |

Screens take no children. Settings do not come in as props (except `androidStyle`, `androidSegmentedCheck` and `fontFamily`); they come from the nearest `GlassSettingsProvider` (see [settings.md](settings.md)).

## Back on each platform

- **iOS.** Every screen except Home shows a back button (a `chevron.backward` glass button at the top leading corner). It sends `onBack`. The swipe from the screen edge is native-stack's own gesture.
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
// Components
GlassHomeScreen, GlassEffectScreen, GlassViewScreen, InteractiveGlassScreen, GlassButtonsScreen,
GlassContainerScreen, GlassMorphingScreen, GlassUnionScreen, GlassToolbarScreen,
GlassFloatingHeaderScreen, GlassTabBarScreen, GlassSheetsScreen, GlassControlsScreen,
GlassTextScreen, GlassTransitionsScreen, GlassSearchScreen, GlassEditSearchScreen,
GlassScrollEdgeScreen, GlassSplitViewScreen, GlassZoomTransitionsScreen, GlassAlertsScreen,
GlassRecipesScreen

// Settings
GlassSettingsProvider, useGlassSettings, DEFAULT_SETTINGS

// Lists and types
GLASS_SCREENS, type GlassScreenId, type GlassScreenProps, type GlassSettings
```

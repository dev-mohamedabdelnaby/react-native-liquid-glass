# docs/assets index

Media for the docs. One line per file: path | platform/style | what it shows | what it illustrates.
Screenshots: 390 px wide, 256-colour PNG, Light, LTR, default settings (CUSTOMIZE false), status bar kept.
GIFs: 320 px wide, 12 fps, at most 6 s.

Notes:
- iOS: iPhone 18 Pro simulator (iOS 27), Release build, status bar overridden to 9:41.
- Android: Redmi Note 10 Pro (MIUI), Release build. The phone's system font is an italic MIUI font, so Android text looks italic; that is the phone, not the library. The status bar shows the phone's real time and notification icons (no demo mode). The phone runs in dark mode, so Android shots set Light from Home's appearance control (Home shows 'Light' selected, not 'System').
- The example's 'Push test' button (an RN button over Home, example-only) was removed for these captures.
- Bar titles differ from the Home row titles on some screens (Container, Interactive, Transitions, Notes, Scroll Edge, Split View; Zoom Transitions has none): the shots show the real titles.

## ios/

- `ios/alerts.png` | iOS (SwiftUI, iPhone 18 Pro simulator, iOS 27) | Alerts & Dialogs: Show Alert, Show Confirmation Dialog, long-press artwork for a context menu | `GlassAlertsScreen` (screen id `alerts`), default settings
- `ios/buttons.png` | iOS (SwiftUI, iPhone 18 Pro simulator, iOS 27) | Buttons: glass, glass prominent, clear, tinted prominent, large with icon, media buttons row, disabled | `GlassButtonsScreen` (screen id `buttons`), default settings
- `ios/container.png` | iOS (SwiftUI, iPhone 18 Pro simulator, iOS 27) | Glass Container: two glass buttons with 'Container spacing' and 'Pull together' sliders that merge them | `GlassContainerScreen` (screen id `container`), default settings
- `ios/controls.png` | iOS (SwiftUI, iPhone 18 Pro simulator, iOS 27) | Controls: slider, toggle, segmented picker, steppers, menu picker, date and colour pickers | `GlassControlsScreen` (screen id `controls`), default settings
- `ios/editSearch.png` | iOS (SwiftUI, iPhone 18 Pro simulator, iOS 27) | Edit & Search Buttons (bar title 'Notes'): a notes list with glass edit and search buttons | `GlassEditSearchScreen` (screen id `editSearch`), default settings
- `ios/floatingHeader.png` | iOS (SwiftUI, iPhone 18 Pro simulator, iOS 27) | Floating Header: a colourful header with glass bar buttons, scroll-edge style picker (Status Bar/Soft/None), rows | `GlassFloatingHeaderScreen` (screen id `floatingHeader`), default settings
- `ios/glassEffect.png` | iOS (SwiftUI, iPhone 18 Pro simulator, iOS 27) | Glass Effect: default capsule, clear variant, tinted glass (orange/blue/green), custom shapes (circle, rounded, rect) | `GlassEffectScreen` (screen id `glassEffect`), default settings
- `ios/glassView.png` | iOS (SwiftUI, iPhone 18 Pro simulator, iOS 27) | Glass View: a glass card over a photo with live options (Regular/Clear, shape, corner radius, interactive, tint) | `GlassViewScreen` (screen id `glassView`), default settings
- `ios/home.png` | iOS (SwiftUI, iPhone 18 Pro simulator, iOS 27) | Home: the settings (appearance, layout direction, tint; on Android also Glass/Material, glass options) and the list of all 21 screens | `GlassHomeScreen` (screen id `home`), default settings
- `ios/interactive.png` | iOS (SwiftUI, iPhone 18 Pro simulator, iOS 27) | Interactive Glass: a press-and-hold glass shape, a tap counter button, static vs interactive | `InteractiveGlassScreen` (screen id `interactive`), default settings
- `ios/morphing.png` | iOS (SwiftUI, iPhone 18 Pro simulator, iOS 27) | Morphing: a + button that morphs a column of tools out of itself | `GlassMorphingScreen` (screen id `morphing`), default settings
- `ios/recipes.png` | iOS (SwiftUI, iPhone 18 Pro simulator, iOS 27) | Recipes: sliding selector, map controls, mini player, floating action menu | `GlassRecipesScreen` (screen id `recipes`), default settings
- `ios/scrollEdge.png` | iOS (SwiftUI, iPhone 18 Pro simulator, iOS 27) | Scroll Edge & Bars (bar title 'Scroll Edge'): filter chips, coloured rows, Auto/Soft/Hard/Hidden edge-effect picker | `GlassScrollEdgeScreen` (screen id `scrollEdge`), default settings
- `ios/search.png` | iOS (SwiftUI, iPhone 18 Pro simulator, iOS 27) | Search: a fruit list with a minimized glass search button in the bottom bar | `GlassSearchScreen` (screen id `search`), default settings
- `ios/sheets.png` | iOS (SwiftUI, iPhone 18 Pro simulator, iOS 27) | Sheets & Menus: Show Sheet, Show Popover and a glass menu button | `GlassSheetsScreen` (screen id `sheets`), default settings
- `ios/splitView.png` | iOS (SwiftUI, iPhone 18 Pro simulator, iOS 27) | Sidebar & Inspector (bar title 'Split View'): explanation card and Open Split View button | `GlassSplitViewScreen` (screen id `splitView`), default settings
- `ios/tabBar.png` | iOS (SwiftUI, iPhone 18 Pro simulator, iOS 27) | Tab Bar: launcher for the floating glass tab bar demos (now playing bar, search inside tabs, search separated, tabs without search), minimize-on-scroll toggle | `GlassTabBarScreen` (screen id `tabBar`), default settings
- `ios/text.png` | iOS (SwiftUI, iPhone 18 Pro simulator, iOS 27) | Text: glass letterforms, large title on glass, tinted headline, paragraph card, tags | `GlassTextScreen` (screen id `text`), default settings
- `ios/toolbar.png` | iOS (SwiftUI, iPhone 18 Pro simulator, iOS 27) | Toolbar: large title, glass toolbar buttons (heart, share, more), options list, bottom toolbar and floating compose button | `GlassToolbarScreen` (screen id `toolbar`), default settings
- `ios/transitions.png` | iOS (SwiftUI, iPhone 18 Pro simulator, iOS 27) | Glass Transitions: Matched/Materialize/Identity picker and a Show Badges button | `GlassTransitionsScreen` (screen id `transitions`), default settings
- `ios/union.png` | iOS (SwiftUI, iPhone 18 Pro simulator, iOS 27) | Union: day and night symbols grouped into two glass unions, padding slider | `GlassUnionScreen` (screen id `union`), default settings
- `ios/zoomTransitions.png` | iOS (SwiftUI, iPhone 18 Pro simulator, iOS 27) | Zoom Transitions (no bar title): Online status, inbox button with badge, New Message button, unread badge stepper | `GlassZoomTransitionsScreen` (screen id `zoomTransitions`), default settings

## android-glass/

- `android-glass/alerts.png` | Android, androidStyle 'glass' (Compose, Redmi Note 10 Pro / MIUI) | Alerts & Dialogs: Show Alert, Show Confirmation Dialog, long-press artwork for a context menu | `GlassAlertsScreen` (screen id `alerts`), default settings
- `android-glass/buttons.png` | Android, androidStyle 'glass' (Compose, Redmi Note 10 Pro / MIUI) | Buttons: glass, glass prominent, clear, tinted prominent, large with icon, media buttons row, disabled | `GlassButtonsScreen` (screen id `buttons`), default settings
- `android-glass/container.png` | Android, androidStyle 'glass' (Compose, Redmi Note 10 Pro / MIUI) | Glass Container: two glass buttons with 'Container spacing' and 'Pull together' sliders that merge them | `GlassContainerScreen` (screen id `container`), default settings
- `android-glass/controls.png` | Android, androidStyle 'glass' (Compose, Redmi Note 10 Pro / MIUI) | Controls: slider, toggle, segmented picker, steppers, menu picker, date and colour pickers | `GlassControlsScreen` (screen id `controls`), default settings
- `android-glass/editSearch.png` | Android, androidStyle 'glass' (Compose, Redmi Note 10 Pro / MIUI) | Edit & Search Buttons (bar title 'Notes'): a notes list with glass edit and search buttons | `GlassEditSearchScreen` (screen id `editSearch`), default settings
- `android-glass/floatingHeader.png` | Android, androidStyle 'glass' (Compose, Redmi Note 10 Pro / MIUI) | Floating Header: a colourful header with glass bar buttons, scroll-edge style picker (Status Bar/Soft/None), rows | `GlassFloatingHeaderScreen` (screen id `floatingHeader`), default settings
- `android-glass/glassEffect.png` | Android, androidStyle 'glass' (Compose, Redmi Note 10 Pro / MIUI) | Glass Effect: default capsule, clear variant, tinted glass (orange/blue/green), custom shapes (circle, rounded, rect) | `GlassEffectScreen` (screen id `glassEffect`), default settings
- `android-glass/glassView.png` | Android, androidStyle 'glass' (Compose, Redmi Note 10 Pro / MIUI) | Glass View: a glass card over a photo with live options (Regular/Clear, shape, corner radius, interactive, tint) | `GlassViewScreen` (screen id `glassView`), default settings
- `android-glass/home.png` | Android, androidStyle 'glass' (Compose, Redmi Note 10 Pro / MIUI) | Home: the settings (appearance, layout direction, tint; on Android also Glass/Material, glass options) and the list of all 21 screens | `GlassHomeScreen` (screen id `home`), default settings
- `android-glass/interactive.png` | Android, androidStyle 'glass' (Compose, Redmi Note 10 Pro / MIUI) | Interactive Glass: a press-and-hold glass shape, a tap counter button, static vs interactive | `InteractiveGlassScreen` (screen id `interactive`), default settings
- `android-glass/morphing.png` | Android, androidStyle 'glass' (Compose, Redmi Note 10 Pro / MIUI) | Morphing: a + button that morphs a column of tools out of itself | `GlassMorphingScreen` (screen id `morphing`), default settings
- `android-glass/recipes.png` | Android, androidStyle 'glass' (Compose, Redmi Note 10 Pro / MIUI) | Recipes: sliding selector, map controls, mini player, floating action menu | `GlassRecipesScreen` (screen id `recipes`), default settings
- `android-glass/scrollEdge.png` | Android, androidStyle 'glass' (Compose, Redmi Note 10 Pro / MIUI) | Scroll Edge & Bars (bar title 'Scroll Edge'): filter chips, coloured rows, Auto/Soft/Hard/Hidden edge-effect picker | `GlassScrollEdgeScreen` (screen id `scrollEdge`), default settings
- `android-glass/search.png` | Android, androidStyle 'glass' (Compose, Redmi Note 10 Pro / MIUI) | Search: a fruit list with a minimized glass search button in the bottom bar | `GlassSearchScreen` (screen id `search`), default settings
- `android-glass/sheets.png` | Android, androidStyle 'glass' (Compose, Redmi Note 10 Pro / MIUI) | Sheets & Menus: Show Sheet, Show Popover and a glass menu button | `GlassSheetsScreen` (screen id `sheets`), default settings
- `android-glass/splitView.png` | Android, androidStyle 'glass' (Compose, Redmi Note 10 Pro / MIUI) | Sidebar & Inspector (bar title 'Split View'): explanation card and Open Split View button | `GlassSplitViewScreen` (screen id `splitView`), default settings
- `android-glass/tabBar.png` | Android, androidStyle 'glass' (Compose, Redmi Note 10 Pro / MIUI) | Tab Bar: launcher for the floating glass tab bar demos (now playing bar, search inside tabs, search separated, tabs without search), minimize-on-scroll toggle | `GlassTabBarScreen` (screen id `tabBar`), default settings
- `android-glass/text.png` | Android, androidStyle 'glass' (Compose, Redmi Note 10 Pro / MIUI) | Text: glass letterforms, large title on glass, tinted headline, paragraph card, tags | `GlassTextScreen` (screen id `text`), default settings
- `android-glass/toolbar.png` | Android, androidStyle 'glass' (Compose, Redmi Note 10 Pro / MIUI) | Toolbar: large title, glass toolbar buttons (heart, share, more), options list, bottom toolbar and floating compose button | `GlassToolbarScreen` (screen id `toolbar`), default settings
- `android-glass/transitions.png` | Android, androidStyle 'glass' (Compose, Redmi Note 10 Pro / MIUI) | Glass Transitions: Matched/Materialize/Identity picker and a Show Badges button | `GlassTransitionsScreen` (screen id `transitions`), default settings
- `android-glass/union.png` | Android, androidStyle 'glass' (Compose, Redmi Note 10 Pro / MIUI) | Union: day and night symbols grouped into two glass unions, padding slider | `GlassUnionScreen` (screen id `union`), default settings
- `android-glass/zoomTransitions.png` | Android, androidStyle 'glass' (Compose, Redmi Note 10 Pro / MIUI) | Zoom Transitions (no bar title): Online status, inbox button with badge, New Message button, unread badge stepper | `GlassZoomTransitionsScreen` (screen id `zoomTransitions`), default settings

## android-material/

- `android-material/alerts.png` | Android, androidStyle 'material' (Compose Material 3, Redmi Note 10 Pro / MIUI) | Alerts & Dialogs: Show Alert, Show Confirmation Dialog, long-press artwork for a context menu | `GlassAlertsScreen` (screen id `alerts`), default settings
- `android-material/buttons.png` | Android, androidStyle 'material' (Compose Material 3, Redmi Note 10 Pro / MIUI) | Buttons: glass, glass prominent, clear, tinted prominent, large with icon, media buttons row, disabled | `GlassButtonsScreen` (screen id `buttons`), default settings
- `android-material/container.png` | Android, androidStyle 'material' (Compose Material 3, Redmi Note 10 Pro / MIUI) | Glass Container: two glass buttons with 'Container spacing' and 'Pull together' sliders that merge them | `GlassContainerScreen` (screen id `container`), default settings
- `android-material/controls.png` | Android, androidStyle 'material' (Compose Material 3, Redmi Note 10 Pro / MIUI) | Controls: slider, toggle, segmented picker, steppers, menu picker, date and colour pickers | `GlassControlsScreen` (screen id `controls`), default settings
- `android-material/editSearch.png` | Android, androidStyle 'material' (Compose Material 3, Redmi Note 10 Pro / MIUI) | Edit & Search Buttons (bar title 'Notes'): a notes list with glass edit and search buttons | `GlassEditSearchScreen` (screen id `editSearch`), default settings
- `android-material/floatingHeader.png` | Android, androidStyle 'material' (Compose Material 3, Redmi Note 10 Pro / MIUI) | Floating Header: a colourful header with glass bar buttons, scroll-edge style picker (Status Bar/Soft/None), rows | `GlassFloatingHeaderScreen` (screen id `floatingHeader`), default settings
- `android-material/glassEffect.png` | Android, androidStyle 'material' (Compose Material 3, Redmi Note 10 Pro / MIUI) | Glass Effect: default capsule, clear variant, tinted glass (orange/blue/green), custom shapes (circle, rounded, rect) | `GlassEffectScreen` (screen id `glassEffect`), default settings
- `android-material/glassView.png` | Android, androidStyle 'material' (Compose Material 3, Redmi Note 10 Pro / MIUI) | Glass View: a glass card over a photo with live options (Regular/Clear, shape, corner radius, interactive, tint) | `GlassViewScreen` (screen id `glassView`), default settings
- `android-material/home.png` | Android, androidStyle 'material' (Compose Material 3, Redmi Note 10 Pro / MIUI) | Home: the settings (appearance, layout direction, tint; on Android also Glass/Material, glass options) and the list of all 21 screens | `GlassHomeScreen` (screen id `home`), default settings
- `android-material/interactive.png` | Android, androidStyle 'material' (Compose Material 3, Redmi Note 10 Pro / MIUI) | Interactive Glass: a press-and-hold glass shape, a tap counter button, static vs interactive | `InteractiveGlassScreen` (screen id `interactive`), default settings
- `android-material/morphing.png` | Android, androidStyle 'material' (Compose Material 3, Redmi Note 10 Pro / MIUI) | Morphing: a + button that morphs a column of tools out of itself | `GlassMorphingScreen` (screen id `morphing`), default settings
- `android-material/recipes.png` | Android, androidStyle 'material' (Compose Material 3, Redmi Note 10 Pro / MIUI) | Recipes: sliding selector, map controls, mini player, floating action menu | `GlassRecipesScreen` (screen id `recipes`), default settings
- `android-material/scrollEdge.png` | Android, androidStyle 'material' (Compose Material 3, Redmi Note 10 Pro / MIUI) | Scroll Edge & Bars (bar title 'Scroll Edge'): filter chips, coloured rows, Auto/Soft/Hard/Hidden edge-effect picker | `GlassScrollEdgeScreen` (screen id `scrollEdge`), default settings
- `android-material/search.png` | Android, androidStyle 'material' (Compose Material 3, Redmi Note 10 Pro / MIUI) | Search: a fruit list with a minimized glass search button in the bottom bar | `GlassSearchScreen` (screen id `search`), default settings
- `android-material/sheets.png` | Android, androidStyle 'material' (Compose Material 3, Redmi Note 10 Pro / MIUI) | Sheets & Menus: Show Sheet, Show Popover and a glass menu button | `GlassSheetsScreen` (screen id `sheets`), default settings
- `android-material/splitView.png` | Android, androidStyle 'material' (Compose Material 3, Redmi Note 10 Pro / MIUI) | Sidebar & Inspector (bar title 'Split View'): explanation card and Open Split View button | `GlassSplitViewScreen` (screen id `splitView`), default settings
- `android-material/tabBar.png` | Android, androidStyle 'material' (Compose Material 3, Redmi Note 10 Pro / MIUI) | Tab Bar: launcher for the floating glass tab bar demos (now playing bar, search inside tabs, search separated, tabs without search), minimize-on-scroll toggle | `GlassTabBarScreen` (screen id `tabBar`), default settings
- `android-material/text.png` | Android, androidStyle 'material' (Compose Material 3, Redmi Note 10 Pro / MIUI) | Text: glass letterforms, large title on glass, tinted headline, paragraph card, tags | `GlassTextScreen` (screen id `text`), default settings
- `android-material/toolbar.png` | Android, androidStyle 'material' (Compose Material 3, Redmi Note 10 Pro / MIUI) | Toolbar: large title, glass toolbar buttons (heart, share, more), options list, bottom toolbar and floating compose button | `GlassToolbarScreen` (screen id `toolbar`), default settings
- `android-material/transitions.png` | Android, androidStyle 'material' (Compose Material 3, Redmi Note 10 Pro / MIUI) | Glass Transitions: Matched/Materialize/Identity picker and a Show Badges button | `GlassTransitionsScreen` (screen id `transitions`), default settings
- `android-material/union.png` | Android, androidStyle 'material' (Compose Material 3, Redmi Note 10 Pro / MIUI) | Union: day and night symbols grouped into two glass unions, padding slider | `GlassUnionScreen` (screen id `union`), default settings
- `android-material/zoomTransitions.png` | Android, androidStyle 'material' (Compose Material 3, Redmi Note 10 Pro / MIUI) | Zoom Transitions (no bar title): Online status, inbox button with badge, New Message button, unread badge stepper | `GlassZoomTransitionsScreen` (screen id `zoomTransitions`), default settings

## settings/

- `settings/android-glass-buttons-rtl.png` | Android glass | Buttons in RTL: mirrored (back arrow on the right) except the media buttons row, which stays LTR | layoutDirection 'rtl'; media players stay LTR
- `settings/android-glass-custom-buttons.png` | Android glass | Buttons with the Cairo font and custom icons | fontFamily + icons (vectorIcon, svg, PNG)
- `settings/android-glass-custom-home.png` | Android glass | Home with CUSTOMIZE = true: the Cairo font replaces the phone's italic MIUI font on every label (the custom row icons are further down the list) | fontFamily (per weight)
- `settings/android-glass-custom-tabBar-demo.png` | Android glass | Tab bar demo (With Now Playing Bar) with the Cairo font and custom tab icons | fontFamily + icons on the tab bar
- `settings/android-glass-custom-tabBar.png` | Android glass | Tab Bar launcher with the Cairo font | fontFamily
- `settings/android-glass-home-dark.png` | Android glass | Home with appearance Dark | settings.appearance = 'dark'
- `settings/android-glass-home-rtl.png` | Android glass | Home with layout direction RTL: whole screen mirrored | settings.layoutDirection = 'rtl'
- `settings/android-glass-recipes-rtl.png` | Android glass | Recipes in RTL: the mini player stays LTR | layoutDirection 'rtl'; media players stay LTR
- `settings/android-material-controls-glow.png` | Android material | Controls with control shape Glow (default intensity 0.35) | androidControlShape 'glow'
- `settings/android-material-controls-labeled.png` | Android material | Controls with control shape Labeled | androidControlShape 'labeled'
- `settings/android-material-controls-pill.png` | Android material | Controls with control shape Pill (default): Material switch, slider, segmented button | androidControlShape 'pill'
- `settings/android-material-home-glow.png` | Android material | Material Home with control shape Glow picked: the Pill/Labeled/Glow picker and the Glow intensity slider | androidControlShape 'glow', androidGlowIntensity (default 0.35)
- `settings/ios-buttons-rtl.png` | iOS | Buttons in RTL: everything mirrored (back chevron on the right) except the play/pause/forward row, which stays LTR | layoutDirection 'rtl'; media players stay LTR
- `settings/ios-custom-buttons.png` | iOS | Buttons with the Cairo font and custom icons (SVG star on Large, Ionicons play, PNG target for forward, SVG back chevron) | fontFamily + icons (vectorIcon, svg, PNG)
- `settings/ios-custom-home.png` | iOS | Home with CUSTOMIZE = true: Cairo font on every label, custom row icons (Ionicons water drop, SVG star for Morphing) | fontFamily (per weight) + icons
- `settings/ios-custom-tabBar-demo.png` | iOS | Tab bar demo (With Now Playing Bar) with the Cairo font and custom tab icons (Ionicons home, PNG target for Library) | fontFamily + icons on the tab bar
- `settings/ios-custom-tabBar.png` | iOS | Tab Bar launcher with the Cairo font | fontFamily
- `settings/ios-home-dark.png` | iOS | Home with appearance Dark (Home's Dark segment; the simulator was in dark mode too, see report) | settings.appearance = 'dark'
- `settings/ios-home-rtl.png` | iOS | Home with layout direction RTL: whole screen mirrored | settings.layoutDirection = 'rtl'
- `settings/ios-recipes-rtl.png` | iOS | Recipes in RTL: selector order mirrored, the mini player stays LTR | layoutDirection 'rtl'; media players stay LTR

## gif/

- `gif/android-glass-controls-liquid.gif` | Android glass | GlassControlsScreen: dragging the liquid slider and flipping the liquid switch | GlassControlsScreen, Glass keeps liquid controls
- `gif/android-glass-tabBar-minimize.gif` | Android glass | GlassTabBarScreen: scrolling the tab demo (With Now Playing Bar): the tab bar minimizes on scroll and returns | GlassTabBarScreen, minimize on scroll
- `gif/android-glass-tabBar-switch.gif` | Android glass | GlassTabBarScreen: switching tabs Library, Profile, Home in the floating tab bar | GlassTabBarScreen
- `gif/android-material-controls-glow.gif` | Android material | GlassControlsScreen: Material Controls with control shape Glow: dragging the slider and flipping the switch | androidControlShape 'glow'
- `gif/ios-container-merge.gif` | iOS | GlassContainerScreen: dragging 'Pull together' merges the two glass buttons into one shape, dragging back splits them | GlassContainerScreen / glass container merge
- `gif/ios-floatingHeader-scroll.gif` | iOS | GlassFloatingHeaderScreen: scrolling the list under the floating header and back | GlassFloatingHeaderScreen
- `gif/ios-interactive-press.gif` | iOS | InteractiveGlassScreen: pressing and holding the interactive glass shape, then tapping the counter button | InteractiveGlassScreen / interactive glass
- `gif/ios-morphing-open-close.gif` | iOS | GlassMorphingScreen: tapping + morphs the tools out of the button, tapping X folds them back | GlassMorphingScreen
- `gif/ios-recipes-player-expand.gif` | iOS | GlassRecipesScreen: tapping the mini player expands it (progress + controls), tapping again collapses it | GlassRecipesScreen mini player
- `gif/ios-search-open-close.gif` | iOS | GlassSearchScreen: tapping the minimized search button expands the field, typing 'Ch' filters, closing collapses it | GlassSearchScreen
- `gif/ios-sheets-sheet.gif` | iOS | GlassSheetsScreen: Show Sheet: a partial-height glass sheet opens and is dismissed | GlassSheetsScreen
- `gif/ios-tabBar-minimize.gif` | iOS | GlassTabBarScreen: scrolling the tab demo (With Now Playing Bar): the tab bar minimizes on scroll and returns | GlassTabBarScreen, minimize on scroll
- `gif/ios-tabBar-switch.gif` | iOS | GlassTabBarScreen: switching tabs Library, Profile, Home in the floating tab bar | GlassTabBarScreen
- `gif/ios-transitions-badges.gif` | iOS | GlassTransitionsScreen: Show Badges: badges grow out of the avatar glass, Hide Badges folds them back | GlassTransitionsScreen (Matched)
- `gif/ios-zoomTransitions-sheet.gif` | iOS | GlassZoomTransitionsScreen: New Message: the sheet zooms out of the button, then is dismissed back into it | GlassZoomTransitionsScreen

Total size of the media: 14.8 MB.

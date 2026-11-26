# AGENTS.md

## Using this library in an app

Read [CONTEXT.md](CONTEXT.md) first: the API, the data flow, the rules, the React Navigation recipe and every known gotcha. To explain a component to a user, use [USAGE.md](USAGE.md). The human guides are in [docs/](docs) (on GitHub; not in the npm package).

The short version: 22 whole native screens, no children, no primitives; `style={{ flex: 1 }}` on every screen; `headerShown: false`; feed Home's `onSettingsChange` back into a memoised `GlassSettingsProvider` value; `onBack` → `navigation.goBack`; on iOS `appearance` is window-wide.

## Working on this repository

A Yarn 4 workspace: the library at the root, the example app in `example/` (React Native CLI, New Architecture). CONTEXT.md section 2 maps every file.

- `src/`: the JavaScript API (`screens.tsx`, `settings.tsx`, `types.ts`, `icons.ts`) and the codegen spec (`LiquidGlassScreenNativeComponent.ts`).
- `ios/Catalog/`: the iOS app's SwiftUI screens. `ios/Host/` (`LiquidGlassScreenHost.swift`, `GlassFont.swift`, `GlassIcon.swift`) and `ios/RNLiquidGlassScreen.mm`: the Fabric host. `ios/Tests/`: XCTest.
- `android/src/main/java/com/mohamedabdelnaby/liquidglass/catalog/`: the Android app's Compose screens (glass and Material). `host/`, `LiquidGlassScreenView.kt`, `LiquidGlassScreenManager.kt`, `FontFamily.kt`, `GlassIcons.kt`: the Fabric host (back routing, bar icons, fonts, icons).
- Keep each `GLASS_SCREENS` entry on one line as `{ id: '…', title: '…' }`: an Android unit test reads the ids with a regex.

Commands, from the repository root:

```sh
yarn                               # install
yarn lint && yarn typecheck && yarn test
yarn example start                 # Metro on 8081
yarn example ios
yarn example android
```

Native tests:

```sh
# Android JVM and Robolectric tests (CI runs these), and a release build of the example
cd example/android && ./gradlew :native-dev-kit_react-native-liquid-glass:testDebugUnitTest :app:assembleRelease

# iOS XCTest (after `cd example/ios && LANG=en_US.UTF-8 LC_ALL=en_US.UTF-8 pod install`;
# the prebuilt pods must be in their Debug flavour, see CONTRIBUTING.md)
cd example/ios && xcodebuild test -workspace ReactNativeLiquidGlassExample.xcworkspace \
  -scheme ReactNativeLiquidGlass-Unit-Tests -destination 'platform=iOS Simulator,name=iPhone 18 Pro'
```

Rules:

- Commit messages follow Conventional Commits (commitlint runs in a hook). Do not use `--no-verify`.
- Change docs with the code: when an API, prop, default, behaviour or platform floor changes, update `README.md`, `USAGE.md`, `docs/`, `CONTEXT.md`, `llms.txt` and `skills/react-native-liquid-glass/SKILL.md` in the same change.
- Every `ts`/`tsx` sample in README.md, USAGE.md and `docs/` must type-check against `src/`.
- README images use absolute `raw.githubusercontent.com/…/main/docs/assets/…` URLs (npm renders only the README); other docs use relative paths. `docs/` stays out of the npm package.
- Check on devices with Release builds. Never leave another project's Metro on 8081.
- Read CONTEXT.md section 16 ("Never do") before changing native code.

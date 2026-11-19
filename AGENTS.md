# AGENTS.md

## Using this library in an app

Read [CONTEXT.md](CONTEXT.md). It has the mental model, the exact API, the React Navigation recipe, the rules and the common mistakes. The guides are in [docs/](docs).

## Working on this repository

A Yarn 4 workspace: the library at the root, the example app in `example/` (React Native CLI, New Architecture).

- `src/`: the JavaScript API (`screens.tsx`, `settings.tsx`, `types.ts`) and the codegen spec (`LiquidGlassScreenNativeComponent.ts`).
- `ios/Catalog/`: the iOS app's SwiftUI screens, copied unchanged except the edits listed in the task reports. `ios/Host/` and `ios/RNLiquidGlassScreen.mm`: the Fabric host.
- `android/src/main/java/com/mohamedabdelnaby/liquidglass/catalog/`: the Android app's Compose screens. `host/` and `LiquidGlassScreenView.kt`: the Fabric host (back routing, bar icons).
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
# Android JVM tests and a release build of the example
cd example/android && ./gradlew :native-dev-kit_react-native-liquid-glass:testDebugUnitTest :app:assembleRelease

# iOS XCTest (after `cd example/ios && LANG=en_US.UTF-8 LC_ALL=en_US.UTF-8 pod install`)
cd example/ios && xcodebuild test -workspace ReactNativeLiquidGlassExample.xcworkspace \
  -scheme ReactNativeLiquidGlass-Unit-Tests -destination 'platform=iOS Simulator,name=iPhone 18 Pro'
```

Rules:

- Commit messages follow Conventional Commits (commitlint runs in a hook). Do not use `--no-verify`.
- Change docs with the code: when an API, prop, default or platform floor changes, update `README.md`, `docs/`, `CONTEXT.md`, `llms.txt` and `skills/react-native-liquid-glass/SKILL.md` in the same change.
- Every code sample in the docs must type-check against `src/` (except the ones marked as planned API).

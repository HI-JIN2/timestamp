# Timestamp

`Timestamp` is a Compose Multiplatform application for Android and iOS.

The app lets users choose a photo and apply a retro timestamp that looks like the timestamp printed on developed photographs.

## Product Direction

- Production-oriented mobile app
- Android and iOS support from the beginning
- Maintainable structure for ongoing GitHub-based development
- Modern stable stack and clear project conventions

## Planned Scope

- Import a photo from the device
- Add a retro timestamp overlay
- Adjust timestamp style for a film-photo feel
- Export or save the edited result

## Development Rules

- Start with a private GitHub repository
- Record progress in small commits by work unit
- Use commit messages such as `feat: 00 사진 선택 기능 추가`
- Keep descriptions detailed enough to understand intent quickly

## Next Step

Initialize the Compose Multiplatform project baseline and then build the photo selection and timestamp rendering features incrementally.

## Current Baseline

- `composeApp`: shared Compose UI and Android application target
- `shared`: reusable preview/domain model for feature growth
- `iosApp`: SwiftUI host app that embeds the shared Compose view

## Local Requirements

- JDK 17
- Android SDK installed locally
- Full Xcode installation required for iOS build and test execution

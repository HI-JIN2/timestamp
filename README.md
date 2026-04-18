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
- Use commit messages such as `feat: 사진 선택 기능 추가`
- Keep descriptions detailed enough to understand intent quickly

## Next Step

Initialize the Compose Multiplatform project baseline and then build the photo selection and timestamp rendering features incrementally.

## Current Baseline

- `composeApp`: app shell and platform integration layer
- `feature/editor`: editor screen UI and MVVM + MVI presentation layer
- `domain/editor`: editor use cases and pure business rules
- `core/model`: shared cross-feature models
- `iosApp`: SwiftUI host app that embeds the shared Compose view

## Architecture

- Clean Architecture baseline
- Multi-module project structure
- MVVM + MVI presentation model
- Platform code isolated to app shell and native host layers
- Shared Gradle rules managed through custom convention plugins in `build-logic`

## Local Requirements

- JDK 17
- Android SDK installed locally
- Full Xcode installation required for iOS build and test execution

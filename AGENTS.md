# Timestamp Agent Guide

## Product Summary

- App type: Compose Multiplatform mobile application
- Platforms: Android, iOS
- Core feature: Select a photo and add a retro photo-print timestamp overlay
- Product goal: Production-ready app for release on Google Play

## Engineering Principles

- Prefer the latest stable technologies that improve long-term maintainability
- Keep platform-specific code minimal and isolate it clearly
- Favor small, reviewable commits for each work unit
- Preserve a clean architecture that supports feature extension and testing
- Use Clean Architecture as the default baseline
- Structure the project as a multi-module codebase
- Use MVVM + MVI in presentation layers

## Architecture Rules

- `composeApp` is the app shell and platform entry layer only
- `feature/*` owns presentation UI, screen state, intents, and view models
- `domain/*` owns use cases and pure business rules
- `core/*` owns reusable cross-feature models and primitives
- Avoid placing feature state machines or business logic directly in platform activities or SwiftUI hosts
- Keep Android/iOS-specific IO, permission, media, and export code at the edge of the system

## Initial Technical Direction

- Kotlin
- Compose Multiplatform
- Shared UI and domain logic where practical
- Clear module boundaries from the start
- Multi-module layering with app shell, feature, domain, and core modules

## Git Workflow

- Start from a private GitHub repository
- Leave a commit for each work unit
- Commit message format:
  - `<type>: 작업 내용`
  - Example: `fix: 타임스탬프 위치 계산 오류 수정`
- Use English for the commit type and concise Korean for the description

## Build Conventions

- Keep repeated Gradle setup in custom convention plugins under `build-logic`
- Prefer module build scripts that declare only module-specific dependencies and exceptions

## Expected Early Milestones

1. Initialize repository and baseline documentation
2. Bootstrap Compose Multiplatform project structure
3. Implement photo selection flow on Android and iOS
4. Implement retro timestamp rendering pipeline
5. Prepare store-release quality, testing, and CI basics

# Everyday Mobile Fixes

Small Android and iOS apps. Each one shows a single problem that real users run into in everyday apps, and the fix.

Every folder is one small app with:
- the problem, and how to see it on a device or emulator
- the cause, in one or two lines
- the fix, with the few lines of code that matter
- a link to the official documentation

Some folders show a brand-new API in a small demo instead of a fix; the table says so.

## Fixes

| # | Problem | Platform | Folder |
|---|---------|----------|--------|
| 01 | Deleting a list row moves its note to the next row | Android (Jetpack Compose) | [01-grocery-notes](01-grocery-notes) |
| 02 | A thumbnail grid decodes full-size photos: 48 MB per tile, 576 MB for 12 | Android (Jetpack Compose) | [02-gallery-grid](02-gallery-grid) |
| 03 | One SwiftUI modifier hides the Send button behind the keyboard | iOS (SwiftUI) | [03-reply-bar](03-reply-bar) |
| 04 | A fast double tap on a list row opens the same detail screen twice | Android (Jetpack Compose, Navigation 3) | [04-notice-board](04-notice-board) |
| 05 | New API demo: a circle morphs into a star while the sky blurs (PolygonShape, progressive blur) | Android (Jetpack Compose 1.13) | [05-wish-star](05-wish-star) |
| 06 | Spot the bug: an autosave runs every 5 s but always saves the first, empty text (LaunchedEffect, rememberUpdatedState) | Android (Jetpack Compose) | [06-draft-saver](06-draft-saver) |

## Stack

- Android: Kotlin, Jetpack Compose
- iOS: Swift, SwiftUI

## Author

Manish Mewada, Android Developer, Pune, India

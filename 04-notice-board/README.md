# 04 - Notice Board

Tap a notice twice, fast, and the detail screen opens twice. The back stack holds the list and two copies of the same
notice, so the first Back press shows the same notice again.

This small app recreates it on purpose and shows the fix next to it. A switch at the top flips between the two, and a chip
shows the size of the back stack on both screens. The notices are made up (an apartment society's board).

| | |
|---|---|
| Language / UI | Kotlin 2.2, Jetpack Compose (Material 3), one Activity |
| Navigation | Navigation 3 (`navigation3-runtime` and `navigation3-ui` 1.2.2), back stack is a plain list |
| Min / target SDK | 26 / 36 |
| Build | Gradle 9.7.1 wrapper, AGP 9.4 |

## The problem

Navigation 3 animates between screens. With its default transition the new screen fades in over 700 ms, and the old screen
stays on screen, and clickable, during that time. A second tap on the same row calls `backStack.add(...)` a second time:

```
tap 1 -> back stack: list, notice
tap 2 -> back stack: list, notice, notice     (0.2 s later, the list is still there)
```

Anyone with a list that opens a detail screen, a payment screen or a camera can run into this. The Up arrow has the same
problem in the other direction: two fast taps pop the stack twice, and Navigation 3 throws
`NavDisplay backstack cannot be empty`.

## How to see it

1. Install the debug build and open the app. The switch is off (BEFORE, plain click).
2. Tap the first notice twice, fast. The detail screen opens and the chip reads `Back stack: 3`.
3. Tap `< Back`: the same notice is shown again, the chip reads 2. A second `< Back` shows the list.
4. Flip the switch to AFTER and repeat: the chip reads 2, and one `< Back` shows the list.

To start straight in AFTER from a script:

```
adb shell am start -n io.github.binarystation.noticeboard/.MainActivity --ez guarded true
```

`scripts/` has the adb scripts used for the numbers below (tap coordinates are for a 1080 x 2400 emulator).

## The cause

While a transition runs, the screen is not RESUMED. Navigation 3 caps the lifecycle of the screens in a transition at
STARTED (see "Destination lifecycle" in the Navigation 3 documentation). A plain click does not look at the lifecycle,
so it runs for every tap.

## The fix

Wrap the click in `dropUnlessResumed`. It runs the block only when the lifecycle is at least RESUMED and ignores the tap
otherwise:

```kotlin
// before
onClick = { backStack.add(NoticeDetail(id)) }

// after
onClick = dropUnlessResumed { backStack.add(NoticeDetail(id)) }
```

In `NoticeList.kt` the row click and in `NoticeDetail.kt` the Up arrow are wrapped the same way when the switch is on.
The documentation says it plainly: "For Navigation users, it's recommended to safeguard navigate methods when using them
while a composable is in transition as a result of navigation."

## Measured numbers

All numbers come from this app on an Android 17 (API 37) arm64 emulator, not on a phone. The UI tests run with the test
clock paused (`mainClock.autoAdvance = false`), so the gap between the two taps is exactly what the test sets.

| | BEFORE (plain click) | AFTER (`dropUnlessResumed`) |
|---|---|---|
| Back stack after one double tap (list = 1) | 3 | 2 |
| Back presses to reach the list | 2 | 1 |
| UI test, gaps of 16, 50, 100, 150, 200, 300, 500 and 650 ms | 8 of 8: stack 3, 2 Back presses | 8 of 8: stack 2, 1 Back press |
| Emulator, two `input tap` calls about 0.2 s apart, 10 cold starts each | 10 of 10: stack 3 | 10 of 10: stack 2 |
| Up arrow, two taps about 0.2 s apart, 5 cold starts each | 5 of 5: crash `NavDisplay backstack cannot be empty` | 5 of 5: one pop, the list, no crash |
| Two taps inside one frame (test clock, gap 0) | stack 3 | stack 3 |
| Two `input tap` calls back to back (about 35 ms apart), 10 each | 10 of 10: stack 3 | 10 of 10: stack 3 |

- The guard works from the moment the first tap has been drawn, not before. Two taps in the same frame, or about 35 ms
  apart on this emulator, both reach the click handler while the screen still counts as RESUMED. I did not measure how far
  apart real double taps are, so this table makes no claim about people.
- The emulator numbers read the chip text with `uiautomator dump` after each run (`scripts/measure-double-tap.sh`,
  `scripts/measure-up-arrow.sh`). The gap is the time of the shell calls, so read it as "about 0.2 s", not as exact.
- The crash shows in `adb logcat` as `java.lang.IllegalArgumentException: NavDisplay backstack cannot be empty`. A test
  cannot assert it, because it ends the process, so the AFTER side of the Up arrow is the one under test.

## Where to look

- `NoticeBoardApp.kt`: the back stack (a `mutableStateListOf`), `NavDisplay`, the entry provider and the BEFORE / AFTER flag.
- `NoticeList.kt`: the row click, `if (guarded) dropUnlessResumed { ... } else { ... }`, the mode card.
- `NoticeDetail.kt`: the notice, the Up arrow with the same guard.
- `BackStackChip.kt`: the `Back stack: N` chip.
- `Notice.kt`: the six made-up notices.

## Build and test

```
./gradlew testDebugUnitTest            # 6 unit tests on the notice data
./gradlew assembleDebug                # app/build/outputs/apk/debug/app-debug.apk
./gradlew connectedDebugAndroidTest    # 5 tests on a device or emulator
```

- `NoticesTest` (6) is plain JVM.
- `DoubleTapTest` (4): one tap opens one notice in both modes; BEFORE double taps give stack 3 and 2 Back presses for
  8 gaps; AFTER gives stack 2 and 1 Back press for the same gaps; two taps in the same frame get through in both modes.
- `UpArrowTest` (1): AFTER, a double tap on the Up arrow pops once and shows the list.

Needs the Android SDK (`local.properties` with `sdk.dir`) and JDK 17 or newer.

## Notes

- Why a plain Column on the detail screen: in a first version the detail screen was a Material `Surface`. A Surface
  swallows touches as soon as it is on screen, so the second tap only got through at a 16 ms gap (1 of 6 gaps tested) and
  the bug was much harder to see. A `Scaffold` contains a Surface, so I expect the same there, but I did not test it. Whether your app shows the bug depends
  on what the new screen is made of and on the transition you use.
- The back stack lives in memory only, so rotating the device resets it. That is enough for this demo.
- This README describes Navigation 3 1.2.2; a later release may behave differently.
- The UI test pins the alpha `espresso-core:3.8.0-alpha04`. With the Espresso version that comes along with the Compose
  test library, UI tests crashed on an Android 17 (API 37) emulator because `InputManager.getInstance()` is missing
  there; the alpha runs fine.

## Documentation

- [dropUnlessResumed](https://developer.android.com/reference/kotlin/androidx/lifecycle/compose/dropUnlessResumed.composable)
- [Events in the UI layer (dropUnlessResumed in a Button)](https://developer.android.com/topic/architecture/ui-layer/events)
- [Navigation 3 basics (destination lifecycle)](https://developer.android.com/guide/navigation/navigation-3/basics)

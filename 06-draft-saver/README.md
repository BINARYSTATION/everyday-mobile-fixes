# 06 - Draft Saver

A postcard editor that autosaves every 5 seconds. The save runs on time, every time, and stores an empty postcard. The
editor shows the text you typed; the saved copy says `(empty)`.

This small app recreates the bug on purpose and shows the fix next to it. A switch at the top flips between the two, and
a card under the editor shows the last saved text and how many saves have run. The postcard text is made up.

| | |
|---|---|
| Language / UI | Kotlin 2.2, Jetpack Compose 1.13 (BOM 2026.11.01, Material 3), one Activity |
| APIs | `LaunchedEffect`, `rememberUpdatedState` (compose-runtime) |
| Min / target / compile SDK | 26 / 37 / 37.1 |
| Build | Gradle 9.7.1 wrapper, AGP 9.4 |

## Spot the bug

```kotlin
@Composable
fun AutoSave(
    text: String,
    onSave: (String) -> Unit,
) {
    LaunchedEffect(Unit) {
        while (true) {
            delay(5_000)
            onSave(text)
        }
    }
}
```

The editor calls `AutoSave(text = draft, ...)` and recomposes on every key press. Answer below.

## The cause

`LaunchedEffect(Unit)` starts its block once, on the first composition, and keeps that block running until it leaves
the composition. The block is a Kotlin lambda, so it holds the values it captured when it was created. On the first
composition `text` was `""`.

```
first composition   text = ""              -> the effect starts the block, the block holds ""
every key press     text = "D" ... "Day 2" -> a new block is built, the key is still Unit, so it is ignored
5 s, 10 s, ...                             -> onSave("")
```

The documentation's warning fits: "`LaunchedEffect(true)` is as suspicious as a `while(true)`." This snippet has both.

## The fix

Read the value through `rememberUpdatedState`. It returns one `State` object for the life of the call site and writes
the newest value into it on every composition. The loop captures the `State`, so each save reads the current text, and
the effect never restarts:

```kotlin
@Composable
fun AutoSave(
    text: String,
    onSave: (String) -> Unit,
) {
    val latest by rememberUpdatedState(text)
    val save by rememberUpdatedState(onSave)
    LaunchedEffect(Unit) {
        while (true) {
            delay(5_000)
            save(latest)
        }
    }
}
```

The documentation states the rule: "Variables used in an effect should be added as a parameter of the effect
composable, or use `rememberUpdatedState`."

`onSave` is wrapped the same way, as in the documentation's `LandingScreen` example. In this app the callback reads its
state when it runs, so the old one would still work; the tests do not cover that line.

### Why not `LaunchedEffect(text)`?

Keying the effect on `text` also gets the newest text into the block, but every key press cancels the loop and starts a
new 5 second wait. While someone keeps typing, nothing is saved; 5 seconds after the last key press it saves once. That
is a debounce. It can be the right behavior, but it is not "save every 5 seconds".

## How to see it

1. Install the debug build and open the app. The switch is off (BEFORE, plain capture).
2. Type something and wait 10 seconds. `Saves:` goes up every 5 seconds, and the saved copy reads `(empty)`.
3. Flip the switch to AFTER (this starts a fresh screen), type again and wait: the saved copy shows your text.

To start straight in AFTER from a script:

```
adb shell am start -n io.github.binarystation.draftsaver/.MainActivity --ez fixed true
```

## Measured

| | BEFORE (plain capture) | AFTER (`rememberUpdatedState`) |
|---|---|---|
| UI test: type "Day 2 in Goa", then 10 s on the test clock | saves `""`, `""` | saves `"Day 2 in Goa"`, `"Day 2 in Goa"` |
| Emulator: type, read the saved copy 12 s after opening, 5 cold starts each | 5 of 5: `Saves: 2`, `(empty)` | 5 of 5: `Saves: 2`, `Day 2 in Goa` |

| One key per second for 12 s (UI test, test clock) | `rememberUpdatedState` | keyed on `text` |
|---|---|---|
| Saves while typing | 2: `"Day 2"`, `"Day 2 in G"` | 0 |
| 5 s after the last key | 1 more: `"Day 2 in Goa"` | 1: `"Day 2 in Goa"` |

- The UI tests run with the test clock paused (`mainClock.autoAdvance = false`), so `delay(5_000)` and the typing
  rhythm follow the test clock exactly. In the typing test the keys land half a second away from each save tick, so
  each save sees an exact prefix.
- The emulator runs are wall-clock: `scripts/measure-autosave.sh` types with `adb shell input text` about 4 s after
  launch and reads the screen with `uiautomator dump` 12 s after launch, one line per run. Read the timing as
  "about", not exact.
- All numbers come from an Android 17 (API 37) arm64 emulator, not a phone.

## Where to look

- `AutoSave.kt`: `AutoSaveBuggy` and `AutoSaveFixed`, the two versions of the effect.
- `DraftScreen.kt`: the editor, the BEFORE / AFTER switch and the saved copy card.
- `SaveLog.kt`: the list of saved texts and what the card shows, as plain Kotlin (unit tested).
- `AutoSaveTest.kt` (androidTest): the three UI tests, including the keyed-on-`text` version used for comparison.

## Build and test

```
./gradlew testDebugUnitTest            # 5 unit tests
./gradlew assembleDebug                # app/build/outputs/apk/debug/app-debug.apk
./gradlew connectedDebugAndroidTest    # 3 tests on a device or emulator
```

- `SaveLogTest` (5): empty log, saves kept in order, blank saves show as empty, text shows, the card follows the newest
  save.
- `AutoSaveTest` (3): BEFORE saves the empty first text twice; AFTER saves the typed text twice; steady typing (fixed
  keeps saving, keyed on `text` saves nothing until the typing stops).

Needs the Android SDK with platform 37.1, `local.properties` with `sdk.dir`, and JDK 17 or newer.

## Notes

- Same build setup and pins as `05-wish-star`. The app only uses stable runtime APIs, so it does not need Compose 1.13;
  the pins are shared so the repo builds one way.
- The draft lives in `remember`, so rotating the device resets the screen. A real editor would keep the draft in a
  `ViewModel` or `rememberSaveable`; this demo keeps it small.
- The UI test pins `espresso-core:3.8.0-alpha04`, as in 04 and 05: the default Espresso crashes on an API 37 emulator.

## Documentation

- [Side-effects in Compose: rememberUpdatedState](https://developer.android.com/develop/ui/compose/side-effects#rememberupdatedstate)
- [Side-effects in Compose: restarting effects](https://developer.android.com/develop/ui/compose/side-effects#restarting-effects)
- [rememberUpdatedState (API reference)](https://developer.android.com/reference/kotlin/androidx/compose/runtime/rememberUpdatedState.composable)

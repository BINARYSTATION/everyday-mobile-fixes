# 05 - Wish Star

Not a fix this time: a small demo of two APIs that are new in Jetpack Compose 1.13 (November 2026 release).

Tap the circle and it morphs into a five-point star while the night sky behind it slides out of focus. Tap again and
both go back. The sky is drawn with `Canvas`, so the app has no image files.

| | |
|---|---|
| Language / UI | Kotlin 2.2, Jetpack Compose 1.13 (BOM 2026.11.01, Material 3 1.5.0), one Activity |
| New APIs | `PolygonShape`, `MorphPolygonShape` (foundation), `Modifier.blur { }` with `BlurRadiusSpec` (ui) |
| Min / target / compile SDK | 26 / 37 / 37.1 |
| Build | Gradle 9.7.1 wrapper, AGP 9.4 |

## The two APIs

```kotlin
fun starShape(p: () -> Float) =
    MorphPolygonShape(
        start = PolygonShape.circle(),
        end = PolygonShape.star(
            numPoints = 5,
            innerRadiusRatio = 0.45f,
        ),
        progress = p,
    )

fun Modifier.skyBlur(p: () -> Float) =
    blur {
        val stops = skyStops(p())
        blurRadiusSpec = BlurRadiusSpec
            .verticalGradient(stops)
    }
```

- `MorphPolygonShape` is a normal `Shape`, so `Modifier.clip(shape)` is all the button needs. It morphs between two
  `PolygonShape`s and reads `progress` each time the outline is resolved. Before 1.13 this needed the
  `androidx.graphics:graphics-shapes` library, a `RoundedPolygon` at the right pixel size and a custom `Shape`.
- `Modifier.blur { }` takes a `BlurRadiusSpec`, so the blur radius can change across the surface.
  `BlurRadiusSpec.verticalGradient(stops)` goes from top to bottom through a list of `BlurStop(fraction, radius)`.
- Both get the animation as a lambda (`p`) and read it while drawing. `WishStarScreen` passes the `State` from
  `animateFloatAsState` on and never reads `.value` during composition.

## What the screen does

| | At rest (progress 0) | After a wish (progress 1) |
|---|---|---|
| Button | cream circle (`PolygonShape.circle()`) | gold five-point star, turned 126 degrees so a tip points up |
| Sky blur | sharp down to 55 % of the sky, then up to 16 dp at the bottom edge | 8 dp from the top, up to 32 dp at the bottom edge |
| Glow | hidden | soft gold glow behind the star (`Modifier.blur(36.dp)`) |
| Text | Tap the circle to make a wish. | Wish sent. Delivery: 3-5 business days. |

The progress comes from a spring (`dampingRatio = 0.6f`, `Spring.StiffnessLow`), so it can overshoot 1 for a moment.
`SkyFocus.stops()` clamps it, because a `BlurStop` with a negative radius throws.

## Measured

All numbers come from this app on an Android 17 (API 37) arm64 emulator, not on a phone.

| | Result |
|---|---|
| Screen compositions for one tap, while the morph and blur animate (90 frames on the test clock) | 1 |
| Same test, with one temporary line that reads `wish.value` during composition (negative control, reverted) | 37 |
| Center of the button before / after the tap (`captureToImage`) | cream `#F4EBD9` / gold `#FFC857` |

- The count comes from a `SideEffect` at the top of `WishStarScreen` (the `onComposed` parameter, empty in the app).
  `Box` and `Column` are inline, so a read of `wish.value` anywhere in the screen body would show up in it, as the
  negative control did.
- Rendering: the API 37 emulator image forces software rendering on this Mac ("API level 37 requires GLES 3.1"). With it,
  `dumpsys gfxinfo` showed frames of 69 ms (median) and 600 ms (90th percentile), and a 10.6 s recording held only 34
  frames. Started with `-gpu host`, the same two morphs drew at 19 ms (median) and 25 ms (90th percentile). The video
  was recorded with `-gpu host`.

## Where to look

- `WishEffects.kt`: `starShape()` and `Modifier.skyBlur()`, the two new APIs.
- `SkyFocus.kt`: the blur stops for any progress, as plain numbers (no Compose types), so they are unit tested.
- `WishStarScreen.kt`: the state, the spring, the button with its glow, the text.
- `NightSky.kt` and `SkyDots.kt`: the drawn sky (gradient, 110 dots from a fixed seed, crescent moon, two hills).

## Build and test

```
./gradlew testDebugUnitTest            # 7 unit tests
./gradlew assembleDebug                # app/build/outputs/apk/debug/app-debug.apk
./gradlew connectedDebugAndroidTest    # 2 tests on a device or emulator
```

- `SkyFocusTest` (5): rest and wish stops, the midpoint, spring overshoot clamped, every progress from -0.2 to 1.2
  gives sorted stops with fractions in 0..1 and no negative radius.
- `SkyDotsTest` (2): the same seed gives the same sky; every dot stays in the upper 70 % of the sky.
- `WishStarTest` (2): tap and tap again switch the text; one tap composes the screen once and turns the center gold.

Needs the Android SDK with platform 37.1 (`android sdk install platforms/android-37.1`), `local.properties` with
`sdk.dir`, and JDK 17 or newer.

## Notes

- Compose 1.13 needs `compileSdk` 37.1 and Kotlin 2.2 or newer. The library metadata says so too
  (`minCompileSdk=37`, `minCompileMinorSdk=1`), and AGP 9 sets a minor level like this:
  `compileSdk { version = release(37) { minorApiLevel = 1 } }`.
- A blur that changes across the surface needs Android 13 (API 33); older versions draw the sky unblurred. The uniform
  glow needs Android 12 (API 31). The morph itself works on every supported version.
- `PolygonShape.star` puts its first tip at 3 o'clock. The button turns 126 degrees (54 + 72) while it morphs, so the
  star lands with a tip at 12 o'clock.
- Since Compose 1.13, `Modifier.clickable` plays the system click sound (per the release notes). The emulator ran
  without audio, so this was not checked here.
- The color check in `WishStarTest` compares the distance to gold and to cream instead of an exact value: right after
  the tap the press ripple can still be fading over the shape (one run read `#F8C255`, 1.5 s later `#FFC857`).
- The UI test pins `espresso-core:3.8.0-alpha04`, as in 04: the default Espresso crashes on an API 37 emulator.

## Documentation

- [What's new in the Jetpack Compose November '26 release](https://developer.android.com/blog/posts/jetpack-compose-november-2026-release)
- [PolygonShape](https://developer.android.com/reference/kotlin/androidx/compose/foundation/shape/PolygonShape)
- [MorphPolygonShape (shape package)](https://developer.android.com/reference/kotlin/androidx/compose/foundation/shape/package-summary)
- [Blur and progressive blur](https://developer.android.com/develop/ui/compose/graphics/blur/overview)
- [BlurRadiusSpec](https://developer.android.com/reference/kotlin/androidx/compose/ui/graphics/blur/BlurRadiusSpec)

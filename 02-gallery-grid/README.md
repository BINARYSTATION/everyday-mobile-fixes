# 02 - Gallery Grid

A photo grid decodes 12 photos of 4000 x 3000 pixels into thumbnail slots that are about 318 pixels wide.
Decoded at full size, every tile holds 48 MB of pixels and the grid holds 576 MB. Decoded at the size of the slot,
the same grid holds 9 MB.

This small app recreates the heavy version on purpose and shows the fix next to it. A switch at the top flips
between the two.

| | |
|---|---|
| Language / UI | Kotlin 2.2, Jetpack Compose (Material 3), one Activity |
| Min / target SDK | 26 / 36 |
| Build | Gradle 9.7.1 wrapper, AGP 9.4 |

## The problem

`BitmapFactory.decodeFile(path)` decodes every pixel of the file. A decoded `Bitmap` in the default `ARGB_8888`
format takes 4 bytes per pixel, so the size in memory depends on the file, not on the slot it is shown in:

```
4000 x 3000 pixels x 4 bytes = 48,000,000 bytes per photo
12 photos                    = 576,000,000 bytes
```

The app keeps every decoded bitmap, like a simple in-memory cache without a limit, and adds up
`Bitmap.allocationByteCount` over all of them. That sum is the big number on the screen.

Anyone with a gallery, a profile photo picker, a receipt upload or a chat with image previews can run into this:
a grid of small thumbnails that quietly holds a big picture per tile. On a phone with little memory such a grid can
stutter or fail to get the memory it asks for (the app then shows "Out of memory" on that tile; on the 4 GB emulator used
here all 12 photos fit and nothing failed, so that path was never triggered).

## How to see it

1. Install the debug build and open the app. On the first launch it draws the 12 test photos itself (nobody's
   pictures are used) and shows a progress bar.
2. The switch is off (BEFORE, full-size decode). The grid fills tile by tile, each tile shows `48.0 MB` and the
   total climbs to `576 MB`.
3. Flip the switch to AFTER. Same photos, same slots: each tile shows `750 KB` and the total is `9.0 MB`.
4. `Reload` decodes everything again in the current mode.

To start straight in AFTER from a script:

```
adb shell am start -n io.github.binarystation.gallerygrid/.MainActivity --ez sampled true
```

## The cause

`decodeFile` has no idea how big the slot is. It returns the whole image, and the memory is allocated before the
bitmap is ever drawn small. The official documentation warns that the decode methods "attempt to allocate memory for
the constructed bitmap and therefore can easily result in an OutOfMemory exception".

## The fix

Read the image size first, without allocating pixels, then decode a smaller copy:

```kotlin
val o = BitmapFactory.Options()
o.inJustDecodeBounds = true // read the size only, no pixels
BitmapFactory.decodeFile(path, o)
o.inSampleSize = sampleSize(o, cellPx)
o.inJustDecodeBounds = false
val bmp = BitmapFactory.decodeFile(path, o)
```

`inJustDecodeBounds = true` fills `outWidth` and `outHeight` and returns no bitmap. `calculateInSampleSize`
(the recipe from the documentation, in `SampleSize.kt`) then picks the largest power of two that still leaves
both sides of the decoded image at least as big as the slot. For a 318 px slot and a 4000 x 3000 photo that is 8:
the decoder returns 500 x 375 pixels, which is 750,000 bytes, 64 times less than 48,000,000.

The tiles look the same: the slot is 318 pixels wide, the sampled image is 500.

## Measured numbers

Everything below was measured with this app on an Android 17 (API 37) arm64 emulator with 4 GB of RAM, not on a
phone. Units are decimal (1 MB = 1,000,000 bytes).

| | BEFORE (full size) | AFTER (inSampleSize 8) |
|---|---|---|
| One tile (`allocationByteCount`) | 48,000,000 bytes | 750,000 bytes |
| 12 tiles | 576,000,000 bytes | 9,000,000 bytes |
| Time to decode all 12 (10 cold starts, two runs of 5) | 1.47 to 1.75 s, median 1.62 s | 0.50 to 0.94 s, median 0.56 s |
| Native heap PSS, `dumpsys meminfo` (3 cold starts) | 572,494 to 572,678 KB | 20,636 to 20,804 KB |
| Total PSS, `dumpsys meminfo` (3 cold starts) | 632,427 to 632,622 KB | 82,812 to 83,144 KB |

- The byte counts are what the platform decoder reports, not arithmetic: `DecodeMemoryTest` and `GalleryScreenTest`
  check them on the emulator.
- The time is the app's own log line (`adb logcat -s GalleryGrid`): from the start of the first decode to the end of the
  twelfth, one after the other. Each mode was cold-started 10 times (force-stop, then launch). The first run of 5 gave
  1.61 to 1.70 s and 0.50 to 0.59 s, the second run of 5 gave 1.47 to 1.75 s and 0.53 to 0.94 s (one slow AFTER start
  took 0.94 s). The table shows all 10 together. An emulator is not a phone, so read it as a direction, not as a promise:
  about 3 times faster here.
- `dumpsys meminfo` was read about 7 seconds after a cold start. The PSS columns are in KB as printed by the tool.

## Where to look

- `PhotoDecoder.kt`: `decodeFullSize` (BEFORE) and `decodeSampled` (AFTER) are the whole difference.
- `SampleSize.kt`: `calculateInSampleSize` and `decodedByteCount`, plain Kotlin, unit tested.
- `GalleryViewModel.kt`: switch, reload, the one-by-one decode and the log line.
- `GalleryScreen.kt`: header, mode switch, the memory card with the total and the grid of tiles.
- `TestPhotos.kt`: draws the 12 landscape test photos with `Canvas` (4000 x 3000, JPEG) into the app's private folder.
- `ByteFormat.kt`: `48.0 MB`, `750 KB`, `576 MB` formatting.

## Build and test

```
./gradlew testDebugUnitTest            # 15 unit tests: size math and byte formatting
./gradlew assembleDebug                # app/build/outputs/apk/debug/app-debug.apk
./gradlew connectedDebugAndroidTest    # 6 tests on a device or emulator: 5 decoder tests, 1 Compose UI test
```

- `SampleSizeTest` (11) and `ByteFormatTest` (4) are plain JVM tests.
- `DecodeMemoryTest` (5) decodes real files with the platform decoder and checks `allocationByteCount`:
  48,000,000 per full decode, 750,000 per sampled decode, 576,000,000 and 9,000,000 for twelve.
- `GalleryScreenTest` (1) launches the screen, waits for `576 MB`, flips the switch and waits for `9.0 MB`.

Needs the Android SDK (`local.properties` with `sdk.dir`) and JDK 17 or newer.

## Notes

- Libraries such as Coil and Glide already size the decode for the target view. This demo shows what hand-rolled
  decoding costs and what the manual fix looks like.
- `inSampleSize` works in powers of two (the documentation says the decoder rounds any other value down to the nearest
  power of two), so the decoded image can be larger than the slot (500 pixels for 318).
  The documentation's recipe keeps both sides at least as big as the slot, so the tile never has to be scaled up.
- The decode is simple on purpose: it runs on `Dispatchers.Default`, one photo after the other, and every state write
  happens on the main thread.
- The UI test pins the alpha `espresso-core:3.8.0-alpha04`. With the Espresso version that comes along with the Compose
  test library, UI tests crashed on an Android 17 (API 37) emulator because `InputManager.getInstance()` is missing
  there; the alpha runs fine.

## Documentation

- [Loading large bitmaps efficiently](https://developer.android.com/topic/performance/graphics/load-bitmap)

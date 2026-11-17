# 01 - Grocery Notes

Delete one row in a Jetpack Compose list and the note typed on it shows up on the next row.

This small app recreates that bug on purpose and shows the fix next to it. A switch at the top flips between
the two versions.

| | |
|---|---|
| Language / UI | Kotlin 2.2, Jetpack Compose (Material 3), one Activity |
| Min / target SDK | 26 / 36 |
| Build | Gradle 9.7.1 wrapper, AGP 9.4 |

## The problem

Every row of the list has its own note field. The text of the field is kept with `remember` inside the row.

1. Switch is off (BEFORE).
2. Type `basmati` in the note of the Rice row.
3. Tap Delete on Rice.

Rice is gone, but `basmati` now sits on the Oil row, which moved up into Rice's place.

Anyone with a shopping list, a to-do list or a cart can run into this: a typed note, a ticked box or an expanded
card ends up on the wrong item after another row is deleted.

## The cause

Without a key, `LazyColumn` keys each item's remembered state by the item's position in the list. After the delete,
the data moves up one place, but the remembered note stays at the old position.
The official documentation says it directly: "By default, each item's state is keyed against the position of the
item in the list or grid."

## The fix

Give every item a stable, unique key:

```kotlin
items(rows, key = { it.id }) { row ->
    GroceryRow(
        row = row,
        onDelete = onDelete,
        modifier = Modifier.animateItem(),
    )
}
```

`key = { it.id }` ties the remembered note to the item, not to the position. `Modifier.animateItem()` slides the
remaining rows into place; the documentation notes that it needs a key to work as expected.

Switch the app to AFTER and run the same three steps: the note leaves with Rice and Oil keeps its own empty note.

## Where to look

- `GroceryList.kt`: `UnkeyedList` (BEFORE) and `KeyedList` (AFTER) are the only difference between the two modes.
- `GroceryViewModel.kt`: the rows, the mode switch, delete and reset. No Android code, so it is unit tested.
- `GroceryScreen.kt`: title, the BEFORE / AFTER switch and the list.

## Build and test

```
./gradlew testDebugUnitTest            # 6 unit tests for the list logic
./gradlew assembleDebug                # app/build/outputs/apk/debug/app-debug.apk
./gradlew connectedDebugAndroidTest    # 2 Compose UI tests, needs an emulator or a phone
```

The two UI tests (`KeyedListTest`) type a note on Rice, delete Rice and check where the note ends up:
without a key it appears on Oil, with a key it is gone.

Needs the Android SDK (`local.properties` with `sdk.dir`) and JDK 17 or newer.

## Notes

- The note is kept with `remember`, so it does not survive screen rotation. `rememberSaveable` would keep it, and
  it has the same position-keyed behavior without a key.
- The UI tests pin the alpha `espresso-core:3.8.0-alpha04`. With the Espresso version that comes along with the
  Compose test library, the tests crashed on an Android 17 (API 37) emulator because `InputManager.getInstance()` is
  missing there; the alpha runs fine.

## Documentation

- [Lists and grids in Compose: item keys and item animations](https://developer.android.com/develop/ui/compose/lists)

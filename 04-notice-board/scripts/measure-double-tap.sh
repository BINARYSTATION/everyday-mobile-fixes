#!/bin/bash
# Real-emulator check of the double tap: N cold starts per mode, two `input tap` calls back to back on the first row.
# Prints one line per run: mode, chip before, chip after, ms between the two shell calls (upper bound for the gap).
# Tap coordinates are for the 1080x2400 Pixel_Play emulator. Needs a booted emulator with the debug APK installed. Usage: ./measure-adb.sh RUNS [PAUSE_SECONDS_BETWEEN_TAPS]
# Also prints the mode label the screen shows (BEFORE / AFTER), so a wrong start mode cannot hide.
PKG=io.github.binarystation.noticeboard
N=${1:-10}
PAUSE=${2:-0}
TMP=$(mktemp -d)
chip() {
  adb shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1
  adb pull /sdcard/ui.xml "$TMP/ui.xml" >/dev/null 2>&1
  grep -o 'text="Back stack: [0-9]*"' "$TMP/ui.xml" | head -1 | grep -o '[0-9]*'
}
label() { grep -o 'text="\(BEFORE\|AFTER\)"' "$TMP/ui.xml" | head -1 | grep -o 'BEFORE\|AFTER'; }
for mode in before after; do
  extra=""; [ "$mode" = "after" ] && extra="--ez guarded true"
  for i in $(seq 1 "$N"); do
    adb shell am force-stop $PKG
    adb shell am start -n $PKG/.MainActivity $extra >/dev/null
    sleep 2.5
    pre=$(chip); lab=$(label)
    t=$(adb shell "a=\$(date +%s%N); input tap 540 617; b=\$(date +%s%N); sleep $PAUSE; input tap 540 617; c=\$(date +%s%N); echo \$(( (b-a)/1000000 )) \$(( (c-b)/1000000 ))")
    sleep 2.2
    post=$(chip)
    echo "$mode run $i: screen says $lab, chip before taps=$pre, after taps=$post, shell call ms (tap1 tap2)=$t"
  done
done
adb shell am force-stop $PKG

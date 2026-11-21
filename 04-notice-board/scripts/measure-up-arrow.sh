#!/bin/bash
# Second face of the bug on the real emulator: open a notice, then two taps on the Up arrow ("< Back"), ~0.2 s apart.
# Per run: did the process die with "NavDisplay backstack cannot be empty", and which screen is left.
# Usage: ./measure-adb-up.sh RUNS [PAUSE_SECONDS]
PKG=io.github.binarystation.noticeboard
N=${1:-5}
PAUSE=${2:-0.15}
TMP=$(mktemp -d)
for mode in before after; do
  extra=""; [ "$mode" = "after" ] && extra="--ez guarded true"
  for i in $(seq 1 "$N"); do
    adb shell am force-stop $PKG
    adb logcat -c
    adb shell am start -n $PKG/.MainActivity $extra >/dev/null
    sleep 2.5
    adb shell input tap 540 617      # open the first notice, once
    sleep 2.2
    adb shell "input tap 120 210; sleep $PAUSE; input tap 120 210"
    sleep 2.2
    crash=$(adb logcat -d | grep -c "NavDisplay backstack cannot be empty")
    alive=$(adb shell pidof $PKG | wc -l | tr -d ' ')
    adb shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1; adb pull /sdcard/ui.xml "$TMP/ui.xml" >/dev/null 2>&1
    chip=$(grep -o 'text="Back stack: [0-9]*"' "$TMP/ui.xml" | head -1 | grep -o '[0-9]*')
    echo "$mode run $i: crash lines in logcat=$crash, process alive=$alive, chip now=${chip:-none}"
  done
done
adb shell am force-stop $PKG

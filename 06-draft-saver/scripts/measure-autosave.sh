#!/usr/bin/env bash
# Real-time check on a device or emulator: open the app, type a postcard, wait 12 s, read the
# "Saved copy" card with uiautomator. Repeats RUNS times in BEFORE and in AFTER mode.
#
# Usage: scripts/measure-autosave.sh [runs] [output-file]
# Needs: adb on PATH (or ANDROID_HOME set), the debug app installed, one device connected.
set -euo pipefail

RUNS="${1:-5}"
OUT="${2:-/dev/stdout}"
ADB="${ADB:-adb}"
PKG="io.github.binarystation.draftsaver"
TEXT="Day%s2%sin%sGoa"   # input text needs %s for spaces

# Center of the editor, read from the UI dump (the field holds the "Your postcard" label).
editor_center() {
    "$ADB" shell uiautomator dump /sdcard/ui.xml >/dev/null
    "$ADB" shell cat /sdcard/ui.xml | tr '>' '\n' | grep 'class="android.widget.EditText"' | head -1 |
        sed -E 's/.*bounds="\[([0-9]+),([0-9]+)\]\[([0-9]+),([0-9]+)\]".*/\1 \2 \3 \4/' |
        awk '{ printf "%d %d\n", ($1 + $3) / 2, ($2 + $4) / 2 }'
}

# Prints the "Saves: N" text and the saved copy, separated by " | ".
saved_card() {
    "$ADB" shell uiautomator dump /sdcard/ui.xml >/dev/null
    "$ADB" shell cat /sdcard/ui.xml | tr '>' '\n' | grep -o 'text="[^"]*"' | sed -E 's/text="(.*)"/\1/' |
        awk 'f == 1 { print saves " | " $0; exit } /^Saves: / { saves = $0; f = 1 }'
}

"$ADB" wait-for-device

{
    echo "# $(date '+%Y-%m-%d %H:%M:%S') runs=$RUNS device=$("$ADB" shell getprop ro.product.model | tr -d '\r') api=$("$ADB" shell getprop ro.build.version.sdk | tr -d '\r')"
    for mode in before after; do
        extra=()
        [ "$mode" = "after" ] && extra=(--ez fixed true)
        for run in $(seq 1 "$RUNS"); do
            "$ADB" shell am force-stop "$PKG"
            start=$(date +%s)
            "$ADB" shell am start -W -n "$PKG/.MainActivity" ${extra[@]+"${extra[@]}"} >/dev/null
            sleep 1
            read -r x y < <(editor_center)
            "$ADB" shell input tap "$x" "$y"
            "$ADB" shell input text "$TEXT"
            typed=$(( $(date +%s) - start ))
            # Wait until about 12 s after launch: two save ticks (5 s, 10 s) have passed.
            while [ $(( $(date +%s) - start )) -lt 12 ]; do sleep 0.5; done
            echo "$mode run $run (typed ${typed}s after launch): $(saved_card)"
        done
    done
    "$ADB" shell am force-stop "$PKG"
} | tee "$OUT"

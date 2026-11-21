#!/bin/bash
# One continuous emulator recording: BEFORE double tap, Back, Back, flip the switch, AFTER double tap, Back.
# Coordinates are for the 1080x2400 Pixel_Play emulator (row 1, "< Back", mode switch).
PKG=io.github.binarystation.noticeboard
OUT=${1:-notice-board-raw.mp4}
adb shell am force-stop $PKG
adb shell am start -n $PKG/.MainActivity >/dev/null
sleep 2.5
adb shell "screenrecord --bit-rate 12000000 --time-limit 24 /sdcard/demo.mp4" &
REC=$!
sleep 2.0
adb shell "input tap 540 617; sleep 0.15; input tap 540 617"   # BEFORE: double tap
sleep 3.2
adb shell input tap 120 210                                     # Back once: same notice again
sleep 3.0
adb shell input tap 120 210                                     # Back twice: the list
sleep 2.4
adb shell input tap 932 412                                     # flip the switch to AFTER
sleep 1.8
adb shell "input tap 540 617; sleep 0.15; input tap 540 617"   # AFTER: double tap
sleep 3.2
adb shell input tap 120 210                                     # Back once: the list
sleep 2.4
wait $REC
sleep 1
adb pull /sdcard/demo.mp4 "$OUT" >/dev/null && adb shell rm /sdcard/demo.mp4
ffprobe -v error -show_entries stream=width,height,r_frame_rate,duration -of csv=p=0 "$OUT"

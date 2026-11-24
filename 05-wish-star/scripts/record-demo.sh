#!/bin/bash
# One continuous emulator recording: rest state, tap (circle -> star), tap back, tap again.
# Coordinates are for the 1080x2400 Pixel_Play emulator (center of the wish button).
PKG=io.github.binarystation.wishstar
OUT=${1:-wish-star-raw.mp4}
X=540; Y=1354
adb shell am force-stop $PKG
adb shell am start -n $PKG/.MainActivity >/dev/null
sleep 2.5
adb shell "screenrecord --bit-rate 12000000 --time-limit 16 /sdcard/demo.mp4" &
REC=$!
sleep 2.0
adb shell input tap $X $Y   # make a wish: circle -> star, the sky blurs
sleep 4.0
adb shell input tap $X $Y   # take it back: star -> circle, the sky sharpens
sleep 3.0
adb shell input tap $X $Y   # wish again
wait $REC
sleep 1
adb pull /sdcard/demo.mp4 "$OUT" >/dev/null && adb shell rm /sdcard/demo.mp4
ffprobe -v error -show_entries stream=width,height,r_frame_rate,duration -of csv=p=0 "$OUT"

# 03 - Reply Bar

A chat screen has a text field and a Send button pinned at the bottom. One SwiftUI modifier on the container,
`ignoresSafeArea(.keyboard)`, makes the whole reply bar stay put when the keyboard opens, so the keyboard covers
the field and the Send button. Delete that line and the bar rides above the keyboard.

This small app recreates the bug on purpose and shows the fix next to it. A segmented control at the top flips
between the two screens.

| | |
|---|---|
| Language / UI | Swift 6, SwiftUI, one scene |
| Min iOS | 17 (checked on the iOS 27.2 simulator) |
| Build | Xcode 27.2; `project.yml` for XcodeGen, the generated project is committed |

## The problem

`ReplyScreenBefore` puts a message list and the reply bar in a `VStack`, then ignores the keyboard:

```swift
VStack(spacing: 0) {
    MessageList(chat.messages)
    ReplyBarView(chat.send)
}
.ignoresSafeArea(.keyboard)
```

It looks like a harmless way to stop a screen from jumping when the keyboard opens. The side effect: the `VStack` keeps its full
height, the reply bar stays at the bottom of the screen, and the keyboard is drawn over it. The user taps the field,
the field disappears, and Send cannot be reached.

## How to see it

1. Run the app on an iOS Simulator. The control at the top is on "Before".
2. Tap the reply field at the bottom. The keyboard opens and the bar is gone.
3. Drag the keyboard down: the bar, with what you typed, was there all the time.
4. Switch to "After" and tap the field again. The bar sits right above the keyboard and Send works.

To start straight in "After" from a script:

```
xcrun simctl launch booted io.github.binarystation.replybar -fixed
```

If the simulator shows no software keyboard, turn off I/O > Keyboard > Connect Hardware Keyboard.

## The cause

By default, SwiftUI sizes and positions views to avoid the safe areas, and the software keyboard is one of them.
The documentation says so: "By default, the SwiftUI layout system sizes and positions views to avoid certain safe
areas. This ensures that system content like the software keyboard or edges of the device don't obstruct your views."
`ignoresSafeArea(_:edges:)` is how a view opts out ("To extend your content into these regions, you can ignore safe
areas on specific edges by applying this modifier"). Opt out of the keyboard region and the layout no longer makes room
for the keyboard.

## The fix

Delete the modifier. `ReplyScreenAfter` is the same code without that line:

```swift
VStack(spacing: 0) {
    MessageList(chat.messages)
    ReplyBarView(chat.send)
}
```

Why not `safeAreaInset(edge: .bottom)`? It is the right tool when a bar should float over a list that scrolls under it.
Here the bar is an ordinary row at the bottom of a `VStack`, and the default keyboard handling already does the right
thing, so removing the one line is the smallest honest fix.

## Measured numbers

Measured by the UI tests on the iPhone 18 Pro simulator (iOS 27.2, screen 402 x 874 points). All values are points as
reported by XCUITest after the keyboard stopped moving and "Pineapple" was typed.

| | BEFORE | AFTER |
|---|---|---|
| Keyboard frame | x 0, y 590, 402 x 226 | x 0, y 590, 402 x 226 |
| Reply field frame | x 16, y 786, 277 x 44 | x 16, y 492, 277 x 44 |
| Send frame | x 305, y 786, 81 x 44 | x 305, y 492, 81 x 44 |
| Send top edge vs keyboard top (590) | 196 pt below it | 98 pt above it |
| Send bottom edge vs keyboard top (590) | 240 pt below it | 54 pt above it |
| `isHittable` for Send | false | true |

- The keyboard element is reported from y 590; its visible area runs to the bottom of the screen.
- Same numbers in both full runs on the same simulator.
- One simulator and one OS version: this is not a claim about other devices or other iOS versions.

## Where to look

- `ReplyScreenBefore.swift` and `ReplyScreenAfter.swift`: the whole difference is one line.
- `ReplyBarView.swift`: the text field and the Send button (Send is disabled while the draft is blank).
- `MessageList.swift`: the scrolling messages, scrolls to the newest one when you send.
- `ChatModel.swift`: `canSend` and `send`, plain Swift.
- `ContentView.swift`: the Before / After control.

## Build and test

```
xcodegen generate     # optional: rebuilds ReplyBar.xcodeproj from project.yml
xcodebuild test -project ReplyBar.xcodeproj -scheme ReplyBar \
  -destination 'platform=iOS Simulator,name=iPhone 18 Pro'
```

- `ChatModelTests` (3, Swift Testing): blank drafts cannot be sent, the sent text is trimmed, blank sends are ignored.
- `KeyboardCoverTests` (3, XCUITest): taps the field, waits for the keyboard, then checks Send against the keyboard
  frame. BEFORE: Send starts below the keyboard's top edge and is not hittable. AFTER: Send ends above it, is hittable,
  and tapping it adds the message. A third test flips the control in the app and checks the bar again.

The UI tests need the software keyboard: turn off Connect Hardware Keyboard in the Simulator (I/O > Keyboard) first.

## Documentation

- [ignoresSafeArea(_:edges:)](https://developer.apple.com/documentation/swiftui/view/ignoressafearea(_:edges:))

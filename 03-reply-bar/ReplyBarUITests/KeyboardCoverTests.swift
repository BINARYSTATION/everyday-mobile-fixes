import XCTest

/// Raises the software keyboard and checks where the reply bar ends up:
/// BEFORE it sits under the keyboard, AFTER it sits right above it.
@MainActor
final class KeyboardCoverTests: XCTestCase {
    override func setUp() {
        continueAfterFailure = false
    }

    private struct Layout {
        let keyboard: CGRect
        let field: CGRect
        let send: CGRect
        let sendIsHittable: Bool
        let fieldIsHittable: Bool
    }

    private func launch(fixed: Bool) -> XCUIApplication {
        let app = XCUIApplication()
        if fixed { app.launchArguments.append("-fixed") }
        app.launch()
        return app
    }

    /// Taps the field, waits until the keyboard has stopped moving, types a short reply and reads the frames.
    private func raiseKeyboardAndMeasure(_ app: XCUIApplication, label: String) -> Layout {
        let field = app.textFields["replyField"]
        XCTAssertTrue(field.waitForExistence(timeout: 5))
        field.tap()

        let keyboard = app.keyboards.firstMatch
        XCTAssertTrue(keyboard.waitForExistence(timeout: 5), "The software keyboard did not appear")
        waitUntilStill(keyboard)
        app.typeText("Pineapple")

        let send = app.buttons["sendButton"]
        XCTAssertTrue(send.exists)
        let layout = Layout(
            keyboard: keyboard.frame,
            field: field.frame,
            send: send.frame,
            sendIsHittable: send.isHittable,
            fieldIsHittable: field.isHittable
        )
        let line = "FRAMES \(label) keyboard=\(layout.keyboard) field=\(layout.field) send=\(layout.send) "
            + "sendHittable=\(layout.sendIsHittable) fieldHittable=\(layout.fieldIsHittable)"
        print(line)
        let note = XCTAttachment(string: line)
        note.lifetime = .keepAlways
        add(note)
        return layout
    }

    private func waitUntilStill(_ element: XCUIElement) {
        var last = element.frame
        for _ in 0..<20 {
            Thread.sleep(forTimeInterval: 0.25)
            let now = element.frame
            if now == last { return }
            last = now
        }
    }

    func testBeforeSendButtonIsUnderTheKeyboard() {
        let app = launch(fixed: false)
        let layout = raiseKeyboardAndMeasure(app, label: "BEFORE")

        XCTAssertGreaterThanOrEqual(layout.send.minY, layout.keyboard.minY, "Send should start below the keyboard's top edge")
        XCTAssertTrue(layout.send.intersects(layout.keyboard))
        XCTAssertFalse(layout.sendIsHittable, "A tap on Send would land on the keyboard")
        XCTAssertFalse(layout.fieldIsHittable, "The text field is under the keyboard too")
    }

    func testAfterSendButtonSitsAboveTheKeyboardAndWorks() {
        let app = launch(fixed: true)
        let layout = raiseKeyboardAndMeasure(app, label: "AFTER")

        XCTAssertLessThanOrEqual(layout.send.maxY, layout.keyboard.minY, "Send should end above the keyboard's top edge")
        XCTAssertFalse(layout.send.intersects(layout.keyboard))
        XCTAssertTrue(layout.sendIsHittable)
        XCTAssertTrue(layout.fieldIsHittable)

        app.buttons["sendButton"].tap()
        XCTAssertTrue(app.staticTexts["Pineapple"].waitForExistence(timeout: 3), "The sent message should show in the list")
    }

    func testSwitchingTheVersionInTheAppFixesTheBar() {
        let app = launch(fixed: false)
        app.segmentedControls["versionPicker"].buttons["After"].tap()
        let layout = raiseKeyboardAndMeasure(app, label: "SWITCHED")
        XCTAssertFalse(layout.send.intersects(layout.keyboard))
        XCTAssertTrue(layout.sendIsHittable)
    }
}

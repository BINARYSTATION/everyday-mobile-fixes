import Testing
@testable import ReplyBar

@MainActor
struct ChatModelTests {
    @Test func blankDraftsCannotBeSent() {
        #expect(!ChatModel.canSend(""))
        #expect(!ChatModel.canSend("   \n "))
        #expect(ChatModel.canSend("Hi"))
    }

    @Test func sendAddsMyMessageWithoutOuterSpaces() {
        let chat = ChatModel()
        let before = chat.messages.count
        chat.send("  Pineapple.  ")
        #expect(chat.messages.count == before + 1)
        #expect(chat.messages.last?.text == "Pineapple.")
        #expect(chat.messages.last?.isMine == true)
    }

    @Test func sendIgnoresBlankDrafts() {
        let chat = ChatModel()
        let before = chat.messages.count
        chat.send("   ")
        #expect(chat.messages.count == before)
    }
}

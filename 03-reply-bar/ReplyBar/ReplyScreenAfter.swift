import SwiftUI

// AFTER: default keyboard avoidance
struct ReplyScreenAfter: View {
    let chat: ChatModel

    var body: some View {
        VStack(spacing: 0) {
            MessageList(chat.messages)
            ReplyBarView(chat.send)
        }
    }
}

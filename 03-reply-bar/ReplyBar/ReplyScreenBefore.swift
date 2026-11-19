import SwiftUI

// BEFORE: the keyboard is ignored
struct ReplyScreenBefore: View {
    let chat: ChatModel

    var body: some View {
        VStack(spacing: 0) {
            MessageList(chat.messages)
            ReplyBarView(chat.send)
        }
        .ignoresSafeArea(.keyboard)
    }
}

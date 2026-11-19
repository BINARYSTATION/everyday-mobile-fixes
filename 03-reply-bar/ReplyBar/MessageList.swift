import SwiftUI

struct MessageList: View {
    let messages: [Message]

    init(_ messages: [Message]) {
        self.messages = messages
    }

    var body: some View {
        ScrollViewReader { proxy in
            ScrollView {
                LazyVStack(spacing: 10) {
                    ForEach(messages) { message in
                        Bubble(message: message).id(message.id)
                    }
                }
                .padding(16)
            }
            .scrollDismissesKeyboard(.interactively)
            .onChange(of: messages) {
                withAnimation { proxy.scrollTo(messages.last?.id, anchor: .bottom) }
            }
        }
        .accessibilityIdentifier("messageList")
    }
}

private struct Bubble: View {
    let message: Message

    var body: some View {
        Text(message.text)
            .padding(.horizontal, 14)
            .padding(.vertical, 10)
            .foregroundStyle(message.isMine ? Color.white : Color.primary)
            .background(
                message.isMine ? Color.accentColor : Color(.secondarySystemBackground),
                in: RoundedRectangle(cornerRadius: 18, style: .continuous)
            )
            .frame(maxWidth: .infinity, alignment: message.isMine ? .trailing : .leading)
    }
}

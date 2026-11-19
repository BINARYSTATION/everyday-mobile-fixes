import SwiftUI

/// The text field and the Send button that sit at the bottom of the screen.
struct ReplyBarView: View {
    let onSend: (String) -> Void
    @State private var draft = ""

    init(_ onSend: @escaping (String) -> Void) {
        self.onSend = onSend
    }

    var body: some View {
        HStack(spacing: 12) {
            TextField("Reply to Mia", text: $draft)
                .padding(.horizontal, 16)
                .frame(height: 44)
                .background(Color(.secondarySystemBackground), in: Capsule())
                .accessibilityIdentifier("replyField")
            Button {
                onSend(draft)
                draft = ""
            } label: {
                Text("Send")
                    .font(.headline)
                    .padding(.horizontal, 20)
                    .frame(height: 44)
                    .foregroundStyle(.white)
                    .background(Color.accentColor, in: Capsule())
            }
            .disabled(!ChatModel.canSend(draft))
            .opacity(ChatModel.canSend(draft) ? 1 : 0.45)
            .accessibilityIdentifier("sendButton")
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 10)
        .background(.bar)
    }
}

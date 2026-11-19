import Observation

@MainActor
@Observable
final class ChatModel {
    private(set) var messages: [Message] = Message.sample

    /// A reply is worth sending when it has at least one visible character.
    nonisolated static func canSend(_ draft: String) -> Bool {
        !draft.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }

    /// Adds the trimmed draft as my message. Blank drafts are ignored.
    func send(_ draft: String) {
        guard Self.canSend(draft) else { return }
        let text = draft.trimmingCharacters(in: .whitespacesAndNewlines)
        messages.append(Message(text: text, isMine: true))
    }
}

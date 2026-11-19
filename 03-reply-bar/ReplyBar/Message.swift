import Foundation

struct Message: Identifiable, Equatable {
    let id = UUID()
    let text: String
    let isMine: Bool
}

extension Message {
    /// The short thread the demo starts with.
    static let sample: [Message] = [
        Message(text: "Pizza tonight at 8?", isMine: false),
        Message(text: "Sure! Which toppings?", isMine: true),
        Message(text: "Your call. Reply and I will order.", isMine: false),
    ]
}

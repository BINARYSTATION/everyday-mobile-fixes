import SwiftUI

struct ContentView: View {
    @State private var chat = ChatModel()
    @State private var showFixed = CommandLine.arguments.contains("-fixed")

    var body: some View {
        VStack(spacing: 0) {
            header
            if showFixed {
                ReplyScreenAfter(chat: chat)
            } else {
                ReplyScreenBefore(chat: chat)
            }
        }
    }

    private var header: some View {
        VStack(spacing: 10) {
            Text("Mia")
                .font(.headline)
            Picker("Version", selection: $showFixed) {
                Text("Before").tag(false)
                Text("After").tag(true)
            }
            .pickerStyle(.segmented)
            .accessibilityIdentifier("versionPicker")
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 10)
        .background(.bar)
    }
}

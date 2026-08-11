import SwiftUI
import AntaraCore

public struct MockMessage: Identifiable {
    public let id: UUID
    public let body: String
    public let isLocalSender: Bool
    
    public init(id: UUID = UUID(), body: String, isLocalSender: Bool) {
        self.id = id
        self.body = body
        self.isLocalSender = isLocalSender
    }
}

public struct ChatView: View {
    let threadTitle: String
    let messages: [MockMessage]
    let onSendMessage: (String) -> Void
    
    @State private var inputText: String = ""
    
    public init(threadTitle: String, messages: [MockMessage], onSendMessage: @escaping (String) -> Void) {
        self.threadTitle = threadTitle
        self.messages = messages
        self.onSendMessage = onSendMessage
    }
    
    public var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            // Header
            Text(threadTitle)
                .font(.custom("Outfit-Medium", size: 20, relativeTo: .title3))
                .foregroundColor(.white)
                .padding(.bottom, 8)
            
            // Messages
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 8) {
                        ForEach(messages) { message in
                            HStack {
                                if message.isLocalSender {
                                    Spacer()
                                    messageBubble(message.body, isLocal: true)
                                } else {
                                    messageBubble(message.body, isLocal: false)
                                    Spacer()
                                }
                            }
                        }
                    }
                }
            }
            
            // Input Area
            HStack {
                TextField("", text: $inputText, prompt: Text("Speak...").foregroundColor(Color(hex: "8E8E93")))
                    .foregroundColor(.white)
                    .font(.body)
                
                Button(action: {
                    guard !inputText.trimmingCharacters(in: .whitespaces).isEmpty else { return }
                    onSendMessage(inputText)
                    inputText = ""
                }) {
                    Text("Send")
                        .font(.headline)
                        .foregroundColor(.white)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(Color(hex: "0A0A0C"))
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(Color(hex: "1C1C1E"), lineWidth: 1)
            )
        }
        .padding(16)
        .background(Color.black.edgesIgnoringSafeArea(.all))
    }
    
    @ViewBuilder
    private func messageBubble(_ text: String, isLocal: Bool) -> some View {
        Text(text)
            .font(.body)
            .foregroundColor(.white)
            .padding(12)
            .background(isLocal ? Color(hex: "0A0A0C") : Color(hex: "1C1C1E"))
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(Color(hex: "1C1C1E"), lineWidth: 1)
            )
    }
}

// Helper Color hex parser
extension Color {
    init(hex: String) {
        let scanner = Scanner(string: hex)
        var rgbValue: UInt64 = 0
        scanner.scanHexInt64(&rgbValue)
        let r = Double((rgbValue & 0xFF0000) >> 16) / 255.0
        let g = Double((rgbValue & 0x00FF00) >> 8) / 255.0
        let b = Double(rgbValue & 0x0000FF) / 255.0
        self.init(red: r, green: g, blue: b)
    }
}

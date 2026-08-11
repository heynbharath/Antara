import SwiftUI
import AntaraCore

public struct MockNeighbor: Identifiable {
    public let id: String
    public let rssi: Int
    public var batteryLevel: Int
    public var trustScore: Double
    
    public init(id: String, rssi: Int, batteryLevel: Int, trustScore: Double) {
        self.id = id
        self.rssi = rssi
        self.batteryLevel = batteryLevel
        self.trustScore = trustScore
    }
}

public struct DashboardView: View {
    let neighbors: [MockNeighbor]
    let onSelectPeer: (MockNeighbor) -> Void
    
    public init(neighbors: [MockNeighbor], onSelectPeer: @escaping (MockNeighbor) -> Void) {
        self.neighbors = neighbors
        self.onSelectPeer = onSelectPeer
    }
    
    public var body: some View {
        VStack(alignment: .leading, spacing: 24) {
            // Calm Header
            Text("Antara")
                .font(.custom("Outfit-SemiBold", size: 32, relativeTo: .largeTitle))
                .foregroundColor(.white)
            
            Text("Nearby Nodes")
                .font(.subheadline)
                .foregroundColor(Color(hex: "8E8E93"))
                .padding(.bottom, -8)
            
            if neighbors.isEmpty {
                Spacer()
                HStack {
                    Spacer()
                    Text("Searching silently...")
                        .font(.body)
                        .foregroundColor(Color(hex: "8E8E93"))
                    Spacer()
                }
                Spacer()
            } else {
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(neighbors) { neighbor in
                            neighborRow(neighbor)
                                .onTapGesture {
                                    onSelectPeer(neighbor)
                                }
                        }
                    }
                }
            }
        }
        .padding(16)
        .background(Color.black.edgesIgnoringSafeArea(.all))
    }
    
    @ViewBuilder
    private func neighborRow(_ neighbor: MockNeighbor) -> some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                // Hex trim
                Text("Node [\(String(neighbor.id.prefix(8)))]")
                    .font(.headline)
                    .foregroundColor(.white)
                
                Text("LQI: \(Int(neighbor.trustScore * 100))% • RSSI: \(neighbor.rssi) dBm")
                    .font(.caption)
                    .foregroundColor(Color(hex: "8E8E93"))
            }
            
            Spacer()
            
            Text("\(neighbor.batteryLevel)%")
                .font(.subheadline)
                .foregroundColor(neighbor.batteryLevel > 20 ? Color(hex: "8E8E93") : .red)
        }
        .padding(16)
        .background(Color(hex: "0A0A0C"))
        .cornerRadius(12)
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color(hex: "1C1C1E"), lineWidth: 1)
        )
    }
}

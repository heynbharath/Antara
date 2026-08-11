import Foundation
import GRDB

public struct MessageRecord: Codable, FetchableRecord, PersistableRecord, TableRecord {
    public static let databaseTableName = "messages"
    
    public var messageId: String
    public var threadId: String
    public var timestamp: Date
    public var body: String
    public var vectorClockJson: String
    public var parentsJson: String
    public var senderIdentity: String

    public init(messageId: String, threadId: String, timestamp: Date, body: String, vectorClockJson: String, parentsJson: String, senderIdentity: String) {
        self.messageId = messageId
        self.threadId = threadId
        self.timestamp = timestamp
        self.body = body
        self.vectorClockJson = vectorClockJson
        self.parentsJson = parentsJson
        self.senderIdentity = senderIdentity
    }
}

public struct ThreadRecord: Codable, FetchableRecord, PersistableRecord, TableRecord {
    public static let databaseTableName = "threads"
    
    public var threadId: String
    public var title: String
    public var createdTime: Date

    public init(threadId: String, title: String, createdTime: Date) {
        self.threadId = threadId
        self.title = title
        self.createdTime = createdTime
    }
}

public struct NeighborRecord: Codable, FetchableRecord, PersistableRecord, TableRecord {
    public static let databaseTableName = "neighbors"
    
    public var nodeId: String
    public var rssi: Int
    public var batteryLevel: Int
    public var trustScore: Double
    public var queueDepth: Double
    public var lastSeen: Date

    public init(nodeId: String, rssi: Int, batteryLevel: Int, trustScore: Double, queueDepth: Double, lastSeen: Date) {
        self.nodeId = nodeId
        self.rssi = rssi
        self.batteryLevel = batteryLevel
        self.trustScore = trustScore
        self.queueDepth = queueDepth
        self.lastSeen = lastSeen
    }
}

public struct RouteRecord: Codable, FetchableRecord, PersistableRecord, TableRecord {
    public static let databaseTableName = "routes"
    
    public var destinationNodeId: String
    public var nextHopNodeId: String
    public var hopCount: Int
    public var expiresAt: Date

    public init(destinationNodeId: String, nextHopNodeId: String, hopCount: Int, expiresAt: Date) {
        self.destinationNodeId = destinationNodeId
        self.nextHopNodeId = nextHopNodeId
        self.hopCount = hopCount
        self.expiresAt = expiresAt
    }
}

public struct DtnPacketRecord: Codable, FetchableRecord, PersistableRecord, TableRecord {
    public static let databaseTableName = "dtn_packets"
    
    public var packetId: String
    public var recipientNodeId: String
    public var ttl: Date
    public var priority: Int
    public var payloadBytes: Data

    public init(packetId: String, recipientNodeId: String, ttl: Date, priority: Int, payloadBytes: Data) {
        self.packetId = packetId
        self.recipientNodeId = recipientNodeId
        self.ttl = ttl
        self.priority = priority
        self.payloadBytes = payloadBytes
    }
}

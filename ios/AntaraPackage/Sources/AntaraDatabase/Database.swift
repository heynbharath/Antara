import Foundation
import GRDB

public final class AntaraDatabaseConnection {
    public let dbQueue: DatabaseQueue
    
    public init(path: String) throws {
        let config = Configuration()
        
        // Open/Create the SQLite connection queue
        // Note: To enable encryption in production, link GRDB with the SQLCipher subspec 
        // and run: config.prepareDatabase { db in try db.usePassphrase(passphrase) }
        self.dbQueue = try DatabaseQueue(path: path, configuration: config)
        try setupMigrations()
    }
    
    private func setupMigrations() throws {
        var migrator = DatabaseMigrator()
        
        migrator.registerMigration("createSchema") { db in
            // 1. Threads Table
            try db.create(table: "threads") { t in
                t.column("threadId", .text).primaryKey()
                t.column("title", .text).notNull()
                t.column("createdTime", .datetime).notNull()
            }
            
            // 2. Messages Table
            try db.create(table: "messages") { t in
                t.column("messageId", .text).primaryKey()
                t.column("threadId", .text).notNull().references("threads", onDelete: .cascade)
                t.column("timestamp", .datetime).notNull()
                t.column("body", .text).notNull()
                t.column("vectorClockJson", .text).notNull()
                t.column("parentsJson", .text).notNull()
                t.column("senderIdentity", .text).notNull()
            }
            
            // 3. Neighbors Table
            try db.create(table: "neighbors") { t in
                t.column("nodeId", .text).primaryKey()
                t.column("rssi", .integer).notNull()
                t.column("batteryLevel", .integer).notNull()
                t.column("trustScore", .double).notNull()
                t.column("queueDepth", .double).notNull()
                t.column("lastSeen", .datetime).notNull()
            }
            
            // 4. Routes Table
            try db.create(table: "routes") { t in
                t.column("destinationNodeId", .text).primaryKey()
                t.column("nextHopNodeId", .text).notNull()
                t.column("hopCount", .integer).notNull()
                t.column("expiresAt", .datetime).notNull()
            }
            
            // 5. DTN Packets Table
            try db.create(table: "dtn_packets") { t in
                t.column("packetId", .text).primaryKey()
                t.column("recipientNodeId", .text).notNull()
                t.column("ttl", .datetime).notNull()
                t.column("priority", .integer).notNull()
                t.column("payloadBytes", .blob).notNull()
            }
        }
        
        try migrator.migrate(dbQueue)
    }
}

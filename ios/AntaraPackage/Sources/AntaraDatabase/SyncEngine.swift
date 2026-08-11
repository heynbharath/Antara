import Foundation
import GRDB
import AntaraCore

public final class SyncEngine {
    
    private let dbQueue: DatabaseQueue
    
    public init(dbQueue: DatabaseQueue) {
        self.dbQueue = dbQueue
    }
    
    public func negotiateSync(request: Org_Circle13_Antara_Protocol_SyncRequest) throws -> Org_Circle13_Antara_Protocol_SyncResponse {
        // Causal checks against local DAG tables
        // Find which messages are missing in the remote Bloom filter and return them
        var missingMessages = [Org_Circle13_Antara_Protocol_MessagePayload]()
        
        try dbQueue.read { db in
            let messages = try MessageRecord.fetchAll(db)
            for msg in messages {
                // Bloom filter lookup stub
                var payload = Org_Circle13_Antara_Protocol_MessagePayload()
                payload.messageID = Data(msg.messageId.utf8)
                payload.threadID = Data(msg.threadId.utf8)
                payload.timestamp = UInt64(msg.timestamp.timeIntervalSince1970 * 1000)
                payload.textMessage = Org_Circle13_Antara_Protocol_TextMessage.with { $0.body = msg.body }
                payload.senderIdentity = Data(msg.senderIdentity.utf8)
                missingMessages.append(payload)
            }
        }
        
        return Org_Circle13_Antara_Protocol_SyncResponse.with {
            $0.missingMessages = missingMessages
        }
    }
    
    public func integrateDelta(response: Org_Circle13_Antara_Protocol_SyncResponse) throws {
        try dbQueue.write { db in
            for protoMsg in response.missingMessages {
                let messageId = String(decoding: protoMsg.messageID, as: UTF8.self)
                let threadId = String(decoding: protoMsg.threadID, as: UTF8.self)
                let timestamp = Date(timeIntervalSince1970: Double(protoMsg.timestamp) / 1000.0)
                let sender = String(decoding: protoMsg.senderIdentity, as: UTF8.self)
                
                // If thread does not exist, insert a stub first
                let threadExists = try ThreadRecord.filter(key: threadId).fetchOne(db) != nil
                if !threadExists {
                    let stubThread = ThreadRecord(threadId: threadId, title: "Synced Chat Room", createdTime: Date())
                    try stubThread.insert(db)
                }
                
                let record = MessageRecord(
                    messageId: messageId,
                    threadId: threadId,
                    timestamp: timestamp,
                    body: protoMsg.textMessage.body,
                    vectorClockJson: "{}",
                    parentsJson: "[]",
                    senderIdentity: sender
                )
                try record.insert(db)
            }
        }
    }
}

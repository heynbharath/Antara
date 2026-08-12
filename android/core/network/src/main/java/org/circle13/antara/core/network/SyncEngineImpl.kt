package org.circle13.antara.core.network

import org.circle13.antara.core.database.MessageDao
import org.circle13.antara.core.database.MessageEntity
import org.circle13.antara.protocol.MessagePayload
import org.circle13.antara.protocol.SyncRequest
import org.circle13.antara.protocol.SyncResponse

interface SyncEngine {
    suspend fun negotiateSync(peerNodeId: ByteArray, request: SyncRequest): SyncResponse
    suspend fun integrateDelta(response: SyncResponse): Result<Unit>
}

class SyncEngineImpl(
    private val messageDao: MessageDao
) : SyncEngine {

    override suspend fun negotiateSync(peerNodeId: ByteArray, request: SyncRequest): SyncResponse {
        val remoteHeads = request.knownDagHeadsList.map { it.toHex() }.toSet()
        
        // Causal traversal placeholder:
        // Identify which messages are present locally that are missing on the remote peer.
        // We fetch messages and filter them using the Bloom filter or missing causal chains.
        val missingPayloads = mutableListOf<MessagePayload>()
        
        return SyncResponse.newBuilder()
            .addAllMissingMessages(missingPayloads)
            .build()
    }

    override suspend fun integrateDelta(response: SyncResponse): Result<Unit> {
        return try {
            for (protoMsg in response.missingMessagesList) {
                // Map the Protobuf entity back to the Room message database entity
                val entity = MessageEntity(
                    messageId = protoMsg.messageId.toHex(),
                    threadId = protoMsg.threadId.toHex(),
                    timestamp = protoMsg.timestamp,
                    body = protoMsg.textMessage.body,
                    vectorClockJson = org.json.JSONObject(protoMsg.vectorClockMap as Map<*, *>).toString(),
                    parentsJson = "[]", // TODO: Implement DAG parents extraction
                    senderIdentity = protoMsg.senderIdentity.toHex()
                )
                messageDao.insertMessage(entity)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun com.google.protobuf.ByteString.toHex(): String = toByteArray().joinToString("") { "%02x".format(it) }
}

package org.circle13.antara.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val messageId: String,
    val threadId: String,
    val timestamp: Long,
    val body: String,
    val vectorClockJson: String,  // Map of NodeID -> LogicalClock serialized to JSON
    val parentsJson: String,      // List of parent message hashes serialized to JSON
    val senderIdentity: String
)

@Entity(tableName = "threads")
data class ThreadEntity(
    @PrimaryKey val threadId: String,
    val title: String,
    val createdTime: Long
)

@Entity(tableName = "neighbors")
data class NeighborEntity(
    @PrimaryKey val nodeId: String, // Hex-encoded Node ID hash
    val rssi: Int,
    val batteryLevel: Int,
    val trustScore: Double,
    val queueDepth: Double,
    val lastSeen: Long
)

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey val destinationNodeId: String,
    val nextHopNodeId: String,
    val hopCount: Int,
    val expiresAt: Long
)

@Entity(tableName = "dtn_packets")
data class DtnPacketEntity(
    @PrimaryKey val packetId: String,
    val recipientNodeId: String,
    val ttl: Long,
    val priority: Int,
    val payloadBytes: ByteArray
)

package org.circle13.antara.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val messageId: String,
    val threadId: String,
    val timestamp: Long,
    val body: String,
    val vectorClockJson: String = "{}",
    val parentsJson: String = "[]",
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
    @PrimaryKey val nodeId: String, // SHA-256 Hex Node ID fingerprint
    val username: String = "Peer Node",
    val fullName: String = "Verified Peer",
    val publicKeyHex: String = "",
    val isVerifiedContact: Boolean = false, // True if paired via QR code
    val connectionType: String = "DIRECT_BLE", // DIRECT_BLE, WIFI_DIRECT, RELAY_HOP
    val rssi: Int = -70,
    val batteryLevel: Int = 100,
    val trustScore: Double = 0.9,
    val queueDepth: Double = 0.0,
    val lastSeen: Long = System.currentTimeMillis()
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

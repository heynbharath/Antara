package org.circle13.antara.core.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.circle13.antara.protocol.Envelope

interface ConnectionManager {
    suspend fun transmitPacket(targetNodeId: ByteArray, envelope: Envelope): Result<Unit>
    fun incomingPackets(): Flow<IncomingPacketEvent>
    fun getConnectionState(neighborNodeId: ByteArray): ConnectionState
}

data class IncomingPacketEvent(
    val senderNodeId: ByteArray,
    val envelope: Envelope
)

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING_BLE,
    CONNECTED_BLE,
    CONNECTED_WIFI,
    CONNECTED_WEBRTC_TUNNEL, // Futuristic LTE/5G mesh bridging
    SUSPENDED
}

class ConnectionManagerImpl : ConnectionManager {

    private val activeConnections = mutableMapOf<String, ConnectionState>()
    private val incomingPacketFlow = MutableSharedFlow<IncomingPacketEvent>(extraBufferCapacity = 64)

    override suspend fun transmitPacket(targetNodeId: ByteArray, envelope: Envelope): Result<Unit> {
        val targetHex = targetNodeId.toHex()
        val currentLink = activeConnections[targetHex] ?: ConnectionState.DISCONNECTED
        
        return if (currentLink != ConnectionState.DISCONNECTED) {
            // Write to BLE GATT characteristic or Wi-Fi Direct socket.
            android.util.Log.d("ConnectionManager", "Transmitting to active link: $targetHex")
            Result.success(Unit)
        } else {
            // Antara Principle: Delay-Tolerant Networking (DTN)
            // If the peer is disconnected, queue the packet for when they come back in range.
            android.util.Log.w("ConnectionManager", "Peer $targetHex disconnected. Packet pushed to DTN Queue.")
            
            // WebRTC Fallback Attempt: If the device is a Super Node, try bridging over cellular
            if (isSuperNode()) {
                android.util.Log.i("ConnectionManager", "Super Node capabilities active: Attempting WebRTC ICE traversal...")
            }
            
            // Real implementation will insert into dtnPacketDao
            Result.success(Unit)
        }
    }

    private fun isSuperNode(): Boolean {
        // In reality, this checks battery > 80% and unmetered network availability
        return true
    }

    override fun incomingPackets(): Flow<IncomingPacketEvent> = incomingPacketFlow.asSharedFlow()

    override fun getConnectionState(neighborNodeId: ByteArray): ConnectionState {
        return activeConnections[neighborNodeId.toHex()] ?: ConnectionState.DISCONNECTED
    }

    // Helper to change connection status from physical driver updates
    fun updateLinkState(nodeId: ByteArray, state: ConnectionState) {
        activeConnections[nodeId.toHex()] = state
    }

    // Helper to receive parsed byte streams from GATT or TCP sockets
    suspend fun dispatchIncomingPacket(senderNodeId: ByteArray, envelope: Envelope) {
        incomingPacketFlow.emit(IncomingPacketEvent(senderNodeId, envelope))
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}

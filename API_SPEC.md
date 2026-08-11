# API & Interface Specification

To maintain decoupling across the 6 layers of the Antara stack, all module interactions occur through strict, platform-independent interface abstractions. The following Kotlin declarations represent the API contracts for the core stack components.

---

## 1. Discovery Service Interface
Handles background BLE and Wi-Fi Aware service advertisement, scanning, and token rotation.

```kotlin
interface DiscoveryService {
    /**
     * Start broadcasting the dynamic, rotating discovery beacon.
     */
    fun startAdvertising(token: ByteArray, capabilities: Int)

    /**
     * Stop active advertising.
     */
    fun stopAdvertising()

    /**
     * Start background scanning for neighboring Antara nodes.
     * Returns a reactive flow of discovered peer tokens and connection details.
     */
    fun startScanning(): Flow<DiscoveredPeerEvent>

    /**
     * Stop scanning activities to conserve battery.
     */
    fun stopScanning()
}

data class DiscoveredPeerEvent(
    val peerAddressHash: ByteArray,
    val signalStrengthRssi: Int,
    val deviceCapabilities: Int,
    val connectionParameters: Map<String, Any>
)
```

---

## 2. Transport Link & Connection Manager
Manages raw frame segmentation, assembly, and transport socket pools.

```kotlin
interface ConnectionManager {
    /**
     * Send a serialized payload frame to a target physical node.
     * Selects BLE or Wi-Fi Direct automatically based on size and connection state.
     */
    suspend fun transmitFrame(targetNodeId: ByteArray, frame: TransportFrame): Result<Unit>

    /**
     * Flows incoming assembled data packets received from adjacent relays.
     */
    fun incomingPackets(): Flow<IncomingPacketEvent>

    /**
     * Get the active connection status of a 1-hop physical neighbor.
     */
    fun getConnectionState(neighborNodeId: ByteArray): ConnectionState
}

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING_BLE,
    CONNECTED_BLE,
    CONNECTED_WIFI,
    SUSPENDED
}
```

---

## 3. Routing Engine Interface
Resolves pathways, maintains neighbor metrics, and controls the DTN packet queue.

```kotlin
interface RoutingEngine {
    /**
     * Routes an envelope toward its destination.
     * If the target is unreachable, places the envelope in the DTN queue.
     */
    suspend fun routeEnvelope(envelope: Envelope): Result<RouteStatus>

    /**
     * Triggers active route discovery query (RREQ) across the mesh.
     */
    suspend fun discoverRoute(destinationNodeId: ByteArray): Flow<RouteEntry>

    /**
     * Returns the current neighbor link quality score (LQI) table.
     */
    fun getNeighbors(): List<NeighborLinkInfo>
}

enum class RouteStatus {
    DELIVERED_DIRECT,
    FORWARDED_TO_RELAY,
    QUEUED_IN_DTN,
    FAILED
}
```

---

## 4. Synchronization Engine Interface
Manages Merkle DAG diff evaluation and executes CRDT conflicts resolution.

```kotlin
interface SyncEngine {
    /**
     * Triggers a sync session when a physical socket connection is established.
     */
    suspend fun negotiateSync(peerNodeId: ByteArray): Result<SyncSummary>

    /**
     * Incorporates an incoming change vector and resolves logical time conflicts.
     */
    suspend fun integrateDelta(deltaPayload: SyncResponse): Result<Unit>

    /**
     * Returns the current roots/heads of the local Merkle DAG tree.
     */
    suspend fun getLocalHeads(): List<ByteArray>
}

data class SyncSummary(
    val messagesSent: Int,
    val messagesReceived: Int,
    val reconciledHeads: List<ByteArray>
)
```

---

## 5. Storage Engine Interface
Coordinates GRDB/SQLite SQLCipher encryption tables and data retention limits.

```kotlin
interface StorageEngine {
    /**
     * Encrypts and writes a messaging or routing envelope to database.
     */
    suspend fun writeSecure(key: String, payload: ByteArray): Result<Unit>

    /**
     * Reads and decrypts database entry.
     */
    suspend fun readSecure(key: String): Result<ByteArray?>

    /**
     * Deletes records that exceed configured local Time-To-Live parameters.
     */
    suspend fun pruneExpiredRecords(ttlThresholdMs: Long): Int
}
```

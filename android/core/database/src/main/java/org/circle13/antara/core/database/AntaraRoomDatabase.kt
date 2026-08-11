package org.circle13.antara.core.database

import androidx.room.Database
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE threadId = :threadId ORDER BY timestamp ASC")
    suspend fun getMessagesForThread(threadId: String): List<MessageEntity>
}

@Dao
interface NeighborDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateNeighbor(neighbor: NeighborEntity)

    @Query("SELECT * FROM neighbors WHERE lastSeen > :timestampThreshold")
    suspend fun getActiveNeighbors(timestampThreshold: Long): List<NeighborEntity>
}

@Dao
interface RouteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: RouteEntity)

    @Query("SELECT * FROM routes WHERE destinationNodeId = :destinationNodeId LIMIT 1")
    suspend fun getRouteForDestination(destinationNodeId: String): RouteEntity?
}

@Dao
interface DtnPacketDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueuePacket(packet: DtnPacketEntity)

    @Query("SELECT * FROM dtn_packets ORDER BY priority DESC, ttl ASC")
    suspend fun getQueuedPackets(): List<DtnPacketEntity>

    @Query("DELETE FROM dtn_packets WHERE packetId = :packetId")
    suspend fun removePacket(packetId: String)
}

@Database(
    entities = [
        MessageEntity::class,
        ThreadEntity::class,
        NeighborEntity::class,
        RouteEntity::class,
        DtnPacketEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AntaraRoomDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun neighborDao(): NeighborDao
    abstract fun routeDao(): RouteDao
    abstract fun dtnPacketDao(): DtnPacketDao
}

package org.circle13.antara.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE threadId = :threadId ORDER BY timestamp ASC")
    suspend fun getMessagesForThread(threadId: String): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE threadId = :threadId ORDER BY timestamp ASC")
    fun getMessagesForThreadFlow(threadId: String): Flow<List<MessageEntity>>
}

@Dao
interface NeighborDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateNeighbor(neighbor: NeighborEntity)

    @Query("SELECT * FROM neighbors ORDER BY lastSeen DESC")
    suspend fun getAllNeighbors(): List<NeighborEntity>

    @Query("SELECT * FROM neighbors ORDER BY lastSeen DESC")
    fun getAllNeighborsFlow(): Flow<List<NeighborEntity>>

    @Query("SELECT * FROM neighbors WHERE isVerifiedContact = 1 ORDER BY lastSeen DESC")
    fun getVerifiedContactsFlow(): Flow<List<NeighborEntity>>

    @Query("SELECT * FROM neighbors WHERE isVerifiedContact = 0 ORDER BY lastSeen DESC")
    fun getRelayNodesFlow(): Flow<List<NeighborEntity>>

    @Query("DELETE FROM neighbors WHERE nodeId = :nodeId")
    suspend fun deleteNeighbor(nodeId: String)
}

@Dao
interface RouteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: RouteEntity)

    @Query("SELECT * FROM routes")
    fun getAllRoutesFlow(): Flow<List<RouteEntity>>

    @Query("SELECT * FROM routes WHERE destinationNodeId = :destinationNodeId LIMIT 1")
    suspend fun getRouteForDestination(destinationNodeId: String): RouteEntity?
}

@Dao
interface DtnPacketDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueuePacket(packet: DtnPacketEntity)

    @Query("SELECT * FROM dtn_packets ORDER BY priority DESC, ttl ASC")
    suspend fun getQueuedPackets(): List<DtnPacketEntity>

    @Query("SELECT COUNT(*) FROM dtn_packets")
    fun getPacketCountFlow(): Flow<Int>

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

    companion object {
        @Volatile
        private var INSTANCE: AntaraRoomDatabase? = null

        fun getDatabase(context: Context): AntaraRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AntaraRoomDatabase::class.java,
                    "antara_secure.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

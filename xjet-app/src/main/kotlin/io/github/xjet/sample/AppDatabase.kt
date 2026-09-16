package io.github.xjet.sample

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String = "",
)

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY id DESC")
    fun all(): Flow<List<MessageEntity>>

    @Insert
    suspend fun insert(entity: MessageEntity): Long

    @Query("DELETE FROM messages")
    suspend fun clear()
}

@Database(entities = [MessageEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}

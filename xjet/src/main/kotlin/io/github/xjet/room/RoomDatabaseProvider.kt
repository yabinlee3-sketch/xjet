package io.github.xjet.room

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import io.github.xjet.core.DatabaseProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Room-backed [DatabaseProvider]. Swap storage engines by passing a different
 * implementation in `XJetConfig.database`, or by re-registering
 * `DatabaseProvider` with a new one via `XJet.override`.
 */
class RoomDatabaseProvider(
    private val database: RoomDatabase,
    private val name: String = "xnodb",
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : DatabaseProvider {

    private val daoCache = ConcurrentHashMap<Class<*>, Any>()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> dao(daoClass: Class<T>): T {
        return daoCache.getOrPut(daoClass) {
            val method = database.javaClass.methods.firstOrNull {
                daoClass.isAssignableFrom(it.returnType) && it.parameterTypes.isEmpty()
            } ?: throw IllegalStateException(
                "Cannot find a DAO accessor for ${daoClass.canonicalName} on ${database.javaClass.name}. " +
                    "Add an abstract val/fun returning the DAO to your @Database class."
            )
            method.invoke(database)
                ?: throw IllegalStateException("DAO accessor returned null for ${daoClass.canonicalName}")
        } as T
    }

    override suspend fun <T : Any> transaction(block: suspend () -> T): T {
        return withContext(dispatcher) {
            database.withTransaction { block() }
        }
    }

    override suspend fun clearAllTables() {
        withContext(dispatcher) {
            database.clearAllTables()
        }
    }

    override fun databaseName(): String = name

    companion object {
        fun <D : RoomDatabase> create(
            context: Context,
            databaseClass: Class<D>,
            name: String,
        ): RoomDatabaseProvider {
            val db = Room.databaseBuilder(context, databaseClass, name).build()
            return RoomDatabaseProvider(db, name)
        }

        inline fun <reified D : RoomDatabase> builder(
            context: Context,
            name: String,
        ): RoomDatabase.Builder<D> = Room.databaseBuilder(context, D::class.java, name)
    }
}

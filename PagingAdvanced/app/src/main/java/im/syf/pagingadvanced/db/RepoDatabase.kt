package im.syf.pagingadvanced.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        RemoteKeysEntity::class,
        RepoEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class RepoDatabase : RoomDatabase() {

    abstract fun remoteKeysDao(): RemoteKeysDao
    abstract fun reposDao(): RepoDao

    companion object {

        @Volatile
        private var INSTANCE: RepoDatabase? = null

        fun getInstance(context: Context): RepoDatabase = INSTANCE ?: synchronized(this) {
            val instance = buildDatabase(context)
            INSTANCE = instance
            instance
        }

        private fun buildDatabase(context: Context): RepoDatabase = Room.databaseBuilder(
            context.applicationContext,
            RepoDatabase::class.java,
            "Github.db"
        ).build()
    }
}

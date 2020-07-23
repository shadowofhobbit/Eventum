package iuliiaponomareva.eventum.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.transaction


@Database(entities = [Channel::class], version = 2)
abstract class ReaderDatabase : RoomDatabase() {

    abstract fun channelDao(): ChannelDao

    companion object {
        @Volatile
        private var INSTANCE: ReaderDatabase? = null

        //In version 1 columns were nullable
        private var MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.transaction {
                    database.execSQL("ALTER TABLE feeds RENAME TO old_feeds")
                    database.execSQL(
                        "CREATE TABLE feeds (feed_url TEXT NOT NULL PRIMARY KEY, " +
                                "title TEXT NOT NULL DEFAULT ''," +
                                "link TEXT NOT NULL DEFAULT ''," +
                                "description TEXT NOT NULL DEFAULT '')"
                    )
                    database.execSQL("INSERT INTO feeds SELECT * FROM old_feeds")
                    database.execSQL("DROP TABLE old_feeds")
                }
            }
        }

        fun getDatabase(context: Context): ReaderDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReaderDatabase::class.java,
                    "FeedReader.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
package iuliiaponomareva.eventum.data

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.transaction


@Database(entities = [Channel::class, News::class], version = 2)
@TypeConverters(Converters::class)
abstract class ReaderDatabase : RoomDatabase() {

    abstract fun channelDao(): ChannelDao

    abstract fun newsDao(): NewsDao

    companion object {
        @Volatile
        private var INSTANCE: ReaderDatabase? = null

        //In version 1 columns were nullable
        @VisibleForTesting
        var MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.transaction {
                    database.execSQL("ALTER TABLE feeds RENAME TO old_feeds")
                    database.execSQL(
                        "UPDATE old_feeds SET feed_url = '' WHERE feed_url ISNULL"
                    )
                    database.execSQL(
                        "UPDATE old_feeds SET title = '' WHERE title ISNULL"
                    )
                    database.execSQL(
                        "UPDATE old_feeds SET link = '' WHERE link ISNULL"
                    )
                    database.execSQL(
                        "UPDATE old_feeds SET description = '' WHERE description ISNULL"
                    )
                    database.execSQL(
                        "CREATE TABLE feeds (feed_url TEXT NOT NULL PRIMARY KEY, " +
                                "title TEXT NOT NULL DEFAULT ''," +
                                "link TEXT NOT NULL DEFAULT ''," +
                                "description TEXT NOT NULL DEFAULT '')"
                    )
                    database.execSQL("INSERT INTO feeds SELECT * FROM old_feeds")
                    database.execSQL("DROP TABLE old_feeds")
                    database.execSQL("CREATE TABLE IF NOT EXISTS news (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "title TEXT NOT NULL, link TEXT, description TEXT NOT NULL, pubDate INTEGER, channelUrl TEXT NOT NULL, " +
                            "FOREIGN KEY(`channelUrl`) REFERENCES `feeds`(`feed_url`) ON UPDATE NO ACTION ON DELETE CASCADE)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS `news_channelUrl` ON news (channelUrl)")
                }
            }
        }

        const val DATABASE_NAME = "FeedReader.db"

        fun getDatabase(context: Context): ReaderDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReaderDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
package iuliiaponomareva.eventum

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import iuliiaponomareva.eventum.data.Channel
import iuliiaponomareva.eventum.data.DbHelper
import iuliiaponomareva.eventum.data.FeedReaderContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.ArrayList

class ChannelRepository(val context: Context) {
    suspend fun load(): List<Channel> {
        return withContext(Dispatchers.IO) {
            var dbHelper: DbHelper? = null
            var db: SQLiteDatabase? = null
            var cursor: Cursor? = null
            val feeds: MutableList<Channel> =
                ArrayList()
            try {
                dbHelper = DbHelper(context)
                db = dbHelper.readableDatabase
                val columns = arrayOf(
                    FeedReaderContract.Feeds.COLUMN_NAME_FEED_URL,
                    FeedReaderContract.Feeds.COLUMN_NAME_TITLE,
                    FeedReaderContract.Feeds.COLUMN_NAME_LINK,
                    FeedReaderContract.Feeds.COLUMN_NAME_DESCRIPTION
                )
                cursor = db.query(
                    FeedReaderContract.Feeds.TABLE_NAME,
                    columns,
                    null,
                    null,
                    null,
                    null,
                    null
                )
                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    val url = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                            FeedReaderContract.Feeds.COLUMN_NAME_FEED_URL
                        )
                    )
                    val title = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                            FeedReaderContract.Feeds.COLUMN_NAME_TITLE
                        )
                    )
                    val link = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                            FeedReaderContract.Feeds.COLUMN_NAME_LINK
                        )
                    )
                    val description = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                            FeedReaderContract.Feeds.COLUMN_NAME_DESCRIPTION
                        )
                    )
                    val channel =
                        Channel(url, title, link, description)
                    feeds.add(channel)
                    cursor.moveToNext()
                }
            } finally {
                cursor?.close()
                db?.close()
                dbHelper?.close()
            }
            Log.wtf("eventum", "${Thread.currentThread().id} ${Thread.currentThread().name} should not be main")
            feeds
        }
    }
}
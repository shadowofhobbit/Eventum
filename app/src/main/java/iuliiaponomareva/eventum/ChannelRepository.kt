package iuliiaponomareva.eventum

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import iuliiaponomareva.eventum.data.Channel
import iuliiaponomareva.eventum.data.DbHelper
import iuliiaponomareva.eventum.data.FeedReaderContract
import java.util.ArrayList

class ChannelRepository {
    fun load(context: Context): List<Channel> {
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
        return feeds
    }
}
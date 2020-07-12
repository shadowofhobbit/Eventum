package iuliiaponomareva.eventum.async

import android.app.IntentService
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import androidx.core.database.sqlite.transaction
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import iuliiaponomareva.eventum.data.Channel
import iuliiaponomareva.eventum.data.DbHelper
import iuliiaponomareva.eventum.data.FeedReaderContract
import java.util.*

class ChannelService : IntentService("ChannelService") {
    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_SAVE == action) {
                val parcelables =
                    intent.getParcelableArrayExtra(NEW_CHANNELS)
                val newChannels =
                    Arrays.copyOf(
                        parcelables,
                        parcelables!!.size,
                        Array<Channel>::class.java
                    )
                handleActionSave(newChannels)
            } else if (ACTION_DELETE == action) {
                val url =
                    intent.getStringExtra(URL_TO_DELETE)
                handleActionDelete(url)
            }
            val broadcastIntent = Intent(ACTION_CHANNELS_CHANGED)
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent)
        }
    }

    private fun handleActionSave(newChannels: Array<Channel>) {
        var dbHelper: DbHelper? = null
        var db: SQLiteDatabase? = null
        try {
            dbHelper = DbHelper(this)
            db = dbHelper.writableDatabase
            db.transaction {
                for (channel in newChannels) {
                    val values = ContentValues()
                    values.put(FeedReaderContract.Feeds.COLUMN_NAME_FEED_URL, channel.url)
                    values.put(FeedReaderContract.Feeds.COLUMN_NAME_TITLE, channel.title)
                    values.put(FeedReaderContract.Feeds.COLUMN_NAME_LINK, channel.link)
                    values.put(
                        FeedReaderContract.Feeds.COLUMN_NAME_DESCRIPTION,
                        channel.description
                    )
                    db.insert(FeedReaderContract.Feeds.TABLE_NAME, null, values)
                }
            }
        } finally {
            db?.close()
            dbHelper?.close()
        }
    }

    private fun handleActionDelete(url: String?) {
        var dbHelper: DbHelper? = null
        var db: SQLiteDatabase? = null
        try {
            dbHelper = DbHelper(this)
            db = dbHelper.writableDatabase
            db.delete(
                FeedReaderContract.Feeds.TABLE_NAME,
                FeedReaderContract.Feeds.COLUMN_NAME_FEED_URL + "= '" + url + "'", null
            )
        } finally {
            db?.close()
            dbHelper?.close()
        }
    }

    companion object {
        private const val ACTION_SAVE = "iuliiaponomareva.eventum.action.SAVE"
        private const val ACTION_DELETE = "iuliiaponomareva.eventum.action.DELETE"
        const val ACTION_CHANNELS_CHANGED =
            "iuliiaponomareva.eventum.action.CHANNELS_CHANGED"
        private const val NEW_CHANNELS = "iuliiaponomareva.eventum.extra.NEW_CHANNELS"
        private const val URL_TO_DELETE = "iuliiaponomareva.eventum.extra.URL_TO_DELETE"
        @JvmStatic
        fun startActionSave(
            context: Context,
            newFeeds: Array<Channel>
        ) {
            val intent = Intent(context, ChannelService::class.java)
            intent.action = ACTION_SAVE
            intent.putExtra(NEW_CHANNELS, newFeeds)
            context.startService(intent)
        }

        @JvmStatic
        fun startActionDelete(
            context: Context,
            urlToDelete: String
        ) {
            val intent = Intent(context, ChannelService::class.java)
            intent.action = ACTION_DELETE
            intent.putExtra(URL_TO_DELETE, urlToDelete)
            context.startService(intent)
        }
    }
}
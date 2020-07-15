package iuliiaponomareva.eventum.async

import android.content.AsyncTaskLoader
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import iuliiaponomareva.eventum.ChannelRepository
import iuliiaponomareva.eventum.data.Channel
import iuliiaponomareva.eventum.data.DbHelper
import iuliiaponomareva.eventum.data.FeedReaderContract
import iuliiaponomareva.eventum.data.Reader
import java.util.*

class ChannelsLoader(
    context: Context?,
    private val reader: Reader
) : AsyncTaskLoader<List<Channel>>(
    context
) {
    private val channelRepository = ChannelRepository()
    override fun onStartLoading() {
        val channels = reader.channels
        if (channels.isEmpty()) {
            forceLoad()
        } else {
            deliverResult(listOf(*channels))
        }
    }

    override fun loadInBackground(): List<Channel> {
        return channelRepository.load(context.applicationContext)
    }

}
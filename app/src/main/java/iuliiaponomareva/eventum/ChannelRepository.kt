package iuliiaponomareva.eventum

import iuliiaponomareva.eventum.data.Channel
import iuliiaponomareva.eventum.data.ChannelDao
import iuliiaponomareva.eventum.util.RSSAndAtomParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChannelRepository(private val channelDao: ChannelDao) {
    private val parser: RSSAndAtomParser by lazy {
        RSSAndAtomParser()
    }

    suspend fun load(): List<Channel> {
        return channelDao.loadAllChannels()
    }


    suspend fun add(url: String): Channel? {
        return withContext(Dispatchers.IO) {
            val channel = parser.parseChannelInfo(url)
            channel?.let {
                channelDao.insertChannel(channel)
            }
            channel
        }
    }

    suspend fun delete(channel: Channel) {
        channelDao.deleteChannel(channel)
    }
}
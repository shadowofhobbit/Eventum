package iuliiaponomareva.eventum.async

import android.app.IntentService
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import iuliiaponomareva.eventum.data.Channel
import iuliiaponomareva.eventum.util.RSSAndAtomParser
import java.util.*

class ParseChannelService : IntentService("ParseChannelService") {
    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_PARSE_CHANNEL_INFO == action) {
                val param1 =
                    intent.getStringArrayExtra(URLS)
                handleActionParseInfo(param1!!)
            }
        }
    }

    private fun handleActionParseInfo(urls: Array<String>) {
        val newChannels: MutableList<Channel> =
            ArrayList(urls.size)
        val parser = RSSAndAtomParser()
        for (url in urls) {
            val channel = parser.parseChannelInfo(url)
            if (channel != null) {
                newChannels.add(channel)
            }
        }
        val intent = Intent(ACTION_BROADCAST_CHANNELS)
        intent.putExtra(NEW_CHANNELS, newChannels.toTypedArray())
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    companion object {
        private const val ACTION_PARSE_CHANNEL_INFO =
            "iuliiaponomareva.eventum.action.PARSE_CHANNEL_INFO"
        private const val URLS = "iuliiaponomareva.eventum.extra.URLS"
        const val NEW_CHANNELS = "iuliiaponomareva.eventum.extra.NEW_CHANNELS"
        const val ACTION_BROADCAST_CHANNELS = "iuliiaponomareva.eventum.BROADCAST"
        fun startActionParseInfo(
            context: Context,
            urls: Array<String>
        ) {
            val intent = Intent(context, ParseChannelService::class.java)
            intent.action = ACTION_PARSE_CHANNEL_INFO
            intent.putExtra(URLS, urls)
            context.startService(intent)
        }
    }
}
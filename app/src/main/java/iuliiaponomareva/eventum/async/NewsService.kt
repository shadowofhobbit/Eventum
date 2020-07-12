package iuliiaponomareva.eventum.async

import android.app.IntentService
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import iuliiaponomareva.eventum.util.RSSAndAtomParser

class NewsService : IntentService("NewsService") {
    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_LOAD_NEWS == action) {
                val urls =
                    intent.getStringArrayExtra(URLS)
                parseNews(urls!!)
            }
        }
    }

    private fun parseNews(urls: Array<String>) {
        val parser = RSSAndAtomParser()
        val intent = Intent(ACTION_BROADCAST_NEWS)
        for (url in urls) {
            val news = parser.parseNews(url)
            intent.putExtra(url, news.toTypedArray())
        }
        intent.putExtra(URLS, urls)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    companion object {
        private const val ACTION_LOAD_NEWS = "iuliiaponomareva.eventum.action.LOAD_NEWS"
        const val URLS = "iuliiaponomareva.eventum.extra.URLS"
        const val ACTION_BROADCAST_NEWS = "iuliiaponomareva.eventum.action.BROADCAST_NEWS"
        fun startActionLoadNews(
            context: Context,
            urls: Array<String>
        ) {
            val intent = Intent(context, NewsService::class.java)
            intent.action = ACTION_LOAD_NEWS
            intent.putExtra(URLS, urls)
            context.startService(intent)
        }
    }
}
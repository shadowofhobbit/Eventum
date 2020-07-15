package iuliiaponomareva.eventum.data

import iuliiaponomareva.eventum.activities.MainActivity
import iuliiaponomareva.eventum.async.NewsService
import iuliiaponomareva.eventum.async.ParseChannelService
import java.util.*

class Reader {
    private val feeds: MutableMap<String, Channel>
    val allNews: MutableSet<News>
    fun addFeed(activity: MainActivity, rssURL: String) {
        ParseChannelService.startActionParseInfo(activity, arrayOf(rssURL))
    }

    fun addFeed(channel: Channel) {
        feeds[channel.url] = channel
    }

    fun removeFeed(rssURL: String) {
        val feed = feeds.remove(rssURL)
        if (feed != null) {
            for (news in feed.news) {
                allNews.remove(news)
            }
        }
    }

    fun getFeed(url: String): Channel? {
        return feeds[url]
    }

    fun getFeeds(): Array<String> {
        return feeds.keys.toTypedArray()
    }

    fun hasFeed(url: String): Boolean {
        return feeds.containsKey(url)
    }

    val channels: Array<Channel>
        get() = feeds.values.toTypedArray()

    fun refreshAllNews(activity: MainActivity) {
        NewsService.startActionLoadNews(activity, getFeeds())
    }

    fun refreshNewsFromFeed(url: String, activity: MainActivity) {
        NewsService.startActionLoadNews(activity, arrayOf(url))
    }

    fun finishRefreshing(res: Array<News>?, url: String) {
        if (res != null) {
            Collections.addAll(allNews, *res)
            val feed = feeds[url]
            feed?.setNews(res)
        }
    }

    fun getNewsFromFeed(rssURL: String?): Set<News> {
        return Collections.unmodifiableSet(feeds[rssURL]!!.news)
    }

    fun addAll(data: List<Channel>) {
        for (channel in data) {
            addFeed(channel)
        }
    }

    companion object {
        @JvmStatic
        fun checkURL(url: String): String {
            if (!url.startsWith("http")) {
                return "http://$url"
            }
            return url
        }
    }

    init {
        feeds = HashMap()
        allNews = TreeSet(NewsComparator())
    }
}
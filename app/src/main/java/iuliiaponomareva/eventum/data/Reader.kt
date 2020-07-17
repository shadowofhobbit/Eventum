package iuliiaponomareva.eventum.data

import java.util.*
import kotlin.collections.HashMap

class Reader {
    private val feeds: MutableMap<String, Channel> = HashMap()
    private val _allNews: MutableSet<News> = TreeSet(NewsComparator())
    val allNews: Set<News>
        get() = _allNews


    fun removeFeed(rssURL: String) {
        val feed = feeds.remove(rssURL)
        if (feed != null) {
            _allNews.removeAll(feed.news)
        }
    }

    fun getFeed(url: String): Channel? {
        return feeds[url]
    }

    fun getFeeds(): Array<String> {
        return feeds.keys.toTypedArray()
    }

    fun finishRefreshing(res: Array<News>?, url: String) {
        if (res != null) {
            Collections.addAll(_allNews, *res)
            val feed = feeds[url]
            feed?.setNews(res)
        }
    }

    fun getNewsFromFeed(rssURL: String?): Set<News> {
        return Collections.unmodifiableSet(feeds[rssURL]!!.news)
    }

    fun addAll(data: List<Channel>) {
        for (channel in data) {
            feeds[channel.url] = channel
        }
    }
}
package iuliiaponomareva.eventum.data

import java.util.*

class Reader {
    private val feeds: MutableMap<String, Channel>
    val allNews: MutableSet<News>

    private fun addFeed(channel: Channel) {
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


    init {
        feeds = HashMap()
        allNews = TreeSet(NewsComparator())
    }
}
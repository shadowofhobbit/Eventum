package iuliiaponomareva.eventum

import android.util.Log
import iuliiaponomareva.eventum.data.News
import iuliiaponomareva.eventum.data.NewsDao
import iuliiaponomareva.eventum.util.RSSAndAtomParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NewsRepository(private val newsDao: NewsDao) {
    private val parser by lazy { RSSAndAtomParser() }

    suspend fun refreshNews(urls: Array<String>): Map<String, Set<News>> {
        return withContext(Dispatchers.IO) {
            val result = HashMap<String, Set<News>>()
            for (url in urls) {
                Log.wtf("eventum", "parsing")
                val news = parser.parseNews(url)
                newsDao.replaceNewsForUrl(news, url)
                result[url] = news
            }
            result
        }
    }

    fun loadAll() = newsDao.loadAllNews()
}
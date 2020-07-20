package iuliiaponomareva.eventum.data

import java.io.Serializable
import java.util.*

/**
 * Created by Julia on 15.04.2017.
 */
class NewsComparator : Comparator<News>, Serializable {
    override fun compare(news1: News, news2: News): Int {
        return if (news1.pubDate != null && news2.pubDate != null) {
            news2.pubDate.compareTo(news1.pubDate)
        } else {
            Int.MAX_VALUE
        }
    }
}
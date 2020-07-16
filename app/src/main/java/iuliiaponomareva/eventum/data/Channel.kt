package iuliiaponomareva.eventum.data

import android.text.TextUtils
import java.util.*


class Channel(
    val url: String,
    var title: String,
    val link: String?,
    val description: String?
) {

    val news: MutableSet<News> = TreeSet(NewsComparator())

    init {
        if (TextUtils.isEmpty(title)) {
            title = url
        }
    }

    override fun toString(): String {
        return title
    }

    fun setNews(news: Array<News>) {
        Collections.addAll(this.news, *news)
    }
}
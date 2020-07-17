package iuliiaponomareva.eventum.data

import iuliiaponomareva.eventum.util.HTMLFormatter
import java.util.*


class News(
    val title: String?, val link: String?, val description: String?,
    internal var pubDate: Date? = null
) {

    fun getPubDate(): Date? {
        return Date(pubDate!!.time)
    }

    fun setPubDate(pubDate: Date) {
        this.pubDate = Date(pubDate.time)
    }

    override fun toString(): String {
        return HTMLFormatter.formatNews(this)
    }

}
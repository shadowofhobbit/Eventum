package iuliiaponomareva.eventum.data

import iuliiaponomareva.eventum.util.HTMLFormatter
import java.util.*


data class News(
    val title: String, val link: String?, val description: String,
    val pubDate: Date? = null
) {

    override fun toString(): String {
        return HTMLFormatter.formatNews(this)
    }

}
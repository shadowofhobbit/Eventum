package iuliiaponomareva.eventum.util

import iuliiaponomareva.eventum.data.News
import java.text.DateFormat

object HTMLFormatter {
    private fun addNewLine(text: StringBuilder) {
        text.append("<br>")
    }

    @JvmStatic
    fun formatNews(news: News): String {
        val newsBuilder = StringBuilder()
        newsBuilder.append("<b>")
        newsBuilder.append(news.title)
        newsBuilder.append("</b>")
        if (news.getPubDate() != null) {
            addNewLine(newsBuilder)
            newsBuilder.append(DateFormat.getDateTimeInstance().format(news.getPubDate()!!))
        }
        addNewLine(newsBuilder)
        val description = news.description
        if (description != null) {
            newsBuilder.append(description)
        }
        return newsBuilder.toString()
    }
}
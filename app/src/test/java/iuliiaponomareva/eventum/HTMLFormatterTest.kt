package iuliiaponomareva.eventum

import iuliiaponomareva.eventum.data.News
import iuliiaponomareva.eventum.util.HTMLFormatter.formatNews
import org.junit.Assert
import org.junit.Test
import java.text.DateFormat
import java.util.*

class HTMLFormatterTest {
    @Test
    fun testFormatNews() {
        val title = "my title"
        val link = "http://www"
        val description = "my description"
        val date = Date()
        val news = News(title, link, description, date)
        val formattedNews = formatNews(news)
        Assert.assertTrue("No title", formattedNews.contains(title))
        val formattedDate = DateFormat.getDateTimeInstance().format(date)
        Assert.assertTrue(
            "No date in default locale format",
            formattedNews.contains(formattedDate)
        )
        Assert.assertTrue("No description", formattedNews.contains(description))
        Assert.assertFalse("Contains link", formattedNews.contains(link))
    }
}
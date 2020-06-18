package iuliiaponomareva.eventum;

import org.junit.Test;

import java.text.DateFormat;
import java.util.Date;

import iuliiaponomareva.eventum.data.News;
import iuliiaponomareva.eventum.util.HTMLFormatter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class HTMLFormatterTest {

    @Test
    public void testFormatNews() {
        String title = "my title";
        String link = "http://www";
        String description = "my description";
        Date date = new Date();
        News news = new News(title, link, description);
        news.setPubDate(date);

        String formattedNews = HTMLFormatter.formatNews(news);
        assertTrue("No title", formattedNews.contains(title));
        String formattedDate = DateFormat.getDateTimeInstance().format(date);
        assertTrue("No date in default locale format", formattedNews.contains(formattedDate));
        assertTrue("No description", formattedNews.contains(description));
        assertFalse("Contains link", formattedNews.contains(link));
    }
}
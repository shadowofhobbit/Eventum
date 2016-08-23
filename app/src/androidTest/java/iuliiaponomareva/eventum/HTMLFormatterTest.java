package iuliiaponomareva.eventum;

import junit.framework.Assert;

import org.junit.Test;

import java.text.DateFormat;
import java.util.Date;


public class HTMLFormatterTest {

    @Test
    public void testFormatNews() throws Exception {
        String title = "my title";
        String link = "http://www";
        String description = "my description";
        Date date = new Date();
        News news = new News(title, link, description);
        news.setPubDate(date);

        String formattedNews = HTMLFormatter.formatNews(news);
        Assert.assertTrue("No title", formattedNews.contains(title));
        String formattedDate = DateFormat.getDateTimeInstance().format(date);
        Assert.assertTrue("No date in default locale format", formattedNews.contains(formattedDate));
        Assert.assertTrue("No description", formattedNews.contains(description));
        Assert.assertFalse("Contains link", formattedNews.contains(link));
    }
}
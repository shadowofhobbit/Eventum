package iuliiaponomareva.eventum.util;

import java.text.DateFormat;

import iuliiaponomareva.eventum.data.News;


public final class HTMLFormatter {
    private HTMLFormatter() {

    }

    private static void addNewLine(StringBuilder text) {
        text.append("<br>");
    }

    public static String formatNews(News news) {
        StringBuilder newsBuilder = new StringBuilder();
        newsBuilder.append("<b>");
        newsBuilder.append(news.getTitle());
        newsBuilder.append("</b>");
        if (news.getPubDate() != null) {
            addNewLine(newsBuilder);
            newsBuilder.append(DateFormat.getDateTimeInstance().format(news.getPubDate()));
        }
        addNewLine(newsBuilder);
        String description = news.getDescription();
        if (description != null) {
            newsBuilder.append(description);
        }
        return newsBuilder.toString();
    }
}

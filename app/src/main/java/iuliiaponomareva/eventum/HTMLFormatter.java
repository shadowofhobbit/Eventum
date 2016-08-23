package iuliiaponomareva.eventum;

import java.text.DateFormat;


class HTMLFormatter {

	private static void addNewLine(StringBuilder text) {
		text.append("<br>");
	}

	static String formatNews(News news) {
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

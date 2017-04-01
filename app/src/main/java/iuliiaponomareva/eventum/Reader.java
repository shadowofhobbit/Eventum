package iuliiaponomareva.eventum;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


public class Reader {
    private final Map<String, Channel> feeds;
    private final Set<News> allNews;

    Reader() {
        feeds = new HashMap<>();
        allNews = new TreeSet<>();
    }

    public void addFeed(MainActivity activity, String... rssURL) {
        ParseChannelService.startActionParseInfo(activity, rssURL);
    }

    public void addFeed(Channel channel) {
        feeds.put(channel.getURL(), channel);
    }

    public void removeFeed(String rssURL) {
        Channel feed = feeds.remove(rssURL);
        if (feed != null)
            for (News news : feed.getNews())
                allNews.remove(news);
    }
    public Channel getFeed(String URL) {
        return feeds.get(URL);
    }
    public String[] getFeeds() {
        return feeds.keySet().toArray(new String[feeds.size()]);
    }
    public boolean hasFeed(String url) {
        return feeds.containsKey(url);
    }
    public Channel[] getChannels() {
        return feeds.values().toArray(new Channel[feeds.size()]);
    }

    public void refreshAllNews(MainActivity activity) {
        NewsService.startActionLoadNews(activity, getFeeds());
    }

    public Set<News> getAllNews() {
        return allNews;
    }

    public void refreshNewsFromFeed(String URL, MainActivity activity) {
        NewsService.startActionLoadNews(activity, new String[]{URL});
    }

    void finishRefreshing(News[] res, String URL) {
        if (res != null) {
            Collections.addAll(allNews, res);
            Channel feed = feeds.get(URL);
            if (feed != null)
                feed.setNews(res);
        }
    }

    public Set<News> getNewsFromFeed(String rssURL) {
        return Collections.unmodifiableSet(feeds.get(rssURL).getNews());
    }

    public static String checkURL(String URL) {
        if (!URL.startsWith("http"))
            URL = "http://" + URL;
        return URL;
    }

    public void addAll(List<Channel> data) {
        for (Channel channel : data) {
            addFeed(channel);
        }
    }


}

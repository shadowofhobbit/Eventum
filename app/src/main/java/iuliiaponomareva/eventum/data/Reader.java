package iuliiaponomareva.eventum.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import iuliiaponomareva.eventum.activities.MainActivity;
import iuliiaponomareva.eventum.async.NewsService;
import iuliiaponomareva.eventum.async.ParseChannelService;


public class Reader {
    private final Map<String, Channel> feeds;
    private final Set<News> allNews;

    public Reader() {
        feeds = new HashMap<>();
        allNews = new TreeSet<>(new NewsComparator());
    }

    public void addFeed(MainActivity activity, String... rssURL) {
        ParseChannelService.startActionParseInfo(activity, rssURL);
    }

    public void addFeed(Channel channel) {
        feeds.put(channel.getURL(), channel);
    }

    public void removeFeed(String rssURL) {
        Channel feed = feeds.remove(rssURL);
        if (feed != null) {
            for (News news : feed.getNews()) {
                allNews.remove(news);
            }
        }
    }
    public Channel getFeed(String url) {
        return feeds.get(url);
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

    public void refreshNewsFromFeed(String url, MainActivity activity) {
        NewsService.startActionLoadNews(activity, new String[]{url});
    }

    public void finishRefreshing(News[] res, String url) {
        if (res != null) {
            Collections.addAll(allNews, res);
            Channel feed = feeds.get(url);
            if (feed != null) {
                feed.setNews(res);
            }
        }
    }

    public Set<News> getNewsFromFeed(String rssURL) {
        return Collections.unmodifiableSet(feeds.get(rssURL).getNews());
    }

    public static String checkURL(String url) {
        if (!url.startsWith("http")) {
            url = "http://" + url;
        }
        return url;
    }

    public void addAll(List<Channel> data) {
        for (Channel channel : data) {
            addFeed(channel);
        }
    }


}

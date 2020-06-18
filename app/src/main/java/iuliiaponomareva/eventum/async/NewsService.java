package iuliiaponomareva.eventum.async;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Set;

import iuliiaponomareva.eventum.data.News;
import iuliiaponomareva.eventum.util.RSSAndAtomParser;

public class NewsService extends IntentService {
    private static final String ACTION_LOAD_NEWS = "iuliiaponomareva.eventum.action.LOAD_NEWS";
    public static final String URLS = "iuliiaponomareva.eventum.extra.URLS";
    public static final String ACTION_BROADCAST_NEWS =
            "iuliiaponomareva.eventum.action.BROADCAST_NEWS";

    public NewsService() {
        super("NewsService");
    }

    public static void startActionLoadNews(Context context, String[] urls) {
        Intent intent = new Intent(context, NewsService.class);
        intent.setAction(ACTION_LOAD_NEWS);
        intent.putExtra(URLS, urls);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_LOAD_NEWS.equals(action)) {
                final String[] urls = intent.getStringArrayExtra(URLS);
                parseNews(urls);
            }
        }
    }

    private void parseNews(String[] urls) {
        RSSAndAtomParser parser = new RSSAndAtomParser();
        Intent intent = new Intent(ACTION_BROADCAST_NEWS);
        for (String url : urls) {
            Set<News> news = parser.parseNews(url);
            intent.putExtra(url, news.toArray(new News[0]));
        }
        intent.putExtra(URLS, urls);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}

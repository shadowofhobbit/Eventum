package iuliiaponomareva.eventum.async;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;

import iuliiaponomareva.eventum.data.Channel;
import iuliiaponomareva.eventum.util.RSSAndAtomParser;


public class ParseChannelService extends IntentService {
    private static final String ACTION_PARSE_CHANNEL_INFO =
            "iuliiaponomareva.eventum.action.PARSE_CHANNEL_INFO";
    private static final String URLS = "iuliiaponomareva.eventum.extra.URLS";
    public static final String NEW_CHANNELS = "iuliiaponomareva.eventum.extra.NEW_CHANNELS";
    public static final String ACTION_BROADCAST_CHANNELS = "iuliiaponomareva.eventum.BROADCAST";

    public ParseChannelService() {
        super("ParseChannelService");
    }

    public static void startActionParseInfo(Context context, String[] urls) {
        Intent intent = new Intent(context, ParseChannelService.class);
        intent.setAction(ACTION_PARSE_CHANNEL_INFO);
        intent.putExtra(URLS, urls);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PARSE_CHANNEL_INFO.equals(action)) {
                final String[] param1 = intent.getStringArrayExtra(URLS);
                handleActionParseInfo(param1);
            }
        }
    }

    private void handleActionParseInfo(String[] urls) {
        List<Channel> newChannels = new ArrayList<>(urls.length);
        RSSAndAtomParser parser = new RSSAndAtomParser();
        for (String url : urls) {
            Channel channel = parser.parseChannelInfo(url);
            if (channel != null) {
                newChannels.add(channel);
            }
        }
        Intent intent = new Intent(ACTION_BROADCAST_CHANNELS);
        intent.putExtra(NEW_CHANNELS, newChannels.toArray(new Channel[0]));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


}

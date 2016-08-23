package iuliiaponomareva.eventum;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Arrays;


public class ChannelService extends IntentService {
    private static final String ACTION_SAVE = "iuliiaponomareva.eventum.action.SAVE";
    private static final String ACTION_DELETE = "iuliiaponomareva.eventum.action.DELETE";
    public static final String ACTION_CHANNELS_CHANGED =
            "iuliiaponomareva.eventum.action.CHANNELS_CHANGED";

    private static final String NEW_CHANNELS = "iuliiaponomareva.eventum.extra.NEW_CHANNELS";
    private static final String URL_TO_DELETE = "iuliiaponomareva.eventum.extra.URL_TO_DELETE";


    public ChannelService() {
        super("ChannelService");
    }


    public static void startActionSave(Context context, Channel[] newFeeds) {
        Intent intent = new Intent(context, ChannelService.class);
        intent.setAction(ACTION_SAVE);
        intent.putExtra(NEW_CHANNELS, newFeeds);
        context.startService(intent);
    }


    public static void startActionDelete(Context context, String urlToDelete) {
        Intent intent = new Intent(context, ChannelService.class);
        intent.setAction(ACTION_DELETE);
        intent.putExtra(URL_TO_DELETE, urlToDelete);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SAVE.equals(action)) {
                final Parcelable[] parcelables = intent.getParcelableArrayExtra(NEW_CHANNELS);
                Channel[] newChannels = Arrays.copyOf(parcelables,
                        parcelables.length, Channel[].class);
                handleActionSave(newChannels);
            } else if (ACTION_DELETE.equals(action)) {
                final String url = intent.getStringExtra(URL_TO_DELETE);
                handleActionDelete(url);
            }
            Intent broadcastIntent = new Intent(ACTION_CHANNELS_CHANGED);
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        }
    }


    private void handleActionSave(Channel[] newChannels) {
        DbHelper dbHelper = null;
        SQLiteDatabase db = null;
        try {
            dbHelper = new DbHelper(this);
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            for (Channel channel : newChannels) {
                ContentValues values = new ContentValues();
                values.put(FeedReaderContract.Feeds.COLUMN_NAME_FEED_URL, channel.getURL());
                values.put(FeedReaderContract.Feeds.COLUMN_NAME_TITLE, channel.getTitle());
                values.put(FeedReaderContract.Feeds.COLUMN_NAME_LINK, channel.getLink());
                values.put(FeedReaderContract.Feeds.COLUMN_NAME_DESCRIPTION,
                        channel.getDescription());
                db.insert(FeedReaderContract.Feeds.TABLE_NAME, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
            if (dbHelper != null)
                dbHelper.close();
        }
    }


    private void handleActionDelete(String url) {
        DbHelper dbHelper = null;
        SQLiteDatabase db = null;
        try {
            dbHelper = new DbHelper(this);
            db = dbHelper.getWritableDatabase();
            db.delete(FeedReaderContract.Feeds.TABLE_NAME,
                    FeedReaderContract.Feeds.COLUMN_NAME_FEED_URL + "= '" + url +"'", null);

        } finally {
            if (db != null)
                db.close();
            if (dbHelper != null)
                dbHelper.close();
        }

    }
}

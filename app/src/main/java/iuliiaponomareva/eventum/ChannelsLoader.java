package iuliiaponomareva.eventum;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



class ChannelsLoader extends AsyncTaskLoader<List<Channel>> {
    private Reader reader;

    public ChannelsLoader(Context context, Reader reader) {
        super(context);
        this.reader = reader;
    }

    @Override
    protected void onStartLoading() {
        Channel[] channels = reader.getChannels();
        if (channels.length == 0)
            forceLoad();
        else {
            List<Channel> channelList = new ArrayList<>();
            Collections.addAll(channelList, channels);
            deliverResult(channelList);
        }
    }

    @Override
    public List<Channel> loadInBackground() {
        DbHelper dbHelper = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<Channel> feeds = new ArrayList<>();
        try {
            dbHelper = new DbHelper(getContext());
            db = dbHelper.getReadableDatabase();
            String[] columns = {FeedReaderContract.Feeds.COLUMN_NAME_FEED_URL,
                    FeedReaderContract.Feeds.COLUMN_NAME_TITLE,
                    FeedReaderContract.Feeds.COLUMN_NAME_LINK,
                    FeedReaderContract.Feeds.COLUMN_NAME_DESCRIPTION};
            cursor = db.query(
                    FeedReaderContract.Feeds.TABLE_NAME,
                    columns,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String url = cursor.getString(
                        cursor.getColumnIndexOrThrow(FeedReaderContract.Feeds.COLUMN_NAME_FEED_URL));
                String title = cursor.getString(
                        cursor.getColumnIndexOrThrow(FeedReaderContract.Feeds.COLUMN_NAME_TITLE));
                String link = cursor.getString(
                        cursor.getColumnIndexOrThrow(FeedReaderContract.Feeds.COLUMN_NAME_LINK));
                String description = cursor.getString(
                        cursor.getColumnIndexOrThrow(FeedReaderContract.Feeds.COLUMN_NAME_DESCRIPTION));
                Channel channel = new Channel(url, title, link, description);
                feeds.add(channel);
                cursor.moveToNext();
            }

        } finally {
            if (cursor != null)
                cursor.close();
            if (db != null)
                db.close();
            if (dbHelper != null)
                dbHelper.close();
        }
        return feeds;
    }
}

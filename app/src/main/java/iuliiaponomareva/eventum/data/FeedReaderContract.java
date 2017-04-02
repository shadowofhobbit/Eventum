package iuliiaponomareva.eventum.data;

import android.provider.BaseColumns;

public final class FeedReaderContract {
    private FeedReaderContract() {
    }
    public abstract static class Feeds implements BaseColumns {
        public static final String TABLE_NAME = "feeds";
        public static final String COLUMN_NAME_FEED_URL = "feed_url";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_LINK = "link";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
    }
}

package iuliiaponomareva.eventum.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedReaderContract.Feeds.TABLE_NAME + " ("
                    + FeedReaderContract.Feeds.COLUMN_NAME_FEED_URL + TEXT_TYPE + " PRIMARY KEY,"
                    + FeedReaderContract.Feeds.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP
                    + FeedReaderContract.Feeds.COLUMN_NAME_LINK + TEXT_TYPE + COMMA_SEP
                    + FeedReaderContract.Feeds.COLUMN_NAME_DESCRIPTION + TEXT_TYPE
                    + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedReaderContract.Feeds.TABLE_NAME;

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FeedReader.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}

package iuliiaponomareva.eventum.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import iuliiaponomareva.eventum.data.FeedReaderContract.Feeds

class DbHelper(context: Context?) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
    }

    override fun onDowngrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        onUpgrade(db, oldVersion, newVersion)
    }

    companion object {
        private const val TEXT_TYPE = " TEXT"
        private const val COMMA_SEP = ","
        private const val SQL_CREATE_ENTRIES = ("CREATE TABLE " + Feeds.TABLE_NAME + " ("
                + Feeds.COLUMN_NAME_FEED_URL + TEXT_TYPE + " PRIMARY KEY,"
                + Feeds.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP
                + Feeds.COLUMN_NAME_LINK + TEXT_TYPE + COMMA_SEP
                + Feeds.COLUMN_NAME_DESCRIPTION + TEXT_TYPE
                + " )")
        private const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "FeedReader.db"
    }
}
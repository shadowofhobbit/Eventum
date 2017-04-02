package iuliiaponomareva.eventum.data;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.test.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class DbHelperTest {

    @Before
    public void deleteDB() {
        InstrumentationRegistry.getTargetContext().deleteDatabase(DbHelper.DATABASE_NAME);
    }

    @Test
    public void testCreateDatabase() {
        SQLiteOpenHelper dbHelper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            Context context = InstrumentationRegistry.getTargetContext();
            dbHelper = new DbHelper(context);
            database = dbHelper.getWritableDatabase();
            assertEquals(true, database.isOpen());

            cursor = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
            assertTrue("The database has not been created correctly", cursor.moveToFirst());

            boolean foundTable = false;
            do {
                String tableName = cursor.getString(0);
                if (tableName.equals(FeedReaderContract.Feeds.TABLE_NAME))
                    foundTable = true;
            } while((!foundTable) && cursor.moveToNext());
            assertTrue("The feeds table has not been created", foundTable);

            cursor = database.rawQuery("PRAGMA table_info("
                            + FeedReaderContract.Feeds.TABLE_NAME + ")",
                    null);
            assertTrue("Unable to query the database for table information", cursor.moveToFirst());

            Set<String> columns = new HashSet<>();
            columns.add(FeedReaderContract.Feeds.COLUMN_NAME_TITLE);
            columns.add(FeedReaderContract.Feeds.COLUMN_NAME_FEED_URL);
            columns.add(FeedReaderContract.Feeds.COLUMN_NAME_DESCRIPTION);
            columns.add(FeedReaderContract.Feeds.COLUMN_NAME_LINK);

            int columnNameIndex = cursor.getColumnIndex("name");
            do {
                String columnName = cursor.getString(columnNameIndex);
                columns.remove(columnName);
            } while(cursor.moveToNext());

            assertTrue("The database doesn't contain all the columns", columns.isEmpty());
        } finally {
            if (cursor != null)
                cursor.close();
            if (database != null)
                database.close();
            if (dbHelper != null)
                dbHelper.close();
        }

    }
}
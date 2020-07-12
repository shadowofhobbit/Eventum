package iuliiaponomareva.eventum.data

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*

class DbHelperTest {
    @Before
    fun deleteDB() {
        InstrumentationRegistry.getInstrumentation().targetContext
            .deleteDatabase(DbHelper.DATABASE_NAME)
    }

    @Test
    fun testCreateDatabase() {
        var dbHelper: SQLiteOpenHelper? = null
        var database: SQLiteDatabase? = null
        var cursor: Cursor? = null
        try {
            val context =
                InstrumentationRegistry.getInstrumentation()
                    .targetContext
            dbHelper = DbHelper(context)
            database = dbHelper.getWritableDatabase()
            Assert.assertTrue(database.isOpen)
            cursor = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)
            Assert.assertTrue(
                "The database has not been created correctly",
                cursor.moveToFirst()
            )
            var foundTable = false
            do {
                val tableName = cursor.getString(0)
                if (tableName == FeedReaderContract.Feeds.TABLE_NAME) foundTable = true
            } while (!foundTable && cursor.moveToNext())
            Assert.assertTrue("The feeds table has not been created", foundTable)
            cursor = database.rawQuery(
                "PRAGMA table_info("
                        + FeedReaderContract.Feeds.TABLE_NAME + ")",
                null
            )
            Assert.assertTrue(
                "Unable to query the database for table information",
                cursor.moveToFirst()
            )
            val columns: MutableSet<String> =
                HashSet()
            columns.add(FeedReaderContract.Feeds.COLUMN_NAME_TITLE)
            columns.add(FeedReaderContract.Feeds.COLUMN_NAME_FEED_URL)
            columns.add(FeedReaderContract.Feeds.COLUMN_NAME_DESCRIPTION)
            columns.add(FeedReaderContract.Feeds.COLUMN_NAME_LINK)
            val columnNameIndex = cursor.getColumnIndex("name")
            do {
                val columnName = cursor.getString(columnNameIndex)
                columns.remove(columnName)
            } while (cursor.moveToNext())
            Assert.assertTrue(
                "The database doesn't contain all the columns",
                columns.isEmpty()
            )
        } finally {
            cursor?.close()
            database?.close()
            dbHelper?.close()
        }
    }
}
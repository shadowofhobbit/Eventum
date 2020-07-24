package iuliiaponomareva.eventum.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val channel = Channel("https://example.com/rss", "title", "", "")

    @get:Rule val migrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ReaderDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )
    @get:Rule val rule = InstantTaskExecutorRule()

    private lateinit var dbHelper: DbVersion1Helper

    @Before
    fun setUp() {
        dbHelper = DbVersion1Helper(
            ApplicationProvider.getApplicationContext(),
            TEST_DB_NAME
        )
        SqliteDatabaseTestHelper.createTable(dbHelper)
    }

    @After
    fun tearDown() {
        SqliteDatabaseTestHelper.clearDatabase(dbHelper)
    }

    @Test
    @Throws(IOException::class)
    fun migrationFrom1To2ContainsCorrectData() {
        // Create the database with the initial version 1 schema
        SqliteDatabaseTestHelper.insertChannel("https://example.com/rss", "title",
            null, "", dbHelper)
        migrationTestHelper.runMigrationsAndValidate(
            TEST_DB_NAME, 2, true,
            ReaderDatabase.MIGRATION_1_2
        )

        val latestDb: ReaderDatabase = getMigratedRoomDatabase()
        val data: LiveData<List<Channel>> =
            latestDb.channelDao().loadAllChannels()
        data.observeForever {  }
        assertNotNull(data.value)
        val loadedChannel = data.value!![0]
        assertEquals(channel.url, loadedChannel.url)
        assertEquals(channel.title, loadedChannel.title)
        assertEquals(channel.link, loadedChannel.link)
        assertEquals(channel.description, loadedChannel.description)
    }

    @Test
    @Throws(IOException::class)
    fun startInVersion2ContainsCorrectData() {
        val db = migrationTestHelper.createDatabase(TEST_DB_NAME, 2)
        insertChannel(channel, db)
        db.close()

        val database: ReaderDatabase = getMigratedRoomDatabase()
        val data: LiveData<List<Channel>> =
            database.channelDao().loadAllChannels()
        data.observeForever {  }
        assertNotNull(data.value)
        val loadedChannel = data.value!![0]
        assertEquals(channel.url, loadedChannel.url)
        assertEquals(channel.title, loadedChannel.title)
        assertEquals(channel.link, loadedChannel.link)
        assertEquals(channel.description, loadedChannel.description)
    }

    private fun getMigratedRoomDatabase(): ReaderDatabase {
        val database: ReaderDatabase = Room.databaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ReaderDatabase::class.java, TEST_DB_NAME
        )
            .addMigrations(ReaderDatabase.MIGRATION_1_2)
            .build()
        migrationTestHelper.closeWhenFinished(database)
        return database
    }

    private fun insertChannel(
        channel: Channel,
        db: SupportSQLiteDatabase
    ) {
        val values = ContentValues()
        values.put("feed_url", channel.url)
        values.put("title", channel.title)
        values.put("link", channel.link)
        values.put("description", channel.description)
        db.insert("feeds", SQLiteDatabase.CONFLICT_REPLACE, values)
    }


    internal class SqliteDatabaseTestHelper {

        companion object {
            fun insertChannel(
                feedUrl: String,
                title: String?,
                link: String?,
                description: String?,
                helper: DbVersion1Helper
            ) {
                val db = helper.writableDatabase
                val values = ContentValues()
                values.put("feed_url", feedUrl)
                values.put("title", title)
                values.put("link", link)
                values.put("description", description)
                db.insertWithOnConflict(
                    "feeds", null, values,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
                db.close()
            }

            fun createTable(helper: DbVersion1Helper) {
                val db = helper.writableDatabase
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS feeds ( feed_url TEXT PRIMARY KEY, title TEXT, " +
                            "link TEXT, description TEXT )"
                )
                db.close()
            }

            fun clearDatabase(helper: DbVersion1Helper) {
                val db = helper.writableDatabase
                db.execSQL("DROP TABLE IF EXISTS feeds")
                db.close()
            }
        }
    }

    companion object {
        private const val TEST_DB_NAME = "test.db"
    }
}

internal class DbVersion1Helper(context: Context?, databaseName: String?) :
    SQLiteOpenHelper(
        context,
        databaseName,
        null,
        DATABASE_VERSION
    ) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE feeds ( feed_url TEXT PRIMARY KEY, title TEXT," +
                "link TEXT, description TEXT )")
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

    }

    companion object {
        const val DATABASE_VERSION = 1
    }
}
package iuliiaponomareva.eventum.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface NewsDao {
    @Transaction
    @Query("SELECT * FROM news")
    fun loadAllNews(): LiveData<List<News>>

    @Transaction
    @Query("SELECT * from news WHERE channelUrl = :url")
    fun loadNewsByUrl(url: String): LiveData<List<News>>

    @Insert
    suspend fun saveNews(news: Set<News>)

    @Transaction
    @Query("DELETE from news WHERE channelUrl = :channelUrl")
    suspend fun deleteNewsForUrl(channelUrl: String)

    @Transaction
    suspend fun replaceNewsForUrl(news: Set<News>, channelUrl: String) {
        deleteNewsForUrl(channelUrl)
        saveNews(news)
    }
}
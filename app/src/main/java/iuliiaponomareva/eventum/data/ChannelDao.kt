package iuliiaponomareva.eventum.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ChannelDao {
    @Transaction
    @Query("SELECT * FROM feeds")
    fun loadAllChannels(): LiveData<List<Channel>>

    @Insert
    suspend fun insertChannel(channel: Channel): Long

    @Delete
    suspend fun deleteChannel(channel: Channel)

}
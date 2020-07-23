package iuliiaponomareva.eventum.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ChannelDao {
    @Query("SELECT * FROM feeds")
    fun loadAllChannels(): LiveData<List<Channel>>

    @Insert
    suspend fun insertChannel(channel: Channel): Long

    @Delete
    suspend fun deleteChannel(channel: Channel)

}
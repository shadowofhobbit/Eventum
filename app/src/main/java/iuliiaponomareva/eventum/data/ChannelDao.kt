package iuliiaponomareva.eventum.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ChannelDao {
    @Query("SELECT * FROM feeds")
    suspend fun loadAllChannels(): List<Channel>

    @Insert
    suspend fun insertChannel(channel: Channel): Long

    @Delete
    suspend fun deleteChannel(channel: Channel)

}
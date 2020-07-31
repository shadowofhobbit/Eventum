package iuliiaponomareva.eventum.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import iuliiaponomareva.eventum.util.HTMLFormatter
import java.util.*

@Entity(
    tableName = "news", foreignKeys = [
        ForeignKey(
            onDelete = CASCADE, entity = Channel::class,
            parentColumns = ["feed_url"], childColumns = ["channelUrl"]
        )],
    indices = [Index(name = "news_channelUrl", value = ["channelUrl"])]
)
data class News(
    val title: String, val link: String?, val description: String,
    val pubDate: Date? = null, val channelUrl: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    override fun toString(): String {
        return HTMLFormatter.formatNews(this)
    }

}
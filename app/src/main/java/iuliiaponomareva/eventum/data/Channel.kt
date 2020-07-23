package iuliiaponomareva.eventum.data

import android.os.Parcelable
import android.text.TextUtils
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
@Entity(tableName = "feeds")
class Channel(
    @PrimaryKey
    @ColumnInfo(name = "feed_url")
    val url: String,
    @ColumnInfo(defaultValue = "")
    var title: String,
    @ColumnInfo(defaultValue = "")
    val link: String,
    @ColumnInfo(defaultValue = "")
    val description: String
)  : Parcelable {

    @IgnoredOnParcel
    @Ignore
    val news: MutableSet<News> = TreeSet(NewsComparator())

    init {
        if (TextUtils.isEmpty(title)) {
            title = url
        }
    }

    override fun toString(): String {
        return title
    }

    fun setNews(news: Array<News>) {
        Collections.addAll(this.news, *news)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Channel

        if (url != other.url) return false

        return true
    }

    override fun hashCode(): Int {
        return url.hashCode()
    }
}
package iuliiaponomareva.eventum.data

import android.os.Parcel
import android.os.Parcelable
import iuliiaponomareva.eventum.util.HTMLFormatter
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.TypeParceler
import java.util.*

@Parcelize
@TypeParceler<Date, News.DateParceler>
class News(
    val title: String?, val link: String?, val description: String?,
    internal var pubDate: Date? = null
) : Parcelable {

    fun getPubDate(): Date? {
        return Date(pubDate!!.time)
    }

    fun setPubDate(pubDate: Date) {
        this.pubDate = Date(pubDate.time)
    }

    override fun toString(): String {
        return HTMLFormatter.formatNews(this)
    }

    object DateParceler : Parceler<Date> {

        override fun create(parcel: Parcel) = Date(parcel.readLong())

        override fun Date.write(parcel: Parcel, flags: Int) = parcel.writeLong(time)
    }
}
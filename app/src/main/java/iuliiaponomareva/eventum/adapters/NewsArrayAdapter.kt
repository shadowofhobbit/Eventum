package iuliiaponomareva.eventum.adapters

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Html
import android.text.Html.ImageGetter
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.Target
import iuliiaponomareva.eventum.R
import iuliiaponomareva.eventum.adapters.NewsArrayAdapter.Companion.DEFAULT_TAG
import iuliiaponomareva.eventum.data.News

class NewsArrayAdapter(
    aContext: Context,
    news: List<News>
) : ArrayAdapter<News>(aContext, R.layout.news_list_item, news) {

    val picasso: Picasso = Picasso.get()

    var channelURL: String? = null

    private class ViewHolder {
        lateinit var textView: TextView
    }

    fun cancel() {
        channelURL?.let { picasso.cancelTag(it) }
        picasso.cancelTag(DEFAULT_TAG)
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val view: View
        val holder: ViewHolder
        if (convertView == null) {
            val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.news_list_item, parent, false)
            holder = ViewHolder()
            holder.textView = view.findViewById(R.id.news_textview)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.textView.text = Html.fromHtml(
                getItem(position).toString(),
                Html.FROM_HTML_MODE_LEGACY,
                MyImageGetter(this, holder.textView), null
            )
        } else {
            holder.textView.text = Html.fromHtml(
                getItem(position).toString(),
                MyImageGetter(this, holder.textView), null
            )
        }
        return view
    }

    companion object {
        const val DEFAULT_TAG = "all"
    }

}

internal class MyDrawable(res: Resources) :
    BitmapDrawable(res, null as Bitmap?) {
    fun setDrawable(drawable: Drawable?) {
        this.drawable = drawable
    }

    private var drawable: Drawable? = null
    override fun draw(canvas: Canvas) {
        drawable?.draw(canvas)
    }
}

internal class MyImageGetter(
    private val newsArrayAdapter: NewsArrayAdapter,
    private val view: TextView?
) :
    ImageGetter, Target {
    private val width: Int
    private var source: String? = null
    private var image: MyDrawable? = null
    override fun getDrawable(source: String): Drawable {
        var source = source
        image = MyDrawable(newsArrayAdapter.context.resources)
        if (source.startsWith("//")) {
            source = "http:$source"
        }
        this.source = source
        newsArrayAdapter.picasso.load(source).tag(newsArrayAdapter.channelURL ?: DEFAULT_TAG)
            .resize(width, 0)
            .onlyScaleDown()
            .into(this)
        return image!!
    }

    override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
        if (source != null && bitmap != null) {
            val image: Drawable = BitmapDrawable(
                newsArrayAdapter.context.resources,
                bitmap
            )
            image.setBounds(0, 0, image.intrinsicWidth, image.intrinsicHeight)
            this.image!!.setBounds(0, 0, image.intrinsicWidth, image.intrinsicHeight)
            this.image!!.setDrawable(image)
            view!!.invalidate()
            view.text = view.text
        }
    }

    override fun onBitmapFailed(
        exception: Exception,
        errorDrawable: Drawable
    ) {
    }

    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

    init {
        val metrics = DisplayMetrics()
        (newsArrayAdapter.context as Activity).windowManager.defaultDisplay
            .getMetrics(metrics)
        width = metrics.widthPixels
    }
}
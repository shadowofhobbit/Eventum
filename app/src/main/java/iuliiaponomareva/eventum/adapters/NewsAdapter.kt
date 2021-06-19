package iuliiaponomareva.eventum.adapters

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import iuliiaponomareva.eventum.R
import iuliiaponomareva.eventum.adapters.NewsAdapter.Companion.DEFAULT_TAG
import iuliiaponomareva.eventum.data.News
import iuliiaponomareva.eventum.data.NewsComparator
import kotlinx.android.synthetic.main.news_list_item.view.*

class NewsAdapter(private val newsListener: (News) -> Unit) : RecyclerView.Adapter<NewsViewHolder>() {
    val picasso: Picasso = Picasso.get()
    var channelURL: String? = null
    private var _allNews = listOf<News>()
    var news: List<News>
        get() = _allNews
        set(value) {
            _allNews = value.sortedWith(kotlin.Comparator(NewsComparator()::compare))
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.news_list_item, parent, false)
        val newsViewHolder = NewsViewHolder(view)
        newsViewHolder.itemView.setOnClickListener {
            newsListener(_allNews[newsViewHolder.bindingAdapterPosition])
        }
        return newsViewHolder
    }

    override fun getItemCount(): Int = _allNews.size

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.itemView.newsTextView.text = HtmlCompat.fromHtml(
            _allNews.elementAt(position).toString(),
            HtmlCompat.FROM_HTML_MODE_LEGACY,
            NewsImageGetter(this, holder.itemView.newsTextView), null
        )
    }

    fun cancel() {
        channelURL?.let { picasso.cancelTag(it) }
        picasso.cancelTag(DEFAULT_TAG)
    }

    companion object {
        const val DEFAULT_TAG = "all"
    }
}

class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

private class NewsDrawable(res: Resources) : BitmapDrawable(res, null as Bitmap?) {
    fun setDrawable(drawable: Drawable?) {
        this.drawable = drawable
    }

    private var drawable: Drawable? = null

    override fun draw(canvas: Canvas) {
        drawable?.draw(canvas)
    }
}

private class NewsImageGetter(
    private val newsArrayAdapter: NewsAdapter,
    private val view: TextView
) :
    Html.ImageGetter, Target {
    private val width: Int
    private var source: String? = null
    private var image: NewsDrawable? = null

    override fun getDrawable(source: String): Drawable {
        var drawableSource = source
        image = NewsDrawable(view.context.resources)
        if (drawableSource.startsWith("//")) {
            drawableSource = "http:$drawableSource"
        }
        this.source = drawableSource
        newsArrayAdapter.picasso.load(drawableSource).tag(newsArrayAdapter.channelURL ?: DEFAULT_TAG)
            .resize(width, 0)
            .onlyScaleDown()
            .into(this)
        return image!!
    }

    override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
        if (source != null) {
            val image: Drawable = BitmapDrawable(
                view.context.resources,
                bitmap
            )
            image.setBounds(0, 0, image.intrinsicWidth, image.intrinsicHeight)
            this.image!!.setBounds(0, 0, image.intrinsicWidth, image.intrinsicHeight)
            this.image!!.setDrawable(image)
            view.invalidate()
            view.text = view.text
        }
    }

    override fun onBitmapFailed(
        exception: Exception,
        errorDrawable: Drawable?
    ) {
    }

    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

    init {
        val metrics = DisplayMetrics()
        getActivity(view.context)?.windowManager?.defaultDisplay?.getMetrics(metrics)
        width = metrics.widthPixels
    }

    private fun getActivity(context: Context): Activity? {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) {
                return currentContext
            }
            currentContext = currentContext.baseContext
        }
        return null
    }
}

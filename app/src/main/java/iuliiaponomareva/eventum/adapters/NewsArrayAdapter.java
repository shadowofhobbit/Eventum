package iuliiaponomareva.eventum.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import iuliiaponomareva.eventum.R;
import iuliiaponomareva.eventum.data.News;

public class NewsArrayAdapter extends ArrayAdapter<News> {
    private Context context;

    Picasso getPicasso() {
        return picasso;
    }

    private Picasso picasso;

    String getChannelURL() {
        return channelURL;
    }

    private String channelURL;

    public NewsArrayAdapter(Context context, List<News> news) {
        super(context, R.layout.news_list_item, news);
        this.context = context;
        picasso = Picasso.with(context);
    }


    private static class ViewHolder {

        private TextView textView;
    }

    public void setChannelURL(String channelURL) {
        this.channelURL = channelURL;
    }

    public void cancel() {
        picasso.cancelTag(channelURL);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.news_list_item, parent, false);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.news_textview);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.textView.setText(Html.fromHtml(getItem(position).toString(),
                    Html.FROM_HTML_MODE_LEGACY,
                    new MyImageGetter(this, holder.textView), null));
        } else {
            holder.textView.setText(Html.fromHtml(getItem(position).toString(),
                    new MyImageGetter(this, holder.textView), null));
        }
        return convertView;
    }

}
class MyDrawable extends BitmapDrawable {
    MyDrawable(Resources res) {
        super(res, (Bitmap) null);
    }

    void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    private Drawable drawable;

    @Override
    public void draw(Canvas canvas) {
        if (drawable != null) {
            drawable.draw(canvas);
        }
    }
}

class MyImageGetter implements Html.ImageGetter, Target {
    private NewsArrayAdapter newsArrayAdapter;
    private int width;
    private String source;
    private MyDrawable image;
    private TextView view;

    MyImageGetter(NewsArrayAdapter newsArrayAdapter, TextView view) {
        super();
        this.newsArrayAdapter = newsArrayAdapter;
        this.view = view;
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) newsArrayAdapter.getContext()).getWindowManager().getDefaultDisplay()
                .getMetrics(metrics);
        width = metrics.widthPixels;
    }

    @Override
    public Drawable getDrawable(String source) {
        image = new MyDrawable(newsArrayAdapter.getContext().getResources());
        if (source.startsWith("//")) {
            source = "http:" + source;
        }
        this.source = source;
        newsArrayAdapter.getPicasso().load(source).tag(newsArrayAdapter.getChannelURL())
                .resize(width, 0)
                .onlyScaleDown().into(this);
        return image;
    }


    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        if ((source != null) && (bitmap != null)) {
            Drawable image = new BitmapDrawable(newsArrayAdapter.getContext().getResources(),
                    bitmap);
            image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
            this.image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
            this.image.setDrawable(image);
            view.invalidate();
            view.setText(view.getText());
        }
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {

    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {

    }
}


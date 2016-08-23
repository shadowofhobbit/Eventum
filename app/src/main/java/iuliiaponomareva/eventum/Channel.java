package iuliiaponomareva.eventum;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

class Channel implements Parcelable{

    private String URL;
    private String title;
    private String link;
    private String description;
    private Set<News> news = new TreeSet<>();

    Channel(String URL, String title, String link, String description) {
        this.URL = URL;
        this.title = TextUtils.isEmpty(title) ? URL : title;
        this.description = description;
        this.link = link;
    }

    protected Channel(Parcel in) {
        URL = in.readString();
        title = in.readString();
        link = in.readString();
        description = in.readString();
    }

    public static final Creator<Channel> CREATOR = new Creator<Channel>() {
        @Override
        public Channel createFromParcel(Parcel in) {
            return new Channel(in);
        }

        @Override
        public Channel[] newArray(int size) {
            return new Channel[size];
        }
    };

    @Override
    public String toString() {
        return getTitle();
    }

    public String getURL() {
        return URL;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getDescription() {
        return description;
    }

    public Set<News> getNews() {
        return news;
    }

    public void setNews(News[] news) {
        Collections.addAll(this.news, news);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(URL);
        parcel.writeString(title);
        parcel.writeString(link);
        parcel.writeString(description);
    }
}



package iuliiaponomareva.eventum.data;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Date;

import iuliiaponomareva.eventum.util.HTMLFormatter;

public class News implements Parcelable {

    private final String title;
    private final String link;
    private final String description;
    private Date pubDate;

    public News(String title, String link, String description) {
        this.title = title;
        this.description = description;
        this.link = link;
    }

    private News(Parcel in) {
        title = in.readString();
        link = in.readString();
        description = in.readString();
        pubDate = new Date(in.readLong());
    }

    public static final Creator<News> CREATOR = new Creator<News>() {
        @Override
        public News createFromParcel(Parcel in) {
            return new News(in);
        }

        @Override
        public News[] newArray(int size) {
            return new News[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getDescription() {
        return description;
    }

    public Date getPubDate() {
        return new Date(pubDate.getTime());
    }

    public void setPubDate(@NonNull Date pubDate) {
        this.pubDate = new Date(pubDate.getTime());
    }


    @Override
    public String toString() {
        return HTMLFormatter.formatNews(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(link);
        parcel.writeString(description);
        parcel.writeLong(pubDate.getTime());
    }
}

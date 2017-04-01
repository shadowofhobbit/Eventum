package iuliiaponomareva.eventum;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Date;

class News implements Comparable<News>, Parcelable {

    private String title;
    private String link;
    private String description;
    private Date pubDate;

    News(String title, String link, String description) {
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

    String getLink() {
        return link;
    }

    String getDescription() {
        return description;
    }

    Date getPubDate() {
        return pubDate;
    }

    void setPubDate(Date pubDate) {
        this.pubDate = pubDate;
    }

    @Override
    public int compareTo(@NonNull News other) {
        if ((getPubDate() != null) && (other.getPubDate() != null))
            return other.getPubDate().compareTo(this.getPubDate());
        else
            return Integer.MAX_VALUE;
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

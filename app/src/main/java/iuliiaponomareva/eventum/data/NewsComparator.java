package iuliiaponomareva.eventum.data;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by Julia on 15.04.2017.
 */
public class NewsComparator implements Comparator<News>, Serializable {

    @Override
    public int compare(News news1, News news2) {
        if ((news1.getPubDate() != null) && (news2.getPubDate() != null)) {
            return news2.getPubDate().compareTo(news1.getPubDate());
        } else {
            return Integer.MAX_VALUE;
        }
    }
}

package iuliiaponomareva.eventum.data

import android.provider.BaseColumns

class FeedReaderContract private constructor() {
    object Feeds : BaseColumns {
        const val TABLE_NAME = "feeds"
        const val COLUMN_NAME_FEED_URL = "feed_url"
        const val COLUMN_NAME_TITLE = "title"
        const val COLUMN_NAME_LINK = "link"
        const val COLUMN_NAME_DESCRIPTION = "description"
    }
}
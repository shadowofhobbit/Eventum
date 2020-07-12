package iuliiaponomareva.eventum.util

import android.util.Xml
import iuliiaponomareva.eventum.data.Channel
import iuliiaponomareva.eventum.data.News
import iuliiaponomareva.eventum.data.NewsComparator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class RSSAndAtomParser {
    private enum class Format {
        RSS, ATOM
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(
            CONNECT_TIMEOUT.toLong(),
            TimeUnit.SECONDS
        )
        .readTimeout(
            READ_TIMEOUT.toLong(),
            TimeUnit.SECONDS
        ).build()

    fun parseChannelInfo(url: String): Channel? {
        var stream: InputStream? = null
        var channel: Channel? = null
        val parser = Xml.newPullParser()
        try {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            stream = getInputStream(url)
            parser.setInput(stream, null)
            parser.nextTag()
            parser.require(XmlPullParser.START_TAG, null, null)
            if (parser.name == RSS_FEED_START_TAG) {
                channel = parseRSSChannel(parser, url)
            } else if (parser.name == ATOM_FEED_START_TAG) {
                channel = parseAtomFeed(parser, url)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } finally {
            if (stream != null) {
                try {
                    stream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return channel
    }

    private fun parseRSSChannel(
        parser: XmlPullParser,
        url: String
    ): Channel? {
        var title: String? = null
        var link: String? = null
        var description: String? = null
        var channel: Channel? = null
        try {
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType == XmlPullParser.START_TAG
                    && parser.name == "channel"
                ) {
                    while (parser.next() != XmlPullParser.END_TAG) {
                        if (parser.eventType == XmlPullParser.START_TAG) {
                            when (val name = parser.name) {
                                "title" -> title = readTag(parser, name)
                                "description" -> description = readTag(parser, name)
                                "link" -> link = readTag(parser, name)
                                else -> {
                                }
                            }
                        }
                    }
                }
            }
            channel = Channel(url, title ?: url, link, description)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        }
        return channel
    }

    private fun parseAtomFeed(
        parser: XmlPullParser,
        url: String
    ): Channel? {
        var title: String? = null
        var link: String? = null
        var description: String? = null
        var channel: Channel? = null
        try {
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType == XmlPullParser.START_TAG) {
                    val name = parser.name
                    when (name) {
                        "title" -> title = readTag(parser, name)
                        "summary" -> description = readTag(parser, name)
                        "link" -> {
                            parser.require(
                                XmlPullParser.START_TAG,
                                NS,
                                "link"
                            )
                            link = parser.getAttributeValue(NS, "href")
                        }
                        else -> {
                        }
                    }
                }
            }
            channel = Channel(url, title ?: url, link, description)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        }
        return channel
    }

    @Throws(IOException::class)
    private fun getInputStream(url: String): InputStream {
        val request = Request.Builder().url(url).build()
        val response: Response
        response = client.newCall(request).execute()
        return response.body()!!.byteStream()
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTag(parser: XmlPullParser, tag: String): String {
        parser.require(XmlPullParser.START_TAG, NS, tag)
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        parser.require(XmlPullParser.END_TAG, NS, tag)
        return result
    }

    fun parseNews(url: String): Set<News?> {
        var result: Set<News?> = TreeSet(NewsComparator())
        var stream: InputStream? = null
        val parser = Xml.newPullParser()
        try {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            stream = getInputStream(url)
            parser.setInput(stream, null)
            parser.nextTag()
            parser.require(XmlPullParser.START_TAG, null, null)
            if (parser.name == RSS_FEED_START_TAG) {
                result = parseRSSNews(parser)
            } else if (parser.name == ATOM_FEED_START_TAG) {
                result = parseAtomNews(parser)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } finally {
            if (stream != null) {
                try {
                    stream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return result
    }

    private fun parseRSSNews(parser: XmlPullParser): Set<News> {
        val result: MutableSet<News> = TreeSet(NewsComparator())
        try {
            while (!(parser.next() == XmlPullParser.END_TAG
                        && parser.name == RSS_FEED_START_TAG)
            ) {
                if (parser.eventType == XmlPullParser.START_TAG
                    && parser.name == "channel"
                ) {
                    while (!(parser.next() == XmlPullParser.END_TAG
                                && parser.name == "channel")
                    ) {
                        if (parser.eventType == XmlPullParser.START_TAG
                            && parser.name == "item"
                        ) {
                            val news = parseItem(parser)
                            result.add(news)
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        }
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun parseItem(parser: XmlPullParser): News {
        var title = ""
        var link = ""
        var description = ""
        var pubDate: String? = null
        while (!(parser.next() == XmlPullParser.END_TAG && parser.name == "item")) {
            if (parser.eventType == XmlPullParser.START_TAG) {
                val name = parser.name
                when (name) {
                    "title" -> title = readTag(parser, name)
                    "description", "summary" -> description = readTag(parser, name)
                    "link" -> link = readTag(parser, name)
                    "pubDate" -> pubDate = readTag(parser, name)
                    else -> {
                    }
                }
            }
        }
        val news = News(title, link, description)
        if (pubDate != null) {
            news.setPubDate(
                parseDate(
                    pubDate,
                    Format.RSS
                )!!
            )
        }
        return news
    }

    private fun parseAtomNews(parser: XmlPullParser): Set<News> {
        val result: MutableSet<News> = TreeSet(NewsComparator())
        try {
            while (!(parser.next() == XmlPullParser.END_TAG
                        && parser.name == ATOM_FEED_START_TAG)
            ) {
                if (parser.eventType == XmlPullParser.START_TAG
                    && parser.name == "entry"
                ) {
                    val news = parseEntry(parser)
                    result.add(news)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        }
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun parseEntry(parser: XmlPullParser): News {
        var title = ""
        var link: String? = ""
        var description = ""
        var pubDate: String? = null
        while (!(parser.next() == XmlPullParser.END_TAG && parser.name == "entry")) {
            if (parser.eventType == XmlPullParser.START_TAG) {
                val name = parser.name
                when (name) {
                    "title" -> title = readTag(parser, name)
                    "summary", "description" -> description = readTag(parser, name)
                    "link" -> {
                        parser.require(
                            XmlPullParser.START_TAG,
                            NS,
                            "link"
                        )
                        link = parser.getAttributeValue(NS, "href")
                    }
                    "updated" -> pubDate = readTag(parser, name)
                    else -> {
                    }
                }
            }
        }
        val news = News(title, link, description)
        if (pubDate != null) {
            news.setPubDate(
                parseDate(
                    pubDate,
                    Format.ATOM
                )!!
            )
        }
        return news
    }

    companion object {
        private const val READ_TIMEOUT = 10
        private const val CONNECT_TIMEOUT = 15
        private const val RSS_FEED_START_TAG = "rss"
        private const val ATOM_FEED_START_TAG = "feed"
        private val NS: String? = null
        private fun parseDate(
            dateToParse: String,
            format: Format
        ): Date? {
            //example of rss
            //Sat, 05 Apr 2014 09:13:01 +0400
            //Tue, 15 Jul 2014 02:10:39 PDT
            //Thu, 16 Oct 2014 20:00:00 Z
            //example of atom
            //2002-10-02T10:00:00-05:00
            //2002-10-02T15:00:00Z
            //2002-10-02T15:00:00.05Z
            var pubDate: Date? = null
            val template: String = if (format == Format.RSS) {
                if (Character.isDigit(dateToParse[dateToParse.length - 1])) {
                    "EEE, dd MMM yyyy HH:mm:ss Z"
                } else if (dateToParse.endsWith("Z")) {
                    "EEE, dd MMM yyyy HH:mm:ss 'Z'"
                } else {
                    "EEE, dd MMM yyyy HH:mm:ss z"
                }
            } else {
                if (dateToParse.endsWith("Z")) {
                    if (dateToParse.contains(".")) {
                        "yyyy-MM-dd'T'HH:mm:ss.S'Z'"
                    } else {
                        "yyyy-MM-dd'T'HH:mm:ss'Z'"
                    }
                } else {
                    if (dateToParse.contains(".")) {
                        "yyyy-MM-dd'T'HH:mm:ss.SZZZZZ"
                    } else if (dateToParse.contains(" ")) {
                        "yyyy-MM-dd HH:mm:ss" //2016-01-28 13:26:03
                    } else {
                        "yyyy-MM-dd'T'HH:mm:ssZZZZZ"
                    }
                }
            }
            val formatter: DateFormat =
                SimpleDateFormat(template, Locale.US)
            try {
                pubDate = formatter.parse(dateToParse)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return pubDate
        }
    }
}
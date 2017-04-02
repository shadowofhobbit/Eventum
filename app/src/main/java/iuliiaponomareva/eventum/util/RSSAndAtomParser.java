package iuliiaponomareva.eventum.util;

import android.support.annotation.NonNull;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import iuliiaponomareva.eventum.data.Channel;
import iuliiaponomareva.eventum.data.News;

public class RSSAndAtomParser {

    private static final int READ_TIMEOUT = 10000;
    private static final int CONNECT_TIMEOUT = 15000;

    private enum Format { RSS, ATOM }

    private static final String RSS_FEED_START_TAG = "rss";
    private static final String ATOM_FEED_START_TAG = "feed";
    private static final String NS = null;

    private static Date parseDate(String dateToParse, Format format) {
        //example of rss
        //Sat, 05 Apr 2014 09:13:01 +0400
        //Tue, 15 Jul 2014 02:10:39 PDT
        //Thu, 16 Oct 2014 20:00:00 Z
        //example of atom
        //2002-10-02T10:00:00-05:00
        //2002-10-02T15:00:00Z
        //2002-10-02T15:00:00.05Z

        Date pubDate = null;
        String template;
        if (format == Format.RSS) {
            if (Character.isDigit(dateToParse.charAt(dateToParse.length() - 1))) {
                template = "EEE, dd MMM yyyy HH:mm:ss Z";
            } else if (dateToParse.endsWith("Z")) {
                template = "EEE, dd MMM yyyy HH:mm:ss 'Z'";
            } else {
                template = "EEE, dd MMM yyyy HH:mm:ss z";
            }
        } else {
            if (dateToParse.endsWith("Z")) {
                if (dateToParse.contains(".")) {
                    template = "yyyy-MM-dd'T'HH:mm:ss.S'Z'";
                } else {
                    template = "yyyy-MM-dd'T'HH:mm:ss'Z'";
                }
            } else {
                if (dateToParse.contains(".")) {
                    template = "yyyy-MM-dd'T'HH:mm:ss.SZZZZZ";
                } else if (dateToParse.contains(" ")) {
                    template = "yyyy-MM-dd HH:mm:ss"; //2016-01-28 13:26:03
                } else {
                    template = "yyyy-MM-dd'T'HH:mm:ssZZZZZ";
                }
            }

        }
        DateFormat formatter = new SimpleDateFormat(template, Locale.US);
        try {
            pubDate = formatter.parse(dateToParse);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return pubDate;
    }

    public Channel parseChannelInfo(String url) {
        InputStream stream = null;
        Channel channel = null;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            stream = getInputStream(url);
            parser.setInput(stream, null);
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, null, null);
            if (parser.getName().equals(RSS_FEED_START_TAG)) {
                channel = parseRSSChannel(parser, url);
            } else if (parser.getName().equals(ATOM_FEED_START_TAG)) {
                channel = parseAtomFeed(parser, url);
            }
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return channel;

    }

    private Channel parseRSSChannel(XmlPullParser parser, String url) {
        String title = null, link = null, description = null;
        Channel channel = null;
        try {
            while (parser.next() != XmlPullParser.END_TAG) {
                if ((parser.getEventType() == XmlPullParser.START_TAG)
                        && (parser.getName().equals("channel"))) {
                    while ((parser.next() != XmlPullParser.END_TAG)) {
                        if (parser.getEventType() == XmlPullParser.START_TAG) {
                            String name = parser.getName();
                            switch (name) {
                                case "title":
                                    title = readTag(parser, name);
                                    break;
                                case "description":
                                    description = readTag(parser, name);
                                    break;
                                case "link":
                                    link = readTag(parser, name);
                                    break;
                                default:
                            }
                        }
                    }

                }
            }
            channel = new Channel(url, title, link, description);

        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
        return channel;
    }

    private Channel parseAtomFeed(XmlPullParser parser, String url) {
        String title = null, link = null, description = null;
        Channel channel = null;
        try {
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() == XmlPullParser.START_TAG) {
                    String name = parser.getName();
                    switch (name) {
                        case "title":
                            title = readTag(parser, name);
                            break;
                        case "summary":
                            description = readTag(parser, name);
                            break;
                        case "link":
                            parser.require(XmlPullParser.START_TAG, NS, "link");
                            link = parser.getAttributeValue(NS, "href");
                            break;
                        default:
                    }
                }
            }
            channel = new Channel(url, title, link, description);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
        return channel;
    }


    private InputStream getInputStream(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setUseCaches(false);
        connection.connect();
        return connection.getInputStream();
    }

    private String readTag(XmlPullParser parser, String tag) throws IOException,
            XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, NS, tag);
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        parser.require(XmlPullParser.END_TAG, NS, tag);
        return result;
    }

    public Set<News> parseNews(String url) {
        Set<News> result = new TreeSet<>();
        InputStream stream = null;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            stream = getInputStream(url);
            parser.setInput(stream, null);
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, null, null);
            if (parser.getName().equals(RSS_FEED_START_TAG)) {
                result = parseRSSNews(parser);
            } else if (parser.getName().equals(ATOM_FEED_START_TAG)) {
                result = parseAtomNews(parser);
            }
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private Set<News> parseRSSNews(XmlPullParser parser) {
        Set<News> result = new TreeSet<>();
        try {

            while (!((parser.next() == XmlPullParser.END_TAG)
                    && (parser.getName().equals(RSS_FEED_START_TAG)))) {
                if ((parser.getEventType() == XmlPullParser.START_TAG)
                        && (parser.getName().equals("channel"))) {
                    while (!((parser.next() == XmlPullParser.END_TAG)
                            && (parser.getName().equals("channel")))) {
                        if ((parser.getEventType() == XmlPullParser.START_TAG)
                                && (parser.getName().equals("item"))) {
                            News news = parseItem(parser);
                            result.add(news);
                        }
                    }
                }
            }

        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
        return result;
    }

    @NonNull
    private News parseItem(XmlPullParser parser) throws XmlPullParserException, IOException {
        String title = "", link = "", description = "", pubDate = null;
        while (!((parser.next() == XmlPullParser.END_TAG) && (parser.getName().equals("item")))) {
            if (parser.getEventType() == XmlPullParser.START_TAG) {
                String name = parser.getName();
                switch (name) {
                    case "title":
                        title = readTag(parser, name);
                        break;
                    case "description":
                        description = readTag(parser, name);
                        break;
                    case "summary":
                        description = readTag(parser, name);
                        break;
                    case "link":
                        link = readTag(parser, name);
                        break;
                    case "pubDate":
                        pubDate = readTag(parser, name);
                        break;
                    default:
                        break;
                }
            }
        }
        News news = new News(title, link, description);
        if (pubDate != null) {
            news.setPubDate(parseDate(pubDate, Format.RSS));
        }
        return news;
    }

    private Set<News> parseAtomNews(XmlPullParser parser) {
        Set<News> result = new TreeSet<>();
        try {
            while (!((parser.next() == XmlPullParser.END_TAG)
                    && parser.getName().equals(ATOM_FEED_START_TAG))) {
                if ((parser.getEventType() == XmlPullParser.START_TAG)
                        && (parser.getName().equals("entry"))) {
                    News news = parseEntry(parser);
                    result.add(news);
                }
            }

        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
        return result;
    }

    @NonNull
    private News parseEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        String title = "", link = "", description = "", pubDate = null;
        while (!((parser.next() == XmlPullParser.END_TAG) && (parser.getName().equals("entry")))) {
            if (parser.getEventType() == XmlPullParser.START_TAG) {
                String name = parser.getName();
                switch (name) {
                    case "title":
                        title = readTag(parser, name);
                        break;
                    case "summary":
                        description = readTag(parser, name);
                        break;
                    case "description":
                        description = readTag(parser, name);
                        break;
                    case "link":
                        parser.require(XmlPullParser.START_TAG, NS, "link");
                        link = parser.getAttributeValue(NS, "href");
                        break;
                    case "updated":
                        pubDate = readTag(parser, name);
                        break;
                    default:
                        //nop
                }

            }
        }
        News news = new News(title, link, description);
        if (pubDate != null) {
            news.setPubDate(parseDate(pubDate, Format.ATOM));
        }
        return news;
    }


}

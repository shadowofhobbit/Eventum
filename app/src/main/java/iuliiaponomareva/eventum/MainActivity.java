package iuliiaponomareva.eventum;


import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements AddFeedDialogFragment.AddFeedDialogListener,
        RemoveFeedDialogFragment.RemoveFeedDialogListener,
        SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<List<Channel>> {
    private static final String ADD_FEED_FRAGMENT_TAG = "addNewFeed";
    private static final String REMOVE_FEED_FRAGMENT_TAG = "removeFeed";
    private Reader reader;
    private ArrayAdapter<Channel> drawerAdapter;
    private List<News> news;
    private NewsArrayAdapter newsAdapter;
    private ListView newsListView;
    public final static String NEWS_LINK = "iuliiaponomareva.eventum.NEWS_LINK";
    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private ActionBarDrawerToggle drawerToggle;
    private Channel selectedChannel;
    private Channel all;
    private SwipeRefreshLayout refreshLayout;
    private BroadcastReceiver receiver;
    private final static int CHANNEL_LOADER_ID = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        reader = new Reader();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        all = new Channel(getString(R.string.all_feeds), getString(R.string.all_feeds), "", "");
        setUpChannelsView();
        setUpNewsView();
        createBroadcastReceiver();
        getLoaderManager();
    }

    private void setUpChannelsView() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerListView = (ListView) findViewById(R.id.drawer);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open,
                R.string.drawer_close) {};
        drawerAdapter = new ArrayAdapter<>(MainActivity.this,
                R.layout.drawer_list_item, new ArrayList<Channel>());
        drawerLayout.addDrawerListener(drawerToggle);
        drawerListView.setAdapter(drawerAdapter);
        drawerListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                drawerListView.setItemChecked(position, true);
                selectedChannel = (Channel) parent.getItemAtPosition(position);
                String selectedFeed = selectedChannel.getURL();
                drawerLayout.closeDrawer(drawerListView);
                newsAdapter.cancel();
                news.clear();
                if (isConnectedToNetwork()) {
                    if (selectedChannel.equals(all)) {
                        reader.refreshAllNews(MainActivity.this);
                    }
                    else
                        reader.refreshNewsFromFeed(selectedFeed, MainActivity.this);
                } else {
                    if (selectedChannel.equals(all))
                        news.addAll(reader.getAllNews());
                    else
                        news.addAll(reader.getNewsFromFeed(selectedFeed));
                    createToast(R.string.no_internet);
                }
                setTitle(selectedChannel.getTitle());

            }
        });

    }

    private void setUpNewsView() {
        newsListView = (ListView) findViewById(R.id.all_news);
        newsListView.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                News selectedNews = (News) parent.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, ViewPageActivity.class);
                intent.putExtra(NEWS_LINK, selectedNews.getLink());
                startActivity(intent);
            }
        });
        news = new ArrayList<>();
        newsAdapter = new NewsArrayAdapter(MainActivity.this, news);
        newsListView.setAdapter(newsAdapter);
        newsListView.setEmptyView(findViewById(R.id.empty_textview));
        refreshLayout = ((SwipeRefreshLayout) findViewById(R.id.swipe_to_refresh));
        refreshLayout.setOnRefreshListener(this);
        newsListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                int topRowVerticalPosition = (newsListView == null
                        || newsListView.getChildCount() == 0) ?
                        0 : newsListView.getChildAt(0).getTop();
                refreshLayout.setEnabled((firstVisibleItem == 0) && (topRowVerticalPosition >= 0));
            }
        });
    }

    private void createBroadcastReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(ParseChannelService.ACTION_BROADCAST_CHANNELS)) {
                    Parcelable[] parcelables = intent.getParcelableArrayExtra(
                            ParseChannelService.NEW_CHANNELS);
                    Channel[] newChannels = Arrays.copyOf(parcelables, parcelables.length,
                            Channel[].class);
                    notifyAboutNewChannel(newChannels);
                } else if (action.equals(ChannelService.ACTION_CHANNELS_CHANGED)) {
                    getLoaderManager().restartLoader(CHANNEL_LOADER_ID, null, MainActivity.this);
                } else if (action.equals(NewsService.ACTION_BROADCAST_NEWS)) {
                    String[] urls = intent.getStringArrayExtra(NewsService.URLS);
                    for (String url : urls) {
                        Parcelable[] parcelables = intent.getParcelableArrayExtra(url);
                        News[] news = Arrays.copyOf(parcelables, parcelables.length, News[].class);
                        reader.finishRefreshing(news, url);
                    }
                    if (urls.length == 1)
                        showNews(reader.getNewsFromFeed(urls[0]));
                    else
                        showNews(reader.getAllNews());
                }
            }
        };
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch(item.getItemId()) {
            case R.id.addFeedItem:
                addFeed();
                return true;
            case R.id.removeFeedItem:
                removeFeed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addFeed() {
        DialogFragment newFragment = new AddFeedDialogFragment();
        newFragment.show(getSupportFragmentManager(), ADD_FEED_FRAGMENT_TAG);
    }

    private void removeFeed() {
        DialogFragment newFragment = new RemoveFeedDialogFragment();
        Bundle args = new Bundle();
        args.putStringArray(RemoveFeedDialogFragment.FEEDS, reader.getFeeds());
        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(), REMOVE_FEED_FRAGMENT_TAG);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(ParseChannelService.ACTION_BROADCAST_CHANNELS);
        intentFilter.addAction(NewsService.ACTION_BROADCAST_NEWS);
        intentFilter.addAction(ChannelService.ACTION_CHANNELS_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
                intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (receiver != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveSelectedChannel();
    }

    private void saveSelectedChannel() {
        if (selectedChannel != null) {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.chosen_feed_pref_label), selectedChannel.getURL());
            editor.apply();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getLoaderManager().restartLoader(CHANNEL_LOADER_ID, null, this);
    }

    @Override
    public void addChosenFeed(String feedUrl) {
        if (isConnectedToNetwork()) {
            feedUrl = Reader.checkURL(feedUrl);
            if (reader.hasFeed(feedUrl)) {
                createToast(feedUrl + " " + getString(R.string.has_already_been_added));
            }
            else {
                reader.addFeed(this, feedUrl);
            }
        }
        else {
            createToast(R.string.no_internet);
        }
    }

    private void notifyAboutNewChannel(Channel[] newChannels) {
        if ((newChannels != null) && (newChannels.length != 0)) {
            ChannelService.startActionSave(this, newChannels);
            for (Channel channel: newChannels)
                reader.addFeed(channel);
            getLoaderManager().restartLoader(CHANNEL_LOADER_ID, null, this);
        }
        else
            createToast(R.string.error_adding_feed);
    }

    private void createToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    private void createToast(int messageRes) {
        Toast toast = Toast.makeText(getApplicationContext(), messageRes, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    @Override
    public void removeChosenFeed(String feedURL) {
        if ((selectedChannel != null)&&(selectedChannel.getURL().equals(feedURL)))
            selectedChannel = all;
        drawerAdapter.remove(reader.getFeed(feedURL));
        reader.removeFeed(feedURL);
        drawerAdapter.notifyDataSetChanged();
        ChannelService.startActionDelete(this, feedURL);

    }

    private boolean isConnectedToNetwork() {
        ConnectivityManager manager = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }


    @Override
    public void onRefresh() {
        refreshNews();
        refreshLayout.setRefreshing(false);
    }

    private void refreshNews() {
        if (selectedChannel == null)
            selectedChannel = all;
        if (isConnectedToNetwork()) {
            news.clear();
            if (selectedChannel.equals(all))
                reader.refreshAllNews(this);
            else
                reader.refreshNewsFromFeed(selectedChannel.getURL(), this);
            newsAdapter.notifyDataSetChanged();
        }
        else {
            createToast(R.string.no_internet);
        }
    }

    private void showNews(Set<News> newsSet) {
        newsAdapter.addAll(newsSet);
        newsAdapter.setChannelURL(selectedChannel.getURL());
        newsAdapter.notifyDataSetChanged();
        setTitle(selectedChannel.getTitle());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        drawerLayout.removeDrawerListener(drawerToggle);
    }

    @Override
    public Loader<List<Channel>> onCreateLoader(int id, Bundle args) {
        return new ChannelsLoader(MainActivity.this, reader);
    }

    @Override
    public void onLoadFinished(Loader<List<Channel>> loader, List<Channel> data) {
        reader.addAll(data);
        drawerAdapter.clear();
        TextView textView = (TextView)findViewById(R.id.empty_textview);
        if (data.isEmpty())
            textView.setText(R.string.no_feeds_added_yet);
        else
            textView.setText(R.string.loading);
        drawerAdapter.add(all);
        drawerAdapter.addAll(data);
        drawerAdapter.notifyDataSetChanged();
        selectedChannel = reader.getFeed(getSelectedFeedFromPreferences());
        int position = drawerAdapter.getPosition(selectedChannel);
        drawerListView.setSelection(position);
        drawerListView.setItemChecked(position, true);
        if (selectedChannel == null)
            setTitle(R.string.chosen_feed_default);
        else
            setTitle(selectedChannel.getTitle());
        refreshNews();
    }

    @Override
    public void onLoaderReset(Loader<List<Channel>> loader) {

    }

    private String getSelectedFeedFromPreferences() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String defaultValue = getString(R.string.chosen_feed_default);
        return sharedPref.getString("chosenFeed", defaultValue);
    }
}



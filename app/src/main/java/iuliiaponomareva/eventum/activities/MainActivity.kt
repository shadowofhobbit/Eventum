package iuliiaponomareva.eventum.activities

import android.content.*
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.AbsListView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import iuliiaponomareva.eventum.ChannelRepository
import iuliiaponomareva.eventum.R
import iuliiaponomareva.eventum.adapters.NewsArrayAdapter
import iuliiaponomareva.eventum.async.ChannelService
import iuliiaponomareva.eventum.async.ChannelService.Companion.startActionDelete
import iuliiaponomareva.eventum.async.ChannelService.Companion.startActionSave
import iuliiaponomareva.eventum.async.NewsService
import iuliiaponomareva.eventum.async.ParseChannelService
import iuliiaponomareva.eventum.data.Channel
import iuliiaponomareva.eventum.data.News
import iuliiaponomareva.eventum.data.Reader
import iuliiaponomareva.eventum.data.Reader.Companion.checkURL
import iuliiaponomareva.eventum.fragments.AddFeedDialogFragment
import iuliiaponomareva.eventum.fragments.AddFeedDialogFragment.AddFeedDialogListener
import iuliiaponomareva.eventum.fragments.RemoveFeedDialogFragment
import iuliiaponomareva.eventum.fragments.RemoveFeedDialogFragment.RemoveFeedDialogListener
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), AddFeedDialogListener,
    RemoveFeedDialogListener, OnRefreshListener {
    private lateinit var reader: Reader
    private lateinit var drawerAdapter: ArrayAdapter<Channel>
    private lateinit var news: MutableList<News>
    private lateinit var newsAdapter: NewsArrayAdapter
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private var selectedChannel: Channel? = null
    private lateinit var all: Channel
    private var receiver: BroadcastReceiver? = null
    private lateinit var viewModel: ChannelViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        reader = Reader()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        all = Channel(
            getString(R.string.all_feeds),
            getString(R.string.all_feeds),
            "",
            ""
        )
        setUpChannelsView()
        setUpNewsView()
        createBroadcastReceiver()
        val repository = ChannelRepository(applicationContext)
        viewModel = ViewModelProvider(this, ChannelViewModelFactory(repository))
            .get(ChannelViewModel::class.java)
        viewModel.getChannels().observe(this, androidx.lifecycle.Observer {
            onLoadFinished(it)
        })
    }

    private fun setUpChannelsView() {
        drawerToggle = ActionBarDrawerToggle(
            this, drawerLayout, R.string.drawer_open,
            R.string.drawer_close
        )
        drawerAdapter = ArrayAdapter(
            this@MainActivity,
            R.layout.drawer_list_item, ArrayList()
        )
        drawerLayout.addDrawerListener(drawerToggle)
        drawer.adapter = drawerAdapter
        drawer.onItemClickListener = OnItemClickListener { parent, _, position, _ ->
            drawer.setItemChecked(position, true)
            selectedChannel =
                parent.getItemAtPosition(position) as Channel
            val selectedFeed = selectedChannel!!.url
            drawerLayout.closeDrawer(drawer)
            newsAdapter.cancel()
            news.clear()
            if (isConnectedToNetwork) {
                if (selectedChannel == all) {
                    reader.refreshAllNews(this@MainActivity)
                } else {
                    reader.refreshNewsFromFeed(selectedFeed, this@MainActivity)
                }
            } else {
                if (selectedChannel == all) {
                    news.addAll(reader.allNews)
                } else {
                    news.addAll(reader.getNewsFromFeed(selectedFeed))
                }
                createToast(R.string.no_internet)
            }
            title = selectedChannel!!.title
        }
    }

    private fun setUpNewsView() {
        newsListView.onItemClickListener = OnItemClickListener { parent, _, position, _ ->
            val selectedNews = parent.getItemAtPosition(position) as News
            val intent = Intent(this@MainActivity, ViewPageActivity::class.java)
            intent.putExtra(NEWS_LINK, selectedNews.link)
            startActivity(intent)
        }
        news = ArrayList()
        newsAdapter = NewsArrayAdapter(this@MainActivity, news)
        newsListView.adapter = newsAdapter
        newsListView.emptyView = emptyView
        refreshLayout.setOnRefreshListener(this)
        newsListView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(
                view: AbsListView,
                scrollState: Int
            ) {
            }

            override fun onScroll(
                view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int,
                totalItemCount: Int
            ) {
                val topRowVerticalPosition = if (newsListView == null
                    || newsListView?.childCount == 0
                ) 0 else newsListView!!.getChildAt(0).top
                refreshLayout.isEnabled = firstVisibleItem == 0 && topRowVerticalPosition >= 0
            }
        })
    }

    private fun createBroadcastReceiver() {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent
            ) {
                when (intent.action) {
                    ParseChannelService.ACTION_BROADCAST_CHANNELS -> {
                        val parcelables = intent.getParcelableArrayExtra(
                            ParseChannelService.NEW_CHANNELS
                        )!!
                        val newChannels =
                            Arrays.copyOf(
                                parcelables, parcelables.size,
                                Array<Channel>::class.java
                            )
                        notifyAboutNewChannel(newChannels)
                    }
                    ChannelService.ACTION_CHANNELS_CHANGED -> {
                        viewModel.onChannelsChanged()
                    }
                    NewsService.ACTION_BROADCAST_NEWS -> {
                        val urls =
                            intent.getStringArrayExtra(NewsService.URLS)!!
                        for (url in urls) {
                            val parcelables2 =
                                intent.getParcelableArrayExtra(url)!!
                            val news = Arrays.copyOf(
                                parcelables2, parcelables2.size,
                                Array<News>::class.java
                            )
                            reader.finishRefreshing(news, url)
                        }
                        if (urls.size == 1) {
                            showNews(reader.getNewsFromFeed(urls[0]))
                        } else {
                            showNews(reader.allNews)
                        }
                    }
                    else -> {
                    }
                }
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_activity_action_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (drawerToggle.onOptionsItemSelected(item)) {
            true
        } else when (item.itemId) {
            R.id.addFeedItem -> {
                addFeed()
                true
            }
            R.id.removeFeedItem -> {
                removeFeed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun addFeed() {
        val newFragment: DialogFragment = AddFeedDialogFragment()
        newFragment.show(supportFragmentManager, ADD_FEED_FRAGMENT_TAG)
    }

    private fun removeFeed() {
        val newFragment: DialogFragment = RemoveFeedDialogFragment()
        val args = Bundle()
        args.putStringArray(RemoveFeedDialogFragment.FEEDS, reader.getFeeds())
        newFragment.arguments = args
        newFragment.show(
            supportFragmentManager,
            REMOVE_FEED_FRAGMENT_TAG
        )
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(ParseChannelService.ACTION_BROADCAST_CHANNELS)
        intentFilter.addAction(NewsService.ACTION_BROADCAST_NEWS)
        intentFilter.addAction(ChannelService.ACTION_CHANNELS_CHANGED)
        LocalBroadcastManager.getInstance(this).registerReceiver(
            receiver!!,
            intentFilter
        )
    }

    override fun onPause() {
        super.onPause()
        if (receiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver!!)
        }
    }

    override fun onStop() {
        super.onStop()
        saveSelectedChannel()
    }

    private fun saveSelectedChannel() {
        if (selectedChannel != null) {
            val sharedPref = getPreferences(Context.MODE_PRIVATE)
            sharedPref.edit {
                putString(getString(R.string.chosen_feed_pref_label), selectedChannel!!.url)
            }
        }
    }

    override fun addChosenFeed(feedUrl: String) {
        if (isConnectedToNetwork) {
            val checkedUrl = checkURL(feedUrl)
            if (reader.hasFeed(checkedUrl)) {
                createToast(checkedUrl + " " + getString(R.string.has_already_been_added))
            } else {
                reader.addFeed(this, checkedUrl)
            }
        } else {
            createToast(R.string.no_internet)
        }
    }

    private fun notifyAboutNewChannel(newChannels: Array<Channel>?) {
        if (newChannels != null && newChannels.isNotEmpty()) {
            startActionSave(this, newChannels)
            for (channel in newChannels) {
                reader.addFeed(channel)
            }
        } else {
            createToast(R.string.error_adding_feed)
        }
    }

    private fun createToast(message: String) {
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0)
        toast.show()
    }

    private fun createToast(messageRes: Int) {
        val toast = Toast.makeText(applicationContext, messageRes, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0)
        toast.show()
    }

    override fun removeChosenFeed(feed: String) {
        if (selectedChannel != null && selectedChannel?.url == feed) {
            selectedChannel = all
        }
        drawerAdapter.remove(reader.getFeed(feed))
        reader.removeFeed(feed)
        drawerAdapter.notifyDataSetChanged()
        startActionDelete(this, feed)
    }

    private val isConnectedToNetwork: Boolean
        get() {
            val manager = applicationContext
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = manager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }

    override fun onRefresh() {
        refreshNews()
        refreshLayout.isRefreshing = false
    }

    private fun refreshNews() {
        if (selectedChannel == null) {
            selectedChannel = all
        }
        if (isConnectedToNetwork) {
            news.clear()
            if (selectedChannel == all) {
                reader.refreshAllNews(this)
            } else {
                reader.refreshNewsFromFeed(selectedChannel!!.url, this)
            }
            newsAdapter.notifyDataSetChanged()
        } else {
            createToast(R.string.no_internet)
        }
    }

    private fun showNews(newsSet: Set<News>) {
        newsAdapter.addAll(newsSet)
        newsAdapter.channelURL = selectedChannel?.url
        newsAdapter.notifyDataSetChanged()
        title = selectedChannel?.title
    }

    override fun onDestroy() {
        super.onDestroy()
        drawerLayout.removeDrawerListener(drawerToggle)
    }


    private fun onLoadFinished(data: List<Channel>) {
        reader.addAll(data)
        drawerAdapter.clear()
        if (data.isEmpty()) {
            emptyView.setText(R.string.no_feeds_added_yet)
        } else {
            emptyView.setText(R.string.loading)
        }
        drawerAdapter.add(all)
        drawerAdapter.addAll(data)
        drawerAdapter.notifyDataSetChanged()
        selectedChannel = reader.getFeed(selectedFeedFromPreferences)
        val position = drawerAdapter.getPosition(selectedChannel)
        drawer.setSelection(position)
        drawer.setItemChecked(position, true)
        if (selectedChannel == null) {
            setTitle(R.string.chosen_feed_default)
        } else {
            title = selectedChannel!!.title
        }
        refreshNews()
    }

    private val selectedFeedFromPreferences: String
        get() {
            val sharedPref = getPreferences(Context.MODE_PRIVATE)
            val defaultValue = getString(R.string.chosen_feed_default)
            return sharedPref.getString("chosenFeed", defaultValue)!!
        }

    companion object {
        private const val ADD_FEED_FRAGMENT_TAG = "addNewFeed"
        private const val REMOVE_FEED_FRAGMENT_TAG = "removeFeed"
        const val NEWS_LINK = "iuliiaponomareva.eventum.NEWS_LINK"
    }
}
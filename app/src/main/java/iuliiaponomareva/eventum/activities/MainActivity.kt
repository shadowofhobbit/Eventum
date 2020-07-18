package iuliiaponomareva.eventum.activities

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import iuliiaponomareva.eventum.ChannelRepository
import iuliiaponomareva.eventum.R
import iuliiaponomareva.eventum.adapters.NewsArrayAdapter
import iuliiaponomareva.eventum.data.Channel
import iuliiaponomareva.eventum.data.News
import iuliiaponomareva.eventum.data.Reader
import iuliiaponomareva.eventum.fragments.AddFeedDialogFragment
import iuliiaponomareva.eventum.fragments.AddFeedDialogFragment.AddFeedDialogListener
import iuliiaponomareva.eventum.fragments.RemoveFeedDialogFragment
import iuliiaponomareva.eventum.fragments.RemoveFeedDialogFragment.RemoveFeedDialogListener
import iuliiaponomareva.eventum.viewModels.ChannelError
import iuliiaponomareva.eventum.viewModels.ChannelViewModel
import iuliiaponomareva.eventum.viewModels.ChannelViewModelFactory
import iuliiaponomareva.eventum.viewModels.NewsViewModel
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
    private lateinit var channelsViewModel: ChannelViewModel
    private lateinit var newsViewModel: NewsViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
        val repository = ChannelRepository(applicationContext)
        channelsViewModel = ViewModelProvider(this,
            ChannelViewModelFactory(
                repository
            )
        )
            .get(ChannelViewModel::class.java)
        newsViewModel = ViewModelProvider(this)[NewsViewModel::class.java]
        reader = newsViewModel.reader
        channelsViewModel.getChannels().observe(this, androidx.lifecycle.Observer { event ->
            Log.wtf("eventum", "channels changed")
            val handled = event.handled
            onChannelsLoaded(event.getEvent(), refreshNews = !handled)
        })
        channelsViewModel.error.observe(this, androidx.lifecycle.Observer {
            when (val error = it.getEventIfNotHandled()) {
                ChannelError.ALREADY_EXISTS -> createToast(error.url + " " + getString(R.string.has_already_been_added))
                ChannelError.ERROR_ADDING -> createToast(R.string.error_adding_feed)
            }
        })
        newsViewModel.news.observe(this, androidx.lifecycle.Observer {newsMap ->
            for (url in newsMap.keys) {
                reader.finishRefreshing(newsMap[url]?.toTypedArray(), url)
            }
            if (newsMap.size == 1) {
                showNews(reader.getNewsFromFeed(newsMap.keys.first()))
            } else {
                showNews(reader.allNews)
            }
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
            if (isConnectedToNetwork()) {
                if (selectedChannel == all) {
                    newsViewModel.refreshNews(reader.getFeeds())
                } else {
                    newsViewModel.refreshNews(arrayOf(selectedChannel!!.url))
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
        if (isConnectedToNetwork()) {
            channelsViewModel.addChannel(feedUrl)
        } else {
            createToast(R.string.no_internet)
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

    override fun removeChosenFeed(url: String) {
        if (selectedChannel?.url == url) {
            selectedChannel = all
        }
        val feed = reader.getFeed(url)
        drawerAdapter.remove(feed)
        reader.removeFeed(url)
        drawerAdapter.notifyDataSetChanged()
        channelsViewModel.deleteChannel(feed!!)
    }

    @Suppress("DEPRECATION")
    private fun isConnectedToNetwork(): Boolean {
        val manager = applicationContext
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = manager.activeNetwork ?: return false
            val networkCapabilities = manager.getNetworkCapabilities(network) ?: return false
            return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networkInfo = manager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    override fun onRefresh() {
        refreshNews()
        refreshLayout.isRefreshing = false
    }

    private fun refreshNews() {
        if (selectedChannel == null) {
            selectedChannel = all
        }
        if (isConnectedToNetwork()) {
            news.clear()
            if (selectedChannel == all) {
                newsViewModel.refreshNews(reader.getFeeds())
            } else {
                newsViewModel.refreshNews(arrayOf(selectedChannel!!.url))
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


    private fun onChannelsLoaded(data: List<Channel>, refreshNews: Boolean = true) {
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
            supportActionBar?.setTitle(R.string.chosen_feed_default)
        } else {
            title = selectedChannel!!.title
        }
        if (refreshNews) {
            refreshNews()
        }
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
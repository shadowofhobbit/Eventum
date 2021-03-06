package iuliiaponomareva.eventum.activities

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import iuliiaponomareva.eventum.ChannelRepository
import iuliiaponomareva.eventum.R
import iuliiaponomareva.eventum.adapters.ChannelsAdapter
import iuliiaponomareva.eventum.adapters.NewsAdapter
import iuliiaponomareva.eventum.data.Channel
import iuliiaponomareva.eventum.data.News
import iuliiaponomareva.eventum.data.ReaderDatabase
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
    private lateinit var drawerAdapter: ChannelsAdapter
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var adapterDataObserver: AdapterDataObserver
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var selectedChannel: Channel
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
        val channelDao = ReaderDatabase.getDatabase(applicationContext).channelDao()
        val repository = ChannelRepository(channelDao)
        channelsViewModel = ViewModelProvider(this,
            ChannelViewModelFactory(
                repository
            )
        )
            .get(ChannelViewModel::class.java)
        newsViewModel = ViewModelProvider(this)[NewsViewModel::class.java]
        channelsViewModel.channels.observe(this, { event ->
            val handled = event.handled
            onChannelsLoaded(event.getEvent(), refreshNews = !handled)
        })
        channelsViewModel.error.observe(this, {
            when (val error = it.getEventIfNotHandled()) {
                ChannelError.ALREADY_EXISTS -> createToast(error.url + " " + getString(R.string.has_already_been_added))
                ChannelError.ERROR_ADDING -> createToast(R.string.error_adding_feed)
            }
        })

        newsViewModel.allNews.observe(this, { newsMap ->
            if (selectedChannel == all) {
                showNews(newsMap.values.flatten())
            } else {
                showNews(newsMap[selectedChannel.url] ?: listOf())
            }
        })
    }

    private fun setUpChannelsView() {
        drawerToggle = ActionBarDrawerToggle(
            this, drawerLayout, R.string.drawer_open,
            R.string.drawer_close
        )
        drawerAdapter = ChannelsAdapter(onChannelSelected())
        drawer.adapter = drawerAdapter
        drawerLayout.addDrawerListener(drawerToggle)
    }

    private fun onChannelSelected(): (Channel) -> Unit {
        return fun(channel: Channel) {
            selectedChannel = channel
            drawerLayout.closeDrawer(drawer)
            supportActionBar?.title = selectedChannel.title
            newsAdapter.cancel()
            if (isConnectedToNetwork()) {
                if (selectedChannel == all) {
                    val urls =
                        channelsViewModel.channels.value?.getEvent()?.map { it.url }?.toTypedArray()
                            ?: arrayOf()
                    newsViewModel.refreshNews(urls)
                } else {
                    newsViewModel.refreshNews(arrayOf(selectedChannel.url))
                }
            } else {
                val allNews = newsViewModel.allNews.value
                if (selectedChannel == all) {
                    showNews(allNews?.values?.flatten() ?: listOf())
                } else {
                    showNews(allNews?.get(selectedChannel.url) ?: listOf())
                }
                createToast(R.string.no_internet)
            }
        }
    }

    private fun setUpNewsView() {
        refreshLayout.setOnRefreshListener(this)
        newsAdapter = NewsAdapter { news ->
            if (!TextUtils.isEmpty(news.link)) {
                val intent = Intent(this@MainActivity, ViewPageActivity::class.java)
                intent.putExtra(NEWS_LINK, news.link)
                startActivity(intent)
            }
        }
        newsRecyclerView.adapter = newsAdapter
        val dividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        val drawable = ContextCompat.getDrawable(this, R.drawable.divider)
        drawable?.let { dividerItemDecoration.setDrawable(it) }
        newsRecyclerView.addItemDecoration(dividerItemDecoration)
        newsRecyclerView.setHasFixedSize(true)
        adapterDataObserver = object: AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                if (newsAdapter.itemCount == 0) {
                    emptyView.visibility = View.VISIBLE
                } else {
                    emptyView.visibility = View.INVISIBLE
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
        val channels = ArrayList<Channel>()
        val event = channelsViewModel.channels.value?.getEvent() ?: listOf()
        channels.addAll(event)
        val args = Bundle()
        args.putParcelableArrayList(RemoveFeedDialogFragment.FEEDS, channels)
        newFragment.arguments = args
        newFragment.show(
            supportFragmentManager,
            REMOVE_FEED_FRAGMENT_TAG
        )
    }

    override fun onStart() {
        super.onStart()
        newsAdapter.registerAdapterDataObserver(adapterDataObserver)
    }

    override fun onStop() {
        super.onStop()
        newsAdapter.unregisterAdapterDataObserver(adapterDataObserver)
        saveSelectedChannel()
    }

    private fun saveSelectedChannel() {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        sharedPref.edit {
            putString(getString(R.string.chosen_feed_pref_label), selectedChannel.url)
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

    override fun removeChosenFeed(feed: Channel) {
        if (selectedChannel == feed) {
            selectedChannel = all
        }
        drawerAdapter.remove(feed)
        channelsViewModel.deleteChannel(feed)
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
        if (!this::selectedChannel.isInitialized) {
            selectedChannel = all
        }
        if (isConnectedToNetwork()) {
            newsAdapter.news = listOf()
            if (selectedChannel == all) {
                val urls = channelsViewModel.channels.value?.getEvent()?.map { it.url }?.toTypedArray()
                    ?: arrayOf()
                newsViewModel.refreshNews(urls)
            } else {
                newsViewModel.refreshNews(arrayOf(selectedChannel.url))
            }
        } else {
            createToast(R.string.no_internet)
        }
    }

    private fun showNews(newsCollection: Collection<News>) {
        newsAdapter.channelURL = selectedChannel.url
        newsAdapter.news = newsCollection.toList()
    }

    override fun onDestroy() {
        super.onDestroy()
        drawerLayout.removeDrawerListener(drawerToggle)
    }


    private fun onChannelsLoaded(data: List<Channel>, refreshNews: Boolean = true) {
        drawerAdapter.clear()
        if (data.isEmpty()) {
            emptyView.setText(R.string.no_feeds_added_yet)
        } else {
            emptyView.setText(R.string.loading)
        }
        val channels = mutableListOf(all)
        channels.addAll(data)
        drawerAdapter.addAll(channels)
        if (!this::selectedChannel.isInitialized) {
            selectedChannel = data.find { it.url == selectedFeedFromPreferences } ?: all
        }
        supportActionBar?.title = selectedChannel.title
        if (refreshNews) {
            refreshNews()
        } else {
            if (selectedChannel == all) {
                showNews(newsViewModel.allNews.value?.values?.flatten() ?: listOf())
            } else {
                showNews(newsViewModel.allNews.value?.get(selectedChannel.url) ?: listOf())
            }
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
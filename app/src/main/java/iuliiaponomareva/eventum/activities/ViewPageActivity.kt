package iuliiaponomareva.eventum.activities

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import iuliiaponomareva.eventum.R
import kotlinx.android.synthetic.main.activity_view_page.*

class ViewPageActivity : AppCompatActivity() {
    private lateinit var url: String

    @SuppressLint("SetJavaScriptEnabled")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_page)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                title = view.title
            }
        }
        configureZoom(webView)
        webView.settings.allowFileAccess = false
        webView.settings.javaScriptEnabled = true
        url = intent.getStringExtra(MainActivity.NEWS_LINK)!!
        webView.loadUrl(url)
    }

    private fun configureZoom(webView: WebView) {
        webView.settings.builtInZoomControls = true
        webView.settings.setSupportZoom(true)
        webView.settings.displayZoomControls = false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.view_page_activity_action_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.copyLink -> {
                copyLink()
                true
            }
            R.id.openInBrowser -> {
                val openBrowserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
                startActivity(openBrowserIntent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun copyLink() {
        val clipboard =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(URL, url)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, R.string.link_copied, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val URL = "URL"
    }
}
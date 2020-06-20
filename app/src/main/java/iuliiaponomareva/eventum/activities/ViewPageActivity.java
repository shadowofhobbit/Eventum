package iuliiaponomareva.eventum.activities;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import iuliiaponomareva.eventum.R;

public class ViewPageActivity extends AppCompatActivity {
    private static final String URL = "URL";
    private String url;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_page);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        WebView webView = findViewById(R.id.full_news);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                setTitle(view.getTitle());
            }
        });
        configureZoom(webView);
        webView.getSettings().setAllowFileAccess(false);
        webView.getSettings().setJavaScriptEnabled(true);
        Intent intent = getIntent();
        url = intent.getStringExtra(MainActivity.NEWS_LINK);
        webView.loadUrl(url);
    }

    private void configureZoom(WebView webView) {
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setDisplayZoomControls(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_page_activity_action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.copyLink) {
            copyLink();
            return true;
        } else if (item.getItemId() == R.id.openInBrowser) {
            Intent openBrowserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(openBrowserIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void copyLink() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(URL, url);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, R.string.link_copied, Toast.LENGTH_SHORT).show();
    }
}

package iuliiaponomareva.eventum.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import iuliiaponomareva.eventum.R;

public class ViewPageActivity extends AppCompatActivity {
    private static final String URL = "URL";
    private String url;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_page);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        WebView webView = (WebView) findViewById(R.id.full_news);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                setTitle(view.getTitle());
            }
        });
        configureZoom(webView);
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

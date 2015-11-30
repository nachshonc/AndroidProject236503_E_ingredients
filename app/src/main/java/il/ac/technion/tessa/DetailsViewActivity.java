package il.ac.technion.tessa;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;

public class DetailsViewActivity extends AppCompatActivity {

    WebView webView;
    EDBHandler dbHandler = new EDBHandler(DetailsViewActivity.this, null, null, 1);
    ArrayList<String> keyStack = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_view);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setLoadWithOverviewMode(true);
//        webView.getSettings().setUseWideViewPort(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d("WVC", url);
                if (url.startsWith("http://fake.com/#")) {
                    String key = url.substring("http://fake.com/#".length());
                    Log.d("SPFK", key);
                    showPageForKey(key, true);
                }
                return true;
            }

        });

        String key = getIntent().getExtras().getString("key");
        showPageForKey(key, true);
    }

    public void showPageForKey(String key, boolean push) {
        if (push)
            keyStack.add(key);

        ActionBar ab = getSupportActionBar();
        if (ab != null)
            ab.setTitle(key);
        EDBIngredient ingredient = dbHandler.findIngredient(key);
        if (ingredient != null) {
            if (ab != null)
                ab.setSubtitle(ingredient.getTitle());
            Log.d("SPFK", "Loading data for key "+key);
            webView.loadUrl("about:blank");
            webView.loadData(ingredient.toHTML(), "text/html", null);
        } else {
            webView.loadData("<html><body><i>Ingredient "+key+" not found in DB</i></body></html>", "text/html", null);
        }
        // TODO: Update action bar here as well
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details_view, menu);
        return true;
        //return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (keyStack.size() > 1) {
            keyStack.remove(keyStack.size()-1);
            String key = keyStack.get(keyStack.size()-1);
            showPageForKey(key, false);
        } else {
            super.onBackPressed();
        }
    }
}

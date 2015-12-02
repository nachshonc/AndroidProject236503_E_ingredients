package il.ac.technion.tessa;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class DetailsViewActivity extends AppCompatActivity implements View.OnClickListener {

    WebView webView;
    EDBHandler dbHandler = new EDBHandler(DetailsViewActivity.this, null, null, 1);
    ArrayList<String> keyStack = new ArrayList<>();
    AlertDialog dialog;
    SharedPreferences preferences;
    MenuItem menuItemSetPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = UserPreferences.get(getApplicationContext());
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
        if(menuItemSetPref!=null)
            setPictureForKey(key);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details_view, menu);
        menuItemSetPref = menu.findItem(R.id.action_set_preferences);
        if(keyStack.isEmpty()==false)
            setPictureForKey(keyStack.get(0));
        return true;
        //return super.onCreateOptionsMenu(menu);
    }

    public void showPageForKey(String key, boolean push) {
        if (push)
            keyStack.add(key);
        if(menuItemSetPref!=null){
            setPictureForKey(key); 
        }

        ActionBar ab = getSupportActionBar();
        if (ab != null)
            ab.setTitle(key);
        EDBIngredient ingredient = dbHandler.findIngredient(key);
        if (ingredient != null) {
            if (ab != null)
                ab.setSubtitle(ingredient.getTitle());
            Log.d("SPFK", "Loading data for key "+key);
            webView.loadUrl("about:blank");
            webView.clearHistory();
            webView.clearCache(false);
            webView.loadData(ingredient.toHTML(), "text/html", null);
        } else {
            webView.loadData("<html><body><i>Ingredient "+key+" not found in DB</i></body></html>", "text/html", null);
        }
        // TODO: Update action bar here as well
    }

    private void setPictureForKey(String key) {
        Options opt;
        if(preferences.contains(key)) {
            Log.d("getView: preferences ", String.format("%d", preferences.getInt(key, -1)));
            opt = Options.getOpt(preferences.getInt(key, -1));
        }
        else
            opt = dbHandler.findIngredient(key).getOptions(); //ONLY THE DEFAULT.
        menuItemSetPref.setIcon(opt.getPic());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_set_preferences) {
            openPreferencesDialog();
            return true;
        }

        if ((id == R.id.action_internet_search) && (keyStack.size() > 0)) {
            String key =  keyStack.get(keyStack.size() - 1);
            EDBIngredient ingredient = dbHandler.findIngredient(key);
            String searchString = key+" "+ingredient.getTitle();

            String url;
            try {
                url = "https://www.google.co.il/webhp?sourceid=chrome-instant&ion=1&espv=2&ie=UTF-8#q=" + URLEncoder.encode(searchString, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                url = "https://www.google.co.il/webhp?sourceid=chrome-instant&ion=1&espv=2&ie=UTF-8#q=" + searchString;
            }
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }
    private void openPreferencesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change safety rating:");
        LayoutInflater inflater = (LayoutInflater) getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate( R.layout.user_choice, null, false);
        View x;
        x=v.findViewById(R.id.choice_dang);     if (x != null) { x.setOnClickListener(this); x.setTag(Options.DANG); }
        x=v.findViewById(R.id.choice_unhealth); if (x != null) { x.setOnClickListener(this); x.setTag(Options.UNHEALTHY); }
        x=v.findViewById(R.id.choice_safe);     if (x != null) { x.setOnClickListener(this); x.setTag(Options.SAFE); }
        x=v.findViewById(R.id.choice_default);  if (x != null) { x.setOnClickListener(this); x.setTag(Options.DEFAULT); }
        ImageView imgDefault = (ImageView)v.findViewById(R.id.imageViewDefault);
        if (imgDefault != null)
            imgDefault.setImageResource(dbHandler.findIngredient(keyStack.get(keyStack.size() - 1)).getOptions().getPic());

        builder.setView(v);
        dialog = builder.show();
    }

    @Override
    public void onClick(View v) {
        String key = keyStack.get(keyStack.size() - 1);
        Options o = (Options)v.getTag();
        SharedPreferences.Editor edit = preferences.edit();
        if(o == Options.DEFAULT)
            edit.remove(key);
        else
            edit.putInt(key, o.value);
        edit.apply();
        setPictureForKey(key);
        switch(o){
            case DANG:
                Log.d("onClick", "DANG");
                break;
            case UNHEALTHY:
                Log.d("onClick", "UNHEALTHY");
                break;
            case SAFE:
                Log.d("onClick", "SAFE");
                break;
            case DEFAULT:
                Log.d("onClick", "DEFAULT");
                break;

        }
        dialog.dismiss();

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

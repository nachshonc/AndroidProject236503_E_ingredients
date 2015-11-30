package il.ac.technion.tessa;

import android.app.VoiceInteractor;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.telecom.Call;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.webkit.WebViewClient;

import java.util.ArrayList;

public class DetailsViewActivity extends AppCompatActivity implements View.OnClickListener {

    WebView webView;
    EDBHandler dbHandler = new EDBHandler(DetailsViewActivity.this, null, null, 1);
    ArrayList<String> keyStack = new ArrayList<>();
    public static String PREFERENCES_KEY = "UserChoice";
    AlertDialog dialog;
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
        if (id == R.id.action_set_preferences) {
            setPreferences(); 
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    final static int TYPE_KEY = 0;
    private void setPreferences() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick your preferences");
        LayoutInflater inflater = (LayoutInflater) getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate( R.layout.user_choice, null, false);
        View x;
        x=v.findViewById(R.id.choice_dang);     x.setOnClickListener(this); x.setTag(Options.DANG);
        x=v.findViewById(R.id.choice_unhealth); x.setOnClickListener(this); x.setTag(Options.UNHEALTHY);
        x=v.findViewById(R.id.choice_safe);     x.setOnClickListener(this); x.setTag(Options.SAFE);
        x=v.findViewById(R.id.choice_default);  x.setOnClickListener(this); x.setTag(Options.DEFAULT);

        builder.setView(v);
        dialog = builder.show();
    }

    @Override
    public void onClick(View v) {
        SharedPreferences preferences = getSharedPreferences(PREFERENCES_KEY, 0);
        String key = keyStack.get(keyStack.size() - 1);
        Options o = (Options)v.getTag();
        SharedPreferences.Editor edit = preferences.edit();
        if(o == Options.DEFAULT)
            edit.remove(key);
        else
            edit.putInt(key, Options.DANG.value);
        edit.commit();
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

package il.ac.technion.tessa;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;

public class DetailsViewActivity extends AppCompatActivity {

    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_view);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        webView = (WebView) findViewById(R.id.webView);
        String key = getIntent().getExtras().getString("key");
        EDBHandler dbHandler = new EDBHandler(this, null, null, 1);
        EDBIngredient ingredient = dbHandler.findIngredient(key);
        if (ingredient != null) {
            webView.loadData(ingredient.toHTML(), "text/html", null);
        }
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

    private void setPreferences() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick your preferences");
        LayoutInflater inflater = (LayoutInflater) getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate( R.layout.user_choice, null, false);
        builder.setView(v);

/*        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });*/

        builder.show();

    }
}

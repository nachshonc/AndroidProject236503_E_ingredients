package il.ac.technion.tessa;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class EIngredientSelectionActivity extends AppCompatActivity implements ListView.OnItemClickListener, ListView.OnItemSelectedListener  {
    private String mENumber;
    private TextView mE;
    private EDBHandler dbHandler;
    public static final String EXTRA_SELECTED = "SELECTED";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eingredient_selection);
        mENumber = "";
        mE = (TextView) findViewById(R.id.e);
        dbHandler = new EDBHandler(this, null, null, 1);
        updateList();
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d("onItemClick", ""+position);
        ECursorAdapter adapter = (ECursorAdapter) parent.getAdapter();
        String tag = ((TextView) view.findViewById(R.id.item_tag)).getText().toString();
        // return the selected result
        Intent intent = new Intent();
        intent.putExtra(EIngredientSelectionActivity.EXTRA_SELECTED,tag);
        setResult(RESULT_OK, intent);
        finish();

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d("onItemSelected", ""+position);
        ECursorAdapter adapter = (ECursorAdapter) parent.getAdapter();
        String tag = ((TextView) view.findViewById(R.id.item_tag)).getText().toString();
        // return the selected result
        Intent intent = new Intent();
        intent.putExtra(EIngredientSelectionActivity.EXTRA_SELECTED,tag);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        ECursorAdapter adapter = (ECursorAdapter) parent.getAdapter();
        adapter.setSelection("");
        parent.refreshDrawableState();
    }

    public class ECursorAdapter extends CursorAdapter {
        String mSelectedTag="";
        public ECursorAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }

        public void setSelection(String tag) {
            Log.d("setSelection", tag);
            mSelectedTag = tag;
        }
        // The newView method is used to inflate a new view and return it,
        // you don't bind any data to the view at this point.
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        }

        // The bindView method is used to bind all data to a given view
        // such as setting the text on a TextView.
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // Find fields to populate in inflated template
            TextView tvTitle = (TextView) view.findViewById(R.id.item_title);
            TextView tvTag = (TextView) view.findViewById(R.id.item_tag);
            // Extract properties from cursor
            String title = cursor.getString(cursor.getColumnIndexOrThrow(EDBHandler.COLUMN_TITLE));
            String tag = cursor.getString(cursor.getColumnIndexOrThrow(EDBHandler.COLUMN_KEY));
            // Populate fields with extracted properties
            tvTitle.setText(title);
            tvTag.setText(String.valueOf(tag));
            Log.d("Tag", "tag=" + tag + ", mSelectedTag=" + mSelectedTag);
            if (tag.equals(mSelectedTag))
                view.setBackgroundColor(Color.LTGRAY);
            else
                view.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    public void updateList() {
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " +
                EDBHandler.COLUMN_ID + ", " +
                EDBHandler.COLUMN_KEY + ", " +
                EDBHandler.COLUMN_TITLE + " " +
                "FROM " + EDBHandler.TABLE_INGREDIENTS + " " +
                "WHERE " + EDBHandler.COLUMN_KEY + " GLOB 'E" + mENumber + "*' " +
                "ORDER BY " + EDBHandler.COLUMN_TITLE + "," + EDBHandler.COLUMN_KEY, null);
        ListView elist = (ListView) findViewById(R.id.elist);
//        elist.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
//        elist.setDrawSelectorOnTop(true);
        ECursorAdapter eCursor = new ECursorAdapter(this, cursor);
        elist.setAdapter(eCursor);
        Log.d("setAdapter","");
        elist.setOnItemClickListener(this);
        Log.d("setOnItemClick","");
        elist.setOnItemSelectedListener(this);
        Log.d("setOnItemSelected", "");
        cursor.moveToFirst();
        elist.setSelection(0);
    }


    public void onDialButtonClick(View v) {
        int dig;
        switch (v.getId()) {
            case R.id.btn0:
                dig=0;
                break;
            case R.id.btn1:
                dig=1;
                break;
            case R.id.btn2:
                dig=2;
                break;
            case R.id.btn3:
                dig=3;
                break;
            case R.id.btn4:
                dig=4;
                break;
            case R.id.btn5:
                dig=5;
                break;
            case R.id.btn6:
                dig=6;
                break;
            case R.id.btn7:
                dig=7;
                break;
            case R.id.btn8:
                dig=8;
                break;
            case R.id.btn9:
                dig=9;
                break;
            default:
                dig = -1;
                break;
        }
        if (dig>=0) {
            if (mENumber.length()==4)
                return;
            mENumber = mENumber + dig;
            mE.setText(mENumber+"|");
            updateList();
            return;
        } else if (v.getId() == R.id.btnbksp && mENumber.length() > 0) {
            mENumber = mENumber.substring(0, mENumber.length()-1);
            mE.setText(mENumber+"|");
            updateList();
            return;
        } else if (v.getId() == R.id.btnok) {
            ListView elist = (ListView) findViewById(R.id.elist);
            if (elist.getCount() > 1) {
                Toast.makeText(getApplicationContext(), "Please pick one of the matching additivies", Toast.LENGTH_LONG).show();
            } else if (elist.getCount() == 1) {
                Cursor tagc = (Cursor) elist.getItemAtPosition(0);
                String tag = tagc.getString(tagc.getColumnIndexOrThrow(EDBHandler.COLUMN_KEY));
                Intent intent = new Intent();
                intent.putExtra(EIngredientSelectionActivity.EXTRA_SELECTED, tag);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Unknown additive", Toast.LENGTH_LONG).show();
            }
            // if more than one item then Toast please pick one of the matching E-Ingredients above
            // otherwise return with the single ingredient on the list (if there is one)
            // Perhaps disable the OK button if the list is empty....
        }
    }
}

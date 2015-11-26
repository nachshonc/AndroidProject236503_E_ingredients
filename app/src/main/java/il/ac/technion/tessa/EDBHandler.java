package il.ac.technion.tessa;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by arietal on 11/17/15.
 */
public class EDBHandler extends SQLiteOpenHelper {

    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/E_Ingredients/";

    public static final String FEEDER_FILE="Edb.txt";

    public static final String FEEDER_PATH = DATA_PATH+"/tessdata/"+FEEDER_FILE;

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "Edb.db";
    private static final String TABLE_INGREDIENTS = "Ingredients";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_KEY = "Key";
    public static final String COLUMN_TITLE = "Title";
    public static final String COLUMN_TYPE = "Type";
    public static final String COLUMN_WARNING = "Warning";
    public static final String COLUMN_BANNED = "Banned";
    public static final String COLUMN_ALLOWEDINEU = "allowedInEU";
    public static final String COLUMN_WIKINOTBANNED = "wiki_notBanned";
    public static final String COLUMN_WIKINOTCONSIDEREDDANGEROUS = "wiki_notConsideredDangerous";
    public static final String COLUMN_DESCRIPTION = "Description";
    public static final String COLUMN_USERANNOTATIONS = "UserAnnotations";

    public EDBHandler(Context context, String name,
                      SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
        parseDB();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_INGERDIENTS_TABLE = "CREATE TABLE if not exists " +
                TABLE_INGREDIENTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY autoincrement, " +
                COLUMN_KEY + " TEXT UNIQUE ON CONFLICT REPLACE, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_TYPE + " TEXT, " +
                COLUMN_WARNING + " TEXT, " +
                COLUMN_BANNED + " TEXT, " +
                COLUMN_ALLOWEDINEU + " TEXT, " +
                COLUMN_WIKINOTBANNED + " TEXT, " +
                COLUMN_WIKINOTCONSIDEREDDANGEROUS + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_USERANNOTATIONS + " TEXT" + ")";
        db.execSQL(CREATE_INGERDIENTS_TABLE);
    }

    private void parseDB() {
        String feederFile = FEEDER_PATH;
        BufferedReader br = null;
        String line = "";
        String feederSplitBy = "\\t"; //delimited by tab
        Boolean failed = false;
        String key;
        EDBIngredient tmp = null;
        StringBuilder description = null;
        try {

            br = new BufferedReader(new FileReader(feederFile));
            while ((line = br.readLine()) != null) {

                String[] columns = line.split(feederSplitBy);
                if (columns.length > 0) {
                    if (columns[0].equals("@MAGIC-ADD@")) {
                        if (tmp != null) {
                            if (description != null)
                                tmp.setDescription(description.toString());
                            addIngredient(tmp);
                        }
                        tmp = null;
                        description = null;
                    } else if (columns[0].equals("@Key@")) {
                        tmp = new EDBIngredient(columns[1]);
                    } else if (columns[0].equals("@Title@")) {
                        if (columns.length > 1)
                            tmp.setTitle(columns[1]);
                    } else if (columns[0].equals("@Type@")) {
                        if (columns.length > 1)
                            tmp.setType(columns[1]);
                    } else if (columns[0].equals("@Warning@")) {
                        if (columns.length > 1)
                            tmp.setWarning(columns[1]);
                    } else if (columns[0].equals("@Banned@")) {
                        if (columns.length > 1)
                            tmp.setBanned(columns[1]);
                    } else if (columns[0].equals("@allowedInEU@")) {
                        if (columns.length > 1)
                            tmp.setAllowedInEU(columns[1]);
                    } else if (columns[0].equals("@wiki_notBanned@")) {
                        if (columns.length > 1)
                            tmp.setWiki_notBanned(columns[1]);
                    } else if (columns[0].equals("@wiki_notConsideredDangerous@")) {
                        if (columns.length > 1)
                            tmp.setWiki_notConsideredDangerous(columns[1]);
                    } else if (columns[0].equals("@Description@")) {
                        description = new StringBuilder();
                    } else if (description != null) {
                        description.append(line).append("\n");
                    }
                } else if (description != null) {
                    description.append(line).append("\n");
                }
            }
            if (tmp != null) {
                if (description != null)
                    tmp.setDescription(description.toString());
                addIngredient(tmp);
            }
        } catch (FileNotFoundException e) {
            failed=true;
        } catch (IOException e) {
            failed=true;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(failed)
            Log.w("parse db", "parse db failed");
        else
            Log.d("parse db", "successfully parsed the database.");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
                          int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INGREDIENTS);
        onCreate(db);
    }


    public void addIngredient(EDBIngredient ingredient) {

        Log.d("EDB", "Adding "+ingredient.toString());
        ContentValues values = new ContentValues();
        values.put(COLUMN_KEY, ingredient.getKey());
        values.put(COLUMN_TITLE, ingredient.getTitle());
        values.put(COLUMN_TYPE, ingredient.getType());
        values.put(COLUMN_WARNING, ingredient.getWarning());
        values.put(COLUMN_BANNED, ingredient.getBanned());
        values.put(COLUMN_ALLOWEDINEU, ingredient.getAllowedInEU());
        values.put(COLUMN_WIKINOTBANNED, ingredient.getWiki_notBanned());
        values.put(COLUMN_WIKINOTCONSIDEREDDANGEROUS, ingredient.getWiki_notConsideredDangerous());
        values.put(COLUMN_DESCRIPTION, ingredient.getDescription());
        values.put(COLUMN_USERANNOTATIONS, ingredient.getUserAnnotations());

        SQLiteDatabase db = this.getWritableDatabase();

        db.insert(TABLE_INGREDIENTS, null, values);
        db.close();
    }

    public EDBIngredient findIngredient(String key) {
        SQLiteDatabase db = this.getReadableDatabase();
        return null;
    }

    public Cursor fetchAll() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_INGREDIENTS,
                new String[] { COLUMN_ID, COLUMN_KEY, COLUMN_TITLE, COLUMN_TYPE,
                COLUMN_WARNING, COLUMN_BANNED, COLUMN_ALLOWEDINEU, COLUMN_WIKINOTBANNED,
                COLUMN_WIKINOTCONSIDEREDDANGEROUS, COLUMN_DESCRIPTION, COLUMN_USERANNOTATIONS}, null, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        return cursor;
    }
}

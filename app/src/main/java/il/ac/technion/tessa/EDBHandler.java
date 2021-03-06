package il.ac.technion.tessa;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class EDBHandler extends SQLiteOpenHelper {

    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/E_Ingredients/";

    public static final String FEEDER_FILE="Edb.txt";

    public static final String FEEDER_PATH = DATA_PATH+"/tessdata/"+FEEDER_FILE;

    public static final int DATABASE_VERSION = 13;
    private static final String DATABASE_NAME = "Edb.db";
    public static final String TABLE_INGREDIENTS = "Ingredients";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_KEY = "Key";
    public static final String COLUMN_TITLE = "Title";
    public static final String COLUMN_TYPE = "Type";
    public static final String COLUMN_WARNING = "Warning";
    public static final String COLUMN_BANNED = "Banned";
    public static final String COLUMN_ALLOWEDINEU = "allowedInEU";
    public static final String COLUMN_WIKINOTBANNED = "wiki_notBanned";
    public static final String COLUMN_WIKINOTCONSIDEREDDANGEROUS = "wiki_notConsideredDangerous";
    public static final String COLUMN_CLASSIFICATION = "Classification";
    public static final String COLUMN_FUNCTIONDETAILS = "FunctionDetails";
    public static final String COLUMN_ORIGIN = "Origin";
    public static final String COLUMN_MYADDITIVESDESCRIPTION = "MyAdditivesDescription";
    public static final String COLUMN_DIETARYRESTRICTIONS = "DietaryRestrictions";
    public static final String COLUMN_SIDEEFFECTS = "SideEffects";
    public static final String COLUMN_MYADDITIVESSAFETYRATING = "MyAdditivesSafetyRating";
    public static final String COLUMN_EVERBUMDESCRIPTION = "EverbumDescription";
    public static final String COLUMN_EVERBUMSAFETYRATING = "EverbumSafetyRating";
    public static final String COLUMN_DESCRIPTION = "Description";

    public EDBHandler(Context context, String name,
                      SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }
/*    private static EDBHandler singleton;
    public static EDBHandler createNew(Context context, String name,
                       SQLiteDatabase.CursorFactory factory, int version) {
        if(singleton==null)
            singleton=new EDBHandler(context, name, factory, version);
        return singleton;
    }*/



    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_INGREDIENTS_TABLE = "CREATE TABLE if not exists " +
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
                COLUMN_CLASSIFICATION + " TEXT, " +
                COLUMN_FUNCTIONDETAILS + " TEXT, " +
                COLUMN_ORIGIN + " TEXT, " +
                COLUMN_MYADDITIVESDESCRIPTION + " TEXT, " +
                COLUMN_DIETARYRESTRICTIONS + " TEXT, " +
                COLUMN_SIDEEFFECTS + " TEXT, " +
                COLUMN_MYADDITIVESSAFETYRATING + " TEXT, " +
                COLUMN_EVERBUMDESCRIPTION + " TEXT, " +
                COLUMN_EVERBUMSAFETYRATING + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT " + ")";
        db.execSQL(CREATE_INGREDIENTS_TABLE);
    }

    public void parseDB() {
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
                    try {
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
                        } else if (columns[0].equals("@Classification@")) {
                            if (columns.length > 1)
                                tmp.setClassification(columns[1]);
                        } else if (columns[0].equals("@FunctionDetails@")) {
                            if (columns.length > 1)
                                tmp.setFunctionDetails(columns[1]);
                        } else if (columns[0].equals("@Origin@")) {
                            if (columns.length > 1)
                                tmp.setOrigin(columns[1]);
                        } else if (columns[0].equals("@MyAdditivesDescription@")) {
                            if (columns.length > 1)
                                tmp.setMyAdditivesDescription(columns[1]);
                        } else if (columns[0].equals("@DietaryRestrictions@")) {
                            if (columns.length > 1)
                                tmp.setDietaryRestrictions(columns[1]);
                        } else if (columns[0].equals("@SideEffects@")) {
                            if (columns.length > 1)
                                tmp.setSideEffects(columns[1]);
                        } else if (columns[0].equals("@MyAdditivesSafetyRating@")) {
                            if (columns.length > 1)
                                tmp.setMyAdditivesSafetyRating(columns[1]);
                        } else if (columns[0].equals("@EverbumDescription@")) {
                            if (columns.length > 1)
                                tmp.setEverbumDescription(columns[1]);
                        } else if (columns[0].equals("@EverbumSafetyRating@")) {
                            if (columns.length > 1)
                                tmp.setEverbumSafetyRating(columns[1]);
                        } else if (columns[0].equals("@Description@")) {
                            description = new StringBuilder();
                        } else if (description != null) {
                            description.append(line).append("\n");
                        }
                    }catch (NullPointerException e){
                        e.printStackTrace();
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

        //Log.d("EDB", "Adding " + ingredient.toString());
        ContentValues values = new ContentValues();
        values.put(COLUMN_KEY, ingredient.getKey());
        values.put(COLUMN_TITLE, ingredient.getTitle());
        values.put(COLUMN_TYPE, ingredient.getType());
        values.put(COLUMN_WARNING, ingredient.getWarning());
        values.put(COLUMN_BANNED, ingredient.getBanned());
        values.put(COLUMN_ALLOWEDINEU, ingredient.getAllowedInEU());
        values.put(COLUMN_WIKINOTBANNED, ingredient.getWiki_notBanned());
        values.put(COLUMN_WIKINOTCONSIDEREDDANGEROUS, ingredient.getWiki_notConsideredDangerous());
        values.put(COLUMN_CLASSIFICATION, ingredient.getClassification());
        values.put(COLUMN_FUNCTIONDETAILS, ingredient.getFunctionDetails());
        values.put(COLUMN_ORIGIN, ingredient.getOrigin());
        values.put(COLUMN_MYADDITIVESDESCRIPTION, ingredient.getMyAdditivesDescription());
        values.put(COLUMN_DIETARYRESTRICTIONS, ingredient.getDietaryRestrictions());
        values.put(COLUMN_SIDEEFFECTS, ingredient.getSideEffects());
        values.put(COLUMN_MYADDITIVESSAFETYRATING, ingredient.getMyAdditivesSafetyRating());
        values.put(COLUMN_EVERBUMDESCRIPTION, ingredient.getEverbumDescription());
        values.put(COLUMN_EVERBUMSAFETYRATING, ingredient.getEverbumSafetyRating());
        values.put(COLUMN_DESCRIPTION, ingredient.getDescription());

        SQLiteDatabase db = this.getWritableDatabase();

        db.insert(TABLE_INGREDIENTS, null, values);
        db.close();
    }

    public EDBIngredient findIngredient(String key) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_INGREDIENTS,
                new String[]{COLUMN_ID, COLUMN_KEY, COLUMN_TITLE, COLUMN_TYPE,
                        COLUMN_WARNING, COLUMN_BANNED, COLUMN_ALLOWEDINEU, COLUMN_WIKINOTBANNED,
                        COLUMN_WIKINOTCONSIDEREDDANGEROUS, COLUMN_CLASSIFICATION, COLUMN_FUNCTIONDETAILS,
                        COLUMN_ORIGIN, COLUMN_MYADDITIVESDESCRIPTION, COLUMN_DIETARYRESTRICTIONS,
                        COLUMN_SIDEEFFECTS, COLUMN_MYADDITIVESSAFETYRATING, COLUMN_EVERBUMDESCRIPTION,
                        COLUMN_EVERBUMSAFETYRATING, COLUMN_DESCRIPTION},
                COLUMN_KEY + "='"+key+"'", null, null, null, null);
        try {
            if (cursor != null) {
                cursor.moveToFirst();
                if (cursor.isAfterLast())
                    return null;
                EDBIngredient res = new EDBIngredient(key);
                res.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)));
                res.setType(cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)));
                res.setWarning(cursor.getString(cursor.getColumnIndex(COLUMN_WARNING)));
                res.setBanned(cursor.getString(cursor.getColumnIndex(COLUMN_BANNED)));
                res.setAllowedInEU(cursor.getString(cursor.getColumnIndex(COLUMN_ALLOWEDINEU)));
                res.setWiki_notBanned(cursor.getString(cursor.getColumnIndex(COLUMN_WIKINOTBANNED)));
                res.setWiki_notConsideredDangerous(cursor.getString(cursor.getColumnIndex(COLUMN_WIKINOTCONSIDEREDDANGEROUS)));
                res.setClassification(cursor.getString(cursor.getColumnIndex(COLUMN_CLASSIFICATION)));
                res.setFunctionDetails(cursor.getString(cursor.getColumnIndex(COLUMN_FUNCTIONDETAILS)));
                res.setOrigin(cursor.getString(cursor.getColumnIndex(COLUMN_ORIGIN)));
                res.setMyAdditivesDescription(cursor.getString(cursor.getColumnIndex(COLUMN_MYADDITIVESDESCRIPTION)));
                res.setDietaryRestrictions(cursor.getString(cursor.getColumnIndex(COLUMN_DIETARYRESTRICTIONS)));
                res.setSideEffects(cursor.getString(cursor.getColumnIndex(COLUMN_SIDEEFFECTS)));
                res.setMyAdditivesSafetyRating(cursor.getString(cursor.getColumnIndex(COLUMN_MYADDITIVESSAFETYRATING)));
                res.setEverbumDescription(cursor.getString(cursor.getColumnIndex(COLUMN_EVERBUMDESCRIPTION)));
                res.setEverbumSafetyRating(cursor.getString(cursor.getColumnIndex(COLUMN_EVERBUMSAFETYRATING)));
                res.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)));

                return res;
            }
            return null;
        }
        finally{
            if(cursor!=null && !cursor.isClosed())
                cursor.close();
            db.close();
        }
    }

    /*public Cursor fetchAll() { WHO IS RESPONSIBLE FOR CLOSING CURSOR AND THE DATABASE???
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_INGREDIENTS,
                new String[]{COLUMN_ID, COLUMN_KEY, COLUMN_TITLE, COLUMN_TYPE,
                        COLUMN_WARNING, COLUMN_BANNED, COLUMN_ALLOWEDINEU, COLUMN_WIKINOTBANNED,
                        COLUMN_WIKINOTCONSIDEREDDANGEROUS, COLUMN_CLASSIFICATION, COLUMN_FUNCTIONDETAILS,
                        COLUMN_ORIGIN, COLUMN_MYADDITIVESDESCRIPTION, COLUMN_DIETARYRESTRICTIONS,
                        COLUMN_SIDEEFFECTS, COLUMN_MYADDITIVESSAFETYRATING, COLUMN_EVERBUMDESCRIPTION,
                        COLUMN_EVERBUMSAFETYRATING, COLUMN_DESCRIPTION},
                null, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        return cursor;
    }*/
}

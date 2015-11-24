package il.ac.technion.tessa;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by nachshonc on 11/16/15.
 * TODO: move to the singleton pattern
 */
public class IngredientDB {
    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/E_Ingredients/";
    public static final String TEST_FILE="IngredientList.txt"; //null; //"bread.jpg";
    public static HashMap<String, ModelIngredient> map = new HashMap<>(500);

    public static final String PATH = DATA_PATH+"/tessdata/"+TEST_FILE;
    //return null if key does not exist
    public static ModelIngredient getIngredient(String tag){
        return map.get(tag);
    }
    public static void loadDB() {

        String csvFile = PATH;
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = "\\t"; //delimited by tab
        Boolean failed = false;
        try {

            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {

                String[] columns = line.split(cvsSplitBy);
                String s;
                if(columns.length!=6) {
                    s = "Database is not formed correctly: " + line + "  " + columns.length;
                    Log.d("parseCsv", s);
                    failed = true;
                }
                else {
                    String[] sub = columns[1].split(" *\\(i+\\) *");
                    if (sub.length == 1) {
                        s = String.format("Line %d: [0]=%s, [1]=%s, [2]=%s, [3]=%s", columns.length,
                                columns[0], columns[1], columns[2], columns[3]);
                        Log.d("parseDB", s);
                        ModelIngredient ingredient = new ModelIngredient(columns[0], columns[1],
                                columns[3].equals("TRUE"), columns[4].equals("FALSE"), columns[5].equals("TRUE"));
                        map.put(columns[0], ingredient);

                    } else {
                        StringBuilder key = new StringBuilder(columns[0]);
                        for (int i1 = 1; i1 < sub.length; i1++) {
                            key.append("i");
                            String description = sub[0] + ": " + sub[i1];
                            s = String.format("Line %d: [0]=%s, [1]=%s, [2]=%s, [3]=%s", columns.length,
                                    key.toString(), description, columns[2], columns[3]);
                            Log.d("parseDB", s);
                            ModelIngredient ingredient = new ModelIngredient(key.toString(), description,
                                    columns[3].equals("TRUE"), columns[4].equals("FALSE"), columns[5].equals("TRUE"));
                            map.put(key.toString(), ingredient);
                        }
                    }
                }
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
            Log.d("parse csv", "successfully parsed the database.");
    }
}

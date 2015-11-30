package il.ac.technion.tessa;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by nachshonc on 11/30/15.
 */
public class UserPreferences {
    public static String PREFERENCES_KEY = "UserChoice";
    private static SharedPreferences preferences;
    public static SharedPreferences get(Context c){
        if(preferences!=null) return preferences;
        preferences = c.getSharedPreferences(PREFERENCES_KEY, 0);
        return preferences;
    }
}

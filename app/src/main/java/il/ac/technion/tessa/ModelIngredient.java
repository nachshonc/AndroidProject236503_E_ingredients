package il.ac.technion.tessa;

import android.util.Log;

/**
 * Created by nachshonc on 11/9/15.
 * Model class for an ingredient item
 */
public class ModelIngredient {
    private String tag;
    private String fullName;
    private boolean allowedInEU;
    private boolean Banned;
    private boolean ConsideredDangerous;


    public ModelIngredient(String tag, String fullName) {
        super();
        this.fullName=fullName;
        this.tag=tag;
    }
    public ModelIngredient(String tag) {
        super();
        this.fullName=tag;
        this.tag=tag;
    }
    public ModelIngredient(String tag, String fullName, Boolean allowedInEU, Boolean banned, Boolean consideredDangerous){
        this.tag=tag;
        this.fullName=fullName;
        this.allowedInEU=allowedInEU;
        this.Banned=banned;
        this.ConsideredDangerous=consideredDangerous;
    }


    public Boolean getAllowedInEU() {
        return allowedInEU;
    }

    public Boolean getBanned() {
        return Banned;
    }

    public Boolean getConsideredDangerous() {
        return ConsideredDangerous;
    }
    public String getFullName() {
        return fullName;
    }
    public String getTag() {
        return tag;
    }

    public int getColor() {
        Log.d("getColor", ""+allowedInEU + "" + Banned + "" + ConsideredDangerous);
        if(!allowedInEU)
            return 0xFFFF0000; //Strong RED
        if(Banned || ConsideredDangerous)
            return 0x80FF0000;
        return 0; //natural..
    }
}

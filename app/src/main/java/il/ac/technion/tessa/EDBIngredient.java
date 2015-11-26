package il.ac.technion.tessa;

/**
 * Created by arietal on 11/17/15.
 */
public class EDBIngredient {
    private int _id;
    private String key, title, type, warning, banned;
    private String allowedInEU, wiki_notBanned, wiki_notConsideredDangerous;
    private String classificaiton;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    private String functionDetails;
    private String origin;
    private String myAdditivesDescription;
    private String dietaryRestrictions;
    private String sideEffects;
    private String myAdditivesSafetyRating;
    private String description;


    public EDBIngredient(String key) {
        this.key = key;
        this.title = "";
        this.type = "";
        this.warning = "";
        this.banned = "";
        this.allowedInEU = "";
        this.wiki_notBanned = "";
        this.wiki_notConsideredDangerous = "";
        this.classificaiton = "";
        this.functionDetails = "";
        this.origin = "";
        this.myAdditivesDescription = "";
        this.dietaryRestrictions = "";
        this.sideEffects = "";
        this.myAdditivesSafetyRating = "";
        this.description = "";
    }

    public void setKey(String key) { this.key = key; }

    public String getKey() { return key; }

    public void setTitle(String title) { this.title = title; }

    public String getTitle() { return title; }

    public void setType(String type) { this.type = type; }

    public String getType() { return type; }

    public void setWarning(String warning) { this.warning = warning; }

    public String getWarning() { return warning; }

    public void setBanned(String banned) { this.banned = banned; }

    public String getBanned() { return banned; }

    public void setAllowedInEU(String allowedInEU) { this.allowedInEU = allowedInEU; }

    public String getAllowedInEU() { return allowedInEU; }

    public void setWiki_notBanned(String wiki_notBanned) { this.wiki_notBanned = wiki_notBanned; }

    public String getWiki_notBanned() { return wiki_notBanned; }

    public void setWiki_notConsideredDangerous(String wiki_notConsideredDangerous) { this.wiki_notConsideredDangerous = wiki_notConsideredDangerous; }

    public String getWiki_notConsideredDangerous() { return wiki_notConsideredDangerous; }

    public void setDescription(String description) { this.description = description; }

    public String getDescription() { return description; }

    public void setMyAdditivesSafetyRating(String myAdditivesSafetyRating) { this.myAdditivesSafetyRating = myAdditivesSafetyRating; }

    public String getMyAdditivesSafetyRating() { return myAdditivesSafetyRating; }

    public void setClassification(String classificaiton) { this.classificaiton = classificaiton; }

    public String getClassificaiton() { return classificaiton; }

    public void setFunctionDetails(String functionDetails) { this.functionDetails = functionDetails; }

    public String getFunctionDetails() { return functionDetails; }

    public void setOrigin(String origin) { this.origin = origin; }

    public String getOrigin() { return origin; }

    public void setMyAdditivesDescription(String myAdditivesDescription) { this.myAdditivesDescription = myAdditivesDescription; }

    public String getMyAdditivesDescription() { return myAdditivesDescription; }

    public void setDietaryRestrictions(String dietaryRestrictions) { this.dietaryRestrictions = dietaryRestrictions; }

    public String getDietaryRestrictions() { return dietaryRestrictions; }

    public void setSideEffects(String sideEffects) { this.sideEffects = sideEffects; }

    public String getSideEffects() { return sideEffects; }


    @Override
    public String toString() {
        return "EDBIngredient("+
                getKey()+ ", " +
                getTitle() + ", " +
                getType() + ", " +
                getWarning() + ", " +
                getBanned() + ", " +
                getAllowedInEU() + ", " +
                getWiki_notBanned() + ", " +
                getWiki_notConsideredDangerous() + ", " +
                getClassificaiton() + ", " +
                getFunctionDetails() + ", " +
                getOrigin() + ", " +
                getMyAdditivesDescription() + ", " +
                getDietaryRestrictions() + ", " +
                getSideEffects() + ", " +
                getMyAdditivesSafetyRating() + ", " +
                getDescription() + ")";

    }
}

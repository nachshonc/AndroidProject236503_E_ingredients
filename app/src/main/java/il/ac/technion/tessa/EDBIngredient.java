package il.ac.technion.tessa;

/**
 * Created by arietal on 11/17/15.
 */
public class EDBIngredient {
    private int _id;
    private String key, title, type, warning, banned;
    private String allowedInEU, wiki_notBanned, wiki_notConsideredDangerous;
    private String description;
    private String userAnnotations;


    public EDBIngredient(String key) {
        this.key = key;
        this.title = "";
        this.type = "";
        this.warning = "";
        this.banned = "";
        this.allowedInEU = "";
        this.wiki_notBanned = "";
        this.wiki_notConsideredDangerous = "";
        this.description = "";
        this.userAnnotations = "";
    }

    public void setID(int id) {
        this._id = id;
    }

    public int getID() {
        return this._id;
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

    public void setUserAnnotations(String userAnnotations) { this.userAnnotations = userAnnotations; }

    public String getUserAnnotations() { return userAnnotations; }


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
                getDescription() + ", "+
                getUserAnnotations() + ")";
    }
}

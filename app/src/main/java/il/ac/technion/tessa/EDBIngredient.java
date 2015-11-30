package il.ac.technion.tessa;

/**
 * Created by arietal on 11/17/15.
 */
public class EDBIngredient {
    private int _id;
    private String key, title, type, warning, banned;
    private String allowedInEU, wiki_notBanned, wiki_notConsideredDangerous;
    private String classification;
    private String functionDetails;
    private String origin;
    private String myAdditivesDescription;
    private String dietaryRestrictions;
    private String sideEffects;
    private String myAdditivesSafetyRating;
    private String everbumDescription, everbumSafetyRating;
    private String description;
    public static final EDBIngredient notFound = new EDBIngredient("Not found");


    public EDBIngredient(String key) {
        this.key = key;
        this.title = "";
        this.type = "";
        this.warning = "";
        this.banned = "";
        this.allowedInEU = "";
        this.wiki_notBanned = "";
        this.wiki_notConsideredDangerous = "";
        this.classification = "";
        this.functionDetails = "";
        this.origin = "";
        this.myAdditivesDescription = "";
        this.dietaryRestrictions = "";
        this.sideEffects = "";
        this.myAdditivesSafetyRating = "";
        this.everbumDescription = "";
        this.everbumSafetyRating = "";
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

    public void setClassification(String classificaiton) { this.classification = classificaiton; }

    public String getClassification() { return classification; }

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

    public void setEverbumDescription(String everbumDescription) { this.everbumDescription = everbumDescription; }

    public String getEverbumDescription() { return everbumDescription; }

    public void setEverbumSafetyRating(String everbumSafetyRating) { this.everbumSafetyRating = everbumSafetyRating; }

    public String getEverbumSafetyRating() { return everbumSafetyRating; }

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
                getClassification() + ", " +
                getFunctionDetails() + ", " +
                getOrigin() + ", " +
                getMyAdditivesDescription() + ", " +
                getDietaryRestrictions() + ", " +
                getSideEffects() + ", " +
                getMyAdditivesSafetyRating() + ", " +
                getEverbumDescription() + ", " +
                getEverbumSafetyRating() + ", " +
                getDescription() + ")";

    }
    public Options getOptions(){
        Options opt;
        if(this.isDangerous())
            opt=Options.DANG;
        else if(this.isUnhealthy() || this.isBanned())
            opt=Options.UNHEALTHY;
        else
            opt=Options.SAFE;
        return opt;
    }
    // a few heuristics based on al the data
    boolean isBanned() {
        return (getWiki_notBanned().equals("FALSE")
                || !getBanned().isEmpty()
                || getAllowedInEU().equals("FALSE"));
    }

    boolean isDangerous() {
        return (getWiki_notConsideredDangerous().equals("FALSE")
                || getMyAdditivesSafetyRating().equals("Dangerous")
                || getEverbumSafetyRating().equals("danger"));
    }

    boolean isSuspect() {
        return (!isDangerous()
                && (getMyAdditivesSafetyRating().equals("Suspect")
                    || getEverbumSafetyRating().equals("suspicious")));
    }

    boolean isUnhealthy() {
        return (!isDangerous()
                && !isSuspect()
                && (getMyAdditivesSafetyRating().equals("Unhealthy")
                || getEverbumSafetyRating().equals("avoid")));
    }

    boolean isSafe() {
        // this rules out the case where one site says it is safe, and another says it is not. A site is allowed to say nothing about an additive.
        return (!isBanned()
                && !isDangerous()
                && !isSuspect()
                && !isUnhealthy()
                && (getMyAdditivesSafetyRating().equals("Safe")
                    || getEverbumSafetyRating().equals("Safe")));
    }

    String getAdditiveSafetyRating() {
        if (isSafe())
            return "Safe";
        if (isUnhealthy())
            return "Unhealthy";
        if (isSuspect())
            return "Suspect";
        if (isDangerous())
            return "Dangerous";
        return "Unknown";
    }

    String getAdditiveType() {
        String type = getType();
        if ((type == null) || type.isEmpty()) {
            type = getClassification();
        }
        if (type == null)
            return "";
        return type;
    }

    public String toHTML() {
        StringBuilder result = new StringBuilder();

        result.append("<HTML><BODY background=#F0F0F0>\n");

        result.append("<h1>").append(getKey()).append(" details</h1>\n");
        result.append("<b>Name:&nbsp;</b>").append(getTitle()).append("<br/>\n");

        if (!getAdditiveType().isEmpty())
            result.append("<b>Type:&nbsp;</b>").append(getAdditiveType()).append("<br/>\n");

        result.append("<b>Safety rating:&nbsp;</b>").append(getAdditiveSafetyRating()).append("<br/>\n");

        if (!getWarning().isEmpty()) {
            result.append("<b>Safety concerns:&nbsp;</b>").append(getWarning()).append("<br/>\n");
        }

        if (!getSideEffects().isEmpty()) {
            result.append("<b>Side-effects may include:&nbsp;</b>").append(getSideEffects()).append("<br/>\n");
        }
        if (isBanned()) {
            result.append("<b>Additive is banned:</b><br/><ul>\n");
            if (getAllowedInEU().equals("FALSE"))
                result.append("<li>Not allowed in the European Union</li>\n");
            if (!getBanned().isEmpty())
                result.append("<li>").append(getBanned()).append("</li>\n");
            // figure out the meaning of banned in wiki and check and add here
            if (getWiki_notBanned().equals("FALSE"))
                result.append("<li>Banned in either the United States or the European Union or both</li>\n");
            result.append("</ul>\n");
        }

        if (!getDietaryRestrictions().isEmpty())
            result.append("<b>Dietary restrictions:&nbsp;</b>").append(getDietaryRestrictions()).append("<br/>\n");

        result.append("<hr><H2>Further reading</H2>\n");

        if (!getFunctionDetails().isEmpty())
            result.append("<b>Function:&nbsp;</b>").append(getFunctionDetails()).append("<br/>\n");

        if (!getOrigin().isEmpty())
            result.append("<br/><b>Origin:&nbsp;</b>").append(getOrigin()).append("<br/>\n");

        if (!getMyAdditivesDescription().isEmpty() || !getEverbumDescription().isEmpty() || !getDescription().isEmpty()) {
            result.append("<br/><b>Description:</b>\n");
            if (!getMyAdditivesDescription().isEmpty()) {
                result.append("<p>").append(getMyAdditivesDescription()).append("</p>\n");
            }
            if (!getEverbumDescription().isEmpty()) {
                result.append("<p>").append(getEverbumDescription()).append("</p>\n");
            }
            if (!getDescription().isEmpty()) {
                result.append(getDescription());
            }
        }

        result.append("</BODY></HTML>\n");

        return result.toString();
    }

}

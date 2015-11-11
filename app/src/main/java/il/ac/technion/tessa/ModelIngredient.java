package il.ac.technion.tessa;

/**
 * Created by nachshonc on 11/9/15.
 * Model class for an ingredient item
 */
public class ModelIngredient {
    private String tag;
    private String fullName;


    public ModelIngredient(String tag, String fullName) {
        super();
        this.fullName=fullName;
        this.tag=tag;
    }

    public String getFullName() {
        return fullName;
    }

    public String getTag() {
        return tag;
    }

}

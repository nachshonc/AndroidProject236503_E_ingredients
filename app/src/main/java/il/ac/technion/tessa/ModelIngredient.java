package il.ac.technion.tessa;

/**
 * Created by nachshonc on 11/9/15.
 */
public class ModelIngredient {
    private int icon;
    private String title;
    private String counter;

    private boolean isGroupHeader = false;

    public ModelIngredient(String title) {
        this(-1,title,null);
        isGroupHeader = true;
    }
    public ModelIngredient(int icon, String title) {
        super();
        this.icon = icon;
        this.title = title;
    }
    public ModelIngredient(int icon, String title, String counter) {
        super();
        this.icon = icon;
        this.title = title;
        this.counter = counter;
    }

    public int getIcon(){return icon; }
    public String getTitle(){return title; }
    public String getCounter(){return counter; }

}

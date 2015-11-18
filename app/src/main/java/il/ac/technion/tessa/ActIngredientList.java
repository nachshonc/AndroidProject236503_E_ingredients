package il.ac.technion.tessa;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by nachshonc on 11/10/15.
 * Choose food activity
 */
public class ActIngredientList extends ListActivity implements AdapterView.OnItemClickListener {

    public static final String PARAM_INGREDIENTS = "ingredientList";
    private AdapterIngredientList adapter;
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        adapter = new AdapterIngredientList(this, generateData(), this.getListView());
        setListAdapter(adapter);

        getListView().setOnItemClickListener(this);
    }
    private ArrayList<ModelIngredient> generateData(){
        ArrayList<String> list = getIntent().getStringArrayListExtra(PARAM_INGREDIENTS);
        ArrayList<ModelIngredient> models = new ArrayList<>();
        for(int i=0; i<list.size(); ++i) {
            ModelIngredient ingredient = IngredientDB.getIngredient(list.get(i));
            if(ingredient==null)
                ingredient=new ModelIngredient(list.get(i), "Unknown additive", true, false, false);
            models.add(ingredient);
        }

        return models;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(this.getApplicationContext(), String.format("Item %d chosen. ID=%s", position, adapter.getModel(position).getFullName()), Toast.LENGTH_SHORT).show();
        String url = "https://www.google.co.il/webhp?sourceid=chrome-instant&ion=1&espv=2&ie=UTF-8#q=" + adapter.getModel(position).getTag();
        url = (url + "+" + adapter.getModel(position).getFullName()).replaceAll(" +", "+");
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
}

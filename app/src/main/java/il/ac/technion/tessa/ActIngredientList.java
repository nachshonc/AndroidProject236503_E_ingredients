package il.ac.technion.tessa;

import android.app.ListActivity;
import android.content.Intent;
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
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        AdapterIngredientList adapter = new AdapterIngredientList(this, generateData(), this.getListView());
        setListAdapter(adapter);

        //getListView().setOnItemClickListener(this);
    }
    private ArrayList<ModelIngredient> generateData(){
        ArrayList<String> list = getIntent().getStringArrayListExtra(PARAM_INGREDIENTS);
        ArrayList<ModelIngredient> models = new ArrayList<>();
        Toast.makeText(getApplicationContext(), String.format("size=%d, item=%s", list.size(), list.get(0)), Toast.LENGTH_SHORT).show();
        //TODO: insert the list of ingredients to the adapter.
//        for(int i=0; i<fitems.length; ++i)
//            models.add(new ModelIngredient(fitems[i], getString(fStrItems[i])));

        return models;
    }

    //No need to implement in this stage of the project. Probably later...
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        /*//Toast.makeText(this.getApplicationContext(), String.format("Item %d chosen. ID=%d", position, id), Toast.LENGTH_SHORT).show();
        Intent res = new Intent();
        String strres = String.format("%d", position);
        res.putExtra("food", strres);
        setResult(RESULT_OK, res);
        finish();*/
    }
}

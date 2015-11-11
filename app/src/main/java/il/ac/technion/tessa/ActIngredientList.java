package il.ac.technion.tessa;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;

/**
 * Created by nachshonc on 11/10/15.
 * Choose food activity
 */
public class ActIngredientList extends ListActivity implements AdapterView.OnItemClickListener {

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        AdapterFoodList adapter = new AdapterFoodList(this, generateData(), this.getListView());
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
    }
    public static final int []fitems = {};
    public static final int []fStrItems={};

    private ArrayList<ModelFoodItem> generateData(){
        ArrayList<ModelFoodItem> models = new ArrayList<ModelFoodItem>();
        for(int i=0; i<fitems.length; ++i)
            models.add(new ModelFoodItem(fitems[i], getString(fStrItems[i])));
        /*models.add(new ModelFoodItem(R.drawable.fries, getString(R.string.food_fries)));
        models.add(new ModelFoodItem(R.drawable.rice, getString(R.string.food_rice)));
        models.add(new ModelFoodItem(R.drawable.salad, getString(R.string.food_salad)));
        models.add(new ModelFoodItem(R.drawable.spaghetti, getString(R.string.food_spaghetti)));
        models.add(new ModelFoodItem(R.drawable.wings, getString(R.string.food_wings)));*/

        return models;
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //Toast.makeText(this.getApplicationContext(), String.format("Item %d chosen. ID=%d", position, id), Toast.LENGTH_SHORT).show();
        Intent res = new Intent();
        String strres = String.format("%d", position);
        res.putExtra("food", strres);
        setResult(RESULT_OK, res);
        finish();
    }
}

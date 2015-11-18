package il.ac.technion.tessa;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;

public class ActIngredientList extends AppCompatActivity implements FrgIngredientList.OnFragmentInteractionListener{
    public static final String PARAM_INGREDIENTS = "ingredientList";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ingredient_list_layout);
        FragmentManager fragmentManager=getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ArrayList<String> list = getIntent().getStringArrayListExtra(ActIngredientList.PARAM_INGREDIENTS);
        FrgIngredientList fragment = FrgIngredientList.newInstance(list);//DEBUGItemFragment.newInstance("param1", "param2"); //
        fragmentTransaction.replace(R.id.root, fragment);
        fragmentTransaction.commit();

    }

    @Override
    public void onFragmentInteraction(String id) {

    }
}

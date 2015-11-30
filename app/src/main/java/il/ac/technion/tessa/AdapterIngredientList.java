package il.ac.technion.tessa;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.AvoidXfermode;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class AdapterIngredientList extends ArrayAdapter<EDBIngredient> implements View.OnClickListener {

    private final Context context;
    private final ArrayList<EDBIngredient> modelsArrayList;
    private SharedPreferences preferences;

    public AdapterIngredientList(Context context, ArrayList<EDBIngredient> modelsArrayList) {

        super(context, R.layout.list_item, modelsArrayList);

        this.context = context;
        this.modelsArrayList = modelsArrayList;
        this.preferences = UserPreferences.get(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        EDBIngredient ingredient = modelsArrayList.get(position);
        View rowView;
        TextView titleView;
        TextView counterView;
        int color=0;
        Options opt;
        if(ingredient.isDangerous())
            opt=Options.DANG;
        else if(ingredient.isUnhealthy() || ingredient.isBanned())
            opt=Options.UNHEALTHY;
        else
            opt=Options.SAFE;
        Log.d("getView: preferences ", preferences.contains(ingredient.getKey())?"true":"false");
        if(preferences.contains(ingredient.getKey())) {
            Log.d("getView: preferences ", String.format("%d", preferences.getInt(ingredient.getKey(), -1)));
            opt = Options.getOpt(preferences.getInt(ingredient.getKey(), -1));
        }

        if(ingredient.equals(EDBIngredient.notFound)){
            rowView = inflater.inflate(R.layout.list_item_empty, parent, false);
        }
        else if(opt==Options.SAFE) {
            rowView = inflater.inflate(R.layout.list_item, parent, false);
        }
        else{
            rowView = inflater.inflate(R.layout.list_item_dang, parent, false);
            ImageView image = (ImageView)rowView.findViewById(R.id.item_icon);
            if (opt==Options.DANG) {
                image.setImageResource(R.drawable.danger);
                color = 0xFFFF0000; //Strong RED
            } else {
                image.setImageResource(R.drawable.caution);
                color = 0x80FF0000;
            }
        }
        titleView = (TextView) rowView.findViewById(R.id.item_title);
        if(ingredient.equals(EDBIngredient.notFound)){
            titleView.setText("Could not detect any E-ingredients");
            titleView.setTypeface(null, Typeface.ITALIC);

        }
        else {
            counterView = (TextView) rowView.findViewById(R.id.item_tag);
            counterView.setText(ingredient.getKey());
            titleView.setText(ingredient.getTitle());
        }


        // 4. Set the text for textView
        rowView.setBackgroundColor(color);
        return rowView;
    }
    public EDBIngredient getModel(int idx){
        return modelsArrayList.get(idx);
    }
    public int getSize(){return modelsArrayList.size();}

    @Override
    public void onClick(View v) {
        Toast.makeText(this.getContext(), "AdapterIngredientList ", Toast.LENGTH_LONG).show();
    }
}
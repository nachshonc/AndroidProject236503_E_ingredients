package il.ac.technion.tessa;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class AdapterIngredientList extends ArrayAdapter<ModelIngredient> implements View.OnClickListener {

    private final Context context;
    private final ArrayList<ModelIngredient> modelsArrayList;

    public AdapterIngredientList(Context context, ArrayList<ModelIngredient> modelsArrayList) {

        super(context, R.layout.list_item, modelsArrayList);

        this.context = context;
        this.modelsArrayList = modelsArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ModelIngredient ingredient = modelsArrayList.get(position);
        View rowView;
        TextView titleView;
        TextView counterView;
        if(ingredient.getAllowedInEU() && !ingredient.getBanned() && !ingredient.getConsideredDangerous()) {
            rowView = inflater.inflate(R.layout.list_item, parent, false);
        }
        else{
            rowView = inflater.inflate(R.layout.list_item_dang, parent, false);
            ImageView image = (ImageView)rowView.findViewById(R.id.item_icon);
            if(ingredient.getAllowedInEU())
                image.setImageResource(R.drawable.caution);
        }
        titleView = (TextView) rowView.findViewById(R.id.item_title);
        counterView = (TextView) rowView.findViewById(R.id.item_tag);


        // 4. Set the text for textView
        titleView.setText(ingredient.getFullName());
        counterView.setText(ingredient.getTag());
        rowView.setBackgroundColor(ingredient.getColor());
        return rowView;
    }
    public ModelIngredient getModel(int idx){
        return modelsArrayList.get(idx);
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(this.getContext(), "AdapterIngredientList ", Toast.LENGTH_LONG).show();
    }
}
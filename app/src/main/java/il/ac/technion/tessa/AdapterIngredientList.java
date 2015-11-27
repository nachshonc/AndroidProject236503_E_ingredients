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

public class AdapterIngredientList extends ArrayAdapter<EDBIngredient> implements View.OnClickListener {

    private final Context context;
    private final ArrayList<EDBIngredient> modelsArrayList;

    public AdapterIngredientList(Context context, ArrayList<EDBIngredient> modelsArrayList) {

        super(context, R.layout.list_item, modelsArrayList);

        this.context = context;
        this.modelsArrayList = modelsArrayList;
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
        if((ingredient.isSafe() || ingredient.isSuspect()) && !ingredient.isBanned() && !ingredient.isUnhealthy()) {
            rowView = inflater.inflate(R.layout.list_item, parent, false);
        }
        else{
            rowView = inflater.inflate(R.layout.list_item_dang, parent, false);
            ImageView image = (ImageView)rowView.findViewById(R.id.item_icon);
            if (ingredient.isDangerous()) {
                image.setImageResource(R.drawable.danger);
                color = 0xFFFF0000; //Strong RED
            } else if(ingredient.isUnhealthy() || ingredient.isBanned()) {
                image.setImageResource(R.drawable.caution);
                color = 0x80FF0000;
            }
        }
        titleView = (TextView) rowView.findViewById(R.id.item_title);
        counterView = (TextView) rowView.findViewById(R.id.item_tag);


        // 4. Set the text for textView
        titleView.setText(ingredient.getTitle());
        counterView.setText(ingredient.getKey());

        rowView.setBackgroundColor(color);
        return rowView;
    }
    public EDBIngredient getModel(int idx){
        return modelsArrayList.get(idx);
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(this.getContext(), "AdapterIngredientList ", Toast.LENGTH_LONG).show();
    }
}
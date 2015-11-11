package il.ac.technion.tessa;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class AdapterIngredientList extends ArrayAdapter<ModelFoodItem> implements View.OnClickListener {

    private final Context context;
    private final ArrayList<ModelFoodItem> modelsArrayList;

    private final View []views;
    public AdapterIngredientList(Context context, ArrayList<ModelFoodItem> modelsArrayList, ViewGroup parent) {

        super(context, R.layout.list_item, modelsArrayList);

        this.context = context;
        this.modelsArrayList = modelsArrayList;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final int len=modelsArrayList.size();
        views = new View[len];
        for(int i=0; i<len; ++i){
            View rowView = inflater.inflate(R.layout.list_item, parent, false);
            // 3. Get icon,title & counter views from the rowView
            TextView titleView = (TextView) rowView.findViewById(R.id.item_title);
            TextView counterView = (TextView) rowView.findViewById(R.id.item_tag);

            // 4. Set the text for textView
            titleView.setText(modelsArrayList.get(i).getTitle());
            counterView.setText(modelsArrayList.get(i).getCounter());

            views[i]=rowView;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return views[position];
        // 1. Create inflater
/*
        // 2. Get rowView from inflater

        View rowView;
        if(convertView!=null){
            rowView = convertView;
        }
        else {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_item, parent, false);
        }

        // 3. Get icon,title & counter views from the rowView
        ImageView imgView = (ImageView) rowView.findViewById(R.id.item_icon);
        TextView titleView = (TextView) rowView.findViewById(R.id.item_title);
        TextView counterView = (TextView) rowView.findViewById(R.id.item_counter);

        // 4. Set the text for textView
        imgView.setImageResource(modelsArrayList.get(position).getIcon());
        titleView.setText(modelsArrayList.get(position).getTitle());
        counterView.setText(modelsArrayList.get(position).getCounter());

        // 5. retrn rowView
        return rowView;*/
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(this.getContext(), "AdapterIngredientList ", Toast.LENGTH_LONG).show();
    }
}
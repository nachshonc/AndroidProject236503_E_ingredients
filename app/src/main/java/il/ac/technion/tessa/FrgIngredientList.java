package il.ac.technion.tessa;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class FrgIngredientList extends ListFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "tagList";

    private ArrayList<String> ingredientTagList;

    private OnFragmentInteractionListener mListener;
    private AdapterIngredientList adapter;

    // TODO: Rename and change types of parameters
    public static FrgIngredientList newInstance(ArrayList<String> param1) {
        FrgIngredientList fragment = new FrgIngredientList();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FrgIngredientList() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            ingredientTagList = getArguments().getStringArrayList(ARG_PARAM1);
        }

        adapter = new AdapterIngredientList(getActivity(), generateData());
        setListAdapter(adapter);

    }
    private ArrayList<ModelIngredient> generateData(){
        ArrayList<String> list = ingredientTagList;
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Toast.makeText(this.getActivity(), String.format("Item %d chosen. ID=%s", position, adapter.getModel(position).getFullName()), Toast.LENGTH_SHORT).show();
        String url = "https://www.google.co.il/webhp?sourceid=chrome-instant&ion=1&espv=2&ie=UTF-8#q=" + adapter.getModel(position).getTag();
        url = (url + "+" + adapter.getModel(position).getFullName()).replaceAll(" +", "+");
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);

        if (null != mListener) {
            mListener.onFragmentInteraction(adapter.getModel(position).getTag());
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}

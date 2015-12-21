package il.ac.technion.tessa;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class StrongProgDialog extends DialogFragment {

    public interface YesNoListener {}
    public String message = "Title";


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d= new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setCancelable(false)
                .create();
        d.setCancelable(false);
        d.setCanceledOnTouchOutside(false);
        return d;
    }
}
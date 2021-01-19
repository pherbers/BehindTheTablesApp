package de.prkmd.behindthetables.view.dialog;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import de.prkmd.behindthetables.R;
import de.prkmd.behindthetables.activity.RandomTableEditActivity;
import de.prkmd.behindthetables.activity.TableSelectActivity;

public class NewTableCollectionDialogFragment extends DialogFragment {
    private final Context context;
    private final String category;

    public NewTableCollectionDialogFragment(Context context, String category) {
        this.context = context;
        this.category = category;
    }

    public NewTableCollectionDialogFragment(Context context) {
        this.context = context;
        this.category = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View v = inflater.inflate(R.layout.dialog_new_table_collection, null);
        final EditText editText = v.findViewById(R.id.edit_text_new_table_collection);
        editText.requestFocus();

        builder.setView(v)
                // Add action buttons
                .setPositiveButton(R.string.add_new, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(context, RandomTableEditActivity.class);

                        String name = editText.getText().toString();
                        if(name.isEmpty()) {
                            name = getString(R.string.new_table_collection);
                        }
                        intent.putExtra("Name", name);

                        if(category != null && !category.isEmpty())
                            intent.putExtra("Category", category);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        NewTableCollectionDialogFragment.this.getDialog().cancel();
                    }
                });
        Dialog d = builder.create();
        d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN|WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return d;
    }


}

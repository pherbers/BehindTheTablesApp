package de.prkmd.behindthetables.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import de.prkmd.behindthetables.R;

public class TextInputDialogFragment extends DialogFragment {

    private final String hint, text, title;
    private final TextEditListener listener;

    public TextInputDialogFragment(String title, String hint, String text, TextEditListener listener) {
        this.hint = hint;
        this.text = text;
        this.title = title;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View v = inflater.inflate(R.layout.dialog_edit_string, null);
        final TextView titleView = v.findViewById(R.id.edit_dialog_title);
        titleView.setText(title);

        final EditText editText = v.findViewById(R.id.edit_dialog_string);
        editText.setHint(hint);
        editText.setText(text);
        editText.requestFocus();

        builder.setView(v)
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String t = editText.getText().toString();
                        listener.onClick(t, !t.equals(text));
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        TextInputDialogFragment.this.getDialog().cancel();
                    }
                });
        Dialog d = builder.create();
        d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN|WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return d;
    }

    public interface TextEditListener {
        public void onClick(String text, boolean hasChanged);
    }
}

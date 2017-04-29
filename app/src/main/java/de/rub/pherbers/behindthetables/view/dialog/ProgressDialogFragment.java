package de.rub.pherbers.behindthetables.view.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

import de.rub.pherbers.behindthetables.R;

/**
 * Created by Nils on 23.04.2017.
 */

public class ProgressDialogFragment extends DialogFragment {

    @Override
    public ProgressDialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog blockingDialog = new ProgressDialog(getActivity());
        blockingDialog.setCancelable(false);
        blockingDialog.setIcon(R.mipmap.ic_launcher);
        blockingDialog.setTitle(R.string.app_name);
        blockingDialog.setIndeterminate(true);
        setCancelable(false);
        blockingDialog.setMessage(getString(R.string.action_discover_external_progressdialog));
        return blockingDialog;
    }

}

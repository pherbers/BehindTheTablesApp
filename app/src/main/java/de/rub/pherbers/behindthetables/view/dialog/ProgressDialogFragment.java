package de.rub.pherbers.behindthetables.view.dialog;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

import de.rub.pherbers.behindthetables.R;
import timber.log.Timber;

/**
 * Created by Nils on 23.04.2017.
 */

public class ProgressDialogFragment extends DialogFragment {

    private int messageID;

    @Override
    public ProgressDialog onCreateDialog(Bundle savedInstanceState) {
        String msg;
        try {
            msg = getString(getMessageID());
        } catch (Exception e) {
            Timber.e(e, "Failed to resolve saved message ID! Reverting to default!");
            msg = getString(R.string.info_db_setup_generic);
        }
        Timber.i("Showing blocking dialog. Message: " + msg);

        ProgressDialog blockingDialog = new ProgressDialog(getActivity());
        blockingDialog.setCancelable(false);
        blockingDialog.setIcon(R.mipmap.ic_launcher);
        blockingDialog.setTitle(R.string.app_name);
        blockingDialog.setIndeterminate(true);
        setCancelable(false);
        blockingDialog.setMessage(msg);
        return blockingDialog;
    }

    public int getMessageID() {
        return messageID;
    }

    public void setMessageID(int messageID) {
        Timber.i("Setting message ID for blocking dialog: " + messageID);
        this.messageID = messageID;
    }

}

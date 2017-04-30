package de.rub.pherbers.behindthetables.concurrent.task;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import de.rub.pherbers.behindthetables.BehindTheTables;
import de.rub.pherbers.behindthetables.data.io.FileManager;
import de.rub.pherbers.behindthetables.sql.DBAdapter;
import de.rub.pherbers.behindthetables.sql.DefaultTables;
import timber.log.Timber;

/**
 * Created by Nils on 21.04.2017.
 */

public class BuildDBTask extends AsyncTask<Void, Void, Boolean> {

    public static final String INTENT_EXTRA_DB_TASK_IMPORTED = BehindTheTables.APP_TAG + "intent_extra_db_task_imported";
    public static final String INTENT_EXTRA_DB_TASK_FAILED = BehindTheTables.APP_TAG + "intent_extra_db_task_failed";

    private Context context;
    private DBAdapter adapter;
    private ArrayList<File> externalTableFiles;
    private ArrayList<File> errorFiles;
    private BuildDBTaskListener listener;
    private Date startingDate;
    private Class callBackTarget;

    public BuildDBTask(Context context) {
        this(context, null);
    }

    public BuildDBTask(Context context, Class callBackTarget) {
        this.context = context;
        this.callBackTarget = callBackTarget;

        externalTableFiles = new ArrayList<>();
        errorFiles = new ArrayList<>();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        startingDate = new Date();
        adapter = new DBAdapter(context).open();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        boolean b = DefaultTables.discoverDefaultTables(context, adapter);
        FileManager manager = new FileManager(context);

        if (isAbleToRequestPermissions()) {
            if (manager.hasPermission()) {
                File origin = manager.getExternalTableDir();
                Timber.i("Attempting to search for external files in " + origin.getAbsolutePath());

                searchExternalDir(origin);
            } else {
                Timber.w("Can't look for external files. Permission denied.");
            }
        } else {
            Timber.w("DB Builder does not have an activity as its context! Storage-Permissions can't be obtained, so it is assumed permissions are denied!");
        }

        for (int i = 0; i < 10; i++) {
            Timber.i("Sleeping stage... " + i);
            try {
                Thread.sleep(250); //TODO remove debug sleep
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return b;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

        Timber.w("Database size after init = " + adapter.getAllTableCollections().getCount() + ". Filling took " + (new Date().getTime() - startingDate.getTime()) + " ms.");
        adapter.close();
        if (listener != null) {
            listener.onDBSetupFinished(result, externalTableFiles, errorFiles);
        }
        if (callBackTarget != null) {
            Intent intent = new Intent(context, callBackTarget);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            intent.putExtra(INTENT_EXTRA_DB_TASK_IMPORTED,parseFileArray(externalTableFiles));
            intent.putExtra(INTENT_EXTRA_DB_TASK_FAILED,parseFileArray(errorFiles));
            Timber.i("Parsed extra example (imported list): "+parseFileArray(externalTableFiles));

            context.startActivity(intent);
        }
    }

    private void searchExternalDir(File dir) {
        Timber.i("Searching for external files in " + dir.getAbsolutePath());
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                searchExternalDir(f);
            } else {
                String path = f.getAbsolutePath();
                if (path.toLowerCase().endsWith(".json")) {
                    Timber.i("Found a valid json file: " + path);
                    externalTableFiles.add(f);

                    try {
                        DefaultTables.insertTable(f, adapter);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        Timber.e(e, "Failed to insert external file to the DB: " + f.getAbsolutePath());
                        errorFiles.add(f);
                    }
                } else {
                    Timber.w("Found an invalid json file: " + path);
                }
            }
        }
    }

    private String[] parseFileArray(ArrayList<File> list) {
        String[] ret = new String[list.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = list.get(i).getAbsolutePath();
            Timber.i("Parsing list. Pos: "+i+" Item: "+list.get(i).getAbsolutePath());
        }
        Timber.i("Packed list to String-Array: Len.: "+ret.length);

        return ret;
    }

    @Deprecated
    public void setListener(BuildDBTaskListener listener) {
        this.listener = listener;
    }

    public boolean isAbleToRequestPermissions() {
        return context instanceof Activity;
    }

    public interface BuildDBTaskListener {

        public void onDBSetupFinished(boolean success, ArrayList<File> foundFiles, ArrayList<File> errorFiles);

    }
}

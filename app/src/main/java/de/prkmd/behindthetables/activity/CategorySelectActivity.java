package de.prkmd.behindthetables.activity;

import android.Manifest;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import de.prkmd.behindthetables.BehindTheTables;
import de.prkmd.behindthetables.R;
import de.prkmd.behindthetables.concurrent.task.BuildDBTask;
import de.prkmd.behindthetables.data.TableFile;
import de.prkmd.behindthetables.imported.nilsfo.FileManager;
import de.prkmd.behindthetables.sql.DBAdapter;
import de.prkmd.behindthetables.view.dialog.ProgressDialogFragment;
import timber.log.Timber;

import static de.prkmd.behindthetables.BehindTheTables.APP_TAG;
import static de.prkmd.behindthetables.BehindTheTables.PREFS_TAG;
import static de.prkmd.behindthetables.activity.TableSelectActivity.EXTRA_SEARCH_REQUEST_QUERY;
import static de.prkmd.behindthetables.concurrent.task.BuildDBTask.INTENT_EXTRA_DB_QUENCH_UPDATE_REQUEST;
import static de.prkmd.behindthetables.concurrent.task.BuildDBTask.INTENT_EXTRA_DB_TASK_FAILED;
import static de.prkmd.behindthetables.concurrent.task.BuildDBTask.INTENT_EXTRA_DB_TASK_IMPORTED;

public class CategorySelectActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, BuildDBTask.BuildDBTaskListener {

    public static final int EXTERNAL_STORAGE_REQUEST_CODE = 1;
    public static final String DIALOG_IDENTIFIER = APP_TAG + "category_dialog";
    public static final String PREFERENCES_REQUEST_DB_UPDATE_TEXT = PREFS_TAG + "request_db_update_text";
    public static final int PREFERENCES_REQUEST_DB_UPDATE_SKIP = -1;
    private CategoryAdapter adapter;
    private BuildDBTask buildDBTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_select);

        GridView gridView = (GridView) findViewById(R.id.category_list_view);
        adapter = new CategoryAdapter(this);
        gridView.setAdapter(adapter);

        Intent intent = getIntent();
        if (intent != null) {
            Timber.i("Checking the intent extras. Has TASK_IMPORTED: " + intent.hasExtra(INTENT_EXTRA_DB_TASK_IMPORTED) + ", TASK_FAILED: " + intent.hasExtra(INTENT_EXTRA_DB_TASK_FAILED));

            if (intent.hasExtra(INTENT_EXTRA_DB_TASK_IMPORTED)) {
                boolean quench = intent.getBooleanExtra(INTENT_EXTRA_DB_QUENCH_UPDATE_REQUEST, true);
                if (quench) {
                    PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(PREFERENCES_REQUEST_DB_UPDATE_TEXT, PREFERENCES_REQUEST_DB_UPDATE_SKIP).apply();
                }

                String[] imported = intent.getStringArrayExtra(INTENT_EXTRA_DB_TASK_IMPORTED);
                String[] failed = intent.getStringArrayExtra(INTENT_EXTRA_DB_TASK_FAILED);
                Timber.i("Checking the resulting file arrays: Imported: " + Arrays.toString(imported) + " Failed: " + Arrays.toString(failed));

                intent.removeExtra(INTENT_EXTRA_DB_TASK_IMPORTED);
                intent.removeExtra(INTENT_EXTRA_DB_TASK_FAILED);
                evaluateImport(imported, failed);
            }
        }

        gridView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Timber.i("User clicked on category in position " + i);
        Bundle b = adapter.getTableInfo(i);

        requestTableActivity(b);
    }

    public void requestTableActivity(Bundle args) {
        Intent intent = new Intent(this, TableSelectActivity.class);
        intent.putExtras(args);
        Timber.w("Changing to a table activity from the 'category select' activity. Category args: " + args.getLong(TableSelectActivity.EXTRA_CATEGORY_DISCRIMINATOR, -1));
        startActivity(intent);
    }

    public void requestDiscoverExternalFiles() {
        FileManager manager = new FileManager(this);
        if (!manager.hasPermission()) {
            Timber.i("This app does not have permission to write the external storage!");
            Timber.i("Asking for permissions now.");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    EXTERNAL_STORAGE_REQUEST_CODE);
        } else {
            File externalTableDir = manager.getExternalTableDir();
            Timber.i("The App has access to the external storage. Displaying info dialog now. Looking in " + externalTableDir.getAbsolutePath());

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.app_name);
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setMessage(getString(R.string.info_discover_external_files_dialog, externalTableDir.getAbsolutePath()));
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            builder.setPositiveButton(R.string.action_discover_external_dialog, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    discoverExternalFiles(false, true, false, R.string.info_db_setup_external);
                }
            });
            builder.setNeutralButton(R.string.action_more_info, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.const_custom_table_link)));
                    startActivity(browserIntent);
                }
            });
            builder.show();
        }
    }

    public void checkForDBUpdateRequest() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int textID = preferences.getInt(PREFERENCES_REQUEST_DB_UPDATE_TEXT, R.string.info_db_setup_generic);
        if (textID == PREFERENCES_REQUEST_DB_UPDATE_SKIP) return;

        discoverExternalFiles(true, true, true, textID);
    }

    private void discoverExternalFiles(boolean includeDefaults, boolean includeExternal, boolean quenchSetupRequest, int messageID) {
        Timber.i("Executing a new request to discover DB files! Include defaults: " + includeDefaults + ". Include externals: " + includeExternal + ". QuenchSetupRequest: " + quenchSetupRequest + ". Message: '" + getString(messageID) + "'.");
        Timber.i("Request stack trace:\n" + Arrays.toString(Thread.currentThread().getStackTrace()));
        buildDBTask = new BuildDBTask(this, getClass(), includeDefaults, includeExternal, quenchSetupRequest);
        buildDBTask.execute();

        displayBlockingDialog(messageID);
    }

    private void displayBlockingDialog(int messageID) {
        FragmentManager fragmentManager = getFragmentManager();
        ProgressDialogFragment newFragment = new ProgressDialogFragment();
        newFragment.setMessageID(messageID);
        newFragment.show(fragmentManager, DIALOG_IDENTIFIER);
    }

    private void evaluateImport(String[] imported, String[] failed) {
        int importedCount = imported.length;
        int failedCount = failed.length;

        if (importedCount == 0) {
            Toast.makeText(this, R.string.info_imports_nothing_found, Toast.LENGTH_LONG).show();
            return;
        }

        if (failedCount == 0) {
            Toast.makeText(this, getString(R.string.info_imports_everything_ok, importedCount), Toast.LENGTH_LONG).show();
        } else {
            int percent = (int) ((failedCount * 100.0f) / importedCount);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.info_imports_failed_title);
            builder.setMessage(getString(R.string.info_imports_failed_text, String.valueOf(failedCount), String.valueOf(importedCount), percent + "%", formatFileList(failed)));
            builder.setIcon(R.drawable.ic_warning_black_48dp);
            builder.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.setPositiveButton(R.string.action_again, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    requestDiscoverExternalFiles();
                }
            });
            builder.show();
        }
    }

    private String formatFileList(String[] list) {
        StringBuilder builder = new StringBuilder();
        int len = new FileManager(this).getExternalTableDir().getAbsolutePath().length();

        for (String s : list) {
            String line = "\n-" + s.substring(len);
            builder.append(line);
        }

        return builder.toString().trim();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_category_select, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clar_favs:
                requestClearFavs();
                break;
            case R.id.action_category_select_search:
                requestSearch();
                break;
            case R.id.action_category_settings: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.action_discover_external:
                requestDiscoverExternalFiles();
                break;
            default:
                Timber.w("Unknown menu item selected.");
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void requestSearch() {
        Intent intent = new Intent(this, TableSelectActivity.class);
        intent.putExtra(EXTRA_SEARCH_REQUEST_QUERY, "");
        startActivity(intent);
    }

    private void requestClearFavs() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int count = preferences.getStringSet(TableFile.PREFS_FAVORITE_TABLES, new HashSet<String>()).size();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_star_black_48dp);
        builder.setTitle(R.string.app_name);
        builder.setMessage(getString(R.string.action_clear_favs_detail, count));
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.setPositiveButton(R.string.action_clear_favs, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                BehindTheTables.clearFavs(CategorySelectActivity.this);
            }
        });
        builder.show();
    }

    @Override
    public void onDBSetupFinished(boolean success, ArrayList<File> allExternalFiles, ArrayList<File> errorExternalFiles) {
        buildDBTask = null;
        int found = allExternalFiles.size();
        int errors = errorExternalFiles.size();
        Timber.i("Finished executing paralel DB task. External files found: " + found + ". Of those errors contained: " + errors);

        DialogFragment df = (DialogFragment) getFragmentManager().findFragmentByTag(DIALOG_IDENTIFIER);
        df.dismiss();
    }

    class CategoryAdapter extends BaseAdapter {

        private Context context;
        private Cursor bufferedCategories;

        public CategoryAdapter(Context context) {
            this.context = context;
            DBAdapter adapter = new DBAdapter(context);

            adapter.open();
            bufferedCategories = adapter.getAllCategories(DBAdapter.KEY_CATEGORY_TITLE);
            adapter.close();

            checkForDBUpdateRequest();

            Timber.i("Displaying category tiles. Count: " + getCount());
        }

        public Bundle getTableInfo(int position) {
            Bundle b = new Bundle();

            if (bufferedCategories.moveToPosition(position)) {
                b.putLong(TableSelectActivity.EXTRA_CATEGORY_DISCRIMINATOR, bufferedCategories.getLong(DBAdapter.COL_CATEGORY_ROWID));
            }
            if (isCategoryFavs(position)) {
                b.putBoolean(TableSelectActivity.EXTRA_FAVS_ONLY, true);
            }

            return b;
        }

        public boolean isCategoryAll(int i) {
            return i == bufferedCategories.getCount();
        }

        public boolean isCategoryFavs(int i) {
            return i == bufferedCategories.getCount() + 1;
        }

        @Override
        public int getCount() {
            return bufferedCategories.getCount() + 2;
        }

        @Override
        public String getItem(int i) {
            Timber.v("Category item requested at position " + i);

            if (isCategoryAll(i)) return context.getString(R.string.category_all);
            if (isCategoryFavs(i)) return context.getString(R.string.category_favs);

            if (bufferedCategories.moveToPosition(i)) {
                return bufferedCategories.getString(DBAdapter.COL_CATEGORY_TITLE);
            }

            Timber.w("No category found at the position " + i);
            return context.getString(R.string.error_unknown);
        }

        @Override
        public long getItemId(int i) {
            if (bufferedCategories.moveToPosition(i)) {
                return bufferedCategories.getLong(DBAdapter.COL_CATEGORY_ROWID);
            }
            return DBAdapter.CATEGORY_NOT_FOUND;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v;
            if (view != null)
                v = view;
            else
                v = LayoutInflater.from(context).inflate(R.layout.category_item, viewGroup, false);

            TextView tv = (TextView) v.findViewById(R.id.category_item_text);

            String text = getItem(i);

            Timber.v("Displaying category button on position " + i + " -> '" + text + "'.");
            tv.setText(getItem(i));

            return tv;
        }
    }
}

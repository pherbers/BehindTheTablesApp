package de.prkmd.behindthetables.activity;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import de.prkmd.behindthetables.BehindTheTables;
import de.prkmd.behindthetables.R;
import de.prkmd.behindthetables.sql.DBAdapter;
import de.prkmd.behindthetables.util.VersionManager;
import timber.log.Timber;

public class SettingsActivity extends AppCompatPreferenceActivity {

    public static final int LAST_UPDATE_DATE_UNKNOWN = -1;
    public static final SettingsBindPreferenceSummaryToValueListener defaultListener = new SettingsBindPreferenceSummaryToValueListener();

    private static int getUpdateDays(Context context) {
        Date now = new Date();
        Date lastUpdate = null;
        try {
            lastUpdate = VersionManager.getCurrentVersionDate(context);
        } catch (Exception e) {
            e.printStackTrace();
            Timber.e(e);
            return LAST_UPDATE_DATE_UNKNOWN;
        }

        long diffTime = now.getTime() - lastUpdate.getTime();
        long diffDays = diffTime / (1000 * 60 * 60 * 24);
        return (int) diffDays;
    }

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    public static void bindPreferenceURLAsAction(Preference preference, final Uri uri, final boolean chooser) {
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                if (chooser) {
                    browserIntent = Intent.createChooser(browserIntent, preference.getContext().getString(R.string.share_via));
                }

                preference.getContext().startActivity(browserIntent);
                return true;
            }
        });
    }

    public static void bindPreferenceURLAsAction(Preference preference, Uri uri) {
        bindPreferenceURLAsAction(preference, uri, false);
    }

    public static void bindPreferenceURLAsAction(Preference preference) {
        String summary = preference.getSummary().toString();
        bindPreferenceURLAsAction(preference, Uri.parse(summary));
    }

    public static void bindPreferenceSummaryToValue(Preference preference) {
        bindPreferenceSummaryToValue(preference, null);
    }

    public static void bindPreferenceSummaryToValue(Preference preference, Integer resource) {
        if (preference == null) return;

        SettingsBindPreferenceSummaryToValueListener listener = defaultListener;
        if (resource != null) {
            listener = new SettingsBindPreferenceSummaryToValueListener(resource);
        }

        preference.setOnPreferenceChangeListener(listener);
        listener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), preference.getContext().getString(R.string.error_unknown)));
    }

    @Override
    public void onCreate(Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        /*switch(PreferenceManager.getDefaultSharedPreferences(this).getString("prefs_dark_mode", "Off")) {
            case "On":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "Off":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }*/
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.prefs_headers, target);
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || AboutPreferenceFragment.class.getName().equals(fragmentName);
    }

    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs_general);
            setHasOptionsMenu(true);

            final Preference resetDB = findPreference("prefs_reset_database");
            resetDB.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final Context context = preference.getContext();
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(R.string.app_name);
                    builder.setIcon(R.mipmap.ic_launcher);
                    builder.setMessage(getString(R.string.info_discover_reset_database_dialog));
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    builder.setPositiveButton(R.string.action_reset_database_dialog, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            resetDB();
                        }
                    });
                    builder.show();
                    return true;
                }
            });
            final Preference backupTables = findPreference("prefs_backup_custom_tables");
            backupTables.setOnPreferenceClickListener(preference -> {
                requestBackupTables(preference.getContext());
                return true;
            });

            final Preference restoreFromBackup = findPreference("prefs_restore_custom_tables");
            restoreFromBackup.setOnPreferenceClickListener(preference -> {
                restoreFromBackup(preference.getContext());
                return true;
            });


        }

        public void resetDB() {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
            editor.putInt(CategorySelectActivity.PREFERENCES_REQUEST_DB_UPDATE_TEXT, R.string.info_db_setup_reset);
            editor.apply();

            Intent intent = new Intent(getActivity(), CategorySelectActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        static final int BACKUP_WRITE_REQUEST_CODE = 451;
        public void requestBackupTables(Context context)  {
            Intent saveIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

            String formattedDate = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.US).format(new Date());

            saveIntent.setType("application/zip");
            saveIntent.putExtra(Intent.EXTRA_TITLE, "BTT_Backup_" + formattedDate + ".zip");

            context.startActivity(saveIntent);
            startActivityForResult(saveIntent, BACKUP_WRITE_REQUEST_CODE);
        }

        public int backupTables(Context context, Uri backupURI) throws IOException {
            OutputStream outputStream = context.getContentResolver().openOutputStream(backupURI);

            DBAdapter adapter = new DBAdapter(context).open();
            Cursor customTables = adapter.getAllCustomTableCollections();
            ZipOutputStream outStream = new ZipOutputStream(outputStream);

            int nExportedTables = 0;

            while(!customTables.isAfterLast()) {
                String tableFileLocation = customTables.getString(0);
                String fileName = tableFileLocation.substring(tableFileLocation.lastIndexOf("/") + 1);

                Timber.tag("Preferences_Backup").i("Backing up table file %s", fileName);

                outStream.putNextEntry(new ZipEntry(fileName));

                FileInputStream inStream = new FileInputStream(tableFileLocation);
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = inStream.read(buf)) > 0) {
                    outStream.write(buf, 0, len);
                }
                inStream.close();

                customTables.moveToNext();

                nExportedTables++;
            }
            outStream.close();

            Timber.tag("Preferences_Backup").i("Backed up %d tables to %s", nExportedTables, backupURI);

            adapter.close();
            outputStream.close();

            return nExportedTables;
        }

        public void restoreFromBackup(Context context) {

        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if(requestCode == BACKUP_WRITE_REQUEST_CODE && resultCode == RESULT_OK) {
                Uri backupLocation = data.getData();
                try {
                    int nExported = backupTables(getActivity().getApplicationContext(), backupLocation);
                    if(nExported > 0)
                        Toast.makeText(getActivity().getApplicationContext(),
                            getString(R.string.prefs_backup_success, nExported),
                            Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity().getApplicationContext(),
                                R.string.prefs_backup_noop,
                                Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(getActivity().getApplicationContext(),
                            R.string.prefs_backup_error,
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AboutPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs_credits);
            setHasOptionsMenu(true);

            int versionCode = 0;
            String versionName = getString(R.string.error_unknown);
            String appName = getString(R.string.app_name);

            PackageManager manager = getActivity().getPackageManager();
            try {
                PackageInfo info = manager.getPackageInfo(getActivity().getPackageName(), 0);
                versionName = info.versionName;
                versionCode = info.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            final int finalVersionCode = versionCode;

            Preference viewOnPlayStore = findPreference("prefs_view_in_playstore");
            //viewOnPlayStore.setSummary(String.format(getString(R.string.prefs_about_summary), appName, versionName));
            viewOnPlayStore.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Context context = getActivity();
                    Intent playStoreIntent = BehindTheTables.buildAppMarketIntent(context);
                    try {
                        context.startActivity(playStoreIntent);
                    }
                    catch (Exception e){
                        Timber.e(e);
                        Toast.makeText(context,R.string.error_internal_error,Toast.LENGTH_LONG).show();
                    }
                    return false;
                }
            });

            Preference changeLogPreference = findPreference("prefs_view_changelog");
            int lastUpdateDays = getUpdateDays(getActivity());
            String lastUpdateText = getString(R.string.error_internal_error);
            if (lastUpdateDays == LAST_UPDATE_DATE_UNKNOWN) {
                Timber.w("Failed to get the last date this app was updated! Doing nothing, as this will result in the text that an error occured!");
            } else {
                lastUpdateText = String.valueOf(lastUpdateDays);
            }
            changeLogPreference.setSummary(getString(R.string.prefs_view_changelog_summary,versionName,lastUpdateText));
            changeLogPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    VersionManager.displayVersionUpdateNews(preference.getContext(), finalVersionCode);
                    return false;
                }
            });

            bindPreferenceURLAsAction(findPreference("prefs_credits_github"));
            bindPreferenceURLAsAction(findPreference("prefs_credits_nilsfo"));
            bindPreferenceURLAsAction(findPreference("prefs_credits_pherbers"));

            bindPreferenceURLAsAction(findPreference("prefs_credits_timber"));
            bindPreferenceURLAsAction(findPreference("prefs_credits_debug_db"));
            bindPreferenceURLAsAction(findPreference("prefs_credits_animators"));
            bindPreferenceURLAsAction(findPreference("prefs_credits_btt"));
            bindPreferenceURLAsAction(findPreference("prefs_credits_leakcanary"));
        }
    }

    public static class SettingsBindPreferenceSummaryToValueListener implements Preference.OnPreferenceChangeListener {

        private Integer stringResource = null;

        public SettingsBindPreferenceSummaryToValueListener() {
        }

        public SettingsBindPreferenceSummaryToValueListener(int stringResource) {
            this.stringResource = stringResource;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            Context context = preference.getContext();

            if (preference instanceof ListPreference) {
                // For list prefs_general, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.

                String text = index >= 0 ? listPreference.getEntries()[index].toString() : context.getString(R.string.error_unknown);
                if (stringResource != null) {
                    text = context.getString(stringResource, text);
                }

                preference.setSummary(text);
            } else {
                // For all other prefs_general, set the summary to the value's
                // simple string representation.
                if (stringResource != null) {
                    stringValue = context.getString(stringResource, stringValue);
                }

                preference.setSummary(stringValue);
            }
            return true;
        }
    }

}

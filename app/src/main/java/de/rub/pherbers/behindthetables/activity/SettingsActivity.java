package de.rub.pherbers.behindthetables.activity;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.util.List;

import de.rub.pherbers.behindthetables.R;
import de.rub.pherbers.behindthetables.sql.DBAdapter;
import timber.log.Timber;

public class SettingsActivity extends AppCompatPreferenceActivity {

    public static final SettingsBindPreferenceSummaryToValueListener defaultListener = new SettingsBindPreferenceSummaryToValueListener();

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

    public static void bindPreferenceURLAsAction(Preference preference, final Uri uri) {
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
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

                            DBAdapter adapter = new DBAdapter(context).open();
                            adapter.fillWithDefaultData(context);
                            adapter.close();
                            Toast.makeText(context, R.string.prefs_reset_db_success, Toast.LENGTH_LONG).show();
                        }
                    });
                    builder.show();
                    return true;
                }
            });

        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AboutPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs_credits);
            setHasOptionsMenu(true);

            bindPreferenceURLAsAction(findPreference("prefs_credits_github"));
            bindPreferenceURLAsAction(findPreference("prefs_credits_nilsfo"));
            bindPreferenceURLAsAction(findPreference("prefs_credits_pherbers"));

            bindPreferenceURLAsAction(findPreference("prefs_credits_timber"));
            bindPreferenceURLAsAction(findPreference("prefs_credits_debug_db"));
            bindPreferenceURLAsAction(findPreference("prefs_credits_animators"));
            bindPreferenceURLAsAction(findPreference("prefs_credits_btt"));
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
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || AboutPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.prefs_headers, target);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

}

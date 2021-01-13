package de.prkmd.behindthetables.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import androidx.appcompat.app.AlertDialog;
import de.prkmd.behindthetables.BehindTheTables;
import de.prkmd.behindthetables.R;
import de.prkmd.behindthetables.activity.CategorySelectActivity;
import timber.log.Timber;

/**
 * Created by Nils on 24.03.2017.
 */

public class VersionManager {

    public static final String VERSION_RELEASE_DATE_PATTERN = "dd.MM.yyyy";
    public static final int CURRENT_VERSION_UNKNOWN = -1;

    private static JSONObject cachedChangeLog;
    private Context context;

    public VersionManager(Context context) {
        this.context = context;
    }

    public void onVersionChange(int oldVersion, int newVersion) {
        Timber.i("App version changed! " + oldVersion + " -> " + newVersion);

        switch (newVersion) {
            default:
                Timber.w("Unknown version change! Let's reset the DB, just in case!");
                placeDBTaskRequest();
                break;
        }

        displayVersionUpdateNews(context, newVersion);
    }

    public void onFirstTimeStartup() {
        Timber.i("Welcome to BTT! Performing first time code!");
        placeDBTaskRequest(R.string.info_first_time_setup);
    }

    public void placeDBTaskRequest() {
        placeDBTaskRequest(R.string.info_db_setup_generic);
    }

    public void placeDBTaskRequest(int textID) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(CategorySelectActivity.PREFERENCES_REQUEST_DB_UPDATE_TEXT, textID);
        editor.apply();
    }

    public String getVersionName() throws PackageManager.NameNotFoundException {
        PackageManager manager = context.getPackageManager();
        PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
        return info.versionName;
    }

    public int getVersionCode() throws PackageManager.NameNotFoundException {
        PackageManager manager = context.getPackageManager();
        PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
        return info.versionCode;
    }

    public static void displayVersionUpdateNews(final Context context, final int version) {
        JSONObject versionList;
        try {
            versionList = getChanceLog(context);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Timber.e(e);
            Toast.makeText(context, R.string.error_internal_error, Toast.LENGTH_LONG).show();
            return;
        }

        SimpleDateFormat jasonDatePattern = new SimpleDateFormat(VERSION_RELEASE_DATE_PATTERN);
        Timber.i("Looking for version changelog for entry: " + version);
        Timber.i("Read changelog JSON: " + versionList.toString());

        String versionName = null, changelog = null, wholeChangelog = null;
        try {
            if (versionList.has(String.valueOf(version))) {
                JSONObject currentVersion = versionList.getJSONObject(String.valueOf(version));
                Timber.i("Reading Version file for " + currentVersion + " (" + version + ")");
                versionName = currentVersion.getString("version");
                changelog = currentVersion.getString("text");
            }

            StringBuilder builder = new StringBuilder();
            Iterator<String> it = versionList.keys();
            TimeUtils utils = new TimeUtils(context);
            while (it.hasNext()) {
                JSONObject currentVersion = versionList.getJSONObject(it.next());
                builder.append(currentVersion.getString("version"));

                String dateText = null;
                if (currentVersion.has("date")) {
                    dateText = currentVersion.getString("date");
                } else {
                    dateText = jasonDatePattern.format(new Date(0));
                    //dateText=context.getString(R.string.error_unknown);
                }

                try {
                    Date d = jasonDatePattern.parse(dateText);
                    builder.append(" (");
                    builder.append(utils.formatDateAbsolute(d));
                    builder.append(")");
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                builder.append("\n");
                builder.append(currentVersion.getString("text"));
                builder.append("\n\n");
            }
            wholeChangelog = builder.toString().trim();
        } catch (JSONException e) {
            e.printStackTrace();
            Timber.e(e);
            Toast.makeText(context, R.string.error_internal_error, Toast.LENGTH_LONG).show();
            return;
        }

        Timber.i("Version info found: " + versionName + " -> " + changelog.replace("\n", "") + " everything: " + wholeChangelog.replace("\n", ""));
        AlertDialog dialog = buildChangelogDialog(context, context.getString(R.string.info_changelog_version, versionName), changelog, true, wholeChangelog);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }


    private static AlertDialog buildChangelogDialog(final Context context, final String title, final String text, boolean showAllButton, final String allText) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.dialog_changelog, null, false);

        TextView nameTF = (TextView) v.findViewById(R.id.dialog_changelog_fragment_name);
        TextView changelogTF = (TextView) v.findViewById(R.id.dialog_changelog_fragment_changelog);
        nameTF.setText(title);
        changelogTF.setText(text);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setIcon(R.mipmap.ic_launcher);
        dialogBuilder.setTitle(R.string.info_update_news);
        dialogBuilder.setView(v);

        if (showAllButton) {
            dialogBuilder.setNegativeButton(R.string.action_full_changelog,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                            buildChangelogDialog(context, context.getString(R.string.action_full_changelog), allText, false, allText).show();
                        }
                    }
            );
        }
        dialogBuilder.setPositiveButton(R.string.action_dismiss,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }
        );
        dialogBuilder.setNeutralButton(R.string.action_feedback, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent goToMarket = BehindTheTables.buildAppMarketIntent(context);
                try {
                    context.startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    Timber.e(e);
                    String packageName = context.getPackageName();
                    context.startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)));
                }
            }
        });
        return dialogBuilder.create();
    }

	public static JSONObject getChanceLog(Context context) throws IOException, JSONException {
		if (cachedChangeLog != null) {
			return cachedChangeLog;
		}

		InputStream inputStream = context.getResources().openRawResource(R.raw.version_changelog);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

		String str = "";
		StringBuilder builder = new StringBuilder();
		while ((str = reader.readLine()) != null) {
			builder.append(str);
		}

		cachedChangeLog = new JSONObject(builder.toString());
		return getChanceLog(context);
	}

    public static int getCurrentVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Timber.e(e);
        }
        return CURRENT_VERSION_UNKNOWN;
    }

    public static Date getCurrentVersionDate(Context context) throws IllegalStateException, IOException, JSONException, ParseException {
        int version = getCurrentVersion(context);
        if (version == CURRENT_VERSION_UNKNOWN)
            throw new IllegalStateException("Current version unknown.");

        JSONObject versionList = getChanceLog(context);
        String s = versionList.getJSONObject(String.valueOf(version)).getString("date");
        SimpleDateFormat sdf = new SimpleDateFormat(VERSION_RELEASE_DATE_PATTERN);
        Date d = sdf.parse(s);

        Timber.i("Formatting the update counter string. Read JSON date: " + s + ". Expected pattern: " + VERSION_RELEASE_DATE_PATTERN + " -> " + d.getTime());

        return d;
    }


}

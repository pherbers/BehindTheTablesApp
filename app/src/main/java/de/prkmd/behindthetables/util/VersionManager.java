package de.prkmd.behindthetables.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import de.prkmd.behindthetables.R;
import de.prkmd.behindthetables.activity.CategorySelectActivity;
import timber.log.Timber;

/**
 * Created by Nils on 24.03.2017.
 */

public class VersionManager {

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

}

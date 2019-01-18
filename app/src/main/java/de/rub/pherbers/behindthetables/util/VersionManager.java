package de.rub.pherbers.behindthetables.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

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
                Timber.w("Unknown version change!");
                break;
        }
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

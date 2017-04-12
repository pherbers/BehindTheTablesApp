package de.rub.pherbers.behindthetables;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.amitshekhar.DebugDB;

import java.util.HashSet;

import de.rub.pherbers.behindthetables.data.TableFile;
import de.rub.pherbers.behindthetables.sql.DBAdapter;
import de.rub.pherbers.behindthetables.util.VersionManager;
import timber.log.Timber;

/**
 * Created by Patrick on 12.03.2017.
 */

public class BehindTheTables extends Application {

    public static final String APP_TAG = "de.rub.btt.";
    public static final String PREFS_TAG = APP_TAG + "prefs_";
    public static final String PREFS_LAST_KNOWN_VERSION = PREFS_TAG + "last_known_version";

    @Override
    public void onCreate() {
        super.onCreate();
        if (isDebugBuild()) {
            Timber.plant(new DebugTree());
            Timber.i("Debug-DB URL: " + DebugDB.getAddressLog());
        } else {
            Timber.plant(new ReleaseTree());
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int lastVer = prefs.getInt(PREFS_LAST_KNOWN_VERSION, 0);
        int currentVer = 0;
        try {
            currentVer = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (lastVer != 0 && lastVer != currentVer) {
            VersionManager.onVersionChange(this, lastVer, currentVer);
        }

        prefs.edit().putInt(PREFS_LAST_KNOWN_VERSION, currentVer).apply();
        Timber.i("Application started. App version: " + currentVer);

        //Warming up the DB for future use!
        new DBAdapter(this).open().close();
    }

    public static void clearFavs(Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putStringSet(TableFile.PREFS_FAVORITE_TABLES, new HashSet<String>());
        editor.apply();
    }

    public static boolean isDebugBuild() {
        return BuildConfig.DEBUG;
    }

    private class DebugTree extends Timber.DebugTree {
        @Override
        protected String createStackElementTag(StackTraceElement element) {
            return APP_TAG + super.createStackElementTag(element) + ":" + element.getLineNumber();
        }
    }

    private class ReleaseTree extends DebugTree {
        @Override
        protected boolean isLoggable(String tag, int priority) {
            return !(priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO);
        }
    }
}

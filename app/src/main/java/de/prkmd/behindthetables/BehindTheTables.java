package de.prkmd.behindthetables;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.HashSet;

import de.prkmd.behindthetables.data.TableFile;
import timber.log.Timber;

/**
 * Created by Patrick on 12.03.2017.
 */

public class BehindTheTables extends Application {

    public static final String APP_TAG = "de.prkmd.behindthetables.";
    public static final String PREFS_TAG = APP_TAG + "prefs_";
    public static final String PREFS_LAST_KNOWN_VERSION = PREFS_TAG + "last_known_version";

    public static void clearFavs(Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putStringSet(TableFile.PREFS_FAVORITE_TABLES, new HashSet<String>());
        editor.apply();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (isDebugBuild()) {
            Timber.plant(new DebugTree());
            //Timber.i("Debug-DB URL: " + DebugDB.getAddressLog());
        } else {
            Timber.plant(new ReleaseTree());
        }
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

    public static Intent buildAppMarketIntent(Context context){
        String packageName = context.getPackageName();
        Uri uri = Uri.parse("market://details?id=" + packageName);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        }
        return intent;
    }
}

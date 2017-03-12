package de.rub.pherbers.behindthetables;

import android.app.Application;
import android.util.Log;

import java.util.Locale;

import timber.log.Timber;

/**
 * Created by Patrick on 12.03.2017.
 */

public class BehindTheTables extends Application {
    public static final String LOGTAG = "de.rub.btt.";
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new DebugTree());
        } else {
            Timber.plant(new ReleaseTree());
        }
        Timber.i("Application started via TIMBER!");

    }

    private class DebugTree extends Timber.DebugTree{
        @Override
        protected String createStackElementTag(StackTraceElement element) {
            return LOGTAG + super.createStackElementTag(element) + ":" + element.getLineNumber();
        }
    }

    private class ReleaseTree extends DebugTree {

        @Override
        protected boolean isLoggable(String tag, int priority) {
            return !(priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO);
        }
    }
}

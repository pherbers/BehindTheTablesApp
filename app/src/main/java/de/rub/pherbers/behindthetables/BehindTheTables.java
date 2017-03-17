package de.rub.pherbers.behindthetables;

import android.Manifest;
import android.app.Application;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import de.rub.pherbers.behindthetables.sql.DBAdapter;
import timber.log.Timber;

/**
 * Created by Patrick on 12.03.2017.
 */

public class BehindTheTables extends Application {
	public static final String APP_TAG = "de.rub.btt.";

	@Override
	public void onCreate() {
		super.onCreate();
		if (isDebugBuild()) {
			Timber.plant(new DebugTree());
		} else {
			Timber.plant(new ReleaseTree());
		}
		Timber.i("Application started.");

		//Warming up the DB for future use!
		new DBAdapter(this).open().close();
	}

	public static boolean isDebugBuild(){
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

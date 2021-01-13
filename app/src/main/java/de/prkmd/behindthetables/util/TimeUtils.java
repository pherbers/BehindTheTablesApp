package de.prkmd.behindthetables.util;

/*
  Util function created by Nils
  A derivative of class: https://github.com/NilsFo/LockScreenNotes/blob/master/app/src/main/java/de/nilsfo/lockscreennotes/util/TimeUtils.java
 */

import android.content.Context;
import android.text.format.DateUtils;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import de.prkmd.behindthetables.R;

/**
 * Util function.
 *
 * @author Nils
 * @link github.com/NilsFo/LockScreenNotes/blob/master/app/src/main/java/de/nilsfo/lockscreennotes/util/TimeUtils.java
 */
public class TimeUtils {

	private static final int LEVEL_OF_DETAIL_SHORT = DateFormat.SHORT;
	private static final int LEVEL_OF_DETAIL_MEDIUM = DateFormat.MEDIUM;
	private static final int LEVEL_OF_DETAIL_LONG = DateFormat.LONG;
	private static final int LEVEL_OF_DETAIL_FULL = DateFormat.FULL;

	private static final int LEVEL_OF_DETAIL_DEFAULT_DATE = LEVEL_OF_DETAIL_MEDIUM;
	private static final int LEVEL_OF_DETAIL_DEFAULT_TIME = LEVEL_OF_DETAIL_LONG;
	private Context context;

	public TimeUtils(Context context) {
		this.context = context;
	}

	public String formatRelative(Date date) {
		return formatRelative(date.getTime());
	}

	public String formatRelative(long timestamp) {
		return DateUtils.getRelativeTimeSpanString(timestamp, new Date().getTime(), 0L, DateUtils.FORMAT_ABBREV_ALL).toString();
	}

	public String formatAbsolute(long time) {
		return context.getString(R.string.util_concat_dash, formatDateAbsolute(time), formatTimeAbsolute(time));
	}

	public String formatAbsolute(Date time, int levelOfDetailTime, int levelOfDetailDate) {
		return context.getString(R.string.util_concat_dash, formatDateAbsolute(time, levelOfDetailTime), formatTimeAbsolute(time, levelOfDetailDate));
	}

	public String formatAbsolute(Date date) {
		return formatAbsolute(date.getTime());
	}

	public String formatDateAbsolute(long time) {
		return formatDateAbsolute(new Date(time));
	}

	public String formatTimeAbsolute(long time) {
		return formatTimeAbsolute(new Date(time));
	}

	public String formatTimeAbsolute(Date date) {
		return formatTimeAbsolute(date, LEVEL_OF_DETAIL_DEFAULT_TIME);
	}

	public String formatDateAbsolute(Date date) {
		return formatDateAbsolute(date, LEVEL_OF_DETAIL_DEFAULT_DATE);
	}

	public String formatTimeAbsolute(Date date, int levelOfDetail) {
		return DateFormat.getTimeInstance(levelOfDetail, getLocale()).format(date);
	}

	public String formatDateAbsolute(Date date, int levelOfDetail) {
		return DateFormat.getDateInstance(levelOfDetail, getLocale()).format(date);
	}

	public String formatDateAbsolute(long date, int levelOfDetailTime, int levelOfDetailDate) {
		return formatDateAbsolute(new Date(date), levelOfDetailTime, levelOfDetailDate);
	}

	public String formatDateAbsolute(Date date, int levelOfDetailTime, int levelOfDetailDate) {
		return formatTimeAbsolute(date, levelOfDetailTime) + " " + formatDateAbsolute(date, levelOfDetailDate);
	}

	private Locale getLocale() {
		return context.getResources().getConfiguration().locale;
	}

}

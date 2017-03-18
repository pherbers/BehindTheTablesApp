package de.rub.pherbers.behindthetables.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Observable;

import timber.log.Timber;

import static de.rub.pherbers.behindthetables.BehindTheTables.APP_TAG;

/**
 * Created by Nils on 14.03.2017.
 */

public class DBAdapter extends Observable {

	// DB Fields
	public static final String KEY_ROWID = "_id";
	public static final int COL_ROWID = 0;

	// Entry col names
	public static final String KEY_TABLE_COLLECTION_PATH = "filepath";
	public static final String KEY_TABLE_COLLECTION_FAV = "fav";
	public static final String KEY_TABLE_COLLECTION_TITLE = "title";
	public static final String KEY_TABLE_COLLECTION_SEARCH_TAGS = "search_tags";

	// field numbers (0 is reserved for ID!)
	public static final int COL_TABLE_COLLECTION_PATH = 1;
	public static final int COL_TABLE_COLLECTION_FAV = 2;
	public static final int COL_TABLE_COLLECTION_TITLE = 3;
	public static final int COL_TABLE_COLLECTION_SEARCH_TAGS = 4;

	public static final String[] ALL_KEYS = new String[]{KEY_ROWID, KEY_TABLE_COLLECTION_PATH, KEY_TABLE_COLLECTION_FAV, KEY_TABLE_COLLECTION_TITLE, KEY_TABLE_COLLECTION_SEARCH_TAGS};

	public static final String DATABASE_NAME = APP_TAG + "database";
	public static final String DATABASE_TABLE = "TableCollectionIndex";

	// Tracking DB version if a new version of the app changes the format.
	public static final int DATABASE_VERSION = 1;

	private static final String DATABASE_CREATE_SQL =
			"create table " + DATABASE_TABLE
					+ " (" + KEY_ROWID + " integer primary key, "
					// + KEY_{...} + " {type} not null"
					//	- Key is the column name you created above.
					//	- {type} is one of: text, integer, real, blob
					//		(http://www.sqlite.org/datatype3.html)
					//  - "not null" means it is a required field (must be given a value).
					// NOTE: All must be comma separated (end of line!) Last one must have NO comma!!
					+ KEY_TABLE_COLLECTION_PATH + " text not null, "
					+ KEY_TABLE_COLLECTION_FAV + " integer not null, "
					+ KEY_TABLE_COLLECTION_SEARCH_TAGS + " text not null, "
					+ KEY_TABLE_COLLECTION_TITLE + " text not null"
					// Rest  of creation:
					+ ");";

	private DatabaseHelper myDBHelper;
	private SQLiteDatabase db;

	public DBAdapter(Context context) {
		myDBHelper = new DatabaseHelper(context);
	}

	// Open the database connection.
	public DBAdapter open() {
		db = myDBHelper.getWritableDatabase();
		return this;
	}

	// Close the database connection.
	public void close() {
		myDBHelper.close();
	}

	public boolean deleteRow(long rowId) {
		String where = KEY_ROWID + "=" + rowId;
		boolean b = db.delete(DATABASE_TABLE, where, null) != 0;

		setChanged();
		notifyObservers();

		return b;
	}

	public void deleteAll() {
		Cursor c = getAllRows();
		long rowId = c.getColumnIndexOrThrow(KEY_ROWID);
		if (c.moveToFirst()) {
			do {
				deleteRow(c.getLong((int) rowId));
			} while (c.moveToNext());
		}
		c.close();

		setChanged();
		notifyObservers();
	}

	public synchronized Cursor getAllRows() {
		String where = null;
		Cursor c = db.query(true, DATABASE_TABLE, ALL_KEYS,
				where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	// Get a specific row (by rowId)
	public Cursor getRow(long rowId) {
		String where = KEY_ROWID + "=" + rowId;
		Cursor c = db.query(true, DATABASE_TABLE, ALL_KEYS,
				where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	public Cursor getViaFileIdentifier(String identifier) {
		String where = KEY_TABLE_COLLECTION_PATH + "=" + identifier;
		Cursor c = db.query(true, DATABASE_TABLE, ALL_KEYS,
				where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	// Add a new set of values to the database.
	public long insertRow(String title, String path, String tags, int fav) {
		// Create row's data:
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_TABLE_COLLECTION_FAV, fav);
		initialValues.put(KEY_TABLE_COLLECTION_PATH, path);
		initialValues.put(KEY_TABLE_COLLECTION_SEARCH_TAGS, tags);
		initialValues.put(KEY_TABLE_COLLECTION_TITLE, title);
		// Insert it into the database.

		long l = db.insert(DATABASE_TABLE, null, initialValues);

		setChanged();
		notifyObservers();

		return l;
	}

	// Change an existing row to be equal to new data.
	public int updateRow(int rowId, String title, String path, String tags, int fav) {
		String where = KEY_ROWID + "=" + rowId;

		/*
		 * CHANGE 4:
		 */
		// TODO: Update data in the row with new fields.
		// TODO: Also change the function's arguments to be what you need!
		// Create row's data:
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_TABLE_COLLECTION_FAV, fav);
		newValues.put(KEY_TABLE_COLLECTION_PATH, path);
		newValues.put(KEY_TABLE_COLLECTION_SEARCH_TAGS, tags);
		newValues.put(KEY_TABLE_COLLECTION_TITLE, title);

		// Insert it into the database.
		int rows = db.update(DATABASE_TABLE, newValues, where, null);

		setChanged();
		notifyObservers();

		return rows;
	}

	/**
	 * Private class which handles database creation and upgrading.
	 * Used to handle low-level database access.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase _db) {
			_db.execSQL(DATABASE_CREATE_SQL);
			Timber.i("Set up a new database without problems! Database ID: "+DATABASE_NAME);

			fillWithDefaultData();
		}

		private void fillWithDefaultData() {
			Timber.w("TODO: Fill the database with default data now!");
			//TODO fill db
		}

		@Override
		public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
			Timber.i("Upgrading application's database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data!");

			// Destroy old database:
			_db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);

			// Recreate new database:
			onCreate(_db);
		}
	}

}
package de.prkmd.behindthetables.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.Date;
import java.util.Observable;

import de.prkmd.behindthetables.R;
import timber.log.Timber;

import static de.prkmd.behindthetables.BehindTheTables.APP_TAG;

/**
 * Created by Nils on 14.03.2017.
 */

public class DBAdapter extends Observable {

	public static final long CATEGORY_NOT_FOUND = -1;
	public static final char LINK_COLLECTION_SEPARATOR = ';';

	// DB Fields
	public static final String KEY_CATEGORY_ROWID = "_id";
	public static final int COL_CATEGORY_ROWID = 0;

	// Entry col names
	public static final String KEY_TABLE_COLLECTION_RESOURCE_LOCATION = "resource_location";
	public static final String KEY_TABLE_COLLECTION_TITLE = "title";
	public static final String KEY_TABLE_COLLECTION_KEYWORDS = "keywords";
	public static final String KEY_TABLE_COLLECTION_USE_WITH = "use_with";
	public static final String KEY_TABLE_COLLECTION_RELATED_TABLES = "related_tables";
	public static final String KEY_TABLE_COLLECTION_DESCRIPTION = "description";
	public static final String KEY_TABLE_COLLECTION_CATEGORY_ID = "category_id";

	public static final String KEY_CATEGORY_TITLE = "title";

	// field numbers (0 is reserved for ID!)
	public static final int COL_TABLE_COLLECTION_LOCATION = 0;
	public static final int COL_TABLE_COLLECTION_TITLE = 1;
	public static final int COL_TABLE_COLLECTION_KEYWORDS = 2;
	public static final int COL_TABLE_COLLECTION_USE_WITH = 3;
	public static final int COL_TABLE_COLLECTION_RELATED_TABLES = 4;
	public static final int COL_TABLE_COLLECTION_DESCRIPTION = 5;
	public static final int COL_TABLE_COLLECTION_CATEGORY_ID = 6;

	public static final int COL_CATEGORY_TITLE = 1;

	public static final String[] ALL_KEYS_TABLE_COLLECTION = new String[]{KEY_TABLE_COLLECTION_RESOURCE_LOCATION, KEY_TABLE_COLLECTION_TITLE, KEY_TABLE_COLLECTION_KEYWORDS, KEY_TABLE_COLLECTION_USE_WITH, KEY_TABLE_COLLECTION_RELATED_TABLES, KEY_TABLE_COLLECTION_DESCRIPTION};
	public static final String[] ALL_KEYS_CATEGORY = new String[]{KEY_CATEGORY_ROWID, KEY_CATEGORY_TITLE};


	public static final String DATABASE_NAME = APP_TAG + "database";
	public static final String DATABASE_TABLE_TABLE_COLLECTION = "TableCollectionIndex";
	public static final String DATABASE_TABLE_CATEGORY = "Category";

	// Tracking DB version if a new version of the app changes the format.
	public static final int DATABASE_VERSION = 4;

	private static final String DATABASE_TABLE_COLLECTION_CREATE_SQL =
			"create table " + DATABASE_TABLE_TABLE_COLLECTION
					+ " (" + KEY_TABLE_COLLECTION_RESOURCE_LOCATION + " text primary key, "
					// + KEY_{...} + " {type} not null"
					//	- Key is the column name you created above.
					//	- {type} is one of: text, integer, real, blob
					//		(http://www.sqlite.org/datatype3.html)
					//  - "not null" means it is a required field (must be given a value).
					// NOTE: All must be comma separated (end of line!) Last one must have NO comma!!
					+ KEY_TABLE_COLLECTION_TITLE + " text not null, "
					+ KEY_TABLE_COLLECTION_KEYWORDS + " text not null, "
					+ KEY_TABLE_COLLECTION_USE_WITH + " integer not null, "
					+ KEY_TABLE_COLLECTION_RELATED_TABLES + " text not null, "
					+ KEY_TABLE_COLLECTION_DESCRIPTION + " text not null, "
					+ KEY_TABLE_COLLECTION_CATEGORY_ID + " integer not null "
					// Rest  of creation:
					+ ");";

	private static final String DATABASE_CATEGORY_CREATE_SQL =
			"create table " + DATABASE_TABLE_CATEGORY + " (" + KEY_CATEGORY_ROWID + " integer primary key, "
					+ KEY_CATEGORY_TITLE + " text not null);";

	private DatabaseHelper myDBHelper;
	private SQLiteDatabase db;

	public DBAdapter(Context context) {
		myDBHelper = new DatabaseHelper(context);

		if (myDBHelper.isSetupRequired()) {
			open();
			fillWithDefaultData(context);
			close();
		}
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

	public boolean deleteTableCollection(String resourceLocation) {
		String where = KEY_TABLE_COLLECTION_RESOURCE_LOCATION + "='" + resourceLocation + "'";
		boolean b = db.delete(DATABASE_TABLE_TABLE_COLLECTION, where, null) != 0;

		setChanged();
		notifyObservers();

		return b;
	}

	public void deleteAll(String tableName) {
		//Cursor c = getAllRows();
		//long rowId = c.getColumnIndexOrThrow(KEY_ROWID);
		//if (c.moveToFirst()) {
		//    do {
		//        deleteRow(c.getLong((int) rowId));
		//    } while (c.moveToNext());
		//}
		//c.close();

		db.delete(tableName, null, null);
		Timber.w("All SQL Entries have been deleted.");

		setChanged();
		notifyObservers();
	}

	public synchronized Cursor getAllTableCollections(long categoryID) {
		String where = DATABASE_TABLE_TABLE_COLLECTION + "." + KEY_TABLE_COLLECTION_CATEGORY_ID + "=" + categoryID;
		Cursor c = db.query(true, DATABASE_TABLE_TABLE_COLLECTION, ALL_KEYS_TABLE_COLLECTION, where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	public synchronized Cursor getAllCategories(String orderBy) {
		Cursor c = db.query(true, DATABASE_TABLE_CATEGORY, ALL_KEYS_CATEGORY, null, null, null, null, orderBy, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	public void fillWithDefaultData(Context context) {
		deleteAll(DATABASE_TABLE_TABLE_COLLECTION);
		deleteAll(DATABASE_TABLE_CATEGORY);

		Timber.w("The database was newly set up and is filled with default data now!");
		long timestamp = new Date().getTime();

		if (!DefaultTables.discoverDefaultTables(context, this)) {
			Timber.e("Failed to discover default internal data!");
			Toast.makeText(context, R.string.error_internal_json_exception, Toast.LENGTH_LONG).show();
		}
	}

	public boolean existsTableCollection(String location) {
		Cursor c = getTableCollection(location);
		return c != null && c.moveToFirst();
	}

	public long existsCategory(String title) {
		String where = KEY_CATEGORY_TITLE + "='" + title + "'";
		long ret = CATEGORY_NOT_FOUND;

		Cursor c = db.query(true, DATABASE_TABLE_CATEGORY, ALL_KEYS_CATEGORY, where, null, null, null, null, null);
		if (c.moveToFirst()) {
			ret = c.getLong(COL_CATEGORY_ROWID);
		}

		c.close();
		return ret;
	}

	// Get a specific row (by rowId)
	public Cursor getTableCollection(String resourceLocation) {
		String where = KEY_TABLE_COLLECTION_RESOURCE_LOCATION + "='" + resourceLocation + "'";
		Cursor c = db.query(true, DATABASE_TABLE_TABLE_COLLECTION, null,
				where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	public Cursor getCategory(long id) {
		String where = KEY_CATEGORY_ROWID + "=" + id;
		Cursor c = db.query(true, DATABASE_TABLE_CATEGORY, null,
				where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	public void insertOrUpdateTableCollection(String resourceLocation, String title, String description, String keywords, String useWith, String relatedTables, long categoryID) {
		if (existsTableCollection(resourceLocation)){
			updateTableCollection(resourceLocation,title,description,keywords,useWith,relatedTables,categoryID);
		}else{
			insertTableCollection(resourceLocation,title,description,keywords,useWith,relatedTables,categoryID);
		}
	}

	public long insertTableCollection(String resourceLocation, String title, String description, String keywords, String useWith, String relatedTables, long categoryID) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_TABLE_COLLECTION_RESOURCE_LOCATION, resourceLocation);
		initialValues.put(KEY_TABLE_COLLECTION_TITLE, title);
		initialValues.put(KEY_TABLE_COLLECTION_DESCRIPTION, description);
		initialValues.put(KEY_TABLE_COLLECTION_KEYWORDS, keywords);
		initialValues.put(KEY_TABLE_COLLECTION_USE_WITH, useWith);
		initialValues.put(KEY_TABLE_COLLECTION_RELATED_TABLES, relatedTables);
		initialValues.put(KEY_TABLE_COLLECTION_CATEGORY_ID, categoryID);

		long l = db.insert(DATABASE_TABLE_TABLE_COLLECTION, null, initialValues);

		setChanged();
		notifyObservers();

		return l;
	}

	public long insertCategory(String title) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_CATEGORY_TITLE, title);

		long l = db.insert(DATABASE_TABLE_CATEGORY, null, initialValues);

		return l;
	}

	// Change an existing row to be equal to new data.
	public int updateTableCollection(String resourceLocation, String title, String description, String keywords, String useWith, String relatedTables, long categoryID) {
		String where = KEY_TABLE_COLLECTION_RESOURCE_LOCATION + "='" + resourceLocation + "'";

		ContentValues newValues = new ContentValues();
		newValues.put(KEY_TABLE_COLLECTION_TITLE, title);
		newValues.put(KEY_TABLE_COLLECTION_DESCRIPTION, description);
		newValues.put(KEY_TABLE_COLLECTION_KEYWORDS, keywords);
		newValues.put(KEY_TABLE_COLLECTION_USE_WITH, useWith);
		newValues.put(KEY_TABLE_COLLECTION_RELATED_TABLES, relatedTables);
		newValues.put(KEY_TABLE_COLLECTION_TITLE, title);
		newValues.put(KEY_TABLE_COLLECTION_CATEGORY_ID, categoryID);

		// Insert it into the database.
		int rows = db.update(DATABASE_TABLE_TABLE_COLLECTION, newValues, where, null);

		setChanged();
		notifyObservers();

		return rows;
	}

	public int updateTableCategory(long id, String title) {
		String where = ALL_KEYS_CATEGORY + "=" + id;

		ContentValues newValues = new ContentValues();
		newValues.put(KEY_CATEGORY_TITLE, title);

		// Insert it into the database.
		int rows = db.update(DATABASE_TABLE_CATEGORY, newValues, where, null);

		return rows;
	}

	public synchronized Cursor getAllTableCollections() {
		String where = null;
		Cursor c = db.query(true, DATABASE_TABLE_TABLE_COLLECTION, ALL_KEYS_TABLE_COLLECTION, where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	public synchronized Cursor getAllCustomTableCollections() {
		String where = "resource_location LIKE '%.json'";
		Cursor c = db.query(true, DATABASE_TABLE_TABLE_COLLECTION, ALL_KEYS_TABLE_COLLECTION, where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	public synchronized Cursor getAllCategories() {
		return getAllCategories(null);
	}

	/**
	 * Private class which handles database creation and upgrading.
	 * Used to handle low-level database access.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		private boolean setupRequired;

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase _db) {
			_db.execSQL(DATABASE_TABLE_COLLECTION_CREATE_SQL);
			_db.execSQL(DATABASE_CATEGORY_CREATE_SQL);
			Timber.i("Set up a new database without problems! Database ID: " + DATABASE_NAME);

			setupRequired = true;
		}

		@Override
		public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
			Timber.i("Upgrading application's database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data!");

			// Destroy old database:
			_db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_CATEGORY);
			_db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_TABLE_COLLECTION);

			// Recreate new database:
			onCreate(_db);
		}

		public boolean isSetupRequired() {
			return setupRequired;
		}
	}

}

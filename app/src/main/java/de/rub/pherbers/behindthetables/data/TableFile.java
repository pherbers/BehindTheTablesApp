package de.rub.pherbers.behindthetables.data;

import android.database.Cursor;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.Date;

import de.rub.pherbers.behindthetables.sql.DBAdapter;
import timber.log.Timber;

/**
 * Created by Nils on 14.03.2017.
 */

public class TableFile implements Comparable<TableFile> {

	private int resource;
	private File file;
	private long databaseID;
	private String title;
	private String tags;
	private boolean fav;

	public TableFile(int resource) {
		this.resource = resource;

		Timber.v("Created a new internal TableFile. Resource ID: " + resource);
	}

	public TableFile(File file) {
		this.file = file;

		Timber.v("Created a new external TableFile. Resource path: " + file.getAbsolutePath());
	}

	public static TableFile getFromDB(long id, DBAdapter adapter) {
		Cursor cursor = adapter.getRow(id);
		if (cursor == null) {
			return null;
		}

		TableFile file = null;
		String path = cursor.getString(DBAdapter.COL_TABLE_COLLECTION_PATH);
		if (isNumeric(path)) {
			file = new TableFile(Integer.parseInt(path));
		} else {
			file = new TableFile(new File(path));
		}

		String title = cursor.getString(DBAdapter.COL_TABLE_COLLECTION_TITLE);
		boolean fav = cursor.getInt(DBAdapter.COL_TABLE_COLLECTION_FAV) != 0;
		String tags = cursor.getString(DBAdapter.COL_TABLE_COLLECTION_SEARCH_TAGS);

		file.setTitle(title);
		file.setFav(fav);
		file.setTags(tags);
		file.setDatabaseID(id);

		return file;
	}

	private static boolean isNumeric(String value) {
		try {
			Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public String getIdentifier() {
		if (isExternal()) return file.getAbsolutePath();
		return String.valueOf(resource);
	}

	@Override
	public String toString() {
		return getIdentifier();
	}

	public boolean isExternal() {
		return getFile() != null;
	}

	public int getResourceID() {
		return resource;
	}

	public File getFile() {
		return file;
	}

	public Date getLastModified() {
		if (!isExternal()) return new Date();
		return new Date(getFile().lastModified());
	}

	public boolean isFav() {
		return fav;
	}

	public void setFav(boolean fav) {
		this.fav = fav;
	}

	public long getDatabaseID() {
		return databaseID;
	}

	private void setDatabaseID(long databaseID) {
		this.databaseID = databaseID;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public boolean equals(TableFile file) {
		return getIdentifier().equals(file.getIdentifier());
	}

	@Override
	public int compareTo(@NonNull TableFile file) {
		if (isExternal() == file.isExternal()) {
			if (isExternal()) {
				return getLastModified().compareTo(file.getLastModified());
			} else {
				return getResourceID() - file.getResourceID();
			}
		} else if (isExternal()) return -1;
		return 1;
	}


}

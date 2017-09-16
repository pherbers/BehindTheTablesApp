package de.rub.pherbers.behindthetables.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.ArraySet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import de.rub.pherbers.behindthetables.data.io.FileManager;
import de.rub.pherbers.behindthetables.sql.DBAdapter;
import timber.log.Timber;

import static de.rub.pherbers.behindthetables.BehindTheTables.APP_TAG;
import static de.rub.pherbers.behindthetables.BehindTheTables.PREFS_TAG;

/**
 * Created by Nils on 14.03.2017.
 */

public class TableFile implements Comparable<TableFile> {

    public static final String PREFS_FAVORITE_TABLES = PREFS_TAG + "fav_tables";

    private String keywords;

    private String title;
    private String description;
    private String resourceLocation;

    private TableFile() {
    }

    public static TableFile createEmpty() {
        return new TableFile();
    }

    public static TableFile createFromDB(String resourceLocation, DBAdapter adapter) {
        TableFile file = createEmpty();

        Cursor c = adapter.getTableCollection(resourceLocation);
        if (!c.moveToFirst()) {
            throw new IllegalArgumentException("Table entry '" + resourceLocation + "' does not exist in the DB!");
        }

        file.setTitle(c.getString(DBAdapter.COL_TABLE_COLLECTION_TITLE));
        file.setDescription(c.getString(DBAdapter.COL_TABLE_COLLECTION_DESCRIPTION));
        file.setResourceLocation(resourceLocation);

        file.setKeywords(c.getString(DBAdapter.COL_TABLE_COLLECTION_KEYWORDS));

        return file;
    }

    public static TableFile createFromDB(Cursor cursor) {
        TableFile file = createEmpty();

        file.setTitle(cursor.getString(DBAdapter.COL_TABLE_COLLECTION_TITLE));
        file.setDescription(cursor.getString(DBAdapter.COL_TABLE_COLLECTION_DESCRIPTION));
        file.setResourceLocation(cursor.getString(DBAdapter.COL_TABLE_COLLECTION_LOCATION));

        file.setKeywords(cursor.getString(DBAdapter.COL_TABLE_COLLECTION_KEYWORDS));

        return file;
    }

    @Deprecated
    public void saveToDB(DBAdapter adapter) {
        //if (isRegisteredInDB()) {
        //    adapter.updateRow(getDatabaseID(), getTitle(), getIdentifier(), getTags(), isFavNumeric());
        //} else {
        //    long id = adapter.insertRow(getTitle(), getIdentifier(), getTags(), isFavNumeric());
        //    setDatabaseID(id);
        //}
    }

    public int getResourceID(Context context) {
        if (isExternal()) {
            return -1;
        } else {
            return context.getResources().getIdentifier(getResourceLocation(), "raw", context.getPackageName());
        }
    }

    @Override
    public String toString() {
        return getTitle();
    }

    public boolean isExternal() {
        return !getResourceLocation().startsWith("table_");
    }

    public File getFile() {
        return new File(getResourceLocation());
    }

    public Date getLastModified() {
        if (!isExternal()) return new Date();
        return new Date(getFile().lastModified());
    }

    public boolean isFavorite(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> favs = new HashSet<String>(preferences.getStringSet(PREFS_FAVORITE_TABLES, new HashSet<String>()));

        for (String s : favs) {
            if (s.equals(getResourceLocation())) return true;
        }
        return false;
    }

    public String getShortExternalPath(Context context){
        if (!isExternal()) return null;

        String basePath = new FileManager(context).getExternalTableDir().getAbsolutePath();
        return getResourceLocation().substring(basePath.length());
    }

    public void setFavorite(Context context, boolean favorite) {
        Timber.i("Request to change fav of '" + getTitle() + "' from " + isFavorite(context) + " to " + favorite);

        if (favorite && isFavorite(context)) {
            Timber.w("Table '" + getTitle() + "' is already in the favorites. Nothing to be changed.");
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> favs = new HashSet<String>(prefs.getStringSet(PREFS_FAVORITE_TABLES, new HashSet<String>()));
        SharedPreferences.Editor editor = prefs.edit();

        if (favorite) {
            favs.add(getResourceLocation());
        } else {
            favs.remove(getResourceLocation());
        }
        editor.putStringSet(PREFS_FAVORITE_TABLES, favs);
        editor.apply();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResourceLocation() {
        return resourceLocation;
    }

    public boolean hasKeyword(String candidate) {
        return getKeywords().toLowerCase().trim().contains(candidate.toLowerCase().trim());
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public void setResourceLocation(String resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public boolean equals(TableFile file) {
        return getResourceLocation().equals(file.getResourceLocation());
    }

    @Override
    public int compareTo(@NonNull TableFile file) {
        //if (isExternal() == file.isExternal()) {
        //    if (isExternal()) {
        //        return getLastModified().compareTo(file.getLastModified());
        //    } else {
                return getTitle().compareTo(file.getTitle());
        //    }
        //} else if (isExternal()) return -1;
        //return 1;
    }

}

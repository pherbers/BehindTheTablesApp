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

import de.rub.pherbers.behindthetables.sql.DBAdapter;
import timber.log.Timber;

import static de.rub.pherbers.behindthetables.BehindTheTables.APP_TAG;
import static de.rub.pherbers.behindthetables.BehindTheTables.PREFS_TAG;

/**
 * Created by Nils on 14.03.2017.
 */

public class TableFile implements Comparable<TableFile> {

    public static final String PREFS_FAVORITE_TABLES = PREFS_TAG + "fav_tables";

    private ArrayList<String> keywords;

    private String title;
    private String description;
    private String resourceLocation;

    private TableFile() {
        keywords = new ArrayList<>();
    }

    public static TableFile createEmpty() {
        return new TableFile();
    }

    public static TableFile createFromDB(String resourceLocation, DBAdapter adapter) {
        TableFile file = createEmpty();

        Cursor c = adapter.getRow(resourceLocation);
        if (!c.moveToFirst()) {
            throw new IllegalArgumentException("Table entry '" + resourceLocation + "' does not exist in the DB!");
        }

        file.setTitle(c.getString(DBAdapter.COL_TABLE_COLLECTION_TITLE));
        file.setDescription(c.getString(DBAdapter.COL_TABLE_COLLECTION_DESCRIPTION));
        file.setResourceLocation(resourceLocation);

        String keys = c.getString(DBAdapter.COL_TABLE_COLLECTION_KEYWORDS);
        for (String s : keys.split(String.valueOf(DBAdapter.LINK_COLLECTION_SEPARATOR))) {
            Timber.v("Added keyword '" + s + "' to " + file.getTitle());
            file.keywords.add(s.toLowerCase().trim());
        }

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
        boolean found = false;
        for (String s : getKeywords()) {
            found |= s.toLowerCase().trim().contains(candidate.toLowerCase().trim());
        }
        return found;
    }

    public ArrayList<String> getKeywords() {
        return keywords;
    }

    public void setResourceLocation(String resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public boolean equals(TableFile file) {
        return getResourceLocation().equals(file.getResourceLocation());
    }

    @Override
    public int compareTo(@NonNull TableFile file) {
        if (isExternal() == file.isExternal()) {
            if (isExternal()) {
                return getLastModified().compareTo(file.getLastModified());
            } else {
                return getTitle().compareTo(file.getTitle());
            }
        } else if (isExternal()) return -1;
        return 1;
    }


}

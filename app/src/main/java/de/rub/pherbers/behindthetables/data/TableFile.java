package de.rub.pherbers.behindthetables.data;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.Date;

import de.rub.pherbers.behindthetables.sql.DBAdapter;

/**
 * Created by Nils on 14.03.2017.
 */

public class TableFile implements Comparable<TableFile> {

    private String title;
    private String description;
    private String resourceLocation;
    private boolean fav;

    private TableFile() {
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

        return file;
    }

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

    public boolean isFav() {
        return fav;
    }

    public void setFav(boolean fav) {
        this.fav = fav;
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

package de.prkmd.behindthetables.sql;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;

import de.prkmd.behindthetables.R;
import timber.log.Timber;

import static de.prkmd.behindthetables.sql.DBAdapter.LINK_COLLECTION_SEPARATOR;

/**
 * Created by Nils on 27.03.2017.
 */

public abstract class DefaultTables {

    public static boolean discoverDefaultTables(Context context, DBAdapter adapter) {
        try {
            JSONObject meta = readJSONFile(new InputStreamReader(context.getResources().openRawResource(R.raw.tables_meta)));
            Iterator<String> keys = meta.keys();
            while (keys.hasNext()) {
                String raw_filename = keys.next();
                JSONObject tableInfo = meta.getJSONObject(raw_filename);
                String redditID = raw_filename.replace("table_", "");

                insertOrUpdateTable(raw_filename, redditID, tableInfo, adapter);
                //int id = context.getResources().getIdentifier(raw_filename, "raw", context.getPackageName());
                //Timber.i("Read through the meta.json. Filename: " + raw_filename + " -> '" + title + "'. Resource ID: " + id);
                //adapter.insertRow(title,String.valueOf(id),"",0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Timber.e(e, "Failed to fetch meta table information because of a JSON error!");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            Timber.e(e, "Failed to fetch meta table information because of an I/O error!");
            return false;
        }

        return true;
    }

    public static JSONObject readJSONFile(Reader resourceStream) throws JSONException, IOException {
        BufferedReader reader = new BufferedReader(resourceStream);
        StringBuilder out = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            out.append(line);
        }

        return new JSONObject(out.toString());
    }

    public static void insertOrUpdateTable(File file, DBAdapter adapter) throws IOException, JSONException {
        insertOrUpdateTable(file.getAbsolutePath(), "", readJSONFile(new FileReader(file)), adapter);
    }

    public static void insertOrUpdateTable(String resourceLocation, String redditID, JSONObject table, DBAdapter adapter) throws JSONException {
        String title = table.getString("title");
        String category = table.getString("category");

        String description = "";
        String keywords = "";
        String relatedTables = "";
        String useWith = "";

        if (table.has("description")) {
            description = table.getString("description");
        }
        if (table.has("keywords")) {
            keywords = extractJSONArray(table.getJSONArray("keywords"));
        }
        if (table.has("related_tables")) {
            relatedTables = extractJSONArray(table.getJSONArray("related_tables"));
        }
        if (table.has("use_with")) {
            useWith = extractJSONArray(table.getJSONArray("use_with"));
        }

        long categoryID = handleCategory(adapter, category);

        Timber.v(resourceLocation + " -> reddit ID: " + redditID + " title: " + title + ", keywords: " + keywords + ", catgetory: " + category + " (" + categoryID + ")!");
        adapter.insertOrUpdateTableCollection(resourceLocation, title, description, keywords, useWith, relatedTables, categoryID);
    }

    public static long handleCategory(DBAdapter adapter, String category) {
        long categoryRow = adapter.existsCategory(category);

        if (categoryRow == DBAdapter.CATEGORY_NOT_FOUND) {
            categoryRow = adapter.insertCategory(category);
        }

        return categoryRow;
    }

    private static String extractJSONArray(JSONArray array) throws JSONException {
        String ret = "";
        for (int i = 0; i < array.length(); i++) {
            String s = array.get(i).toString().trim();
            ret = ret + s + LINK_COLLECTION_SEPARATOR;
        }

        if (ret.endsWith(String.valueOf(LINK_COLLECTION_SEPARATOR))) {
            ret = ret.substring(0, ret.length() - 1);
        }
        return ret;
    }
}

package de.rub.pherbers.behindthetables.sql;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import de.rub.pherbers.behindthetables.R;
import timber.log.Timber;

import static de.rub.pherbers.behindthetables.sql.DBAdapter.LINK_COLLECTION_SEPARATOR;

/**
 * Created by Nils on 27.03.2017.
 */

public abstract class DefaultTables {

    public static boolean discoverDefaultTables(Context context, DBAdapter adapter) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.tables_meta)));
        StringBuilder out = new StringBuilder();
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Timber.e(e, "Failed to read the meta RAW file!");
            return false;
        }

        try {
            JSONObject meta = new JSONObject(out.toString());
            Iterator<String> keys = meta.keys();
            while (keys.hasNext()) {
                String raw_filename = keys.next();
                JSONObject tableInfo = meta.getJSONObject(raw_filename);

                String redditID = raw_filename.replace("table_", "");
                String description = tableInfo.getString("description");
                String title = tableInfo.getString("title");
                String category = tableInfo.getString("category");
                String keywords = extractJSONArray(tableInfo.getJSONArray("keywords"));
                String relatedTables = extractJSONArray(tableInfo.getJSONArray("related_tables"));
                String useWith = extractJSONArray(tableInfo.getJSONArray("use_with"));

                Timber.i(raw_filename + " -> reddit ID: " + redditID + " title: " + title + ", keywords: " + keywords + ", catgetory: " + category + "!");

                adapter.insertRow(raw_filename, title, description, keywords, useWith, relatedTables);
                //int id = context.getResources().getIdentifier(raw_filename, "raw", context.getPackageName());
                //Timber.i("Read through the meta.json. Filename: " + raw_filename + " -> '" + title + "'. Resource ID: " + id);
                //adapter.insertRow(title,String.valueOf(id),"",0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Timber.e(e, "Failed to fetch meta table information because of a JSON error!");
            return false;
        }

        return true;
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

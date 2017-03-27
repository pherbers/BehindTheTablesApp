package de.rub.pherbers.behindthetables.sql;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import de.rub.pherbers.behindthetables.R;
import timber.log.Timber;

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

        Timber.i("Read meta.json content: " + out.toString());
        try {
            JSONObject meta = new JSONObject(out.toString());
            Iterator<String> keys = meta.keys();
            while (keys.hasNext()) {
                String raw_filename = keys.next();
                String title = meta.getString(raw_filename);

                int id = context.getResources().getIdentifier(raw_filename, "raw", context.getPackageName());
                Timber.i("Read through the meta.json. Filename: " + raw_filename + " -> '" + title + "'. Resource ID: " + id);

                adapter.insertRow(title,String.valueOf(id),"",0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Timber.e(e, "Failed to fetch meta table information because of a JSON error!");
            return false;
        }

        return true;
    }
}

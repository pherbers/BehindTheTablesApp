package de.rub.pherbers.behindthetables.data;

import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Patrick on 11.03.2017.
 */

public class TableReader {

    private TableReader() { }

    public static TableCollection readTable(InputStream is) throws IOException {

        JsonReader jr = new JsonReader(new InputStreamReader(is, "UTF-8"));
        jr.beginObject();
        String title = "Default";
        List<RandomTable> tables = null;
        while(jr.hasNext()) {
            String name = jr.nextName();
            if (name.equals("title"))
                title = jr.nextString();
            else if (name.equals("tables"))
                tables = readTableListJson(jr);
        }
        jr.endObject();

        TableCollection tc = new TableCollection(title, tables);

        is.close();

        return tc;
    }

    private static List<RandomTable> readTableListJson(JsonReader jr) throws IOException {
        jr.beginArray();
        List<RandomTable> tables = new ArrayList<>();
        while(jr.hasNext()) {
            tables.add(readTableJson(jr));
        }
        jr.endArray();
        return tables;
    }

    private static RandomTable readTableJson(JsonReader jr) throws IOException {
        String title = "Table";
        String dice = "xdy";
        List<TableEntry> entries = new ArrayList<>();

        jr.beginObject();

        while(jr.hasNext()) {
            String name = jr.nextName();
            if (name.equals("name"))
                title = jr.nextString();
            else if (name.equals("dice"))
                dice = jr.nextString();
            else if (name.equals("table_entries")) {
                int i = 1;
                jr.beginArray();
                while (jr.hasNext()) {
                    entries.add(new TableEntry(jr.nextString(), i));
                    i++;
                }
                jr.endArray();
            }
        }
        jr.endObject();
        RandomTable table = new RandomTable(title, dice, entries);
        return table;
    }
}

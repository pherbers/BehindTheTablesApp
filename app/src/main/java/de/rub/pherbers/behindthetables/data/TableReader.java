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

public abstract class TableReader {

    public static TableCollection readTable(InputStream is) throws IOException {

        JsonReader jr = new JsonReader(new InputStreamReader(is, "UTF-8"));
        jr.beginObject();

        String title = "Default";
        List<RandomTable> tables = null;
        String reference = "";
        String description = "";
        String id = "";
        String category = "No Category";
        List<TableLink> related_tables = null;
        List<TableLink> use_with_tables = null;
        List<String> keywords = null;

        while (jr.hasNext()) {
            String name = jr.nextName();
            if (name.equals("title"))
                title = jr.nextString();
            else if (name.equals("category"))
                category = jr.nextString();
            else if (name.equals("tables"))
                tables = readTableListJson(jr);
            else if (name.equals("description")) {
                description = jr.nextString();
            } else if (name.equals("id")) {
                id = jr.nextString();
            } else if (name.equals("keywords")) {
                jr.beginArray();
                keywords = new ArrayList<>();
                while (jr.hasNext())
                    keywords.add(jr.nextString());
                jr.endArray();
            } else if (name.equals("reference")) {
                reference = jr.nextString();
            } else if (name.equals("related_tables")) {
                related_tables = readLinks(jr);
            } else if (name.equals("use_with")) {
                use_with_tables = readLinks(jr);
            }
        }
        jr.endObject();

        TableCollection tc = new TableCollection(title, tables, reference, description, category, id, related_tables, use_with_tables, keywords);

        is.close();

        return tc;
    }

    private static List<TableLink> readLinks(JsonReader jr) throws IOException {
        List<TableLink> tableLinks = new ArrayList<>();
        jr.beginArray();
        while (jr.hasNext()) {
            jr.beginObject();
            String link = "";
            String title = "";
            while (jr.hasNext()) {
                String name = jr.nextName();
                if (name.equals("link"))
                    link = jr.nextString();
                else if (name.equals("title"))
                    title = jr.nextString();
            }
            tableLinks.add(new TableLink(link, title));
            jr.endObject();
        }
        jr.endArray();
        return tableLinks;
    }

    private static List<RandomTable> readTableListJson(JsonReader jr) throws IOException {
        jr.beginArray();
        List<RandomTable> tables = new ArrayList<>();
        int i = 0;
        while (jr.hasNext()) {
            tables.add(readTableJson(jr, i));
            i++;
        }
        jr.endArray();
        return tables;
    }

    private static RandomTable readTableJson(JsonReader jr, int index) throws IOException {
        String title = "Table";
        String dice = "xdy";
        List<TableEntry> entries = new ArrayList<>();

        jr.beginObject();

        while (jr.hasNext()) {
            String name = jr.nextName();
            if (name.equals("name"))
                title = jr.nextString();
            else if (name.equals("dice"))
                dice = jr.nextString();
            else if (name.equals("table_entries")) {
                int i = 1; //TODO is this ever used?
                jr.beginArray();
                while (jr.hasNext()) {
                    jr.beginObject();
                    String entry = "";
                    String diceVal = "";
                    while (jr.hasNext()) {
                        String entry_key = jr.nextName();
                        if (entry_key.equals("dice_val")) {
                            diceVal = jr.nextString();
                        } else if (entry_key.equals("entry")) {
                            entry = jr.nextString();
                        }
                    }
                    entries.add(new TableEntry(entry, diceVal));
                    i++;
                    jr.endObject();
                }
                jr.endArray();
            }
        }
        jr.endObject();
        RandomTable table = new RandomTable(title, dice, index, entries);
        return table;
    }
}

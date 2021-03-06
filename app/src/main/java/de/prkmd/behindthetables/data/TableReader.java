package de.prkmd.behindthetables.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
        String title;
        List<TableCollectionEntry> tables;
        String reference = null;
        String description = "";
        String id = null;
        String category;
        List<TableLink> related_tables = new ArrayList<>();
        List<TableLink> use_with_tables = new ArrayList<>();
        List<String> keywords = new ArrayList<>();

        JsonParser parser = new JsonParser();
        JsonObject tableCollection = parser.parse(new InputStreamReader(is, "UTF-8")).getAsJsonObject();
        is.close();
        title = tableCollection.get("title").getAsString();
        category = tableCollection.get("category").getAsString();

        if(tableCollection.has("id"))
            id = tableCollection.get("id").getAsString();
        if(tableCollection.has("reference"))
            reference = tableCollection.get("reference").getAsString();
        if(tableCollection.has("description"))
            description = tableCollection.get("description").getAsString();

        if(tableCollection.has("keywords")) {
            JsonArray keywordsArray = tableCollection.get("keywords").getAsJsonArray();
            for (JsonElement e : keywordsArray) {
                keywords.add(e.getAsString());
            }
        }
        if(tableCollection.has("related_tables"))
            related_tables = readLinks(tableCollection.get("related_tables").getAsJsonArray());
        if(tableCollection.has("use_with"))
            use_with_tables = readLinks(tableCollection.get("use_with").getAsJsonArray());

        tables = readTableListJson(tableCollection.get("tables").getAsJsonArray());

        TableCollection tc = new TableCollection(title, tables, reference, description, category, id, related_tables, use_with_tables, keywords);


        return tc;
    }

    private static List<TableLink> readLinks(JsonArray ja) throws IOException {
        List<TableLink> tableLinks = new ArrayList<>();
        for(JsonElement e: ja) {
            String title = e.getAsJsonObject().get("title").getAsString();
            String link = e.getAsJsonObject().get("link").getAsString();
            tableLinks.add(new TableLink(link, title));
        }
        return tableLinks;
    }

    private static List<TableCollectionEntry> readTableListJson(JsonArray ja) throws IOException {
        int i = 0;
        List<TableCollectionEntry> tables = new ArrayList<>();
        for(JsonElement e: ja) {
            JsonObject jo = e.getAsJsonObject();
            if(jo.has("subcategory")) {
                tables.add(new SubcategoryEntry(jo.get("subcategory").getAsString()));
            } else {
                tables.add(readTableJson(jo, i));
                i++;
            }
        }
        return tables;
    }

    private static RandomTable readTableJson(JsonObject jo, int index) throws IOException {
        String title;
        String dice;
        List<TableEntry> entries = new ArrayList<>();

        title = jo.get("name").getAsString();
        dice = jo.get("dice").getAsString();
        JsonArray table_entries = jo.get("table_entries").getAsJsonArray();
        int i = 0;
        for(JsonElement e: table_entries) {
            String entry = e.getAsJsonObject().get("entry").getAsString();
            String diceVal = e.getAsJsonObject().get("dice_val").getAsString();
            entries.add(new TableEntry(i, entry, diceVal));
            i++;
        }

        RandomTable table = new RandomTable(title, dice, index, entries);
        return table;
    }
}

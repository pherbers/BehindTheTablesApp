package de.prkmd.behindthetables.data;

import java.util.HashMap;

/**
 * Created by Patrick on 12.03.2017.
 */

public class TableCollectionContainer {
    private HashMap<String, TableCollection> tableCollectionMap;

    private static TableCollectionContainer tableCollectionContainer;

    private TableCollectionContainer() {
        tableCollectionMap = new HashMap<>();
    }

    public static TableCollectionContainer getTableCollectionContainer() {
        if (tableCollectionContainer == null) {
            tableCollectionContainer = new TableCollectionContainer();
        }
        return tableCollectionContainer;
    }

    public void put(String key, TableCollection value) {
        tableCollectionMap.put(key, value);
    }

    public TableCollection get(String key) {
        return tableCollectionMap.get(key);
    }

    public boolean containsKey(String key) {
        return tableCollectionMap.containsKey(key);
    }
}

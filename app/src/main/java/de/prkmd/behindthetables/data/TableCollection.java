package de.prkmd.behindthetables.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Patrick on 11.03.2017.
 */

public class TableCollection {

    private String title;
    private String reference;
    private List<TableCollectionEntry> tables;
    private String description = "";
    private String id = "";
    private String category;
    private List<TableLink> relatedTables = null;
    private List<TableLink> useWithTables = null;
    private List<String> keywords = null;

    public TableCollection() {
        // Creates a default empty table
        this.title = "New Table Collection";
        this.reference = "";
        this.description = "An empty table";
        this.id = TableCollectionContainer.getTableCollectionContainer().getNewID();
        this.category = "Custom";
        tables = new ArrayList<>();
        relatedTables = new ArrayList<>();
        useWithTables = new ArrayList<>();
        keywords = new ArrayList<>();
    }

    public TableCollection(String title, List<TableCollectionEntry> tables, String reference, String description, String category, String id, List<TableLink> relatedTables, List<TableLink> useWithTables, List<String> keywords) {
        this.title = title;
        this.reference = reference;
        this.tables = tables;
        this.description = description;
        this.category = category;
        this.id = id;
        this.relatedTables = relatedTables;
        this.useWithTables = useWithTables;
        this.keywords = keywords;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public List<TableCollectionEntry> getTables() {
        return tables;
    }

    public void setTables(List<TableCollectionEntry> tables) {
        this.tables = tables;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<TableLink> getRelatedTables() {
        return relatedTables;
    }

    public void setRelatedTables(List<TableLink> relatedTables) {
        this.relatedTables = relatedTables;
    }

    public List<TableLink> getUseWithTables() {
        return useWithTables;
    }

    public void setUseWithTables(List<TableLink> useWithTables) {
        this.useWithTables = useWithTables;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public void rollAllTables() {
        for(TableCollectionEntry table: tables) {
            if(table instanceof RandomTable)
                ((RandomTable)table).roll();
        }
    }

    public void addNewTable(String name) {
        List<TableEntry> l = new ArrayList<>();
        l.add(new TableEntry());
        tables.add(new RandomTable(name, "1d1", tables.size(), l));
    }
}

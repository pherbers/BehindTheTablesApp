package de.rub.pherbers.behindthetables.data;

import java.util.List;

/**
 * Created by Patrick on 11.03.2017.
 */

public class TableCollection {

    private String title;
    private String reference;
    private List<RandomTable> tables;
    private String description = "";
    private String id = "";
    private List<TableLink> relatedTables = null;
    private List<TableLink> useWithTables = null;
    private List<String> keywords = null;

    public TableCollection(String title, List<RandomTable> tables) {
        this.title = title;
        this.tables = tables;
    }

    public TableCollection(String title, List<RandomTable> tables, String reference, String description, String id, List<TableLink> relatedTables, List<TableLink> useWithTables, List<String> keywords) {
        this.title = title;
        this.reference = reference;
        this.tables = tables;
        this.description = description;
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

    public List<RandomTable> getTables() {
        return tables;
    }

    public void setTables(List<RandomTable> tables) {
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
        for(RandomTable table: tables) {
            table.roll();
        }
    }
}

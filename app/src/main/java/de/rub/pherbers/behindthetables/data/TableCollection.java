package de.rub.pherbers.behindthetables.data;

import java.util.List;
import java.util.Observer;
import java.util.Random;

import de.rub.pherbers.behindthetables.RandomTableActivity;

/**
 * Created by Patrick on 11.03.2017.
 */

public class TableCollection {

    private String title;
    private String reference;
    private List<RandomTable> tables;

    public TableCollection(String title, List<RandomTable> tables) {
        this.title = title;
        this.tables = tables;
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

    public void addObserver(Observer o) {
        for(RandomTable table: tables) {
            table.addObserver(o);
        }
    }

    public void removeObserver(Observer o) {
        for(RandomTable table: tables) {
            table.deleteObserver(o);
        }
    }

    public void rollAllTables() {
        for(RandomTable table: tables) {
            table.roll();
        }
    }
}

package de.rub.pherbers.behindthetables.data;

import java.util.List;
import java.util.Observable;
import java.util.Random;

/**
 * Created by Patrick on 11.03.2017.
 */

public class RandomTable extends Observable {
    private String name;
    private String dice;
    private List<TableEntry> entries;
    private int tableIndex;

    private int rolledIndex = -1;

    private boolean expanded = false;

    public RandomTable(String name, String dice, int index, List<TableEntry> entries) {
        this.name = name;
        this.entries = entries;
        this.dice = dice;
        this.tableIndex = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDice() {
        return dice;
    }

    public void setDice(String dice) {
        this.dice = dice;
    }

    public List<TableEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<TableEntry> entries) {
        this.entries = entries;
    }

    public int getTableIndex() {
        return tableIndex;
    }

    public void setTableIndex(int tableIndex) {
        this.tableIndex = tableIndex;
    }

    public int getRolledIndex() {
        return rolledIndex;
    }

    public void setRolledIndex(int rolledIndex) {
        this.rolledIndex = rolledIndex;
        setChanged();
        notifyObservers(tableIndex);
    }

    public boolean hasRolled() {
        return getRolledIndex() > -1;
    }

    public void roll() {
        setRolledIndex(new Random().nextInt(entries.size()));
    }

    public int size() {
        return entries.size();
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public void toggle() {
        setExpanded(!isExpanded());
    }

    @Override
    public String toString() {
        return name;
    }
}

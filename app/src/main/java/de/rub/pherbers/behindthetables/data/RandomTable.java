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

    private int rolledIndex = -1;

    public RandomTable(String name, String dice, List<TableEntry> entries) {
        this.name = name;
        this.entries = entries;
        this.dice = dice;
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

    public int getRolledIndex() {
        return rolledIndex;
    }

    public void setRolledIndex(int rolledIndex) {
        this.rolledIndex = rolledIndex;
        setChanged();
        notifyObservers();
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

    @Override
    public String toString() {
        return name;
    }
}

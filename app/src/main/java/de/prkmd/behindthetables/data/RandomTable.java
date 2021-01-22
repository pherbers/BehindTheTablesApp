package de.prkmd.behindthetables.data;

import java.util.List;
import java.util.Random;

/**
 * Created by Patrick on 11.03.2017.
 */

public class RandomTable implements TableCollectionEntry {

    public static final int TABLE_NOT_ROLLED_YET = -1;

    private String name;
    private String dice;
    private List<TableEntry> entries;
    private int tableIndex;

    private int rolledIndex = TABLE_NOT_ROLLED_YET;

    private boolean expanded = false;

    private int maxDiceValue;

    // If the table has entries that have a dice range, we have to do additional calculations
    private boolean nonUniformDiceEntries = false;

    public RandomTable(String name, String dice, int index, List<TableEntry> entries) {
        this.name = name;
        this.entries = entries;
        this.dice = dice;
        this.tableIndex = index;
        checkEntries();
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
    }

    public boolean hasRolled() {
        return getRolledIndex() > -1;
    }

    public void roll() {
        if(!isNonUniformDiceEntries())
            setRolledIndex(new Random().nextInt(entries.size()));
        else {
            int rolledDiceValue = new Random().nextInt(maxDiceValue);
            for(int i = 1; i < entries.size(); i++) {
                if(rolledDiceValue < entries.get(i).getDiceValue()) {
                    setRolledIndex(i - 1);
                    return;
                }
            }
            setRolledIndex(entries.size() - 1);
        }
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

    public boolean isNonUniformDiceEntries() {
        return nonUniformDiceEntries;
    }

    public void toggle() {
        setExpanded(!isExpanded());
    }

    public void checkEntries() {
        for(TableEntry e: entries) {
            if(e.getDiceValueTo() > -1) {
                nonUniformDiceEntries = true;
                if(e.getDiceValueTo() > maxDiceValue)
                    maxDiceValue = e.getDiceValueTo();
            }
            if(e.getDiceValue() > maxDiceValue)
                maxDiceValue = e.getDiceValue();
        }
    }

    @Override
    public String toString() {
        return name;
    }

    public void addNewEntry() {
        entries.add(new TableEntry(entries.size(), "", maxDiceValue+1));
        maxDiceValue++;
    }

    /***
     * Removes empty entries at the back of the table and returns the new size.
     * @return The new number of tables.
     */
    public int trim() {
        for(int i = entries.size() - 1; i > 0; i--) {
            if(entries.get(i).isEmpty()) {
                entries.remove(i);
            }
            else
                break;
        }
        return entries.size();
    }

    public void fixDiceValues() {
        int i = 0;
        int j = 0;
        for(TableEntry e: entries) {
            if(e.getDiceValueTo() > -1) {
                nonUniformDiceEntries = true;

                i++;
                int range = Math.abs(e.getDiceValueTo() - e.getDiceValue());
                e.setDiceValue(i);
                i += range;
                e.setDiceValueTo(i);
            } else {
                i++;
                e.setDiceValue(i);
            }
            e.setEntryPosition(j);
            j++;
        }
        maxDiceValue = i;
        dice = "1d" + maxDiceValue;
    }
}

package de.prkmd.behindthetables.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Patrick on 11.03.2017.
 */

public class TableEntry {
    private String text;

    private int entryPosition;
    private int diceValue;
    private int diceValueTo = -1;

//    private static final String singleNumberRegex = "^(\\d+)$";

    private static final Pattern singleNumberRegex = Pattern.compile("^(\\d+)$");
    private static final Pattern doubleNumberRegex = Pattern.compile("^(\\d+)\\s*-\\s*(\\d+)$");

    public TableEntry() {
        text = "";
    }

    public TableEntry(int entryPosition, String text, int diceValue) {
        this.entryPosition = entryPosition;
        this.text = text;
        this.diceValue = diceValue;
    }

    public TableEntry(int entryPosition, String text, int diceValue, int diceValueTo) {
        this.entryPosition = entryPosition;
        this.text = text;
        this.diceValue = diceValue;
        this.diceValueTo = diceValueTo;
    }

    public TableEntry(int entryPosition, String entry, String diceString) {
        this.entryPosition = entryPosition;
        this.text = entry;

        Matcher m1 = singleNumberRegex.matcher(diceString);
        if (m1.matches()) {
            this.diceValue = Integer.parseInt(m1.group(1));
        } else {
            Matcher m2 = doubleNumberRegex.matcher(diceString);
            if (m2.matches()) {
                this.diceValue = Integer.parseInt(m2.group(1));
                this.diceValueTo = Integer.parseInt(m2.group(2));
            }
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getDiceValue() {
        return diceValue;
    }

    public void setDiceValue(int diceValue) {
        this.diceValue = diceValue;
    }

    public int getDiceValueTo() {
        return diceValueTo;
    }

    public void setDiceValueTo(int diceValueTo) {
        this.diceValueTo = diceValueTo;
    }

    public int getEntryPosition() {
        return entryPosition;
    }

    public void setEntryPosition(int entryPosition) {
        this.entryPosition = entryPosition;
    }

    public String getDiceString() {
        if(diceValueTo > -1) {
            return diceValue + " - " + diceValueTo;
        } else {
            return Integer.toString(diceValue);
        }
    }

    @Override
    public String toString() {
        return getText();
    }

    public boolean isEmpty() {
        return text.isEmpty();
    }
}

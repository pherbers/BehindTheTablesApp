package de.rub.pherbers.behindthetables.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Patrick on 11.03.2017.
 */

public class TableEntry {
    private String text;
    private int diceValue;
    private int diceValueTo = -1;

//    private static final String singleNumberRegex = "^(\\d+)$";

    private static final Pattern singleNumberRegex = Pattern.compile("^(\\d+)$");
    private static final Pattern doubleNumberRegex = Pattern.compile("^(\\d+)\\s*-\\s*(\\d+)$");

    public TableEntry(String text, int diceValue) {
        this.text = text;
        this.diceValue = diceValue;
    }

    public TableEntry(String text, int diceValue, int diceValueTo) {
        this.text = text;
        this.diceValue = diceValue;
        this.diceValueTo = diceValueTo;
    }

    public TableEntry(String entry, String diceString) {
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
}

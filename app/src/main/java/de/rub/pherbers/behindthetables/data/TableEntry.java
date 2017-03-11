package de.rub.pherbers.behindthetables.data;

/**
 * Created by Patrick on 11.03.2017.
 */

public class TableEntry {
    private String text;


    private int diceValue;

    public TableEntry(String text, int diceValue) {
        this.text = text;
        this.diceValue = diceValue;
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

    @Override
    public String toString() {
        return text;
    }
}

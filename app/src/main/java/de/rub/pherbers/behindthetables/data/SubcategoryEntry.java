package de.rub.pherbers.behindthetables.data;

/**
 * Created by Patrick on 16.04.2017.
 */

public class SubcategoryEntry implements TableCollectionEntry {
    private String text;

    public SubcategoryEntry(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

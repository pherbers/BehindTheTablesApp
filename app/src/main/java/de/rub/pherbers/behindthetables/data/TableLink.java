package de.rub.pherbers.behindthetables.data;

/**
 * Created by Patrick on 15.03.2017.
 */

public class TableLink {
    private String link_id;
    private String link_title;

    public TableLink(String link_id, String link_title) {
        this.link_id = link_id;
        this.link_title = link_title;
    }

    public String getLink_id() {
        return link_id;
    }

    public void setLink_id(String link_id) {
        this.link_id = link_id;
    }

    public String getLink_title() {
        return link_title;
    }

    public void setLink_title(String link_title) {
        this.link_title = link_title;
    }
}

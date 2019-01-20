package de.prkmd.behindthetables.data;

/**
 * Created by Patrick on 15.03.2017.
 */

public class TableLink {
    private String linkId;
    private String linkTitle;

    public TableLink(String linkId, String linkTitle) {
        this.linkId = linkId;
        this.linkTitle = linkTitle;
    }

    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public String getLinkTitle() {
        return linkTitle;
    }

    public void setLinkTitle(String linkTitle) {
        this.linkTitle = linkTitle;
    }

    public String getHTML() {
        return "<a href=\"" + getLinkId() + "\">" + getLinkTitle() + "</a>";
    }
}

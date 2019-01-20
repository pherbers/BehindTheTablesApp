package de.prkmd.behindthetables.data;

import java.util.Comparator;

/**
 * Created by Patrick on 16.09.2017.
 */

public class TableFileComparator implements Comparator<TableFile> {

    String searchQuery = null;

    public TableFileComparator() {

    }
    public TableFileComparator(String searchQuery) {
        this.searchQuery = searchQuery.toLowerCase();
    }

    @Override
    public int compare(TableFile o1, TableFile o2) {
        if (searchQuery == null) {
            return o1.compareTo(o2);
        }

        int titleDiff = rankStrings(o1.getTitle().toLowerCase(), o2.getTitle().toLowerCase());
        if(titleDiff != 0)
            return titleDiff;

        int keywordDiff = rankStrings(o1.getKeywords().toLowerCase(), o2.getKeywords().toLowerCase());
        if(keywordDiff != 0)
            return keywordDiff;

        int descriptionDiff = rankStrings(o1.getDescription().toLowerCase(), o2.getDescription().toLowerCase());
        if(descriptionDiff != 0)
            return descriptionDiff;

        return 0;
    }

    private int rankStrings(String s1, String s2) {
        int s1Diff = s1.indexOf(searchQuery);
        int s2Diff = s2.indexOf(searchQuery);
        if(s1Diff != -1 && s2Diff == -1)
            return -1;
        if(s1Diff == -1 && s2Diff != -1)
            return 1;
        if(s1Diff != -1 && s2Diff != -1)
            return s1Diff - s2Diff;

        return 0;
    }
}

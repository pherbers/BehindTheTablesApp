package de.rub.pherbers.behindthetables.util;

import android.content.SearchRecentSuggestionsProvider;

import de.rub.pherbers.behindthetables.BehindTheTables;

/**
 * Created by Nils on 02.04.2017.
 */

public class TableSearchRecentSuggestionsProvider extends SearchRecentSuggestionsProvider {

    public final static String AUTHORITY = BehindTheTables.APP_TAG + "TableSearchSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public TableSearchRecentSuggestionsProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }

}

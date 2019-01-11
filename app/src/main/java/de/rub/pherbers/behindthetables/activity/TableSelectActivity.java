package de.rub.pherbers.behindthetables.activity;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;

import de.rub.pherbers.behindthetables.R;
import de.rub.pherbers.behindthetables.adapter.TableFileAdapter;
import de.rub.pherbers.behindthetables.data.TableFile;
import de.rub.pherbers.behindthetables.data.TableFileComparator;
import de.rub.pherbers.behindthetables.sql.DBAdapter;
import de.rub.pherbers.behindthetables.util.TableSearchRecentSuggestionsProvider;
import de.rub.pherbers.behindthetables.view.listener.RecyclerItemClickListener;
import timber.log.Timber;

import static de.rub.pherbers.behindthetables.BehindTheTables.APP_TAG;

public class TableSelectActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String INSTANCE_SEARCH_QUERY = APP_TAG + "home_search_query";
    public static final String INSTANCE_SCROLL_POSITION = APP_TAG + "home_scroll_position";
    public static final String EXTRA_CATEGORY_DISCRIMINATOR = APP_TAG + "extra_category_discriminator";
    public static final String EXTRA_FAVS_ONLY = APP_TAG + "extra_favs_only";

    private ArrayList<TableFile> foundTables, matchedTables;
    private RecyclerView list;
    private SearchView searchView;
    private TableFileAdapter listAdapter;

    private String bufferedSearchQuery;
    private int bufferedScrollPos;
    private boolean favsOnly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_select);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        matchedTables = null;

        //FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        //fab.setOnClickListener(new View.OnClickListener() {
        //	@Override
        //	public void onClick(View view) {
        //		Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
        //				.setAction("Action", null).show();
        //	}
        //});

        Intent intent = getIntent();
        favsOnly = intent.getBooleanExtra(EXTRA_FAVS_ONLY, false);

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setTitle(R.string.app_name);

            if (favsOnly) {
                bar.setSubtitle(R.string.category_favs);
            } else {
                if (intent.hasExtra(EXTRA_CATEGORY_DISCRIMINATOR)) {
                    DBAdapter adapter = new DBAdapter(this).open();
                    Cursor c = adapter.getCategory(intent.getLongExtra(EXTRA_CATEGORY_DISCRIMINATOR, -1));
                    if (c.moveToFirst()) {
                        bar.setSubtitle(c.getString(DBAdapter.COL_CATEGORY_TITLE));
                    } else {
                        bar.setSubtitle(R.string.category_all);
                    }
                } else {
                    bar.setSubtitle(R.string.category_all);
                }
            }
        }

        list = (RecyclerView) findViewById(R.id.home_table_file_list);
        list.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        list.setLayoutManager(layoutManager);
        list.addOnItemTouchListener(new RecyclerItemClickListener(this, list, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                TableFile file;
                if (matchedTables == null) {
                    file = foundTables.get(position);
                } else {
                    file = matchedTables.get(position);
                }

                onItemClicked(file);
            }

            @Override
            public void onLongItemClick(View view, int position) {
                TableFile file = foundTables.get(position);
                onItemLongClickd(file);
            }
        }));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(list.getContext(),
                layoutManager.getOrientation());
        list.addItemDecoration(dividerItemDecoration);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(INSTANCE_SCROLL_POSITION)) {
                list.scrollToPosition(savedInstanceState.getInt(INSTANCE_SCROLL_POSITION));
            }
            if (savedInstanceState.containsKey(INSTANCE_SEARCH_QUERY)) {
                String query = savedInstanceState.getString(INSTANCE_SEARCH_QUERY);
                Timber.i("Extracted search query from SI: " + query);
                bufferedSearchQuery = query;
            }
        }

        handleSearchIntent(getIntent());
        hideSoftInput();
    }

    public void viewTableCollection(Random random) {
        ArrayList<TableFile> list;
        if (matchedTables == null) {
            list = new ArrayList<>(foundTables);
        } else {
            list = new ArrayList<>(matchedTables);
        }

        viewTableCollection(list.get(random.nextInt(list.size())));
    }

    public void viewTableCollection(TableFile file) {
        if (file == null) {
            Timber.e("Attempting to view a table, but the table-file is null!");
            return;
        }

        Intent intent = new Intent(this, RandomTableActivity.class);
        intent.putExtra(RandomTableActivity.EXTRA_TABLE_DATABASE_RESOURCE_LOCATION, file.getResourceLocation());
        startActivity(intent);
    }

    private void discoverTables() {
        matchedTables = null;
        foundTables = new ArrayList<>();
        DBAdapter adapter = new DBAdapter(this).open();

        if (favsOnly) {
            //Discovering JSONs from favs.

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            for (String s : preferences.getStringSet(TableFile.PREFS_FAVORITE_TABLES, new HashSet<String>())) {
                foundTables.add(TableFile.createFromDB(s, adapter));
            }
            Timber.i("DB files that are favs found: " + foundTables.size());
        } else {
            //Discovering JSONs from DB

            Intent intent = getIntent();
            Cursor cursor;
            if (intent.hasExtra(EXTRA_CATEGORY_DISCRIMINATOR)) {
                long discriminator = intent.getLongExtra(EXTRA_CATEGORY_DISCRIMINATOR, -1);
                Timber.i("Category discriminator: " + discriminator);
                cursor = adapter.getAllTableCollections(discriminator);
            } else {
                Timber.i("No category discriminator specified. Displaying all tables.");
                cursor = adapter.getAllTableCollections();
            }

            TableFile file = null;
            if (cursor.moveToFirst())
                do {
                    file = TableFile.createFromDB(cursor);
                    foundTables.add(file);
                    //long id = cursor.getLong(DBAdapter.COL_ROWID);
                    //foundTables.add(TableFile.getFromDB(id, adapter));
                } while (cursor.moveToNext());
            Timber.i("Number of JSONs found in the DB: " + cursor.getCount());
        }
        adapter.close();

        //Discovering external JSONs.
        Timber.i("Attempting to discover external JSON files.");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //TODO implement external search
        } else {
            Timber.i("... but this app has no permission to read the external storage.");
        }

        Timber.i("List of displayed tables [count: " + foundTables.size() + "]: " + Arrays.toString(foundTables.toArray()));
        displayFiles(foundTables);
    }

    private void updateDiscoveredFiles(String searchQuery) {
        Timber.i("search query request: " + searchQuery);
        bufferedSearchQuery = searchQuery;

        searchQuery = searchQuery.replace(String.valueOf(DBAdapter.LINK_COLLECTION_SEPARATOR), " ").toLowerCase().trim();
        matchedTables = new ArrayList<>();

        for (TableFile f : foundTables) {
            //TODO maybe apply preferences to narrow search creteria?
            if (f.getTitle().toLowerCase().contains(searchQuery)) {
                matchedTables.add(f);
                continue;
            }
            if (f.hasKeyword(searchQuery)) {
                matchedTables.add(f);
                continue;
            }
            if (f.isFavorite(this)) {
                if (getString(R.string.search_query_support_fav_tag).contains(searchQuery)) {
                    matchedTables.add(f);
                    continue;
                }
            }
            if (f.getDescription().toLowerCase().contains(searchQuery)) {
                matchedTables.add(f);
            }
        }



        Timber.i("Already discovered tables: " + Arrays.toString(foundTables.toArray()));
        Timber.i("Discovered tables that match search query: " + Arrays.toString(matchedTables.toArray()));
        Timber.i(matchedTables.size() + " / " + foundTables.size() + " apply to the search query.");

        TableFileComparator tfc = new TableFileComparator(searchQuery);
        displayFiles(matchedTables, tfc);
    }

    private void displayFiles(ArrayList<TableFile> tables) {
        Collections.sort(tables);
        listAdapter = new TableFileAdapter(this, tables);
        list.setAdapter(listAdapter);
    }

    private void displayFiles(ArrayList<TableFile> tables, Comparator<TableFile> comparator) {
        Collections.sort(tables, comparator);
        listAdapter = new TableFileAdapter(this, tables);
        list.setAdapter(listAdapter);
    }

    private void onItemClicked(TableFile file) {
        Timber.i("User clicked: " + file.getTitle());
        viewTableCollection(file);
    }

    public void onItemLongClickd(final TableFile file) {
        Timber.i("User long clicked: " + file.getTitle());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(file.getTitle());
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        int icon;
        int message;
        if (file.isFavorite(this)) {
            icon = R.drawable.ic_star_border_black_48dp;
            message = R.string.action_unfav_detail;
            builder.setPositiveButton(R.string.action_unfav, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    file.setFavorite(TableSelectActivity.this, false);
                    Toast.makeText(TableSelectActivity.this, getString(R.string.info_removed_from_favs, file.getTitle()), Toast.LENGTH_LONG).show();
                    listAdapter.notifyDataSetChanged();
                }
            });
        } else {
            icon = R.drawable.ic_star_black_48dp;
            message = R.string.action_fav_detail;
            builder.setPositiveButton(R.string.action_fav, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    file.setFavorite(TableSelectActivity.this, true);
                    Toast.makeText(TableSelectActivity.this, getString(R.string.info_added_to_favs, file.getTitle()), Toast.LENGTH_LONG).show();
                    listAdapter.notifyDataSetChanged();
                }
            });
        }
        builder.setMessage(getString(message, file.getTitle()));
        builder.setIcon(icon);

        builder.show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(INSTANCE_SCROLL_POSITION, getScrollPosition());
        if (bufferedSearchQuery != null) {
            outState.putString(INSTANCE_SEARCH_QUERY, bufferedSearchQuery);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        foundTables = new ArrayList<>();
        discoverTables();
        if (bufferedSearchQuery != null) {
            updateDiscoveredFiles(bufferedSearchQuery);
            if (searchView != null) {
                searchView.setIconified(false);
                searchView.setQuery(bufferedSearchQuery, false);
            }
        }

        list.scrollToPosition(bufferedScrollPos);
    }

    @Override
    protected void onPause() {
        super.onPause();

        bufferedScrollPos = getScrollPosition();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_table_select, menu);

        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        MenuItem item = menu.findItem(R.id.action_table_select_search);
        searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setQueryRefinementEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                updateDiscoveredFiles(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                updateDiscoveredFiles(newText);
                return false;
            }
        });
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        if (bufferedSearchQuery == null) {
            searchView.setIconified(true);
        } else {
            Timber.i("Found this buffered query while setting up the options menu: " + bufferedSearchQuery);
            searchView.setIconified(false);
            searchView.setQuery(bufferedSearchQuery, false);
        }

        return true;
    }

    private void hideSoftInput() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private int getScrollPosition() {
        Timber.i("Packing saved instance for adapterview.");
        int pos = 0;
        RecyclerView.LayoutManager layoutManager = list.getLayoutManager();
        if (layoutManager != null && layoutManager instanceof LinearLayoutManager) {
            pos = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }
        return pos;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleSearchIntent(intent);
        hideSoftInput();
    }

    private void handleSearchIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            bufferedSearchQuery = intent.getStringExtra(SearchManager.QUERY);

            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    TableSearchRecentSuggestionsProvider.AUTHORITY, TableSearchRecentSuggestionsProvider.MODE);
            suggestions.saveRecentQuery(bufferedSearchQuery, null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_table_settings:
                //TODO Settings?
                break;
            case android.R.id.home:
                finish();
                //TODO Why no burger menu?
                break;
            case R.id.action_random_table:
                viewTableCollection(new Random());
                break;
            default:
                Timber.w("Unknown menu item selected.");
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Timber.i("Nav item selected: " + item.getTitle());

        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            default:
                Timber.w("Unknown nav item selected.");
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

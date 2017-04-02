package de.rub.pherbers.behindthetables.activity;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SearchEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import de.rub.pherbers.behindthetables.BehindTheTables;
import de.rub.pherbers.behindthetables.R;
import de.rub.pherbers.behindthetables.adapter.RandomTableListAdapter;
import de.rub.pherbers.behindthetables.adapter.TableFileAdapter;
import de.rub.pherbers.behindthetables.data.TableFile;
import de.rub.pherbers.behindthetables.sql.DBAdapter;
import de.rub.pherbers.behindthetables.sql.DefaultTables;
import de.rub.pherbers.behindthetables.view.listener.RecyclerItemClickListener;
import timber.log.Timber;

import static android.provider.AlarmClock.EXTRA_MESSAGE;
import static de.rub.pherbers.behindthetables.BehindTheTables.APP_TAG;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String INSTANCE_SCROLL_POSITION = APP_TAG + "home_scroll_position";
    public static final String INSTANCE_SEARCH_QUERY = APP_TAG + "home_search_query";

    private String bufferedSearchQuery;
    private ArrayList<TableFile> foundTables, matchedTables;
    private RecyclerView list;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        ((NavigationView) findViewById(R.id.nav_view)).setNavigationItemSelectedListener(this);

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

        //TODO Code to request permission
        //Timber.i("My files dir: " + new FileManager(this).getJSONTableDir());
        //if (ContextCompat.checkSelfPermission(this,
        //		Manifest.permission.WRITE_EXTERNAL_STORAGE)
        //		!= PackageManager.PERMISSION_GRANTED) {
        //
        //	Timber.i("This app does not have permission to write the external storage!");
        //	Timber.i("Asking for permissions now.");
        //	ActivityCompat.requestPermissions(this,
        //			new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
        //			PERMISSION_REQUEST_CODE);
        //}
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

        //Discovering JSONs from DB
        DBAdapter adapter = new DBAdapter(this).open();
        Cursor cursor = adapter.getAllRows();
        while (cursor.moveToNext()) {
            String res = cursor.getString(DBAdapter.COL_TABLE_COLLECTION_LOCATION);
            foundTables.add(TableFile.createFromDB(res, adapter));
            //long id = cursor.getLong(DBAdapter.COL_ROWID);
            //foundTables.add(TableFile.getFromDB(id, adapter));
        }
        adapter.close();
        Timber.i("Number of JSONs found in the DB: " + cursor.getCount());

        //Discovering external JSONs.
        Timber.i("Attempting to discover external JSON files.");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //TODO implement external search
        } else {
            Timber.i("... but this app has no permission to read the external storage.");
        }

        Timber.i("List of displayed tables [count: " + foundTables.size() + "]: " + Arrays.toString(foundTables.toArray()));
        list.setAdapter(new TableFileAdapter(this, foundTables));
    }

    private void updateDiscoveredFiles(String searchQuery) {
        Timber.i("search query request: " + searchQuery);
        bufferedSearchQuery = searchQuery;

        searchQuery = searchQuery.replace(String.valueOf(DBAdapter.LINK_COLLECTION_SEPARATOR), " ").toLowerCase();
        matchedTables = new ArrayList<>();

        for (TableFile f : foundTables) {
            if (f.getTitle().toLowerCase().contains(searchQuery)) {
                matchedTables.add(f);
            }
        }

        Timber.i("Already discovered tables: " + Arrays.toString(foundTables.toArray()));
        Timber.i("Discovered tables that match search query: " + Arrays.toString(matchedTables.toArray()));
        Timber.i(matchedTables.size() + " / " + foundTables.size() + " apply to the search query.");

        list.setAdapter(new TableFileAdapter(this, matchedTables));
    }

    private void onItemClicked(TableFile file) {
        Timber.i("User clicked: " + file.getTitle());
        viewTableCollection(file);
    }

    public void onItemLongClickd(TableFile file) {
        Timber.i("User long clicked: " + file.getTitle());

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Timber.i("Packing saved instance.");
        int scrollPos = 0;
        RecyclerView.LayoutManager layoutManager = list.getLayoutManager();
        if (layoutManager != null && layoutManager instanceof LinearLayoutManager) {
            scrollPos = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }
        outState.putInt(INSTANCE_SCROLL_POSITION, scrollPos);

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);

        if (!BehindTheTables.isDebugBuild()) {
            menu.removeItem(R.id.action_debug_reset_db);
        }

        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(item);
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
            Timber.e("Found this buffered query while setting up the options menu: " + bufferedSearchQuery);
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleSearchIntent(intent);
        hideSoftInput();
    }

    private void handleSearchIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            bufferedSearchQuery = intent.getStringExtra(SearchManager.QUERY);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                //TODO Settings?
                break;
            case R.id.action_debug_reset_db:
                DBAdapter adapter = new DBAdapter(this).open();
                adapter.fillWithDefaultData(this);
                adapter.close();
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

package de.rub.pherbers.behindthetables.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Arrays;

import de.rub.pherbers.behindthetables.BehindTheTables;
import de.rub.pherbers.behindthetables.R;
import de.rub.pherbers.behindthetables.adapter.RandomTableListAdapter;
import de.rub.pherbers.behindthetables.adapter.TableFileAdapter;
import de.rub.pherbers.behindthetables.data.TableFile;
import de.rub.pherbers.behindthetables.sql.DBAdapter;
import de.rub.pherbers.behindthetables.view.listener.RecyclerItemClickListener;
import timber.log.Timber;

import static android.provider.AlarmClock.EXTRA_MESSAGE;
import static de.rub.pherbers.behindthetables.BehindTheTables.APP_TAG;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String INSTANCE_SCROLL_POSITION = APP_TAG + "home_scroll_position";

    private ArrayList<TableFile> foundTables;
    private RecyclerView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
                TableFile file = foundTables.get(position);
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
        }

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
        intent.putExtra(RandomTableActivity.EXTRA_TABLE_DATABASE_ID, file.getDatabaseID());
        startActivity(intent);
    }

    private void discoverTables() {
        foundTables = new ArrayList<>();

        //Discovering internal JSONs. Might be obsolete
        //Timber.i("Just to be sure. Known raw file ID: " + R.raw.table_4y5pl2);
        //Field[] fields = R.raw.class.getFields();
        //for (int i = 0; i < fields.length - 1; i++) {
        //	String name = fields[i].getName();
        //	Timber.v("Found this file in the raw data: " + name);
        //
        //	if (name.toLowerCase().startsWith(tableIdentifier)) {
        //		Timber.v("Found this raw data to be a valid table: " + name);
        //		foundTables.add(new TableFile(getResources().getIdentifier(name, "raw", getPackageName())));
        //	}
        //}

        //Discovering JSONs from DB
        DBAdapter adapter = new DBAdapter(this).open();
        Cursor cursor = adapter.getAllRows();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(DBAdapter.COL_ROWID);
            foundTables.add(TableFile.getFromDB(id, adapter));
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

    private void onItemClicked(TableFile file) {
        Timber.i("User clicked: " + file.getIdentifier());
        viewTableCollection(file);
    }

    public void onItemLongClickd(TableFile file) {
        Timber.i("User long clicked: " + file.getIdentifier());

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        int scrollPos = 0;
        RecyclerView.LayoutManager layoutManager = list.getLayoutManager();
        if (layoutManager != null && layoutManager instanceof LinearLayoutManager) {
            scrollPos = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }
        savedInstanceState.putInt(INSTANCE_SCROLL_POSITION, scrollPos);
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);

        if (!BehindTheTables.isDebugBuild()) {
            menu.removeItem(R.id.action_debug_reset_db);
        }

        return true;
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

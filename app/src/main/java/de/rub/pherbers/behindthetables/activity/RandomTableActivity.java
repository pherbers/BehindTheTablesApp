package de.rub.pherbers.behindthetables.activity;
//TODO move this to package de.rub.pherbers.behindthetables.activiy

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Toast;

import java.io.IOException;

import de.rub.pherbers.behindthetables.BehindTheTables;
import de.rub.pherbers.behindthetables.R;
import de.rub.pherbers.behindthetables.adapter.RandomTableListAdapter;
import de.rub.pherbers.behindthetables.data.RandomTable;
import de.rub.pherbers.behindthetables.data.TableCollection;
import de.rub.pherbers.behindthetables.data.TableCollectionContainer;
import de.rub.pherbers.behindthetables.data.TableFile;
import de.rub.pherbers.behindthetables.data.TableReader;
import de.rub.pherbers.behindthetables.sql.DBAdapter;
import de.rub.pherbers.behindthetables.view.RandomTableViewHolder;
import timber.log.Timber;

public class RandomTableActivity extends AppCompatActivity {

    public static final String EXTRA_TABLE_DATABASE_RESOURCE_LOCATION = BehindTheTables.APP_TAG + "extra_table_database_resource_location";

    private TableCollection table;
    private RecyclerView listView;
    private RandomTableListAdapter listAdapter;
    private TableFile tableFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_table);
        Toolbar toolbar = (Toolbar) findViewById(R.id.random_table_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent sourceIntent = getIntent();
        if (!sourceIntent.hasExtra(EXTRA_TABLE_DATABASE_RESOURCE_LOCATION)) {
            Timber.e("This Random Table activity has no Table Database ID in the start intent! Aborting!");
            //TODO handle this case better
            finish();
            return;
        }

        String resourceLocation = sourceIntent.getStringExtra(EXTRA_TABLE_DATABASE_RESOURCE_LOCATION);
        DBAdapter adapter = new DBAdapter(this).open();
        tableFile = TableFile.createFromDB(resourceLocation, adapter);
        adapter.close();

        if (tableFile == null) {
            Timber.e("Failed to obtain a table file from the DB with the location " + resourceLocation + "! Aborting activity!");
            //TODO handle this case better
            finish();
            return;
        }

        TableCollectionContainer tableCollectionContainer = TableCollectionContainer.getTableCollectionContainer();
        if (!tableCollectionContainer.containsKey(resourceLocation)) {
            try {
                if (tableFile.isExternal()) {
                    //TODO Load external file
                } else {
                    table = TableReader.readTable(getResources().openRawResource(tableFile.getResourceID(this)));
                }
                tableCollectionContainer.put(resourceLocation, table);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            table = tableCollectionContainer.get(resourceLocation);
        }

        setTitle(table.getTitle());

        listView = (RecyclerView) findViewById(R.id.random_table_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);

        listAdapter = new RandomTableListAdapter(this, table);
        listView.setAdapter(listAdapter);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        listView.addItemDecoration(mDividerItemDecoration);

        RecyclerView.ItemAnimator animator = new DefaultItemAnimator();
        listView.setItemAnimator(animator);

        //TextView tv = (TextView) findViewById(R.id.random_table_title);
        //tv.setText(table.getTitle());

        //TextView desc_tv = (TextView) findViewById(R.id.random_table_desc);
        //desc_tv.setText(table.getDescription());
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void diceRollAction(View mView) {
        //table.rollAllTables();
        for (int i = 0; i < listAdapter.getItemCount()-1; i++) {
            RandomTableViewHolder v = (RandomTableViewHolder) listView.findViewHolderForAdapterPosition(i + 1);
            int prev = table.getTables().get(i).getRolledIndex();
            table.getTables().get(i).roll();
            if (v != null) {
                v.rerollAnimation(prev);
            }
        }
        updateListOutOfView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

    }

    public void scrollToPosition(int pos) {
        listView.smoothScrollToPosition(pos);
    }

    public void actionCollapseAll() {
        for (int i = 1; i < listAdapter.getItemCount(); i++) {
            RecyclerView.ViewHolder v = listView.findViewHolderForLayoutPosition(i);
            if (v == null) {
                continue;
            }
            if (v instanceof RandomTableViewHolder)
                ((RandomTableViewHolder) v).collapse(false);
        }
        updateListOutOfView();
    }

    public void actionExpandAll() {
        for (int i = 1; i < listAdapter.getItemCount(); i++) {
            RecyclerView.ViewHolder v = listView.findViewHolderForLayoutPosition(i);
            if (v == null) {
                continue;
            }
            if (v instanceof RandomTableViewHolder)
                ((RandomTableViewHolder) v).expand(false);
        }
        for (RandomTable t : table.getTables())
            t.setExpanded(true);
        updateListOutOfView();
    }

    public void updateListOutOfView() {
        int firstPos = ((LinearLayoutManager) listView.getLayoutManager()).findFirstVisibleItemPosition();
        int lastPos = ((LinearLayoutManager) listView.getLayoutManager()).findLastVisibleItemPosition();
        if (firstPos > 0) {
            listAdapter.notifyItemRangeChanged(0, firstPos);
        }
        if (lastPos < listAdapter.getItemCount() - 1) {
            listAdapter.notifyItemRangeChanged(lastPos + 1, listAdapter.getItemCount() - 1);
        }
    }

    public void redrawList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listAdapter.notifyDataSetChanged();
                listView.invalidate();
            }
        });
    }

    public void redrawListAtPos(int pos) {
        listAdapter.notifyItemChanged(pos);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.random_table_activity_collapse_all:
                actionCollapseAll();
                break;
            case R.id.random_table_activity_expand_all:
                actionExpandAll();
                break;
            case R.id.random_table_activity_reference:
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(table.getReference()));
                startActivity(i);
                break;
            case R.id.random_table_activity_reset:
                resetTable();
                break;
            case R.id.random_table_activity_fav:
                setTableFavorite(true);
                break;
            case R.id.random_table_activity_unfav:
                setTableFavorite(false);
                break;
            default:
                Timber.w("Unknown menu in RandomTableActivity");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setTableFavorite(boolean favorite) {
        int text;
        if (favorite) text = R.string.info_added_to_favs;
        else text = R.string.info_removed_from_favs;

        Toast.makeText(this, getString(text, tableFile.getTitle()), Toast.LENGTH_LONG).show();
        tableFile.setFavorite(this, favorite);
        invalidateOptionsMenu();
    }

    public void resetTable() {
        for (int i = 0; i < listAdapter.getItemCount(); i++) {
            table.getTables().get(i).setRolledIndex(-1);
            redrawListAtPos(i);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.random_table_activity_menu, menu);
        // Disable "Open on reddit" if no reference is given
        // TODO this could be optimised. See code below.
        if (!URLUtil.isValidUrl(table.getReference())) {
            MenuItem item = menu.findItem(R.id.random_table_activity_reference);
            item.setVisible(false);
            item.setEnabled(false);
        }

        Timber.i("onCreateOptionsMenu() called -> tableFile is fav? " + tableFile.isFavorite(this));
        if (tableFile.isFavorite(this)) {
            menu.removeItem(R.id.random_table_activity_fav);
        } else {
            menu.removeItem(R.id.random_table_activity_unfav);
        }

        return true;
    }

    public RecyclerView getListView() {
        return listView;
    }

}

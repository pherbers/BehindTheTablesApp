package de.rub.pherbers.behindthetables;

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
import android.widget.TextView;

import java.io.IOException;

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

    public static final String EXTRA_TABLE_DATABASE_ID = BehindTheTables.APP_TAG + "extra_table_database_id";

    private TableCollection table;
    private RecyclerView listView;
    private RandomTableListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_table);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent sourceIntent = getIntent();
        if (!sourceIntent.hasExtra(EXTRA_TABLE_DATABASE_ID)) {
            Timber.e("This Random Table activity has no Table Database ID in the start intent! Aborting!");
            finish();
            return;
        }

        long databaseID = sourceIntent.getLongExtra(EXTRA_TABLE_DATABASE_ID, -1);
        DBAdapter adapter = new DBAdapter(this).open();
        TableFile file = TableFile.getFromDB(databaseID, adapter);
        adapter.close();

        if (file == null) {
            Timber.e("Failed to optain a table file from the DB with the ID " + databaseID + "! Aborting activity!");
            finish();
            return;
        }

        String tableIdentifier = file.getIdentifier();
        TableCollectionContainer tableCollectionContainer = TableCollectionContainer.getTableCollectionContainer();
        if (!tableCollectionContainer.containsKey(tableIdentifier)) {
            try {
                if (file.isExternal()) {
                    //TODO Load external file
                } else {
                    table = TableReader.readTable(getResources().openRawResource(file.getResourceID()));
                }
                tableCollectionContainer.put(tableIdentifier, table);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            table = tableCollectionContainer.get(tableIdentifier);
        }


        listView = (RecyclerView) findViewById(R.id.random_table_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);

        listAdapter = new RandomTableListAdapter(this, table);
        listView.setAdapter(listAdapter);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        listView.addItemDecoration(mDividerItemDecoration);

        RecyclerView.ItemAnimator animator = new DefaultItemAnimator();
        listView.setItemAnimator(animator);

        TextView tv = (TextView) findViewById(R.id.random_table_title);
        tv.setText(table.getTitle());

        TextView desc_tv = (TextView) findViewById(R.id.random_table_desc);
        desc_tv.setText(table.getDescription());
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void diceRollAction(View mView) {
        //table.rollAllTables();
        for (int i = 0; i < listAdapter.getItemCount(); i++) {
            RandomTableViewHolder v = (RandomTableViewHolder) listView.findViewHolderForAdapterPosition(i);
            int prev = table.getTables().get(i).getRolledIndex();
            table.getTables().get(i).roll();
            if (v != null) {
                v.rerollAnimation(prev);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

    }

    public void scrollToPosition(int pos) {
        listView.smoothScrollToPosition(pos);
    }

    public void actionCollapseAll() {
        for (int i = 0; i < listAdapter.getItemCount(); i++) {
            RecyclerView.ViewHolder v = listView.findViewHolderForLayoutPosition(i);
            if (v == null) {
                continue;
            }
            if (v instanceof RandomTableViewHolder)
                ((RandomTableViewHolder) v).collapse(false);
        }
        for (RandomTable t : table.getTables())
            t.setExpanded(false);
        int firstPos = ((LinearLayoutManager) listView.getLayoutManager()).findFirstVisibleItemPosition();
        int lastPos = ((LinearLayoutManager) listView.getLayoutManager()).findLastVisibleItemPosition();
        if (firstPos > 0) {
            listAdapter.notifyItemRangeChanged(0, firstPos);
        }
        if (lastPos < listAdapter.getItemCount() - 1) {
            listAdapter.notifyItemRangeChanged(lastPos + 1, listAdapter.getItemCount() - 1);
        }
        //listView.smoothScrollToPosition(0);
    }

    public void actionExpandAll() {
        for (int i = 0; i < listAdapter.getItemCount(); i++) {
            RecyclerView.ViewHolder v = listView.findViewHolderForLayoutPosition(i);
            if (v == null) {
                continue;
            }
            if (v instanceof RandomTableViewHolder)
                ((RandomTableViewHolder) v).expand(false);
        }
        for (RandomTable t : table.getTables())
            t.setExpanded(true);
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
            default:
                Timber.w("Unknown menu in RandomTableActivity");
                break;
        }
        return super.onOptionsItemSelected(item);
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
        if (!URLUtil.isValidUrl(table.getReference())) {
            MenuItem item = menu.findItem(R.id.random_table_activity_reference);
            item.setVisible(false);
            item.setEnabled(false);
        }
        return true;
    }

    public RecyclerView getListView() {
        return listView;
    }

}

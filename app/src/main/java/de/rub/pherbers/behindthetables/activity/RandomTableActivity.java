package de.rub.pherbers.behindthetables.activity;
//TODO move this to package de.rub.pherbers.behindthetables.activiy

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.IOException;

import de.rub.pherbers.behindthetables.BehindTheTables;
import de.rub.pherbers.behindthetables.R;
import de.rub.pherbers.behindthetables.adapter.RandomTableListAdapter;
import de.rub.pherbers.behindthetables.data.RandomTable;
import de.rub.pherbers.behindthetables.data.TableCollection;
import de.rub.pherbers.behindthetables.data.TableCollectionContainer;
import de.rub.pherbers.behindthetables.data.TableCollectionEntry;
import de.rub.pherbers.behindthetables.data.TableFile;
import de.rub.pherbers.behindthetables.data.TableReader;
import de.rub.pherbers.behindthetables.sql.DBAdapter;
import de.rub.pherbers.behindthetables.view.DividerItemDecoration;
import de.rub.pherbers.behindthetables.view.MyItemAnimator;
import jp.wasabeef.recyclerview.animators.FadeInDownAnimator;
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
                    table = TableReader.readTable(new FileInputStream(tableFile.getFile()));
                } else {
                    table = TableReader.readTable(getResources().openRawResource(tableFile.getResourceID(this)));
                }
                tableCollectionContainer.put(resourceLocation, table);
            } catch (IOException e) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.app_name)
                        .setIcon(R.drawable.ic_warning_black_48dp)
                        .setMessage(getString(R.string.table_file_ioexception, tableFile.getResourceLocation()))
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        }).show();
                return;
            }
        } else {
            table = tableCollectionContainer.get(resourceLocation);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.random_table_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setTitle(table.getTitle());

        listView = (RecyclerView) findViewById(R.id.random_table_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);

        listAdapter = new RandomTableListAdapter(this, table);
        listView.setAdapter(listAdapter);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        listView.addItemDecoration(mDividerItemDecoration);

        RecyclerView.ItemAnimator animator = new MyItemAnimator();
        animator.setAddDuration(100);
        animator.setChangeDuration(100);
        listView.setItemAnimator(animator);



    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void diceRollAction(View mView) {
        for(TableCollectionEntry tce: table.getTables())
            if(tce instanceof RandomTable)
                listAdapter.rollTable((RandomTable) tce);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

    }

    public void scrollToPosition(int pos) {
        listView.smoothScrollToPosition(pos);
    }

    public void actionCollapseAll() {
        for(TableCollectionEntry tce: table.getTables())
            if(tce instanceof RandomTable)
                listAdapter.collapseTable((RandomTable) tce);
    }

    public void actionExpandAll() {
        for(TableCollectionEntry tce: table.getTables())
            if(tce instanceof RandomTable)
                listAdapter.expandTable((RandomTable) tce);
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
        for (int i = 1; i < listAdapter.getItemCount(); i++) {
            if(table.getTables().get(i) instanceof RandomTable) {
                ((RandomTable)table.getTables().get(i)).setRolledIndex(-1);
                redrawListAtPos(i);
            }
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

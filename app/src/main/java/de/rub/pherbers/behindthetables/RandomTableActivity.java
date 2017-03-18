package de.rub.pherbers.behindthetables;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;

import de.rub.pherbers.behindthetables.adapter.RandomTableListAdapter;
import de.rub.pherbers.behindthetables.data.RandomTable;
import de.rub.pherbers.behindthetables.data.TableCollection;
import de.rub.pherbers.behindthetables.data.TableCollectionContainer;
import de.rub.pherbers.behindthetables.data.TableReader;
import de.rub.pherbers.behindthetables.view.RandomTableViewHolder;
import timber.log.Timber;

public class RandomTableActivity extends AppCompatActivity {

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

        TableCollectionContainer tableCollectionContainer = TableCollectionContainer.getTableCollectionContainer();
        if(!tableCollectionContainer.containsKey("asdf")) {
            try {
                table = TableReader.readTable(getResources().openRawResource(R.raw.table_3sgzxb));
                tableCollectionContainer.put("asdf", table);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            table = tableCollectionContainer.get("asdf");
        }

        listView = (RecyclerView) findViewById(R.id.random_table_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);

        listAdapter = new RandomTableListAdapter(this, table);
        listView.setAdapter(listAdapter);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        listView.addItemDecoration(mDividerItemDecoration);

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
        table.rollAllTables();
        for(int i = 0; i  < listAdapter.getItemCount(); i++) {
            redrawListAtPos(i);
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
        for(int i = 0; i < listAdapter.getItemCount(); i++) {
            RecyclerView.ViewHolder v = listView.findViewHolderForLayoutPosition(i);
            if (v == null) {
                continue;
            }
            if(v instanceof RandomTableViewHolder)
                ((RandomTableViewHolder) v).collapse(false);
        }
        for(RandomTable t:table.getTables())
            t.setExpanded(false);
        int firstPos = ((LinearLayoutManager)listView.getLayoutManager()).findFirstVisibleItemPosition();
        int lastPos = ((LinearLayoutManager)listView.getLayoutManager()).findLastVisibleItemPosition();
        if(firstPos > 0) {
            listAdapter.notifyItemRangeChanged(0, firstPos);
        }
        if(lastPos < listAdapter.getItemCount()-1) {
            listAdapter.notifyItemRangeChanged(lastPos+1, listAdapter.getItemCount()-1);
        }
        //listView.smoothScrollToPosition(0);
    }

    public void actionExpandAll() {
        for(int i = 0; i < listAdapter.getItemCount(); i++) {
            RecyclerView.ViewHolder v = listView.findViewHolderForLayoutPosition(i);
            if (v == null) {
                continue;
            }
            if(v instanceof RandomTableViewHolder)
                ((RandomTableViewHolder) v).expand(false);
        }
        for(RandomTable t:table.getTables())
            t.setExpanded(true);
        int firstPos = ((LinearLayoutManager)listView.getLayoutManager()).findFirstVisibleItemPosition();
        int lastPos = ((LinearLayoutManager)listView.getLayoutManager()).findLastVisibleItemPosition();
        if(firstPos > 0) {
            listAdapter.notifyItemRangeChanged(0, firstPos - 1);
        }
        if(lastPos < listAdapter.getItemCount()) {
            listAdapter.notifyItemRangeChanged(lastPos+1, listAdapter.getItemCount());
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
        switch(item.getItemId()) {
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
            default:
                Timber.w("Unknown menu in RandomTableActivity");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.random_table_activity_menu, menu);
        // Disable "Open on reddit" if no reference is given
        if(!URLUtil.isValidUrl(table.getReference())) {
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

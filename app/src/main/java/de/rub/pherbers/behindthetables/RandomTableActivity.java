package de.rub.pherbers.behindthetables;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;

import de.rub.pherbers.behindthetables.adapter.RandomTableListAdapter;
import de.rub.pherbers.behindthetables.data.RandomTable;
import de.rub.pherbers.behindthetables.data.TableCollection;
import de.rub.pherbers.behindthetables.data.TableCollectionContainer;
import de.rub.pherbers.behindthetables.data.TableReader;
import de.rub.pherbers.behindthetables.view.RandomTableView;
import timber.log.Timber;

public class RandomTableActivity extends AppCompatActivity {

    private TableCollection table;
    private ListView listView;
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
                table = TableReader.readTable(getResources().openRawResource(R.raw.table_t4y5pl2));
                tableCollectionContainer.put("asdf", table);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            table = tableCollectionContainer.get("asdf");
        }

        listView = (ListView) findViewById(R.id.random_table_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RandomTableView rtv = (RandomTableView) view;
                Timber.i("Click");
                rtv.toggle();
                //listAdapter.notifyDataSetChanged();
                //listView.postInvalidate();

                // Scroll to expanded list
                listView.smoothScrollToPosition(position);
            }
        });

        listAdapter = new RandomTableListAdapter(this, table);
        listView.setAdapter(listAdapter);

        TextView tv = (TextView) findViewById(R.id.random_table_title);
        tv.setText(table.getTitle());

        TextView desc_tv = (TextView) findViewById(R.id.random_table_desc);
        desc_tv.setText(table.getTitle() + " Description");
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void diceRollAction(View mView) {
        table.rollAllTables();
        redrawList();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

    }

    public void scrollToPosition(int pos) {
        listView.smoothScrollToPosition(pos);
    }

    public void actionCollapseAll() {
        for(int i = 0; i < listView.getChildCount(); i++) {
            View v = listView.getChildAt(i);
            if(v instanceof RandomTableView)
                ((RandomTableView) v).collapse(false);
        }
        for(RandomTable t:table.getTables())
            t.setExpanded(false);
        listView.smoothScrollToPosition(0);
    }

    public void actionExpandAll() {
        for(int i = 0; i < listView.getChildCount(); i++) {
            View v = listView.getChildAt(i);
            if(v instanceof RandomTableView)
                ((RandomTableView) v).expand(false);
        }
        for(RandomTable t:table.getTables())
            t.setExpanded(true);
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
            default:
                Timber.w("Unknown menu in RandomTableActivity");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.random_table_activity_menu, menu);
        return true;
    }
}

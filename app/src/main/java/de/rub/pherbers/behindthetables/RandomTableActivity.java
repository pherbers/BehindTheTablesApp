package de.rub.pherbers.behindthetables;

import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import de.rub.pherbers.behindthetables.adapter.RandomTableListAdapter;
import de.rub.pherbers.behindthetables.data.RandomTable;
import de.rub.pherbers.behindthetables.data.TableCollection;
import de.rub.pherbers.behindthetables.data.TableCollectionContainer;
import de.rub.pherbers.behindthetables.data.TableReader;

public class RandomTableActivity extends AppCompatActivity implements Observer{

    private TableCollection table;
    private ExpandableListView listView;
    private RandomTableListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_table);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TableCollectionContainer tableCollectionContainer = TableCollectionContainer.getTableCollectionContainer();
        if(!tableCollectionContainer.containsKey("asdf")) {
            try {
                table = TableReader.readTable(getResources().openRawResource(R.raw.table_4y5pl2));
                tableCollectionContainer.put("asdf", table);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            table = tableCollectionContainer.get("asdf");
        }

        if (table != null) {
            table.addObserver(this);
        }

        listView = (ExpandableListView) findViewById(R.id.random_table_list);

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
        if (table != null) {
            table.removeObserver(this);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o instanceof RandomTable && arg instanceof Integer) {
            int index = (int) arg;
            listView.collapseGroup(index);
            listAdapter.notifyDataSetChanged();
            listView.postInvalidate();
        }
    }

    public void diceRollAction(View mView) {
        table.rollAllTables();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

    }
}

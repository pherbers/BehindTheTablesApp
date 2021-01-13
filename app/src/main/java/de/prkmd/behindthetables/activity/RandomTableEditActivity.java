package de.prkmd.behindthetables.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.FileInputStream;
import java.io.IOException;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.prkmd.behindthetables.R;
import de.prkmd.behindthetables.adapter.RandomTableEditListAdapter;
import de.prkmd.behindthetables.adapter.RandomTableListAdapter;
import de.prkmd.behindthetables.data.TableCollection;
import de.prkmd.behindthetables.data.TableCollectionContainer;
import de.prkmd.behindthetables.data.TableFile;
import de.prkmd.behindthetables.data.TableReader;
import de.prkmd.behindthetables.imported.wasabeef.MyItemAnimator;
import de.prkmd.behindthetables.sql.DBAdapter;
import de.prkmd.behindthetables.view.DividerItemDecoration;
import timber.log.Timber;

import static de.prkmd.behindthetables.BehindTheTables.APP_TAG;

public class RandomTableEditActivity extends AppCompatActivity {

    public static final String EXTRA_TABLE_DATABASE_RESOURCE_LOCATION = APP_TAG + "extra_table_database_resource_location";


    private TableCollection table;
    private RecyclerView listView;
    private RandomTableEditListAdapter listAdapter;
    private TableFile tableFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_table_edit);

        Intent sourceIntent = getIntent();

        if (sourceIntent.hasExtra(EXTRA_TABLE_DATABASE_RESOURCE_LOCATION)) {
            // Load existing table for editing

            String resourceLocation = sourceIntent.getStringExtra(EXTRA_TABLE_DATABASE_RESOURCE_LOCATION);
            DBAdapter adapter = new DBAdapter(this).open();
            tableFile = TableFile.createFromDB(resourceLocation, adapter);
            adapter.close();

            if (tableFile == null) {
                Timber.e("Failed to obtain a table file from the DB with the location " + resourceLocation + "! Aborting activity!");
                Toast.makeText(this,R.string.error_table_not_found,Toast.LENGTH_LONG).show();
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
                            .setIcon(R.drawable.baseline_warning_dialog_48)
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
        } else {
            // Create empty Table

            table = new TableCollection();
            if(sourceIntent.hasExtra("Category")) {
                table.setCategory(sourceIntent.getStringExtra("Category"));
            }
        }


        Toolbar toolbar = (Toolbar) findViewById(R.id.random_table_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setTitle(table.getTitle());

        listView = findViewById(R.id.random_table_edit_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);

        listAdapter = new RandomTableEditListAdapter(this, table);
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


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.random_table_edit_activity_menu, menu);

        return true;
    }

    @Override
    public void onBackPressed() {
        if(listAdapter.getState() == RandomTableEditListAdapter.STATE.EDIT_TABLE)
            listAdapter.finishEditTable();
        else
            super.onBackPressed();
    }

    public void saveAndExit(View view) {
        // TODO save this
    }
}

package de.prkmd.behindthetables.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.PersistableBundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.gson.JsonParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.prkmd.behindthetables.R;
import de.prkmd.behindthetables.adapter.RandomTableEditListAdapter;
import de.prkmd.behindthetables.data.RandomTable;
import de.prkmd.behindthetables.data.TableCollection;
import de.prkmd.behindthetables.data.TableCollectionContainer;
import de.prkmd.behindthetables.data.TableFile;
import de.prkmd.behindthetables.data.TableIO;
import de.prkmd.behindthetables.data.TableLink;
import de.prkmd.behindthetables.imported.nilsfo.FileManager;
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
                        table = TableIO.readTable(new FileInputStream(tableFile.getFile()));
                    } else {
                        table = TableIO.readTable(getResources().openRawResource(tableFile.getResourceID(this)));
                    }
                    tableCollectionContainer.put(resourceLocation, table);
                } catch (IOException|IllegalStateException|JsonParseException e) {
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
        animator.setAddDuration(50);
        animator.setChangeDuration(100);
        listView.setItemAnimator(animator);

    }

    @Override
    protected void onStop() {
        save();
        super.onStop();
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        save();
        if(listAdapter.getState() == RandomTableEditListAdapter.STATE.EDIT_COLLECTION)
            outState.putInt("adapterState", -1);
        else
            outState.putInt("adapterState", table.getTables().indexOf(listAdapter.getActiveTable()));
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            int savedTableIndex = savedInstanceState.getInt("adapterState");
            if(savedTableIndex >= 0) {
                listAdapter.editTable((RandomTable)table.getTables().get(savedTableIndex));
            }
        }
        super.onRestoreInstanceState(savedInstanceState);
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
        else {
            save();
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void saveAndExit(View view) {
        save();
        onBackPressed();
    }

    public void save() {
        DBAdapter adapter = new DBAdapter(this).open();

        StringBuilder sb = new StringBuilder();
        for(String s: table.getKeywords()) {
            sb.append(s);
            sb.append(';');
        }
        String keywords = sb.toString();

        sb = new StringBuilder();
        for(TableLink tl: table.getUseWithTables()) {
            sb.append(tl.getLinkId());
            sb.append(';');
        }

        if(table.getCategory().isEmpty())
            table.setCategory("My Tables");
        long catId = adapter.existsCategory(table.getCategory());
        if (catId == DBAdapter.CATEGORY_NOT_FOUND) {
            catId = adapter.insertCategory(table.getCategory());
            // TODO Check for any categories without tables and remove them
        }

        FileManager fileManager = new FileManager(this);
        File extDir = fileManager.getExternalTableDir();
        File filepath = new File(extDir, table.getId() + ".json");

        try {
            TableIO.writeTableCollection(table, new FileOutputStream(filepath));
        } catch (IOException e) {
            e.printStackTrace();
        }


        adapter.insertOrUpdateTableCollection(filepath.toString(),
                table.getTitle(),
                table.getDescription(),
                keywords,
                "",
                "",
                catId
                );
        adapter.close();

    }
}

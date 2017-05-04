package de.rub.pherbers.behindthetables.activity;
//TODO move this to package de.rub.pherbers.behindthetables.activiy

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import de.rub.pherbers.behindthetables.BehindTheTables;
import de.rub.pherbers.behindthetables.R;
import de.rub.pherbers.behindthetables.adapter.RandomTableListAdapter;
import de.rub.pherbers.behindthetables.data.RandomTable;
import de.rub.pherbers.behindthetables.data.TableCollection;
import de.rub.pherbers.behindthetables.data.TableCollectionContainer;
import de.rub.pherbers.behindthetables.data.TableCollectionEntry;
import de.rub.pherbers.behindthetables.data.TableFile;
import de.rub.pherbers.behindthetables.data.TableReader;
import de.rub.pherbers.behindthetables.data.io.FileManager;
import de.rub.pherbers.behindthetables.sql.DBAdapter;
import de.rub.pherbers.behindthetables.view.RandomTableViewHolder;
import timber.log.Timber;

import static de.rub.pherbers.behindthetables.R.string.action_externa_file_info_detail;
import static de.rub.pherbers.behindthetables.activity.CategorySelectActivity.EXTERNAL_STORAGE_REQUEST_CODE;

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
        ImageButton btn = (ImageButton) findViewById(R.id.floatingActionButton);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.dice_button_rotator);
        btn.startAnimation(anim);
        //table.rollAllTables();
        for (int i = 0; i < listAdapter.getItemCount() - 1; i++) {
            if (table.getTables().get(i) instanceof RandomTable) {
                RandomTableViewHolder v = (RandomTableViewHolder) listView.findViewHolderForAdapterPosition(i + 1);
                int prev = ((RandomTable) table.getTables().get(i)).getRolledIndex();
                ((RandomTable) table.getTables().get(i)).roll();
                if (v != null) {
                    v.rerollAnimation(prev);
                }
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
        for (TableCollectionEntry t : table.getTables())
            if (t instanceof RandomTable)
                ((RandomTable) t).setExpanded(false);
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
        for (TableCollectionEntry t : table.getTables())
            if (t instanceof RandomTable)
                ((RandomTable) t).setExpanded(true);
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
            case R.id.action_external_file_info:
                requestFileFinfoDialog();
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
            if (table.getTables().get(i) instanceof RandomTable) {
                ((RandomTable) table.getTables().get(i)).setRolledIndex(-1);
                redrawListAtPos(i);
            }
        }
    }

    private void requestFileFinfoDialog() {
        if (!tableFile.isExternal()) {
            Timber.w("File info requested, but the file is not external.");
            return;
        }

        FileManager manager = new FileManager(this);
        if (!manager.hasPermission()) {
            Timber.e("Can't give file info because the app has lost permission!");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    EXTERNAL_STORAGE_REQUEST_CODE);
            return;
        }

        File f = tableFile.getFile();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle(table.getTitle());
        builder.setMessage(getString(R.string.action_externa_file_info_detail, tableFile.getShortExternalPath(this), manager.getFileSizeFormated(f), manager.getFileDateRelative(f)));
        builder.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.setNeutralButton(R.string.action_share_external_file_content, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                File f = tableFile.getFile();
                StringBuilder builder = new StringBuilder();
                try {
                    FileInputStream fstream = new FileInputStream(f);
                    BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
                    String strLine;
                    while ((strLine = br.readLine()) != null) {
                        builder.append(strLine);
                    }
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Timber.e("Failed to read file '" + f.getAbsolutePath() + "'");
                    //TODO exception handling
                    return;
                }

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, builder.toString());
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getString(R.string.action_share_external_file_content)));
            }
        });
        builder.setPositiveButton(R.string.action_share_external_file, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                File f = tableFile.getFile();
                //f = new File("/storage/emulated/0/Documents/Behind%20the%20Tables/Roll%20tables/phb_jsons/backgrounds/template.txt");
                Uri uri = Uri.fromFile(f);
                String fileType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("json");
                if (fileType == null) {
                    Timber.w("Android has found no intent type for JSON files via the 'MimeTypeMap' approach! Reverting to default.");
                    Toast.makeText(RandomTableActivity.this, R.string.error_no_mimetype_for_json, Toast.LENGTH_LONG).show();
                    fileType = "application/json";
                }

                Timber.i("Sharing '" + f.getAbsolutePath() + "' -> URI: " + uri + " -> Type: " + fileType);

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setDataAndType(uri, fileType);

                //MimeTypeMap myMime = MimeTypeMap.getSingleton();
                //Intent intent = new Intent(Intent.ACTION_VIEW);
                //String mimeExtension = myMime.getMimeTypeFromExtension("json");
                //if (mimeExtension != null) {
                //    String mimeType = mimeExtension;
                //    //if (notificationIntent) {
                //        mimeType = mimeExtension.substring(1);
                //    //}
                //    intent.setDataAndType(Uri.fromFile(f), mimeType);
                //    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                //} else{
                //    Timber.e("Faild bc no extension");
                //    return;
                //}

                startActivity(Intent.createChooser(intent, getString(R.string.action_share_external_file)));
                //Intent selector = intent.getSelector();
                //if (selector == null) {
                //    Timber.e("Intent failed!");
                //} else {
                //    startActivity(selector);
                //}
            }
        });

        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.random_table_activity_menu, menu);
        // Disable "Open on reddit" if no reference is given
        // TODO could this be optimised? See code below.
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

        if (!tableFile.isExternal()) {
            menu.removeItem(R.id.action_external_file_info);
        }

        return true;
    }

    public RecyclerView getListView() {
        return listView;
    }

}

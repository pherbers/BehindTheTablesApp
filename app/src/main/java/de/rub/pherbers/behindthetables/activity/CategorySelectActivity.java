package de.rub.pherbers.behindthetables.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashSet;

import de.rub.pherbers.behindthetables.BehindTheTables;
import de.rub.pherbers.behindthetables.R;
import de.rub.pherbers.behindthetables.data.TableFile;
import de.rub.pherbers.behindthetables.sql.DBAdapter;
import timber.log.Timber;

public class CategorySelectActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private CategoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_select);

        GridView gridView = (GridView) findViewById(R.id.category_list_view);
        adapter = new CategoryAdapter(this);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Timber.i("User clicked on category in position " + i);
        Bundle b = adapter.getTableInfo(i);

        requestTableActivity(b);
    }

    public void requestTableActivity(Bundle args) {
        Intent intent = new Intent(this, TableSelectActivity.class);
        intent.putExtras(args);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_table_settings:
                //TODO Settings?
                break;
            case R.id.action_debug_reset_db:
                DBAdapter adapter = new DBAdapter(this).open();
                adapter.fillWithDefaultData(this);
                adapter.close();
                break;
            case R.id.action_clar_favs:
                requestClearFavs();
                break;
            case R.id.action_category_settings:
                //TODO Settings?
                break;
            default:
                Timber.w("Unknown menu item selected.");
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_category_select, menu);

        if (!BehindTheTables.isDebugBuild()) {
            menu.removeItem(R.id.action_debug_reset_db);
        }

        return true;
    }

    private void requestClearFavs() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int count = preferences.getStringSet(TableFile.PREFS_FAVORITE_TABLES, new HashSet<String>()).size();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_star_black_48dp);
        builder.setTitle(R.string.app_name);
        builder.setMessage(getString(R.string.action_clear_favs_detail, count));
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.setPositiveButton(R.string.action_clear_favs, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                BehindTheTables.clearFavs(CategorySelectActivity.this);
            }
        });
        builder.show();
    }

    class CategoryAdapter extends BaseAdapter {

        private Context context;
        private Cursor bufferedCategories;

        public CategoryAdapter(Context context) {
            this.context = context;
            DBAdapter adapter = new DBAdapter(context);

            adapter.open();
            bufferedCategories = adapter.getAllCategories(DBAdapter.KEY_CATEGORY_TITLE);
            adapter.close();

            Timber.i("Displaying category tiles. Count: " + getCount());
        }

        public Bundle getTableInfo(int position) {
            Bundle b = new Bundle();

            if (bufferedCategories.moveToPosition(position)) {
                b.putLong(TableSelectActivity.EXTRA_CATEGORY_DISCRIMINATOR, bufferedCategories.getLong(DBAdapter.COL_CATEGORY_ROWID));
            }
            if (isCategoryFavs(position)) {
                b.putBoolean(TableSelectActivity.EXTRA_FAVS_ONLY, true);
            }

            return b;
        }

        @Override
        public int getCount() {
            return bufferedCategories.getCount() + 2;
        }

        public boolean isCategoryAll(int i) {
            return i == bufferedCategories.getCount();
        }

        public boolean isCategoryFavs(int i) {
            return i == bufferedCategories.getCount() + 1;
        }

        @Override
        public String getItem(int i) {
            Timber.v("Category item requested at position " + i);

            if (isCategoryAll(i)) return context.getString(R.string.category_all);
            if (isCategoryFavs(i)) return context.getString(R.string.category_favs);

            if (bufferedCategories.moveToPosition(i)) {
                return bufferedCategories.getString(DBAdapter.COL_CATEGORY_TITLE);
            }

            Timber.w("No category found at the position " + i);
            return context.getString(R.string.error_unknown);
        }

        @Override
        public long getItemId(int i) {
            if (bufferedCategories.moveToPosition(i)) {
                return bufferedCategories.getLong(DBAdapter.COL_CATEGORY_ROWID);
            }
            return DBAdapter.CATEGORY_NOT_FOUND;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            TextView tv = new TextView(context);
            //button.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
            tv.setGravity(Gravity.CENTER);
            tv.setTextAppearance(context, android.R.style.TextAppearance_Large);
            tv.setBackground(context.getResources().getDrawable(R.drawable.category_select_button_color));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 200);
            tv.setLayoutParams(layoutParams);
            String text = getItem(i);

            Timber.i("Displaying category button on position " + i + " -> '" + text + "'.");
            tv.setText(getItem(i));

            return tv;
        }
    }
}

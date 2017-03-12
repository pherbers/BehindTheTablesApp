package de.rub.pherbers.behindthetables.adapter;

import android.content.Context;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;


import de.rub.pherbers.behindthetables.R;
import de.rub.pherbers.behindthetables.data.RandomTable;
import de.rub.pherbers.behindthetables.data.TableCollection;
import de.rub.pherbers.behindthetables.data.TableEntry;
import de.rub.pherbers.behindthetables.view.RandomTableView;
import timber.log.Timber;

/**
 * Created by Patrick on 11.03.2017.
 */

public class RandomTableListAdapter extends BaseAdapter {

    private TableCollection tableCollection;
    private Context context;

    public RandomTableListAdapter(Context context, TableCollection tableCollection) {
        this.context = context;
        this.tableCollection = tableCollection;
    }

    @Override
    public int getCount() {
        return tableCollection.getTables().size();
    }

    @Override
    public RandomTable getItem(int position) {
        return tableCollection.getTables().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RandomTableView view;
        if (convertView != null) {
            view = (RandomTableView) convertView;
            Timber.i("Reused a view!");
        } else {
            RandomTable group = getItem(position);
            view = new RandomTableView(context, parent, group);
        }
        return view;
    }

    @Override
    public int getItemViewType(int position) {
        return IGNORE_ITEM_VIEW_TYPE;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return tableCollection.getTables().isEmpty();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }
}

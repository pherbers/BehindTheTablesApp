package de.rub.pherbers.behindthetables.adapter;

import android.content.Context;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;


import de.rub.pherbers.behindthetables.R;
import de.rub.pherbers.behindthetables.data.TableCollection;
import de.rub.pherbers.behindthetables.view.RandomTableHeaderViewHolder;
import de.rub.pherbers.behindthetables.view.RandomTableViewHolder;

/**
 * Created by Patrick on 11.03.2017.
 */

public class RandomTableListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private TableCollection tableCollection;
    private Context context;

    private static int VIEW_HEADER = 0;
    private static int VIEW_ENTRY = 1;

    public RandomTableListAdapter(Context context, TableCollection tableCollection) {
        this.context = context;
        this.tableCollection = tableCollection;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return tableCollection.getTables().size() + 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder v = null;
        if(viewType == VIEW_ENTRY) {
            v = new RandomTableViewHolder(context, parent);
        } else if(viewType == VIEW_HEADER) {
            v = new RandomTableHeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.table_info_layout, parent, false));
        }
        return v;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_HEADER;
        }
        return VIEW_ENTRY;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(position > 0) {
            ((RandomTableViewHolder)holder).bindData(tableCollection.getTables().get(position-1));
        } else
            ((RandomTableHeaderViewHolder)holder).bindData(tableCollection);
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        if(holder instanceof RandomTableViewHolder) {
            ((RandomTableViewHolder)holder).clearAnimation();
        }
    }

}

package de.rub.pherbers.behindthetables.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.LinearLayout;


import de.rub.pherbers.behindthetables.data.TableCollection;
import de.rub.pherbers.behindthetables.view.RandomTableViewHolder;

/**
 * Created by Patrick on 11.03.2017.
 */

public class RandomTableListAdapter extends RecyclerView.Adapter<RandomTableViewHolder> {

    private TableCollection tableCollection;
    private Context context;

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
        return tableCollection.getTables().size();
    }

    @Override
    public RandomTableViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RandomTableViewHolder v = new RandomTableViewHolder(context, parent);
        return v;
    }

    @Override
    public void onBindViewHolder(RandomTableViewHolder holder, int position) {
        holder.bindData(tableCollection.getTables().get(position));
    }

    @Override
    public void onViewDetachedFromWindow(RandomTableViewHolder holder) {
        holder.clearAnimation();
    }

}

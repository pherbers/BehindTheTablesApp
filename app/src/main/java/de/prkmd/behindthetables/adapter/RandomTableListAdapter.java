package de.prkmd.behindthetables.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;

import de.prkmd.behindthetables.R;
import de.prkmd.behindthetables.data.RandomTable;
import de.prkmd.behindthetables.data.SubcategoryEntry;
import de.prkmd.behindthetables.data.TableCollection;
import de.prkmd.behindthetables.data.TableCollectionEntry;
import de.prkmd.behindthetables.data.TableEntry;
import de.prkmd.behindthetables.view.RandomTableEntryViewHolder;
import de.prkmd.behindthetables.view.RandomTableHeaderViewHolder;
import de.prkmd.behindthetables.view.RandomTableSubcategoryViewHolder;
import de.prkmd.behindthetables.view.RandomTableViewHolder;

/**
 * Created by Patrick on 11.03.2017.
 */

public class RandomTableListAdapter extends RecyclerView.Adapter<ViewHolder> {

    private TableCollection tableCollection;
    private ArrayList<ListItem> flatRepr;
    private Context context;

    private final static int VIEW_HEADER = 0;
    private final static int VIEW_SUBCATEGORY = 1;
    private final static int VIEW_TABLE_TITLE = 2;
    private final static int VIEW_TABLE_ENTRY = 3;

    public RandomTableListAdapter(Context context, TableCollection tableCollection) {
        this.context = context;
        this.tableCollection = tableCollection;
        flatRepr = new ArrayList<>();
        rebuildArray();
        setHasStableIds(true);
    }

    private void rebuildArray() {
        flatRepr.clear();
        // First item is always the description, represented by the table collection itself
        flatRepr.add(new DescriptionItem(tableCollection));

        for(TableCollectionEntry collectionEntry: tableCollection.getTables()) {
            if(collectionEntry instanceof RandomTable) {
                RandomTable randomTable = (RandomTable) collectionEntry;
                flatRepr.add(new TableTitleItem(randomTable));
                if((randomTable).isExpanded())
                    for(TableEntry tableEntry: (randomTable).getEntries()) {
                        flatRepr.add(new TableEntryItem(tableEntry, randomTable));
                    }
                else if((randomTable).getRolledIndex() >= 0)
                    flatRepr.add(new TableEntryItem((randomTable).getEntries().get((randomTable).getRolledIndex()), randomTable));
            } else if(collectionEntry instanceof SubcategoryEntry) {
                flatRepr.add(new SubcategoryItem((SubcategoryEntry) collectionEntry));
            }
        }
    }

    public void collapseTable(RandomTable table) {
        if(!table.isExpanded())
            return;

        int index = getTablePos(table);

        if(index == -1)
            return;
        int indexTo = getTableEntryRange(index);

        int rollIndex = table.getRolledIndex();
        if(rollIndex < 0) {
            // If no rolled item exists, just remove all
            flatRepr.subList(index + 1, indexTo + 1).clear();

            notifyItemRangeRemoved(index + 1, indexTo - index);
        } else {
            int globalRollIndex = rollIndex + index + 1;
            flatRepr.subList(globalRollIndex + 1, indexTo + 1).clear();
            notifyItemRangeRemoved(globalRollIndex + 1, indexTo - globalRollIndex);
            flatRepr.subList(index + 1, globalRollIndex).clear();
            notifyItemRangeRemoved(index + 1, globalRollIndex - index - 1);
        }

        table.setExpanded(false);
    }

    public void expandTable(RandomTable table) {
        if(table.isExpanded())
            return;
        int index = getTablePos(table);
        if(index == -1)
            return;

        ArrayList<ListItem> tableEntryItems = new ArrayList<>(table.getEntries().size());
        for(TableEntry tableEntry: table.getEntries()) {
            tableEntryItems.add(new TableEntryItem(tableEntry, table));
        }
        int rollIndex = table.getRolledIndex();
        if(rollIndex < 0) {
            flatRepr.addAll(index + 1, tableEntryItems);

            notifyItemRangeInserted(index + 1, tableEntryItems.size());
        } else {
            // Insert after rolled entry
            flatRepr.addAll(index + 2, tableEntryItems.subList(rollIndex + 1, tableEntryItems.size()));

            // Insert before rolled entry
            flatRepr.addAll(index  + 1, tableEntryItems.subList(0, rollIndex));

            // Notify adapter
            notifyItemRangeInserted(index + 1, rollIndex);
            notifyItemRangeInserted(index + rollIndex + 2, tableEntryItems.size() - rollIndex - 1);
        }

        table.setExpanded(true);
    }

    public void rollTable(RandomTable table) {
        int prev = table.getRolledIndex();
        table.roll();
        int index = getTablePos(table);
        if(table.isExpanded()) {
            if (index == -1)
                return;
            notifyItemChanged(index + table.getRolledIndex() + 1);
            if (prev >= 0)
                notifyItemChanged(index + prev + 1);
        } else if(prev < 0) {
            // No previous item available, add a new one with the rolled index
            flatRepr.add(index + 1, new TableEntryItem(table.getEntries().get(table.getRolledIndex()), table));
            notifyItemInserted(index + 1);
        } else {
            // Table is not expanded, but the view is already present
            flatRepr.set(index + 1, new TableEntryItem(table.getEntries().get(table.getRolledIndex()), table));
            notifyItemChanged(index + 1);
        }
    }

    public void setRolledIndex(RandomTable table, TableEntry tableEntry) {
        int prev = table.getRolledIndex();
        int index = getTablePos(table);
        table.setRolledIndex(tableEntry.getEntryPosition());

        notifyItemChanged(index + table.getRolledIndex() + 1);
        if(prev >= 0)
            notifyItemChanged(index + prev + 1);
    }

    private int getTablePos(RandomTable table) {
        for(int i = 0; i < flatRepr.size(); i++) {
            ListItem li = flatRepr.get(i);
            if(li instanceof TableTitleItem && li.equals(table))
                return i;
        }
        return -1;
    }

    private int getTableEntryRange(int index) {
        int i = index + 1;
        while(i < flatRepr.size()) {
            ListItem li = flatRepr.get(i);
            if(!(li instanceof TableEntryItem))
                break;
            i++;
        }
        return i - 1;
    }

    @Override
    public long getItemId(int position) {
        return flatRepr.get(position).getID();
    }

    @Override
    public int getItemCount() {
        return flatRepr.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder v = null;
        if(viewType == VIEW_TABLE_TITLE) {
            v = createRandomTableViewHolder(context, parent);
        } else if(viewType == VIEW_HEADER) {
            v = createRandomTableHeaderViewHolder(context, parent);
        } else if(viewType == VIEW_SUBCATEGORY) {
            v = createRandomTableSubcategoryViewHolder(context, parent);
        } else if(viewType == VIEW_TABLE_ENTRY) {
            v = createRandomTableEntryViewHolder(context, parent);
        }
        return v;
    }

    @Override
    public int getItemViewType(int position) {
        return flatRepr.get(position).getViewType();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        flatRepr.get(position).bindData(holder);
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {

    }

    private RandomTableHeaderViewHolder createRandomTableHeaderViewHolder(Context context, ViewGroup parent) {
        return new RandomTableHeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.table_info_layout, parent, false));
    }
    private RandomTableSubcategoryViewHolder createRandomTableSubcategoryViewHolder(Context context, ViewGroup parent) {
        return new RandomTableSubcategoryViewHolder(LayoutInflater.from(context).inflate(R.layout.table_subcategory_layout, parent, false));
    }
    private RandomTableViewHolder createRandomTableViewHolder(Context context, ViewGroup parent) {
        return new RandomTableViewHolder(context, parent, this);
    }
    private RandomTableEntryViewHolder createRandomTableEntryViewHolder(Context context, ViewGroup parent) {
        return new RandomTableEntryViewHolder(LayoutInflater.from(context).inflate(R.layout.table_entry_layout, parent, false), this);
    }


    private interface ListItem {
        int getViewType();
        void bindData(ViewHolder viewHolder);
        int getID();
    }

    private class DescriptionItem implements ListItem {

        TableCollection tableCollection;

        DescriptionItem(TableCollection tableCollection) {
            this.tableCollection = tableCollection;
        }

        @Override
        public int getViewType() {
            return VIEW_HEADER;
        }

        @Override
        public void bindData(ViewHolder viewHolder) {
            ((RandomTableHeaderViewHolder)viewHolder).bindData(tableCollection);
        }

        @Override
        public int getID() {
            return tableCollection.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof TableCollection)
                return obj.equals(tableCollection);
            return super.equals(obj);
        }
    }

    private class SubcategoryItem implements ListItem {

        SubcategoryEntry subcategoryEntry;

        SubcategoryItem(SubcategoryEntry subcategoryEntry) {
            this.subcategoryEntry = subcategoryEntry;
        }

        @Override
        public int getViewType() {
            return VIEW_SUBCATEGORY;
        }

        @Override
        public void bindData(ViewHolder viewHolder) {
            ((RandomTableSubcategoryViewHolder)viewHolder).bindData(subcategoryEntry);
        }

        @Override
        public int getID() {
            return subcategoryEntry.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof SubcategoryEntry)
                return obj.equals(subcategoryEntry);
            return super.equals(obj);
        }
    }

    private class TableTitleItem implements ListItem {

        RandomTable randomTable;

        TableTitleItem(RandomTable randomTable) {
            this.randomTable = randomTable;
        }
        @Override
        public int getViewType() {
            return VIEW_TABLE_TITLE;
        }

        @Override
        public void bindData(ViewHolder viewHolder) {
            ((RandomTableViewHolder)viewHolder).bindData(randomTable);
        }

        @Override
        public int getID() {
            return randomTable.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof RandomTable)
                return obj.equals(randomTable);
            return super.equals(obj);
        }
    }

    private class TableEntryItem implements ListItem {
        TableEntry tableEntry;
        RandomTable table;

        TableEntryItem(TableEntry tableEntry, RandomTable table) {
            this.tableEntry = tableEntry;
            this.table = table;
        }

        @Override
        public int getViewType() {
            return VIEW_TABLE_ENTRY;
        }

        @Override
        public void bindData(ViewHolder viewHolder) {
            ((RandomTableEntryViewHolder)viewHolder).bindData(tableEntry, table);
        }

        @Override
        public int getID() {
            return tableEntry.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof TableEntry)
                return obj.equals(tableEntry);
            return super.equals(obj);
        }
    }

}

package de.prkmd.behindthetables.adapter;

import android.content.Context;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.prkmd.behindthetables.data.RandomTable;
import de.prkmd.behindthetables.data.SubcategoryEntry;
import de.prkmd.behindthetables.data.TableCollection;
import de.prkmd.behindthetables.data.TableCollectionEntry;
import de.prkmd.behindthetables.data.TableEntry;

public abstract class TreeListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public final static int VIEW_HEADER = 0;
    public final static int VIEW_SUBCATEGORY = 1;
    public final static int VIEW_TABLE_TITLE = 2;
    public final static int VIEW_TABLE_ENTRY = 3;

    protected ArrayList<ListItem> flatRepr;
    protected TableCollection tableCollection;
    protected Context context;


    public TreeListAdapter(Context context, TableCollection tableCollection) {
        this.context = context;
        this.tableCollection = tableCollection;
        flatRepr = new ArrayList<>();
        rebuildArray();
        setHasStableIds(true);
    }

    protected void rebuildArray() {
        flatRepr.clear();
        // First item is always the description, represented by the table collection itself
        flatRepr.add(new RandomTableListAdapter.DescriptionItem(tableCollection));

        for(TableCollectionEntry collectionEntry: tableCollection.getTables()) {
            if(collectionEntry instanceof RandomTable) {
                RandomTable randomTable = (RandomTable) collectionEntry;
                flatRepr.add(new RandomTableListAdapter.TableTitleItem(randomTable));
                if((randomTable).isExpanded())
                    for(TableEntry tableEntry: (randomTable).getEntries()) {
                        flatRepr.add(new RandomTableListAdapter.TableEntryItem(tableEntry, randomTable));
                    }
                else if((randomTable).getRolledIndex() >= 0)
                    flatRepr.add(new RandomTableListAdapter.TableEntryItem((randomTable).getEntries().get((randomTable).getRolledIndex()), randomTable));
            } else if(collectionEntry instanceof SubcategoryEntry) {
                flatRepr.add(new RandomTableListAdapter.SubcategoryItem((SubcategoryEntry) collectionEntry));
            }
        }
    }

    protected int getTablePos(RandomTable table) {
        for(int i = 0; i < flatRepr.size(); i++) {
            ListItem li = flatRepr.get(i);
            if(li instanceof RandomTableListAdapter.TableTitleItem && li.equals(table))
                return i;
        }
        return -1;
    }

    protected int getTableEntryRange(int index) {
        int i = index + 1;
        while(i < flatRepr.size()) {
            ListItem li = flatRepr.get(i);
            if(!(li instanceof RandomTableListAdapter.TableEntryItem))
                break;
            i++;
        }
        return i - 1;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        flatRepr.get(position).bindData(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {

    }

    @Override
    public long getItemId(int position) {
        return flatRepr.get(position).getID();
    }

    @Override
    public int getItemViewType(int position) {
        return flatRepr.get(position).getViewType();
    }

    @Override
    public int getItemCount() {
        return flatRepr.size();
    }

    protected interface ListItem {
        int getViewType();
        void bindData(RecyclerView.ViewHolder viewHolder);
        int getID();
    }
}

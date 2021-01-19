package de.prkmd.behindthetables.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.prkmd.behindthetables.R;
import de.prkmd.behindthetables.data.RandomTable;
import de.prkmd.behindthetables.data.SubcategoryEntry;
import de.prkmd.behindthetables.data.TableCollection;
import de.prkmd.behindthetables.data.TableCollectionEntry;
import de.prkmd.behindthetables.data.TableEntry;
import de.prkmd.behindthetables.view.RandomTableEditAddButtonViewHolder;
import de.prkmd.behindthetables.view.RandomTableEditEntryViewHolder;
import de.prkmd.behindthetables.view.RandomTableEditViewHolder;
import de.prkmd.behindthetables.view.RandomTableEntryViewHolder;
import de.prkmd.behindthetables.view.RandomTableHeaderViewHolder;
import de.prkmd.behindthetables.view.RandomTableSubcategoryViewHolder;
import de.prkmd.behindthetables.view.RandomTableViewHolder;
import timber.log.Timber;

public class RandomTableEditListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected TableCollection tableCollection;
    protected Context context;
    protected RandomTable activeTable;

    public enum STATE {
        EDIT_COLLECTION, EDIT_TABLE
    }

    protected STATE state = STATE.EDIT_COLLECTION;

    public final static int VIEW_HEADER = 0;
    public final static int VIEW_SUBCATEGORY = 1;
    public final static int VIEW_TABLE_TITLE = 2;
    public final static int VIEW_TABLE_ENTRY = 3;
    public final static int VIEW_ADD_BTN = 4;

    public RandomTableEditListAdapter(Context context, TableCollection tableCollection) {
        this.context = context;
        this.tableCollection = tableCollection;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder v = null;
        if(viewType == VIEW_TABLE_TITLE) {
            v = createRandomTableViewHolder(context, parent);
        } else if(viewType == VIEW_HEADER) {
            v = createRandomTableHeaderViewHolder(context, parent);
        } else if(viewType == VIEW_SUBCATEGORY) {
            v = createRandomTableSubcategoryViewHolder(context, parent);
        } else if(viewType == VIEW_TABLE_ENTRY) {
            v = createRandomTableEntryViewHolder(context, parent);
        } else if(viewType == VIEW_ADD_BTN) {
            v = createRandomTableAddBtnViewHolder(context, parent);
        }
        return v;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(state == STATE.EDIT_COLLECTION) {
            if(position == 0) {
                ((RandomTableHeaderViewHolder)holder).bindData(tableCollection);
            } else if(position == tableCollection.getTables().size() + 1) {
                // Do nothing
            } else {
                TableCollectionEntry entry = tableCollection.getTables().get(position - 1);
                if(entry instanceof RandomTable) {
                    ((RandomTableEditViewHolder)holder).bindData((RandomTable) entry, this);
                } else if (entry instanceof SubcategoryEntry) {
                    ((RandomTableSubcategoryViewHolder)holder).bindData((SubcategoryEntry) entry);
                }
            }
        } else {
            if(position == 0) {
                ((RandomTableEditViewHolder)holder).bindData(activeTable, this);
            } else if(position == activeTable.getEntries().size() + 1) {
                // Do nothing
            } else {
                TableEntry entry = activeTable.getEntries().get(position - 1);
                ((RandomTableEditEntryViewHolder)holder).bindData(entry, activeTable);
            }
        }
    }

    @Override
    public long getItemId(int position) {
        if(state == STATE.EDIT_COLLECTION) {
            if(position == 0) {
                return tableCollection.getId().hashCode();
            } else if(position == tableCollection.getTables().size() + 1) {
                return VIEW_ADD_BTN;
            } else {
                TableCollectionEntry entry = tableCollection.getTables().get(position - 1);
                return entry.hashCode();
            }
        } else {
            if(position == 0) {
                return activeTable.hashCode();
            } else if(position == activeTable.getEntries().size() + 1) {
                return VIEW_ADD_BTN;
            } else {
                TableEntry entry = activeTable.getEntries().get(position - 1);
                return entry.hashCode();
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(state == STATE.EDIT_COLLECTION) {
            if(position == 0) {
                return VIEW_HEADER;
            } else if(position == tableCollection.getTables().size() + 1) {
                return VIEW_ADD_BTN;
            }
            else {
                TableCollectionEntry entry = tableCollection.getTables().get(position - 1);
                if(entry instanceof RandomTable) {
                    return VIEW_TABLE_TITLE;
                } else if (entry instanceof SubcategoryEntry) {
                    return VIEW_SUBCATEGORY;
                }
            }
        } else {
            if(position == 0) {
                return VIEW_TABLE_TITLE;
            } else if(position == activeTable.getEntries().size() + 1) {
                return VIEW_ADD_BTN;
            } else {
                return VIEW_TABLE_ENTRY;
            }
        }
        return 0;
    }


    private RecyclerView.ViewHolder createRandomTableHeaderViewHolder(Context context, ViewGroup parent) {
        return new RandomTableHeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.table_info_layout, parent, false));
    }

    private RecyclerView.ViewHolder createRandomTableSubcategoryViewHolder(Context context, ViewGroup parent) {
        return new RandomTableSubcategoryViewHolder(LayoutInflater.from(context).inflate(R.layout.table_subcategory_layout, parent, false));
    }

    private RandomTableEditViewHolder createRandomTableViewHolder(Context context, ViewGroup parent) {
        return new RandomTableEditViewHolder(LayoutInflater.from(context).inflate(R.layout.table_group_edit_layout, parent, false));
    }

    private RandomTableEditEntryViewHolder createRandomTableEntryViewHolder(Context context, ViewGroup parent) {
        return new RandomTableEditEntryViewHolder(LayoutInflater.from(context).inflate(R.layout.table_entry_edit_layout, parent, false), this);
    }

    private RandomTableEditAddButtonViewHolder createRandomTableAddBtnViewHolder(Context context, ViewGroup parent) {
        return new RandomTableEditAddButtonViewHolder(LayoutInflater.from(context).inflate(R.layout.table_edit_add_button, parent, false), this);
    }

    public void editTable(RandomTable table) {
        if(!tableCollection.getTables().contains(table)) {
            Timber.e("Unknown table " + table + " in active Table Collection " + tableCollection);
            return;
        }

        activeTable = table;
        state = STATE.EDIT_TABLE;

        notifyDataSetChanged();
    }

    public void finishEditTable() {
        activeTable.trim();
        activeTable.fixDiceValues();
        activeTable = null;
        state = STATE.EDIT_COLLECTION;

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if(state == STATE.EDIT_COLLECTION) {
            return tableCollection.getTables().size() + 2;
        } else {
            return activeTable.size() + 2;
        }
    }

    public void addNewTable() {
        if(state == STATE.EDIT_COLLECTION) {
            String new_table_string = context.getString(R.string.new_empty_table_text);
            tableCollection.addNewTable(new_table_string);
            notifyItemInserted(getItemCount() - 2);
        } else {
            throw new IllegalStateException("Tried to add a new random table, but table editor is in state " + state.toString());
        }
    }

    public void addNewTableEntry() {
        if(state == STATE.EDIT_TABLE) {
            activeTable.addNewEntry();
            notifyItemInserted(getItemCount() - 2);
        } else {
            throw new IllegalStateException("Tried to add a new random table, but table editor is in state " + state.toString());
        }
    }

    public STATE getState() {
        return state;
    }

    public TableCollection getTableCollection() {
        return tableCollection;
    }

    public RandomTable getActiveTable() {
        if(state == STATE.EDIT_TABLE)
            return activeTable;
        else
            return null;
    }
}

package de.prkmd.behindthetables.view;

import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.widget.TextView;

import de.prkmd.behindthetables.R;
import de.prkmd.behindthetables.adapter.RandomTableListAdapter;
import de.prkmd.behindthetables.data.RandomTable;
import de.prkmd.behindthetables.data.TableEntry;

/**
 * Created by Patrick on 04.05.2017.
 */

public class RandomTableEntryViewHolder extends ViewHolder implements View.OnClickListener, View.OnLongClickListener {
    private RandomTable table;
    private TableEntry tableEntry;
    private RandomTableListAdapter adapter;
    public RandomTableEntryViewHolder(View itemView, RandomTableListAdapter adapter) {
        super(itemView);
        this.adapter = adapter;
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    public void bindData(TableEntry tableEntry, RandomTable table) {
        this.table = table;
        this.tableEntry = tableEntry;

        TextView textentry = (TextView) itemView.findViewById(R.id.table_entry_text);
        TextView diceentry = (TextView) itemView.findViewById(R.id.table_entry_dice_value);
        textentry.setText(tableEntry.getText());
        setDiceEntry(diceentry, tableEntry);

        updateColor();
    }

    public void updateColor() {
        final int childPosition = this.tableEntry.getEntryPosition();

        if(this.table.getRolledIndex() == childPosition) {
            itemView.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.colorTableHighlight));
            //TODO: Idea: Write selected item text in bold?
        } else if (childPosition % 2 == 0) {
            itemView.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.colorTableEven));
        } else {
            itemView.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.colorTableOdd));
        }
    }

    private void setDiceEntry(TextView diceentry, TableEntry te) {
        if(te.getDiceValueTo() < 0)
            diceentry.setText(itemView.getContext().getString(R.string.dice_entry_string, te.getDiceValue()));
        else {
            diceentry.setText(itemView.getContext().getString(R.string.dice_entry_from_to_string, te.getDiceValue(), te.getDiceValueTo()));

            // We have to scale the text down a bit, otherwise it wont fit :(
            if(te.getDiceValueTo() > 9)
                diceentry.setTextScaleX(0.8f);
        }

    }

    @Override
    public void onClick(View v) {
        if(table.isExpanded())
            adapter.collapseTable(table);
        else
            adapter.expandTable(table);
    }

    @Override
    public boolean onLongClick(View v) {
        adapter.setRolledIndex(table, tableEntry);
        return true;
    }

    public boolean isRolledEntry() {
        return table.getRolledIndex() == tableEntry.getEntryPosition();
    }

    public TableEntry getTableEntry() {
        return tableEntry;
    }
}

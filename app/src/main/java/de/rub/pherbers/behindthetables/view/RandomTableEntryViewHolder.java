package de.rub.pherbers.behindthetables.view;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.widget.TextView;

import de.rub.pherbers.behindthetables.R;
import de.rub.pherbers.behindthetables.adapter.RandomTableListAdapter;
import de.rub.pherbers.behindthetables.data.RandomTable;
import de.rub.pherbers.behindthetables.data.TableEntry;

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

        final int childPosition = tableEntry.getEntryPosition();

        if(table.getRolledIndex() == childPosition) {
            itemView.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.colorTableHighlight));
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

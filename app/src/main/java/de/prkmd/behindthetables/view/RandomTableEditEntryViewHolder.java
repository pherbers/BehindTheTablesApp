package de.prkmd.behindthetables.view;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.prkmd.behindthetables.R;
import de.prkmd.behindthetables.adapter.RandomTableEditListAdapter;
import de.prkmd.behindthetables.data.RandomTable;
import de.prkmd.behindthetables.data.TableEntry;
import timber.log.Timber;

import static androidx.core.content.ContextCompat.getSystemService;

public class RandomTableEditEntryViewHolder extends RecyclerView.ViewHolder {
    private RandomTable table;
    private TableEntry tableEntry;
    private RandomTableEditListAdapter adapter;
    private EditText editText;

    public RandomTableEditEntryViewHolder(View itemView, RandomTableEditListAdapter adapter) {
        super(itemView);
        this.adapter = adapter;
//        itemView.setOnClickListener(this);
    }

    public void bindData(TableEntry tableEntry, RandomTable table) {
        this.table = table;
        this.tableEntry = tableEntry;

        editText = (EditText) itemView.findViewById(R.id.table_edit_entry_text);
        TextView diceentry = (TextView) itemView.findViewById(R.id.table_entry_dice_value);

        final int childPosition = tableEntry.getEntryPosition();
        setDiceEntry(diceentry, tableEntry);

        if(table.getRolledIndex() == childPosition) {
            itemView.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.colorTableHighlight));
        } else if (childPosition % 2 == 0) {
            itemView.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.colorTableEven));
        } else {
            itemView.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.colorTableOdd));
        }


        editText.setText(tableEntry.getText());
        if(table.getEntries().get(table.getEntries().size() - 1) == tableEntry) {
            requestFocus();
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

    public void onClick(View v) {
        Timber.d("Clicked on %s", v);
        //if(v.getId() == R.id.table_edit_entry_text) {
        //setEditMode(true);
        //} else if(v.getId() == R.id.table_edit_entry_button) {
        //    openEditDialogue();
        //}
    }

    public boolean onLongClick(View v) {
        openEditDialogue();
        return true;
    }

    public void openEditDialogue() {
        // TODO
    }

    public void requestFocus() {
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();

        InputMethodManager imm = (InputMethodManager) itemView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        editText.setSelection(0);
    }


    //editText.requestFocus();

}

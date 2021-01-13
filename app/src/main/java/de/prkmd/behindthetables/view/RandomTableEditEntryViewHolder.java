package de.prkmd.behindthetables.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import de.prkmd.behindthetables.R;
import de.prkmd.behindthetables.adapter.RandomTableEditListAdapter;
import de.prkmd.behindthetables.data.RandomTable;
import de.prkmd.behindthetables.data.TableEntry;
import timber.log.Timber;

public class RandomTableEditEntryViewHolder extends RecyclerView.ViewHolder {
    private RandomTable table;
    private TableEntry tableEntry;
    private RandomTableEditListAdapter adapter;
    private EditText editText;

    private ItemTextWatcher textWatcher;

    public RandomTableEditEntryViewHolder(View itemView, RandomTableEditListAdapter adapter) {
        super(itemView);
        this.adapter = adapter;


        editText = (EditText) itemView.findViewById(R.id.table_edit_entry_text);
        textWatcher = new ItemTextWatcher();
        editText.addTextChangedListener(textWatcher);
//        itemView.setOnClickListener(this);
    }

    public void bindData(TableEntry tableEntry, RandomTable table) {
        this.table = table;
        this.tableEntry = tableEntry;

        TextView diceentry = (TextView) itemView.findViewById(R.id.table_entry_dice_value);

        final int childPosition = tableEntry.getEntryPosition();
        setDiceEntry(diceentry, tableEntry);

        if (childPosition % 2 == 0) {
            itemView.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.colorTableEven));
        } else {
            itemView.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.colorTableOdd));
        }

        textWatcher.isLast = childPosition == table.getEntries().size() - 1;

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
        editText.requestFocus();

        InputMethodManager imm = (InputMethodManager) itemView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        editText.setSelection(0);
    }

    private class ItemTextWatcher implements TextWatcher {
        public boolean isLast = false;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {

            tableEntry.setText(s.toString());

            if(s.length() == 0)
                return;

            if(s.charAt(s.length() - 1) == '\n') {

                s.delete(s.length() - 1, s.length());
                Timber.d("Newline detected: %s", s);
                if(isLast) {
                    table.addNewEntry();
                    adapter.notifyItemInserted(table.getEntries().size());
                } else {
                    Timber.d("Jump ahead");
                    editText.requestFocus(View.FOCUS_DOWN);
                }

            }
        }
    }
}

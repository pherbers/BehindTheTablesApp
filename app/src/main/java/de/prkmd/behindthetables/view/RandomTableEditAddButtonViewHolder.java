package de.prkmd.behindthetables.view;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.prkmd.behindthetables.R;
import de.prkmd.behindthetables.adapter.RandomTableEditListAdapter;
import de.prkmd.behindthetables.data.RandomTable;

public class RandomTableEditAddButtonViewHolder extends RecyclerView.ViewHolder {
    RandomTableEditListAdapter adapter;
    private ImageButton button;

    public RandomTableEditAddButtonViewHolder(@NonNull View itemView, final RandomTableEditListAdapter adapter) {
        super(itemView);
        this.adapter = adapter;

        button = itemView.findViewById(R.id.table_edit_add_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnClick();
            }
        });
    }

    public void OnClick() {
        if(adapter.getState() == RandomTableEditListAdapter.STATE.EDIT_COLLECTION) {
            adapter.addNewTableCollection();
        }
        else {
            adapter.addNewTableEntry();
        }
    }



}

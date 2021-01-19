package de.prkmd.behindthetables.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import de.prkmd.behindthetables.R;
import de.prkmd.behindthetables.adapter.RandomTableEditListAdapter;
import de.prkmd.behindthetables.adapter.RandomTableListAdapter;
import de.prkmd.behindthetables.data.RandomTable;
import de.prkmd.behindthetables.view.dialog.EditTableTitleDialogFragment;

public class RandomTableEditViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    private RandomTable table;

    private ImageButton button;

    private RandomTableEditListAdapter adapter;

    private int pos;

    public RandomTableEditViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);

    }

    public void bindData(final RandomTable table, final RandomTableEditListAdapter adapter, final int pos, final FragmentActivity context) {
        this.table = table;
        this.adapter = adapter;
        TextView tv = itemView.findViewById(R.id.table_group_edit_text);;
        tv.setText(table.getName());

        button = itemView.findViewById(R.id.table_group_edit_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(itemView.getContext() instanceof Activity) {
                    new EditTableTitleDialogFragment(table, adapter, pos).show(context.getSupportFragmentManager(), "editTableDialog");
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (adapter.getState() == RandomTableEditListAdapter.STATE.EDIT_COLLECTION)
            adapter.editTable(table);
        else
            adapter.finishEditTable();
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }
}

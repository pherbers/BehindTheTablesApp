package de.rub.pherbers.behindthetables.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.rub.pherbers.behindthetables.R;
import de.rub.pherbers.behindthetables.adapter.RandomTableListAdapter;
import de.rub.pherbers.behindthetables.data.RandomTable;
import timber.log.Timber;

/**
 * Created by Patrick on 12.03.2017.
 */

public class RandomTableViewHolder extends RecyclerView.ViewHolder implements OnClickListener{

    private RandomTable table;

    private LinearLayout view;

    private View tableGroup;

    private RandomTableListAdapter adapter;

    public RandomTableViewHolder(final Context context, final ViewGroup parent, RandomTableListAdapter listAdapter) {
        super(new LinearLayout(context));
        view = (LinearLayout)itemView;

        this.adapter = listAdapter;

        LayoutInflater li = LayoutInflater.from(context);
        tableGroup = li.inflate(R.layout.table_group_layout, parent, false);
        ImageButton btn = (ImageButton) tableGroup.findViewById(R.id.table_group_roll_button);
        btn.setFocusable(false);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Timber.d("Click Entry Btn");

                ImageButton btn = (ImageButton) view.findViewById(R.id.table_group_roll_button);
                Animation anim = AnimationUtils.loadAnimation(context, R.anim.dice_button_rotator);
                btn.startAnimation(anim);

                adapter.rollTable(table);
            }
        });

        view.setOrientation(LinearLayout.VERTICAL);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        view.setClickable(true);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);

        view.addView(tableGroup);
        itemView.setOnClickListener(this);
    }

    public void bindData(RandomTable table) {
        this.table = table;
        TextView tv = (TextView) tableGroup.findViewById(R.id.table_group_text);;
        tv.setText(table.getName());
    }

    public void toggle() {
        if(table.isExpanded())
            adapter.collapseTable(table);
        else
            adapter.expandTable(table);
    }

    public View getView() {
        return view;
    }

    @Override
    public void onClick(View v) {
        int pos = getAdapterPosition();
        if (pos != RecyclerView.NO_POSITION) { // Check if an item was deleted, but the user clicked it before the UI removed it
            toggle();
        }
    }

}

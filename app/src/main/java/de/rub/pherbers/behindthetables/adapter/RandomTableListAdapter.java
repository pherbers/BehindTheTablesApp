package de.rub.pherbers.behindthetables.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.rub.pherbers.behindthetables.R;
import de.rub.pherbers.behindthetables.data.RandomTable;
import de.rub.pherbers.behindthetables.data.TableCollection;
import de.rub.pherbers.behindthetables.data.TableEntry;

/**
 * Created by Patrick on 11.03.2017.
 */

public class RandomTableListAdapter extends BaseExpandableListAdapter {

    private TableCollection tableCollection;
    private Context context;

    public RandomTableListAdapter(Context context, TableCollection tableCollection) {
        this.context = context;
        this.tableCollection = tableCollection;
    }

    @Override
    public int getGroupCount() {
        return tableCollection.getTables().size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return tableCollection.getTables().get(groupPosition).size();
    }

    @Override
    public RandomTable getGroup(int groupPosition) {
        return tableCollection.getTables().get(groupPosition);
    }

    @Override
    public TableEntry getChild(int groupPosition, int childPosition) {
        return tableCollection.getTables().get(groupPosition).getEntries().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final RandomTable group = getGroup(groupPosition);
        String text = group.toString();

        LinearLayout ll = new LinearLayout(context);
        LayoutInflater li = LayoutInflater.from(context);
        View v = li.inflate(R.layout.table_group_layout, parent, false);

        TextView tv = (TextView) v.findViewById(R.id.table_group_text);
        tv.setText(text);
        ImageButton btn = (ImageButton) v.findViewById(R.id.table_group_roll_button);
        btn.setFocusable(false);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                group.roll();
            }
        });
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ll.addView(v);
        if(!isExpanded && group.hasRolled()) {
            ImageView divider = new ImageView(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 3);
            divider.setLayoutParams(lp);
            // divider.setImageDrawable(context.getResources().getDrawable(android.R.attr.listDivider, context.getTheme()));
            divider.setBackground(context.getResources().getDrawable(android.R.drawable.divider_horizontal_bright));
            ll.addView(divider);
            View childView = getChildView(groupPosition, group.getRolledIndex(), false, convertView, parent);
            childView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
            ll.addView(childView);
        }

        return ll;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        LayoutInflater li = LayoutInflater.from(context);
        View v = li.inflate(R.layout.table_entry_layout, parent, false);
        // TextView tv = (TextView) v.findViewById(android.R.id.text1);
        // String text = getChild(groupPosition, childPosition).toString();
        // tv.setText(text);
        TextView textentry = (TextView) v.findViewById(R.id.table_entry_text);
        TextView diceentry = (TextView) v.findViewById(R.id.table_entry_dive_value);
        TableEntry te = getChild(groupPosition, childPosition);
        textentry.setText(te.getText());
        diceentry.setText(context.getString(R.string.dice_entry_string, te.getDiceValue()));

        if(getGroup(groupPosition).getRolledIndex() == childPosition) {
            v.setBackgroundColor(context.getResources().getColor(R.color.colorLightPrimary));
        }
        return v;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}

package de.rub.pherbers.behindthetables.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.rub.pherbers.behindthetables.R;
import de.rub.pherbers.behindthetables.data.RandomTable;
import de.rub.pherbers.behindthetables.data.TableEntry;
import timber.log.Timber;

/**
 * Created by Patrick on 12.03.2017.
 */

public class RandomTableView extends LinearLayout {

    private RandomTable table;

    public RandomTableView(Context context, ViewGroup parent, RandomTable table) {
        super(context);
        Timber.i("New child");
        this.table = table;
        LayoutInflater li = LayoutInflater.from(context);
        View v = li.inflate(R.layout.table_group_layout, parent, false);

        TextView tv = (TextView) v.findViewById(R.id.table_group_text);
        tv.setText(table.getName());
        ImageButton btn = (ImageButton) v.findViewById(R.id.table_group_roll_button);
        btn.setFocusable(false);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RandomTableView.this.table.roll();
            }
        });
        setOrientation(LinearLayout.VERTICAL);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        addView(v);
        if(!table.isExpanded() && table.hasRolled()) {
            appendChild(table.getRolledIndex(), parent, true, true);
        } else if (table.isExpanded()){
            for(int i = 0; i < table.getEntries().size(); i++) {
                appendChild(i, parent, true, false);
            }
        }
    }

    private void appendChild(int entrypos, ViewGroup parent, boolean addDivider, boolean highlight) {
        if(addDivider) {
            ImageView divider = new ImageView(getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 3);
            divider.setLayoutParams(lp);
            divider.setBackground(getContext().getResources().getDrawable(android.R.drawable.divider_horizontal_bright));
            addView(divider);
        }
        View childView = getChildView(entrypos, parent);
        if(highlight)
            childView.setBackgroundColor(getContext().getResources().getColor(R.color.colorTableHighlight));
        addView(childView);
    }

    public View getChildView(int childPosition, ViewGroup parent) {
        LayoutInflater li = LayoutInflater.from(getContext());
        View v = li.inflate(R.layout.table_entry_layout, parent, false);
        // TextView tv = (TextView) v.findViewById(android.R.id.text1);
        // String text = getChild(groupPosition, childPosition).toString();
        // tv.setText(text);
        TextView textentry = (TextView) v.findViewById(R.id.table_entry_text);
        TextView diceentry = (TextView) v.findViewById(R.id.table_entry_dive_value);
        TableEntry te = table.getEntries().get(childPosition);
        textentry.setText(te.getText());
        diceentry.setText(getContext().getString(R.string.dice_entry_string, te.getDiceValue()));

        if(table.getRolledIndex() == childPosition) {
            v.setBackgroundColor(getContext().getResources().getColor(R.color.colorTableHighlight));
        } else if (childPosition % 2 == 0) {
            v.setBackgroundColor(getContext().getResources().getColor(R.color.colorTableEven));
        } else {
            v.setBackgroundColor(getContext().getResources().getColor(R.color.colorTableOdd));
        }
        return v;
    }

    public void toggle() {
        table.toggle();
    }

}

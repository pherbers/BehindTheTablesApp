package de.rub.pherbers.behindthetables.view;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import de.rub.pherbers.behindthetables.R;
import de.rub.pherbers.behindthetables.RandomTableActivity;
import de.rub.pherbers.behindthetables.data.RandomTable;
import de.rub.pherbers.behindthetables.data.TableEntry;
import timber.log.Timber;

/**
 * Created by Patrick on 12.03.2017.
 */

public class RandomTableView extends LinearLayout {

    private RandomTable table;
    private int pos;

    private static final int ANIM_DURATION = 200;

    private ArrayList<View> childEntryViews;
    private LinearLayout viewBefore;
    private View highlightedView;
    private LinearLayout viewAfter;

    public RandomTableView(Context context, final ViewGroup parent, RandomTable ptable, int pos) {
        super(context);
        //Timber.i("New child");
        this.table = ptable;
        this.pos = pos;

        LayoutInflater li = LayoutInflater.from(context);
        View v = li.inflate(R.layout.table_group_layout, parent, false);

        TextView tv = (TextView) v.findViewById(R.id.table_group_text);
        tv.setText(table.getName());
        ImageButton btn = (ImageButton) v.findViewById(R.id.table_group_roll_button);
        btn.setFocusable(false);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Timber.d("Click Entry Btn");
                RandomTableView.this.table.roll();
                ((RandomTableActivity)getContext()).redrawList();
            }
        });
        setOrientation(LinearLayout.VERTICAL);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        childEntryViews = new ArrayList<>();

        addView(v);
        viewBefore = new LinearLayout(context);
        ViewGroup viewCurrent = viewBefore;
        addView(viewBefore);
        viewBefore.setOrientation(LinearLayout.VERTICAL);
        viewBefore.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        viewBefore.setVisibility(table.isExpanded()?View.VISIBLE:View.GONE);

        viewAfter = new LinearLayout(context);
        viewAfter.setOrientation(LinearLayout.VERTICAL);
        viewAfter.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        viewAfter.setVisibility(table.isExpanded()?View.VISIBLE:View.GONE);
        for(int i = 0; i < table.getEntries().size(); i++) {
            if(table.getRolledIndex() == i) {
                appendChild(i, this, false, true, true);
                addView(viewAfter);
                viewCurrent = viewAfter;
            }
            else
                appendChild(i, viewCurrent, false, table.isExpanded(), false);
        }
    }

    private void appendChild(int entrypos, ViewGroup parent, boolean addDivider, boolean visible, boolean highlight) {
        if(addDivider) {
            ImageView divider = new ImageView(getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 3);
            divider.setLayoutParams(lp);
            divider.setBackground(getContext().getResources().getDrawable(android.R.drawable.divider_horizontal_bright));
            parent.addView(divider);
            if(!visible)
                divider.setVisibility(View.GONE);
        }
        View childView = getChildView(entrypos, parent);
        if(highlight)
            childView.setBackgroundColor(getContext().getResources().getColor(R.color.colorTableHighlight));
//        if(!visible)
//            childView.setVisibility(View.GONE);

        childEntryViews.add(childView);
        if(highlight)
            highlightedView = childView;
        parent.addView(childView);
    }

    public View getChildView(int childPosition, ViewGroup parent) {
        LayoutInflater li = LayoutInflater.from(getContext());
        View v = li.inflate(R.layout.table_entry_layout, parent, false);
        // TextView tv = (TextView) v.findViewById(android.R.id.text1);
        // String text = getChild(groupPosition, childPosition).toString();
        // tv.setText(text);
        TextView textentry = (TextView) v.findViewById(R.id.table_entry_text);
        TextView diceentry = (TextView) v.findViewById(R.id.table_entry_dice_value);
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

    public void expand(boolean scroll) {
        if(table.isExpanded())
            return;
        Timber.i("Expand");
        ExpandCollapseAnimation.setHeightForWrapContent((Activity) getContext(), viewBefore);
        ExpandCollapseAnimation animBefore = new ExpandCollapseAnimation(viewBefore, ANIM_DURATION);
        viewBefore.startAnimation(animBefore);

        ExpandCollapseAnimation.setHeightForWrapContent((Activity) getContext(), viewAfter);
        ExpandCollapseAnimation animAfter = new ExpandCollapseAnimation(viewAfter, ANIM_DURATION);
        viewAfter.startAnimation(animAfter);
        if (scroll) {
            animBefore.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {((RandomTableActivity) getContext()).scrollToPosition(pos);}
                    }, 5);
                }
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
        table.setExpanded(true);
    }

    public void collapse(boolean scroll) {
        if(!table.isExpanded())
            return;
        Timber.i("Collapse");
        ExpandCollapseAnimation.setHeightForWrapContent((Activity) getContext(), viewBefore);
        ExpandCollapseAnimation animBefore = new ExpandCollapseAnimation(viewBefore, ANIM_DURATION);
        viewBefore.startAnimation(animBefore);

        ExpandCollapseAnimation.setHeightForWrapContent((Activity) getContext(), viewAfter);
        ExpandCollapseAnimation animAfter = new ExpandCollapseAnimation(viewAfter, ANIM_DURATION);
        viewAfter.startAnimation(animAfter);
        if (scroll) {
            animBefore.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {((RandomTableActivity) getContext()).scrollToPosition(pos);}
                    }, 5);
                }
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
        table.setExpanded(false);
    }

    public void toggle() {
        if(!table.isExpanded())
            expand(true);
        else
            collapse(true);
        //table.toggle();
    }

    public boolean isExpanded() {
        return table.isExpanded();
    }

}

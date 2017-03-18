package de.rub.pherbers.behindthetables.view;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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

public class RandomTableViewHolder extends RecyclerView.ViewHolder implements OnClickListener{

    private RandomTable table;

    private static final int ANIM_DURATION = 200;

    private LinearLayout viewBefore;
    private View highlightedView;
    private LinearLayout viewAfter;

    private LinearLayout view;

    private View tableGroup;

    public RandomTableViewHolder(Context context, final ViewGroup parent) {
        super(new LinearLayout(context));
        view = (LinearLayout)itemView;

        //Timber.i("New child");

        LayoutInflater li = LayoutInflater.from(context);
        tableGroup = li.inflate(R.layout.table_group_layout, parent, false);
        ImageButton btn = (ImageButton) tableGroup.findViewById(R.id.table_group_roll_button);
        btn.setFocusable(false);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Timber.d("Click Entry Btn");
                RandomTableViewHolder.this.table.roll();
                ((RandomTableActivity)view.getContext()).redrawListAtPos(getAdapterPosition());
//                ((RandomTableActivity)view.getContext()).redrawList();
            }
        });

        view.setOrientation(LinearLayout.VERTICAL);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        view.setClickable(true);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);

        view.addView(tableGroup);
        viewBefore = new LinearLayout(view.getContext());
        view.addView(viewBefore);
        viewBefore.setOrientation(LinearLayout.VERTICAL);
        viewBefore.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        viewAfter = new LinearLayout(view.getContext());
        viewAfter.setOrientation(LinearLayout.VERTICAL);
        viewAfter.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        itemView.setOnClickListener(this);
    }

    public void bindData(RandomTable table) {
        Timber.d("Binding data for " + table);
        this.table = table;
        TextView tv = (TextView) tableGroup.findViewById(R.id.table_group_text);;
        tv.setText(table.getName());
        viewBefore.setVisibility(table.isExpanded()?View.VISIBLE:View.GONE);
        viewAfter.setVisibility(table.isExpanded()?View.VISIBLE:View.GONE);
        viewBefore.removeAllViews();
        viewAfter.removeAllViews();
        ViewGroup viewCurrent = viewBefore;
        view.removeView(viewAfter);
        view.removeView(highlightedView);
        for(int i = 0; i < table.getEntries().size(); i++) {
            if(table.getRolledIndex() == i) {
                if (highlightedView != null) {
                    updateHighlight();
                } else
                    appendChild(i, view, false, true, true);
                view.addView(viewAfter);
                viewCurrent = viewAfter;
            }
            else
                appendChild(i, viewCurrent, false, table.isExpanded(), false);
        }
    }

    private void appendChild(int entrypos, ViewGroup parent, boolean addDivider, boolean visible, boolean highlight) {
        if(addDivider) {
            ImageView divider = new ImageView(view.getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 3);
            divider.setLayoutParams(lp);
            divider.setBackground(view.getContext().getResources().getDrawable(android.R.drawable.divider_horizontal_bright));
            parent.addView(divider);
            if(!visible)
                divider.setVisibility(View.GONE);
        }
        View childView = getChildView(entrypos, parent);
        if(highlight)
            childView.setBackgroundColor(view.getContext().getResources().getColor(R.color.colorTableHighlight));
//        if(!visible)
//            childView.setVisibility(View.GONE);

        if(highlight) {
            highlightedView = childView;

//            highlightedView.setVisibility(View.GONE);
//            ExpandCollapseAnimation.setHeightForWrapContent((Activity) view.getContext(), highlightedView);
//            final ExpandCollapseAnimation animHighlight = new ExpandCollapseAnimation(highlightedView, ANIM_DURATION);
//            highlightedView.startAnimation(animHighlight);
        }

        parent.addView(childView);

    }

    private View getChildView(int childPosition, ViewGroup parent) {
        LayoutInflater li = LayoutInflater.from(view.getContext());
        View v = li.inflate(R.layout.table_entry_layout, parent, false);
        // TextView tv = (TextView) v.findViewById(android.R.id.text1);
        // String text = getChild(groupPosition, childPosition).toString();
        // tv.setText(text);
        TextView textentry = (TextView) v.findViewById(R.id.table_entry_text);
        TextView diceentry = (TextView) v.findViewById(R.id.table_entry_dice_value);
        TableEntry te = table.getEntries().get(childPosition);
        textentry.setText(te.getText());
        diceentry.setText(view.getContext().getString(R.string.dice_entry_string, te.getDiceValue()));

        if(table.getRolledIndex() == childPosition) {
            v.setBackgroundColor(view.getContext().getResources().getColor(R.color.colorTableHighlight));
        } else if (childPosition % 2 == 0) {
            v.setBackgroundColor(view.getContext().getResources().getColor(R.color.colorTableEven));
        } else {
            v.setBackgroundColor(view.getContext().getResources().getColor(R.color.colorTableOdd));
        }
        return v;
    }

    private void updateHighlight() {
        TextView textentry = (TextView) highlightedView.findViewById(R.id.table_entry_text);
        TextView diceentry = (TextView) highlightedView.findViewById(R.id.table_entry_dice_value);
        TableEntry te = table.getEntries().get(table.getRolledIndex());
        textentry.setText(te.getText());
        diceentry.setText(view.getContext().getString(R.string.dice_entry_string, te.getDiceValue()));
        view.addView(highlightedView);
    }

    public void expand(boolean scroll) {
        if(table.isExpanded())
            return;
        Timber.i("Expand");
        ExpandCollapseAnimation.setHeightForWrapContent((Activity) view.getContext(), viewBefore);
        final ExpandCollapseAnimation animBefore = new ExpandCollapseAnimation(viewBefore, ANIM_DURATION);
        viewBefore.startAnimation(animBefore);

        ExpandCollapseAnimation.setHeightForWrapContent((Activity) view.getContext(), viewAfter);
        final ExpandCollapseAnimation animAfter = new ExpandCollapseAnimation(viewAfter, ANIM_DURATION);
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
                        public void run() {((RandomTableActivity) view.getContext()).scrollToPosition(getAdapterPosition());}
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
        setIsRecyclable(false);
        Timber.i("Collapse");
        ExpandCollapseAnimation.setHeightForWrapContent((Activity) view.getContext(), viewBefore);
        ExpandCollapseAnimation animBefore = new ExpandCollapseAnimation(viewBefore, ANIM_DURATION);
        viewBefore.startAnimation(animBefore);

        ExpandCollapseAnimation.setHeightForWrapContent((Activity) view.getContext(), viewAfter);
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
                        public void run() {((RandomTableActivity) view.getContext()).scrollToPosition(getAdapterPosition());}
                    }, 5);
                    setIsRecyclable(true);
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

    public void clearAnimation() {
        viewBefore.clearAnimation();
        viewAfter.clearAnimation();
        viewBefore.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        viewAfter.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

    }
}

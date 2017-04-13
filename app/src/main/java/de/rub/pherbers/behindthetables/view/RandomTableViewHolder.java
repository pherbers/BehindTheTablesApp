package de.rub.pherbers.behindthetables.view;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
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

import de.rub.pherbers.behindthetables.R;
import de.rub.pherbers.behindthetables.activity.RandomTableActivity;
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
    private ViewGroup highlightedView;
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
                int prev = RandomTableViewHolder.this.table.getRolledIndex();
                RandomTableViewHolder.this.table.roll();
                rerollAnimation(prev);
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
        bindData(table, table.isExpanded());
    }

    public void bindData(RandomTable table, boolean addAllViews) {
        Timber.d("Binding data for " + table + (addAllViews?" and adding all views":""));
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
        highlightedView = null;
        if(addAllViews){
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
                    appendChild(i, viewCurrent, false, false, false);
            }
            viewBefore.getLayoutParams().height = RecyclerView.LayoutParams.WRAP_CONTENT;
            if (highlightedView != null) {
                highlightedView.getLayoutParams().height = RecyclerView.LayoutParams.WRAP_CONTENT;
            } else
                view.addView(viewAfter);
            viewAfter.getLayoutParams().height = RecyclerView.LayoutParams.WRAP_CONTENT;
        } else if (table.getRolledIndex() > -1) {
            if (highlightedView != null) {
                updateHighlight();
                Timber.d("Reused highlight view for " + table);
            } else
                appendChild(table.getRolledIndex(), view, false, true, true);
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
            highlightedView = (LinearLayout)childView;

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
        setDiceEntry(diceentry, te);

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
        setDiceEntry(diceentry, te);
        view.addView(highlightedView);
    }

    private void setDiceEntry(TextView diceentry, TableEntry te) {
        if(te.getDiceValueTo() < 0)
            diceentry.setText(view.getContext().getString(R.string.dice_entry_string, te.getDiceValue()));
        else {
            diceentry.setText(view.getContext().getString(R.string.dice_entry_from_to_string, te.getDiceValue(), te.getDiceValueTo()));

            // We have to scale the text down a bit, otherwise it wont fit :(
            if(te.getDiceValueTo() > 9)
                diceentry.setTextScaleX(0.8f);
        }

    }

    public void expand(boolean scroll) {
        if(table.isExpanded())
            return;
        bindData(table, true);
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
        if(highlightedView != null) {
            highlightedView.clearAnimation();
            highlightedView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            highlightedView.setAlpha(1);
        }
    }

    public void rerollAnimation(int previous) {
        if(previous > -1 && !isExpanded()) {
            // Animation if highlight exists but table is not expanded
            ViewCompat.animate(highlightedView.getChildAt(1)).alpha(0).setDuration(ANIM_DURATION/2).setListener(new ViewPropertyAnimatorListener() {
                @Override
                public void onAnimationStart(View view) {}
                @Override
                public void onAnimationEnd(View view) {
                    bindData(table);
                    ViewCompat.animate(highlightedView.getChildAt(1)).alpha(1).setDuration(ANIM_DURATION/2).setListener(new ViewPropertyAnimatorListener() {
                        @Override
                        public void onAnimationStart(View view) {}
                        @Override
                        public void onAnimationEnd(View view) {view.setAlpha(1);}
                        @Override
                        public void onAnimationCancel(View view) {view.setAlpha(1);}
                    });
                }
                @Override
                public void onAnimationCancel(View view) {
                    bindData(table);
                    view.setAlpha(1);
                }
            });
        } else if (previous < 0 && !isExpanded()) {
            // Animation to expand new highlight if table is not expanded
            bindData(table);
            highlightedView.setVisibility(View.GONE);
            ExpandCollapseAnimation.setHeightForWrapContent((Activity)view.getContext(), highlightedView);
            ExpandCollapseAnimation anim = new ExpandCollapseAnimation(highlightedView, ANIM_DURATION);
            highlightedView.startAnimation(anim);
        } else if (previous > 0 && isExpanded()){
            // Table is expanded and an element is already highlighted
            ValueAnimator anim = ValueAnimator.ofObject(new ArgbEvaluator(), ContextCompat.getColor(view.getContext(),R.color.colorTableHighlight),
                    previous%2==1?ContextCompat.getColor(view.getContext(),R.color.colorTableOdd):ContextCompat.getColor(view.getContext(),R.color.colorTableEven));
            anim.setDuration(ANIM_DURATION/2);
            final ValueAnimator.AnimatorUpdateListener animupdate = new AnimUpdater(highlightedView);
            anim.addUpdateListener(animupdate);
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {}
                @Override
                public void onAnimationEnd(Animator animation) {
                    bindData(table);
                    int newIndex = table.getRolledIndex();
                    highlightedView.setBackgroundColor(ContextCompat.getColor(view.getContext(), newIndex%2==0?R.color.colorTableEven: R.color.colorTableOdd));
                    ValueAnimator anim = ValueAnimator.ofObject(new ArgbEvaluator(),ContextCompat.getColor(view.getContext(),newIndex%2==1?R.color.colorTableOdd:R.color.colorTableEven),
                            ContextCompat.getColor(view.getContext(),R.color.colorTableHighlight));
                    anim.setDuration(ANIM_DURATION/2);
                    anim.addUpdateListener(animupdate);
                    anim.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {}
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            highlightedView.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.colorTableHighlight));
                        }
                        @Override
                        public void onAnimationCancel(Animator animation) {
                            highlightedView.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.colorTableHighlight));
                        }
                        @Override
                        public void onAnimationRepeat(Animator animation) {}
                    });
                    anim.start();
                }
                @Override
                public void onAnimationCancel(Animator animation) {
                    bindData(table);
                    highlightedView.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.colorTableHighlight));
                }
                @Override
                public void onAnimationRepeat(Animator animation) {}
            });
            anim.start();
        } else {
            // Table is expanded, but nothing is highlighted yet

            bindData(table);
            int newIndex = table.getRolledIndex();
            View v = highlightedView;
            v.setBackgroundColor(ContextCompat.getColor(view.getContext(), newIndex%2==0?R.color.colorTableEven: R.color.colorTableOdd));
            ValueAnimator anim = ValueAnimator.ofObject(new ArgbEvaluator(),ContextCompat.getColor(view.getContext(),newIndex%2==0?R.color.colorTableEven:R.color.colorTableOdd),
                    ContextCompat.getColor(view.getContext(),R.color.colorTableHighlight));
            anim.setDuration(ANIM_DURATION/2);
            anim.addUpdateListener(new AnimUpdater(v));
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {}
                @Override
                public void onAnimationEnd(Animator animation) {
                    highlightedView.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.colorTableHighlight));
                }
                @Override
                public void onAnimationCancel(Animator animation) {
                    bindData(table);
                    highlightedView.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.colorTableHighlight));
                }
                @Override
                public void onAnimationRepeat(Animator animation) {}
            });
            anim.start();
        }
    }
    private class AnimUpdater implements ValueAnimator.AnimatorUpdateListener {
        View v;
        public AnimUpdater(View v) {
            this.v = v;
        }
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            v.setBackgroundColor((int) animation.getAnimatedValue());
        }
    }
}

package de.prkmd.behindthetables.view;

import android.app.Activity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import de.prkmd.behindthetables.R;
import de.prkmd.behindthetables.adapter.RandomTableEditListAdapter;
import de.prkmd.behindthetables.data.RandomTable;
import de.prkmd.behindthetables.data.TableCollection;
import de.prkmd.behindthetables.view.dialog.TextInputDialogFragment;

public class RandomTableEditHeaderViewHolder extends RecyclerView.ViewHolder {

    private TableCollection tableCollection;

    private RandomTableEditListAdapter adapter;

    public RandomTableEditHeaderViewHolder(View itemView) {
        super(itemView);
    }
    public void bindData(final TableCollection tableCollection, final RandomTableEditListAdapter adapter, final FragmentActivity context) {
        this.tableCollection = tableCollection;
        this.adapter = adapter;

        TextView categoryText = itemView.findViewById(R.id.category_text);
        if(tableCollection.getCategory().isEmpty())
            categoryText.setText(context.getString(R.string.category_text, context.getString(R.string.no_category)));
        else
            categoryText.setText(context.getString(R.string.category_text, tableCollection.getCategory()));

        ImageButton categoryEditButton = itemView.findViewById(R.id.category_edit_button);
        categoryEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TextInputDialogFragment(
                        context.getString(R.string.edit_category),
                        context.getString(R.string.category),
                        tableCollection.getCategory(),
                        new TextInputDialogFragment.TextEditListener() {
                            @Override
                            public void onClick(String text, boolean hasChanged) {
                                tableCollection.setCategory(text.trim());
                                adapter.notifyItemChanged(0);
                            }
                        }).show(context.getSupportFragmentManager(), "tableCollectionEditCategoryDialog");
            }
        });


        TextView descriptionText = itemView.findViewById(R.id.description_text);
        if(tableCollection.getDescription().isEmpty())
            descriptionText.setText(context.getString(R.string.description_text, context.getString(R.string.no_description)));
        else
            descriptionText.setText(context.getString(R.string.description_text, tableCollection.getDescription()));

        ImageButton descriptionEditButton = itemView.findViewById(R.id.description_edit_button);
        descriptionEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TextInputDialogFragment(
                        context.getString(R.string.edit_description),
                        context.getString(R.string.description),
                        tableCollection.getDescription(),
                        new TextInputDialogFragment.TextEditListener() {
                            @Override
                            public void onClick(String text, boolean hasChanged) {
                                tableCollection.setDescription(text.trim());
                                adapter.notifyItemChanged(0);
                            }
                        }).show(context.getSupportFragmentManager(), "tableCollectionEditDescDialog");
            }
        });


        ImageButton keywordsEditButton = itemView.findViewById(R.id.keywords_edit_button);
        TextView keywordsText = itemView.findViewById(R.id.keywords_text);
        if(tableCollection.getKeywords().size() == 0)
            keywordsText.setText(context.getString(R.string.keywords_text, context.getString(R.string.no_keywords)));
        else
            keywordsText.setText(context.getString(R.string.keywords_text, concatStringList(tableCollection.getKeywords())));

        keywordsEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keywords = concatStringList(tableCollection.getKeywords());

                new TextInputDialogFragment(
                        context.getString(R.string.edit_keywords),
                        context.getString(R.string.keywords_example),
                        keywords,
                        new TextInputDialogFragment.TextEditListener() {
                            @Override
                            public void onClick(String text, boolean hasChanged) {
                                if(text.trim().isEmpty()) {
                                    tableCollection.setKeywords(new ArrayList<String>());
                                } else {
                                    String[] kw = text.trim().split("\\s*,\\s*");
                                    List<String> kwl = Arrays.asList(kw);
                                    tableCollection.setKeywords(kwl);
                                }
                                adapter.notifyItemChanged(0);
                            }
                        }).show(context.getSupportFragmentManager(), "tableCollectionEditCategoryDialog");
            }
        });
    }

    private static String concatStringList(List<String> strings) {
        return concatStringList(strings, ", ");
    }

    private static String concatStringList(List<String> strings, String delimeter) {
        String out;
        if(!strings.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(strings.get(0));
            for (int i = 1; i < strings.size(); i++) {
                String s = strings.get(i);
                sb.append(delimeter);
                sb.append(s);
            }
            out = sb.toString();
        } else {
            out = "";
        }
        return out;
    }
}

package de.rub.pherbers.behindthetables.view;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import de.rub.pherbers.behindthetables.R;
import de.rub.pherbers.behindthetables.data.TableCollection;
import de.rub.pherbers.behindthetables.data.TableLink;
import de.rub.pherbers.behindthetables.util.TableLinkMovementMethod;

/**
 * Created by Patrick on 29.03.2017.
 */

public class RandomTableHeaderViewHolder extends ViewHolder{
    public RandomTableHeaderViewHolder(View itemView) {
        super(itemView);
    }

    public void bindData(TableCollection table) {
        if(table.getUseWithTables().isEmpty()) {
            itemView.findViewById(R.id.table_info_use_with_scroll).setVisibility(View.GONE);
        } else {
            itemView.findViewById(R.id.table_info_use_with_scroll).setVisibility(View.VISIBLE);
            TextView txUse = (TextView) itemView.findViewById(R.id.table_info_use_with);
            setLinkGroup(txUse, "Use with: ", table.getUseWithTables());
        }

        if(table.getRelatedTables().isEmpty()) {
            itemView.findViewById(R.id.table_info_related_scroll).setVisibility(View.GONE);
        } else {
            itemView.findViewById(R.id.table_info_related_scroll).setVisibility(View.VISIBLE);
            TextView txRelated = (TextView) itemView.findViewById(R.id.table_info_related);
            setLinkGroup(txRelated, "Related: ", table.getRelatedTables());
        }
        if(table.getKeywords().isEmpty()) {
            itemView.findViewById(R.id.table_info_keywords_scroll).setVisibility(View.GONE);
        } else {
            itemView.findViewById(R.id.table_info_keywords_scroll).setVisibility(View.VISIBLE);
            TextView txKeywords = (TextView) itemView.findViewById(R.id.table_info_keywords);
            //setLinkGroup(txKeywords, table.getKeywords());
            // TODO: Make keywords link to search
            // TODO: Externalize Strings
            txKeywords.setText("Keywords: " + buildCommaSeparatedString(table.getKeywords()));
        }
    }

    private void setLinkGroup(TextView tx, String prefix, List<TableLink> links) {
        tx.setText(Html.fromHtml(prefix + buildCommaSeparatedLinkString(links)));
        tx.setMovementMethod(TableLinkMovementMethod.getInstance());
    }

    private static String buildCommaSeparatedString(List<String> collectionOfStrings){
        StringBuilder result = new StringBuilder();
        for(String string : collectionOfStrings) {
            result.append(string);
            result.append(", ");
        }
        return result.length() > 0 ? result.substring(0, result.length() - 2): "";
    }

    private static String buildCommaSeparatedLinkString(List<TableLink> collectionOfLinks) {
        StringBuilder result = new StringBuilder();
        for(TableLink tl: collectionOfLinks) {
            result.append(tl.getHTML());
            result.append(", ");
        }
        return result.length() > 0 ? result.substring(0, result.length() - 2): "";
    }
}

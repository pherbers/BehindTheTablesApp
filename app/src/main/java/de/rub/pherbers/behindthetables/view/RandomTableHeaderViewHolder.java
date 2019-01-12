package de.rub.pherbers.behindthetables.view;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.ms.square.android.expandabletextview.ExpandableTextView;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

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
        final ExpandableTextView desc = (ExpandableTextView) itemView.findViewById(R.id.table_info_description);
        final TextView textView = (TextView) itemView.findViewById(R.id.expandable_text);

        // Parse Markdown
        Parser parser = Parser.builder().build();
        String htmlString = HtmlRenderer.builder().build().render(parser.parse(table.getDescription()));

        desc.setText(RandomTableSubcategoryViewHolder.trimTrailingWhitespace(Html.fromHtml(htmlString)));

        textView.setMovementMethod(LinkMovementMethod.getInstance());

        if(table.getUseWithTables().isEmpty()) {
            itemView.findViewById(R.id.table_info_use_with_scroll).setVisibility(View.GONE);
        } else {
            itemView.findViewById(R.id.table_info_use_with_scroll).setVisibility(View.VISIBLE);
            TextView txUse = (TextView) itemView.findViewById(R.id.table_info_use_with);
            setLinkGroup(txUse, this.itemView.getContext().getString(R.string.info_use_with), table.getUseWithTables());
        }

        if(table.getRelatedTables().isEmpty()) {
            itemView.findViewById(R.id.table_info_related_scroll).setVisibility(View.GONE);
        } else {
            itemView.findViewById(R.id.table_info_related_scroll).setVisibility(View.VISIBLE);
            TextView txRelated = (TextView) itemView.findViewById(R.id.table_info_related);
            setLinkGroup(txRelated, this.itemView.getContext().getString(R.string.info_related), table.getRelatedTables());
        }
        if(table.getKeywords().isEmpty()) {
            itemView.findViewById(R.id.table_info_keywords_scroll).setVisibility(View.GONE);
        } else {
            itemView.findViewById(R.id.table_info_keywords_scroll).setVisibility(View.VISIBLE);
            TextView txKeywords = (TextView) itemView.findViewById(R.id.table_info_keywords);

            txKeywords.setText(this.itemView.getContext().getString(R.string.info_keywords, buildCommaSeparatedString(table.getKeywords())));

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

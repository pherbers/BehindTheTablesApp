package de.prkmd.behindthetables.view;

import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import de.prkmd.behindthetables.R;
import de.prkmd.behindthetables.data.SubcategoryEntry;

/**
 * Created by Patrick on 16.04.2017.
 */

public class RandomTableSubcategoryViewHolder extends RecyclerView.ViewHolder {
    public RandomTableSubcategoryViewHolder(View itemView) {
        super(itemView);
    }
    public void bindData(SubcategoryEntry subcategory) {
        TextView subcategoryText = (TextView) itemView.findViewById(R.id.table_subcategory_text);
        Parser parser = Parser.builder().build();
        String htmlString = HtmlRenderer.builder().build().render(parser.parse("**" + subcategory.getText() + "**"));
        subcategoryText.setText(trimTrailingWhitespace(Html.fromHtml(htmlString)));

    }

    /** Trims trailing whitespace. Removes any of these characters:
     * 0009, HORIZONTAL TABULATION
     * 000A, LINE FEED
     * 000B, VERTICAL TABULATION
     * 000C, FORM FEED
     * 000D, CARRIAGE RETURN
     * 001C, FILE SEPARATOR
     * 001D, GROUP SEPARATOR
     * 001E, RECORD SEPARATOR
     * 001F, UNIT SEPARATOR
     * @return "" if source is null, otherwise string with all trailing whitespace removed
     */
    public static CharSequence trimTrailingWhitespace(CharSequence source) {

        if(source == null)
            return "";

        int i = source.length();

        // loop back to the first non-whitespace character
        while(--i >= 0 && Character.isWhitespace(source.charAt(i))) {
        }

        return source.subSequence(0, i+1);
    }
}

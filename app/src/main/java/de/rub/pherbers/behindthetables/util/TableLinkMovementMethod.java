package de.rub.pherbers.behindthetables.util;

import android.content.Context;
import android.content.Intent;
import android.text.Layout;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.widget.Toast;

import de.rub.pherbers.behindthetables.activity.RandomTableActivity;
import timber.log.Timber;

/**
 * Created by Patrick on 29.03.2017.
 */

public class TableLinkMovementMethod extends LinkMovementMethod {

    private static TableLinkMovementMethod tableMovementMethod = new TableLinkMovementMethod();

    public boolean onTouchEvent(android.widget.TextView widget, android.text.Spannable buffer, android.view.MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);
            if (link.length != 0) {
                String url = link[0].getURL();
                Timber.d("Link " + url);
                //Toast.makeText(widget.getContext(), "Table link was clicked: " + url, Toast.LENGTH_LONG).show();

                Context context = widget.getContext();
                Intent intent = new Intent(context, RandomTableActivity.class);
                intent.putExtra(RandomTableActivity.EXTRA_TABLE_DATABASE_RESOURCE_LOCATION, "table_" + url);
                context.startActivity(intent);

                return true;
            }
        }

        return super.onTouchEvent(widget, buffer, event);
    }

    public static MovementMethod getInstance() {
        if (tableMovementMethod == null)
            tableMovementMethod = new TableLinkMovementMethod();

        return tableMovementMethod;
    }
}

package de.prkmd.behindthetables.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.prkmd.behindthetables.R;
import de.prkmd.behindthetables.data.TableFile;

/**
 * Created by Nils on 17.03.2017.
 */

public class TableFileAdapter extends RecyclerView.Adapter<TableFileAdapter.Holder> {

    private List<TableFile> files;
    private Context context;

    public TableFileAdapter(Context context, List<TableFile> files) {
        this.context = context;
        this.files = files;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.table_file_list_view, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(final Holder holder, int position) {
        final TableFile file = files.get(position);

        holder.flavorTF.setText(file.getDescription());
        holder.titleTF.setText(file.getTitle());
        //holder.indexTF.setText(context.getString(R.string.info_index_indicator, (position + 1)));

        holder.titleTF.setCompoundDrawablesRelative(null, null, null, null);
        if (file.isFavorite(getContext())) {
            Drawable img = getContext().getResources().getDrawable(R.drawable.baseline_star_text_18);
            holder.titleTF.setCompoundDrawablesRelativeWithIntrinsicBounds(img, null, null, null);
        }
    }

    private Context getContext() {
        return context;
    }

    @Override
    public int getItemCount() {
        return (null != files ? files.size() : 0);
    }

    class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView titleTF, flavorTF;
        //protected ImageButton favBT;

        public Holder(View view) {
            super(view);

            flavorTF = (TextView) view.findViewById(R.id.table_file_flavor);
            titleTF = (TextView) view.findViewById(R.id.table_file_title);
            // indexTF = (TextView) view.findViewById(R.id.table_file_index);

            //this.favBT = (ImageButton) view.findViewById(R.id.table_file_list_fav_bt);
        }

        @Override
        public void onClick(View view) {

        }
    }
}

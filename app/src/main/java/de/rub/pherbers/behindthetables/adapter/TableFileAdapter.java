package de.rub.pherbers.behindthetables.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;
import java.util.zip.Inflater;

import de.rub.pherbers.behindthetables.R;
import de.rub.pherbers.behindthetables.data.TableFile;
import de.rub.pherbers.behindthetables.sql.DBAdapter;
import timber.log.Timber;

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

        holder.titleTF.setText(file.getTitle());
        holder.indexTF.setText(context.getString(R.string.info_index_indicator, (position + 1)));

        if (file.isExternal()) {
            holder.pathTF.setText(file.getFile().getAbsolutePath());
        } else {
            holder.pathTF.setVisibility(View.GONE);
        }

        //applyFavToButton(file, holder);
        //holder.pathTF.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View view) {
        //        Timber.i("Click event on fav-button for '" + file.getTitle() + "'!");
        //        file.setFav(!file.isFav());
        //        applyFavToButton(file, holder);
        //
        //        DBAdapter adapter = new DBAdapter(getContext());
        //        adapter.open();
        //        file.saveToDB(adapter);
        //        adapter.close();
        //    }
        //});
    }

    private Context getContext() {
        return context;
    }

    //private void applyFavToButton(TableFile file, Holder holder) {
    //    if (file.isFav()) {
    //        holder.favBT.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_star_black_48dp));
    //    } else {
    //        holder.favBT.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_star_border_black_48dp));
    //    }
    //}

    @Override
    public int getItemCount() {
        return (null != files ? files.size() : 0);
    }

    class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView titleTF, indexTF, pathTF;
        //protected ImageButton favBT;

        public Holder(View view) {
            super(view);

            this.titleTF = (TextView) view.findViewById(R.id.table_file_title);
            this.indexTF = (TextView) view.findViewById(R.id.table_file_index);
            this.pathTF = (TextView) view.findViewById(R.id.table_file_path);
            //this.favBT = (ImageButton) view.findViewById(R.id.table_file_list_fav_bt);
        }

        @Override
        public void onClick(View view) {

        }
    }
}
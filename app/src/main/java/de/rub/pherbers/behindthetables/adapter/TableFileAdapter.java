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
import android.widget.TextView;

import java.util.List;
import java.util.zip.Inflater;

import de.rub.pherbers.behindthetables.R;
import de.rub.pherbers.behindthetables.data.TableFile;

/**
 * Created by Nils on 17.03.2017.
 */

public class TableFileAdapter extends RecyclerView.Adapter<TableFileAdapter.Holder> {

	private List<TableFile> files;
	private Context context;

	public TableFileAdapter(Context context,List<TableFile> files) {
		this.context=context;
		this.files = files;
	}

	@Override
	public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.table_file_list_view, parent, false);
		return new Holder(view);
	}

	@Override
	public void onBindViewHolder(Holder holder, int position) {
		TableFile file = files.get(position);

		holder.flavorTF.setText(file.getIdentifier());
		holder.titleTF.setText(file.getTitle());
		holder.indexTF.setText(context.getString(R.string.info_index_indicator, (position+1)));
	}

	@Override
	public int getItemCount() {
		return (null != files ? files.size() : 0);
	}

	class Holder extends RecyclerView.ViewHolder {
		protected TextView titleTF, flavorTF, indexTF;

		public Holder(View view) {
			super(view);

			this.titleTF = (TextView) view.findViewById(R.id.table_file_title);
			this.flavorTF = (TextView) view.findViewById(R.id.table_file_flavor);
			this.indexTF = (TextView) view.findViewById(R.id.table_file_index);
		}
	}
}

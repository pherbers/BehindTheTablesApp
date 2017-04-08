package de.rub.pherbers.behindthetables.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.rub.pherbers.behindthetables.R;

/**
 * Created by Patrick on 08.04.2017.
 */

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    @Override
    public CategoryAdapter.CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CategoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false));
    }

    @Override
    public void onBindViewHolder(CategoryAdapter.CategoryViewHolder holder, int position) {
        holder.bindData(position + ".");
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {

        String category;

        public CategoryViewHolder(View itemView) {
            super(itemView);
        }

        public void bindData(String category) {
            this.category = category;
        }
    }
}

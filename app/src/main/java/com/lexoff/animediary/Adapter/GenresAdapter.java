package com.lexoff.animediary.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lexoff.animediary.R;

import java.util.Collections;
import java.util.List;

public class GenresAdapter extends RecyclerView.Adapter<GenresAdapter.ViewHolder> {

    private List<String> localItems;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameView;

        public ViewHolder(View view) {
            super(view);

            nameView=view.findViewById(R.id.name_view);
        }

        public TextView getNameView() {
            return nameView;
        }
    }

    public GenresAdapter(List<String> items) {
        this.localItems=items;
    }

    @Override
    public GenresAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new GenresAdapter.ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.genre_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(GenresAdapter.ViewHolder viewHolder, final int position) {
        viewHolder.getNameView().setText(localItems.get(position));
    }

    @Override
    public int getItemCount() {
        return localItems.size();
    }

    public String getItem(int position){
        return this.localItems.get(position);
    }

    public List<String> getItems(){
        if (this.localItems == null) {
            return Collections.emptyList();
        }

        return this.localItems;
    }

    public void addItems(List<String> items){
        this.localItems.addAll(items);

        notifyDataSetChanged();
    }

}

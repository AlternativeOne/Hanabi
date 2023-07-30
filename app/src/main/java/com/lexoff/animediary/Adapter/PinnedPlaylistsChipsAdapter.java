package com.lexoff.animediary.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lexoff.animediary.CustomOnItemClickListener;
import com.lexoff.animediary.Database.Model.PlaylistEntity;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Util.Utils;

import java.util.Collections;
import java.util.List;

public class PinnedPlaylistsChipsAdapter extends RecyclerView.Adapter<PinnedPlaylistsChipsAdapter.ViewHolder> {

    private List<PlaylistEntity> localItems;

    private CustomOnItemClickListener listener;

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

    public PinnedPlaylistsChipsAdapter(List<PlaylistEntity> items, CustomOnItemClickListener listener) {
        this.localItems=items;
        this.listener=listener;
    }

    @Override
    public PinnedPlaylistsChipsAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new PinnedPlaylistsChipsAdapter.ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.pinned_playlist_chip_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(PinnedPlaylistsChipsAdapter.ViewHolder viewHolder, final int position) {
        viewHolder.getNameView().setText(localItems.get(position).name);

        viewHolder.getNameView().setOnClickListener(v -> {
            Utils.animateClickOnItem(viewHolder.itemView, () -> {
                if (listener != null)
                    listener.onClick(viewHolder.itemView, viewHolder.getAdapterPosition());
            });
        });
    }

    @Override
    public int getItemCount() {
        return localItems.size();
    }

    public PlaylistEntity getItem(int position){
        return this.localItems.get(position);
    }

    public List<PlaylistEntity> getItems(){
        if (this.localItems == null) {
            return Collections.emptyList();
        }

        return this.localItems;
    }

    public void addItems(List<PlaylistEntity> items){
        this.localItems.addAll(items);

        notifyDataSetChanged();
    }

}

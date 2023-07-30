package com.lexoff.animediary.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lexoff.animediary.CustomOnItemClickListener;
import com.lexoff.animediary.Database.ADatabase;
import com.lexoff.animediary.Database.AppDatabase;
import com.lexoff.animediary.Database.Model.PlaylistEntity;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Util.Utils;

import java.util.Collections;
import java.util.List;

public class PlaylistsDialogAdapter extends RecyclerView.Adapter<PlaylistsAdapter.ViewHolder> {

    private List<PlaylistEntity> localItems;

    private CustomOnItemClickListener listener;

    private Context context;

    private AppDatabase database;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameView, descriptionView;

        public ViewHolder(View view) {
            super(view);

            nameView = view.findViewById(R.id.name_view);
            descriptionView=view.findViewById(R.id.description_view);
        }

        public TextView getNameView() {
            return nameView;
        }

        public TextView getDescriptionView(){
            return descriptionView;
        }
    }

    public PlaylistsDialogAdapter(Context context, List<PlaylistEntity> items, CustomOnItemClickListener listener) {
        this.localItems=items;
        this.listener=listener;
        this.context=context;
        this.database= ADatabase.getInstance(context);
    }

    @Override
    public PlaylistsAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new PlaylistsAdapter.ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.playlist_dialog_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(PlaylistsAdapter.ViewHolder viewHolder, final int position) {
        TextView nameView=viewHolder.getNameView();
        nameView.setText(localItems.get(position).name);

        TextView descriptionView=viewHolder.getDescriptionView();
        String description=localItems.get(position).description;
        if (description==null || description.isEmpty()){
            descriptionView.setVisibility(View.GONE);
        } else {
            descriptionView.setVisibility(View.VISIBLE);
            descriptionView.setText(description);
        }

        viewHolder.itemView.setOnClickListener(v -> {
            Utils.animateClickOnItem(v, () -> {
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
        if (localItems == null) {
            return Collections.emptyList();
        }

        return localItems;
    }

    public void addItems(List<PlaylistEntity> items){
        localItems.addAll(items);

        notifyDataSetChanged();
    }

}

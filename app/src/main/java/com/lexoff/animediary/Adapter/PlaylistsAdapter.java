package com.lexoff.animediary.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lexoff.animediary.CustomOnItemClickListener;
import com.lexoff.animediary.Database.ADatabase;
import com.lexoff.animediary.Database.AppDatabase;
import com.lexoff.animediary.Database.Model.PlaylistEntity;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Util.Utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistsAdapter.ViewHolder> {

    private List<PlaylistEntity> localItems;

    private CustomOnItemClickListener listener;

    private Context context;

    private AppDatabase database;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameView, descriptionView;
        private final ImageView pinnedView;

        public ViewHolder(View view) {
            super(view);

            nameView = view.findViewById(R.id.name_view);
            descriptionView=view.findViewById(R.id.description_view);
            pinnedView=view.findViewById(R.id.pinned_view);
        }

        public TextView getNameView() {
            return nameView;
        }

        public TextView getDescriptionView(){
            return descriptionView;
        }

        public ImageView getPinnedView(){
            return pinnedView;
        }
    }

    public PlaylistsAdapter(Context context, List<PlaylistEntity> items, CustomOnItemClickListener listener) {
        this.localItems=items;
        this.listener=listener;
        this.context=context;
        this.database= ADatabase.getInstance(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.playlist_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        TextView nameView=viewHolder.getNameView();
        nameView.setText(localItems.get(position).name);

        viewHolder.getDescriptionView().setText(localItems.get(position).description);

        ImageView pinnedView=viewHolder.getPinnedView();
        boolean pinned=localItems.get(position).pinned==1;
        if (pinned){
            pinnedView.setImageResource(R.drawable.ic_pin_white);

            nameView.setPadding(nameView.getPaddingLeft(), nameView.getPaddingTop(), nameView.getPaddingRight()+Utils.dpToPx(context, 25), nameView.getPaddingBottom());
        } else {
            nameView.setPadding(nameView.getPaddingLeft(), nameView.getPaddingTop(), nameView.getPaddingRight(), nameView.getPaddingBottom());
        }
        pinnedView.setVisibility(pinned ? View.VISIBLE : View.INVISIBLE);

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

    public void filterPinnedFirst() {
        Collections.sort(localItems, new Comparator<PlaylistEntity>() {
            @Override
            public int compare(PlaylistEntity o1, PlaylistEntity o2) {
                if (o1.pinned == 1 && o2.pinned == 0) return -1;
                else if (o2.pinned == 1 && o1.pinned == 0) return 1;
                else if (o1.pinned == o2.pinned) {
                    if (o1.id > o2.id) return 1;
                    else return -1;
                } else return 0;
            }
        });

        notifyDataSetChanged();
    }

}
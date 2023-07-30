package com.lexoff.animediary.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.lexoff.animediary.AppIcon;
import com.lexoff.animediary.CustomOnItemClickListener;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Util.Utils;

import java.util.Collections;
import java.util.List;

public class AppIconsAdapter extends RecyclerView.Adapter<AppIconsAdapter.ViewHolder> {

    private List<AppIcon> localItems;

    private CustomOnItemClickListener listener;

    private int selectedItemPosition=0;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iconView;
        private final View backgroundView;

        public ViewHolder(View view) {
            super(view);

            iconView=view.findViewById(R.id.icon_view);
            backgroundView=view.findViewById(R.id.background_view);
        }

        public ImageView getIconView() {
            return iconView;
        }

        public View getBackgroundView(){
            return backgroundView;
        }
    }

    public AppIconsAdapter(List<AppIcon> items, CustomOnItemClickListener listener) {
        this.localItems=items;
        this.listener=listener;
    }

    @Override
    public AppIconsAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new AppIconsAdapter.ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.app_icon_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(AppIconsAdapter.ViewHolder viewHolder, final int position) {
        viewHolder.getIconView().setImageResource(localItems.get(position).iconResource);

        if (position==selectedItemPosition) {
            viewHolder.getBackgroundView().setBackgroundResource(R.drawable.selected_app_icon_background);
        } else {
            viewHolder.getBackgroundView().setBackgroundResource(R.drawable.rounded_item);
        }

        viewHolder.getIconView().setOnClickListener(v -> {
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

    public AppIcon getItem(int position){
        return this.localItems.get(position);
    }

    public List<AppIcon> getItems(){
        if (this.localItems == null) {
            return Collections.emptyList();
        }

        return this.localItems;
    }

    public void addItems(List<AppIcon> items){
        this.localItems.addAll(items);

        notifyDataSetChanged();
    }

    public void setSelectedItem(int position){
        if (position>=0 && position<localItems.size()){
            selectedItemPosition=position;

            notifyDataSetChanged();
        }
    }

}

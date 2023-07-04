package com.lexoff.animediary.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lexoff.animediary.Constants;
import com.lexoff.animediary.CustomOnItemClickListener;
import com.lexoff.animediary.Database.AnimeToWatchEntity;
import com.lexoff.animediary.ImageLoaderWrapper;
import com.lexoff.animediary.ListMode;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Utils;

import java.util.Collections;
import java.util.List;

public class ToWatchRecordsAdapter extends RecyclerView.Adapter<ToWatchRecordsAdapter.ViewHolder> {

    private List<AnimeToWatchEntity> localItems;

    private ListMode listMode;

    private CustomOnItemClickListener listener;

    private boolean showEnglishTitles=false;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnailView;
        private final TextView titleView, summaryView;

        public ViewHolder(View view) {
            super(view);

            thumbnailView=view.findViewById(R.id.thumbnail_view);
            titleView = view.findViewById(R.id.title_view);
            summaryView=view.findViewById(R.id.summary_view);
        }

        public ImageView getThumbnailView() {
            return thumbnailView;
        }

        public TextView getTitleView() {
            return titleView;
        }

        public TextView getSummaryView() {
            return summaryView;
        }
    }

    public ToWatchRecordsAdapter(List<AnimeToWatchEntity> items, ListMode listMode, CustomOnItemClickListener listener) {
        this.localItems=items;
        this.listMode=listMode;
        this.listener=listener;
    }

    public void setAdditionalParams(boolean showEnglishTitles){
        this.showEnglishTitles=showEnglishTitles;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (listMode==ListMode.GRID) {
            return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.towatch_grid_item, viewGroup, false));
        } else {
            return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.towatch_item, viewGroup, false));
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        if (listMode == ListMode.LIST) {
            Utils.resizeImageView(viewHolder.getThumbnailView(), Constants.GRID_ITEM_DUMMY_BITMAP_WIDTH, Constants.GRID_ITEM_DUMMY_BITMAP_HEIGHT);

            ImageLoaderWrapper.loadImageWithPlaceholder(localItems.get(position).thumbnail_url,
                    viewHolder.getThumbnailView(),
                    () -> {
                        viewHolder.getThumbnailView().setImageResource(R.drawable.dummy_no_thumbnail);
                    });
        } else if (listMode == ListMode.GRID) {
            //pre-resize of imageview solves a lot of problems with list
            //for example, jumping views and wrong restored position in onRestoreInstance
            Utils.resizeImageView(viewHolder.getThumbnailView(), Constants.GRID_ITEM_DUMMY_BITMAP_WIDTH, Constants.GRID_ITEM_DUMMY_BITMAP_HEIGHT);

            ImageLoaderWrapper.loadImageAndResizeWithPlaceholder(localItems.get(position).thumbnail_url,
                    viewHolder.getThumbnailView(),
                    Constants.GRID_ITEM_DUMMY_BITMAP_WIDTH,
                    Constants.GRID_ITEM_DUMMY_BITMAP_HEIGHT,
                    () -> {
                        viewHolder.getThumbnailView().setImageResource(R.drawable.dummy_no_thumbnail);
                    });
        }

        String title = localItems.get(position).title;
        if (showEnglishTitles && !localItems.get(position).second_title.isEmpty()) {
            title = localItems.get(position).second_title;
        }

        viewHolder.getTitleView().setText(title);

        TextView summaryView = viewHolder.getSummaryView();
        if (summaryView != null) {
            String summary = Utils.unescapeStr(localItems.get(position).summary);
            summary = Utils.formatSummary(summary);
            summaryView.setText(Utils.shortenSummary(summary, 200));
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

    public AnimeToWatchEntity getItem(int position){
        return this.localItems.get(position);
    }

    public List<AnimeToWatchEntity> getItems(){
        if (this.localItems == null) {
            return Collections.emptyList();
        }

        return  this.localItems;
    }

}

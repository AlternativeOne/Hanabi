package com.lexoff.animediary.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lexoff.animediary.Constants;
import com.lexoff.animediary.CustomOnItemClickListener;
import com.lexoff.animediary.Database.ADatabase;
import com.lexoff.animediary.Database.AppDatabase;
import com.lexoff.animediary.Database.PlaylistStreamEntity;
import com.lexoff.animediary.ImageLoaderWrapper;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Utils;

import java.util.Collections;
import java.util.List;

public class PlaylistItemsAdapter extends RecyclerView.Adapter<PlaylistItemsAdapter.ViewHolder> {

    private List<PlaylistStreamEntity> localItems;

    private CustomOnItemClickListener listener;

    private Context context;

    private AppDatabase database;

    private boolean showEnglishTitles = false, showAddedToBadge=true;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnailView;
        private final TextView titleView, summaryView, libraryBadgeView, watchlistBadgeView;
        private final ImageView deleteBtn;

        public ViewHolder(View view) {
            super(view);

            thumbnailView = view.findViewById(R.id.thumbnail_view);
            titleView = view.findViewById(R.id.title_view);
            summaryView = view.findViewById(R.id.summary_view);
            libraryBadgeView=view.findViewById(R.id.library_badge_view);
            watchlistBadgeView=view.findViewById(R.id.watchlist_badge_view);
            deleteBtn=view.findViewById(R.id.delete_from_playlist_button);
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

        public TextView getLibraryBadgeView(){
            return libraryBadgeView;
        }

        public TextView getWatchlistBadgeView(){
            return watchlistBadgeView;
        }

        public ImageView getDeleteButton(){
            return deleteBtn;
        }
    }

    public PlaylistItemsAdapter(Context context, List<PlaylistStreamEntity> items, CustomOnItemClickListener listener) {
        this.localItems = items;
        this.listener = listener;
        this.context=context;
        this.database=ADatabase.getInstance(context);
    }

    public void setAdditionalParams(boolean showEnglishTitles, boolean showAddedToBadge) {
        this.showEnglishTitles = showEnglishTitles;
        this.showAddedToBadge=showAddedToBadge;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.playlist_stream_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Utils.resizeImageView(viewHolder.getThumbnailView(), Constants.GRID_ITEM_DUMMY_BITMAP_WIDTH, Constants.GRID_ITEM_DUMMY_BITMAP_HEIGHT);

        ImageLoaderWrapper.loadImageWithPlaceholder(localItems.get(position).thumbnail_url,
                viewHolder.getThumbnailView(),
                () -> {
                    viewHolder.getThumbnailView().setImageResource(R.drawable.dummy_no_thumbnail);
                });

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

        TextView libraryBadgeView = viewHolder.getLibraryBadgeView();

        if (showAddedToBadge && database.animeWatchedDAO().countByMALId(localItems.get(position).malid) == 1) {
            libraryBadgeView.setText(context.getString(R.string.in_watched_badge_text));

            libraryBadgeView.setVisibility(View.VISIBLE);
        } else {
            libraryBadgeView.setVisibility(View.GONE);
        }

        TextView watchlistBadgeView = viewHolder.getWatchlistBadgeView();

        if (showAddedToBadge && database.animeToWatchDAO().countByMALId(localItems.get(position).malid) == 1) {
            watchlistBadgeView.setText(context.getString(R.string.in_towatch_badge_text));

            watchlistBadgeView.setVisibility(View.VISIBLE);
        } else {
            watchlistBadgeView.setVisibility(View.GONE);
        }

        viewHolder.itemView.setOnClickListener(v -> {
            Utils.animateClickOnItem(v, () -> {
                if (listener != null)
                    listener.onClick(viewHolder.itemView, viewHolder.getAdapterPosition());
            });
        });

        viewHolder.getDeleteButton().setOnClickListener(v -> {
            Utils.animateClickOnItem(v, () -> {
                if (listener != null)
                    listener.onClick2(viewHolder.getDeleteButton(), viewHolder.getAdapterPosition());
            });
        });
    }

    @Override
    public int getItemCount() {
        return localItems.size();
    }

    public PlaylistStreamEntity getItem(int position) {
        return this.localItems.get(position);
    }

    public List<PlaylistStreamEntity> getItems() {
        if (this.localItems == null) {
            return Collections.emptyList();
        }

        return this.localItems;
    }

    public void moveItems(int oldPos, int newPos){
        PlaylistStreamEntity removed=localItems.remove(oldPos);
        localItems.add(newPos, removed);

        notifyItemMoved(oldPos, newPos);
    }

    public void removeItem(int pos){
        localItems.remove(pos);

        notifyItemRemoved(pos);
    }

}

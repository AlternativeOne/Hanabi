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
import com.lexoff.animediary.ImageLoaderWrapper;
import com.lexoff.animediary.Info.AnimeSearchItemInfo;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Utils;

import java.util.Collections;
import java.util.List;

public class TrendingItemsAdapter extends RecyclerView.Adapter<TrendingItemsAdapter.ViewHolder> {

    private List<AnimeSearchItemInfo> localItems;

    private CustomOnItemClickListener listener;

    private Context context;

    private AppDatabase database;

    private boolean showAddedToBadge=true, showEnglishTitles=false;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnailView;
        private final TextView titleView, libraryBadgeView, watchlistBadgeView;

        public ViewHolder(View view) {
            super(view);

            thumbnailView=view.findViewById(R.id.thumbnail_view);
            titleView = view.findViewById(R.id.title_view);
            libraryBadgeView=view.findViewById(R.id.library_badge_view);
            watchlistBadgeView=view.findViewById(R.id.watchlist_badge_view);
        }

        public ImageView getThumbnailView() {
            return thumbnailView;
        }

        public TextView getTitleView() {
            return titleView;
        }

        public TextView getLibraryBadgeView(){
            return libraryBadgeView;
        }

        public TextView getWatchlistBadgeView(){
            return watchlistBadgeView;
        }
    }

    public TrendingItemsAdapter(Context context, List<AnimeSearchItemInfo> items, CustomOnItemClickListener listener) {
        this.localItems=items;
        this.listener=listener;
        this.context=context;
        this.database=ADatabase.getInstance(context);
    }

    public void setAdditionalParams(boolean showAddedToBadge, boolean showEnglishTitles){
        this.showAddedToBadge=showAddedToBadge;
        this.showEnglishTitles=showEnglishTitles;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.trending_grid_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Utils.resizeImageView(viewHolder.getThumbnailView(), Constants.GRID_ITEM_DUMMY_BITMAP_WIDTH, Constants.GRID_ITEM_DUMMY_BITMAP_HEIGHT);

        ImageLoaderWrapper.loadImageAndResizeWithPlaceholder(localItems.get(position).getThumbnailUrl(),
                viewHolder.getThumbnailView(),
                Constants.GRID_ITEM_DUMMY_BITMAP_WIDTH,
                Constants.GRID_ITEM_DUMMY_BITMAP_HEIGHT,
                () -> {
                    viewHolder.getThumbnailView().setImageResource(R.drawable.dummy_no_thumbnail);
                });

        String title = localItems.get(position).getTitle();
        if (showEnglishTitles && !localItems.get(position).getSecondaryTitle().isEmpty()) {
            title = localItems.get(position).getSecondaryTitle();
        }
        viewHolder.getTitleView().setText(title);

        TextView libraryBadgeView = viewHolder.getLibraryBadgeView();

        if (showAddedToBadge && database.animeWatchedDAO().countByMALId(localItems.get(position).getMalid()) == 1) {
            libraryBadgeView.setText(context.getString(R.string.in_watched_badge_text));

            libraryBadgeView.setVisibility(View.VISIBLE);
        } else {
            libraryBadgeView.setVisibility(View.GONE);
        }

        TextView watchlistBadgeView = viewHolder.getWatchlistBadgeView();

        if (showAddedToBadge && database.animeToWatchDAO().countByMALId(localItems.get(position).getMalid()) == 1) {
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
    }

    @Override
    public int getItemCount() {
        return localItems.size();
    }

    public AnimeSearchItemInfo getItem(int position){
        return this.localItems.get(position);
    }

    public List<AnimeSearchItemInfo> getItems(){
        if (this.localItems == null) {
            return Collections.emptyList();
        }

        return this.localItems;
    }

    public void addItems(List<AnimeSearchItemInfo> items){
        this.localItems.addAll(items);

        notifyDataSetChanged();
    }

}

package com.lexoff.animediary.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
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
import com.lexoff.animediary.Info.CompanySearchItemInfo;
import com.lexoff.animediary.Info.SearchItemInfo;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Utils;

import java.util.ArrayList;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {

    private ArrayList<SearchItemInfo> localItems;

    private CustomOnItemClickListener listener;

    private Context context;

    private AppDatabase database;

    private boolean showAddedToBadge=true;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnailView;
        private final TextView titleView, summaryView, libraryBadgeView, watchlistBadgeView;

        public ViewHolder(View view) {
            super(view);

            thumbnailView=view.findViewById(R.id.thumbnail_view);
            titleView = view.findViewById(R.id.title_view);
            summaryView=view.findViewById(R.id.summary_view);
            libraryBadgeView=view.findViewById(R.id.library_badge_view);
            watchlistBadgeView=view.findViewById(R.id.watchlist_badge_view);
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
    }

    public SearchResultsAdapter(Context context, ArrayList<SearchItemInfo> items, CustomOnItemClickListener listener) {
        this.localItems=items;
        this.listener=listener;
        this.context=context;
        this.database=ADatabase.getInstance(context);
    }

    public void setAdditionalParams(boolean showAddedToBadge){
        this.showAddedToBadge=showAddedToBadge;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.search_results_item, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        SearchItemInfo sItem=localItems.get(position);

        if (sItem instanceof AnimeSearchItemInfo) {
            AnimeSearchItemInfo item=(AnimeSearchItemInfo) sItem;

            Utils.resizeImageView(viewHolder.getThumbnailView(), Constants.GRID_ITEM_DUMMY_BITMAP_WIDTH, Constants.GRID_ITEM_DUMMY_BITMAP_HEIGHT);

            ImageLoaderWrapper.loadImageWithPlaceholder(item.getThumbnailUrl(), viewHolder.getThumbnailView(), () -> {
                viewHolder.getThumbnailView().setImageResource(R.drawable.dummy_no_thumbnail);
            });

            viewHolder.getTitleView().setText(item.getTitle());
            viewHolder.getSummaryView().setText(item.getSummary());

            TextView libraryBadgeView = viewHolder.getLibraryBadgeView();

            if (showAddedToBadge && database.animeWatchedDAO().countByMALId(item.getMalid()) == 1) {
                libraryBadgeView.setText(context.getString(R.string.in_watched_badge_text));

                libraryBadgeView.setVisibility(View.VISIBLE);
            } else {
                libraryBadgeView.setVisibility(View.GONE);
            }

            TextView watchlistBadgeView = viewHolder.getWatchlistBadgeView();

            if (showAddedToBadge && database.animeToWatchDAO().countByMALId(item.getMalid()) == 1) {
                watchlistBadgeView.setText(context.getString(R.string.in_towatch_badge_text));

                watchlistBadgeView.setVisibility(View.VISIBLE);
            } else {
                watchlistBadgeView.setVisibility(View.GONE);
            }
        } else if (sItem instanceof CompanySearchItemInfo){
            CompanySearchItemInfo item=(CompanySearchItemInfo) sItem;

            viewHolder.getLibraryBadgeView().setVisibility(View.GONE);
            viewHolder.getWatchlistBadgeView().setVisibility(View.GONE);

            Utils.resizeImageView(viewHolder.getThumbnailView(), Constants.GRID_COMPANY_ITEM_BITMAP_WIDTH, Constants.GRID_COMPANY_ITEM_BITMAP_WIDTH);

            ImageLoaderWrapper.loadImageWithPlaceholder(item.getThumbnailUrl(), viewHolder.getThumbnailView(), () -> {
                Bitmap dummyBmp = Bitmap.createBitmap(Constants.GRID_COMPANY_ITEM_BITMAP_WIDTH, Constants.GRID_COMPANY_ITEM_BITMAP_WIDTH, Bitmap.Config.ARGB_8888);
                viewHolder.getThumbnailView().setImageBitmap(dummyBmp);
            });

            viewHolder.getTitleView().setText(item.getName());
            viewHolder.getSummaryView().setText(item.getInfo());
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

    public void addItems(ArrayList<SearchItemInfo> items){
        this.localItems.addAll(items);

        notifyDataSetChanged();
    }

    public SearchItemInfo getItem(int position){
        return this.localItems.get(position);
    }

}

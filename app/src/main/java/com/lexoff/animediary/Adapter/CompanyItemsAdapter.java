package com.lexoff.animediary.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lexoff.animediary.Constants;
import com.lexoff.animediary.CustomOnItemClickListener;
import com.lexoff.animediary.Database.ADatabase;
import com.lexoff.animediary.Database.AppDatabase;
import com.lexoff.animediary.Info.CompanyAnimeItemInfo;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Util.ImageLoaderWrapper;
import com.lexoff.animediary.Util.Utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CompanyItemsAdapter extends RecyclerView.Adapter<CompanyItemsAdapter.ViewHolder> {

    private List<CompanyAnimeItemInfo> localItems;

    private CustomOnItemClickListener listener;

    private Context context;

    private AppDatabase database;

    private boolean showAddedToBadge=true;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnailView;
        private final TextView titleView, categoryView, libraryBadgeView, watchlistBadgeView;

        public ViewHolder(View view) {
            super(view);

            thumbnailView=view.findViewById(R.id.thumbnail_view);
            titleView = view.findViewById(R.id.title_view);
            categoryView=view.findViewById(R.id.category_view);
            libraryBadgeView=view.findViewById(R.id.library_badge_view);
            watchlistBadgeView=view.findViewById(R.id.watchlist_badge_view);
        }

        public ImageView getThumbnailView() {
            return thumbnailView;
        }

        public TextView getTitleView() {
            return titleView;
        }

        public TextView getCategoryView(){
            return categoryView;
        }

        public TextView getLibraryBadgeView(){
            return libraryBadgeView;
        }

        public TextView getWatchlistBadgeView(){
            return watchlistBadgeView;
        }
    }

    public CompanyItemsAdapter(Context context, List<CompanyAnimeItemInfo> items, CustomOnItemClickListener listener) {
        this.localItems=items;
        this.listener=listener;
        this.context=context;
        this.database= ADatabase.getInstance(context);
    }

    public void setAdditionalParams(boolean showAddedToBadge){
        this.showAddedToBadge=showAddedToBadge;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.company_grid_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        CompanyAnimeItemInfo item = localItems.get(position);

        Utils.resizeImageView(viewHolder.getThumbnailView(), Constants.GRID_ITEM_DUMMY_BITMAP_WIDTH, Constants.GRID_ITEM_DUMMY_BITMAP_HEIGHT);

        ImageLoaderWrapper.loadImageWithPlaceholder(item.getThumbnailUrl(),
                viewHolder.getThumbnailView(),
                () -> {
                    viewHolder.getThumbnailView().setImageResource(R.drawable.dummy_no_thumbnail);
                }
        );

        viewHolder.getTitleView().setText(item.getTitle());
        viewHolder.getCategoryView().setText(item.getCategory());

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

    public CompanyAnimeItemInfo getItem(int position){
        return this.localItems.get(position);
    }

    public List<CompanyAnimeItemInfo> getItems(){
        if (this.localItems == null) {
            return Collections.emptyList();
        }

        return this.localItems;
    }

    public void sortByName(){
        localItems.sort(new Comparator<CompanyAnimeItemInfo>() {
            @Override
            public int compare(CompanyAnimeItemInfo o1, CompanyAnimeItemInfo o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });

        notifyDataSetChanged();
    }

    public void addItems(List<CompanyAnimeItemInfo> items){
        int start=localItems.size();

        localItems.addAll(items);

        notifyItemRangeChanged(start, items.size());
    }

}

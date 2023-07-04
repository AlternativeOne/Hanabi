package com.lexoff.animediary.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lexoff.animediary.Constants;
import com.lexoff.animediary.CustomOnItemClickListener;
import com.lexoff.animediary.Database.AnimeWatchedEntity;
import com.lexoff.animediary.ImageLoaderWrapper;
import com.lexoff.animediary.ListMode;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WatchedRecordsAdapter extends RecyclerView.Adapter<WatchedRecordsAdapter.ViewHolder> {

    private ArrayList<AnimeWatchedEntity> localItems;

    private ArrayList<AnimeWatchedEntity> origLocalItems;

    private ListMode listMode;

    private CustomOnItemClickListener listener;

    private boolean showEpisodesLeftBadge=true, showEnglishTitles=false;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnailView;
        private final TextView titleView, summaryView, badgeView;

        public ViewHolder(View view) {
            super(view);

            thumbnailView=view.findViewById(R.id.thumbnail_view);
            titleView = view.findViewById(R.id.title_view);
            summaryView=view.findViewById(R.id.summary_view);
            badgeView=view.findViewById(R.id.badge_view);
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

        public TextView getBadgeView(){
            return badgeView;
        }
    }

    public WatchedRecordsAdapter(List<AnimeWatchedEntity> items, ListMode listMode, CustomOnItemClickListener listener) {
        this.localItems=new ArrayList<>(items);
        this.origLocalItems=new ArrayList<>(items);
        this.listMode=listMode;
        this.listener=listener;
    }

    public void setAdditionalParams(boolean showEpisodesLeftBadge, boolean showEnglishTitles){
        this.showEpisodesLeftBadge=showEpisodesLeftBadge;
        this.showEnglishTitles=showEnglishTitles;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (listMode==ListMode.GRID) {
            return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.watched_grid_item, viewGroup, false));
        } else {
            return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.watched_item, viewGroup, false));
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        if (listMode==ListMode.LIST) {
            Utils.resizeImageView(viewHolder.getThumbnailView(), Constants.GRID_ITEM_DUMMY_BITMAP_WIDTH, Constants.GRID_ITEM_DUMMY_BITMAP_HEIGHT);

            ImageLoaderWrapper.loadImageWithPlaceholder(localItems.get(position).thumbnail_url, viewHolder.getThumbnailView(), () -> {
                viewHolder.getThumbnailView().setImageResource(R.drawable.dummy_no_thumbnail);
            });
        } else if (listMode==ListMode.GRID){
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

        String title=localItems.get(position).title;
        if (showEnglishTitles && !localItems.get(position).second_title.isEmpty()){
            title=localItems.get(position).second_title;
        }

        viewHolder.getTitleView().setText(title);

        TextView summaryView=viewHolder.getSummaryView();
        if (summaryView != null) {
            String summary = Utils.unescapeStr(localItems.get(position).summary);
            summary = Utils.formatSummary(summary);
            summaryView.setText(Utils.shortenSummary(summary, 200));
        }

        int watched=0;
        try {
            watched=Integer.parseInt(localItems.get(position).watched_episodes);
        } catch (Exception e){}
        int unwatched=localItems.get(position).epcount-watched;

        TextView badgeView=viewHolder.getBadgeView();

        if (showEpisodesLeftBadge && unwatched>0) {
            badgeView.setText(String.valueOf(unwatched));

            badgeView.setVisibility(View.VISIBLE);
        } else {
            if (localItems.get(position).epcount==0){
                badgeView.setText("?");

                badgeView.setVisibility(View.VISIBLE);
            } else {
                badgeView.setVisibility(View.INVISIBLE);
            }
        }

        viewHolder.itemView.setOnClickListener(v -> {
            Utils.animateClickOnItem(v, () -> {
                if (listener!=null) listener.onClick(viewHolder.itemView, viewHolder.getAdapterPosition());
            });
        });
    }

    @Override
    public int getItemCount() {
        return localItems.size();
    }

    public AnimeWatchedEntity getItem(int position){
        return this.localItems.get(position);
    }

    public void sortByName(boolean desc){
        localItems.sort(new Comparator<AnimeWatchedEntity>() {
            @Override
            public int compare(AnimeWatchedEntity o1, AnimeWatchedEntity o2) {
                String title1=o1.title;
                if (showEnglishTitles && !o1.second_title.isEmpty()){
                    title1=o1.second_title;
                }

                String title2=o2.title;
                if (showEnglishTitles && !o2.second_title.isEmpty()){
                    title2=o2.second_title;
                }

                return desc ? title1.compareTo(title2) : title2.compareTo(title1);
            }
        });

        notifyDataSetChanged();
    }

    /*
    public void sortByAddingDate(){
        localItems.sort(new Comparator<AnimeWatchedEntity>() {
            @Override
            public int compare(AnimeWatchedEntity o1, AnimeWatchedEntity o2) {
                if (o1.added>o2.added) return 1;
                if (o2.added>o1.added) return -1;
                else if (o1.added==o2.added) { //just a trick
                    if (o1.id>o2.id) return 1;
                    else return -1;
                }
                else return 0;
            }
        });

        notifyDataSetChanged();
    }
    */

    public void sortByUpdatingDate(boolean desc){
        localItems.sort(new Comparator<AnimeWatchedEntity>() {
            @Override
            public int compare(AnimeWatchedEntity o1, AnimeWatchedEntity o2) {
                long upd1=desc ? (o1.updated_at>0 ? o1.updated_at : o1.added) : (o2.updated_at>0 ? o2.updated_at : o2.added);
                long upd2=desc ? (o2.updated_at>0 ? o2.updated_at : o2.added) : (o1.updated_at>0 ? o1.updated_at : o1.added);

                if (upd1>upd2) return 1;
                if (upd2>upd1) return -1;
                else if (upd1==upd2) { //just a trick
                    if ((desc && o1.id>o2.id) || (!desc && o2.id>o1.id)) return 1;
                    else return -1;
                }
                else return 0;
            }
        });

        notifyDataSetChanged();
    }

    public void filter(String query, boolean finished){
        localItems=new ArrayList<>(origLocalItems);

        if (query.isEmpty() && !finished){
            notifyDataSetChanged();

            return;
        }

        if (!query.isEmpty()) {
            for (int i = 0; i < localItems.size(); i++) {
                AnimeWatchedEntity item = localItems.get(i);

                if (!Utils.containsIgnoreCase(item.title, query) && !Utils.containsIgnoreCase(item.second_title, query)) {
                    localItems.remove(i);

                    i--;
                }
            }
        }

        if (finished) {
            for (int i = 0; i < localItems.size(); i++) {
                AnimeWatchedEntity item = localItems.get(i);
                int watchedEpisodes = Integer.parseInt(item.watched_episodes);

                if (item.epcount == 0 || watchedEpisodes != item.epcount) {
                    localItems.remove(i);

                    i--;
                }
            }
        }

        notifyDataSetChanged();
    }

    public List<AnimeWatchedEntity> getItems(){
        if (this.localItems == null) {
            return Collections.emptyList();
        }

        return  this.origLocalItems;
    }

}

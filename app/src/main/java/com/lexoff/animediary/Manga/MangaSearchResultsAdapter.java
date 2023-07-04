package com.lexoff.animediary.Manga;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lexoff.animediary.Constants;
import com.lexoff.animediary.CustomOnItemClickListener;
import com.lexoff.animediary.ImageLoaderWrapper;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Utils;

import java.util.ArrayList;

public class MangaSearchResultsAdapter extends RecyclerView.Adapter<MangaSearchResultsAdapter.ViewHolder> {

    private ArrayList<MangaItemInfo> localItems;

    private CustomOnItemClickListener listener;

    private Context context;

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

    public MangaSearchResultsAdapter(Context context, ArrayList<MangaItemInfo> items, CustomOnItemClickListener listener) {
        this.localItems=items;
        this.listener=listener;
        this.context=context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.manga_item, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Utils.resizeImageView(viewHolder.getThumbnailView(), Constants.GRID_ITEM_DUMMY_BITMAP_WIDTH, Constants.GRID_ITEM_DUMMY_BITMAP_HEIGHT);

        ImageLoaderWrapper.loadImageWithPlaceholder(localItems.get(position).getThumbnailUrl(), viewHolder.getThumbnailView(), () -> {
            viewHolder.getThumbnailView().setImageResource(R.drawable.dummy_no_thumbnail);
        });

        viewHolder.getTitleView().setText(localItems.get(position).getTitle());
        viewHolder.getSummaryView().setText(Utils.shortenSummary(localItems.get(position).getDescription(), 200));

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

    public void addItems(ArrayList<MangaItemInfo> items){
        this.localItems.addAll(items);

        notifyDataSetChanged();
    }

    public MangaItemInfo getItem(int position){
        return this.localItems.get(position);
    }

}
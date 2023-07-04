package com.lexoff.animediary.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lexoff.animediary.Constants;
import com.lexoff.animediary.ImageLoaderWrapper;
import com.lexoff.animediary.Info.ChartsItemInfo;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChartsItemsAdapter extends RecyclerView.Adapter<ChartsItemsAdapter.ViewHolder> {

    private List<ChartsItemInfo> localItems;

    private Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnailView;
        private final TextView titleView, descriptionView, positionView;

        public ViewHolder(View view) {
            super(view);

            thumbnailView=view.findViewById(R.id.thumbnail_view);
            titleView = view.findViewById(R.id.title_view);
            descriptionView=view.findViewById(R.id.description_view);
            positionView=view.findViewById(R.id.position_view);
        }

        public ImageView getThumbnailView() {
            return thumbnailView;
        }

        public TextView getTitleView() {
            return titleView;
        }

        public TextView getDescriptionView(){
            return descriptionView;
        }

        public TextView getPositionView(){
            return positionView;
        }
    }

    public ChartsItemsAdapter(Context context, List<ChartsItemInfo> items) {
        this.localItems=items;
        Collections.sort(this.localItems, new Comparator<ChartsItemInfo>() {
            @Override
            public int compare(ChartsItemInfo o1, ChartsItemInfo o2) {
                if (o1.getPosition()>o2.getPosition()) return 1;
                else if (o2.getPosition()>o1.getPosition()) return -1;
                else return 0;
            }
        });
        this.context=context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.charts_item, viewGroup, false));
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

        int postn=localItems.get(position).getPosition();
        int prevPostn=localItems.get(position).getPrevPosition();
        int stagnation = prevPostn - postn;
        String stagnationStr = prevPostn==0 ? context.getString(R.string.charts_new) : (stagnation < 0 ? stagnation + "" : (stagnation>0 ? "+" + stagnation : localItems.get(position).getStagnation()));
        String positionStr=(stagnation==0 && stagnationStr.isEmpty()) ? String.format("%d", postn) : String.format("%d (%s)", postn, stagnationStr);
        viewHolder.getPositionView().setText(positionStr);

        viewHolder.getTitleView().setText(localItems.get(position).getTitle());
        viewHolder.getDescriptionView().setText(localItems.get(position).getDescription());
    }

    @Override
    public int getItemCount() {
        return localItems.size();
    }

    public ChartsItemInfo getItem(int position){
        return this.localItems.get(position);
    }

    public List<ChartsItemInfo> getItems(){
        if (this.localItems == null) {
            return Collections.emptyList();
        }

        return  this.localItems;
    }

}

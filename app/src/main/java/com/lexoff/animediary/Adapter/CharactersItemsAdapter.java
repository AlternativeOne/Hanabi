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
import com.lexoff.animediary.Util.ImageLoaderWrapper;
import com.lexoff.animediary.Info.CharacterVAInfo;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Util.Utils;

import java.util.ArrayList;

public class CharactersItemsAdapter extends RecyclerView.Adapter<CharactersItemsAdapter.ViewHolder> {

    private ArrayList<CharacterVAInfo> localItems;

    private CustomOnItemClickListener listener;

    private Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView characterThumbnailView, vaThumbnailView;
        private final TextView characterNameView, vaNameView;
        private final TextView roleView, languageView;

        public ViewHolder(View view) {
            super(view);

            characterThumbnailView=view.findViewById(R.id.char_thumbnail_view);
            characterNameView = view.findViewById(R.id.char_name_view);

            roleView=view.findViewById(R.id.role_view);

            vaThumbnailView=view.findViewById(R.id.va_thumbnail_view);
            vaNameView=view.findViewById(R.id.va_name_view);

            languageView=view.findViewById(R.id.language_view);
        }

        public ImageView getCharacterThumbnailView() {
            return characterThumbnailView;
        }

        public TextView getCharacterNameView() {
            return characterNameView;
        }

        public TextView getRoleView(){
            return roleView;
        }

        public ImageView getVAThumbnailView(){
            return vaThumbnailView;
        }

        public TextView getVANameView() {
            return vaNameView;
        }

        public TextView getLanguageView(){
            return languageView;
        }
    }

    public CharactersItemsAdapter(Context context, ArrayList<CharacterVAInfo> items, CustomOnItemClickListener listener) {
        this.localItems=items;
        this.listener=listener;
        this.context=context;
    }

    @Override
    public CharactersItemsAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.character_item, viewGroup, false);

        return new CharactersItemsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CharactersItemsAdapter.ViewHolder viewHolder, final int position) {
        int textviewWidth=(Utils.getDisplaySize(context)[0]/2)-Utils.dpToPx(context, 105);

        CharacterVAInfo item=localItems.get(position);

        Utils.resizeImageView(viewHolder.getCharacterThumbnailView(), Constants.GRID_ITEM_DUMMY_BITMAP_WIDTH, Constants.GRID_ITEM_DUMMY_BITMAP_HEIGHT);

        ImageLoaderWrapper.loadImageWithPlaceholder(item.getCharacterThumbnailUrl(), viewHolder.getCharacterThumbnailView(), () -> {
            viewHolder.getCharacterThumbnailView().setImageResource(R.drawable.dummy_no_thumbnail);
        });

        viewHolder.getCharacterNameView().setWidth(textviewWidth);
        viewHolder.getCharacterNameView().setText(item.getCharacterName());

        viewHolder.getRoleView().setWidth(textviewWidth);
        viewHolder.getRoleView().setText(Utils.fromCapitalLetter(item.getCharacterRole()));

        Utils.resizeImageView(viewHolder.getVAThumbnailView(), Constants.GRID_ITEM_DUMMY_BITMAP_WIDTH, Constants.GRID_ITEM_DUMMY_BITMAP_HEIGHT);

        ImageLoaderWrapper.loadImageWithPlaceholder(item.getVAThumbnailUrl(), viewHolder.getVAThumbnailView(), () -> {
            viewHolder.getVAThumbnailView().setVisibility(View.INVISIBLE);
        });

        viewHolder.getVANameView().setWidth(textviewWidth);
        viewHolder.getVANameView().setText(item.getVAName());

        viewHolder.getLanguageView().setWidth(textviewWidth);
        viewHolder.getLanguageView().setText(item.getVALanguage());

        /*
        viewHolder.itemView.setOnClickListener(v -> {
            Utils.animateClickOnItem(v, () -> {
                if (listener != null)
                    listener.onClick(viewHolder.itemView, viewHolder.getAdapterPosition());
            });
        });
        */
    }

    @Override
    public int getItemCount() {
        return localItems.size();
    }

    public void addItems(ArrayList<CharacterVAInfo> items){
        this.localItems.addAll(items);

        notifyDataSetChanged();
    }

    public CharacterVAInfo getItem(int position){
        return this.localItems.get(position);
    }


}

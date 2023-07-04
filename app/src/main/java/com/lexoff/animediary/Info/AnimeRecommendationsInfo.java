package com.lexoff.animediary.Info;

import java.util.ArrayList;

public class AnimeRecommendationsInfo extends Info {

    private ArrayList<AnimeSearchItemInfo> items;

    private Exception error=null;

    public AnimeRecommendationsInfo(){
        items=new ArrayList<>();
    }

    public void addItem(AnimeSearchItemInfo item){
        items.add(item);
    }

    public ArrayList<AnimeSearchItemInfo> getItems(){
        return items;
    }

    public Exception getError() {
        return error;
    }

    public void setError(Exception error) {
        this.error = error;
    }

    public boolean hasErrors(){
        return error!=null;
    }

}

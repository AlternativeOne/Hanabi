package com.lexoff.animediary.Info;

import java.util.ArrayList;

public class SeasonInfo extends Info {

    private ArrayList<AnimeSearchItemInfo> items;

    private int maxPage;

    private Exception error=null;

    public SeasonInfo(){
        items=new ArrayList<>();
    }

    public void addItem(AnimeSearchItemInfo item){
        items.add(item);
    }

    public ArrayList<AnimeSearchItemInfo> getItems(){
        return items;
    }

    public int getMaxPage() {
        return maxPage;
    }

    public void setMaxPage(int maxPage) {
        this.maxPage = maxPage;
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

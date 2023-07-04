package com.lexoff.animediary.Manga;

import com.lexoff.animediary.Info.Info;

import java.util.ArrayList;

public class MangaInfo extends Info {

    private ArrayList<MangaItemInfo> items;

    private int totalItemsCount=0;

    private int maxPage;

    private Exception error=null;

    public MangaInfo(){
        items=new ArrayList<>();
    }

    public ArrayList<MangaItemInfo> getItems(){
        return items;
    }

    public void addItem(MangaItemInfo item){
        items.add(item);
    }

    public int getTotalItemsCount() {
        return totalItemsCount;
    }

    public void setTotalItemsCount(int totalItemsCount) {
        this.totalItemsCount = totalItemsCount;
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

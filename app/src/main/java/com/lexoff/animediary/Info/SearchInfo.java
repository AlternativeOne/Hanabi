package com.lexoff.animediary.Info;

import java.util.ArrayList;

public class SearchInfo extends Info {
    private ArrayList<SearchItemInfo> items;

    private int maxPage;

    public SearchInfo(){
        items=new ArrayList<>();
    }

    public void addItem(SearchItemInfo item){
        items.add(item);
    }

    public ArrayList<SearchItemInfo> getItems(){
        return items;
    }

    public int getMaxPage() {
        return maxPage;
    }

    public void setMaxPage(int maxPage) {
        this.maxPage = maxPage;
    }
}

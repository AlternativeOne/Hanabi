package com.lexoff.animediary.Info;

import java.util.ArrayList;
import java.util.List;

public class CompanyInfo extends Info {

    private String name;
    private String info="";
    private String thumbnailUrl="";

    private ArrayList<CompanyAnimeItemInfo> itemsAll;

    public CompanyInfo(){
        itemsAll=new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public List<CompanyAnimeItemInfo> getItems(){
        return itemsAll;
    }

    public void addItem(CompanyAnimeItemInfo item){
        this.itemsAll.add(item);
    }
}

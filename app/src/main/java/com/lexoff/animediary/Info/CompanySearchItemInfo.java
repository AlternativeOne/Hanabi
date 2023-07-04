package com.lexoff.animediary.Info;

public class CompanySearchItemInfo extends SearchItemInfo {

    private String name;
    private String info;
    private String thumbnailUrl;

    private long cmalid;

    public CompanySearchItemInfo(){
        //empty
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

    public long getCmalid() {
        return cmalid;
    }

    public void setCmalid(long cmalid) {
        this.cmalid = cmalid;
    }
}

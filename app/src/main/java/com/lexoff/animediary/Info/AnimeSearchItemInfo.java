package com.lexoff.animediary.Info;

public class AnimeSearchItemInfo extends SearchItemInfo {

    private long malid;

    private String title;
    private String secondaryTitle="";
    private String summary;
    private String thumbnailUrl;

    public AnimeSearchItemInfo(){
        //empty
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSecondaryTitle() {
        return secondaryTitle;
    }

    public void setSecondaryTitle(String secondaryTitle) {
        this.secondaryTitle = secondaryTitle;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public long getMalid() {
        return malid;
    }

    public void setMalid(long malid) {
        this.malid = malid;
    }
}

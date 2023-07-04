package com.lexoff.animediary.Info;

public class CompanyAnimeItemInfo extends Info {

    private long malid;

    private String title;
    private String thumbnailUrl;
    private String category;

    //additional info for sorting mostly

    public CompanyAnimeItemInfo(){
        //empty
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getMalid() {
        return malid;
    }

    public void setMalid(long malid) {
        this.malid = malid;
    }

}

package com.lexoff.animediary.Info;

public class ChartsItemInfo extends Info {

    private String title;
    private String description;
    private String thumbnailUrl;

    private int position;
    private int prevPosition;

    private String stagnation="";

    public ChartsItemInfo(){

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPrevPosition() {
        return prevPosition;
    }

    public void setPrevPosition(int prevPosition) {
        this.prevPosition = prevPosition;
    }

    public String getStagnation() {
        return stagnation;
    }

    public void setStagnation(String stagnation) {
        this.stagnation = stagnation;
    }
}

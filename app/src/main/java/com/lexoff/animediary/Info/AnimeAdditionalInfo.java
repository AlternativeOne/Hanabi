package com.lexoff.animediary.Info;

public class AnimeAdditionalInfo extends Info {
    private String thumbnailHiRes="";

    private long nextEpisodeAiringAt=-1;
    private int nextEpisode=-1;
    private boolean hasNextEpisode=false;

    public AnimeAdditionalInfo(){
        //empty
    }

    public String getThumbnailHiRes() {
        return thumbnailHiRes;
    }

    public void setThumbnailHiRes(String thumbnailHiRes) {
        this.thumbnailHiRes = thumbnailHiRes;
    }

    public long getNextEpisodeAiringAt() {
        return nextEpisodeAiringAt;
    }

    public void setNextEpisodeAiringAt(long nextEpisodeAiringAt) {
        this.nextEpisodeAiringAt = nextEpisodeAiringAt;
    }

    public int getNextEpisode() {
        return nextEpisode;
    }

    public void setNextEpisode(int nextEpisode) {
        this.nextEpisode = nextEpisode;
    }

    public boolean hasNextEpisode() {
        return hasNextEpisode;
    }

    public void setHasNextEpisode(boolean hasNextEpisode) {
        this.hasNextEpisode = hasNextEpisode;
    }
}

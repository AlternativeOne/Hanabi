package com.lexoff.animediary.Info;

public class AnimeInfo extends Info {
    private boolean local;

    private String title;
    private String secondTitle;

    private String summary;

    private String thumbnailUrl;

    private String type="";
    private int epcount=0;
    private String status="";
    private String aired="";
    private String producers="";
    private String studios="";
    private String sourceMaterial="";
    private String duration="";
    private String genres="";
    private String tags="";

    private String availableAt="";
    private String relations="";

    private String opening_themes="";
    private String ending_themes="";

    private long prequel_malid=0;

    private long malid;

    public AnimeInfo(){
        //empty
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSecondTitle() {
        return secondTitle;
    }

    public void setSecondTitle(String secondTitle) {
        this.secondTitle = secondTitle;
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

    public int getEpisodesCount() {
        return epcount;
    }

    public void setEpisodesCount(int epcount) {
        this.epcount = epcount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAired() {
        return aired;
    }

    public void setAired(String aired) {
        this.aired = aired;
    }

    public String getProducers() {
        return producers;
    }

    public void setProducers(String producers) {
        this.producers = producers;
    }

    public String getStudios() {
        return studios;
    }

    public void setStudios(String studios) {
        this.studios = studios;
    }

    public String getSourceMaterial() {
        return sourceMaterial;
    }

    public void setSourceMaterial(String sourceMaterial) {
        this.sourceMaterial = sourceMaterial;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public long getPrequelMalid() {
        return prequel_malid;
    }

    public void setPrequelMalid(long prequelMalid) {
        this.prequel_malid = prequelMalid;
    }

    public String getAvailableAt() {
        return availableAt;
    }

    public void setAvailableAt(String availableAt) {
        this.availableAt = availableAt;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getRelations() {
        return relations;
    }

    public void setRelations(String relations) {
        this.relations = relations;
    }

    public String getOpeningThemes() {
        return opening_themes;
    }

    public void setOpeningThemes(String opening_themes) {
        this.opening_themes = opening_themes;
    }

    public String getEndingThemes() {
        return ending_themes;
    }

    public void setEndingThemes(String ending_themes) {
        this.ending_themes = ending_themes;
    }

}

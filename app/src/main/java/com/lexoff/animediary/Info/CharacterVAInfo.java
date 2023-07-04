package com.lexoff.animediary.Info;

public class CharacterVAInfo extends Info {

    private String characterName;
    private String characterRole;
    private String characterThumbnailUrl;

    private String vaName;
    private String vaLanguage;
    private String vaThumbnailUrl;

    public CharacterVAInfo(){

    }

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public String getCharacterRole() {
        return characterRole;
    }

    public void setCharacterRole(String characterRole) {
        this.characterRole = characterRole;
    }

    public String getCharacterThumbnailUrl() {
        return characterThumbnailUrl;
    }

    public void setCharacterThumbnailUrl(String characterThumbnailUrl) {
        this.characterThumbnailUrl = characterThumbnailUrl;
    }

    public String getVAName() {
        return vaName;
    }

    public void setVAName(String vaName) {
        this.vaName = vaName;
    }

    public String getVALanguage() {
        return vaLanguage;
    }

    public void setVALanguage(String vaLanguage) {
        this.vaLanguage = vaLanguage;
    }

    public String getVAThumbnailUrl() {
        return vaThumbnailUrl;
    }

    public void setVAThumbnailUrl(String vaThumbnailUrl) {
        this.vaThumbnailUrl = vaThumbnailUrl;
    }
}

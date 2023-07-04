package com.lexoff.animediary.Info;

import java.util.ArrayList;

public class CharactersInfo extends Info {

    private ArrayList<CharacterVAInfo> items;

    private int maxPage;

    private Exception error=null;

    public CharactersInfo(){
        items=new ArrayList<>();
    }

    public ArrayList<CharacterVAInfo> getItems(){
        return items;
    }

    public void addItem(CharacterVAInfo item){
        items.add(item);
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

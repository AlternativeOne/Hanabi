package com.lexoff.animediary.Info;

import java.util.ArrayList;
import java.util.List;

public class ChartsInfo extends Info {

    private String date;

    private int week;

    private ArrayList<ChartsItemInfo> items;

    public ChartsInfo(){
        items=new ArrayList<>();
    }

    public void addItem(ChartsItemInfo item){
        items.add(item);
    }

    public List<ChartsItemInfo> getItems() {
        return items;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }
}

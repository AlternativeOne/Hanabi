package com.lexoff.animediary.Info;

import java.util.ArrayList;
import java.util.List;

public class AiringScheduleInfo extends Info {

    private ArrayList<AnimeSearchItemInfo> mondayItems;
    private ArrayList<AnimeSearchItemInfo> tuesdayItems;
    private ArrayList<AnimeSearchItemInfo> wednesdayItems;
    private ArrayList<AnimeSearchItemInfo> thursdayItems;
    private ArrayList<AnimeSearchItemInfo> fridayItems;
    private ArrayList<AnimeSearchItemInfo> saturdayItems;
    private ArrayList<AnimeSearchItemInfo> sundayItems;

    public AiringScheduleInfo(){
        mondayItems=new ArrayList<>();
        tuesdayItems=new ArrayList<>();
        wednesdayItems=new ArrayList<>();
        thursdayItems=new ArrayList<>();
        fridayItems=new ArrayList<>();
        saturdayItems=new ArrayList<>();
        sundayItems=new ArrayList<>();
    }

    public List<AnimeSearchItemInfo> getMondayItems(){
        return mondayItems;
    }

    public void addMondayItem(AnimeSearchItemInfo item){
        this.mondayItems.add(item);
    }

    public List<AnimeSearchItemInfo> getTuesdayItems(){
        return tuesdayItems;
    }

    public void addTuesdayItem(AnimeSearchItemInfo item){
        this.tuesdayItems.add(item);
    }

    public List<AnimeSearchItemInfo> getWednesdayItems(){
        return wednesdayItems;
    }

    public void addWednesdayItem(AnimeSearchItemInfo item){
        this.wednesdayItems.add(item);
    }

    public List<AnimeSearchItemInfo> getThursdayItems(){
        return thursdayItems;
    }

    public void addThursdayItem(AnimeSearchItemInfo item){
        this.thursdayItems.add(item);
    }

    public List<AnimeSearchItemInfo> getFridayItems(){
        return fridayItems;
    }

    public void addFridayItem(AnimeSearchItemInfo item){
        this.fridayItems.add(item);
    }

    public List<AnimeSearchItemInfo> getSaturdayItems(){
        return saturdayItems;
    }

    public void addSaturdayItem(AnimeSearchItemInfo item){
        this.saturdayItems.add(item);
    }

    public List<AnimeSearchItemInfo> getSundayItems(){
        return sundayItems;
    }

    public void addSundayItem(AnimeSearchItemInfo item){
        this.sundayItems.add(item);
    }

}

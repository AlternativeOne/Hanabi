package com.lexoff.animediary;

import androidx.annotation.DrawableRes;

import java.util.List;

public class AppIcon {
    public String name;
    public @DrawableRes int iconResource;
    public String alias;

    public AppIcon(String name, @DrawableRes int iconResource, String alias){
        this.name=name;
        this.iconResource=iconResource;
        this.alias=alias;
    }

    public static List<AppIcon> getIcons(){
        return List.of(
                new AppIcon("Default", R.mipmap.ic_app_icon, "Default"),
                new AppIcon("Firework", R.mipmap.ic_app_icon_firework, "Firework")
        );
    }
}

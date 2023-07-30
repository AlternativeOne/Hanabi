package com.lexoff.animediary.Util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import com.lexoff.animediary.AppIcon;
import com.lexoff.animediary.BuildConfig;
import com.lexoff.animediary.R;

import java.util.List;

public class ApplicationUtils {

    public static String IS_SHORTCUT_INTENT="is_shortcut_intent";
    public static String SHORTCUT_INTENT_TYPE="shortcut_intent_type";

    public static String SHORTCUT_SEARCH="shortcut_search";
    public static String SHORTCUT_CHARTS="shortcut_charts";

    public static void changeAppIcon(Context context, String newAlias){
        for (AppIcon appIcon : AppIcon.getIcons()){
            String className=String.format("%s.%s", BuildConfig.APPLICATION_ID, appIcon.alias);

            PackageManager packageManager=context.getPackageManager();
            packageManager.setComponentEnabledSetting(
                    new ComponentName(context.getPackageName(), className),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
            );
        }

        String newClassName=String.format("%s.%s", BuildConfig.APPLICATION_ID, newAlias);

        PackageManager packageManager=context.getPackageManager();
        packageManager.setComponentEnabledSetting(
                new ComponentName(context.getPackageName(), newClassName),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
        );
    }

    public static void buildAndPushDynamicShortcuts(Context context){
        if (Build.VERSION.SDK_INT<25) return;

        PackageManager pm=context.getPackageManager();

        Intent shortcutSearchIntent=pm.getLaunchIntentForPackage(context.getPackageName());
        shortcutSearchIntent.putExtra(IS_SHORTCUT_INTENT, true);
        shortcutSearchIntent.putExtra(SHORTCUT_INTENT_TYPE, SHORTCUT_SEARCH);

        ShortcutInfoCompat shortcutSearchInfo=new ShortcutInfoCompat.Builder(context, "hanabi:shortcut:search")
                .setShortLabel(context.getString(R.string.shortcut_search_short_label))
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_search))
                .setIntent(shortcutSearchIntent)
                .build();

        Intent shortcutChartsIntent=pm.getLaunchIntentForPackage(context.getPackageName());
        shortcutChartsIntent.putExtra(IS_SHORTCUT_INTENT, true);
        shortcutChartsIntent.putExtra(SHORTCUT_INTENT_TYPE, SHORTCUT_CHARTS);

        ShortcutInfoCompat shortcutChartsInfo=new ShortcutInfoCompat.Builder(context, "hanabi:shortcut:charts")
                .setShortLabel(context.getString(R.string.shortcut_charts_short_label))
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_charts))
                .setIntent(shortcutChartsIntent)
                .build();

        List<ShortcutInfoCompat> shortcutsList=List.of(shortcutSearchInfo, shortcutChartsInfo);
        ShortcutManagerCompat.addDynamicShortcuts(context, shortcutsList);
    }

}

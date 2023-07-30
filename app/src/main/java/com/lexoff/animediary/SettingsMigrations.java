package com.lexoff.animediary;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.lexoff.animediary.Util.Utils;

public class SettingsMigrations {

    private static SettingsMigration MIGRATION_1_2=new SettingsMigration(1, 2){
        public void migrate(SharedPreferences prefs){
            prefs.edit().remove("show_cover_tip").commit();
        }
    };

    private static SettingsMigration[] SETTINGS_MIGRATIONS=new SettingsMigration[]{
            MIGRATION_1_2
    };
    private static int SETTING_VERSION=2;

    public static void doMigrations(Context context){
        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(context);
        int currentVersion=prefs.getInt(Constants.SETTINGS_VERSION, 1);

        for (SettingsMigration migration : SETTINGS_MIGRATIONS){
            try {
                if (migration.shouldMigrate(currentVersion)){
                    migration.migrate(prefs);
                    currentVersion=migration.newVersion();
                }
            } catch (Exception e){
                prefs.edit().putInt(Constants.SETTINGS_VERSION, currentVersion).commit();

                Toast.makeText(context, String.format(context.getString(R.string.error_happened_with_message), Utils.getStringOrEmpty(e.getMessage())), Toast.LENGTH_SHORT).show();

                return;
            }
        }

        prefs.edit().putInt(Constants.SETTINGS_VERSION, currentVersion).commit();
    }

}

class SettingsMigration {
    private int oldVer, newVer;

    public SettingsMigration(int oldVer, int newVer){
        this.oldVer=oldVer;
        this.newVer=newVer;
    }

    public boolean shouldMigrate(int curVer){
        return oldVer>=curVer;
    }

    public void migrate(SharedPreferences prefs){
    }

    public int newVersion(){
        return newVer;
    }
}

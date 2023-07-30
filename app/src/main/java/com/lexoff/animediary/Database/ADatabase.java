package com.lexoff.animediary.Database;

import static com.lexoff.animediary.Database.AppDatabase.DATABASE_NAME;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class ADatabase {
    private static volatile AppDatabase databaseInstance;

    private ADatabase(){
        //empty
    }

    private static AppDatabase getDatabase(final Context context) {
        return Room
                .databaseBuilder(context.getApplicationContext(), AppDatabase.class, DATABASE_NAME)
                .addMigrations(new Migration(1, 2) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {
                        database.execSQL("CREATE TABLE IF NOT EXISTS 'towatch'"
                                + " ('id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                                + "'title' TEXT NOT NULL,"
                                + "'second_title' TEXT NOT NULL,"
                                + "'summary' TEXT NOT NULL,"
                                + "'thumbnail_url' TEXT NOT NULL,"
                                + "'malid' BIGINT NOT NULL,"
                                + "'added' BIGINT NOT NULL,"
                                + "'available_at' TEXT NOT NULL,"
                                + "'genres' TEXT NOT NULL,"
                                + "'tags' TEXT NOT NULL,"
                                + "'relations' TEXT NOT NULL,"
                                + "'opening_themes' TEXT NOT NULL,"
                                + "'opening_themes' TEXT NOT NULL,"
                                + "'source_material' TEXT NOT NULL,"
                                + "'epcount' BIGINT NOT NULL,"
                                + "'type' TEXT NOT NULL,"
                                + "'status' TEXT NOT NULL,"
                                + "'aired' TEXT NOT NULL,"
                                + "'producers' TEXT NOT NULL,"
                                + "'studios' TEXT NOT NULL,"
                                + "'duration' TEXT NOT NULL)"
                        );
                    }
                })
                .addMigrations(new Migration(2, 3) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {
                        database.execSQL("CREATE TABLE IF NOT EXISTS 'watched'"
                                + " ('id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                                + "'title' TEXT NOT NULL,"
                                + "'second_title' TEXT NOT NULL,"
                                + "'summary' TEXT NOT NULL,"
                                + "'thumbnail_url' TEXT NOT NULL,"
                                + "'malid' BIGINT NOT NULL,"
                                + "'added' BIGINT NOT NULL,"
                                + "'epcount' BIGINT NOT NULL,"
                                + "'watched_episodes' TEXT NOT NULL,"
                                + "'prequel_malid' BIGINT NOT NULL,"
                                + "'updated_at' BIGINT NOT NULL,"
                                + "'available_at' TEXT NOT NULL,"
                                + "'genres' TEXT NOT NULL,"
                                + "'tags' TEXT NOT NULL,"
                                + "'relations' TEXT NOT NULL,"
                                + "'opening_themes' TEXT NOT NULL,"
                                + "'opening_themes' TEXT NOT NULL,"
                                + "'source_material' TEXT NOT NULL,"
                                + "'type' TEXT NOT NULL,"
                                + "'status' TEXT NOT NULL,"
                                + "'aired' TEXT NOT NULL,"
                                + "'producers' TEXT NOT NULL,"
                                + "'studios' TEXT NOT NULL,"
                                + "'duration' TEXT NOT NULL)"
                        );
                    }
                })
                .addMigrations(new Migration(3, 4) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {
                        database.execSQL("CREATE TABLE IF NOT EXISTS 'notes'"
                                + " ('id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                                + "'note' TEXT NOT NULL,"
                                + "'rating' FLOAT NOT NULL,"
                                + "'malid' BIGINT NOT NULL)"
                        );
                    }
                })
                .addMigrations(new Migration(4, 5) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {
                        database.execSQL("CREATE TABLE IF NOT EXISTS 'playlists'"
                                + " ('id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                                + "'name' TEXT NOT NULL,"
                                + "'description' TEXT NOT NULL,"
                                + "'pinned' INTEGER NOT NULL)"
                        );
                    }
                })
                .addMigrations(new Migration(5, 6) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {
                        database.execSQL("CREATE TABLE IF NOT EXISTS 'playlist_streams'"
                                + " ('id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                                + "'title' TEXT NOT NULL,"
                                + "'second_title' TEXT NOT NULL,"
                                + "'summary' TEXT NOT NULL,"
                                + "'thumbnail_url' TEXT NOT NULL,"
                                + "'malid' BIGINT NOT NULL,"
                                + "'playlist_id' INTEGER NOT NULL,"
                                + "'position' INTEGER NOT NULL,"
                                + "'opening_themes' TEXT NOT NULL,"
                                + "'opening_themes' TEXT NOT NULL,"
                                + "'source_material' TEXT NOT NULL,"
                                + "'epcount' BIGINT NOT NULL,"
                                + "'type' TEXT NOT NULL,"
                                + "'status' TEXT NOT NULL,"
                                + "'aired' TEXT NOT NULL,"
                                + "'producers' TEXT NOT NULL,"
                                + "'studios' TEXT NOT NULL,"
                                + "'duration' TEXT NOT NULL,"
                                + "'available_at' TEXT NOT NULL,"
                                + "'genres' TEXT NOT NULL,"
                                + "'relations' TEXT NOT NULL,"
                                + "'tags' TEXT NOT NULL)"
                        );
                    }
                })
                .allowMainThreadQueries()
                .build();
    }

    @NonNull
    public static AppDatabase getInstance(@NonNull final Context context){
        AppDatabase result=databaseInstance;
        if (result==null){
            synchronized (ADatabase.class){
                result=databaseInstance;
                if (result==null){
                    databaseInstance=getDatabase(context);
                    result=databaseInstance;
                }
            }
        }

        return result;
    }

    public static void close(){
        if (databaseInstance!=null){
            synchronized (ADatabase.class){
                if (databaseInstance!=null){
                    databaseInstance.close();
                    databaseInstance=null;
                }
            }
        }
    }

    public static void checkpoint() {
        if (databaseInstance == null) {
            throw new IllegalStateException("database is not initialized");
        }
        final Cursor c = databaseInstance.query("pragma wal_checkpoint(full)", null);
        if (c.moveToFirst() && c.getInt(0) == 1) {
            throw new RuntimeException("Checkpoint was blocked from completing");
        }
    }

}

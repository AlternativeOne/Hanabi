package com.lexoff.animediary.Database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {AnimeToWatchEntity.class, AnimeWatchedEntity.class, NoteEntity.class, PlaylistEntity.class, PlaylistStreamEntity.class}, version = 9)
public abstract class AppDatabase extends RoomDatabase {
    public static final String DATABASE_NAME="main.db";

    public abstract AnimeToWatchDAO animeToWatchDAO();
    public abstract AnimeWatchedDAO animeWatchedDAO();
    public abstract NoteDAO noteDAO();
    public abstract PlaylistDAO playlistDAO();
    public abstract PlaylistStreamDAO playlistStreamDAO();
}

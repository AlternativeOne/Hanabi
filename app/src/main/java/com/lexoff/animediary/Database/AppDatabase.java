package com.lexoff.animediary.Database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.lexoff.animediary.Database.DAO.AnimeToWatchDAO;
import com.lexoff.animediary.Database.DAO.AnimeWatchedDAO;
import com.lexoff.animediary.Database.DAO.NoteDAO;
import com.lexoff.animediary.Database.DAO.PlaylistDAO;
import com.lexoff.animediary.Database.DAO.PlaylistStreamDAO;
import com.lexoff.animediary.Database.Model.AnimeToWatchEntity;
import com.lexoff.animediary.Database.Model.AnimeWatchedEntity;
import com.lexoff.animediary.Database.Model.NoteEntity;
import com.lexoff.animediary.Database.Model.PlaylistEntity;
import com.lexoff.animediary.Database.Model.PlaylistStreamEntity;

@Database(entities = {AnimeToWatchEntity.class, AnimeWatchedEntity.class, NoteEntity.class, PlaylistEntity.class, PlaylistStreamEntity.class}, version = 6)
public abstract class AppDatabase extends RoomDatabase {
    public static final String DATABASE_NAME="main.db";

    public abstract AnimeToWatchDAO animeToWatchDAO();
    public abstract AnimeWatchedDAO animeWatchedDAO();
    public abstract NoteDAO noteDAO();
    public abstract PlaylistDAO playlistDAO();
    public abstract PlaylistStreamDAO playlistStreamDAO();
}

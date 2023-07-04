package com.lexoff.animediary.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PlaylistDAO {
    @Query("SELECT * FROM playlists")
    List<PlaylistEntity> getAll();

    @Query("SELECT * FROM playlists WHERE id=:id")
    PlaylistEntity getById(int id);

    @Query("SELECT * FROM playlists WHERE pinned=1")
    List<PlaylistEntity> getAllPinned();

    @Insert
    void insert(PlaylistEntity record);

    @Update
    void update(PlaylistEntity record);

    @Query("UPDATE playlists SET name=:name, description=:description WHERE id=:id")
    void update(int id, String name, String description);

    @Delete
    void delete(PlaylistEntity record);

    @Query("DELETE FROM playlists WHERE id=:id")
    void deleteById(int id);

    @Query("UPDATE playlists SET pinned=:pinned WHERE id=:id")
    void setPinnedUnpinned(int id, int pinned);
}

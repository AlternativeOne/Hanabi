package com.lexoff.animediary.Database.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.lexoff.animediary.Database.Model.PlaylistStreamEntity;

import java.util.List;

@Dao
public interface PlaylistStreamDAO {
    @Query("select sum(case when playlist_id = :playlistId then 1 else 0 end) from playlist_streams")
    int countByPlaylistId(int playlistId);

    //TODO: this must be filtered by playlist_id
    @Query("SELECT * FROM playlist_streams WHERE malid=:malid")
    List<PlaylistStreamEntity> getAllByMALId(long malid);

    @Query("SELECT * FROM playlist_streams WHERE playlist_id=:playlistId ORDER BY position")
    List<PlaylistStreamEntity> getAll(int playlistId);

    @Query("select sum(case when playlist_id = :playlistId and malid = :malid then 1 else 0 end) from playlist_streams")
    int countByMALId(int playlistId, long malid);

    @Insert
    void insert(PlaylistStreamEntity record);

    @Update
    void update(PlaylistStreamEntity record);

    @Query("UPDATE playlist_streams SET title=:title, second_title=:second_title, summary=:summary, thumbnail_url=:thumbnail_url, epcount=:epcount, type=:type, status=:status, aired=:aired, producers=:producers, studios=:studios, source_material=:source_material, duration=:duration, genres=:genres, tags=:tags, available_at=:available_at, relations=:relations, opening_themes=:opening_themes, ending_themes=:ending_themes WHERE malid=:malid")
    void partialUpdate(long malid, String title, String second_title, String summary, String thumbnail_url, int epcount, String type, String status, String aired, String producers, String studios, String source_material, String duration, String genres, String tags, String available_at, String relations, String opening_themes, String ending_themes);

    @Query("UPDATE playlist_streams SET position=:newPos WHERE id=:id")
    void updatePosition(int id, int newPos);

    @Delete
    void delete(PlaylistStreamEntity record);

    @Query("DELETE FROM playlist_streams WHERE id=:id")
    void deleteById(int id);

    @Query("DELETE FROM playlist_streams WHERE playlist_id=:playlistId")
    void deleteByPlaylistId(int playlistId);

    @Query("DELETE FROM playlist_streams WHERE playlist_id=:playlistId AND malid=:malid")
    void deleteByPlaylistIdAndMALId(int playlistId, long malid);
}

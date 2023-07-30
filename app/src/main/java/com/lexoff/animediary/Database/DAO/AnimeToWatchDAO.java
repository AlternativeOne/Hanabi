package com.lexoff.animediary.Database.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.lexoff.animediary.Database.Model.AnimeToWatchEntity;

import java.util.List;

@Dao
public interface AnimeToWatchDAO {
    @Query("SELECT * FROM towatch")
    List<AnimeToWatchEntity> getAll();

    @Query("SELECT * FROM towatch WHERE malid=:malid")
    List<AnimeToWatchEntity> getAllByMALId(long malid);

    @Query("select sum(case when malid = :malid then 1 else 0 end) from towatch")
    int countByMALId(long malid);

    @Query("SELECT * FROM towatch WHERE id=:id")
    List<AnimeToWatchEntity> getAllById(int id);

    @Insert
    void insert(AnimeToWatchEntity record);

    @Update
    void update(AnimeToWatchEntity record);

    @Query("UPDATE towatch SET title=:title, second_title=:second_title, summary=:summary, thumbnail_url=:thumbnail_url, epcount=:epcount, type=:type, status=:status, aired=:aired, producers=:producers, studios=:studios, source_material=:source_material, duration=:duration, genres=:genres, tags=:tags, available_at=:available_at, relations=:relations, opening_themes=:opening_themes, ending_themes=:ending_themes WHERE malid=:malid")
    void partialUpdate(long malid, String title, String second_title, String summary, String thumbnail_url, int epcount, String type, String status, String aired, String producers, String studios, String source_material, String duration, String genres, String tags, String available_at, String relations, String opening_themes, String ending_themes);

    @Delete
    void delete(AnimeToWatchEntity record);

    @Query("DELETE FROM towatch WHERE malid=:malid")
    void deleteByMALId(long malid);

    @Query("SELECT MIN(malid) from towatch")
    long getMinMALId();
}

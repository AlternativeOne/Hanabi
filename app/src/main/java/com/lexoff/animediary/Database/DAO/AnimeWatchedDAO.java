package com.lexoff.animediary.Database.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.lexoff.animediary.Database.Model.AnimeWatchedEntity;

import java.util.List;

@Dao
public interface AnimeWatchedDAO {
    @Query("SELECT * FROM watched")
    List<AnimeWatchedEntity> getAll();

    @Query("SELECT * FROM watched WHERE malid=:malid")
    List<AnimeWatchedEntity> getAllByMALId(long malid);

    @Query("select sum(case when malid = :malid then 1 else 0 end) from watched")
    int countByMALId(long malid);

    @Query("SELECT * FROM watched WHERE id=:id")
    List<AnimeWatchedEntity> getAllById(int id);

    @Insert
    void insert(AnimeWatchedEntity record);

    @Update
    void update(AnimeWatchedEntity record);

    @Query("UPDATE watched SET title=:title, second_title=:second_title, summary=:summary, thumbnail_url=:thumbnail_url, epcount=:epcount, type=:type, status=:status, aired=:aired, producers=:producers, studios=:studios, source_material=:source_material, duration=:duration, genres=:genres, tags=:tags, available_at=:available_at, relations=:relations, opening_themes=:opening_themes, ending_themes=:ending_themes WHERE malid=:malid")
    void partialUpdate(long malid, String title, String second_title, String summary, String thumbnail_url, int epcount, String type, String status, String aired, String producers, String studios, String source_material, String duration, String genres, String tags, String available_at, String relations, String opening_themes, String ending_themes);

    @Query("UPDATE watched SET watched_episodes=:watched_episodes, updated_at=:updated_at WHERE malid=:malid")
    void updateWatchedEpisodes(long malid, String watched_episodes, long updated_at);

    @Query("UPDATE watched SET prequel_malid=:prequel_malid WHERE malid=:malid")
    void updatePrequel(long malid, long prequel_malid);

    @Delete
    void delete(AnimeWatchedEntity record);
}

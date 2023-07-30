package com.lexoff.animediary.Database.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.lexoff.animediary.Database.Model.NoteEntity;

import java.util.List;

@Dao
public interface NoteDAO {
    @Query("SELECT * FROM notes")
    List<NoteEntity> getAll();

    @Query("SELECT * FROM notes WHERE malid=:malid")
    List<NoteEntity> getAllByMALId(long malid);

    @Query("select sum(case when malid = :malid then 1 else 0 end) from notes")
    int countByMALId(long malid);

    @Insert
    void insert(NoteEntity record);

    @Update
    void update(NoteEntity record);

    @Delete
    void delete(NoteEntity record);
}

package com.lexoff.animediary.Database.Model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class NoteEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "note")
    public String note;

    @ColumnInfo(name = "rating")
    public float rating;

    @ColumnInfo(name = "malid")
    public long malid;

    public NoteEntity(String note, float rating, long malid){
        this.note=note;
        this.rating=rating;
        this.malid=malid;
    }
}

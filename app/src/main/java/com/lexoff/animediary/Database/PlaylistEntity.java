package com.lexoff.animediary.Database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "playlists")
public class PlaylistEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "name")
    @NonNull
    public String name;

    @ColumnInfo(name = "description")
    @NonNull
    public String description;

    @ColumnInfo(name = "pinned")
    @NonNull
    public int pinned;

    public PlaylistEntity(String name, String description, int pinned){
        this.name=name;
        this.description=description;
        this.pinned=pinned;
    }
}

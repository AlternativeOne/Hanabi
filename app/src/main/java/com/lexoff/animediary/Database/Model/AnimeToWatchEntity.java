package com.lexoff.animediary.Database.Model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "towatch")
public class AnimeToWatchEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "second_title")
    public String second_title;

    @ColumnInfo(name = "summary")
    public String summary;

    @ColumnInfo(name = "thumbnail_url")
    public String thumbnail_url;

    @ColumnInfo(name = "malid")
    public long malid;

    @ColumnInfo(name = "added")
    public long added;

    @ColumnInfo(name = "epcount")
    public int epcount;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "status")
    public String status;

    @ColumnInfo(name = "aired")
    public String aired;

    @ColumnInfo(name = "producers")
    public String producers;

    @ColumnInfo(name = "studios")
    public String studios;

    @ColumnInfo(name = "source_material")
    public String source_material;

    @ColumnInfo(name = "duration")
    public String duration;

    @ColumnInfo(name = "genres")
    public String genres;

    @ColumnInfo(name = "tags")
    public String tags;

    @ColumnInfo(name = "available_at")
    public String available_at;

    @ColumnInfo(name = "relations")
    public String relations;

    @ColumnInfo(name = "opening_themes")
    public String opening_themes;

    @ColumnInfo(name = "ending_themes")
    public String ending_themes;

    public AnimeToWatchEntity(String title, String second_title, String summary, String thumbnail_url, long malid, long added, int epcount, String type, String status, String aired, String producers, String studios, String source_material, String duration, String genres, String tags, String available_at, String relations, String opening_themes, String ending_themes){
        this.title=title;
        this.second_title=second_title;
        this.summary=summary;
        this.thumbnail_url=thumbnail_url;
        this.malid=malid;
        this.added=added;
        this.epcount=epcount;
        this.type=type;
        this.status=status;
        this.aired=aired;
        this.producers=producers;
        this.studios=studios;
        this.source_material=source_material;
        this.duration=duration;
        this.genres=genres;
        this.tags=tags;
        this.available_at=available_at;
        this.relations=relations;
        this.opening_themes=opening_themes;
        this.ending_themes=ending_themes;
    }
}

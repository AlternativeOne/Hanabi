package com.lexoff.animediary.Extractor;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.lexoff.animediary.Client;
import com.lexoff.animediary.Constants;
import com.lexoff.animediary.Info.AnimeSearchItemInfo;
import com.lexoff.animediary.Info.Info;
import com.lexoff.animediary.Info.SeasonInfo;

public class SeasonExtractor extends AniListBaseExtractor {

    private String dataPattern="{\"query\":\"query($page:Int = 1 $id:Int $type:MediaType $isAdult:Boolean = false $search:String $format:[MediaFormat]$status:MediaStatus $countryOfOrigin:CountryCode $source:MediaSource $season:MediaSeason $seasonYear:Int $year:String $onList:Boolean $yearLesser:FuzzyDateInt $yearGreater:FuzzyDateInt $episodeLesser:Int $episodeGreater:Int $durationLesser:Int $durationGreater:Int $chapterLesser:Int $chapterGreater:Int $volumeLesser:Int $volumeGreater:Int $licensedBy:[Int]$isLicensed:Boolean $genres:[String]$excludedGenres:[String]$tags:[String]$excludedTags:[String]$minimumTagRank:Int $sort:[MediaSort]=[POPULARITY_DESC,SCORE_DESC]){Page(page:$page,perPage:20){pageInfo{total perPage currentPage lastPage hasNextPage}media(id:$id type:$type season:$season format_in:$format status:$status countryOfOrigin:$countryOfOrigin source:$source search:$search onList:$onList seasonYear:$seasonYear startDate_like:$year startDate_lesser:$yearLesser startDate_greater:$yearGreater episodes_lesser:$episodeLesser episodes_greater:$episodeGreater duration_lesser:$durationLesser duration_greater:$durationGreater chapters_lesser:$chapterLesser chapters_greater:$chapterGreater volumes_lesser:$volumeLesser volumes_greater:$volumeGreater licensedById_in:$licensedBy isLicensed:$isLicensed genre_in:$genres genre_not_in:$excludedGenres tag_in:$tags tag_not_in:$excludedTags minimumTagRank:$minimumTagRank sort:$sort isAdult:$isAdult){id idMal title{romaji english}coverImage{extraLarge large color}startDate{year month day}endDate{year month day}bannerImage season seasonYear description type format status(version:2)episodes duration chapters volumes genres isAdult averageScore popularity nextAiringEpisode{airingAt timeUntilAiring episode}mediaListEntry{id status}studios(isMain:true){edges{isMain node{id name}}}}}}\",\"variables\":{\"page\":%d,\"type\":\"ANIME\",\"seasonYear\":%d,\"season\":\"%s\"}}";

    public SeasonExtractor(Client client, int page, int year, String season){
        super(client, "");

        setData(String.format(dataPattern, page, year, season.toUpperCase()));
    }

    @Override
    protected Info buildInfo() {
        SeasonInfo info=new SeasonInfo();

        try {
            JsonObject obj = JsonParser.object().from(response);

            if (obj.has("errors")){
                JsonArray errors=obj.getArray("errors");
                JsonObject firstError=errors.getObject(0);
                info.setError(new Exception(firstError.getString("message")));

                return info;
            }

            JsonObject data=obj.getObject("data");
            JsonObject page=data.getObject("Page");
            JsonObject pageInfo=page.getObject("pageInfo");
            JsonArray media=page.getArray("media");

            for (int i=0; i<media.size(); i++){
                JsonObject mediaObj=media.getObject(i);

                AnimeSearchItemInfo itemInfo=new AnimeSearchItemInfo();
                itemInfo.setMalid(mediaObj.getLong("idMal"));
                itemInfo.setTitle(mediaObj.getObject("title").getString("romaji"));
                String secondaryTitle=mediaObj.getObject("title").getString("english");
                itemInfo.setSecondaryTitle(secondaryTitle==null ? "" : secondaryTitle);
                itemInfo.setSummary(mediaObj.getString("description"));
                itemInfo.setThumbnailUrl(mediaObj.getObject("coverImage").getString("extraLarge"));

                //idMal==0 means this is future release
                //that hasn't connected with MAL yet
                //it's pretty rare though
                //I met only 1 such occasion in almost 60 items
                if (itemInfo.getMalid()> Constants.NON_EXIST_MALID) {
                    info.addItem(itemInfo);
                }
            }

            info.setMaxPage(pageInfo.getInt("lastPage"));
        } catch (Exception e){
            info.setError(e);
        }

        return info;
    }

}

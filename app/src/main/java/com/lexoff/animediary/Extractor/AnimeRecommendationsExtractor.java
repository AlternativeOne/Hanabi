package com.lexoff.animediary.Extractor;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.lexoff.animediary.Client;
import com.lexoff.animediary.Info.AnimeRecommendationsInfo;
import com.lexoff.animediary.Info.AnimeSearchItemInfo;
import com.lexoff.animediary.Info.Info;

public class AnimeRecommendationsExtractor extends AniListBaseExtractor {

    private String dataPattern="{\"query\":\"query media($id:Int,$type:MediaType,$isAdult:Boolean){Media(idMal:$id,type:$type,isAdult:$isAdult){recommendations(perPage:100,sort:[RATING_DESC,ID]){pageInfo{total}nodes{id mediaRecommendation{id idMal title{romaji english} coverImage{extraLarge}}}}}}\",\"variables\":{\"id\":\"%d\",\"type\":\"ANIME\",\"isAdult\":false}}";

    public AnimeRecommendationsExtractor(Client client, long malid){
        super(client, "");

        setData(String.format(dataPattern, malid));
    }

    @Override
    protected Info buildInfo() {
        AnimeRecommendationsInfo info=new AnimeRecommendationsInfo();

        try {
            JsonObject obj=JsonParser.object().from(response);

            if (obj.has("errors")){
                JsonArray errors=obj.getArray("errors");
                JsonObject firstError=errors.getObject(0);
                info.setError(new Exception(firstError.getString("message")));

                return info;
            }

            JsonObject data=obj.getObject("data");
            JsonObject media=data.getObject("Media");
            JsonObject recommendations=media.getObject("recommendations");
            JsonArray nodes=recommendations.getArray("nodes");

            for (int i=0; i<nodes.size(); i++){
                JsonObject node=nodes.getObject(i);
                JsonObject mediaRecommendation=node.getObject("mediaRecommendation");

                AnimeSearchItemInfo itemInfo=new AnimeSearchItemInfo();

                itemInfo.setTitle(mediaRecommendation.getObject("title").getString("romaji"));
                String secondaryTitle=mediaRecommendation.getObject("title").getString("english");
                itemInfo.setSecondaryTitle(secondaryTitle==null ? "" : secondaryTitle);
                itemInfo.setMalid(mediaRecommendation.getLong("idMal"));
                itemInfo.setSummary("");
                itemInfo.setThumbnailUrl(mediaRecommendation.getObject("coverImage").getString("extraLarge"));

                info.addItem(itemInfo);
            }
        } catch (Exception e){
            info.setError(e);
        }

        return info;
    }

}

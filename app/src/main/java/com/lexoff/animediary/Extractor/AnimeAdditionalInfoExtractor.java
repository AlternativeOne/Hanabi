package com.lexoff.animediary.Extractor;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.lexoff.animediary.Client;
import com.lexoff.animediary.Info.AnimeAdditionalInfo;
import com.lexoff.animediary.Info.Info;

public class AnimeAdditionalInfoExtractor extends AniListBaseExtractor {

    private String dataPattern="{\"query\":\"query ($id:Int){Media(idMal:$id,type:ANIME){id nextAiringEpisode{airingAt timeUntilAiring episode}coverImage{extraLarge large}}}\",\"variables\":{\"id\":\"%d\"}}";

    public AnimeAdditionalInfoExtractor(Client client, long malid){
        super(client, "");

        setData(String.format(dataPattern, malid));
    }

    @Override
    protected Info buildInfo() {
        AnimeAdditionalInfo info=new AnimeAdditionalInfo();

        try {
            JsonObject obj= JsonParser.object().from(response);
            JsonObject data=obj.getObject("data");
            JsonObject media=data.getObject("Media");

            JsonObject coverImage=media.getObject("coverImage");
            String thumbnailUrl=coverImage.getString("extraLarge");
            thumbnailUrl=thumbnailUrl.replace("\\", "");
            info.setThumbnailHiRes(thumbnailUrl);

            JsonObject nextAiringEpisode=media.getObject("nextAiringEpisode");
            if (nextAiringEpisode!=null && nextAiringEpisode.getInt("episode")!=0) {
                long airingAt = nextAiringEpisode.getLong("airingAt");
                info.setNextEpisodeAiringAt(airingAt);

                int episode = nextAiringEpisode.getInt("episode");
                info.setNextEpisode(episode);

                info.setHasNextEpisode(true);
            }
        } catch (Exception e){

        }

        return info;
    }

}

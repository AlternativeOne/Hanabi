package com.lexoff.animediary.Extractor;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.lexoff.animediary.ChartsCategory;
import com.lexoff.animediary.Client;
import com.lexoff.animediary.Info.ChartsInfo;
import com.lexoff.animediary.Info.ChartsItemInfo;
import com.lexoff.animediary.Info.Info;

public class ChartsExtractor extends Extractor {

    private String BASE_URL="https://us-central1-anitrendz-prod.cloudfunctions.net/animeTrendingAPI/charts";

    private String PATH_TOP_ANIME="/top-anime",
                   PATH_MALE_CHARACTERS="/male-characters",
                   PATH_FEMALE_CHARACTERS="/female-characters",
                   PATH_COUPLES="/couple-ship",
                   PATH_OP_SONG="/op-theme-songs",
                   PATH_ED_SONG="/ed-theme-songs";

    public ChartsExtractor(Client client, ChartsCategory category){
        super(client, "");

        if (category==ChartsCategory.MALE_CHARACTERS){
            setUrl(BASE_URL+PATH_MALE_CHARACTERS);
        } else if (category==ChartsCategory.FEMALE_CHARACTERS){
            setUrl(BASE_URL+PATH_FEMALE_CHARACTERS);
        } else if (category==ChartsCategory.COUPLES){
            setUrl(BASE_URL+PATH_COUPLES);
        } else if (category==ChartsCategory.OP_SONGS){
            setUrl(BASE_URL+PATH_OP_SONG);
        } else if (category==ChartsCategory.ED_SONGS) {
            setUrl(BASE_URL+PATH_ED_SONG);
        } else {
            setUrl(BASE_URL+PATH_TOP_ANIME);
        }
    }

    @Override
    protected Info buildInfo() {
        ChartsInfo info=new ChartsInfo();

        try {
            JsonArray arr = JsonParser.array().from(response);
            JsonObject obj=arr.getObject(0);

            info.setDate(obj.getString("date"));
            Object week=obj.get("week");
            info.setWeek((week instanceof String) ? (Integer.parseInt((String) week)) : ((int) week));

            JsonArray choices=obj.getArray("choices");

            for (int i=0; i<choices.size(); i++){
                JsonObject jO=choices.getObject(i);

                ChartsItemInfo item=new ChartsItemInfo();

                item.setTitle(jO.getString("name"));
                String subtext=jO.getString("subText");
                if (subtext==null || subtext.isEmpty()){
                    subtext=jO.getString("subtext");
                }
                item.setDescription(subtext);
                item.setThumbnailUrl(jO.getObject("images").getString("full"));

                item.setPosition(jO.getInt("position"));
                item.setPrevPosition(jO.getInt("previously"));

                if (jO.has("stagnation")){
                    item.setStagnation(jO.getString("stagnation"));
                }

                info.addItem(item);
            }
        } catch (Exception e){

        }

        return info;
    }

}

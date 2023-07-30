package com.lexoff.animediary.Manga;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.lexoff.animediary.Client;
import com.lexoff.animediary.Extractor.Extractor;
import com.lexoff.animediary.Extractor.ExtractorUtils;
import com.lexoff.animediary.Info.Info;

public class MDSearchExtractor extends Extractor {

    private String BASE_URL="https://api.mangadex.org";
    private String PATH="/manga?title=%s&limit=20&offset=%d&includes[]=cover_art&order[relevance]=desc";

    private String COVER_URL="https://mangadex.org/covers/%s/%s";

    public MDSearchExtractor(Client client, String q, int page){
        super(client, "");

        setUrl(String.format(BASE_URL+PATH, q, (page == 1 ? 0 : 20 * (page - 1))));
    }

    @Override
    protected Info buildInfo() {
        MangaInfo info=new MangaInfo();

        try {
            JsonObject obj = JsonParser.object().from(response);

            JsonArray data=obj.getArray("data");

            for (int i=0; i<data.size(); i++){
                JsonObject dataObj=data.getObject(i);
                JsonObject attributes=dataObj.getObject("attributes");
                JsonObject title=attributes.getObject("title");
                JsonObject description=attributes.getObject("description");
                JsonArray relationships=dataObj.getArray("relationships");
                JsonObject coverArt=new JsonObject();
                for (int a=0; a<relationships.size(); a++){
                    JsonObject relationship=relationships.getObject(a);

                    if (relationship.getString("type").equals("cover_art")){
                        coverArt=relationship;

                        break;
                    }
                }

                MangaItemInfo item=new MangaItemInfo();

                String id=dataObj.getString("id");
                String titleStr=title.getString("en");
                //TODO: maybe description available in different place?
                String descriptionStr=ExtractorUtils.getStringOrEmpty(description.getString("en"));
                String thumbnailUrl=String.format(COVER_URL, id, coverArt.getObject("attributes").getString("fileName"));

                item.setId(id);
                item.setTitle(titleStr);
                item.setDescription(descriptionStr);
                item.setThumbnailUrl(thumbnailUrl);

                info.addItem(item);

            }

            int total=obj.getInt("total");
            int perPage=obj.getInt("limit");

            info.setTotalItemsCount(total);
            info.setMaxPage(total/perPage);
        } catch (Exception e){
            info.setError(e);
        }

        return info;
    }

}

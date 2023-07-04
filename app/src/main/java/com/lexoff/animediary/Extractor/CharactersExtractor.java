package com.lexoff.animediary.Extractor;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.lexoff.animediary.Client;
import com.lexoff.animediary.Info.CharacterVAInfo;
import com.lexoff.animediary.Info.CharactersInfo;
import com.lexoff.animediary.Info.Info;

public class CharactersExtractor extends Extractor {

    private String PATH="https://graphql.anilist.co";

    private String dataPattern="{\"query\":\"query media($id:Int,$page:Int){Media(idMal:$id){id characters(page:$page,sort:[ROLE,RELEVANCE,ID]){pageInfo{total perPage currentPage lastPage hasNextPage}edges{id role name voiceActorRoles(sort:[RELEVANCE,ID]){roleNotes dubGroup voiceActor{id name{userPreferred}language:languageV2 image{large}}}node{id name{userPreferred}image{large}}}}}}\",\"variables\":{\"id\":%d,\"type\":\"ANIME\",\"page\":%d}}";

    public CharactersExtractor(Client client, long malid, int page){
        super(client, "");

        setUrl(PATH);
        setData(String.format(dataPattern, malid, page));
        setPOST();
        setThrowOnErrorCodes(false);
    }

    @Override
    protected Info buildInfo() {
        CharactersInfo info=new CharactersInfo();

        try {
            JsonObject obj= JsonParser.object().from(response);

            if (obj.has("errors")){
                JsonArray errors=obj.getArray("errors");
                JsonObject firstError=errors.getObject(0);
                info.setError(new Exception(firstError.getString("message")));

                return info;
            }

            JsonObject data=obj.getObject("data");
            JsonObject media=data.getObject("Media");
            JsonObject characters=media.getObject("characters");
            JsonArray edges=characters.getArray("edges");

            for (int i=0; i<edges.size(); i++){
                JsonObject edge=edges.getObject(i);

                CharacterVAInfo item=new CharacterVAInfo();

                item.setCharacterRole(edge.getString("role"));

                JsonObject node=edge.getObject("node");
                JsonObject charName=node.getObject("name");
                item.setCharacterName(charName.getString("userPreferred"));

                JsonObject charImage=node.getObject("image");
                item.setCharacterThumbnailUrl(charImage.getString("large"));

                JsonArray voiceActorRoles=edge.getArray("voiceActorRoles");

                for (int a=0; a<voiceActorRoles.size(); a++) {
                    JsonObject voiceActorRole = voiceActorRoles.getObject(a);
                    JsonObject voiceActor = voiceActorRole.getObject("voiceActor");

                    if (voiceActor.getString("language").equals("Japanese")) {
                        JsonObject vaName = voiceActor.getObject("name");
                        item.setVAName(vaName.getString("userPreferred"));

                        JsonObject vaImage=voiceActor.getObject("image");
                        item.setVAThumbnailUrl(vaImage.getString("large"));

                        item.setVALanguage(voiceActor.getString("language"));

                        break;
                    }
                }

                info.addItem(item);
            }

            JsonObject pageInfo=characters.getObject("pageInfo");
            info.setMaxPage(pageInfo.getInt("lastPage"));
        } catch (Exception e){
            info.setError(e);
        }

        return info;
    }

}

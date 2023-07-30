package com.lexoff.animediary.Extractor;

import com.lexoff.animediary.Client;
import com.lexoff.animediary.Info.Info;

public class AniListBaseExtractor extends Extractor {
    protected String BASE_URL="https://graphql.anilist.co";

    public AniListBaseExtractor(Client client, String url){
        super(client, url);

        setUrl(BASE_URL);
        setPOST();
        setThrowOnErrorCodes(false);
    }

    @Override
    protected Info buildInfo() {
        return null;
    }
}

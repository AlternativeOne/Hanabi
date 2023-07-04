package com.lexoff.animediary.Extractor;

import com.lexoff.animediary.Client;
import com.lexoff.animediary.Info.AiringScheduleInfo;
import com.lexoff.animediary.Info.AnimeSearchItemInfo;
import com.lexoff.animediary.Info.Info;
import com.lexoff.animediary.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class AiringScheduleExtractor extends Extractor {

    private String PATH="anime/season/schedule";

    public AiringScheduleExtractor(Client client){
        super(client, "");

        setUrl(BASE_URL+PATH);
    }

    @Override
    protected Info buildInfo() {
        AiringScheduleInfo info=new AiringScheduleInfo();

        Document doc = Jsoup.parse(response);

        Element mondayElement=doc.select("div.js-seasonal-anime-list-key-monday").first();
        for (Element item : mondayElement.select("div.js-anime-type-all")){
            info.addMondayItem(parseItem(item));
        }

        Element tuesdayElement=doc.select("div.js-seasonal-anime-list-key-tuesday").first();
        for (Element item : tuesdayElement.select("div.js-anime-type-all")){
            info.addTuesdayItem(parseItem(item));
        }

        Element wednesdayElement=doc.select("div.js-seasonal-anime-list-key-wednesday").first();
        for (Element item : wednesdayElement.select("div.js-anime-type-all")){
            info.addWednesdayItem(parseItem(item));
        }

        Element thursdayElement=doc.select("div.js-seasonal-anime-list-key-thursday").first();
        for (Element item : thursdayElement.select("div.js-anime-type-all")){
            info.addThursdayItem(parseItem(item));
        }

        Element fridayElement=doc.select("div.js-seasonal-anime-list-key-friday").first();
        for (Element item : fridayElement.select("div.js-anime-type-all")){
            info.addFridayItem(parseItem(item));
        }

        Element saturdayElement=doc.select("div.js-seasonal-anime-list-key-saturday").first();
        for (Element item : saturdayElement.select("div.js-anime-type-all")){
            info.addSaturdayItem(parseItem(item));
        }

        Element sundayElement=doc.select("div.js-seasonal-anime-list-key-sunday").first();
        for (Element item : sundayElement.select("div.js-anime-type-all")){
            info.addSundayItem(parseItem(item));
        }

        return info;
    }

    private AnimeSearchItemInfo parseItem(Element item){
        AnimeSearchItemInfo itemInfo=new AnimeSearchItemInfo();

        Element linkTitle=item.select("a.link-title").first();
        itemInfo.setTitle(linkTitle.text());

        itemInfo.setMalid(ExtractorUtils.tryToExtractMALidFromLink(linkTitle.attr("href")));

        Element summary=item.select("div.synopsis").first().select("p").first();
        itemInfo.setSummary(summary.text());

        Element thumbnail=item.select("div.image").first().select("img").first();
        itemInfo.setThumbnailUrl(ExtractorUtils.tryToExtractSrcOfImg(thumbnail));

        return itemInfo;
    }

}

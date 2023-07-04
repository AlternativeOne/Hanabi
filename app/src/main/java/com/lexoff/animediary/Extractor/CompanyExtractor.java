package com.lexoff.animediary.Extractor;

import com.lexoff.animediary.Client;
import com.lexoff.animediary.Info.CompanyAnimeItemInfo;
import com.lexoff.animediary.Info.CompanyInfo;
import com.lexoff.animediary.Info.Info;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CompanyExtractor extends Extractor {

    private String PATH="anime/producer/%d";

    public CompanyExtractor(Client client, long cmalid){
        super(client, "");

        setUrl(String.format(BASE_URL+PATH, cmalid));
    }

    @Override
    protected Info buildInfo() {
        CompanyInfo info=new CompanyInfo();

        Document doc = Jsoup.parse(response);

        Element name=doc.select("#contentWrapper").first().select("h1").first();
        info.setName(name.text());

        try {
            Element details = doc.select("div.spaceit_pad").last();
            if (!details.text().startsWith("Member Fav")) {
                info.setInfo(details.text());
            }
        } catch (Exception e){

        }

        try {
            Element thumbnail=doc.select("div.logo").first().select("img").first();
            info.setThumbnailUrl(ExtractorUtils.tryToExtractSrcOfImg(thumbnail));
        } catch (Exception e){

        }

        Elements itemsAll=doc.select("div.js-anime-type-all");
        for (Element item : itemsAll){
            CompanyAnimeItemInfo itemInfo=new CompanyAnimeItemInfo();

            Element title=item.select("div.title").first();
            itemInfo.setTitle(title.text());

            Element thumbnail=item.select("div.image").first().select("img").first();
            itemInfo.setThumbnailUrl(ExtractorUtils.tryToExtractSrcOfImg(thumbnail));

            Element category=item.select("div.category").first();
            itemInfo.setCategory(category.text());

            Element titlelink=title.select("a").first();
            itemInfo.setMalid(ExtractorUtils.tryToExtractMALidFromLink(titlelink.attr("href")));

            info.addItem(itemInfo);
        }

        return info;
    }

}

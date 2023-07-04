package com.lexoff.animediary.Extractor;

import com.lexoff.animediary.Client;
import com.lexoff.animediary.Info.AnimeSearchItemInfo;
import com.lexoff.animediary.Info.CompanySearchItemInfo;
import com.lexoff.animediary.Info.Info;
import com.lexoff.animediary.Info.SearchInfo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

public class SearchExtractor extends Extractor {

    private String ANIME_PATH="anime.php?cat=anime&q=%s&show=%d",
                    ANIME_ALT_PATH="anime.php?cat=anime&q=%s";

    private String COMPANY_PATH="company?cat=company&q=%s&p=%d";

    private SearchCategory category;

    public SearchExtractor(Client client, String searchStr, SearchCategory category, int page){
        super(client, "");

        this.category=category;

        if (category==SearchCategory.ANIME) {
            //TODO: this almost surely will be reworked in future
            String advParams="";

            if (searchStr.contains(":")) {
                int a = searchStr.indexOf(":");
                advParams = searchStr.substring(a + 1).toLowerCase();
                searchStr = searchStr.substring(0, a).trim();

                if (advParams.equals("tv")) advParams = "&type=1";
                else if (advParams.equals("ova")) advParams = "&type=2";
                else if (advParams.equals("movie")) advParams = "&type=3";
                else if (advParams.equals("special")) advParams = "&type=4";
                else if (advParams.equals("ona")) advParams = "&type=5";
                else if (advParams.equals("music")) advParams = "&type=6";
            }

            if (searchStr.length() < 3) {
                if (searchStr.length() == 1) searchStr += "++";
                else if (searchStr.length() == 2) searchStr += "+";

                setUrl(String.format(BASE_URL + ANIME_ALT_PATH+advParams, searchStr));
            } else {
                setUrl(String.format(BASE_URL + ANIME_PATH+advParams, searchStr, (page == 1 ? 0 : 50 * (page - 1))));
            }
        } else if (category==SearchCategory.COMPANY){
            setUrl(String.format(BASE_URL+COMPANY_PATH, searchStr, page));
        }
    }

    @Override
    protected Info buildInfo() {
        SearchInfo info=new SearchInfo();

        if (category==SearchCategory.ANIME) {
            Document doc = Jsoup.parse(response);

            Element resultsTable = doc.select("table").last();
            Elements trs = resultsTable.select("tr");
            for (Element tr : trs) {
                try {
                    AnimeSearchItemInfo item = new AnimeSearchItemInfo();

                    Element titleDiv = tr.select("div.title").first();
                    String titleUrl = titleDiv.select("a").first().attr("href");

                    String[] splits = titleUrl.split("/");
                    for (String split : splits) {
                        try {
                            long malid = Long.parseLong(split);

                            item.setMalid(malid);

                            break;
                        } catch (Exception e) {
                            continue;
                        }
                    }

                    Element title = titleDiv.select("strong").first();
                    item.setTitle(title.text());

                    Element summary = tr.select("div.pt4").first();
                    String summaryText = Jsoup.clean(summary.html(), Whitelist.basic());
                    item.setSummary(summaryText.substring(0, summaryText.indexOf("<a")));

                    Element thumbnail = tr.select("img").first();
                    String thumbnailUrl = thumbnail.attr("data-src");
                    thumbnailUrl = thumbnailUrl.substring(0, thumbnailUrl.lastIndexOf("?"));
                    thumbnailUrl = thumbnailUrl.replace("r/50x70/", "");
                    item.setThumbnailUrl(thumbnailUrl);

                    info.addItem(item);
                } catch (Exception e) {
                    continue;
                }
            }

            try {
                Element pages = doc.select("div.fl-r.di-ib.fs11.fw-n").first();
                info.setMaxPage(Integer.parseInt(pages.select("a").last().text()));
            } catch (Exception e) {
                info.setMaxPage(1);
            }
        } else if (category==SearchCategory.COMPANY){
            Document doc = Jsoup.parse(response);

            Element resultsTable = doc.select("table").last();
            Elements trs = resultsTable.select("tr");
            for (Element tr : trs) {
                try {
                    CompanySearchItemInfo item = new CompanySearchItemInfo();

                    Element img = tr.select("img").first();
                    item.setThumbnailUrl(ExtractorUtils.tryToExtractSrcOfImg(img));

                    Element name = tr.select("td.borderClass").last().select("a").first();
                    item.setName(name.text());

                    //TODO: maybe return?
                    //Element add=tr.select("td.borderClass").last().select("small").first();
                    //item.setInfo(add.text());

                    Element cmalid=tr.select("td.borderClass").last().select("a").first();
                    item.setCmalid(Long.parseLong(cmalid.attr("href").split("/")[3]));

                    info.addItem(item);
                } catch (Exception e) {
                    continue;
                }
            }

            try {
                Element pages = doc.select("div.pagination.ac").first();
                String s=pages.select("a").last().text();
                s=s.substring(s.indexOf("-")+1).trim();
                info.setMaxPage(Integer.parseInt(s)/50);
            } catch (Exception e) {
                info.setMaxPage(1);
            }
        }

        return info;
    }

}

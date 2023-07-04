package com.lexoff.animediary.Extractor;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonWriter;
import com.lexoff.animediary.Client;
import com.lexoff.animediary.Info.AnimeInfo;
import com.lexoff.animediary.Info.Info;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

public class AnimeExtractor extends Extractor {

    private String PATH="anime/%d";

    public AnimeExtractor(Client client, long malid){
        super(client, "");

        setUrl(String.format(BASE_URL+PATH, malid));
    }

    @Override
    protected Info buildInfo() {
        AnimeInfo info = new AnimeInfo();

        info.setLocal(false);

        Document doc = Jsoup.parse(response);

        Element title = doc.select(".title-name").first();
        info.setTitle(title.text());

        try {
            Element secondTitle = doc.select(".title-english").first();
            info.setSecondTitle(secondTitle.text());
        } catch (Exception e) {
            info.setSecondTitle("");
        }

        Element summary = doc.select("tr").first().select("p").first();
        String summaryText = summary.html();
        summaryText = Jsoup.clean(summaryText, Whitelist.none().addTags("br"));
        summaryText = summaryText.replaceAll("<br> ", "\n").replace("<br>", "\n");

        //this regex is tricky
        summaryText = summaryText.replaceAll("(\\n)?(\\s+)(\\n)", "\n\n");

        info.setSummary(summaryText);

        Element thumbnail = doc.select("#content").first().select("img").first();
        info.setThumbnailUrl(thumbnail.attr("data-src"));

        try {
            Element epCount = doc.select("#curEps").first();
            info.setEpisodesCount(Integer.parseInt(epCount.text()));
        } catch (Exception e){
            info.setEpisodesCount(0);
        }

        try {
            Elements spaceits = doc.select(".spaceit_pad");
            for (int i = 0; i < spaceits.size(); i++) {
                Element spaceit = spaceits.get(i);
                String text = spaceit.text();

                if (text.startsWith("Type:")) {
                    String value = text.substring(text.indexOf(":") + 1).trim();
                    info.setType(value);
                } else if (text.startsWith("Status:")) {
                    String value = text.substring(text.indexOf(":") + 1).trim();
                    info.setStatus(value);
                } else if (text.startsWith("Aired:")) {
                    String value = text.substring(text.indexOf(":") + 1).trim();
                    info.setAired(value);
                } else if (text.startsWith("Producers:")) {
                    String t2=text.substring(text.indexOf(":") + 1).trim();
                    if (t2.startsWith("None found")){
                        info.setProducers(t2);
                    } else {
                        StringBuilder value = new StringBuilder();

                        Elements links = spaceit.select("a");
                        for (Element link : links) {
                            value.append(link.text())
                                    .append("|")
                                    .append(link.attr("href").split("/")[3])
                                    .append(";");
                        }

                        info.setProducers(value.toString());
                    }
                } else if (text.startsWith("Studios:")) {
                    String t2=text.substring(text.indexOf(":") + 1).trim();
                    if (t2.startsWith("None found")){
                        info.setStudios(t2);
                    } else {
                        StringBuilder value = new StringBuilder();

                        Elements links = spaceit.select("a");
                        for (Element link : links) {
                            value.append(link.text())
                                    .append("|")
                                    .append(link.attr("href").split("/")[3])
                                    .append(";");
                        }

                        info.setStudios(value.toString());
                    }
                } else if (text.startsWith("Duration:")) {
                    String value = text.substring(text.indexOf(":") + 1).trim();
                    info.setDuration(value);
                } else if (text.startsWith("Genres:")){
                    String s="";

                    Elements aTags=spaceit.select("a");
                    for (Element aTag : aTags){
                        s+=aTag.text()+";";
                    }

                    info.setGenres(s);
                } else if (text.startsWith("Themes:")){
                    String s="";

                    Elements aTags=spaceit.select("a");
                    for (Element aTag : aTags){
                        s+=aTag.text()+";";
                    }

                    info.setTags(s);
                } else if (text.startsWith("Source:")){
                    String t2=text.substring(text.indexOf(":") + 1).trim();

                    JsonObject obj=new JsonObject();
                    obj.put("type", t2);

                    info.setSourceMaterial(JsonWriter.string(obj));
                }
            }
        } catch (Exception e){
            //do nothing
            //AnimeInfo.class has default values set
        }

        try {
            JsonArray availableAt = new JsonArray();

            Elements broadcasts = doc.select(".broadcast");
            if (broadcasts.size()>0) {
                for (int i = 0; i < broadcasts.size(); i++) {
                    Element broadcast = broadcasts.get(i);
                    Element aTag = broadcast.select("a").first();

                    JsonObject obj=new JsonObject();
                    obj.put("title", aTag.attr("title"));
                    obj.put("url", aTag.attr("href"));

                    availableAt.add(obj);
                }

                info.setAvailableAt(JsonWriter.string(availableAt));
            }
        } catch (Exception e){
            //do nothing
            //it's VERY secondary info, so if isn't fetched it's okay
        }

        try {
            JsonArray relations=new JsonArray();

            Elements related=doc.select(".anime_detail_related_anime");
            if (related.size()>0){
                Element relatedFirst=related.first();

                Elements trs=relatedFirst.select("tr");
                for (Element tr : trs){
                    Elements tds=tr.select("td");
                    String status=tds.get(0).text().replace(":", "");

                    Elements aTags=tds.get(1).select("a");
                    for (Element aTag : aTags) {
                        String name = aTag.text();
                        long malid = ExtractorUtils.tryToExtractMALidFromLink(aTag.attr("href"));

                        if (aTag.attr("href").startsWith("/anime/")) {
                            JsonObject obj=new JsonObject();
                            obj.put("status", status);
                            obj.put("title", name);
                            obj.put("malid", malid);

                            relations.add(obj);
                        } else if (aTag.attr("href").startsWith("/manga/")) {
                            //TODO

                            JsonObject obj=JsonParser.object().from(info.getSourceMaterial());
                            obj.put("title", name);
                            info.setSourceMaterial(JsonWriter.string(obj));
                        }
                    }
                }

                info.setRelations(JsonWriter.string(relations));
            }
        } catch (Exception e){
            //do nothing?
        }

        try {
            JsonArray openingThemes=new JsonArray();

            Elements opThemesElements=doc.select(".theme-songs.opnening");
            if (opThemesElements.size()>0){
                Element opThemes=opThemesElements.first();
                Element lastTable=opThemes.select("table").last();
                Elements trs=lastTable.select("tr");
                for (Element tr : trs){
                    Elements tds=tr.select("td");
                    if (tds.size()<2) continue;

                    Element td=tds.get(1);

                    boolean parsed=false;

                    String opTitle;
                    String opArtist;
                    String opEpisodes;

                    //parse this early becuase title and artist parsing is coming next
                    try {
                        opEpisodes=td.select(".theme-song-episode").first().text();
                        opEpisodes=opEpisodes.replace("(", "").replace(")", "").replace("eps", "").trim();
                    } catch (Exception e){
                        opEpisodes="";
                    }

                    try {
                        opTitle=td.select(".theme-song-title").first().text();

                        opArtist=td.select(".theme-song-artist").first().text();

                        parsed=true;
                    } catch (Exception e){
                        opTitle=td.text();
                        opArtist=" ";

                        parsed=false;
                    }

                    if (!parsed){
                        Elements theme_song_artists=td.select(".theme-song-artist");
                        if (theme_song_artists.size()>0){
                            String s=theme_song_artists.first().text();

                            opTitle=opTitle.replace(s, "");

                            opArtist=s;

                            parsed = true;
                        }

                        Elements theme_song_episodes=td.select(".theme-song-episode");
                        if (theme_song_episodes.size()>0){
                            opTitle=opTitle.replace(theme_song_episodes.first().text(), "");
                        }

                        Elements theme_song_indexes=td.select(".theme-song-index");
                        if (theme_song_indexes.size()>0){
                            opTitle=opTitle.replace(theme_song_indexes.first().text(), "");
                        }
                    }

                    opTitle=opTitle.replaceAll("\\s", " ").trim();

                    if (!parsed) {
                        //last resort: need to parse manually

                        int a = opTitle.indexOf(": \"");
                        if (a >= 0) {
                            opTitle = opTitle.substring(a + 3);
                        }

                        int b = opTitle.indexOf("\" by ");
                        if (b >= 0) {
                            opArtist = opTitle.substring(b + 5);

                            opTitle = opTitle.substring(0, b);
                        }
                    }

                    if (opTitle.startsWith("\"")) opTitle=opTitle.substring(1);
                    if (opTitle.endsWith("\"")) opTitle=opTitle.substring(0, opTitle.length()-1);

                    if (opArtist.startsWith("by ")) opArtist=opArtist.substring(3);

                    String opSpotifyUrl="";

                    Elements inputs=tr.select("input");
                    for (Element input : inputs){
                        if (input.attr("id").startsWith("spotify_url")){
                            opSpotifyUrl=input.attr("value");

                            break;
                        }
                    }

                    JsonObject obj=new JsonObject();
                    obj.put("title", opTitle);
                    obj.put("artist", opArtist);
                    obj.put("spotify_url", opSpotifyUrl);
                    obj.put("episodes", opEpisodes);

                    openingThemes.add(obj);
                }

                info.setOpeningThemes(JsonWriter.string(openingThemes));
            }

            JsonArray endingThemes=new JsonArray();

            Elements edThemesElements=doc.select(".theme-songs.ending");
            if (edThemesElements.size()>0){
                Element edThemes=edThemesElements.first();
                Element lastTable=edThemes.select("table").last();
                Elements trs=lastTable.select("tr");
                for (Element tr : trs){
                    Elements tds=tr.select("td");
                    if (tds.size()<2) continue;

                    Element td=tds.get(1);

                    boolean parsed=false;

                    String edTitle;
                    String edArtist;
                    String edEpisodes;

                    //parse this early becuase title and artist parsing is coming next
                    try {
                        edEpisodes=td.select(".theme-song-episode").first().text();
                        edEpisodes=edEpisodes.replace("(", "").replace(")", "").replace("eps", "").trim();
                    } catch (Exception e){
                        edEpisodes="";
                    }

                    try {
                        edTitle=td.select(".theme-song-title").first().text();

                        edArtist=td.select(".theme-song-artist").first().text();

                        parsed=true;
                    } catch (Exception e){
                        edTitle=td.text();
                        edArtist=" ";

                        parsed=false;
                    }

                    if (!parsed){
                        Elements theme_song_artists=td.select(".theme-song-artist");
                        if (theme_song_artists.size()>0) {
                            String s = theme_song_artists.first().text();

                            edTitle = edTitle.replace(s, "");

                            edArtist = s;

                            parsed = true;
                        }

                        Elements theme_song_episodes=td.select(".theme-song-episode");
                        if (theme_song_episodes.size()>0){
                            edTitle=edTitle.replace(theme_song_episodes.first().text(), "");
                        }

                        Elements theme_song_indexes=td.select(".theme-song-index");
                        if (theme_song_indexes.size()>0){
                            edTitle=edTitle.replace(theme_song_indexes.first().text(), "");
                        }
                    }

                    edTitle=edTitle.replaceAll("\\s", " ").trim();

                    if (!parsed){
                        //last resort: need to parse manually

                        int a=edTitle.indexOf(": \"");
                        if (a>=0){
                            edTitle=edTitle.substring(a+3);
                        }

                        int b=edTitle.indexOf("\" by ");
                        if (b>=0){
                            edArtist=edTitle.substring(b+5);

                            edTitle=edTitle.substring(0, b);
                        }
                    }

                    if (edTitle.startsWith("\"")) edTitle=edTitle.substring(1);
                    if (edTitle.endsWith("\"")) edTitle=edTitle.substring(0, edTitle.length()-1);

                    if (edArtist.startsWith("by ")) edArtist=edArtist.substring(3);

                    String edSpotifyUrl="";

                    Elements inputs=tr.select("input");
                    for (Element input : inputs){
                        if (input.attr("id").startsWith("spotify_url")){
                            edSpotifyUrl=input.attr("value");

                            break;
                        }
                    }

                    JsonObject obj=new JsonObject();
                    obj.put("title", edTitle);
                    obj.put("artist", edArtist);
                    obj.put("spotify_url", edSpotifyUrl);
                    obj.put("episodes", edEpisodes);

                    endingThemes.add(obj);
                }

                info.setEndingThemes(JsonWriter.string(endingThemes));
            }
        } catch (Exception e){
            //do nothing
        }

        Elements borders=doc.select(".borderClass");
        for (int i=0; i<borders.size(); i++){
            Element border=borders.get(i);
            String text=border.text().toLowerCase();

            if (text.startsWith("prequel:") || text.startsWith("parent story:")){
                Element next=borders.get(i+1);
                Element aTag=next.select("a").first();
                String link=aTag.attr("href");
                link=link.replace("/anime/", "");
                link=link.split("/")[0];

                long prMalid=Long.parseLong(link);
                info.setPrequelMalid(prMalid);

                break;
            }
        }

        info.setMalid(ExtractorUtils.tryToExtractMALidFromLink(url));

        return info;
    }

}

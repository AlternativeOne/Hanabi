package com.lexoff.animediary;

import com.lexoff.animediary.Extractor.AiringScheduleExtractor;
import com.lexoff.animediary.Extractor.AnimeAdditionalInfoExtractor;
import com.lexoff.animediary.Extractor.AnimeExtractor;
import com.lexoff.animediary.Extractor.AnimeRecommendationsExtractor;
import com.lexoff.animediary.Extractor.CharactersExtractor;
import com.lexoff.animediary.Extractor.ChartsExtractor;
import com.lexoff.animediary.Extractor.CompanyExtractor;
import com.lexoff.animediary.Extractor.SeasonExtractor;
import com.lexoff.animediary.Info.CharactersInfo;
import com.lexoff.animediary.Info.SeasonInfo;
import com.lexoff.animediary.Manga.MDSearchExtractor;
import com.lexoff.animediary.Extractor.SearchCategory;
import com.lexoff.animediary.Extractor.SearchExtractor;
import com.lexoff.animediary.Extractor.TrendingExtractor;
import com.lexoff.animediary.Info.AiringScheduleInfo;
import com.lexoff.animediary.Info.AnimeAdditionalInfo;
import com.lexoff.animediary.Info.AnimeInfo;
import com.lexoff.animediary.Info.AnimeRecommendationsInfo;
import com.lexoff.animediary.Info.ChartsInfo;
import com.lexoff.animediary.Info.CompanyInfo;
import com.lexoff.animediary.Manga.MangaInfo;
import com.lexoff.animediary.Info.SearchInfo;
import com.lexoff.animediary.Info.TrendingInfo;

import java.io.IOException;

public class Api {
    private Client client;

    private LruCache cache;

    public Api(){
        client=App.getApp().getClient();

        cache=LruCache.getInstance();
    }

    //TODO: reimplement cache when proper handling of loading will be done
    public SearchInfo getSearchInfo(String searchStr, SearchCategory category, int page) throws IOException, ServiceUnavailableException {
        /*if (cache.exist(searchStr+"&"+Utils.searchCategoryToInt(category)+"&"+page)){
            return (SearchInfo) cache.getInfo(searchStr+"&"+Utils.searchCategoryToInt(category)+"&"+page);
        }*/

        SearchExtractor searchExtractor = new SearchExtractor(client, searchStr, category, page);
        SearchInfo searchInfo=(SearchInfo) searchExtractor.getInfo();

        //cache.addInfo(searchStr+"&"+Utils.searchCategoryToInt(category)+"&"+page, searchInfo);

        return searchInfo;
    }

    public AnimeInfo getAnimeInfo(long malid, boolean forceload) throws IOException, ServiceUnavailableException {
        if (!forceload && cache.exist(Constants.ANIME_INFO_CACHE_KEY_PATTERN+malid)){
            return (AnimeInfo) cache.getInfo(Constants.ANIME_INFO_CACHE_KEY_PATTERN+malid);
        }

        AnimeExtractor animeExtractor = new AnimeExtractor(client, malid);
        AnimeInfo animeInfo=(AnimeInfo) animeExtractor.getInfo();

        cache.addInfo(Constants.ANIME_INFO_CACHE_KEY_PATTERN+malid, animeInfo);

        return animeInfo;
    }

    public CompanyInfo getCompanyInfo(long cmalid) throws IOException, ServiceUnavailableException {
        if (cache.exist(Constants.COMPANY_INFO_CACHE_KEY_PATTERN+cmalid)){
            return (CompanyInfo) cache.getInfo(Constants.COMPANY_INFO_CACHE_KEY_PATTERN+cmalid);
        }

        CompanyExtractor companyExtractor = new CompanyExtractor(client, cmalid);
        CompanyInfo companyInfo=(CompanyInfo) companyExtractor.getInfo();

        cache.addInfo(Constants.COMPANY_INFO_CACHE_KEY_PATTERN+cmalid, companyInfo);

        return companyInfo;
    }

    public AnimeAdditionalInfo getAnimeAdditionalInfo(long malid) throws IOException, ServiceUnavailableException {
        AnimeAdditionalInfoExtractor extractor = new AnimeAdditionalInfoExtractor(client, malid);
        AnimeAdditionalInfo info=(AnimeAdditionalInfo) extractor.getInfo();

        return info;
    }

    public TrendingInfo getTrendingInfo(boolean forceload, int page) throws IOException, ServiceUnavailableException {
        if (!forceload && cache.exist(Constants.TRENDING_INFO_CACHE_KEY_PATTERN+page)){
            return (TrendingInfo) cache.getInfo(Constants.TRENDING_INFO_CACHE_KEY_PATTERN+page);
        }

        TrendingExtractor extractor = new TrendingExtractor(client, page);
        TrendingInfo info=(TrendingInfo) extractor.getInfo();

        cache.addInfo(Constants.TRENDING_INFO_CACHE_KEY_PATTERN+page, info);

        return info;
    }

    public AiringScheduleInfo getAiringScheduleInfo() throws IOException, ServiceUnavailableException {
        if (cache.exist(Constants.AIRING_SCHEDULE_INFO_CACHE_KEY_PATTERN)){
            return (AiringScheduleInfo) cache.getInfo(Constants.AIRING_SCHEDULE_INFO_CACHE_KEY_PATTERN);
        }

        AiringScheduleExtractor extractor = new AiringScheduleExtractor(client);
        AiringScheduleInfo airingScheduleInfo=(AiringScheduleInfo) extractor.getInfo();

        cache.addInfo(Constants.AIRING_SCHEDULE_INFO_CACHE_KEY_PATTERN, airingScheduleInfo);

        return airingScheduleInfo;
    }

    public AnimeRecommendationsInfo getAnimeRecommendationsInfo(long malid) throws IOException, ServiceUnavailableException {
        if (cache.exist(Constants.ANIME_RECOMMENDATIONS_INFO_CACHE_KEY_PATTERN+malid)){
            return (AnimeRecommendationsInfo) cache.getInfo(Constants.ANIME_RECOMMENDATIONS_INFO_CACHE_KEY_PATTERN+malid);
        }

        AnimeRecommendationsExtractor extractor = new AnimeRecommendationsExtractor(client, malid);
        AnimeRecommendationsInfo animeRecommendationsInfo=(AnimeRecommendationsInfo) extractor.getInfo();

        cache.addInfo(Constants.ANIME_RECOMMENDATIONS_INFO_CACHE_KEY_PATTERN+malid, animeRecommendationsInfo);

        return animeRecommendationsInfo;
    }

    public ChartsInfo getTopAnimeInfo(ChartsCategory category) throws IOException, ServiceUnavailableException {
        ChartsExtractor extractor=new ChartsExtractor(client, category);
        ChartsInfo info=(ChartsInfo) extractor.getInfo();

        return info;
    }

    public MangaInfo getMDSearchInfo(String query, int page) throws IOException, ServiceUnavailableException {
        MDSearchExtractor extractor=new MDSearchExtractor(client, query, page);
        MangaInfo info=(MangaInfo) extractor.getInfo();

        return info;
    }

    public SeasonInfo getSeasonInfo(boolean forceload, int page, int year, String season) throws IOException, ServiceUnavailableException {
        String cacheKey=String.format(Constants.SEASON_INFO_CACHE_KEY_PATTERN, year, season, page);

        if (!forceload && cache.exist(cacheKey)){
            return (SeasonInfo) cache.getInfo(cacheKey);
        }

        SeasonExtractor extractor=new SeasonExtractor(client, page, year, season);
        SeasonInfo info=(SeasonInfo) extractor.getInfo();

        cache.addInfo(cacheKey, info);

        return info;
    }

    public CharactersInfo getAnimeCharactersInfo(long malid, int page) throws IOException, ServiceUnavailableException {
        String cacheKey=String.format(Constants.ANIME_CHARACTERS_INFO_CACHE_KEY_PATTERN, malid, page);

        if (cache.exist(cacheKey)){
            return (CharactersInfo) cache.getInfo(cacheKey);
        }

        CharactersExtractor extractor=new CharactersExtractor(client, malid, page);
        CharactersInfo info=(CharactersInfo) extractor.getInfo();

        cache.addInfo(cacheKey, info);

        return info;
    }

}

package com.lexoff.animediary;

public class Constants {

    public static String AAV="1.6.0-beta8";
    public static String DEV_GITHUB_LINK="https://github.com/AlternativeOne";
    public static String APP_GITHUB_LINK="https://github.com/AlternativeOne/Hanabi";

    public static long SMALLEST_LOCAL_MALID=-1;
    public static long NON_EXIST_MALID=0;

    public static int MAIN_THUMBNAIL_DUMMY_BITMAP_WIDTH=225 * 2;
    public static int MAIN_THUMBNAIL_DUMMY_BITMAP_HEIGHT=318 * 2;

    public static int GRID_ITEM_DUMMY_BITMAP_WIDTH=225;
    public static int GRID_ITEM_DUMMY_BITMAP_HEIGHT=318;

    public static int GRID_COMPANY_ITEM_BITMAP_WIDTH=150;

    public static int POST_EXEC_SMALLEST_DELAY=100;

    public static String USE_PASSWORD_BLOCK="use_password_block";
    public static String PIN_VALUE="pin_value";

    public static String USE_SECURE_MODE_KEY="use_secure_mode";

    public static String USE_GRADIENT_IN_POST="use_gradient_in_post";
    public static String ALWAYS_UPDATE_ITEM_INFO="always_update_item_info";
    public static String SHOW_EPISODES_LEFT_BADGE="show_episodes_left_badge";
    public static String SHOW_ADDED_TO_BADGE="show_added_to_badge";
    public static String SHOW_ENGLISH_TITLES="show_english_titles";
    public static String SHOW_NEXT_AIRING_EPISODE="show_next_airing_episode";
    public static String SETTINGS_VERSION="settings_version";

    public static String TOWATCH_LIST_MODE_KEY="towatch_list_mode";
    public static String WATCHED_LIST_MODE_KEY="watched_list_mode";

    public static String APP_ICON_KEY="app_icon";

    public static String ANIME_INFO_CACHE_KEY_PATTERN="anime_";
    public static String COMPANY_INFO_CACHE_KEY_PATTERN="company_";
    public static String TRENDING_INFO_CACHE_KEY_PATTERN="trending_";
    public static String SEASON_INFO_CACHE_KEY_PATTERN="season_%d_%s_%d";
    public static String AIRING_SCHEDULE_INFO_CACHE_KEY_PATTERN="airing_schedule";
    public static String ANIME_RECOMMENDATIONS_INFO_CACHE_KEY_PATTERN="anime_recommendations_";
    public static String ANIME_CHARACTERS_INFO_CACHE_KEY_PATTERN="anime_characters_%d_%d";

    public static String BUMPER_FILE_NAME="bumper.jpg";

    public static String SPOTIFY_APP_PACKAGE_NAME="com.spotify.music";
    public static String TACHIYOMI_APP_PACKAGE_NAME="eu.kanade.tachiyomi";
    public static String TACHIYOMI_MANGADEX_APP_PACKAGE_NAME="eu.kanade.tachiyomi.extension.all.mangadex";

    public static String SCANNER_APP_PACKAGE_NAME=BuildConfig.DEBUG ? "com.lexoff.animediary.scanner.debug" : "com.lexoff.animediary.scanner";
    public static String SCANNER_INTENT_MAL_ID_EXTRA_NAME="SCANNER_MAL_ID";

}

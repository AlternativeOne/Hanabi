package com.lexoff.animediary;

import android.app.Activity;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.lexoff.animediary.Fragment.AiringScheduleFragment;
import com.lexoff.animediary.Fragment.AnimeCharactersFragment;
import com.lexoff.animediary.Fragment.AnimeFragment;
import com.lexoff.animediary.Fragment.AnimeRecommendationsFragment;
import com.lexoff.animediary.Fragment.BumperPreviewFragment;
import com.lexoff.animediary.Fragment.ChartsFragment;
import com.lexoff.animediary.Fragment.CompanyFragment;
import com.lexoff.animediary.Fragment.NavigationFragment;
import com.lexoff.animediary.Fragment.PINFragment;
import com.lexoff.animediary.Fragment.PlaylistFragment;
import com.lexoff.animediary.Fragment.PlaylistsFragment;
import com.lexoff.animediary.Fragment.SettingsFragment;
import com.lexoff.animediary.Fragment.SplashFragment;
import com.lexoff.animediary.Fragment.StatisticsFragment;
import com.lexoff.animediary.Fragment.ThumbnailViewerFragment;
import com.lexoff.animediary.Manga.MDSearchFragment;

public class NavigationUtils {

    public static void openSplashFragment(Activity activity){
        FragmentManager fragmentManager = ((MainActivity) activity).getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, SplashFragment.newInstance());
        fragmentTransaction.commit();
    }

    public static void openNavigationFragment(Activity activity) {
        FragmentManager fragmentManager = ((MainActivity) activity).getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.fragment_container, NavigationFragment.newInstance());

        Fragment last = Utils.getLastFragment(fragmentManager);
        if (last != null)
            fragmentTransaction.remove(last);

        fragmentTransaction.commit();
    }

    private static void openFragment(Activity activity, Fragment fragment){
        FragmentManager fragmentManager = ((AppCompatActivity) activity).getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fragment_slide_lr_enter, R.anim.fragment_slide_lr_exit, R.anim.fragment_slide_lr_popenter, R.anim.fragment_slide_lr_popexit);
        fragmentTransaction.add(R.id.fragment_container, fragment);
        fragmentTransaction.hide(Utils.getLastFragment(fragmentManager)); //last fragment will not be null because NavigationFragment always open
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public static void openAnimeFragment(Activity activity, long malid, InfoSourceType type){
        openFragment(activity, AnimeFragment.newInstance(malid, type));
    }

    public static void openCompanyFragment(Activity activity, long cmalid){
        openFragment(activity, CompanyFragment.newInstance(cmalid));
    }

    public static void openSettingsFragment(Activity activity){
        openFragment(activity, SettingsFragment.newInstance());
    }

    public static void openStatisticsFragment(Activity activity){
        openFragment(activity, StatisticsFragment.newInstance());
    }

    public static void openThumbnailViewerFragment(Activity activity, long malid){
        openFragment(activity, ThumbnailViewerFragment.newInstance(malid));
    }

    public static void openAnimeRecommendations(Activity activity, long malid, String title){
        openFragment(activity, AnimeRecommendationsFragment.newInstance(malid, title));
    }

    public static void openAiringScheduleFragment(Activity activity){
        openFragment(activity, AiringScheduleFragment.newInstance());
    }

    public static void openPlaylistsFragment(Activity activity){
        openFragment(activity, PlaylistsFragment.newInstance());
    }

    public static void openPlaylistFragment(Activity activity, int playlistId){
        openFragment(activity, PlaylistFragment.newInstance(playlistId));
    }

    public static void openChartsFragment(Activity activity){
        openFragment(activity, ChartsFragment.newInstance());
    }

    public static void openPINFragment(Activity activity, int mode){
        FragmentManager fragmentManager = ((MainActivity) activity).getSupportFragmentManager();
        Fragment lastFragment=Utils.getLastFragment(fragmentManager);

        if (lastFragment instanceof SplashFragment) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.add(R.id.fragment_container, PINFragment.newInstance(mode));
            fragmentTransaction.remove(lastFragment);
            fragmentTransaction.commit();
        } else {
            openFragment(activity, PINFragment.newInstance(mode));
        }
    }

    public static void openMDSearchFragment(Activity activity, String mangaTitle){
        openFragment(activity, MDSearchFragment.newInstance(mangaTitle));
    }

    public static void openAnimeCharactersFragment(Activity activity, long malid){
        openFragment(activity, AnimeCharactersFragment.newInstance(malid));
    }

    public static void openBumperPreviewFragment(Activity activity, Uri uri, BumperCallback callback){
        openFragment(activity, BumperPreviewFragment.newInstance(uri, callback));
    }

}

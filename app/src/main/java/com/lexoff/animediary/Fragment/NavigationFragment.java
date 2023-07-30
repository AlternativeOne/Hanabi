package com.lexoff.animediary.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lexoff.animediary.R;

public class NavigationFragment extends BaseFragment {

    @IdRes
    private int fragmentToOpenId=R.id.towatch_nav_item;

    public NavigationFragment() {
        //empty
    }

    public static NavigationFragment newInstance() {
        NavigationFragment fragment = new NavigationFragment();
        return fragment;
    }

    public static NavigationFragment newInstance(@IdRes int fragmentToOpenId) {
        NavigationFragment fragment = new NavigationFragment();
        fragment.setFragmentToOpenId(fragmentToOpenId);
        return fragment;
    }

    private void setFragmentToOpenId(@IdRes int fragmentToOpenId){
        this.fragmentToOpenId=fragmentToOpenId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //kinda hack but didn't find any other WORKING solution
    @Override
    public void onHiddenChanged (boolean hidden) {
        super.onHiddenChanged(hidden);

        Fragment fragment = getChildFragmentManager().getPrimaryNavigationFragment();

        if (!hidden) {
            fragment.onResume();
        } else {
            fragment.onPause();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_navigation, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        setupBottomNav(rootView);

        openToWatchFragment();
    }

    @Override
    public boolean onBackPressed() {
        Fragment fragment=getChildFragmentManager().getPrimaryNavigationFragment();
        if (fragment instanceof SearchFragment){
            if (((SearchFragment) fragment).onBackPressed()) return true;
        }

        return false;
    }

    private void setupBottomNav(View view) {
        BottomNavigationView bottomNavigationView = view.findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            //TODO: rework this
            //to not allow to open the same fragment as already opened

            int id = item.getItemId();

            if (id == R.id.kiosk_nav_item) {
                openKioskFragment();
            } else if (id == R.id.towatch_nav_item) {
                openToWatchFragment();
            } else if (id == R.id.search_nav_item) {
                openSearchFragment();
            } else if (id == R.id.watched_nav_item) {
                openWatchedFragment();
            } else if (id == R.id.more_options_nav_item) {
                openMoreOptionsFragment();
            }

            //kinda hack but didn't find any other WORKING solution
            Fragment fragment = getChildFragmentManager().getPrimaryNavigationFragment();
            if (fragment != null) {
                fragment.onResume();
            }

            return true;
        });

        //without this it won't open search fragment from static shortcut
        //but with this, it could lead to race condition
        bottomNavigationView.post(() -> {
            bottomNavigationView.setSelectedItemId(fragmentToOpenId);
        });
    }

    private final String KIOSK_FRAGMENT_TAG="KIOSK_FRAGMENT_TAG";
    private final String TOWATCH_FRAGMENT_TAG="TOWATCH_FRAGMENT_TAG";
    private final String SEARCH_FRAGMENT_TAG="SEARCH_FRAGMENT_TAG";
    private final String WATCHED_FRAGMENT_TAG="WATCHED_FRAGMENT_TAG";
    private final String MORE_OPTIONS_FRAGMENT_TAG="MORE_OPTIONS_FRAGMENT_TAG";

    public void openKioskFragment(){
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Fragment curFrag = fragmentManager.getPrimaryNavigationFragment();
        if (curFrag != null) {
            fragmentTransaction.hide(curFrag);
        }

        Fragment fragment = fragmentManager.findFragmentByTag(KIOSK_FRAGMENT_TAG);
        if (fragment == null) {
            fragment = KioskFragment.newInstance();
            fragmentTransaction.add(R.id.nav_fragment_container, fragment, KIOSK_FRAGMENT_TAG);
        } else {
            fragmentTransaction.show(fragment);
        }

        fragmentTransaction.setPrimaryNavigationFragment(fragment);
        fragmentTransaction.setReorderingAllowed(true);
        fragmentTransaction.commitNowAllowingStateLoss();
    }

    public void openToWatchFragment(){
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Fragment curFrag = fragmentManager.getPrimaryNavigationFragment();
        if (curFrag != null) {
            fragmentTransaction.hide(curFrag);
        }

        Fragment fragment = fragmentManager.findFragmentByTag(TOWATCH_FRAGMENT_TAG);
        if (fragment == null) {
            fragment = ToWatchFragment.newInstance();
            fragmentTransaction.add(R.id.nav_fragment_container, fragment, TOWATCH_FRAGMENT_TAG);
        } else {
            fragmentTransaction.show(fragment);
        }

        fragmentTransaction.setPrimaryNavigationFragment(fragment);
        fragmentTransaction.setReorderingAllowed(true);
        fragmentTransaction.commitNowAllowingStateLoss();
    }

    public void openSearchFragment(){
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Fragment curFrag = fragmentManager.getPrimaryNavigationFragment();
        if (curFrag != null) {
            fragmentTransaction.hide(curFrag);
        }

        Fragment fragment = fragmentManager.findFragmentByTag(SEARCH_FRAGMENT_TAG);
        if (fragment == null) {
            fragment = SearchFragment.newInstance();
            fragmentTransaction.add(R.id.nav_fragment_container, fragment, SEARCH_FRAGMENT_TAG);
        } else {
            fragmentTransaction.show(fragment);
        }

        fragmentTransaction.setPrimaryNavigationFragment(fragment);
        fragmentTransaction.setReorderingAllowed(true);
        fragmentTransaction.commitNowAllowingStateLoss();
    }

    public void openWatchedFragment(){
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Fragment curFrag = fragmentManager.getPrimaryNavigationFragment();
        if (curFrag != null) {
            fragmentTransaction.hide(curFrag);
        }

        Fragment fragment = fragmentManager.findFragmentByTag(WATCHED_FRAGMENT_TAG);
        if (fragment == null) {
            fragment = WatchedFragment.newInstance();
            fragmentTransaction.add(R.id.nav_fragment_container, fragment, WATCHED_FRAGMENT_TAG);
        } else {
            fragmentTransaction.show(fragment);
        }

        fragmentTransaction.setPrimaryNavigationFragment(fragment);
        fragmentTransaction.setReorderingAllowed(true);
        fragmentTransaction.commitNowAllowingStateLoss();
    }

    public void openMoreOptionsFragment(){
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Fragment curFrag = fragmentManager.getPrimaryNavigationFragment();
        if (curFrag != null) {
            fragmentTransaction.hide(curFrag);
        }

        Fragment fragment = fragmentManager.findFragmentByTag(MORE_OPTIONS_FRAGMENT_TAG);
        if (fragment == null) {
            fragment = MoreOptionsFragment.newInstance();
            fragmentTransaction.add(R.id.nav_fragment_container, fragment, MORE_OPTIONS_FRAGMENT_TAG);
        } else {
            fragmentTransaction.show(fragment);
        }

        fragmentTransaction.setPrimaryNavigationFragment(fragment);
        fragmentTransaction.setReorderingAllowed(true);
        fragmentTransaction.commitNowAllowingStateLoss();
    }

}

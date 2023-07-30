package com.lexoff.animediary.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.widget.NestedScrollView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lexoff.animediary.Adapter.TrendingItemsAdapter;
import com.lexoff.animediary.Api;
import com.lexoff.animediary.Constants;
import com.lexoff.animediary.CustomHashMap;
import com.lexoff.animediary.CustomOnItemClickListener;
import com.lexoff.animediary.Info.SeasonInfo;
import com.lexoff.animediary.Info.TrendingInfo;
import com.lexoff.animediary.Enum.InfoSourceType;
import com.lexoff.animediary.Enum.KioskCategory;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Util.NavigationUtils;
import com.lexoff.animediary.Util.Utils;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class KioskFragment extends BaseFragment {

    private AtomicBoolean isLoading = new AtomicBoolean(false);
    private Disposable currentWorker;

    private Handler toastHandler;

    private SharedPreferences defPrefs;

    private RecyclerView resultsView;

    private View toTopBtn;

    private ProgressBar moreItemsPB;

    private int currentPage, maxPage;

    private KioskCategory category=KioskCategory.TRENDING;

    public KioskFragment() {
        //empty
    }

    public static KioskFragment newInstance() {
        KioskFragment fragment = new KioskFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        toastHandler=new Handler(Looper.myLooper(), null);

        defPrefs=PreferenceManager.getDefaultSharedPreferences(requireContext());
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if (currentWorker!=null){
            currentWorker.dispose();
            currentWorker=null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_kiosk, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        int navbarHeight=Utils.getNavBarHeight(requireContext());
        rootView.setPadding(0, 0, 0, navbarHeight);

        int statusbarHeight=Utils.getStatusBarHeight(requireContext());
        View sbMarginView=rootView.findViewById(R.id.statusbar_margin_view);
        ViewGroup.LayoutParams params=sbMarginView.getLayoutParams();
        params.height=statusbarHeight;
        sbMarginView.setLayoutParams(params);

        moreItemsPB=rootView.findViewById(R.id.load_more_items_progressbar);
        FrameLayout.LayoutParams params2=(FrameLayout.LayoutParams) moreItemsPB.getLayoutParams();
        params2.topMargin=statusbarHeight+25;
        moreItemsPB.setLayoutParams(params2);

        resultsView=rootView.findViewById(R.id.results_view);

        GridLayoutManager layoutManager=new GridLayoutManager(requireContext(), 4);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        resultsView.setLayoutManager(layoutManager);

        ImageView categoryBtn=rootView.findViewById(R.id.category_btn);
        categoryBtn.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);

            popupMenu.getMenuInflater().inflate(R.menu.kiosk_categories_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                int itemId=menuItem.getItemId();

                if (itemId==R.id.trending_item){
                    category=KioskCategory.TRENDING;
                } else if (itemId==R.id.next_season_item){
                    category=KioskCategory.NEXT_SEASON;
                }

                setTitle();

                loadInfo(false);

                return true;
            });

            popupMenu.show();
        });

        NestedScrollView mainLayout=rootView.findViewById(R.id.main_layout);

        toTopBtn=rootView.findViewById(R.id.to_top_button);
        toTopBtn.setOnClickListener(v -> {
            if (mainLayout!=null) {
                mainLayout.smoothScrollTo(0, 0);
            }
        });
        toTopBtn.setVisibility(View.INVISIBLE);

        mainLayout.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY<=resultsView.getY()){
                    toTopBtn.setVisibility(View.INVISIBLE);
                } else {
                    if ((scrollY - oldScrollY) < 0) {
                        toTopBtn.setVisibility(View.VISIBLE);
                    } else {
                        toTopBtn.setVisibility(View.INVISIBLE);
                    }
                }

                if ((scrollY-oldScrollY)>0) {
                    View lastChild = mainLayout.getChildAt(mainLayout.getChildCount() - 1);

                    if ((lastChild.getBottom() - (mainLayout.getHeight() + mainLayout.getScrollY()))==0) {
                        if (currentPage<maxPage) {
                            loadMoreItems(false);
                        }
                    }
                }
            }
        });

        setTitle();

        loadInfo(false);
    }

    @Override
    public void onResume(){
        super.onResume();

        toTopBtn.setVisibility(View.INVISIBLE);
    }

    private void loadInfo(boolean forceload){
        currentPage=1;
        maxPage=1;

        if (category==KioskCategory.TRENDING){
            loadTrending(forceload);
        } else if (category==KioskCategory.NEXT_SEASON){
            loadSeason(forceload);
        }
    }

    private void loadTrending(boolean forceload){
        if (isLoading.get()) return;

        isLoading.set(true);

        updateLoading();

        if (currentWorker != null) currentWorker.dispose();

        currentWorker = Single.fromCallable(() -> (new Api()).getTrendingInfo(forceload, currentPage))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final TrendingInfo result) -> {
                    isLoading.set(false);

                    updateLoading();

                    handleResult(result);
                }, (@NonNull final Throwable throwable) -> {
                    isLoading.set(false);
                    handleError(throwable);
                });
    }

    private void handleResult(TrendingInfo info){
        if (info.hasErrors()){
            handleError(info.getError());

            return;
        }

        maxPage=info.getMaxPage();

        TrendingItemsAdapter adapter=new TrendingItemsAdapter(requireContext(), info.getItems(), new CustomOnItemClickListener(){
            @Override
            public void onClick(View v, int position) {
                NavigationUtils.openAnimeFragment(requireActivity(), ((TrendingItemsAdapter) resultsView.getAdapter()).getItem(position).getMalid(), InfoSourceType.REMOTE);
            }
        });
        adapter.setAdditionalParams(defPrefs.getBoolean(Constants.SHOW_ADDED_TO_BADGE, true), defPrefs.getBoolean(Constants.SHOW_ENGLISH_TITLES, false));
        resultsView.setAdapter(adapter);
    }

    private void loadSeason(boolean forceload){
        if (isLoading.get()) return;

        isLoading.set(true);

        updateLoading();

        if (currentWorker != null) currentWorker.dispose();

        currentWorker = Single.fromCallable(() -> (new Api()).getSeasonInfo(forceload, currentPage, getNextSeason().getInt("year"), getNextSeason().getString("season")))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final SeasonInfo result) -> {
                    isLoading.set(false);

                    updateLoading();

                    handleResult(result);
                }, (@NonNull final Throwable throwable) -> {
                    isLoading.set(false);
                    handleError(throwable);
                });
    }

    private void handleResult(SeasonInfo info){
        if (info.hasErrors()){
            handleError(info.getError());

            return;
        }

        maxPage=info.getMaxPage();

        TrendingItemsAdapter adapter=new TrendingItemsAdapter(requireContext(), info.getItems(), new CustomOnItemClickListener(){
            @Override
            public void onClick(View v, int position) {
                NavigationUtils.openAnimeFragment(requireActivity(), ((TrendingItemsAdapter) resultsView.getAdapter()).getItem(position).getMalid(), InfoSourceType.REMOTE);
            }
        });
        adapter.setAdditionalParams(defPrefs.getBoolean(Constants.SHOW_ADDED_TO_BADGE, true), defPrefs.getBoolean(Constants.SHOW_ENGLISH_TITLES, false));
        resultsView.setAdapter(adapter);
    }

    private void loadMoreItems(boolean forceload){
        if (category==KioskCategory.TRENDING){
            loadMoreTrendingItems(forceload);
        } else if (category==KioskCategory.NEXT_SEASON){
            loadMoreNextSeasonItems(forceload);
        }
    }

    private void loadMoreTrendingItems(boolean forceload){
        if (isLoading.get()) return;

        isLoading.set(true);

        showOrHideMoreItemsProgressBar(true);

        if (currentWorker != null) currentWorker.dispose();

        currentWorker = Single.fromCallable(() -> (new Api()).getTrendingInfo(forceload, currentPage+=1))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final TrendingInfo result) -> {
                    isLoading.set(false);

                    showOrHideMoreItemsProgressBar(false);

                    handleMoreItems(result);
                }, (@NonNull final Throwable throwable) -> {
                    isLoading.set(false);

                    showOrHideMoreItemsProgressBar(false);

                    handleError(throwable);
                });
    }

    private void handleMoreItems(TrendingInfo info){
        if (info.hasErrors()){
            handleError(info.getError());

            return;
        }

        ((TrendingItemsAdapter) resultsView.getAdapter()).addItems(info.getItems());
    }

    private void loadMoreNextSeasonItems(boolean forceload){
        if (isLoading.get()) return;

        isLoading.set(true);

        showOrHideMoreItemsProgressBar(true);

        if (currentWorker != null) currentWorker.dispose();

        currentWorker = Single.fromCallable(() -> (new Api()).getSeasonInfo(forceload, currentPage+=1, getNextSeason().getInt("year"), getNextSeason().getString("season")))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final SeasonInfo result) -> {
                    isLoading.set(false);

                    showOrHideMoreItemsProgressBar(false);

                    handleMoreItems(result);
                }, (@NonNull final Throwable throwable) -> {
                    isLoading.set(false);

                    showOrHideMoreItemsProgressBar(false);

                    handleError(throwable);
                });
    }

    private void handleMoreItems(SeasonInfo info){
        if (info.hasErrors()){
            handleError(info.getError());

            return;
        }

        ((TrendingItemsAdapter) resultsView.getAdapter()).addItems(info.getItems());
    }

    private CustomHashMap getNextSeason(){
        Calendar calendar=Calendar.getInstance();
        int year=calendar.get(Calendar.YEAR);
        int month=calendar.get(Calendar.MONTH);
        String season="";
        if (month==0 || month==1 || month==2){
            season="Spring";
        } else if (month==3 || month==4 || month==5){
            season="Summer";
        } else if (month==6 || month==7 || month==8){
            season="Fall";
        } else if (month==9 || month==10 || month==11){
            season="Winter";

            year+=1;
        }

        CustomHashMap map=new CustomHashMap();
        map.put("year", year);
        map.put("season", season);

        return map;
    }

    private void updateLoading(){
        showOrHideMainLayout(!isLoading.get());
        hideErrorLayout();
        showOrHideLoadingLayout(isLoading.get());
    }

    private void setTitle(){
        TextView titleView=getView().findViewById(R.id.results_title);

        String title=getString(R.string.kiosk_trending_title);
        if (category==KioskCategory.NEXT_SEASON){
            title=getString(R.string.kiosk_next_season_title);
        }

        titleView.setText(title);
    }

    private void showOrHideMainLayout(boolean show){
        View mainLayout=getView().findViewById(R.id.main_layout);
        mainLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showOrHideLoadingLayout(boolean show){
        View loadingLayout=getView().findViewById(R.id.loading_layout);

        if (show) {
            ImageView loadingSplashView = loadingLayout.findViewById(R.id.loading_splash);
            loadingSplashView.setImageURI(null);
            loadingSplashView.setImageURI(Utils.resolveBumperUri(requireContext()));
        }

        ProgressBar loadingProgressBar=loadingLayout.findViewById(R.id.loading_progressbar);
        loadingProgressBar.setPadding(loadingProgressBar.getPaddingLeft(), loadingProgressBar.getPaddingTop(), loadingProgressBar.getPaddingRight(), Utils.getNavBarHeight(requireContext()));

        loadingLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showOrHideMoreItemsProgressBar(boolean show){
        moreItemsPB.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showErrorLayout(String title, String message){
        View errorLayout=getView().findViewById(R.id.error_layout);
        errorLayout.setVisibility(View.VISIBLE);

        TextView titleView=errorLayout.findViewById(R.id.error_title);
        titleView.setText(title);

        TextView messageView=errorLayout.findViewById(R.id.error_message);
        messageView.setText(message);

        TextView reloadButton=errorLayout.findViewById(R.id.error_reload_btn);
        reloadButton.setOnClickListener(v -> {
            hideErrorLayout();

            if (currentPage==1) {
                loadInfo(true);
            } else {
                currentPage-=1;

                loadMoreItems(true);
            }
        });
    }

    private void showToastMessage(String message){
        View toastLayout=getView().findViewById(R.id.toast_layout);

        if (toastLayout.getVisibility()==View.GONE) {
            toastLayout.setVisibility(View.VISIBLE);

            TextView toastMessage = toastLayout.findViewById(R.id.toast_message);
            toastMessage.setText(message);

            toastHandler.postDelayed(() -> toastLayout.setVisibility(View.GONE), 3000);
        } else {
            toastHandler.removeCallbacksAndMessages(null);

            toastLayout.setVisibility(View.GONE);

            toastHandler.postDelayed(() -> showToastMessage(message), 200);
        }
    }

    private void hideErrorLayout(){
        View errorLayout=getView().findViewById(R.id.error_layout);
        errorLayout.setVisibility(View.GONE);
    }

    private void handleError(Throwable e){
        showOrHideLoadingLayout(false);
        showErrorLayout(getString(R.string.error_happened), e.getMessage());
    }

}

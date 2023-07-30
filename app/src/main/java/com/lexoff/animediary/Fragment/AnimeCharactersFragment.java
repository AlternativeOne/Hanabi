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
import androidx.core.widget.NestedScrollView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lexoff.animediary.Adapter.CharactersItemsAdapter;
import com.lexoff.animediary.Api;
import com.lexoff.animediary.Info.CharactersInfo;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Util.Utils;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AnimeCharactersFragment extends BaseFragment {

    private AtomicBoolean isLoading = new AtomicBoolean(false);
    private Disposable currentWorker;

    private long malid;

    private Handler toastHandler;

    private SharedPreferences defPrefs;

    private RecyclerView resultsView;

    private ProgressBar moreItemsPB;

    private int currentPage, maxPage;

    public AnimeCharactersFragment() {
        //empty
    }

    public static AnimeCharactersFragment newInstance(long malid) {
        AnimeCharactersFragment fragment = new AnimeCharactersFragment();
        fragment.setMalid(malid);
        return fragment;
    }

    private void setMalid(long malid){
        this.malid=malid;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        toastHandler=new Handler(Looper.myLooper(), null);

        defPrefs=PreferenceManager.getDefaultSharedPreferences(requireContext());

        currentPage=1;
        maxPage=1;
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
        return inflater.inflate(R.layout.fragment_anime_characters, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        int navbarHeight= Utils.getNavBarHeight(requireContext());
        rootView.setPadding(0, 0, 0, navbarHeight);

        int statusbarHeight=Utils.getStatusBarHeight(requireContext());
        View pab=rootView.findViewById(R.id.pab);
        FrameLayout.LayoutParams params=(FrameLayout.LayoutParams) pab.getLayoutParams();
        params.topMargin=statusbarHeight;
        pab.setLayoutParams(params);

        ImageView backBtn = rootView.findViewById(R.id.ab_back_btn);
        backBtn.setOnClickListener(v -> {
            Utils.animateClickOnImageButton(v, () -> {
                requireActivity().onBackPressed();
            });
        });

        moreItemsPB=rootView.findViewById(R.id.load_more_items_progressbar);

        //only once!
        FrameLayout.LayoutParams params2=(FrameLayout.LayoutParams) moreItemsPB.getLayoutParams();
        params2.topMargin+=statusbarHeight+Utils.dpToPx(requireContext(), 5);
        moreItemsPB.setLayoutParams(params2);

        NestedScrollView mainLayout=rootView.findViewById(R.id.main_layout);
        FrameLayout.LayoutParams params3=(FrameLayout.LayoutParams) mainLayout.getLayoutParams();
        params3.topMargin+=statusbarHeight;
        mainLayout.setLayoutParams(params3);

        resultsView=rootView.findViewById(R.id.results_view);

        LinearLayoutManager layoutManager=new LinearLayoutManager(requireContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        resultsView.setLayoutManager(layoutManager);

        mainLayout.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if ((scrollY-oldScrollY)>0) {
                    View lastChild = mainLayout.getChildAt(mainLayout.getChildCount() - 1);

                    if ((lastChild.getBottom() - (mainLayout.getHeight() + mainLayout.getScrollY()))==0) {
                        if (currentPage<maxPage) {
                            loadMoreItems();
                        }
                    }
                }
            }
        });

        loadInfo();
    }

    private void loadInfo(){
        if (isLoading.get()) return;

        isLoading.set(true);

        updateLoading();

        if (currentWorker != null) currentWorker.dispose();

        currentWorker = Single.fromCallable(() -> (new Api()).getAnimeCharactersInfo(malid, currentPage))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final CharactersInfo result) -> {
                    isLoading.set(false);

                    updateLoading();

                    handleResult(result);
                }, (@NonNull final Throwable throwable) -> {
                    isLoading.set(false);
                    handleError(throwable);
                });
    }

    private void handleResult(CharactersInfo info){
        if (info.hasErrors()){
            handleError(info.getError());

            return;
        }

        if (info.getItems().size()==0){
            showOrHideMainLayout(false);
            showOrHideEmptyLayout(true);

            return;
        }

        maxPage=info.getMaxPage();

        CharactersItemsAdapter adapter=new CharactersItemsAdapter(requireContext(), info.getItems(), null);
        resultsView.setAdapter(adapter);
    }

    private void loadMoreItems(){
        if (isLoading.get()) return;

        isLoading.set(true);

        showOrHideMoreItemsProgressBar(true);

        if (currentWorker != null) currentWorker.dispose();

        currentWorker = Single.fromCallable(() -> (new Api()).getAnimeCharactersInfo(malid, currentPage+=1))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final CharactersInfo result) -> {
                    isLoading.set(false);

                    showOrHideMoreItemsProgressBar(false);

                    handleMoreItems(result);
                }, (@NonNull final Throwable throwable) -> {
                    isLoading.set(false);

                    showOrHideMoreItemsProgressBar(false);

                    handleError(throwable);
                });
    }

    private void handleMoreItems(CharactersInfo info){
        if (info.hasErrors()){
            handleError(info.getError());

            return;
        }

        ((CharactersItemsAdapter) resultsView.getAdapter()).addItems(info.getItems());
    }

    private void updateLoading(){
        showOrHideMainLayout(!isLoading.get());
        hideErrorLayout();
        showOrHideLoadingLayout(isLoading.get());
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

    private void showOrHideEmptyLayout(boolean show){
        View emptyLayout=getView().findViewById(R.id.empty_layout);
        emptyLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showErrorLayout(String title, String message) {
        View errorLayout = getView().findViewById(R.id.error_layout);
        errorLayout.setVisibility(View.VISIBLE);

        TextView titleView = errorLayout.findViewById(R.id.error_title);
        titleView.setText(title);

        TextView messageView = errorLayout.findViewById(R.id.error_message);
        messageView.setText(message);

        TextView reloadButton = errorLayout.findViewById(R.id.error_reload_btn);
        reloadButton.setOnClickListener(v -> {
            hideErrorLayout();

            loadInfo();
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

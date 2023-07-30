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
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lexoff.animediary.Adapter.TrendingItemsAdapter;
import com.lexoff.animediary.Api;
import com.lexoff.animediary.Constants;
import com.lexoff.animediary.CustomOnItemClickListener;
import com.lexoff.animediary.Info.AnimeRecommendationsInfo;
import com.lexoff.animediary.Enum.InfoSourceType;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Util.NavigationUtils;
import com.lexoff.animediary.Util.Utils;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AnimeRecommendationsFragment extends BaseFragment {

    private AtomicBoolean isLoading = new AtomicBoolean(false);
    private Disposable currentWorker;

    private long malid;
    private String title;

    private Handler toastHandler;

    private SharedPreferences defPrefs;

    private RecyclerView resultsView;

    public AnimeRecommendationsFragment() {
        //empty
    }

    public static AnimeRecommendationsFragment newInstance(long malid, String title) {
        AnimeRecommendationsFragment fragment = new AnimeRecommendationsFragment();
        fragment.setMalid(malid);
        fragment.setTitle(title);
        return fragment;
    }

    private void setMalid(long malid){
        this.malid=malid;
    }

    private void setTitle(String title){
        this.title=title;
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
        return inflater.inflate(R.layout.fragment_anime_recommendations, container, false);
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

        View mainLayout=rootView.findViewById(R.id.main_layout);
        FrameLayout.LayoutParams params2=(FrameLayout.LayoutParams) mainLayout.getLayoutParams();
        params2.topMargin+=statusbarHeight;
        mainLayout.setLayoutParams(params2);

        TextView resultsTitleView=rootView.findViewById(R.id.results_title);
        resultsTitleView.setText(String.format(getString(R.string.similar_to), title));

        resultsView=rootView.findViewById(R.id.results_view);

        GridLayoutManager layoutManager=new GridLayoutManager(requireContext(), 4);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        resultsView.setLayoutManager(layoutManager);

        loadInfo();
    }

    private void loadInfo(){
        if (isLoading.get()) return;

        isLoading.set(true);

        updateLoading();

        if (currentWorker != null) currentWorker.dispose();

        currentWorker = Single.fromCallable(() -> (new Api()).getAnimeRecommendationsInfo(malid))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final AnimeRecommendationsInfo result) -> {
                    isLoading.set(false);

                    updateLoading();

                    handleResult(result);
                }, (@NonNull final Throwable throwable) -> {
                    isLoading.set(false);
                    handleError(throwable);
                });
    }

    private void handleResult(AnimeRecommendationsInfo info){
        if (info.hasErrors()){
            handleError(info.getError());

            return;
        }

        if (info.getItems().size()==0){
            showOrHideMainLayout(false);
            showOrHideEmptyLayout(true);

            return;
        }

        TrendingItemsAdapter adapter=new TrendingItemsAdapter(requireContext(), info.getItems(), new CustomOnItemClickListener(){
            @Override
            public void onClick(View v, int position) {
                NavigationUtils.openAnimeFragment(requireActivity(), ((TrendingItemsAdapter) resultsView.getAdapter()).getItem(position).getMalid(), InfoSourceType.REMOTE);
            }
        });
        adapter.setAdditionalParams(defPrefs.getBoolean(Constants.SHOW_ADDED_TO_BADGE, true), defPrefs.getBoolean(Constants.SHOW_ENGLISH_TITLES, false));
        resultsView.setAdapter(adapter);
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

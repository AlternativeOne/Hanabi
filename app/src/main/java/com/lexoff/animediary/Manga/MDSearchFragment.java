package com.lexoff.animediary.Manga;

import android.content.Intent;
import android.net.Uri;
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
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lexoff.animediary.Api;
import com.lexoff.animediary.Constants;
import com.lexoff.animediary.CustomOnItemClickListener;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Utils;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MDSearchFragment extends Fragment {

    private String mangaTitle;

    private AtomicBoolean isLoading = new AtomicBoolean(false);
    private Disposable currentWorker;

    private Handler toastHandler;

    private View rootView;

    private NestedScrollView mainLayout;
    private RecyclerView resultsView;

    private int currentPage, maxPage;

    public MDSearchFragment() {
        //empty
    }

    public static MDSearchFragment newInstance(String mangaTitle) {
        MDSearchFragment fragment = new MDSearchFragment();
        fragment.setMangaTitle(mangaTitle);
        return fragment;
    }

    private void setMangaTitle(String mangaTitle){
        this.mangaTitle=mangaTitle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        toastHandler=new Handler(Looper.getMainLooper());

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
        return inflater.inflate(R.layout.fragment_md_search, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        int navbarHeight= Utils.getNavBarHeight(requireContext());
        rootView.setPadding(0, 0, 0, navbarHeight);

        int statusbarHeight=Utils.getStatusBarHeight(requireContext());
        View sbMarginView=rootView.findViewById(R.id.statusbar_margin_view);
        ViewGroup.LayoutParams params=sbMarginView.getLayoutParams();
        params.height=statusbarHeight;
        sbMarginView.setLayoutParams(params);

        this.rootView=rootView;

        //this needs to be done once!
        View toastLayout=rootView.findViewById(R.id.toast_layout);
        FrameLayout.LayoutParams params2=(FrameLayout.LayoutParams) toastLayout.getLayoutParams();
        params2.setMargins(params2.leftMargin, params2.topMargin, params2.rightMargin, params2.bottomMargin+navbarHeight);
        toastLayout.setLayoutParams(params2);

        View errorLayout=rootView.findViewById(R.id.error_layout);
        FrameLayout.LayoutParams params3=(FrameLayout.LayoutParams) errorLayout.getLayoutParams();
        params3.setMargins(params3.leftMargin, params3.topMargin, params3.rightMargin, params3.bottomMargin+navbarHeight);
        errorLayout.setLayoutParams(params3);

        ImageView backBtn=rootView.findViewById(R.id.ab_back_btn);
        backBtn.setOnClickListener(v -> {
            Utils.animateClickOnImageButton(v, () -> {
                requireActivity().onBackPressed();
            });
        });

        mainLayout=rootView.findViewById(R.id.main_layout);
        resultsView=rootView.findViewById(R.id.results_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        resultsView.setLayoutManager(layoutManager);

        loadInfo();
    }

    private void loadInfo(){
        if (isLoading.get()) return;

        isLoading.set(true);

        updateLoading();

        if (currentWorker != null) currentWorker.dispose();

        currentWorker = Single.fromCallable(() -> (new Api()).getMDSearchInfo(mangaTitle, currentPage))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final MangaInfo result) -> {
                    isLoading.set(false);

                    updateLoading();

                    handleResult(result);
                }, (@NonNull final Throwable throwable) -> {
                    isLoading.set(false);
                    handleError(throwable);
                });
    }

    private void handleResult(MangaInfo info) {
        if (info.hasErrors()) {
            handleError(info.getError());

            return;
        }

        TextView resultsTitle=rootView.findViewById(R.id.results_title);
        resultsTitle.setText(String.format(getString(R.string.results_found_title), info.getTotalItemsCount(), getString(R.string.mangadex)));

        if (info.getItems().size() == 0) {
            //showOrHideMainLayout(false);
            showOrHideEmptyLayout(true);

            return;
        }

        MangaSearchResultsAdapter adapter = new MangaSearchResultsAdapter(requireContext(), info.getItems(), new CustomOnItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                final MangaItemInfo item=((MangaSearchResultsAdapter) resultsView.getAdapter()).getItem(position);

                boolean tachiyomiInstalled=Utils.isPackageInstalled(requireActivity(), Constants.TACHIYOMI_APP_PACKAGE_NAME)
                        && Utils.isPackageInstalled(requireActivity(), Constants.TACHIYOMI_MANGADEX_APP_PACKAGE_NAME);

                int l=1;
                if (tachiyomiInstalled){
                    l+=1;
                }

                String[] items=new String[l];
                items[0]=getString(R.string.manga_dialog_item_copy_link);
                if (tachiyomiInstalled){
                    items[1]=getString(R.string.manga_dialog_item_open_in_tachiyomi);
                }

                new AlertDialog.Builder(requireContext(), R.style.DarkDialogTheme)
                        .setItems(items, (dialog, which) -> {
                            if (which==0){
                                Utils.copyToClipboard(requireContext(), "", Utils.buildMangaDexUrl(item.getId()));
                            } else if (which==1){
                                try {
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.setPackage(Constants.TACHIYOMI_MANGADEX_APP_PACKAGE_NAME);
                                    intent.setData(Uri.parse(Utils.buildMangaDexUrl(item.getId())));
                                    startActivity(intent);
                                } catch (Exception e) {
                                    showToastMessage(getString(R.string.error_happened));
                                }
                            }
                        })
                        .setCancelable(true)
                        .create()
                        .show();
            }
        });
        resultsView.setAdapter(adapter);
    }

    private void updateLoading(){
        showOrHideMainLayout(!isLoading.get());
        hideErrorLayout();
        showOrHideLoadingLayout(isLoading.get());
    }

    private void showOrHideMainLayout(boolean show){
        View mainLayout=rootView.findViewById(R.id.main_layout);
        mainLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showOrHideEmptyLayout(boolean show){
        View emptyLayout=getView().findViewById(R.id.empty_layout);
        emptyLayout.setVisibility(show ? View.VISIBLE : View.GONE);
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

    private void showErrorLayout(String title, String message){
        View errorLayout=rootView.findViewById(R.id.error_layout);
        errorLayout.setVisibility(View.VISIBLE);

        TextView titleView=errorLayout.findViewById(R.id.error_title);
        titleView.setText(title);

        TextView messageView=errorLayout.findViewById(R.id.error_message);
        messageView.setText(message);

        TextView reloadButton=errorLayout.findViewById(R.id.error_reload_btn);
        reloadButton.setOnClickListener(v -> loadInfo());
    }

    private void showToastMessage(String message){
        View toastLayout=rootView.findViewById(R.id.toast_layout);

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
        View errorLayout=rootView.findViewById(R.id.error_layout);
        errorLayout.setVisibility(View.GONE);
    }

    private void handleError(Throwable e){
        showOrHideLoadingLayout(false);
        showErrorLayout(getString(R.string.error_happened), e.getMessage());
    }

}

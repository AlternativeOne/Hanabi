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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.preference.PreferenceManager;

import com.lexoff.animediary.Api;
import com.lexoff.animediary.Adapter.CompanyItemsAdapter;
import com.lexoff.animediary.Constants;
import com.lexoff.animediary.CustomOnItemClickListener;
import com.lexoff.animediary.ImageLoaderWrapper;
import com.lexoff.animediary.Info.CompanyAnimeItemInfo;
import com.lexoff.animediary.Info.CompanyInfo;
import com.lexoff.animediary.InfoSourceType;
import com.lexoff.animediary.NavigationUtils;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Utils;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CompanyFragment extends Fragment {

    private long cmalid;

    private AtomicBoolean isLoading = new AtomicBoolean(false);
    private Disposable currentWorker;

    private Handler toastHandler;

    private View rootView;

    private TextView nameView, infoView;
    private ImageView logoView;
    private RecyclerView resultsView;

    private View toTopBtn;

    private SharedPreferences defPrefs;

    public CompanyFragment() {
        //empty
    }

    public static CompanyFragment newInstance(long cmalid) {
        CompanyFragment fragment = new CompanyFragment();
        fragment.setCMalid(cmalid);
        return fragment;
    }

    private void setCMalid(long cmalid){
        this.cmalid=cmalid;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        toastHandler=new Handler(Looper.getMainLooper());

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
        return inflater.inflate(R.layout.fragment_company, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        //set margins of statusbar
        int statusbarHeight = Utils.getStatusBarHeight(requireContext());
        rootView.setPadding(0, statusbarHeight, 0, 0);

        int navbarHeight=Utils.getNavBarHeight(requireContext());
        View nbMarginView=rootView.findViewById(R.id.navbar_margin_view);
        ViewGroup.LayoutParams params=nbMarginView.getLayoutParams();
        params.height+=navbarHeight;
        nbMarginView.setLayoutParams(params);

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

        nameView=rootView.findViewById(R.id.name_view);
        infoView=rootView.findViewById(R.id.info_view);
        logoView=rootView.findViewById(R.id.logo_view);

        resultsView=rootView.findViewById(R.id.results_view);

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
            }
        });

        loadInfo();
    }

    @Override
    public void onResume(){
        super.onResume();

        toTopBtn.setVisibility(View.INVISIBLE);
    }

    private void loadInfo(){
        if (isLoading.get()) return;

        isLoading.set(true);

        updateLoading();

        if (currentWorker != null) currentWorker.dispose();

        currentWorker = Single.fromCallable(() -> (new Api()).getCompanyInfo(cmalid))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final CompanyInfo result) -> {
                    isLoading.set(false);

                    updateLoading();

                    handleResult(result);
                }, (@NonNull final Throwable throwable) -> {
                    isLoading.set(false);
                    handleError(throwable);
                });
    }

    private void handleResult(CompanyInfo info) {
        ImageLoaderWrapper.loadImage(info.getThumbnailUrl(), logoView, null);

        nameView.setText(info.getName());

        //info is not presented for all companies
        //so probably better not to show it at all
        //infoView.setText(info.getInfo());

        GridLayoutManager layoutManager=new GridLayoutManager(requireContext(), 4);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        resultsView.setLayoutManager(layoutManager);

        CompanyItemsAdapter adapter = new CompanyItemsAdapter(requireContext(), info.getItems(), new CustomOnItemClickListener(){
            @Override
            public void onClick(View v, int position){
                CompanyAnimeItemInfo item=((CompanyItemsAdapter) resultsView.getAdapter()).getItem(position);

                NavigationUtils.openAnimeFragment(requireActivity(), item.getMalid(), InfoSourceType.REMOTE);
            }
        });
        adapter.setAdditionalParams(defPrefs.getBoolean(Constants.SHOW_ADDED_TO_BADGE, true));
        adapter.sortByName();
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

    private void showOrHideLoadingLayout(boolean show){
        View loadingLayout=rootView.findViewById(R.id.loading_layout);

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

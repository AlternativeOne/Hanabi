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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.preference.PreferenceManager;

import com.lexoff.animediary.Adapter.ChartsItemsAdapter;
import com.lexoff.animediary.Api;
import com.lexoff.animediary.ChartsCategory;
import com.lexoff.animediary.Info.ChartsInfo;
import com.lexoff.animediary.Info.ChartsItemInfo;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Utils;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ChartsFragment extends Fragment {

    private AtomicBoolean isLoading = new AtomicBoolean(false);
    private Disposable currentWorker;

    private Handler toastHandler;

    private View rootView;

    private TextView weekView, dateView;
    private RecyclerView resultsView;

    private SharedPreferences defPrefs;

    private ChartsCategory category=ChartsCategory.TOP_ANIME;

    public ChartsFragment() {
        //empty
    }

    public static ChartsFragment newInstance() {
        ChartsFragment fragment = new ChartsFragment();
        return fragment;
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
        return inflater.inflate(R.layout.fragment_charts, container, false);
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

        weekView=rootView.findViewById(R.id.week_view);
        dateView=rootView.findViewById(R.id.date_view);

        resultsView=rootView.findViewById(R.id.results_view);

        LinearLayoutManager layoutManager=new LinearLayoutManager(requireContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        resultsView.setLayoutManager(layoutManager);

        ImageView categoryBtn=rootView.findViewById(R.id.category_btn);
        categoryBtn.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);

            popupMenu.getMenuInflater().inflate(R.menu.charts_categories_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                int itemId=menuItem.getItemId();

                if (itemId==R.id.top_anime_item){
                    category=ChartsCategory.TOP_ANIME;
                } else if (itemId==R.id.male_characters_item){
                    category=ChartsCategory.MALE_CHARACTERS;
                } else if (itemId==R.id.female_characters_item){
                    category=ChartsCategory.FEMALE_CHARACTERS;
                } else if (itemId==R.id.couples_item){
                    category=ChartsCategory.COUPLES;
                } else if (itemId==R.id.op_songs_item){
                    category=ChartsCategory.OP_SONGS;
                } else if (itemId==R.id.ed_songs_item){
                    category=ChartsCategory.ED_SONGS;
                }

                setTitle();

                loadInfo();

                return true;
            });

            popupMenu.show();
        });

        setTitle();

        loadInfo();
    }

    private void loadInfo() {
        if (isLoading.get()) return;

        isLoading.set(true);

        updateLoading();

        if (currentWorker != null) currentWorker.dispose();

        currentWorker = Single.fromCallable(() -> (new Api()).getTopAnimeInfo(category))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final ChartsInfo result) -> {
                    isLoading.set(false);

                    updateLoading();

                    handleResult(result);
                }, (@NonNull final Throwable throwable) -> {
                    isLoading.set(false);
                    handleError(throwable);
                });
    }

    private void handleResult(ChartsInfo info){
        if (info.getItems().size() > 0) {
            showOrHideEmptyLayout(false);

            String week=String.format(getString(R.string.charts_week_text), info.getWeek());
            weekView.setText(week);

            String date=info.getDate();
            if (date.contains("-")) {
                String[] dateSplits = date.split("-");
                date=String.format("%s.%s.%s", dateSplits[2], dateSplits[1], dateSplits[0]);
            }
            dateView.setText(date);

            setAdapter(info.getItems());
        } else {
            showOrHideEmptyLayout(true);
        }
    }

    private void setAdapter(List<ChartsItemInfo> records){
        ChartsItemsAdapter adapter = new ChartsItemsAdapter(requireContext(), records);
        resultsView.setAdapter(adapter);
    }

    private void updateLoading(){
        showOrHideMainLayout(!isLoading.get());
        hideErrorLayout();
        showOrHideLoadingLayout(isLoading.get());
    }

    private void setTitle(){
        TextView titleView=rootView.findViewById(R.id.results_title);

        String title=getString(R.string.charts_top_anime_title);
        if (category==ChartsCategory.MALE_CHARACTERS){
            title=getString(R.string.charts_male_characters_title);
        } else if (category==ChartsCategory.FEMALE_CHARACTERS){
            title=getString(R.string.charts_female_characters_title);
        } else if (category==ChartsCategory.COUPLES){
            title=getString(R.string.charts_couples_title);
        } else if (category==ChartsCategory.OP_SONGS){
            title=getString(R.string.charts_op_songs_title);
        } else if (category==ChartsCategory.ED_SONGS){
            title=getString(R.string.charts_ed_songs_title);
        }

        titleView.setText(title);
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

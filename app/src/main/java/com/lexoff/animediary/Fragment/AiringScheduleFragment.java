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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.preference.PreferenceManager;

import com.lexoff.animediary.Adapter.AiringScheduleItemsAdapter;
import com.lexoff.animediary.Api;
import com.lexoff.animediary.Constants;
import com.lexoff.animediary.CustomOnItemClickListener;
import com.lexoff.animediary.Info.AiringScheduleInfo;
import com.lexoff.animediary.InfoSourceType;
import com.lexoff.animediary.NavigationUtils;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AiringScheduleFragment extends Fragment {

    private AtomicBoolean isLoading = new AtomicBoolean(false);
    private Disposable currentWorker;

    private SharedPreferences defPrefs;

    private Handler toastHandler;

    private RecyclerView mondayResultsView, tuesdayResultsView, wednesdayResultsView, thursdayResultsView, fridayResultsView, saturdayResultsView, sundayResultsView;

    public AiringScheduleFragment() {
        //empty
    }

    public static AiringScheduleFragment newInstance() {
        AiringScheduleFragment fragment = new AiringScheduleFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        defPrefs=PreferenceManager.getDefaultSharedPreferences(requireContext());

        toastHandler=new Handler(Looper.getMainLooper());
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
        return inflater.inflate(R.layout.fragment_airing_schedule, container, false);
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

        mondayResultsView=rootView.findViewById(R.id.monday_results_view);

        GridLayoutManager layoutManager1=new GridLayoutManager(requireContext(), 1);
        layoutManager1.setOrientation(RecyclerView.HORIZONTAL);
        mondayResultsView.setLayoutManager(layoutManager1);

        tuesdayResultsView=rootView.findViewById(R.id.tuesday_results_view);

        GridLayoutManager layoutManager2=new GridLayoutManager(requireContext(), 1);
        layoutManager2.setOrientation(RecyclerView.HORIZONTAL);
        tuesdayResultsView.setLayoutManager(layoutManager2);

        wednesdayResultsView=rootView.findViewById(R.id.wednesday_results_view);

        GridLayoutManager layoutManager3=new GridLayoutManager(requireContext(), 1);
        layoutManager3.setOrientation(RecyclerView.HORIZONTAL);
        wednesdayResultsView.setLayoutManager(layoutManager3);

        thursdayResultsView=rootView.findViewById(R.id.thursday_results_view);

        GridLayoutManager layoutManager4=new GridLayoutManager(requireContext(), 1);
        layoutManager4.setOrientation(RecyclerView.HORIZONTAL);
        thursdayResultsView.setLayoutManager(layoutManager4);

        fridayResultsView=rootView.findViewById(R.id.friday_results_view);

        GridLayoutManager layoutManager5=new GridLayoutManager(requireContext(), 1);
        layoutManager5.setOrientation(RecyclerView.HORIZONTAL);
        fridayResultsView.setLayoutManager(layoutManager5);

        saturdayResultsView=rootView.findViewById(R.id.saturday_results_view);

        GridLayoutManager layoutManager6=new GridLayoutManager(requireContext(), 1);
        layoutManager6.setOrientation(RecyclerView.HORIZONTAL);
        saturdayResultsView.setLayoutManager(layoutManager6);

        sundayResultsView=rootView.findViewById(R.id.sunday_results_view);

        GridLayoutManager layoutManager7=new GridLayoutManager(requireContext(), 1);
        layoutManager7.setOrientation(RecyclerView.HORIZONTAL);
        sundayResultsView.setLayoutManager(layoutManager7);

        loadInfo();
    }

    private void loadInfo(){
        if (isLoading.get()) return;

        isLoading.set(true);

        updateLoading();

        if (currentWorker != null) currentWorker.dispose();

        currentWorker = Single.fromCallable(() -> (new Api()).getAiringScheduleInfo())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final AiringScheduleInfo result) -> {
                    isLoading.set(false);

                    updateLoading();

                    handleResult(result);
                }, (@NonNull final Throwable throwable) -> {
                    isLoading.set(false);
                    handleError(throwable);
                });
    }

    private void handleResult(AiringScheduleInfo info){
        AiringScheduleItemsAdapter adapter1=new AiringScheduleItemsAdapter(requireContext(), info.getMondayItems(), new CustomOnItemClickListener(){
            @Override
            public void onClick(View v, int position) {
                NavigationUtils.openAnimeFragment(requireActivity(), ((AiringScheduleItemsAdapter) mondayResultsView.getAdapter()).getItem(position).getMalid(), InfoSourceType.REMOTE);
            }
        });
        adapter1.setAdditionalParams(defPrefs.getBoolean(Constants.SHOW_ADDED_TO_BADGE, true));
        mondayResultsView.setAdapter(adapter1);

        AiringScheduleItemsAdapter adapter2=new AiringScheduleItemsAdapter(requireContext(), info.getTuesdayItems(), new CustomOnItemClickListener(){
            @Override
            public void onClick(View v, int position) {
                NavigationUtils.openAnimeFragment(requireActivity(), ((AiringScheduleItemsAdapter) tuesdayResultsView.getAdapter()).getItem(position).getMalid(), InfoSourceType.REMOTE);
            }
        });
        adapter2.setAdditionalParams(defPrefs.getBoolean(Constants.SHOW_ADDED_TO_BADGE, true));
        tuesdayResultsView.setAdapter(adapter2);

        AiringScheduleItemsAdapter adapter3=new AiringScheduleItemsAdapter(requireContext(), info.getWednesdayItems(), new CustomOnItemClickListener(){
            @Override
            public void onClick(View v, int position) {
                NavigationUtils.openAnimeFragment(requireActivity(), ((AiringScheduleItemsAdapter) wednesdayResultsView.getAdapter()).getItem(position).getMalid(), InfoSourceType.REMOTE);
            }
        });
        adapter3.setAdditionalParams(defPrefs.getBoolean(Constants.SHOW_ADDED_TO_BADGE, true));
        wednesdayResultsView.setAdapter(adapter3);

        AiringScheduleItemsAdapter adapter4=new AiringScheduleItemsAdapter(requireContext(), info.getThursdayItems(), new CustomOnItemClickListener(){
            @Override
            public void onClick(View v, int position) {
                NavigationUtils.openAnimeFragment(requireActivity(), ((AiringScheduleItemsAdapter) thursdayResultsView.getAdapter()).getItem(position).getMalid(), InfoSourceType.REMOTE);
            }
        });
        adapter4.setAdditionalParams(defPrefs.getBoolean(Constants.SHOW_ADDED_TO_BADGE, true));
        thursdayResultsView.setAdapter(adapter4);

        AiringScheduleItemsAdapter adapter5=new AiringScheduleItemsAdapter(requireContext(), info.getFridayItems(), new CustomOnItemClickListener(){
            @Override
            public void onClick(View v, int position) {
                NavigationUtils.openAnimeFragment(requireActivity(), ((AiringScheduleItemsAdapter) fridayResultsView.getAdapter()).getItem(position).getMalid(), InfoSourceType.REMOTE);
            }
        });
        adapter5.setAdditionalParams(defPrefs.getBoolean(Constants.SHOW_ADDED_TO_BADGE, true));
        fridayResultsView.setAdapter(adapter5);

        AiringScheduleItemsAdapter adapter6=new AiringScheduleItemsAdapter(requireContext(), info.getSaturdayItems(), new CustomOnItemClickListener(){
            @Override
            public void onClick(View v, int position) {
                NavigationUtils.openAnimeFragment(requireActivity(), ((AiringScheduleItemsAdapter) saturdayResultsView.getAdapter()).getItem(position).getMalid(), InfoSourceType.REMOTE);
            }
        });
        adapter6.setAdditionalParams(defPrefs.getBoolean(Constants.SHOW_ADDED_TO_BADGE, true));
        saturdayResultsView.setAdapter(adapter6);

        AiringScheduleItemsAdapter adapter7=new AiringScheduleItemsAdapter(requireContext(), info.getSundayItems(), new CustomOnItemClickListener(){
            @Override
            public void onClick(View v, int position) {
                NavigationUtils.openAnimeFragment(requireActivity(), ((AiringScheduleItemsAdapter) sundayResultsView.getAdapter()).getItem(position).getMalid(), InfoSourceType.REMOTE);
            }
        });
        adapter7.setAdditionalParams(defPrefs.getBoolean(Constants.SHOW_ADDED_TO_BADGE, true));
        sundayResultsView.setAdapter(adapter7);

        updateTitles();
    }

    private void updateTitles(){
        View rootView=getView();
        if (rootView==null) return;

        String dayOfWeek=new SimpleDateFormat("EEEE", new Locale("en")).format(new Date()).trim();
        if (dayOfWeek.equals("Monday")){
            TextView mondayTitle=rootView.findViewById(R.id.monday_title);
            mondayTitle.setText(String.format(getString(R.string.airing_schedule_today_title), getString(R.string.airing_schedule_monday_title)));
        } else if (dayOfWeek.equals("Tuesday")){
            TextView tuesdayTitle=rootView.findViewById(R.id.tuesday_title);
            tuesdayTitle.setText(String.format(getString(R.string.airing_schedule_today_title), getString(R.string.airing_schedule_tuesday_title)));
        } else if (dayOfWeek.equals("Wednesday")){
            TextView wednesdayTitle=rootView.findViewById(R.id.wednesday_title);
            wednesdayTitle.setText(String.format(getString(R.string.airing_schedule_today_title), getString(R.string.airing_schedule_wednesday_title)));
        } else if (dayOfWeek.equals("Thursday")){
            TextView thursdayTitle=rootView.findViewById(R.id.thursday_title);
            thursdayTitle.setText(String.format(getString(R.string.airing_schedule_today_title), getString(R.string.airing_schedule_thursday_title)));
        } else if (dayOfWeek.equals("Friday")){
            TextView fridayTitle=rootView.findViewById(R.id.friday_title);
            fridayTitle.setText(String.format(getString(R.string.airing_schedule_today_title), getString(R.string.airing_schedule_friday_title)));
        } else if (dayOfWeek.equals("Saturday")){
            TextView saturdayTitle=rootView.findViewById(R.id.saturday_title);
            saturdayTitle.setText(String.format(getString(R.string.airing_schedule_today_title), getString(R.string.airing_schedule_saturday_title)));
        } else if (dayOfWeek.equals("Sunday")){
            TextView sundayTitle=rootView.findViewById(R.id.sunday_title);
            sundayTitle.setText(String.format(getString(R.string.airing_schedule_today_title), getString(R.string.airing_schedule_sunday_title)));
        }
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

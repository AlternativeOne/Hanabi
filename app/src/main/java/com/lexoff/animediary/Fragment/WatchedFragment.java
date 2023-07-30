package com.lexoff.animediary.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.lexoff.animediary.Adapter.PinnedPlaylistsChipsAdapter;
import com.lexoff.animediary.Adapter.WatchedRecordsAdapter;
import com.lexoff.animediary.Constants;
import com.lexoff.animediary.CustomOnItemClickListener;
import com.lexoff.animediary.Database.ADatabase;
import com.lexoff.animediary.Database.AppDatabase;
import com.lexoff.animediary.Database.Model.AnimeWatchedEntity;
import com.lexoff.animediary.Database.Model.PlaylistEntity;
import com.lexoff.animediary.Enum.InfoSourceType;
import com.lexoff.animediary.Enum.ListMode;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Util.NavigationUtils;
import com.lexoff.animediary.Util.Utils;
import com.lexoff.animediary.Enum.WatchedSortingMode;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class WatchedFragment extends BaseFragment {

    private AppDatabase database;
    private SharedPreferences defPrefs;

    private AtomicBoolean isLoading = new AtomicBoolean(false);
    private Disposable currentWorker;

    private NestedScrollView mainLayout;
    private RecyclerView resultsView;

    private RecyclerView pinnedPlaylistsView;

    private View toTopBtn;

    private Parcelable savedListState;

    private ListMode listMode=ListMode.LIST;

    private WatchedSortingMode sortMode=WatchedSortingMode.UPDATING_DATE_ASC;

    private String filterQuery="";
    private boolean filterFinished=false;

    public WatchedFragment() {
        //empty
    }

    public static WatchedFragment newInstance() {
        WatchedFragment fragment = new WatchedFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        defPrefs=PreferenceManager.getDefaultSharedPreferences(requireContext());
        listMode=Utils.intToListMode(defPrefs.getInt(Constants.WATCHED_LIST_MODE_KEY, 0));

        database=ADatabase.getInstance(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_watched, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        int navbarHeight=Utils.getNavBarHeight(requireContext());
        rootView.setPadding(0, 0, 0, navbarHeight);

        int statusbarHeight= Utils.getStatusBarHeight(requireContext());
        View sbMarginView=rootView.findViewById(R.id.statusbar_margin_view);
        ViewGroup.LayoutParams params=sbMarginView.getLayoutParams();
        params.height=statusbarHeight;
        sbMarginView.setLayoutParams(params);

        mainLayout=rootView.findViewById(R.id.main_layout);
        resultsView=rootView.findViewById(R.id.results_view);

        LinearLayoutManager layoutManager=new LinearLayoutManager(requireContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        resultsView.setLayoutManager(layoutManager);

        pinnedPlaylistsView=rootView.findViewById(R.id.pinned_playlists_view);

        GridLayoutManager layoutManager2=new GridLayoutManager(requireContext(), 1);
        layoutManager2.setOrientation(RecyclerView.HORIZONTAL);
        pinnedPlaylistsView.setLayoutManager(layoutManager2);

        ImageButton listModeBtn=rootView.findViewById(R.id.list_mode_btn);
        changeActionBarListModeButtonIcon(listMode);
        listModeBtn.setOnClickListener(v -> {
            Utils.animateClickOnImageButton(v, () -> {
                if (listMode == ListMode.LIST){
                    WatchedRecordsAdapter adapter=(WatchedRecordsAdapter) resultsView.getAdapter();

                    if (adapter==null) return;

                    listMode=ListMode.GRID;

                    changeActionBarListModeButtonIcon(listMode);

                    defPrefs.edit().putInt(Constants.WATCHED_LIST_MODE_KEY, Utils.listModeToInt(listMode)).apply();

                    setAdapter(adapter.getItems(), listMode);
                } else if (listMode == ListMode.GRID){
                    WatchedRecordsAdapter adapter=(WatchedRecordsAdapter) resultsView.getAdapter();

                    if (adapter==null) return;

                    listMode=ListMode.LIST;

                    changeActionBarListModeButtonIcon(listMode);

                    defPrefs.edit().putInt(Constants.WATCHED_LIST_MODE_KEY, Utils.listModeToInt(listMode)).apply();

                    setAdapter(adapter.getItems(), listMode);
                }
            });
        });

        ImageButton sortBtn=rootView.findViewById(R.id.sort_btn);
        sortBtn.setOnClickListener(v -> {
            Utils.animateClickOnImageButton(v, () -> {
                BottomSheetDialog dsDialog=new BottomSheetDialog(requireContext());

                View dialogView=View.inflate(requireContext(), R.layout.watched_sort_dialog, null);

                //TODO: find out how Tachiyome solved it
                TextView sortTitleTextView=dialogView.findViewById(R.id.sort_title_view);
                Utils.setUnsetBold(sortTitleTextView, true);
                TextView filterTitleTextView=dialogView.findViewById(R.id.filter_title_view);

                View sortLayout=dialogView.findViewById(R.id.sort_layout);
                View filterLayout=dialogView.findViewById(R.id.filter_layout);

                View sortTitleView=dialogView.findViewById(R.id.sort_title_layout);
                sortTitleView.setOnClickListener(v11 -> {
                    sortLayout.setVisibility(View.VISIBLE);
                    filterLayout.setVisibility(View.INVISIBLE);

                    Utils.setUnsetBold(sortTitleTextView, true);
                    Utils.setUnsetBold(filterTitleTextView, false);
                });

                View filterTitleView=dialogView.findViewById(R.id.filter_title_layout);
                filterTitleView.setOnClickListener(v12 -> {
                    filterLayout.setVisibility(View.VISIBLE);
                    sortLayout.setVisibility(View.INVISIBLE);

                    Utils.setUnsetBold(filterTitleTextView, true);
                    Utils.setUnsetBold(sortTitleTextView, false);
                });

                EditText filterEditText=dialogView.findViewById(R.id.filter_edittext);
                filterEditText.setText(filterQuery);
                filterEditText.setFilters(new InputFilter[] {
                        new InputFilter.AllCaps() {
                            @Override
                            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                                return String.valueOf(source).toLowerCase();
                            }
                        }
                });
                filterEditText.setOnEditorActionListener((v13, actionId, event) -> {
                    if (actionId==EditorInfo.IME_ACTION_DONE){
                        Utils.hideKeyboard(filterEditText);

                        filterQuery=filterEditText.getText().toString();

                        WatchedRecordsAdapter adapter=(WatchedRecordsAdapter) resultsView.getAdapter();

                        adapter.filter(filterQuery, filterFinished);
                        sortItems(adapter);

                        return true;
                    }

                    return false;
                });

                ImageView sortByNameIconView=dialogView.findViewById(R.id.sort_by_name_icon_view);
                if (sortMode==WatchedSortingMode.NAME_ASC){
                    sortByNameIconView.setImageResource(R.drawable.ic_bottom_arrow_white);
                } else if (sortMode==WatchedSortingMode.NAME_DESC) {
                    sortByNameIconView.setImageResource(R.drawable.ic_top_arrow_white);
                } else {
                    sortByNameIconView.setImageDrawable(null);
                }

                ImageView sortByDateIconView=dialogView.findViewById(R.id.sort_by_date_icon_view);
                if (sortMode==WatchedSortingMode.UPDATING_DATE_ASC){
                    sortByDateIconView.setImageResource(R.drawable.ic_bottom_arrow_white);
                } else if (sortMode==WatchedSortingMode.UPDATING_DATE_DESC){
                    sortByDateIconView.setImageResource(R.drawable.ic_top_arrow_white);
                } else {
                    sortByDateIconView.setImageDrawable(null);
                }

                View sortByNameView=dialogView.findViewById(R.id.sort_by_name_view);
                sortByNameView.setOnClickListener(v14 -> {
                    if (sortMode == WatchedSortingMode.NAME_ASC) {
                        sortMode = WatchedSortingMode.NAME_DESC;

                        sortByNameIconView.setImageResource(R.drawable.ic_top_arrow_white);
                    } else {
                        sortMode = WatchedSortingMode.NAME_ASC;

                        sortByNameIconView.setImageResource(R.drawable.ic_bottom_arrow_white);
                    }

                    sortByDateIconView.setImageDrawable(null);

                    sortItems((WatchedRecordsAdapter) resultsView.getAdapter());

                    //dsDialog.dismiss();
                });

                View sortByDateView=dialogView.findViewById(R.id.sort_by_date_view);
                sortByDateView.setOnClickListener(v15 -> {
                    if (sortMode==WatchedSortingMode.UPDATING_DATE_ASC){
                        sortMode=WatchedSortingMode.UPDATING_DATE_DESC;

                        sortByDateIconView.setImageResource(R.drawable.ic_top_arrow_white);
                    } else {
                        sortMode=WatchedSortingMode.UPDATING_DATE_ASC;

                        sortByDateIconView.setImageResource(R.drawable.ic_bottom_arrow_white);
                    }

                    sortByNameIconView.setImageDrawable(null);

                    sortItems((WatchedRecordsAdapter) resultsView.getAdapter());

                    //dsDialog.dismiss();
                });

                CheckBox filterFinishedCheckbox=dialogView.findViewById(R.id.filter_finished_checkbox);
                filterFinishedCheckbox.setChecked(filterFinished);
                filterFinishedCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    filterFinished=isChecked;

                    WatchedRecordsAdapter adapter=(WatchedRecordsAdapter) resultsView.getAdapter();

                    adapter.filter(filterQuery, isChecked);
                    sortItems(adapter);
                });

                View filterFinishedTextView=dialogView.findViewById(R.id.filter_finished_textview);
                filterFinishedTextView.setOnClickListener(v16 -> {
                    filterFinishedCheckbox.setChecked(!filterFinishedCheckbox.isChecked());
                });

                View clearFiltersButton=dialogView.findViewById(R.id.clear_filters_btn);
                clearFiltersButton.setOnClickListener(v17 -> {
                    filterQuery="";
                    filterFinished=false;

                    filterEditText.setText("");
                    filterFinishedCheckbox.setChecked(false);

                    WatchedRecordsAdapter adapter=(WatchedRecordsAdapter) resultsView.getAdapter();

                    adapter.filter(filterQuery, filterFinished);
                    sortItems(adapter);
                });

                dsDialog.setContentView(dialogView);
                dsDialog.setCancelable(true);
                dsDialog.setCanceledOnTouchOutside(true);
                dsDialog.show();
            });
        });

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
    }

    @Override
    public void onResume() {
        super.onResume();

        setupPinnedPlaylists();

        loadInfo();

        toTopBtn.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (resultsView != null && resultsView.getLayoutManager()!=null)
            savedListState = resultsView.getLayoutManager().onSaveInstanceState();
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
    public void onHiddenChanged (boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    private void setupPinnedPlaylists(){
        List<PlaylistEntity> playlists=database.playlistDAO().getAllPinned();

        if (playlists.size() == 0) {
            pinnedPlaylistsView.setVisibility(View.GONE);
        } else {
            PinnedPlaylistsChipsAdapter adapter = new PinnedPlaylistsChipsAdapter(playlists, new CustomOnItemClickListener() {
                @Override
                public void onClick(View v, int positiion) {
                    NavigationUtils.openPlaylistFragment(requireActivity(), playlists.get(positiion).id);
                }
            });
            pinnedPlaylistsView.setAdapter(adapter);

            pinnedPlaylistsView.setVisibility(View.VISIBLE);
        }
    }

    private void loadInfo() {
        if (isLoading.get()) return;

        isLoading.set(true);

        //showOrHideMainLayout(false);

        if (currentWorker != null) currentWorker.dispose();

        currentWorker = Single.fromCallable(() -> database.animeWatchedDAO().getAll())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final List<AnimeWatchedEntity> result) -> {
                    isLoading.set(false);

                    //showOrHideMainLayout(true);

                    handleResult(result);
                }, (@NonNull final Throwable throwable) -> {
                    isLoading.set(false);

                    handleError(throwable);
                });
    }

    private void handleResult(List<AnimeWatchedEntity> records) {
        if (records.size() > 0) {
            showOrHideMainLayout(true);
            showOrHideEmptyLayout(false);

            setAdapter(records, listMode);

            if (savedListState != null) {
                resultsView.getLayoutManager().onRestoreInstanceState(savedListState);
            }
        } else {
            showOrHideMainLayout(false);
            showOrHideEmptyLayout(true);
        }
    }

    private void setAdapter(List<AnimeWatchedEntity> records, ListMode mode){
        if (mode == ListMode.LIST){
            LinearLayoutManager layoutManager=new LinearLayoutManager(requireContext());
            layoutManager.setOrientation(RecyclerView.VERTICAL);
            resultsView.setLayoutManager(layoutManager);
        } else if (mode == ListMode.GRID){
            GridLayoutManager layoutManager=new GridLayoutManager(requireContext(), 4);
            layoutManager.setOrientation(RecyclerView.VERTICAL);
            resultsView.setLayoutManager(layoutManager);
        }

        WatchedRecordsAdapter adapter = new WatchedRecordsAdapter(records, mode, new CustomOnItemClickListener(){
            @Override
            public void onClick(View v, int position){
                AnimeWatchedEntity item=((WatchedRecordsAdapter) resultsView.getAdapter()).getItem(position);

                NavigationUtils.openAnimeFragment(requireActivity(), item.malid, InfoSourceType.WATCHED);
            }
        });
        adapter.setAdditionalParams(defPrefs.getBoolean(Constants.SHOW_EPISODES_LEFT_BADGE, true), defPrefs.getBoolean(Constants.SHOW_ENGLISH_TITLES, false));
        adapter.filter(filterQuery, filterFinished);
        sortItems(adapter);
        resultsView.setAdapter(adapter);
    }

    private void sortItems(WatchedRecordsAdapter adapter) {
        if (sortMode == WatchedSortingMode.UPDATING_DATE_DESC) {
            adapter.sortByUpdatingDate(false);
        } else if (sortMode == WatchedSortingMode.UPDATING_DATE_ASC){
            adapter.sortByUpdatingDate(true);
        } else if (sortMode == WatchedSortingMode.NAME_DESC) {
            adapter.sortByName(false);
        } else if (sortMode == WatchedSortingMode.NAME_ASC){
            adapter.sortByName(true);
        }
    }

    private void updateLoading(boolean loading){
        showOrHideMainLayout(!loading);
        hideErrorLayout();
        showOrHideLoadingLayout(loading);
    }

    private void showOrHideMainLayout(boolean show){
        View mainLayout=getView().findViewById(R.id.main_layout);
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
        View errorLayout=getView().findViewById(R.id.error_layout);
        errorLayout.setVisibility(View.VISIBLE);

        TextView titleView=errorLayout.findViewById(R.id.error_title);
        titleView.setText(title);

        TextView messageView=errorLayout.findViewById(R.id.error_message);
        messageView.setText(message);

        TextView reloadButton=errorLayout.findViewById(R.id.error_reload_btn);
        reloadButton.setOnClickListener(v -> loadInfo());
    }

    private void hideErrorLayout(){
        View errorLayout=getView().findViewById(R.id.error_layout);
        errorLayout.setVisibility(View.GONE);
    }

    private void handleError(Throwable e){
        showOrHideLoadingLayout(false);
        showErrorLayout(getString(R.string.error_happened), e.getMessage());
    }

    private void changeActionBarListModeButtonIcon(ListMode listMode){
        View rootView=getView();
        ImageButton listModeBtn=rootView.findViewById(R.id.list_mode_btn);

        if (listMode==ListMode.LIST){
            listModeBtn.setImageResource(R.drawable.ic_grid_white);
        } else if (listMode==ListMode.GRID){
            listModeBtn.setImageResource(R.drawable.ic_list_white);
        }
    }

}

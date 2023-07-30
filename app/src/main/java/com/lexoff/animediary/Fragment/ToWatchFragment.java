package com.lexoff.animediary.Fragment;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lexoff.animediary.Adapter.PinnedPlaylistsChipsAdapter;
import com.lexoff.animediary.Adapter.ToWatchRecordsAdapter;
import com.lexoff.animediary.Constants;
import com.lexoff.animediary.CustomOnItemClickListener;
import com.lexoff.animediary.Database.ADatabase;
import com.lexoff.animediary.Database.AppDatabase;
import com.lexoff.animediary.Database.DAO.AnimeToWatchDAO;
import com.lexoff.animediary.Database.DAO.NoteDAO;
import com.lexoff.animediary.Database.Model.AnimeToWatchEntity;
import com.lexoff.animediary.Database.Model.PlaylistEntity;
import com.lexoff.animediary.Enum.InfoSourceType;
import com.lexoff.animediary.Enum.ListMode;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Util.NavigationUtils;
import com.lexoff.animediary.Util.ROMTHelper;
import com.lexoff.animediary.Util.ResourcesHelper;
import com.lexoff.animediary.Util.Utils;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ToWatchFragment extends BaseFragment {

    private SharedPreferences defPrefs;
    private AppDatabase database;

    private AtomicBoolean isLoading = new AtomicBoolean(false);
    private Disposable currentWorker;

    private NestedScrollView mainLayout;
    private RecyclerView resultsView;

    private RecyclerView pinnedPlaylistsView;

    private View toTopBtn;

    private Parcelable savedListState;

    private ListMode listMode=ListMode.LIST;

    public ToWatchFragment() {
        //empty
    }

    public static ToWatchFragment newInstance() {
        ToWatchFragment fragment = new ToWatchFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        defPrefs=PreferenceManager.getDefaultSharedPreferences(requireContext());
        listMode=Utils.intToListMode(defPrefs.getInt(Constants.TOWATCH_LIST_MODE_KEY, 0));

        database=ADatabase.getInstance(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_towatch, container, false);
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

        mainLayout=rootView.findViewById(R.id.main_layout);
        resultsView=rootView.findViewById(R.id.results_view);

        pinnedPlaylistsView=rootView.findViewById(R.id.pinned_playlists_view);

        GridLayoutManager layoutManager=new GridLayoutManager(requireContext(), 1);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        pinnedPlaylistsView.setLayoutManager(layoutManager);

        ImageButton listModeBtn=rootView.findViewById(R.id.list_mode_btn);
        changeActionBarListModeButtonIcon(listMode);
        listModeBtn.setOnClickListener(v -> {
            Utils.animateClickOnImageButton(v, () -> {
                if (listMode == ListMode.LIST){
                    ToWatchRecordsAdapter adapter=(ToWatchRecordsAdapter) resultsView.getAdapter();

                    if (adapter==null) return;

                    listMode=ListMode.GRID;

                    changeActionBarListModeButtonIcon(listMode);

                    defPrefs.edit().putInt(Constants.TOWATCH_LIST_MODE_KEY, Utils.listModeToInt(listMode)).apply();

                    setAdapter(adapter.getItems(), listMode);
                } else if (listMode == ListMode.GRID){
                    ToWatchRecordsAdapter adapter=(ToWatchRecordsAdapter) resultsView.getAdapter();

                    if (adapter==null) return;

                    listMode=ListMode.LIST;

                    changeActionBarListModeButtonIcon(listMode);

                    defPrefs.edit().putInt(Constants.TOWATCH_LIST_MODE_KEY, Utils.listModeToInt(listMode)).apply();

                    setAdapter(adapter.getItems(), listMode);
                }
            });
        });

        ImageButton randomBtn=rootView.findViewById(R.id.random_btn);
        randomBtn.setOnClickListener(v -> {
            Utils.animateClickOnImageButton(v, () -> {
                try {
                    Random random=new Random();
                    int tries=0;

                    List<AnimeToWatchEntity> records=database.animeToWatchDAO().getAll();
                    AnimeToWatchEntity record=records.get(random.nextInt(records.size()));
                    while (record.malid<Constants.SMALLEST_LOCAL_MALID && tries<5){
                        record=records.get(random.nextInt(records.size()));

                        tries++;
                    }
                    NavigationUtils.openAnimeFragment(requireActivity(), record.malid, InfoSourceType.TO_WATCH);
                } catch (Exception e){}
            });
        });

        ImageButton addBtn=rootView.findViewById(R.id.add_entry_btn);
        addBtn.setOnClickListener(v -> {
            Utils.animateClickOnImageButton(v, () -> {
                showAddDialog();
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

        currentWorker = Single.fromCallable(() -> database.animeToWatchDAO().getAll())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final List<AnimeToWatchEntity> result) -> {
                    isLoading.set(false);

                    //showOrHideMainLayout(true);

                    handleResult(result);
                }, (@NonNull final Throwable throwable) -> {
                    isLoading.set(false);

                    handleError(throwable);
                });
    }

    private void handleResult(List<AnimeToWatchEntity> records) {
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

    private void setAdapter(List<AnimeToWatchEntity> records, ListMode mode){
        if (mode == ListMode.LIST){
            LinearLayoutManager layoutManager=new LinearLayoutManager(requireContext());
            layoutManager.setOrientation(RecyclerView.VERTICAL);
            resultsView.setLayoutManager(layoutManager);
        } else if (mode == ListMode.GRID){
            GridLayoutManager layoutManager=new GridLayoutManager(requireContext(), 4);
            layoutManager.setOrientation(RecyclerView.VERTICAL);
            resultsView.setLayoutManager(layoutManager);
        }

        ToWatchRecordsAdapter adapter = new ToWatchRecordsAdapter(records, mode, new CustomOnItemClickListener(){
            @Override
            public void onClick(View v, int position){
                AnimeToWatchEntity item=((ToWatchRecordsAdapter) resultsView.getAdapter()).getItem(position);

                NavigationUtils.openAnimeFragment(requireActivity(), item.malid, InfoSourceType.TO_WATCH);
            }
        });
        adapter.setAdditionalParams(defPrefs.getBoolean(Constants.SHOW_ENGLISH_TITLES, false));
        resultsView.setAdapter(adapter);
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

    //this is only for items not existing in mal db
    //for example for unannounced sequels
    private void showAddDialog(){
        View dialogView=View.inflate(requireContext(), R.layout.add_towatch_item_dialog, null);
        EditText titleEditText=dialogView.findViewById(R.id.title_edittext);
        EditText secondaryTitleEditText=dialogView.findViewById(R.id.secondary_title_edittext);
        EditText summaryEditText=dialogView.findViewById(R.id.summary_edittext);

        TextView titleValidationTextView=dialogView.findViewById(R.id.title_validation_textview);

        AlertDialog dialog=new MaterialAlertDialogBuilder(requireContext(), R.style.DarkDialogTheme)
                .setView(dialogView)
                .setCancelable(true)
                .setPositiveButton(getString(R.string.dialog_add_button_title), null)
                .setNegativeButton(getString(R.string.dialog_cancel_button_title), null)
                .setBackground(ResourcesHelper.roundedDarkDialogBackground())
                .create();

        dialog.setOnShowListener(d -> {
            titleEditText.requestFocus();

            Button positiveBtn= dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            positiveBtn.setOnClickListener(v -> {
                if (titleEditText.getText().length()==0){
                    titleValidationTextView.setVisibility(View.VISIBLE);

                    return;
                }

                ROMTHelper.runOnMainThread(() -> {
                    addRecord(titleEditText.getText().toString(), secondaryTitleEditText.getText().toString(), summaryEditText.getText().toString());
                }, Constants.POST_EXEC_SMALLEST_DELAY);

                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void addRecord(String title, String secondaryTitle, String summary){
        AnimeToWatchDAO animeToWatchDAO=database.animeToWatchDAO();
        NoteDAO noteDAO=database.noteDAO();

        long newMalid=animeToWatchDAO.getMinMALId();
        if (newMalid>Constants.SMALLEST_LOCAL_MALID) {
            newMalid=Constants.SMALLEST_LOCAL_MALID;
        } else {
            newMalid-=1;
        }

        //since notes don't gets deleted AT ALL
        //if anime was added and then note was written
        //and then anime was removed
        //so if add new anime it gets same id as previous with note
        //and this way new anime gets wrong note
        //to prevent this from happening this check is needed
        //TODO: remove this loop after correct notes recycling will be implemented
        while (noteDAO.countByMALId(newMalid)==1){
            newMalid-=1;
        }

        AnimeToWatchEntity record = new AnimeToWatchEntity(title, secondaryTitle, summary, "", newMalid, new Date().getTime(), 0, "", "", "", "", "", "", "", "", "", "", "", "", "");

        animeToWatchDAO.insert(record);

        Toast.makeText(requireContext(), String.format(getString(R.string.added_to_towatch_toast_message), title), Toast.LENGTH_SHORT).show();

        loadInfo();
    }

}

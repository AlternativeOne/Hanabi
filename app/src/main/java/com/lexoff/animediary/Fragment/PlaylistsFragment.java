package com.lexoff.animediary.Fragment;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lexoff.animediary.Adapter.PlaylistsAdapter;
import com.lexoff.animediary.Constants;
import com.lexoff.animediary.CustomOnItemClickListener;
import com.lexoff.animediary.Database.ADatabase;
import com.lexoff.animediary.Database.AppDatabase;
import com.lexoff.animediary.Database.Model.PlaylistEntity;
import com.lexoff.animediary.Util.NavigationUtils;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Util.ResourcesHelper;
import com.lexoff.animediary.Util.Utils;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PlaylistsFragment extends BaseFragment {

    private AppDatabase database;
    private SharedPreferences defPrefs;

    private Handler toastHandler;

    private AtomicBoolean isLoading = new AtomicBoolean(false);
    private Disposable currentWorker;

    private RecyclerView resultsView;
    private Parcelable savedListState;

    public PlaylistsFragment() {
        //empty
    }

    public static PlaylistsFragment newInstance() {
        PlaylistsFragment fragment = new PlaylistsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        toastHandler=new Handler(Looper.myLooper(), null);

        defPrefs=PreferenceManager.getDefaultSharedPreferences(requireContext());

        database=ADatabase.getInstance(requireContext());
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
        return inflater.inflate(R.layout.fragment_playlists, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        int navbarHeight= Utils.getNavBarHeight(requireContext());
        rootView.setPadding(0, 0, 0, navbarHeight);

        int statusbarHeight= Utils.getStatusBarHeight(requireContext());
        View sbMarginView=rootView.findViewById(R.id.statusbar_margin_view);
        ViewGroup.LayoutParams params=sbMarginView.getLayoutParams();
        params.height=statusbarHeight;
        sbMarginView.setLayoutParams(params);

        ImageView backBtn = rootView.findViewById(R.id.ab_back_btn);
        backBtn.setOnClickListener(v -> {
            Utils.animateClickOnImageButton(v, () -> {
                requireActivity().onBackPressed();
            });
        });

        ImageButton addBtn=rootView.findViewById(R.id.add_playlist_btn);
        addBtn.setOnClickListener(v -> {
            Utils.animateClickOnImageButton(v, () -> {
                showAddDialog();
            });
        });

        resultsView=rootView.findViewById(R.id.results_view);

        LinearLayoutManager layoutManager=new LinearLayoutManager(requireContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        resultsView.setLayoutManager(layoutManager);
    }

    @Override
    public void onResume() {
        super.onResume();

        loadInfo();
    }

    @Override
    public void onPause(){
        super.onPause();

        savedListState=resultsView.getLayoutManager().onSaveInstanceState();
    }

    @Override
    public void onHiddenChanged (boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden){
            savedListState=resultsView.getLayoutManager().onSaveInstanceState();

            loadInfo();
        }
    }

    private void loadInfo() {
        if (isLoading.get()) return;

        isLoading.set(true);

        showOrHideMainLayout(false);

        if (currentWorker != null) currentWorker.dispose();

        currentWorker = Single.fromCallable(() -> database.playlistDAO().getAll())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final List<PlaylistEntity> result) -> {
                    isLoading.set(false);

                    showOrHideMainLayout(true);

                    handleResult(result);
                }, (@NonNull final Throwable throwable) -> {
                    isLoading.set(false);

                    handleError(throwable);
                });
    }

    private void handleResult(List<PlaylistEntity> records){
        if (records.size()>0) {
            showOrHideMainLayout(true);
            showOrHideEmptyLayout(false);

            setAdapter(records);

            if (savedListState!=null) {
                resultsView.getLayoutManager().onRestoreInstanceState(savedListState);
            }
        } else {
            showOrHideMainLayout(false);
            showOrHideEmptyLayout(true);
        }
    }

    private void setAdapter(List<PlaylistEntity> records){
        PlaylistsAdapter adapter = new PlaylistsAdapter(requireContext(), records, new CustomOnItemClickListener(){
            @Override
            public void onClick(View v, int position){
                PlaylistEntity item=((PlaylistsAdapter) resultsView.getAdapter()).getItem(position);

                NavigationUtils.openPlaylistFragment(requireActivity(), item.id);
            }
        });
        adapter.filterPinnedFirst();
        resultsView.setAdapter(adapter);
    }

    private void showAddDialog(){
        View dialogView=View.inflate(requireContext(), R.layout.add_playlist_dialog, null);
        EditText nameEditText=dialogView.findViewById(R.id.name_edittext);
        EditText descriptionEditText=dialogView.findViewById(R.id.description_edittext);

        TextView nameValidationTextView=dialogView.findViewById(R.id.name_validation_textview);

        AlertDialog dialog=new MaterialAlertDialogBuilder(requireContext(), R.style.DarkDialogTheme)
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.dialog_add_button_title), null)
                .setNegativeButton(getString(R.string.dialog_cancel_button_title), null)
                .setBackground(ResourcesHelper.roundedDarkDialogBackground())
                .create();

        dialog.setOnShowListener(d -> {
            nameEditText.requestFocus();

            Button positiveBtn= dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            positiveBtn.setOnClickListener(v -> {
                if (nameEditText.getText().length()==0){
                    nameValidationTextView.setVisibility(View.VISIBLE);

                    return;
                }

                positiveBtn.postDelayed(() -> {
                    addRecord(nameEditText.getText().toString(), descriptionEditText.getText().toString());
                }, Constants.POST_EXEC_SMALLEST_DELAY);

                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void addRecord(String name, String description){
        PlaylistEntity record=new PlaylistEntity(name, description, 0);
        database.playlistDAO().insert(record);

        loadInfo();
    }

    private void updateLoading(boolean loading){
        showOrHideMainLayout(!loading);
        hideErrorLayout();
        showOrHideLoadingLayout(loading);
    }

    private void showOrHideMainLayout(boolean show){
        //View mainLayout=getView().findViewById(R.id.main_layout);
        //mainLayout.setVisibility(show ? View.VISIBLE : View.GONE);

        resultsView.setVisibility(show ? View.VISIBLE : View.GONE);
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

}

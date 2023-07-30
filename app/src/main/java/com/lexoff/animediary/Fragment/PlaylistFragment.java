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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lexoff.animediary.Adapter.PlaylistItemsAdapter;
import com.lexoff.animediary.Constants;
import com.lexoff.animediary.CustomOnItemClickListener;
import com.lexoff.animediary.Database.ADatabase;
import com.lexoff.animediary.Database.AppDatabase;
import com.lexoff.animediary.Database.Model.PlaylistEntity;
import com.lexoff.animediary.Database.Model.PlaylistStreamEntity;
import com.lexoff.animediary.Enum.InfoSourceType;
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

public class PlaylistFragment extends BaseFragment {

    private int playlistId=-1;
    private PlaylistEntity playlist;

    private AppDatabase database;
    private SharedPreferences defPrefs;

    private Handler toastHandler;

    private AtomicBoolean isLoading = new AtomicBoolean(false);
    private Disposable currentWorker;

    private RecyclerView resultsView;
    private Parcelable savedListState;

    private TextView resultsTitleView;
    private ImageView pinButton;

    public PlaylistFragment() {
        //empty
    }

    public static PlaylistFragment newInstance(int playlistId) {
        PlaylistFragment fragment = new PlaylistFragment();
        fragment.setPlaylistId(playlistId);
        return fragment;
    }

    private void setPlaylistId(int playlistId){
        this.playlistId=playlistId;
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
        return inflater.inflate(R.layout.fragment_playlist, container, false);
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

        resultsTitleView=rootView.findViewById(R.id.results_title);
        pinButton=rootView.findViewById(R.id.pin_btn);
        pinButton.setOnClickListener(v -> {
            Utils.animateClickOnImageButton(v, () -> {
                database.playlistDAO().setPinnedUnpinned(playlist.id, playlist.pinned == 0 ? 1 : 0);

                updatePlaylistInfo();
            });
        });

        ImageView deleteBtn=rootView.findViewById(R.id.delete_btn);
        deleteBtn.setOnClickListener(v -> {
            Utils.animateClickOnImageButton(v, () -> {
                showDeleteDialog();
            });
        });

        ImageView editBtn=rootView.findViewById(R.id.edit_btn);
        editBtn.setOnClickListener(v -> Utils.animateClickOnImageButton(v, () -> {
            showEditDialog(playlist);
        }));

        resultsView=rootView.findViewById(R.id.results_view);

        LinearLayoutManager layoutManager=new LinearLayoutManager(requireContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        resultsView.setLayoutManager(layoutManager);

        ItemTouchHelper itemTouchHelper=new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT){
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder source, @NonNull RecyclerView.ViewHolder target) {
                if (source.getItemViewType() != target.getItemViewType()) {
                    return false;
                }

                final int sourceIndex = source.getLayoutPosition();
                final int targetIndex = target.getLayoutPosition();

                if (recyclerView.getAdapter()==null) {
                    return false;
                }

                PlaylistItemsAdapter adapter=(PlaylistItemsAdapter) recyclerView.getAdapter();

                PlaylistStreamEntity sourceItem=adapter.getItem(sourceIndex),
                        targetItem=adapter.getItem(targetIndex);

                adapter.moveItems(sourceIndex, targetIndex);

                database.playlistStreamDAO().updatePosition(sourceItem.id, targetIndex);
                database.playlistStreamDAO().updatePosition(targetItem.id, sourceIndex);

                return true;
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            }
        });
        itemTouchHelper.attachToRecyclerView(resultsView);

        updatePlaylistInfo();
    }

    @Override
    public void onResume() {
        super.onResume();

        loadInfo();

        if (savedListState!=null) {
            resultsView.getLayoutManager().onRestoreInstanceState(savedListState);
        }
    }

    @Override
    public void onPause(){
        super.onPause();

        savedListState=resultsView.getLayoutManager().onSaveInstanceState();
    }

    @Override
    public void onHiddenChanged (boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden) {
            loadInfo();
        }
    }

    private void updatePlaylistInfo(){
        playlist=database.playlistDAO().getById(playlistId);
        resultsTitleView.setText(playlist.name);
        if (playlist.pinned==1){
            pinButton.setImageResource(R.drawable.ic_unpin_white);
        } else {
            pinButton.setImageResource(R.drawable.ic_pin_white);
        }
    }

    private void loadInfo() {
        if (isLoading.get()) return;

        isLoading.set(true);

        showOrHideMainLayout(false);

        if (currentWorker != null) currentWorker.dispose();

        currentWorker = Single.fromCallable(() -> database.playlistStreamDAO().getAll(playlistId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final List<PlaylistStreamEntity> result) -> {
                    isLoading.set(false);

                    showOrHideMainLayout(true);

                    handleResult(result);
                }, (@NonNull final Throwable throwable) -> {
                    isLoading.set(false);

                    handleError(throwable);
                });
    }

    private void handleResult(List<PlaylistStreamEntity> records){
        if (records.size()>0) {
            showOrHideMainLayout(true);
            showOrHideEmptyLayout(false);

            setAdapter(records);
        } else {
            showOrHideMainLayout(false);
            showOrHideEmptyLayout(true);
        }
    }

    private void setAdapter(List<PlaylistStreamEntity> records){
        PlaylistItemsAdapter adapter = new PlaylistItemsAdapter(requireContext(), records, new CustomOnItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                PlaylistStreamEntity item = ((PlaylistItemsAdapter) resultsView.getAdapter()).getItem(position);

                NavigationUtils.openAnimeFragment(requireActivity(), item.malid, InfoSourceType.PLAYLIST);
            }

            @Override
            public void onClick2(View v, int position) {
                PlaylistItemsAdapter adapter=(PlaylistItemsAdapter) resultsView.getAdapter();

                PlaylistStreamEntity item = adapter.getItem(position);
                database.playlistStreamDAO().deleteById(item.id);

                adapter.removeItem(position);

                showToastMessage(String.format(getString(R.string.removed_from_playlist_toast_message), item.title, resultsTitleView.getText()));
            }
        });
        adapter.setAdditionalParams(defPrefs.getBoolean(Constants.SHOW_ENGLISH_TITLES, false), defPrefs.getBoolean(Constants.SHOW_ADDED_TO_BADGE, true));
        resultsView.setAdapter(adapter);
    }

    private void showEditDialog(PlaylistEntity item){
        View dialogView=View.inflate(requireContext(), R.layout.add_playlist_dialog, null);
        TextView dialogTitleView=dialogView.findViewById(R.id.dialog_title_textview);
        dialogTitleView.setText(R.string.edit_playlist_dialog_title);

        EditText nameEditText=dialogView.findViewById(R.id.name_edittext);
        nameEditText.setText(item.name);
        nameEditText.setSelection(item.name.length()); //TODO: bad UX?
        EditText descriptionEditText=dialogView.findViewById(R.id.description_edittext);
        descriptionEditText.setText(item.description);

        TextView nameValidationTextView=dialogView.findViewById(R.id.name_validation_textview);

        AlertDialog dialog=new MaterialAlertDialogBuilder(requireContext(), R.style.DarkDialogTheme)
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.dialog_save_button_title), null)
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
                    updateRecord(item.id, nameEditText.getText().toString(), descriptionEditText.getText().toString());
                }, Constants.POST_EXEC_SMALLEST_DELAY);

                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void updateRecord(int id, String newName, String newDescription){
        database.playlistDAO().update(id, newName, newDescription);

        updatePlaylistInfo();

        loadInfo();
    }

    private void showDeleteDialog(){
        new MaterialAlertDialogBuilder(requireContext(), R.style.DarkDialogTheme)
                .setMessage(String.format(getString(R.string.dialog_delete_message), playlist.name))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.dialog_delete_button_title), (dialog, which) -> {
                    database.playlistDAO().deleteById(playlist.id);
                    database.playlistStreamDAO().deleteByPlaylistId(playlist.id);

                    requireActivity().onBackPressed();
                })
                .setNegativeButton(getString(R.string.dialog_cancel_button_title), null)
                .setBackground(ResourcesHelper.roundedDarkDialogBackground())
                .create()
                .show();
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

package com.lexoff.animediary.Fragment;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.preference.PreferenceManager;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.lexoff.animediary.Adapter.GenresAdapter;
import com.lexoff.animediary.Adapter.PlaylistsAdapter;
import com.lexoff.animediary.Adapter.PlaylistsChipsAdapter;
import com.lexoff.animediary.Api;
import com.lexoff.animediary.Constants;
import com.lexoff.animediary.CustomOnItemClickListener;
import com.lexoff.animediary.CustomSpinnerArrayAdapter;
import com.lexoff.animediary.Database.ADatabase;
import com.lexoff.animediary.Database.AnimeToWatchEntity;
import com.lexoff.animediary.Database.AnimeWatchedEntity;
import com.lexoff.animediary.Database.AppDatabase;
import com.lexoff.animediary.Database.NoteEntity;
import com.lexoff.animediary.Database.PlaylistEntity;
import com.lexoff.animediary.Database.PlaylistStreamEntity;
import com.lexoff.animediary.ImageLoaderWrapper;
import com.lexoff.animediary.Info.AnimeAdditionalInfo;
import com.lexoff.animediary.Info.AnimeInfo;
import com.lexoff.animediary.InfoSourceType;
import com.lexoff.animediary.NavigationUtils;
import com.lexoff.animediary.R;
import com.lexoff.animediary.SelectSpinner;
import com.lexoff.animediary.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AnimeFragment extends Fragment {

    private final int TOAST_ANIMATION_DURATION=100;
    private final int WRITE_TO_DATABASE_DELAY=250;

    private AtomicBoolean isLoading = new AtomicBoolean(false);
    private Disposable currentWorker;

    private AtomicBoolean isAdditionalLoading=new AtomicBoolean(false);
    private Disposable additionalWorker;

    private Handler noteHandler, toastHandler, tipHolder;

    private SharedPreferences defPrefs;

    private AppDatabase database;

    private long malid;
    private InfoSourceType type=InfoSourceType.UNDEFINED;

    private View rootView;

    private TextView titleView, secondTitleView, summaryView;
    private ImageView thumbnailView;

    private ImageView addToWatchedButton, addToWatchButton;
    private TextView addToWatchedButtonTitleView, addToWatchButtonTitleView;
    private TextView markAsFinishedButton, addToPlaylistButton;

    private RecyclerView addedToPlaylistsResultsView;

    private TextView typeView, episodesCountView, statusView, airedView, producersView, studiosView, durationView;
    private RecyclerView tagsView;
    private LinearLayout availableAtLayout, relationsLayout, openingThemesLayout, endingThemesLayout;

    private RatingBar ratingBar;

    private EditText notesView;

    private AnimeInfo currentInfo;
    private AnimeAdditionalInfo additionalInfo;

    public AnimeFragment() {
        //empty
    }

    public static AnimeFragment newInstance(long malid, InfoSourceType type) {
        AnimeFragment fragment = new AnimeFragment();
        fragment.setMalid(malid);
        fragment.setType(type);
        return fragment;
    }

    private void setMalid(long malid){
        this.malid=malid;
    }

    private void setType(InfoSourceType type){
        this.type=type;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        noteHandler=new Handler(Looper.getMainLooper());
        toastHandler=new Handler(Looper.getMainLooper());
        tipHolder=new Handler(Looper.getMainLooper());

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

        if (additionalWorker!=null){
            additionalWorker.dispose();
            additionalWorker=null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_anime, container, false);
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

        //this needs to be done once!
        View toastLayout=rootView.findViewById(R.id.toast_layout);
        FrameLayout.LayoutParams params2=(FrameLayout.LayoutParams) toastLayout.getLayoutParams();
        params2.setMargins(params2.leftMargin, params2.topMargin, params2.rightMargin, params2.bottomMargin+navbarHeight);
        toastLayout.setLayoutParams(params2);

        View errorLayout=rootView.findViewById(R.id.error_layout);
        FrameLayout.LayoutParams params3=(FrameLayout.LayoutParams) errorLayout.getLayoutParams();
        params3.setMargins(params3.leftMargin, params3.topMargin, params3.rightMargin, params3.bottomMargin+navbarHeight);
        errorLayout.setLayoutParams(params3);

        this.rootView=rootView;

        ImageView backBtn=rootView.findViewById(R.id.ab_back_btn);
        backBtn.setOnClickListener(v -> {
            Utils.animateClickOnImageButton(v, () -> {
                requireActivity().onBackPressed();
            });
        });

        ImageView shareBtn=rootView.findViewById(R.id.ab_share_btn);
        shareBtn.setOnClickListener(v -> {
            Utils.animateClickOnImageButton(v, () -> {
                if (currentInfo!=null){
                    Utils.copyToClipboard(requireContext(), "", Utils.buildAnimeUrl(currentInfo.getMalid()));
                }
            });
        });

        ImageView refreshBtn=rootView.findViewById(R.id.ab_refresh_btn);
        refreshBtn.setOnClickListener(v -> {
            Utils.animateClickOnImageButton(v, () -> {
                refreshInfo(currentInfo.getMalid());
            });
        });
        if (type==InfoSourceType.REMOTE){
            refreshBtn.setVisibility(View.GONE);
        } else {
            refreshBtn.setVisibility(View.VISIBLE);
        }

        titleView=rootView.findViewById(R.id.title_view);
        titleView.setOnLongClickListener(v -> {
            Utils.copyToClipboard(requireContext(), "", currentInfo.getTitle());

            return true;
        });

        secondTitleView=rootView.findViewById(R.id.second_title_view);
        secondTitleView.setOnLongClickListener(v -> {
            Utils.copyToClipboard(requireContext(), "", currentInfo.getSecondTitle());

            return true;
        });

        thumbnailView=rootView.findViewById(R.id.thumbnail_view);

        /*TextView posterBtn=rootView.findViewById(R.id.poster_btn);
        posterBtn.setOnClickListener(v ->
                NavigationUtils.openThumbnailViewerFragment(requireActivity(), currentInfo.getMalid())
        );*/

        thumbnailView.setOnClickListener(v -> {
            NavigationUtils.openThumbnailViewerFragment(requireActivity(), currentInfo.getMalid());
        });

        TextView searchMangaBtn=rootView.findViewById(R.id.search_manga_btn);
        searchMangaBtn.setOnClickListener(v -> {
            if (currentInfo!=null) {
                String title=currentInfo.getTitle();

                try {
                    JsonObject sourceMaterialObj = JsonParser.object().from(currentInfo.getSourceMaterial());
                    title=sourceMaterialObj.getString("title");
                } catch (Exception e){}

                NavigationUtils.openMDSearchFragment(requireActivity(), title);
            }
        });

        TextView recommendationsBtn=rootView.findViewById(R.id.recommendations_btn);
        recommendationsBtn.setOnClickListener(v -> {
            if (currentInfo!=null) {
                String title = currentInfo.getTitle();
                if (defPrefs.getBoolean(Constants.SHOW_ENGLISH_TITLES, false) && !currentInfo.getSecondTitle().isEmpty()) {
                    title = currentInfo.getSecondTitle();
                }

                NavigationUtils.openAnimeRecommendations(requireActivity(), currentInfo.getMalid(), title);
            }
        });

        TextView charactersBtn=rootView.findViewById(R.id.characters_btn);
        charactersBtn.setOnClickListener(v -> {
            if (currentInfo!=null){
                NavigationUtils.openAnimeCharactersFragment(requireActivity(), currentInfo.getMalid());
            }
        });

        summaryView=rootView.findViewById(R.id.summary_view);

        //to prevent from "false" clicks while scrolling and etc.
        summaryView.setOnLongClickListener(v -> true);

        typeView=rootView.findViewById(R.id.type_textview);
        episodesCountView=rootView.findViewById(R.id.episodes_textview);
        statusView=rootView.findViewById(R.id.status_textview);
        airedView=rootView.findViewById(R.id.aired_textview);
        producersView=rootView.findViewById(R.id.producers_textview);
        studiosView=rootView.findViewById(R.id.studios_textview);
        durationView=rootView.findViewById(R.id.duration_textview);

        tagsView=rootView.findViewById(R.id.tags_view);

        GridLayoutManager layoutManager1=new GridLayoutManager(requireContext(), 1);
        layoutManager1.setOrientation(RecyclerView.HORIZONTAL);
        tagsView.setLayoutManager(layoutManager1);

        availableAtLayout=rootView.findViewById(R.id.available_at_layout);

        ImageView availableAtExpandBtn=rootView.findViewById(R.id.available_at_expand_btn);
        availableAtExpandBtn.setOnClickListener(v -> {
            notesView.clearFocus();

            if (availableAtLayout.getVisibility()==View.VISIBLE){
                availableAtLayout.setVisibility(View.GONE);

                availableAtExpandBtn.setImageResource(R.drawable.ic_expand_more_white);
            } else {
                availableAtLayout.setVisibility(View.VISIBLE);

                availableAtExpandBtn.setImageResource(R.drawable.ic_expand_less_white);
            }
        });

        relationsLayout=rootView.findViewById(R.id.relations_layout);

        openingThemesLayout=rootView.findViewById(R.id.opening_themes_layout);

        ImageView openingThemesExpandBtn=rootView.findViewById(R.id.opening_themes_expand_btn);
        openingThemesExpandBtn.setOnClickListener(v -> {
            notesView.clearFocus();

            if (openingThemesLayout.getVisibility()==View.VISIBLE){
                openingThemesLayout.setVisibility(View.GONE);

                openingThemesExpandBtn.setImageResource(R.drawable.ic_expand_more_white);
            } else {
                openingThemesLayout.setVisibility(View.VISIBLE);

                openingThemesExpandBtn.setImageResource(R.drawable.ic_expand_less_white);
            }
        });

        endingThemesLayout=rootView.findViewById(R.id.ending_themes_layout);

        ImageView endingThemesExpandBtn=rootView.findViewById(R.id.ending_themes_expand_btn);
        endingThemesExpandBtn.setOnClickListener(v -> {
            notesView.clearFocus();

            if (endingThemesLayout.getVisibility()==View.VISIBLE){
                endingThemesLayout.setVisibility(View.GONE);

                endingThemesExpandBtn.setImageResource(R.drawable.ic_expand_more_white);
            } else {
                endingThemesLayout.setVisibility(View.VISIBLE);

                endingThemesExpandBtn.setImageResource(R.drawable.ic_expand_less_white);
            }
        });

        addToWatchedButton=rootView.findViewById(R.id.add_to_watched_btn);
        addToWatchedButtonTitleView=rootView.findViewById(R.id.add_to_watched_btn_title_view);
        addToWatchButton=rootView.findViewById(R.id.add_to_watch_btn);
        addToWatchButtonTitleView=rootView.findViewById(R.id.add_to_watch_btn_title_view);
        markAsFinishedButton=rootView.findViewById(R.id.mark_as_finished_btn);
        addToPlaylistButton=rootView.findViewById(R.id.add_to_playlist_btn);

        addedToPlaylistsResultsView=rootView.findViewById(R.id.added_to_playlists_results_view);
        GridLayoutManager layoutManager2=new GridLayoutManager(requireContext(), 1);
        layoutManager2.setOrientation(RecyclerView.HORIZONTAL);
        addedToPlaylistsResultsView.setLayoutManager(layoutManager2);

        ratingBar=rootView.findViewById(R.id.rating_bar);
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser){
                if (database.noteDAO().countByMALId(malid)==0){
                    NoteEntity record=new NoteEntity("", rating, malid);

                    database.noteDAO().insert(record);
                } else {
                    NoteEntity record=database.noteDAO().getAllByMALId(malid).get(0);
                    record.rating=rating;

                    database.noteDAO().update(record);
                }
            }
        });

        notesView=rootView.findViewById(R.id.notes_view);
        notesView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                noteHandler.removeCallbacksAndMessages(null);

                noteHandler.postDelayed(() -> {
                    long malid=currentInfo.getMalid();

                    if (database.noteDAO().countByMALId(malid)==0){
                        NoteEntity record=new NoteEntity(s.toString(), 0.0f, malid);

                        database.noteDAO().insert(record);
                    } else {
                        NoteEntity record=database.noteDAO().getAllByMALId(malid).get(0);
                        record.note=s.toString();

                        database.noteDAO().update(record);
                    }
                }, WRITE_TO_DATABASE_DELAY);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        notesView.setOnFocusChangeListener((v, hasFocus) -> {
            //loose of focus happens only if any other button clicked or if fragment closed
            if (!hasFocus) {
                Utils.hideKeyboard(notesView);
            }
        });

        loadInfo();
    }

    @Override
    public void onHiddenChanged (boolean hidden) {
        super.onHiddenChanged(hidden);

        //partial update of info that could change behind the curtains
        if (!hidden){
            setupPlaylistsChips();
        }
    }

    private void loadInfo(){
        if (type==InfoSourceType.REMOTE) {
            loadRemoteInfo();
        } else {
            loadLocalInfo();
        }

        if (defPrefs.getBoolean(Constants.SHOW_NEXT_AIRING_EPISODE, true)) {
            loadAdditionalInfo();
        }
    }

    private void loadRemoteInfo(){
        if (isLoading.get()) return;

        isLoading.set(true);

        updateLoading();

        if (currentWorker != null) currentWorker.dispose();

        currentWorker = Single.fromCallable(() -> (new Api()).getAnimeInfo(malid, false))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final AnimeInfo result) -> {
                    isLoading.set(false);

                    updateLoading();

                    handleResult(result);
                }, (@NonNull final Throwable throwable) -> {
                    isLoading.set(false);
                    handleError(throwable);
                });
    }

    private void refreshInfo(final long malid){
        if (isLoading.get()) return;

        isLoading.set(true);

        updateLoading();

        if (currentWorker != null) currentWorker.dispose();

        currentWorker = Single.fromCallable(() -> (new Api()).getAnimeInfo(malid, true))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final AnimeInfo result) -> {
                    isLoading.set(false);

                    updateLoading();

                    handleResult(result);

                    updateInfo(result);
                }, (@NonNull final Throwable throwable) -> {
                    isLoading.set(false);
                    handleError(throwable);
                });
    }

    private void handleResult(AnimeInfo info) {
        currentInfo = info;

        loadNote(info);

        setBasicInfo();
        setDetailedInfo();

        addToWatchedButton.setVisibility(View.VISIBLE);

        firstUpdateWatchedButton();

        addToWatchedButton.setClickable(true);

        addToWatchButton.setVisibility(View.VISIBLE);

        firstUpdateToWatchButton();

        addToWatchButton.setClickable(true);

        markAsFinishedButton.setClickable(true);

        firstUpdateMarkAsWatchedButton();

        addToPlaylistButton.setClickable(true);

        firstUpdateAddToPlaylistButton();

        /*if (type == 1) {
            showOrHideRatingBar(false);
        }*/

        String[] episodes = new String[info.getEpisodesCount() + 1];

        episodes[0] = getString(R.string.spinner_no_episode);

        for (int i = 1; i <= info.getEpisodesCount(); i++) {
            episodes[i] = String.format(getString(R.string.spinner_episode), i);
        }

        CustomSpinnerArrayAdapter adapter = new CustomSpinnerArrayAdapter(requireContext(), R.layout.custom_spinner_item, episodes);
        SelectSpinner episodesSpinner = rootView.findViewById(R.id.episodes_spinner);
        episodesSpinner.setAdapter(adapter);

        List<AnimeWatchedEntity> records = database.animeWatchedDAO().getAllByMALId(info.getMalid());
        if (records.size() == 1) {
            AnimeWatchedEntity record = records.get(0);
            int wEp = Integer.parseInt(record.watched_episodes);

            episodesSpinner.setSelection(wEp);

            showOrHideEpisodesSection(true);
        }

        episodesSpinner.addOnSelectSpinnerListener(new SelectSpinner.OnSelectSpinnerListener() {
            @Override
            public void onOpenSelectMenu() {

            }

            @Override
            public void onItemSelectByUser(AdapterView<?> parent, View view, int position, long id) {
                String watchedEpisodes = String.valueOf(position);

                database.animeWatchedDAO().updateWatchedEpisodes(currentInfo.getMalid(), watchedEpisodes, new Date().getTime());

                updateMarkAsWatchedButton();
            }
        });

        if (info.getMalid() <= Constants.SMALLEST_LOCAL_MALID) {
            ImageView refreshBtn = rootView.findViewById(R.id.ab_refresh_btn);
            refreshBtn.setVisibility(View.INVISIBLE);

            View detailsLayout = rootView.findViewById(R.id.details_layout);
            detailsLayout.setVisibility(View.GONE);

            View searchMangaBtn=rootView.findViewById(R.id.search_manga_btn);
            searchMangaBtn.setVisibility(View.GONE);

            View recommendationsBtn=rootView.findViewById(R.id.recommendations_btn);
            recommendationsBtn.setVisibility(View.GONE);

            thumbnailView.setClickable(false);
        }
    }

    private void updateInfo(AnimeInfo info){
        database.animeWatchedDAO().partialUpdate(info.getMalid(), info.getTitle(), info.getSecondTitle(), info.getSummary(), info.getThumbnailUrl(), info.getEpisodesCount(), info.getType(), info.getStatus(), info.getAired(), info.getProducers(), info.getStudios(), info.getSourceMaterial(), info.getDuration(), info.getGenres(), info.getTags(), info.getAvailableAt(), info.getRelations(), info.getOpeningThemes(), info.getEndingThemes());
        database.animeWatchedDAO().updatePrequel(info.getMalid(), info.getPrequelMalid());
        database.animeToWatchDAO().partialUpdate(info.getMalid(), info.getTitle(), info.getSecondTitle(), info.getSummary(), info.getThumbnailUrl(), info.getEpisodesCount(), info.getType(), info.getStatus(), info.getAired(), info.getProducers(), info.getStudios(), info.getSourceMaterial(), info.getDuration(), info.getGenres(), info.getTags(), info.getAvailableAt(), info.getRelations(), info.getOpeningThemes(), info.getEndingThemes());
        database.playlistStreamDAO().partialUpdate(info.getMalid(), info.getTitle(), info.getSecondTitle(), info.getSummary(), info.getThumbnailUrl(), info.getEpisodesCount(), info.getType(), info.getStatus(), info.getAired(), info.getProducers(), info.getStudios(), info.getSourceMaterial(), info.getDuration(), info.getGenres(), info.getTags(), info.getAvailableAt(), info.getRelations(), info.getOpeningThemes(), info.getEndingThemes());

        showToastMessage(getString(R.string.info_updated_toast_message));
    }

    private void loadAdditionalInfo(){
        if (isAdditionalLoading.get()) return;

        isAdditionalLoading.set(true);

        if (additionalWorker != null) additionalWorker.dispose();

        additionalWorker = Single.fromCallable(() -> (new Api()).getAnimeAdditionalInfo(malid))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final AnimeAdditionalInfo result) -> {
                    isAdditionalLoading.set(false);

                    handleResult(result);
                }, (@NonNull final Throwable throwable) -> {
                    isAdditionalLoading.set(false);
                });
    }

    private void handleResult(AnimeAdditionalInfo info) {
        additionalInfo = info;

        View nextEpisodeRow = rootView.findViewById(R.id.next_episode_row);
        nextEpisodeRow.setVisibility(info.hasNextEpisode() ? View.VISIBLE : View.GONE);

        if (!info.hasNextEpisode()) return;

        Date now = new Date();
        Date airingAt = new Date(info.getNextEpisodeAiringAt() * 1000);
        long datesDiff = (airingAt.getTime() - now.getTime()) / 1000;

        int hours = (int) (datesDiff / 3600);
        int temp = (int) (datesDiff - hours * 3600);
        int mins = temp / 60;
        int days = hours / 24;
        hours = hours % 24;

        if (mins>0) hours+=1;

        String when = days > 0 ? String.format("%d d. %d h.", days, hours) : String.format("%d h.", hours);

        TextView nextEpisodeView = nextEpisodeRow.findViewById(R.id.next_episode_textview);
        nextEpisodeView.setText(String.format("%d in %s", info.getNextEpisode(), when));
    }

    private void firstUpdateWatchedButton(){
        if (currentInfo.getMalid() <= Constants.SMALLEST_LOCAL_MALID){
            addToWatchedButton.setVisibility(View.GONE);

            //basically, layout_gravity=center_horizontal
            ConstraintLayout.LayoutParams params=(ConstraintLayout.LayoutParams) addToWatchButton.getLayoutParams();
            params.leftToLeft=ConstraintLayout.LayoutParams.PARENT_ID;
            params.rightToRight=ConstraintLayout.LayoutParams.PARENT_ID;
            addToWatchButton.setLayoutParams(params);

            return;
        }

        if (database.animeWatchedDAO().countByMALId(currentInfo.getMalid()) == 1) {
            addToWatchedButton.setImageResource(R.drawable.ic_heart_filled_white);

            addToWatchedButtonTitleView.setTextColor(Color.parseColor("#FFFFFF"));
            addToWatchedButtonTitleView.setText(getString(R.string.remove_from_watched_button_title));
        } else {
            addToWatchedButton.setImageResource(R.drawable.ic_heart_white);

            addToWatchedButtonTitleView.setTextColor(Color.parseColor("#909090"));
            addToWatchedButtonTitleView.setText(getString(R.string.add_to_watched_button_title));
        }

        addToWatchedButton.setOnClickListener(v -> {
            notesView.clearFocus();

            handleWatchedButtonOnClick();
        });
    }

    private void updateWatchedButton(){
        Animation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(TOAST_ANIMATION_DURATION);
        anim.setRepeatCount(1);
        anim.setRepeatMode(Animation.REVERSE);

        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                if (database.animeWatchedDAO().countByMALId(currentInfo.getMalid()) == 1) {
                    addToWatchedButton.setImageResource(R.drawable.ic_heart_filled_white);

                    addToWatchedButtonTitleView.setTextColor(Color.parseColor("#FFFFFF"));
                    addToWatchedButtonTitleView.setText(getString(R.string.remove_from_watched_button_title));
                } else {
                    addToWatchedButton.setImageResource(R.drawable.ic_heart_white);

                    addToWatchedButtonTitleView.setTextColor(Color.parseColor("#909090"));
                    addToWatchedButtonTitleView.setText(getString(R.string.add_to_watched_button_title));
                }
            }
        });

        addToWatchedButton.startAnimation(anim);
    }

    private void handleWatchedButtonOnClick(){
        if (database.animeWatchedDAO().countByMALId(currentInfo.getMalid()) == 1) {
            AnimeWatchedEntity record=database.animeWatchedDAO().getAllByMALId(currentInfo.getMalid()).get(0);

            database.animeWatchedDAO().delete(record);

            showOrHideEpisodesSection(false);

            toastHandler.postDelayed(() -> showToastMessage(String.format(getString(R.string.removed_from_watched_toast_message), currentInfo.getTitle())), TOAST_ANIMATION_DURATION);
        } else {
            long now=new Date().getTime();

            AnimeWatchedEntity record = new AnimeWatchedEntity(
                    currentInfo.getTitle(),
                    currentInfo.getSecondTitle(),
                    currentInfo.getSummary(),
                    currentInfo.getThumbnailUrl(),
                    currentInfo.getMalid(),
                    now,
                    now,
                    currentInfo.getEpisodesCount(),
                    currentInfo.getType(),
                    currentInfo.getStatus(),
                    currentInfo.getAired(),
                    currentInfo.getProducers(),
                    currentInfo.getStudios(),
                    currentInfo.getSourceMaterial(),
                    currentInfo.getDuration(),
                    currentInfo.getGenres(),
                    currentInfo.getTags(),
                    currentInfo.getAvailableAt(),
                    currentInfo.getRelations(),
                    currentInfo.getOpeningThemes(),
                    currentInfo.getEndingThemes(),
                    "0",
                    currentInfo.getPrequelMalid()
            );

            database.animeWatchedDAO().insert(record);

            showOrHideEpisodesSection(true);

            toastHandler.postDelayed(() -> showToastMessage(String.format(getString(R.string.added_to_watched_toast_message), currentInfo.getTitle())), TOAST_ANIMATION_DURATION);
        }

        updateWatchedButton();

        updateMarkAsWatchedButton();
    }

    private void firstUpdateToWatchButton(){
        if (database.animeToWatchDAO().countByMALId(currentInfo.getMalid()) == 1) {
            addToWatchButton.setImageResource(R.drawable.ic_added_to_playlist);

            addToWatchButtonTitleView.setTextColor(Color.parseColor("#FFFFFF"));
            addToWatchButtonTitleView.setText(getString(R.string.remove_from_towatch_button_title));
        } else {
            addToWatchButton.setImageResource(R.drawable.ic_add_to_playlist_white);

            addToWatchButtonTitleView.setTextColor(Color.parseColor("#909090"));
            addToWatchButtonTitleView.setText(getString(R.string.add_to_towatch_button_title));
        }

        addToWatchButton.setOnClickListener(v -> {
            notesView.clearFocus();

            handleToWatchButtonOnClick();
        });
    }

    private void updateToWatchButton(){
        Animation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(TOAST_ANIMATION_DURATION);
        anim.setRepeatCount(1);
        anim.setRepeatMode(Animation.REVERSE);

        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                if (database.animeToWatchDAO().countByMALId(currentInfo.getMalid()) == 1) {
                    addToWatchButton.setImageResource(R.drawable.ic_added_to_playlist);

                    addToWatchButtonTitleView.setTextColor(Color.parseColor("#FFFFFF"));
                    addToWatchButtonTitleView.setText(getString(R.string.remove_from_towatch_button_title));
                } else {
                    addToWatchButton.setImageResource(R.drawable.ic_add_to_playlist_white);

                    addToWatchButtonTitleView.setTextColor(Color.parseColor("#909090"));
                    addToWatchButtonTitleView.setText(getString(R.string.add_to_towatch_button_title));
                }
            }
        });

        addToWatchButton.startAnimation(anim);
    }

    private void handleToWatchButtonOnClick(){
        if (database.animeToWatchDAO().countByMALId(currentInfo.getMalid()) == 1) {
            AnimeToWatchEntity record=database.animeToWatchDAO().getAllByMALId(currentInfo.getMalid()).get(0);

            database.animeToWatchDAO().delete(record);

            toastHandler.postDelayed(() -> showToastMessage(String.format(getString(R.string.removed_from_towatch_toast_message), currentInfo.getTitle())), TOAST_ANIMATION_DURATION);
        } else {
            AnimeToWatchEntity record = new AnimeToWatchEntity(
                    currentInfo.getTitle(),
                    currentInfo.getSecondTitle(),
                    currentInfo.getSummary(),
                    currentInfo.getThumbnailUrl(),
                    currentInfo.getMalid(),
                    new Date().getTime(),
                    currentInfo.getEpisodesCount(),
                    currentInfo.getType(),
                    currentInfo.getStatus(),
                    currentInfo.getAired(),
                    currentInfo.getProducers(),
                    currentInfo.getStudios(),
                    currentInfo.getSourceMaterial(),
                    currentInfo.getDuration(),
                    currentInfo.getGenres(),
                    currentInfo.getTags(),
                    currentInfo.getAvailableAt(),
                    currentInfo.getRelations(),
                    currentInfo.getOpeningThemes(),
                    currentInfo.getEndingThemes()
            );

            database.animeToWatchDAO().insert(record);

            toastHandler.postDelayed(() -> showToastMessage(String.format(getString(R.string.added_to_towatch_toast_message), currentInfo.getTitle())), TOAST_ANIMATION_DURATION);
        }

        updateToWatchButton();
    }

    private void firstUpdateMarkAsWatchedButton(){
        updateMarkAsWatchedButton();

        markAsFinishedButton.setOnClickListener(v -> {
            notesView.clearFocus();

            handleMarkAsFinishedButtonClick();
        });
    }

    private void updateMarkAsWatchedButton(){
        if (database.animeWatchedDAO().countByMALId(malid)==0) {
            return;
        }

        Animation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(TOAST_ANIMATION_DURATION);
        anim.setRepeatCount(1);
        anim.setRepeatMode(Animation.REVERSE);

        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                AnimeWatchedEntity record=database.animeWatchedDAO().getAllByMALId(malid).get(0);

                if (record.epcount==Long.parseLong(record.watched_episodes)){
                    markAsFinishedButton.setText(getString(R.string.finished_status));
                    markAsFinishedButton.setClickable(false);
                } else {
                    markAsFinishedButton.setText(getString(R.string.mark_as_finished_button_title));
                    markAsFinishedButton.setClickable(true);
                }
            }
        });

        markAsFinishedButton.startAnimation(anim);
    }

    private void handleMarkAsFinishedButtonClick() {
        if (database.animeWatchedDAO().countByMALId(malid) == 0) {
            return;
        }

        //just in case - try and catch
        try {
            AnimeWatchedEntity record = database.animeWatchedDAO().getAllByMALId(malid).get(0);

            SelectSpinner episodesSpinner = rootView.findViewById(R.id.episodes_spinner);
            episodesSpinner.setSelection(record.epcount);

            database.animeWatchedDAO().updateWatchedEpisodes(currentInfo.getMalid(), String.valueOf(record.epcount), new Date().getTime());

            updateMarkAsWatchedButton();
        } catch (Exception e) {
            handleError(e);
        }
    }

    private void setupPlaylistsChips() {
        ArrayList<PlaylistEntity> playlists = new ArrayList<>();

        List<PlaylistStreamEntity> playlistStreamEntities = database.playlistStreamDAO().getAllByMALId(malid);
        for (PlaylistStreamEntity pse : playlistStreamEntities) {
            PlaylistEntity pe = database.playlistDAO().getById(pse.playlist_id);

            if (pe != null) {
                playlists.add(pe);
            }
        }

        if (playlists.size() == 0) {
            addedToPlaylistsResultsView.setVisibility(View.GONE);
        } else {
            PlaylistsChipsAdapter adapter = new PlaylistsChipsAdapter(playlists, new CustomOnItemClickListener() {
                @Override
                public void onClick(View v, int positiion) {
                    NavigationUtils.openPlaylistFragment(requireActivity(), playlists.get(positiion).id);
                }

                @Override
                public void onClick2(View v, int positiion) {
                    database.playlistStreamDAO().deleteByPlaylistIdAndMALId(playlists.get(positiion).id, malid);

                    setupPlaylistsChips();

                    showToastMessage(String.format(getString(R.string.removed_from_playlist_toast_message), currentInfo.getTitle(), playlists.get(positiion).name));
                }
            });
            addedToPlaylistsResultsView.setAdapter(adapter);

            addedToPlaylistsResultsView.setVisibility(View.VISIBLE);
        }
    }

    private void firstUpdateAddToPlaylistButton(){
        //let this fun to update playlists view
        //though, it would also be made in onHiddenChanged
        setupPlaylistsChips();

        if (currentInfo.getMalid() <= Constants.SMALLEST_LOCAL_MALID) {
            addToPlaylistButton.setVisibility(View.GONE);

            return;
        }

        addToPlaylistButton.setText(getString(R.string.add_to_playlist_button_title));

        addToPlaylistButton.setOnClickListener(v -> {
            notesView.clearFocus();

            handleAddToPlaylistButtonClick();
        });
    }

    private void handleAddToPlaylistButtonClick(){
        View dialogView=View.inflate(requireContext(), R.layout.add_to_playlist_dialog, null);
        RecyclerView resultsView=dialogView.findViewById(R.id.results_view);

        LinearLayoutManager layoutManager=new LinearLayoutManager(requireContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        resultsView.setLayoutManager(layoutManager);

        AlertDialog dialog=new AlertDialog.Builder(requireContext(), R.style.DarkDialogTheme)
                .setView(dialogView)
                .setCancelable(true)
                .setOnDismissListener(dialog1 -> setupPlaylistsChips())
                .create();

        List<PlaylistEntity> playlists=database.playlistDAO().getAll();

        PlaylistsAdapter adapter = new PlaylistsAdapter(requireContext(), playlists, new CustomOnItemClickListener(){
            @Override
            public void onClick(View v, int position){
                PlaylistEntity item=((PlaylistsAdapter) resultsView.getAdapter()).getItem(position);

                if (database.playlistStreamDAO().countByMALId(item.id, currentInfo.getMalid()) > 0){
                    showToastMessage(String.format(getString(R.string.already_added_to_playlist_toast_message), currentInfo.getTitle(), item.name));

                    return;
                }

                PlaylistStreamEntity record=new PlaylistStreamEntity(currentInfo.getTitle(),
                        currentInfo.getSecondTitle(),
                        currentInfo.getSummary(),
                        currentInfo.getThumbnailUrl(),
                        currentInfo.getMalid(),
                        currentInfo.getEpisodesCount(),
                        currentInfo.getType(),
                        currentInfo.getStatus(),
                        currentInfo.getAired(),
                        currentInfo.getProducers(),
                        currentInfo.getStudios(),
                        currentInfo.getSourceMaterial(),
                        currentInfo.getDuration(),
                        currentInfo.getGenres(),
                        currentInfo.getTags(),
                        currentInfo.getAvailableAt(),
                        currentInfo.getRelations(),
                        currentInfo.getOpeningThemes(),
                        currentInfo.getEndingThemes(),
                        item.id,
                        database.playlistStreamDAO().countByPlaylistId(item.id)
                );
                database.playlistStreamDAO().insert(record);

                showToastMessage(String.format(getString(R.string.added_to_playlist_toast_message), currentInfo.getTitle(), item.name));

                dialog.dismiss();
            }
        });
        resultsView.setAdapter(adapter);

        dialog.show();
    }

    private void loadLocalInfo() {
        try {
            isLoading.set(true);

            updateLoading();

            if (type == InfoSourceType.TO_WATCH) {
                List<AnimeToWatchEntity> records = database.animeToWatchDAO().getAllByMALId(malid);

                isLoading.set(false);

                //supposedly, unreachable state
                if (records.size() == 0) {
                    handleError(new Exception("No records with malid=" + malid));
                    return;
                }

                AnimeToWatchEntity record = records.get(0);

                AnimeInfo info = new AnimeInfo();
                info.setLocal(true);
                info.setTitle(record.title);
                info.setSecondTitle(record.second_title);
                info.setSummary(record.summary);
                info.setThumbnailUrl(record.thumbnail_url);
                info.setMalid(record.malid);
                info.setEpisodesCount(record.epcount);
                info.setType(record.type);
                info.setStatus(record.status);
                info.setAired(record.aired);
                info.setProducers(record.producers);
                info.setStudios(record.studios);
                info.setSourceMaterial(record.source_material);
                info.setDuration(record.duration);
                info.setGenres(record.genres);
                info.setTags(record.tags);
                info.setAvailableAt(record.available_at);
                info.setRelations(record.relations);
                info.setOpeningThemes(record.opening_themes);
                info.setEndingThemes(record.ending_themes);

                updateLoading();

                handleResult(info);
            } else if (type == InfoSourceType.WATCHED) {
                List<AnimeWatchedEntity> records = database.animeWatchedDAO().getAllByMALId(malid);

                isLoading.set(false);

                //supposedly, unreachable state
                if (records.size() == 0) {
                    handleError(new Exception("No records with malid=" + malid));
                    return;
                }

                AnimeWatchedEntity record = records.get(0);

                AnimeInfo info = new AnimeInfo();
                info.setLocal(true);
                info.setTitle(record.title);
                info.setSecondTitle(record.second_title);
                info.setSummary(record.summary);
                info.setThumbnailUrl(record.thumbnail_url);
                info.setMalid(record.malid);
                info.setEpisodesCount(record.epcount);
                info.setType(record.type);
                info.setStatus(record.status);
                info.setAired(record.aired);
                info.setProducers(record.producers);
                info.setStudios(record.studios);
                info.setSourceMaterial(record.source_material);
                info.setDuration(record.duration);
                info.setGenres(record.genres);
                info.setTags(record.tags);
                info.setPrequelMalid(record.prequel_malid);
                info.setAvailableAt(record.available_at);
                info.setRelations(record.relations);
                info.setOpeningThemes(record.opening_themes);
                info.setEndingThemes(record.ending_themes);

                updateLoading();

                handleResult(info);
            } else if (type == InfoSourceType.PLAYLIST){
                List<PlaylistStreamEntity> records=database.playlistStreamDAO().getAllByMALId(malid);

                isLoading.set(false);

                //supposedly, unreachable state
                if (records.size() == 0) {
                    handleError(new Exception("No records with malid=" + malid));
                    return;
                }

                PlaylistStreamEntity record=records.get(0);

                AnimeInfo info=new AnimeInfo();
                info.setLocal(true);
                info.setTitle(record.title);
                info.setSecondTitle(record.second_title);
                info.setSummary(record.summary);
                info.setThumbnailUrl(record.thumbnail_url);
                info.setMalid(record.malid);
                info.setEpisodesCount(record.epcount);
                info.setType(record.type);
                info.setStatus(record.status);
                info.setAired(record.aired);
                info.setProducers(record.producers);
                info.setStudios(record.studios);
                info.setSourceMaterial(record.source_material);
                info.setDuration(record.duration);
                info.setGenres(record.genres);
                info.setTags(record.tags);
                info.setAvailableAt(record.available_at);
                info.setRelations(record.relations);
                info.setOpeningThemes(record.opening_themes);
                info.setEndingThemes(record.ending_themes);

                updateLoading();

                handleResult(info);
            }

            if (defPrefs.getBoolean(Constants.ALWAYS_UPDATE_ITEM_INFO, false) && malid > Constants.SMALLEST_LOCAL_MALID){
                refreshInfo(malid);
            }
        } catch (Exception e){
            isLoading.set(false);

            handleError(e);
        }
    }

    private void setBasicInfo() {
        Utils.resizeImageView(thumbnailView, Constants.MAIN_THUMBNAIL_DUMMY_BITMAP_WIDTH, Constants.MAIN_THUMBNAIL_DUMMY_BITMAP_HEIGHT);

        ImageLoaderWrapper.loadImageWithPlaceholder(currentInfo.getThumbnailUrl(), new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                if (bitmap.getWidth()>bitmap.getHeight()){
                    Utils.resizeImageView(thumbnailView, Constants.MAIN_THUMBNAIL_DUMMY_BITMAP_HEIGHT, Constants.MAIN_THUMBNAIL_DUMMY_BITMAP_WIDTH);
                }

                thumbnailView.setImageBitmap(bitmap);

                TextView coverTipView = rootView.findViewById(R.id.cover_poster_tip_view);
                if (defPrefs.getBoolean(Constants.SHOW_COVER_TIP, true)) {
                    coverTipView.setWidth((bitmap.getWidth() * 2) + 100);
                    coverTipView.setVisibility(View.VISIBLE);
                    tipHolder.postDelayed(() -> coverTipView.setVisibility(View.GONE), 2000L);
                } else {
                    coverTipView.setVisibility(View.GONE);
                }

                if (defPrefs.getBoolean(Constants.USE_GRADIENT_IN_POST, true)) {
                    Palette p = Palette.from(bitmap).generate();

                    try {
                        //VibrantSwatch could be wrong sometimes
                        //noticed on Konosuba
                        //technically, it could be improved, but even this way it is good because it is kinda normal practice
                        int[] colors = {p.getDarkMutedSwatch().getRgb(), Color.parseColor("#171717")};
                        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
                        gd.setCornerRadius(0f);
                        gd.setAlpha(255 - 65);
                        rootView.setBackground(gd);
                    } catch (Exception e) {
                        //do nothing
                    }
                }
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                thumbnailView.setImageDrawable(errorDrawable);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                thumbnailView.setImageDrawable(placeHolderDrawable);
            }
        }, () -> {
            thumbnailView.setImageResource(R.drawable.dummy_no_thumbnail);
        });

        titleView.setText(currentInfo.getTitle());

        if (currentInfo.getSecondTitle().isEmpty()) {
            secondTitleView.setVisibility(View.INVISIBLE); //not GONE to preserve space
        } else {
            secondTitleView.setVisibility(View.VISIBLE);

            secondTitleView.setText(currentInfo.getSecondTitle());
        }

        String sum = (currentInfo.getSummary().isEmpty() || currentInfo.getSummary().startsWith("No synopsis information has been added to this title."))
                ? getString(R.string.summary_not_presented)
                : Utils.formatSummary(Utils.unescapeStr(currentInfo.getSummary()));

        if (sum.length() > 400) {
            SpannableString shortedSummary = new SpannableString(Utils.shortenSummary(sum, 400));
            int len = shortedSummary.length();
            shortedSummary.setSpan(new StyleSpan(Typeface.BOLD), len - 3, len, 0);

            summaryView.setText(shortedSummary);
            summaryView.setOnClickListener(v -> {
                notesView.clearFocus();

                summaryView.setText(sum);
            });
        } else {
            summaryView.setText(sum);
        }

        TextView recommendationsBtn = rootView.findViewById(R.id.recommendations_btn);
        if (recommendationsBtn != null) {
            String title=currentInfo.getTitle();
            if (defPrefs.getBoolean(Constants.SHOW_ENGLISH_TITLES, false) && !currentInfo.getSecondTitle().isEmpty()){
                title=currentInfo.getSecondTitle();
            }

            recommendationsBtn.setText(String.format(getString(R.string.recommendations_button_title), title).toUpperCase());
        }
    }

    private void setDetailedInfo() {
        String type = currentInfo.getType();
        String status = currentInfo.getStatus();
        String aired = currentInfo.getAired();
        String duration = currentInfo.getDuration();
        String genres = currentInfo.getGenres();
        String tags = currentInfo.getTags();
        String availableAt = currentInfo.getAvailableAt();
        String relations = currentInfo.getRelations();
        String openingThemes = currentInfo.getOpeningThemes();
        String endingThemes = currentInfo.getEndingThemes();
        String sourceMaterial=currentInfo.getSourceMaterial();

        if (type.isEmpty()) type = getString(R.string.unknown_placeholder);
        if (status.isEmpty()) status = getString(R.string.unknown_placeholder);
        if (aired.isEmpty()) aired = getString(R.string.unknown_placeholder);
        if (duration.isEmpty()) duration = getString(R.string.unknown_placeholder);

        typeView.setText(type);
        episodesCountView.setText(currentInfo.getEpisodesCount() == 0 ? "?" : String.valueOf(currentInfo.getEpisodesCount()));
        statusView.setText(status);
        airedView.setText(aired);
        durationView.setText(duration);

        String producers = currentInfo.getProducers();
        if (producers.isEmpty() || Utils.containsIgnoreCase(producers, "None found")) {
            producersView.setText(getString(R.string.unknown_placeholder));
        } else {
            if (!producers.contains("|")) {
                producers = producers.replaceAll(", ", ",\n");
                producersView.setText(producers);
            } else {
                String str = "";

                String[] splits1 = producers.split(";");

                for (String split1 : splits1) {
                    String name = split1.substring(0, split1.indexOf("|"));

                    str += name;
                    str += ",\n";
                }

                str = str.substring(0, str.length() - 2);

                SpannableString ss = new SpannableString(str);

                int pos = 0;

                for (String split1 : splits1) {
                    String name = split1.substring(0, split1.indexOf("|"));
                    long cmalid = Long.parseLong(split1.substring(split1.indexOf("|") + 1));

                    ClickableSpan clickableSpan = new ClickableSpan() {
                        @Override
                        public void onClick(View textView) {
                            NavigationUtils.openCompanyFragment(requireActivity(), cmalid);
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            super.updateDrawState(ds);

                            ds.setUnderlineText(true);
                        }
                    };
                    ss.setSpan(clickableSpan, pos, pos + name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    pos += (name.length() + 2);
                }

                producersView.setMovementMethod(LinkMovementMethod.getInstance());
                producersView.setText(ss);
            }
        }

        String studios = currentInfo.getStudios();
        if (studios.isEmpty() || Utils.containsIgnoreCase(studios, "None found")) {
            studiosView.setText(getString(R.string.unknown_placeholder));
        } else {
            if (!studios.contains("|")) {
                studios = studios.replaceAll(", ", ",\n");
                studiosView.setText(studios);
            } else {
                String str = "";

                String[] splits1 = studios.split(";");

                for (String split1 : splits1) {
                    String name = split1.substring(0, split1.indexOf("|"));

                    str += name;
                    str += ",\n";
                }

                str = str.substring(0, str.length() - 2);

                SpannableString ss = new SpannableString(str);

                int pos = 0;

                for (String split1 : splits1) {
                    String name = split1.substring(0, split1.indexOf("|"));
                    long cmalid = Long.parseLong(split1.substring(split1.indexOf("|") + 1));

                    ClickableSpan clickableSpan = new ClickableSpan() {
                        @Override
                        public void onClick(View textView) {
                            NavigationUtils.openCompanyFragment(requireActivity(), cmalid);
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            super.updateDrawState(ds);

                            ds.setUnderlineText(true);
                        }
                    };
                    ss.setSpan(clickableSpan, pos, pos + name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    pos += (name.length() + 2);
                }

                studiosView.setMovementMethod(LinkMovementMethod.getInstance());
                studiosView.setText(ss);
            }
        }

        if (genres.isEmpty() && tags.isEmpty()) {
            tagsView.setVisibility(View.GONE);
        } else {
            ArrayList<String> items = new ArrayList<>();

            if (!genres.isEmpty()) {
                items.addAll(Arrays.asList(genres.split(";")));
            }

            if (!tags.isEmpty()) {
                items.addAll(Arrays.asList(tags.split(";")));
            }

            GenresAdapter genresAdapter = new GenresAdapter(items);
            tagsView.setAdapter(genresAdapter);

            tagsView.setVisibility(View.VISIBLE);
        }

        availableAtLayout.removeAllViewsInLayout();

        View availableAtTitleLayout=rootView.findViewById(R.id.available_at_title_layout);

        if (availableAt.isEmpty()) {
            availableAtTitleLayout.setVisibility(View.GONE);
        } else {
            JsonArray availableAtArray = null;
            try {
                availableAtArray = JsonParser.array().from(availableAt);
            } catch (Exception e) {}

            if (availableAtArray != null && !availableAtArray.isEmpty()) {
                for (int i = 0; i < availableAtArray.size(); i++) {
                    JsonObject obj = availableAtArray.getObject(i);

                    final String title=obj.getString("title");
                    final String url=obj.getString("url");

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.gravity = Gravity.LEFT;
                    params.topMargin = 25;

                    TextView spBtn = new TextView(requireContext());
                    spBtn.setLayoutParams(params);
                    spBtn.setBackgroundResource(R.drawable.bordered_button);
                    spBtn.setPadding(15, 15, 15, 15);
                    spBtn.setText(title);
                    spBtn.setTextSize(17.0f);
                    spBtn.setTextColor(Color.parseColor("#FFFFFF"));

                    spBtn.setOnClickListener(v ->
                            Utils.copyToClipboard(requireContext(), title, url)
                    );

                    availableAtLayout.addView(spBtn);
                }

                availableAtTitleLayout.setVisibility(View.VISIBLE);
            } else {
                availableAtTitleLayout.setVisibility(View.GONE);
            }
        }

        relationsLayout.removeAllViewsInLayout();

        View relationsTitle=rootView.findViewById(R.id.relations_title);

        if (relations.isEmpty()) {
            relationsTitle.setVisibility(View.GONE);
            relationsLayout.setVisibility(View.GONE);
        } else {
            JsonArray relationsArray = null;
            try {
                relationsArray = JsonParser.array().from(relations);
            } catch (Exception e) {
            }

            if (relationsArray != null && !relationsArray.isEmpty()) {
                for (int i = 0; i < relationsArray.size(); i++) {
                    JsonObject obj = relationsArray.getObject(i);

                    final String st=obj.getString("status");
                    final String title=obj.getString("title");
                    long mid = obj.getLong("malid");

                    InfoSourceType sourceType = InfoSourceType.REMOTE;
                    if (database.animeWatchedDAO().countByMALId(mid) == 1) {
                        sourceType = InfoSourceType.WATCHED;
                    } else if (database.animeToWatchDAO().countByMALId(mid) == 1) {
                        sourceType = InfoSourceType.TO_WATCH;
                    }
                    final InfoSourceType finalSourceType = sourceType;

                    SpannableString ss = new SpannableString(String.format("%s: %s", st, title));
                    ss.setSpan(new StyleSpan(Typeface.BOLD), 0, st.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    ss.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View textView) {
                            NavigationUtils.openAnimeFragment(requireActivity(), mid, finalSourceType);
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            super.updateDrawState(ds);

                            ds.setUnderlineText(true);
                        }
                    }, st.length() + 2, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    if (i!=(relationsArray.size()-1)) {
                        params.bottomMargin = 25;
                    }

                    TextView tv = new TextView(requireContext());
                    tv.setLayoutParams(params);
                    tv.setText(ss);
                    tv.setTextColor(Color.parseColor("#FFFFFF"));
                    tv.setMovementMethod(LinkMovementMethod.getInstance());
                    relationsLayout.addView(tv);
                }

                rootView.findViewById(R.id.relations_title).setVisibility(View.VISIBLE);
                relationsLayout.setVisibility(View.VISIBLE);
            } else {
                relationsTitle.setVisibility(View.GONE);
                relationsLayout.setVisibility(View.GONE);
            }
        }

        openingThemesLayout.removeAllViewsInLayout();

        View opThemesTitleLayout=rootView.findViewById(R.id.opening_themes_title_layout);

        if (openingThemes.isEmpty()) {
            opThemesTitleLayout.setVisibility(View.GONE);
        } else {
            JsonArray opThemesArray = null;
            try {
                opThemesArray = JsonParser.array().from(openingThemes);
            } catch (Exception e) {}

            if (opThemesArray != null && !opThemesArray.isEmpty()) {
                for (int i = 0; i < opThemesArray.size(); i++) {
                    JsonObject obj = opThemesArray.getObject(i);

                    final String title = obj.getString("title");
                    final String artist = obj.getString("artist");
                    final String spotifyUrl = obj.getString("spotify_url");
                    final String episodes=obj.getString("episodes");

                    LinearLayout optLayout = new LinearLayout(requireContext());
                    optLayout.setOrientation(LinearLayout.HORIZONTAL);

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.gravity = Gravity.LEFT;
                    layoutParams.topMargin = 25;

                    optLayout.setLayoutParams(layoutParams);

                    if (!spotifyUrl.isEmpty()) {
                        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(Utils.dpToPx(requireContext(), 20), Utils.dpToPx(requireContext(), 20));
                        iconParams.gravity = Gravity.CENTER_VERTICAL;

                        ImageView optIcon = new ImageView(requireContext());
                        optIcon.setLayoutParams(iconParams);
                        optIcon.setImageResource(R.drawable.spotify_icon);

                        optLayout.addView(optIcon);
                    }

                    LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    btnParams.gravity = Gravity.CENTER_VERTICAL;
                    btnParams.leftMargin = Utils.dpToPx(requireContext(), 10);

                    TextView optBtn = new TextView(requireContext());
                    optBtn.setLayoutParams(btnParams);
                    if (artist.trim().isEmpty()) {
                        optBtn.setText(title);
                    } else {
                        optBtn.setText(String.format("%s - %s", title, artist));
                    }
                    if (episodes!=null && !episodes.isEmpty()){
                        optBtn.setText(optBtn.getText()+String.format(" [%s]", episodes));
                    }
                    optBtn.setTextSize(17.0f);
                    optBtn.setTextColor(Color.parseColor("#FFFFFF"));

                    optBtn.setOnClickListener(v -> {
                        Utils.animateClickOnItem(optLayout, () -> {
                            boolean spotifyInstalled = Utils.isSpotifyAppInstalled(requireActivity());

                            int l = 1;
                            if (!spotifyUrl.isEmpty()) {
                                l += 1;

                                if (spotifyInstalled) {
                                    l += 1;
                                }
                            }

                            String[] items = new String[l];
                            items[0] = getString(R.string.song_dialog_item_copy_title);
                            if (!spotifyUrl.isEmpty()) {
                                items[1] = getString(R.string.song_dialog_item_copy_link);

                                if (spotifyInstalled) {
                                    items[2] = getString(R.string.song_dialog_item_open_in_spotify);
                                }
                            }

                            new AlertDialog.Builder(requireContext(), R.style.DarkDialogTheme)
                                    .setItems(items, (dialog, which) -> {
                                        if (which == 0) {
                                            Utils.copyToClipboard(requireContext(), "", artist+" "+title);
                                        } else if (which == 1) {
                                            Utils.copyToClipboard(requireContext(), "", spotifyUrl);
                                        } else if (which == 2) {
                                            if (!Utils.openUrlInSpotifyApp(requireContext(), spotifyUrl)) {
                                                showToastMessage(getString(R.string.error_happened));
                                            }
                                        }
                                    })
                                    .setCancelable(true)
                                    .create()
                                    .show();
                        });
                    });

                    optLayout.addView(optBtn);

                    openingThemesLayout.addView(optLayout);
                }

                rootView.findViewById(R.id.opening_themes_title_layout).setVisibility(View.VISIBLE);
            } else {
                opThemesTitleLayout.setVisibility(View.GONE);
            }
        }

        endingThemesLayout.removeAllViewsInLayout();

        View edThemesTitleLayout=rootView.findViewById(R.id.ending_themes_title_layout);

        if (endingThemes.isEmpty()) {
            edThemesTitleLayout.setVisibility(View.GONE);
        } else {
            JsonArray edThemesArray = null;
            try {
                edThemesArray = JsonParser.array().from(endingThemes);
            } catch (Exception e) {}

            if (edThemesArray != null && !edThemesArray.isEmpty()) {
                for (int i = 0; i < edThemesArray.size(); i++) {
                    JsonObject obj = edThemesArray.getObject(i);

                    final String title = obj.getString("title");
                    final String artist = obj.getString("artist");
                    final String spotifyUrl = obj.getString("spotify_url");
                    final String episodes=obj.getString("episodes");

                    LinearLayout optLayout = new LinearLayout(requireContext());
                    optLayout.setOrientation(LinearLayout.HORIZONTAL);

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.gravity = Gravity.LEFT;
                    layoutParams.topMargin = 25;

                    optLayout.setLayoutParams(layoutParams);

                    if (!spotifyUrl.isEmpty()) {
                        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(Utils.dpToPx(requireContext(), 20), Utils.dpToPx(requireContext(), 20));
                        iconParams.gravity = Gravity.CENTER_VERTICAL;

                        ImageView optIcon = new ImageView(requireContext());
                        optIcon.setLayoutParams(iconParams);
                        optIcon.setImageResource(R.drawable.spotify_icon);

                        optLayout.addView(optIcon);
                    }

                    LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    btnParams.gravity = Gravity.CENTER_VERTICAL;
                    btnParams.leftMargin = Utils.dpToPx(requireContext(), 10);

                    TextView optBtn = new TextView(requireContext());
                    optBtn.setLayoutParams(btnParams);
                    if (artist.trim().isEmpty()) {
                        optBtn.setText(title);
                    } else {
                        optBtn.setText(String.format("%s - %s", title, artist));
                    }
                    if (episodes!=null && !episodes.isEmpty()){
                        optBtn.setText(optBtn.getText()+String.format(" [%s]", episodes));
                    }
                    optBtn.setTextSize(17.0f);
                    optBtn.setTextColor(Color.parseColor("#FFFFFF"));

                    optBtn.setOnClickListener(v -> {
                        Utils.animateClickOnItem(optLayout, () -> {
                            boolean spotifyInstalled = Utils.isSpotifyAppInstalled(requireActivity());

                            int l = 1;
                            if (!spotifyUrl.isEmpty()) {
                                l += 1;

                                if (spotifyInstalled) {
                                    l += 1;
                                }
                            }

                            String[] items = new String[l];
                            items[0] = getString(R.string.song_dialog_item_copy_title);
                            if (!spotifyUrl.isEmpty()) {
                                items[1] = getString(R.string.song_dialog_item_copy_link);

                                if (spotifyInstalled) {
                                    items[2] = getString(R.string.song_dialog_item_open_in_spotify);
                                }
                            }

                            new AlertDialog.Builder(requireContext(), R.style.DarkDialogTheme)
                                    .setItems(items, (dialog, which) -> {
                                        if (which == 0) {
                                            Utils.copyToClipboard(requireContext(), "", artist+" "+title);
                                        } else if (which == 1) {
                                            Utils.copyToClipboard(requireContext(), "", spotifyUrl);
                                        } else if (which == 2) {
                                            if (!Utils.openUrlInSpotifyApp(requireContext(), spotifyUrl)) {
                                                showToastMessage(getString(R.string.error_happened));
                                            }
                                        }
                                    })
                                    .setCancelable(true)
                                    .create()
                                    .show();
                        });
                    });

                    optLayout.addView(optBtn);

                    endingThemesLayout.addView(optLayout);
                }

                rootView.findViewById(R.id.ending_themes_title_layout).setVisibility(View.VISIBLE);
            } else {
                edThemesTitleLayout.setVisibility(View.GONE);
            }
        }

        TextView searchMangaBtn=rootView.findViewById(R.id.search_manga_btn);

        JsonObject sourceMaterialObj=null;
        try {
            sourceMaterialObj=JsonParser.object().from(sourceMaterial);
        } catch (Exception e){}

        if (sourceMaterialObj==null || !Utils.containsIgnoreCase(sourceMaterialObj.getString("type"), "manga")){
            searchMangaBtn.setVisibility(View.GONE);
        } else {
            searchMangaBtn.setText(String.format(getString(R.string.search_manga_button_title), getString(R.string.mangadex)).toUpperCase());

            searchMangaBtn.setVisibility(View.VISIBLE);
        }

    }

    private void loadNote(AnimeInfo info) {
        try {
            List<NoteEntity> records = database.noteDAO().getAllByMALId(info.getMalid());

            if (records.size()==0) {
                return;
            }

            NoteEntity record = records.get(0);

            handleNoteResult(record.note);
            handleRatingResult(record.rating);
        } catch (Exception e) {
            handleError(e);
        }
    }

    private void handleNoteResult(String noteText){
        notesView.setText(noteText);
    }

    private void handleRatingResult(float rating){
        ratingBar.setRating(rating);
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

    private void showOrHideEpisodesSection(boolean show){
        View episodesTitle=rootView.findViewById(R.id.episodes_title);
        episodesTitle.setVisibility(show ? View.VISIBLE : View.GONE);

        SelectSpinner episodesSpinner=rootView.findViewById(R.id.episodes_spinner);
        episodesSpinner.setVisibility(show ? View.VISIBLE : View.GONE);

        if (!show){
            episodesSpinner.setSelection(0);
        }

        markAsFinishedButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showOrHideRatingBar(boolean show) {
        View ratingTitle = rootView.findViewById(R.id.rating_title);
        ratingTitle.setVisibility(show ? View.VISIBLE : View.GONE);

        View ratingBar = rootView.findViewById(R.id.rating_bar);
        ratingBar.setVisibility(show ? View.VISIBLE : View.GONE);
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

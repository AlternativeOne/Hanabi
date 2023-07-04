package com.lexoff.animediary.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lexoff.animediary.Database.ADatabase;
import com.lexoff.animediary.Database.AnimeWatchedEntity;
import com.lexoff.animediary.Database.AppDatabase;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Utils;

import java.util.List;

public class StatisticsFragment extends BaseFragment {

    private AppDatabase database;

    private TextView uniqueTotalWatchedView, totalWatchedView, uniqueTotalFinishedView, totalFinishedView, episodesWatchedView, timeSpentView;

    public StatisticsFragment() {
        //empty
    }

    public static StatisticsFragment newInstance() {
        StatisticsFragment fragment = new StatisticsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database=ADatabase.getInstance(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
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

        ImageView backBtn = rootView.findViewById(R.id.ab_back_btn);
        backBtn.setOnClickListener(v -> {
            Utils.animateClickOnImageButton(v, () -> {
                requireActivity().onBackPressed();
            });
        });

        uniqueTotalWatchedView=rootView.findViewById(R.id.unique_total_watched_textview);
        totalWatchedView=rootView.findViewById(R.id.total_watched_textview);
        uniqueTotalFinishedView=rootView.findViewById(R.id.unique_total_finished_textview);
        totalFinishedView=rootView.findViewById(R.id.total_finished_textview);
        episodesWatchedView=rootView.findViewById(R.id.total_episodes_textview);
        timeSpentView=rootView.findViewById(R.id.total_time_watched_textview);

        loadInfo();
    }

    @Override
    public void onResume(){
        super.onResume();

        loadInfo();
    }

    private void loadInfo(){
        List<AnimeWatchedEntity> records=database.animeWatchedDAO().getAll();

        int uniqueTotalWatched=0, totalWatched=0, uniqueTotalFinished=0, totalFinished=0, episodesWatched=0, timeSpent=0;
        for (AnimeWatchedEntity record : records){
            int curEpisodesWatched=Integer.parseInt(record.watched_episodes);

            episodesWatched+=curEpisodesWatched;

            int durInt=0;

            try {
                String dur = record.duration.trim();
                dur = dur.replace(" per ep.", "");
                if (dur.contains("hr.")) {
                    int a=dur.indexOf("hr.")+4;

                    String hr=dur.substring(0, a).replace("hr.", "").trim();
                    String min=dur.substring(a).replace("min.", "").trim();
                    durInt=(Integer.parseInt(hr)*60*60)+(Integer.parseInt(min)*60);
                } else if (dur.contains("min.")) {
                    durInt=Integer.parseInt(dur.replace("min.", "").trim())*60;
                } else if (dur.contains("sec.")){
                    durInt=Integer.parseInt(dur.replace("sec.", "").trim());
                }
            } catch (Exception e){
                //do nothing
            }

            timeSpent+=Math.round((curEpisodesWatched*durInt)/60);

            //count only unique titles
            if (database.animeWatchedDAO().countByMALId(record.prequel_malid)==0) {
                uniqueTotalWatched++;

                if (curEpisodesWatched==record.epcount){
                    uniqueTotalFinished++;
                }
            }

            totalWatched++;

            if (curEpisodesWatched==record.epcount){
                totalFinished++;
            }
        }

        uniqueTotalWatchedView.setText(String.valueOf(uniqueTotalWatched));
        totalWatchedView.setText(String.valueOf(totalWatched));
        uniqueTotalFinishedView.setText(String.valueOf(uniqueTotalFinished));
        totalFinishedView.setText(String.valueOf(totalFinished));
        episodesWatchedView.setText(String.valueOf(episodesWatched));

        int hTimeSpentFullHours=Math.round(timeSpent/60);
        int hTimeSpentLeftMinutes=timeSpent%60;

        int dTimeSpentFullDays=Math.round(timeSpent/60/24);
        int dTimeTemp=timeSpent-(dTimeSpentFullDays*60*24);
        int dTimeSpentFullHours=Math.round(dTimeTemp/60);
        int dTimeSpentLeftMinutes=dTimeTemp%60;

        timeSpentView.setText(String.format(getString(R.string.stats_time_spent), timeSpent, hTimeSpentFullHours, hTimeSpentLeftMinutes, dTimeSpentFullDays, dTimeSpentFullHours, dTimeSpentLeftMinutes));
    }

}

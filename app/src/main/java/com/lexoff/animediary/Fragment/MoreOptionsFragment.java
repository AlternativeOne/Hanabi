package com.lexoff.animediary.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lexoff.animediary.NavigationUtils;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Utils;

public class MoreOptionsFragment extends BaseFragment {

    public MoreOptionsFragment() {
        //empty
    }

    public static MoreOptionsFragment newInstance() {
        MoreOptionsFragment fragment = new MoreOptionsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_moreoptions, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        View openByUrlRow=rootView.findViewById(R.id.open_by_url_row);
        openByUrlRow.setOnClickListener(v -> {
            Utils.animateClickOnItem(v, () -> {
                Utils.showOpenByUrlDialog(requireActivity());
            });
        });

        View airingScheduleRow=rootView.findViewById(R.id.airing_schedule_row);
        airingScheduleRow.setOnClickListener(v -> {
            Utils.animateClickOnItem(v, () -> {
                NavigationUtils.openAiringScheduleFragment(requireActivity());
            });
        });

        View chartsRow=rootView.findViewById(R.id.charts_row);
        chartsRow.setOnClickListener(v -> {
            Utils.animateClickOnItem(v, () -> {
                NavigationUtils.openChartsFragment(requireActivity());
            });
        });

        View playlistsRow=rootView.findViewById(R.id.playlists_row);
        playlistsRow.setOnClickListener(v -> {
            Utils.animateClickOnItem(v, () -> {
                NavigationUtils.openPlaylistsFragment(requireActivity());
            });
        });

        View statsRow=rootView.findViewById(R.id.stats_row);
        statsRow.setOnClickListener(v -> {
            Utils.animateClickOnItem(v, () -> {
                NavigationUtils.openStatisticsFragment(requireActivity());
            });
        });

        View settingsRow=rootView.findViewById(R.id.settings_row);
        settingsRow.setOnClickListener(v -> {
            Utils.animateClickOnItem(v, () -> {
                NavigationUtils.openSettingsFragment(requireActivity());
            });
        });
    }

}

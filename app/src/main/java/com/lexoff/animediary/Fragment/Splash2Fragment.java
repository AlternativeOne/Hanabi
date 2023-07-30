package com.lexoff.animediary.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.lexoff.animediary.R;
import com.lexoff.animediary.Util.Utils;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class Splash2Fragment extends BaseFragment {
    private final long ANIMATION_DURATION=2000L;

    public Splash2Fragment() {
        //empty
    }

    public static Splash2Fragment newInstance() {
        Splash2Fragment fragment = new Splash2Fragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        ImageView logoView = rootView.findViewById(R.id.logo_view);

        ImageView additionalView=rootView.findViewById(R.id.additional_view);
        int navbarHeight= Utils.getNavBarHeight(requireContext());
        ConstraintLayout.LayoutParams params=(ConstraintLayout.LayoutParams) additionalView.getLayoutParams();
        params.bottomMargin=navbarHeight;
        additionalView.setLayoutParams(params);

        logoView.post(() -> {
            try {
                GifImageView bgView = rootView.findViewById(R.id.background_view);
                GifDrawable gif = new GifDrawable(getResources(), R.drawable.splash_fireworks_theathrical_version);
                bgView.setImageDrawable(gif);
            } catch (IOException ignored) {}

            logoView.animate()
                    .alpha(1.0f)
                    .setDuration(ANIMATION_DURATION); //compensate delay with longer duration //fireworks gif's length is approx. 2.5 secs

            additionalView.animate()
                    .alpha(1.0f)
                    .setDuration(ANIMATION_DURATION);
        });
    }
}

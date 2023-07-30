package com.lexoff.animediary.Fragment;

import android.animation.Animator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import com.lexoff.animediary.Constants;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Util.NavigationUtils;
import com.lexoff.animediary.Util.Utils;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import pl.droidsonroids.gif.AnimationListener;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class SplashFragment extends BaseFragment {

    private final long ANIMATION_DURATION=2000L;

    private AtomicBoolean shouldStartWithoutGif = new AtomicBoolean(false);

    public SplashFragment() {
        //empty
    }

    public static SplashFragment newInstance() {
        SplashFragment fragment = new SplashFragment();
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
        int navbarHeight=Utils.getNavBarHeight(requireContext());
        ConstraintLayout.LayoutParams params=(ConstraintLayout.LayoutParams) additionalView.getLayoutParams();
        params.bottomMargin=navbarHeight;
        additionalView.setLayoutParams(params);

        logoView.post(() -> {
            try {
                GifImageView bgView = rootView.findViewById(R.id.background_view);
                GifDrawable gif = new GifDrawable(getResources(), R.drawable.splash_fireworks_theathrical_version);
                gif.addAnimationListener(new AnimationListener() {
                    @Override
                    public void onAnimationCompleted(int loopNumber) {
                        //ignore loop count

                        if (PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(Constants.USE_PASSWORD_BLOCK, false)) {
                            NavigationUtils.openPINFragment(requireActivity(), 0, () -> NavigationUtils.openNavigationFragment(requireActivity()));
                        } else {
                            NavigationUtils.openNavigationFragment(requireActivity());
                        }
                    }
                });
                bgView.setImageDrawable(gif);
            } catch (IOException e) {
                shouldStartWithoutGif.set(true);
            }

            logoView.animate()
                    .alpha(1.0f)
                    .setDuration(ANIMATION_DURATION) //compensate delay with longer duration //fireworks gif's length is approx. 2.5 secs
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (shouldStartWithoutGif.get()) {
                                if (PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(Constants.USE_PASSWORD_BLOCK, false)) {
                                    NavigationUtils.openPINFragment(requireActivity(), 0, () -> NavigationUtils.openNavigationFragment(requireActivity()));
                                } else {
                                    NavigationUtils.openNavigationFragment(requireActivity());
                                }
                            }
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });

            additionalView.animate()
                    .alpha(1.0f)
                    .setDuration(ANIMATION_DURATION);
        });
    }

}

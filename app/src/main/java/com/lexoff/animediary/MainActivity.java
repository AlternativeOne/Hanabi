package com.lexoff.animediary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;
import com.lexoff.animediary.Enum.InfoSourceType;
import com.lexoff.animediary.Extractor.ExtractorUtils;
import com.lexoff.animediary.Fragment.BaseFragment;
import com.lexoff.animediary.Fragment.PINFragment;
import com.lexoff.animediary.Util.ApplicationUtils;
import com.lexoff.animediary.Util.NavigationUtils;
import com.lexoff.animediary.Util.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences.OnSharedPreferenceChangeListener prefsChangeListener
            = (sharedPreferences, key) -> {
        if (Constants.USE_SECURE_MODE_KEY.equals(key)) {
            changeSecureMode(sharedPreferences.getBoolean(key, false));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            SplashScreen.installSplashScreen(this)
                    .setOnExitAnimationListener(splashScreen -> {
                        Animation slideAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fragment_slide_lr_popexit);
                        slideAnimation.setStartOffset(0L);
                        slideAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                splashScreen.remove();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                        splashScreen.getView().startAnimation(slideAnimation);
                    });
        } else {
            setTheme(R.style.Theme_Base_Hanabi);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        changeSecureMode(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.USE_SECURE_MODE_KEY, false));

        Window window = getWindow();
        if (window != null) {
            //modern way to set fullscreen
            WindowCompat.setDecorFitsSystemWindows(window, false);

            int halfTransparentColor = Color.argb(75, 0, 0, 0);
            window.setStatusBarColor(halfTransparentColor);
            window.setNavigationBarColor(halfTransparentColor);
        }

        if (processIntent(getIntent())) return;

        openPINFragmentOrCallback(() -> NavigationUtils.openNavigationFragment(this));
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        processIntent(intent);
    }

    private boolean processIntent(Intent intent) {
        if (intent != null) {
            boolean isShortcutIntent=intent.getBooleanExtra(ApplicationUtils.IS_SHORTCUT_INTENT, false);
            if (isShortcutIntent) {
                String type = intent.getStringExtra(ApplicationUtils.SHORTCUT_INTENT_TYPE);
                    if (ApplicationUtils.SHORTCUT_SEARCH.equals(type)) {
                        openPINFragmentOrCallback(() -> NavigationUtils.openNavigationFragment(this, R.id.search_nav_item));

                        return true;
                    } else if (ApplicationUtils.SHORTCUT_CHARTS.equals(type)) {
                        openPINFragmentOrCallback(() -> {
                            NavigationUtils.openNavigationFragment(this, R.id.more_options_nav_item, () -> {
                                NavigationUtils.openChartsFragment(this);
                            });
                        });

                        return true;
                    }

                return true;
            }

            String scannerMALIdStr = intent.getStringExtra(Constants.SCANNER_INTENT_MAL_ID_EXTRA_NAME);
            if (scannerMALIdStr != null) {
                try {
                    final long scannerMALId = ExtractorUtils.tryToExtractMALidFromLink(scannerMALIdStr);

                    if (scannerMALId != Constants.NON_EXIST_MALID) {
                        openPINFragmentOrCallback(() -> {
                            NavigationUtils.openNavigationFragment(this, R.id.more_options_nav_item, () -> {
                                NavigationUtils.openAnimeFragment(this, scannerMALId, InfoSourceType.REMOTE);
                            });
                        });

                        return true;
                    }
                } catch (Exception e) {
                }
            }

            String url = getUrl(intent);
            if (url != null && !url.isEmpty()) {
                final long malid = ExtractorUtils.tryToExtractMALidFromLink(url);

                if (malid != Constants.NON_EXIST_MALID) {
                    openPINFragmentOrCallback(() -> {
                        NavigationUtils.openNavigationFragment(this, () -> {
                            NavigationUtils.openAnimeFragment(this, malid, InfoSourceType.REMOTE);
                        });
                    });

                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(prefsChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();

        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(prefsChangeListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LruCache.getInstance().clear();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment last = Utils.getLastFragment(fragmentManager);

        if (last instanceof BaseFragment) {
            if (((BaseFragment) last).onBackPressed()) return;
        }

        if (fragmentManager.getFragments().size() == 1 && last instanceof PINFragment) {
            finish();

            return;
        }

        if (fragmentManager.getFragments().size() > 1 /*NavigationFragment is always opened*/) {
            //popBackStack instead of super.onBackPressed
            fragmentManager.popBackStack();

            return;
        }

        //snackbar will be shown above BottomNavigationView
        //not optimal, but still workable
        View anchorView = findViewById(R.id.nav_fragment_container);
        Snackbar.make(anchorView, getString(R.string.exit_dialog_prompt), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.exit_button_title), v -> finish())
                .show();
    }

    private void openPINFragmentOrCallback(Runnable callback) {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.USE_PASSWORD_BLOCK, false)) {
            NavigationUtils.openPINFragment(this, 0, callback);
        } else {
            callback.run();
        }
    }

    // https://github.com/TeamNewPipe/NewPipe/blob/dev/app/src/main/java/org/schabi/newpipe/RouterActivity.java#L1062
    private String getUrl(final Intent intent) {
        String foundUrl = null;
        if (intent.getData() != null) {
            //From another app
            foundUrl = intent.getData().toString();
        } else if (intent.getStringExtra(Intent.EXTRA_TEXT) != null) {
            //From share menu
            final String extraText = intent.getStringExtra(Intent.EXTRA_TEXT);

            Pattern malPattern = Pattern.compile("myanimelist\\.net/anime/\\d*");
            Matcher malMatcher = malPattern.matcher(extraText);
            malMatcher.find();

            foundUrl = String.format("https://%s", extraText.substring(malMatcher.start(), malMatcher.end()));
        }

        return foundUrl;
    }

    private void changeSecureMode(boolean enable) {
        Window window = getWindow();

        if (window == null) return;

        if (enable) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

}

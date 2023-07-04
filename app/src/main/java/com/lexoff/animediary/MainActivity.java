package com.lexoff.animediary;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;
import com.lexoff.animediary.Fragment.BaseFragment;
import com.lexoff.animediary.Fragment.PINFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState==null) {
            SplashScreen.installSplashScreen(this);
        } else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //https://stackoverflow.com/a/56730934
        Window window = getWindow();
        if (window != null) {
            int uiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            window.getDecorView().setSystemUiVisibility(uiVisibility);

            int flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
            window.getAttributes().flags &= ~flags;

            int halfTransparentColor = Color.argb(75, 0, 0, 0);
            window.setStatusBarColor(halfTransparentColor);
            window.setNavigationBarColor(halfTransparentColor);
        }

        //NavigationUtils.openSplashFragment(this);

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.USE_PASSWORD_BLOCK, false)) {
            NavigationUtils.openPINFragment(this, 0);
        } else {
            NavigationUtils.openNavigationFragment(this);
        }
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

        if (fragmentManager.getFragments().size()==1 && last instanceof PINFragment){
            finish();

            return;
        }

        if (fragmentManager.getFragments().size() > 1 /*NavigationFragment is always opened*/) {
            super.onBackPressed();

            return;
        }

        //snackbar will be shown above BottomNavigationView
        //not optimal, but still workable
        View anchorView=findViewById(R.id.nav_fragment_container);
        Snackbar.make(anchorView, getString(R.string.exit_dialog_prompt), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.exit_button_title), v -> finish())
                .show();
    }

}

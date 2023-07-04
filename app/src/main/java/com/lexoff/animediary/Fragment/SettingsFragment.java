package com.lexoff.animediary.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.lexoff.animediary.BumperCallback;
import com.lexoff.animediary.CacheUtils;
import com.lexoff.animediary.Changelog;
import com.lexoff.animediary.Constants;
import com.lexoff.animediary.LicensesHelper;
import com.lexoff.animediary.LruCache;
import com.lexoff.animediary.NavigationUtils;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Utils;

import java.io.File;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SettingsFragment extends Fragment {

    private SharedPreferences prefs;

    private Handler toastHandler;

    private Disposable cWorker;

    private View rootView;

    public SettingsFragment() {
        //empty
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        toastHandler=new Handler(Looper.getMainLooper());

        prefs=PreferenceManager.getDefaultSharedPreferences(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onResume(){
        super.onResume();

        /*Password Block*/
        setupPwdSwitch();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        /*Password Block*/
        if (!hidden) {
            setupPwdSwitch();
        }
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        //set margins of statusbar and navbar
        int statusbarHeight = Utils.getStatusBarHeight(requireContext());
        rootView.setPadding(0, statusbarHeight, 0, 0);

        int navbarHeight=Utils.getNavBarHeight(requireContext());
        View nbMarginView=rootView.findViewById(R.id.navbar_margin_view);
        ViewGroup.LayoutParams params=nbMarginView.getLayoutParams();
        params.height=navbarHeight;
        nbMarginView.setLayoutParams(params);

        this.rootView=rootView;

        //this needs to be done once!
        View toastLayout=rootView.findViewById(R.id.toast_layout);
        FrameLayout.LayoutParams params2=(FrameLayout.LayoutParams) toastLayout.getLayoutParams();
        params2.setMargins(params2.leftMargin, params2.topMargin, params2.rightMargin, params2.bottomMargin+navbarHeight);
        toastLayout.setLayoutParams(params2);

        ImageView backBtn = rootView.findViewById(R.id.ab_back_btn);
        backBtn.setOnClickListener(v -> {
            Utils.animateClickOnImageButton(v, () -> {
                requireActivity().onBackPressed();
            });
        });

        setupSwitch(R.id.use_gradient_switch, Constants.USE_GRADIENT_IN_POST, true);
        setupSwitch(R.id.always_update_info_switch, Constants.ALWAYS_UPDATE_ITEM_INFO, false);
        setupSwitch(R.id.show_left_episodes_badge_switch, Constants.SHOW_EPISODES_LEFT_BADGE, true);
        setupSwitch(R.id.show_added_to_badge_switch, Constants.SHOW_ADDED_TO_BADGE, true);
        setupSwitch(R.id.show_english_titles_switch, Constants.SHOW_ENGLISH_TITLES, false);
        setupSwitch(R.id.show_next_airing_episode_switch, Constants.SHOW_NEXT_AIRING_EPISODE, true);
        setupSwitch(R.id.show_cover_tip_switch, Constants.SHOW_COVER_TIP, true);

        /*Password Block*/
        setupPwdSwitch();

        View setBumperRow=rootView.findViewById(R.id.set_bumper_row);
        setBumperRow.setOnClickListener(v -> {
            Utils.animateClickOnItem(v, () -> {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.settings_select_bumper_intent_title)), Constants.SELECT_BUMPER_ID);
            });
        });

        View removeBumperRow=rootView.findViewById(R.id.remove_bumper_row);
        removeBumperRow.setOnClickListener(v -> {
            Utils.animateClickOnItem(v, () -> {
                try {
                    File file=Utils.getBumperFile(requireContext());
                    if (file.exists()){
                        file.delete();

                        showToastMessage(getString(R.string.settings_bumper_removed_successfully));
                    } else {
                        showToastMessage(getString(R.string.settings_no_bumper_set_message));
                    }
                } catch (Exception e){
                    String msg=e.getMessage();
                    if (msg==null) msg="";

                    showToastMessage(String.format(getString(R.string.error_happened_with_message), msg));
                }
            });
        });

        TextView versionView = rootView.findViewById(R.id.version_summary);
        versionView.setText(/*BuildConfig.VERSION_NAME*/ Constants.AAV);

        View changelogRow=rootView.findViewById(R.id.changelog_row);
        changelogRow.setOnClickListener(v -> {
            Utils.animateClickOnItem(v, () -> {
                new AlertDialog.Builder(requireContext(), R.style.DarkDialogTheme)
                        .setTitle(getString(R.string.changelog_dialog_title))
                        .setMessage(Changelog.buildChangelog())
                        .setPositiveButton(getString(R.string.close_button_title), null)
                        .create()
                        .show();
            });
        });

        /*Cache Section*/

        calculateCache();

        View clearCacheRow=rootView.findViewById(R.id.clear_cache_row);
        clearCacheRow.setOnClickListener(v -> {
            Utils.animateClickOnItem(v, () -> {
                tryDeleteCacheFiles();
            });
        });

        View clearMetadataRow=rootView.findViewById(R.id.clear_metadata_row);
        clearMetadataRow.setOnClickListener(v -> {
            Utils.animateClickOnItem(v, () -> {
                try {
                    LruCache.getInstance().clear();

                    Toast.makeText(requireContext(), getString(R.string.cached_metadata_cleared_toast_message), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(requireContext(), getString(R.string.error_happened), Toast.LENGTH_SHORT).show();
                }
            });
        });

        /***************/

        View malRow = rootView.findViewById(R.id.mal_row);
        malRow.setOnClickListener(v -> {
            Utils.animateClickOnItem(v, () -> Utils.copyToClipboard(requireContext(), "MyAnimeList", "https://myanimelist.net/"));
        });

        View alRow = rootView.findViewById(R.id.al_row);
        alRow.setOnClickListener(v -> {
            Utils.animateClickOnItem(v, () -> Utils.copyToClipboard(requireContext(), "AniList", "https://anilist.co/"));
        });

        View fiRow=rootView.findViewById(R.id.fi_row);
        fiRow.setOnClickListener(v -> {
            Utils.animateClickOnItem(v, () -> Utils.copyToClipboard(requireContext(), "Feather", "https://feathericons.com/"));
        });

        View atRow=rootView.findViewById(R.id.at_row);
        atRow.setOnClickListener(v -> {
            Utils.animateClickOnItem(v, () -> Utils.copyToClipboard(requireContext(), "Anime Trending", "https://anitrendz.com/"));
        });

        View mdRow=rootView.findViewById(R.id.md_row);
        mdRow.setOnClickListener(v -> {
            Utils.animateClickOnItem(v, () -> Utils.copyToClipboard(requireContext(), "MangaDex", "https://mangadex.org/"));
        });

        /*Licenses*/
        View agRow=rootView.findViewById(R.id.ag_row);
        agRow.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> showLicenseDialog(LicensesHelper.getLicenseText(requireContext(), LicensesHelper.License.MIT))));

        View axRow=rootView.findViewById(R.id.ax_row);
        axRow.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> showLicenseDialog(LicensesHelper.getLicenseText(requireContext(), LicensesHelper.License.APACHE2))));

        View jsRow=rootView.findViewById(R.id.js_row);
        jsRow.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> showLicenseDialog(LicensesHelper.getLicenseText(requireContext(), LicensesHelper.License.MIT))));

        View mcRow=rootView.findViewById(R.id.mc_row);
        mcRow.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> showLicenseDialog(LicensesHelper.getLicenseText(requireContext(), LicensesHelper.License.APACHE2))));

        View njRow=rootView.findViewById(R.id.nj_row);
        njRow.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> showLicenseDialog(LicensesHelper.getLicenseText(requireContext(), LicensesHelper.License.MIT))));

        View ohRow=rootView.findViewById(R.id.oh_row);
        ohRow.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> showLicenseDialog(LicensesHelper.getLicenseText(requireContext(), LicensesHelper.License.APACHE2))));

        View pcRow=rootView.findViewById(R.id.pc_row);
        pcRow.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> showLicenseDialog(LicensesHelper.getLicenseText(requireContext(), LicensesHelper.License.APACHE2))));

        View raRow=rootView.findViewById(R.id.ra_row);
        raRow.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> showLicenseDialog(LicensesHelper.getLicenseText(requireContext(), LicensesHelper.License.APACHE2))));

        View rjRow=rootView.findViewById(R.id.rj_row);
        rjRow.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> showLicenseDialog(LicensesHelper.getLicenseText(requireContext(), LicensesHelper.License.APACHE2))));

        View svRow=rootView.findViewById(R.id.sv_row);
        svRow.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> showLicenseDialog(LicensesHelper.getLicenseText(requireContext(), LicensesHelper.License.APACHE2))));
        /**********/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.SELECT_BUMPER_ID) {
            if (data == null || data.getData() == null) {
                //null can be not only if error
                //but if exited without choice made

                return;
            }

            NavigationUtils.openBumperPreviewFragment(requireActivity(), data.getData(), new BumperCallback() {
                @Override
                public void onSet() {
                    showToastMessage(getString(R.string.settings_bumper_set_successfully));
                }

                @Override
                public void onError(Throwable e) {
                    String msg = e.getMessage();
                    if (msg == null) msg = "";

                    showToastMessage(String.format(getString(R.string.error_happened_with_message), msg));
                }


            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (cWorker!=null) cWorker.dispose();

        cWorker=null;
    }

    private void calculateCache(){
        Context context=requireContext();
        View rootView=getView();

        if (context==null || rootView==null) return;

        long freeSize=context.getExternalFilesDir(null).getUsableSpace(); //maybe getFreeSpace()?
        long fullSize = context.getExternalFilesDir(null).getTotalSpace()-freeSize;
        long cacheSize=CacheUtils.folderSize(context.getCacheDir());

        TextView otherAppsValue=rootView.findViewById(R.id.other_apps_row_value);
        TextView cacheValue=rootView.findViewById(R.id.cache_row_value);
        TextView freeValue=rootView.findViewById(R.id.free_row_value);

        otherAppsValue.setText(CacheUtils.formatSize(context, fullSize));
        cacheValue.setText(CacheUtils.formatSize(context, cacheSize));
        freeValue.setText(CacheUtils.formatSize(context, freeSize));
    }

    private void tryDeleteCacheFiles() {
        View rootView=getView();

        if (rootView==null) return;

        View clearCacheRow=rootView.findViewById(R.id.clear_cache_row);

        clearCacheRow.setClickable(false);

        cWorker = Single.fromCallable(() -> deleteCacheFile())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull Boolean done) -> {
                    calculateCache();

                    clearCacheRow.setClickable(true);

                    if (done) {
                        Toast.makeText(requireContext(), getString(R.string.cached_cleared_toast_message), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.error_happened), Toast.LENGTH_SHORT).show();
                    }
                }, (@NonNull final Throwable throwable) -> {
                    calculateCache();

                    clearCacheRow.setClickable(true);

                    Toast.makeText(requireContext(), getString(R.string.error_happened), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean deleteCacheFile() {
        File cacheDir = requireContext().getCacheDir();

        if (!cacheDir.exists() || !cacheDir.isDirectory()) return false;

        try {
            File[] files = cacheDir.listFiles();
            for (File file : files) {
                CacheUtils.deleteRecursively(file);
            }

            return true;
        } catch (Exception ignored) {
            //do nothing
        }

        return false;
    }

    private void setupSwitch(int switchId, String settingName, boolean defValue){
        boolean setting = prefs.getBoolean(settingName, defValue);

        Switch settingSwitch = rootView.findViewById(switchId);
        settingSwitch.setChecked(setting);
        settingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(settingName, isChecked).commit();
        });
    }

    private void setupPwdSwitch() {
        boolean usePwdBlock = prefs.getBoolean(Constants.USE_PASSWORD_BLOCK, false);

        Switch usePwdSwitch = rootView.findViewById(R.id.use_password_block_switch);
        usePwdSwitch.setChecked(usePwdBlock);
        usePwdSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(Constants.USE_PASSWORD_BLOCK, isChecked).commit();

            if (isChecked) {
                NavigationUtils.openPINFragment(requireActivity(), 1);
            }
        });
    }

    private void showLicenseDialog(Spanned text){
        new AlertDialog.Builder(requireContext(), R.style.DarkDialogTheme)
                .setMessage(text)
                .setPositiveButton(getString(R.string.close_button_title), null)
                .create()
                .show();
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

}

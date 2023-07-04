package com.lexoff.animediary;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.lexoff.animediary.Extractor.ExtractorUtils;
import com.lexoff.animediary.Extractor.SearchCategory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class Utils {

    private static Handler innerHandler=new Handler(Looper.getMainLooper());

    public static Fragment getLastFragment(FragmentManager manager){
        List<Fragment> fragments=manager.getFragments();

        if (fragments.size()==0) return null;

        return fragments.get(fragments.size()-1);
    }

    public static int getStatusBarHeight(Context context){
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }

        return result;
    }

    //unstable method that won't work on MIUI, for example
    public static int getNavBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }

        return result;
    }

    //it actually calculates correctly
    public static int dpToPx(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    public static void animateClickOnItem(View v, Runnable callback){
        //drawable instead of tint list
        //because tint list has no effect in grid layout
        final Drawable startDrw=v.getForeground();

        v.setForeground(new ColorDrawable(Color.parseColor("#30F5F5F5")));
        v.postDelayed(() -> {
            v.setForeground(startDrw);

            callback.run();
        }, 10); //was 100
    }

    public static void animateClickOnImageButton(View v, Runnable callback) {
        if (!(v instanceof ImageView)) {
            return;
        }

        final ImageView imgView=(ImageView) v;

        final Drawable drawable=imgView.getDrawable();
        Bitmap bitmap=drawableToBitmap(drawable);

        Bitmap resized=Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth()*0.85), (int) (bitmap.getHeight()*0.85), false);
        imgView.setImageBitmap(resized);

        imgView.postDelayed(() -> {
            imgView.setImageDrawable(drawable);

            callback.run();
        }, 100);
    }

    public static Bitmap drawableToBitmap(Drawable drawable){
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static String formatSummary(String fullSummary){
        return fullSummary;
    }

    public static String shortenSummary(String fullSummary, int maxLength){
        if (fullSummary.length()<=maxLength) return fullSummary;
        else {
            String shortSummary=fullSummary.substring(0, maxLength);

            shortSummary=shortSummary.substring(0, shortSummary.lastIndexOf(" "));

            //TODO: this needs to be a regex, obv
            while (shortSummary.endsWith(",") || shortSummary.endsWith(".")){
                shortSummary=shortSummary.substring(0, shortSummary.length()-1);
            }

            return shortSummary + "...";
        }
    }

    public static String unescapeStr(String s){
        //there must go all stuff that doesn't unescape automatically
        return s.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
    }

    public static void copyToClipboard(Context context, String label, String text) {
        try {
            ClipboardManager cManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (cManager == null) return;

            ClipData clipData = ClipData.newPlainText(label, text);
            cManager.setPrimaryClip(clipData);

            Toast.makeText(context, String.format(context.getString(R.string.copied_to_clipboard_toast_message), text), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            //just in case android will break something
        }
    }

    public static void showOpenByUrlDialog(Activity contextActivity){
        View dialogView=View.inflate(contextActivity, R.layout.open_by_url_dialog, null);
        EditText editText=dialogView.findViewById(R.id.url_edittext);

        AlertDialog dialog=new AlertDialog.Builder(contextActivity, R.style.DarkDialogTheme)
                .setView(dialogView)
                .setCancelable(true)
                .setPositiveButton(contextActivity.getString(R.string.dialog_open_button_title), null)
                .setNeutralButton(contextActivity.getString(R.string.dialog_paste_button_title), null)
                .create();

        dialog.setOnShowListener(dialog1 -> {
            Button positiveBtn = ((AlertDialog) dialog1).getButton(AlertDialog.BUTTON_POSITIVE);
            positiveBtn.setOnClickListener(v1 -> {
                String input=editText.getText().toString().trim();

                if (input.isEmpty()){
                    Toast.makeText(contextActivity, contextActivity.getString(R.string.url_cannot_be_empty_message), Toast.LENGTH_SHORT).show();

                    return;
                }

                try {
                    //hide keyboard to remove flags set by opened keyboard to Activity
                    hideKeyboard(editText);

                    //call with delay to let keyboard to become hidden and remove flags from Activity
                    //100 msecs usually is enough
                    innerHandler.postDelayed(() -> {
                        long malid = ExtractorUtils.tryToExtractMALidFromLink(input);
                        NavigationUtils.openAnimeFragment(contextActivity, malid, InfoSourceType.REMOTE);
                    }, 100L);
                } catch (Exception e){}

                dialog.dismiss();
            });

            Button neutralBtn=((AlertDialog) dialog1).getButton(AlertDialog.BUTTON_NEUTRAL);
            neutralBtn.setOnClickListener(v2 -> {
                try {
                    ClipboardManager clipboard = (ClipboardManager) contextActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboard != null) {
                        ClipData clip = clipboard.getPrimaryClip();
                        if (clip != null && clip.getItemCount() > 0 && clip.getItemAt(0).getText() != null) {
                            editText.setText(clip.getItemAt(0).getText());
                        }
                    }
                } catch (Exception e){
                    Toast.makeText(contextActivity, contextActivity.getString(R.string.error_happened), Toast.LENGTH_SHORT).show();
                }
            });

            //showKeyboard(editText);
        });

        dialog.show();
    }

    public static void setUnsetBold(TextView textView, boolean bold){
        if (textView!=null){
            textView.setTypeface(null, bold ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    public static void showKeyboard(EditText view) {
        if (view == null) return;

        try {
            if (view.requestFocus()) {
                InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
            }
        } catch (Exception e){
            //oh no, by the way...
        }
    }

    public static void hideKeyboard(EditText view) {
        if (view == null) return;

        try {
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

            view.clearFocus();
        } catch (Exception e){
            //oh no, by the way...
        }
    }

    public static boolean containsIgnoreCase(String str, String subStr){
        return str.toLowerCase().contains(subStr.toLowerCase());
    }

    public static boolean compareIgnoreCase(String str1, String str2){
        return str1.toLowerCase().equals(str2.toLowerCase());
    }

    public static int listModeToInt(ListMode listMode){
        if (listMode==ListMode.LIST){
            return 0;
        } else if (listMode==ListMode.GRID){
            return 1;
        }

        return 0;
    }

    public static ListMode intToListMode(int listMode){
        if (listMode==0){
            return ListMode.LIST;
        } else if (listMode==1){
            return ListMode.GRID;
        }

        return ListMode.LIST;
    }

    public static int searchCategoryToInt(SearchCategory category){
        if (category==SearchCategory.ANIME){
            return 0;
        } else if (category==SearchCategory.COMPANY){
            return 1;
        }

        return 0;
    }

    public static SearchCategory intToSearchCategory(int category){
        if (category==0){
            return SearchCategory.ANIME;
        } else if (category==1){
            return SearchCategory.COMPANY;
        }

        return SearchCategory.ANIME;
    }

    public static String buildAnimeUrl(long malid){
        //by default it is MAL url, but would be nice to build also AL urls
        return String.format("https://myanimelist.net/anime/%d/", malid);
    }

    public static void resizeImageView(ImageView view, int newWidth, int newHeight){
        ViewGroup.LayoutParams params=view.getLayoutParams();
        params.width=newWidth;
        params.height=newHeight;
        view.setLayoutParams(params);
    }

    public static int resolveAttrColor(Context context, int id){
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(id, value, true);
        return value.data;
    }

    public static File getBumperFile(Context context){
        return new File(context.getFilesDir() + Constants.BUMPER_FILE_NAME);
    }

    public static Uri resolveBumperUri(Context context){
        try {
            File file = getBumperFile(context);
            if (file.exists()) {
                Uri uri = Uri.fromFile(file);
                return uri;
            }

            return null;
        } catch (Exception e){
            return null;
        }
    }

    public static boolean isPackageInstalled(Activity activity, String packageName){
        try {
            activity.getPackageManager().getPackageInfo(packageName, 0);

            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isSpotifyAppInstalled(Activity activity){
        return isPackageInstalled(activity, Constants.SPOTIFY_APP_PACKAGE_NAME);
    }

    public static boolean openUrlInSpotifyApp(Context context, String url){
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setPackage(Constants.SPOTIFY_APP_PACKAGE_NAME);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String buildMangaDexUrl(String id){
        return String.format("https://mangadex.org/title/%s", id);
    }

    public static String getStringOrEmpty(String s){
        return s==null ? "" : s;
    }

    //TODO: replace RELATIVE_PATH with something to api <Q
    public static void saveImageToDownloads(Context context, Bitmap bmp, String name) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/*");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        Uri uri = resolver.insert(MediaStore.Files.getContentUri("external"), values);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if (name.toLowerCase().endsWith(".png")) {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, bos);
        } else {
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        }
        byte[] bmpData = bos.toByteArray();
        bos.close();

        OutputStream os = resolver.openOutputStream(uri, "w");
        os.write(bmpData);
        os.flush();
        os.close();
    }

    public static String fromCapitalLetter(String s){
        return s.substring(0, 1).toUpperCase()+s.substring(1).toLowerCase();
    }

    public static int[] getDisplaySize(Context context){
        if (!(context instanceof Activity)) {
            return new int[]{0, 0};
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return new int[]{
                displayMetrics.widthPixels,
                displayMetrics.heightPixels
        };
    }

}

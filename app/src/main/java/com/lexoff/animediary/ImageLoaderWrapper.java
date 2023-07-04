package com.lexoff.animediary;

import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

//Methods without placeholder are deprecated
//but they will be kept for some time just in case

public class ImageLoaderWrapper {

    //Deprecated
    public static void loadImage(String url, Target success, Runnable fail) {
        if (url == null || url.isEmpty() || success == null) {
            if (fail != null)
                fail.run();

            return;
        }

        Picasso.get().load(url).into(success);
    }

    public static void loadImageWithPlaceholder(String url, Target success, Runnable fail) {
        if (url == null || url.isEmpty() || success == null) {
            if (fail != null)
                fail.run();

            return;
        }

        Picasso.get().load(url).placeholder(R.drawable.dummy_loading_thumbnail).error(R.drawable.dummy_no_thumbnail).into(success);
    }

    //Deprecated
    public static void loadImage(String url, ImageView view, Runnable fail) {
        if (url == null || url.isEmpty() || view == null) {
            if (fail != null)
                fail.run();

            return;
        }

        Picasso.get().load(url).into(view);
    }

    public static void loadImageWithPlaceholder(String url, ImageView view, Runnable fail) {
        if (url == null || url.isEmpty() || view == null) {
            if (fail != null)
                fail.run();

            return;
        }

        Picasso.get().load(url).placeholder(R.drawable.dummy_loading_thumbnail).error(R.drawable.dummy_no_thumbnail).into(view);
    }

    //Deprecated
    public static void loadImageAndResize(String url, ImageView view, int targetW, int targetH, Runnable fail) {
        if (url == null || url.isEmpty() || view == null) {
            if (fail != null)
                fail.run();

            return;
        }

        Picasso.get().load(url).resize(targetW,targetH).centerCrop().into(view);
    }

    public static void loadImageAndResizeWithPlaceholder(String url, ImageView view, int targetW, int targetH, Runnable fail) {
        if (url == null || url.isEmpty() || view == null) {
            if (fail != null)
                fail.run();

            return;
        }

        Picasso.get().load(url).resize(targetW,targetH).centerCrop().placeholder(R.drawable.dummy_loading_thumbnail).error(R.drawable.dummy_no_thumbnail).into(view);
    }

}

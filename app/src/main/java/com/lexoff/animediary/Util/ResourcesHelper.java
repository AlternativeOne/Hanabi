package com.lexoff.animediary.Util;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.res.ResourcesCompat;

import com.lexoff.animediary.App;
import com.lexoff.animediary.R;

public class ResourcesHelper {
    private static Drawable roundedDarkDialogBackground;

    public static Drawable roundedDarkDialogBackground(){
        Context context= App.getApp();

        return roundedDarkDialogBackground==null ? ResourcesCompat.getDrawable(context.getResources(), R.drawable.rounded_dark_dialog_surface, context.getTheme()) : roundedDarkDialogBackground;
    }
}

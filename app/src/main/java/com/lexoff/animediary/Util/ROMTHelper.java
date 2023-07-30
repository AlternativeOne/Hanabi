package com.lexoff.animediary.Util;

import android.os.Handler;
import android.os.Looper;

public class ROMTHelper {

    private static Handler handler=new Handler(Looper.myLooper(), null);

    public static void runOnMainThread(Runnable r){
        handler.post(r);
    }

    public static void runOnMainThread(Runnable r, long d){
        handler.postDelayed(r, d);
    }

}

package com.lexoff.animediary.Util;

import android.content.Context;
import android.text.format.Formatter;

import java.io.File;

public class CacheUtils {

    public static long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }

    public static String formatSize(Context context, long size) {
        //TODO: write custom solution
        return Formatter.formatFileSize(context, size);
    }

    public static void deleteRecursively(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursively(child);

        fileOrDirectory.delete();
    }

}

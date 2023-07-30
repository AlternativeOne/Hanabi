package com.lexoff.animediary.Util;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;

import androidx.core.content.ContextCompat;

import com.lexoff.animediary.App;
import com.lexoff.animediary.Exception.InvalidZipException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BackupHelper {

    private static String BACKUP_FILE_NAME_PATTERN="Hanabi_%s.zip";
    private static SimpleDateFormat EXPORT_DATE_FORMAT=new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);

    private static int PERSISTABLE_READ_FLAGS=Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION;
    private static int PERSISTABLE_WRITE_FLAGS=Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

    public static Intent getBackupPicker() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT)
                .putExtra("android.content.extra.SHOW_ADVANCED", true)
                .setType("application/zip")
                .addCategory(Intent.CATEGORY_OPENABLE)
                .addFlags(PERSISTABLE_WRITE_FLAGS);

        intent.putExtra(Intent.EXTRA_TITLE, String.format(BACKUP_FILE_NAME_PATTERN, EXPORT_DATE_FORMAT.format(new Date())));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.getExternalStorageDirectory().getAbsolutePath());
        }

        return intent;
    }

    public static Intent getRestorePicker(){
        return new Intent(Intent.ACTION_OPEN_DOCUMENT)
                .putExtra("android.content.extra.SHOW_ADVANCED", false /*true*/)
                .setType("application/zip")
                .addCategory(Intent.CATEGORY_OPENABLE)
                .addFlags(PERSISTABLE_READ_FLAGS);
    }

    public static void addToZip(Uri zipFile, File[] files) throws IOException {
        int BUFFER = 2048;

        BufferedInputStream origin;
        OutputStream dest = App.getApp().getContentResolver().openOutputStream(zipFile);

        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

        byte[] data = new byte[BUFFER];

        for (File file : files) {
            FileInputStream fi = new FileInputStream(file);
            origin = new BufferedInputStream(fi, BUFFER);
            ZipEntry entry = new ZipEntry(file.getName());
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            out.flush();
            origin.close();
        }

        out.close();
    }

    public static void getFromZip(Uri zipFile) throws IOException, InvalidZipException {
        if (!isValidZipFile(zipFile)){
            throw new InvalidZipException();
        }

        InputStream is = App.getApp().getContentResolver().openInputStream(zipFile);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
        ZipEntry ze;
        byte[] buffer = new byte[2048];
        int count;

        while ((ze = zis.getNextEntry()) != null) {
            String entryName = ze.getName();

            if (ze.isDirectory()) {
                continue;
            }

            File output;

            if (entryName.equals("main.db")) {
                output = db();
            } else {
                continue;
            }

            if (output.exists()) {
                if (!output.delete()) {
                    throw new IOException("Could not delete " + output);
                }
            }

            FileOutputStream fout = new FileOutputStream(output);

            while ((count = zis.read(buffer)) != -1) {
                fout.write(buffer, 0, count);
            }

            fout.flush();
            fout.close();

            zis.closeEntry();
        }

        zis.close();
    }

    public static boolean isValidZipFile(Uri zipFile) {
        try {
            ZipInputStream ignored =
                    new ZipInputStream(new BufferedInputStream(App.getApp().getContentResolver().openInputStream(zipFile)));

            return true;
        } catch (final IOException ioe) {
            return false;
        }
    }

    public static File db(){
        return new File(ContextCompat.getDataDir(App.getApp()), "/databases/main.db");
    }

    public static File dbJournal(){
        return new File(ContextCompat.getDataDir(App.getApp()), "/databases/main.db-journal");
    }

    public static File dbWal(){
        return new File(ContextCompat.getDataDir(App.getApp()), "/databases/main.db-wal");
    }

    public static File dbShm(){
        return new File(ContextCompat.getDataDir(App.getApp()), "/databases/main.db-shm");
    }
}

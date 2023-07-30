package com.lexoff.animediary.Util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lexoff.animediary.R;

public class ShareUtils {

    public static void copyToClipboard(Context context, String label, String text) {
        try {
            ClipboardManager cManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (cManager == null) return;

            ClipData clipData = ClipData.newPlainText(label, text);
            cManager.setPrimaryClip(clipData);

            if (Build.VERSION.SDK_INT < 33) {
                Toast.makeText(context, String.format(context.getString(R.string.copied_to_clipboard_toast_message), text), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            //just in case android will break something
        }
    }

    public static void shareText(Context context, String text, String extraTitle){
        View dialogView=View.inflate(context, R.layout.share_dialog, null);
        TextView textView=dialogView.findViewById(R.id.text_view);
        textView.setText(text);

        new MaterialAlertDialogBuilder(context, R.style.DarkDialogTheme)
                .setBackground(ResourcesHelper.roundedDarkDialogBackground())
                .setView(dialogView)
                .setPositiveButton(context.getString(R.string.share_dialog_copy_button_title), (dialog, which) -> copyToClipboard(context, null, text))
                .setNegativeButton(context.getString(R.string.share_dialog_share_button_title), (dialog, which) -> {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/*");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, text);
                    if (extraTitle!=null && !extraTitle.isEmpty())
                        shareIntent.putExtra(Intent.EXTRA_TITLE, extraTitle);

                    context.startActivity(Intent.createChooser(shareIntent, null));
                })
                .create()
                .show();
    }

}

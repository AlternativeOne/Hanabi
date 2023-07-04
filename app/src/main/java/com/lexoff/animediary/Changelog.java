package com.lexoff.animediary;

import android.text.Html;
import android.text.SpannableStringBuilder;

public class Changelog {

    private static String[] changelog = new String[]{
            "<b>1.3 build_ver.N</b><p>",
            "<b>1.3 build_ver.O</b><p>",
            "<b>1.3 build_ver.P</b><p>",
            "<b>1.3 build_ver.Q</b><p>",
            "<b>1.4 build_ver.A</b><p>",
            "<b>1.4 build_ver.B</b><p>",
            "<b>1.4 build_ver.C</b><p>",
            "<b>1.4 build_ver.D</b><p>",
            "<b>1.4 build_ver.E</b><p>",
            "<b>1.4 build_ver.F</b><p>",
            "<b>1.4 build_ver.G (26.04.23)</b><p>",
            "<b>1.4 build_ver.H (28.04.23)</b><p>",
            "<b>1.5 build_ver.A (03.07.23)</b><p>"
    };

    public static SpannableStringBuilder buildChangelog() {
        SpannableStringBuilder s = new SpannableStringBuilder();

        for (int i=changelog.length-1; i>=0; i--) {
            String c=changelog[i];

            s.append(Html.fromHtml(c, 0));
        }

        return s;
    }

}

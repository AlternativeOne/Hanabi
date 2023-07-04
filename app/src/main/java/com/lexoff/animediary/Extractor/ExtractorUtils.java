package com.lexoff.animediary.Extractor;

import com.lexoff.animediary.Constants;

import org.jsoup.nodes.Element;

public class ExtractorUtils {

    public static String tryToExtractSrcOfImg(Element element){
        //for some reason src attr is empty, but data-src is not
        String src=element.attr("data-src").trim();
        if (src.isEmpty()) return element.attr("src").trim();
        return src;
    }

    public static long tryToExtractMALidFromLink(String link){
        String[] splits = link.split("/");
        for (String split : splits) {
            try {
                long malid = Long.parseLong(split);

                return malid;
            } catch (Exception e) {
                continue;
            }
        }

        return Constants.NON_EXIST_MALID;
    }

    public static String getStringOrEmpty(String s){
        return s==null ? "" : s;
    }

}

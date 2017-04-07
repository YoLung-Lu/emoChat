package com.crazyhitty.chdev.ks.firebasechat.utils;

import android.support.annotation.NonNull;
import android.text.TextUtils;


import java.io.File;
import java.util.Locale;

/**
 * Created by prada on 10/5/15.
 */
public class FileUtils {
    public static boolean isAnimatedFile(String path) {
        return TextUtils.isEmpty(path) ? false : path.endsWith(".gif") || path.endsWith(".mp4");
    }

    /**
     * In order to speed up the examination, it refers to the extension name only.
     */
    public static boolean isGifFile(String url) {
        return TextUtils.isEmpty(url) ? false : url.endsWith(".gif");
    }

    public static String getCaptureType(@NonNull String path) {
        if (path.endsWith(".gif")) {
            return "Gif";
        }
        if (path.endsWith(".mp4")) {
            return "Video";
        }
        return "Image";
    }

    public static String appendPath(final String originalUrl, String path) {
        if (TextUtils.isEmpty(originalUrl) || TextUtils.isEmpty(path)) {
            return originalUrl;
        }
        if (originalUrl.endsWith("/") && path.startsWith("/")) {
            return originalUrl + path.substring(1);
        } else if (!originalUrl.endsWith("/") && !path.startsWith("/")) {
            return originalUrl + "/" + path;
        }
        return originalUrl + path;
    }
}

package com.download.util;

import android.text.TextUtils;
import android.util.Base64;

/**
 * Created by peiboning on 2018/3/14.
 */

public class KeyBuilder {
    public static String key(String url){
        if(TextUtils.isEmpty(url)){
            return "";
        }
        return Base64.encodeToString(url.getBytes(), Base64.DEFAULT);
    }
}

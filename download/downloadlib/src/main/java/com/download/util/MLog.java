package com.download.util;

import android.util.Log;

/**
 * Created by peiboning on 2018/3/14.
 */

public class MLog {
    public static boolean DEBUG = true;

    public static void d(String tag, String msg){
        if(DEBUG){
            Log.d(tag, msg);
        }
    }
    public static void i(String tag, String msg){
        if(DEBUG){
            Log.i(tag, msg);
        }
    }
    public static void e(String tag, String msg){
        if(DEBUG){
            Log.e(tag, msg);
        }
    }
}

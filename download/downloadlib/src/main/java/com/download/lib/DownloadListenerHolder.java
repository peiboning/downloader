package com.download.lib;

import android.text.TextUtils;

import com.download.model.DownloadListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by peiboning on 2018/3/15.
 */

public class DownloadListenerHolder {
    private static Map<String, List<DownloadListener>> map = new HashMap<>();

    public static void addListener(String key, DownloadListener listener){
        if(!TextUtils.isEmpty(key) && null != listener){
            List<DownloadListener> list = map.get(key);
            if(null == list){
                list = new ArrayList<>();
            }
            boolean flag = true;
            if(list.size()>0){
                for(DownloadListener reference : list){
                    if(listener == reference){
                        flag = false;
                        break;
                    }
                }
            }
            if(flag){
                list.add(listener);
                map.put(key, list);
            }
        }
    }

    public static List<DownloadListener> getListener(String key){
        return map.get(key);
    }

    public static void removeListener(String key, DownloadListener listener){
        List<DownloadListener> list = map.get(key);
        if(null != list && list.size() > 0){
            list.remove(listener);
            map.put(key, list);
        }
    }
    public static void removeListener(String key){
        map.remove(key);
    }
}

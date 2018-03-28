package com.download.lib;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.download.model.DownloadListener;
import com.download.model.Task;
import com.download.util.KeyBuilder;
import com.download.util.MLog;

import java.io.File;

//TODO 1、权限检测 2、任务重新开始测试 3、网络加载外部可以实现 4、

/**
 *
 * @author peiboning
 * @date 2018/3/13
 */

public class Downloader {
    private static Downloader sInstance;
    private Object mQueueLock;
    private DownloadLooper mLooper;
    private String downloaderPath;
    private String dbName;
    private Context context;


    public static Downloader getInstance(){
        if(null == sInstance){
            synchronized (Downloader.class){
                if(null == sInstance){
                    sInstance = new Downloader();
                }
            }
        }
        return sInstance;
    }

    private Downloader(){
        mQueueLock = new Object();
        mLooper = new DownloadLooper(mQueueLock);
    }

    public void setContext(Context context){
        this.context = context;
    }

    public Context getContext(){
        return context;
    }

    public String getDownloaderPath() {
        if(TextUtils.isEmpty(downloaderPath)){
            downloaderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "test_down";
            File file = new File(downloaderPath);
            if(!file.exists()){
                file.mkdirs();
            }
            downloaderPath = file.getAbsolutePath();
        }
        MLog.i("DOWN_SDK", "storage path is " + downloaderPath);
        return downloaderPath;
    }

    public boolean addTask(String url){
        if(TextUtils.isEmpty(url)){
            MLog.i("DOWN_SDK", "download url is empty ");
            return false;
        }
        Task task = new Task();
        task.setKey(KeyBuilder.key(url));
        task.setUrl(url);
        task.setLock(mQueueLock);
        mLooper.putTask(task);
        return true;
    }
    public boolean addTask(String url, DownloadListener listener){
        if(TextUtils.isEmpty(url)){
            MLog.i("DOWN_SDK", "download url is empty ");
            return false;
        }
        Task task = new Task();
        task.setKey(KeyBuilder.key(url));
        task.setUrl(url);
        task.setLock(mQueueLock);
        task.addListener(listener);
        DownloadListenerHolder.addListener(url, listener);
        mLooper.putTask(task);

        return true;
    }

    public void addListener(String url, DownloadListener listener){
        if(TextUtils.isEmpty(url)){
            MLog.i("DOWN_SDK", "download url is empty , add listener failed");
            return ;
        }
        if(null == listener){
            MLog.i("DOWN_SDK", "listener is null,add failed ");
        }
        DownloadListenerHolder.addListener(url, listener);
        mLooper.addListener(url, listener);
    }

    public void stopDownload(){
        mLooper.stop();
    }

    public void resumeDownload(){
        mLooper.resume(true);
    }

    public void setDownloaderPath(String downloaderPath) {
        this.downloaderPath = downloaderPath;
    }

    public void setDbName(String dbName){
        this.dbName = dbName;
    }

    public String getDbName(){
        return dbName;
    }
}

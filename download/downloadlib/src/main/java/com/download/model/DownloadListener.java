package com.download.model;

/**
 * Created by peiboning on 2018/3/13.
 */

public abstract class DownloadListener {
    private String ower;

    protected DownloadListener(){
    }
    protected void setOwer(String key){
        ower = key;
    }

    public String getOwerUrl(){
        return ower;
    }

    public abstract void onDownloadStart();
    public abstract void onProcess(int percent);
    public abstract void onDownloadEnd(boolean isSuccess);
    public void onDownloadEnd(boolean isSuccess, String speed){

    }
}

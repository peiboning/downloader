package com.download.model;

import com.download.db.DownloadDAO;
import com.download.lib.Downloader;
import com.download.util.MLog;
import com.download.util.NetUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author peiboning
 * @date 2018/3/13
 */

public class DownloadExecutor implements Runnable{
    private Task task;
    private String key;
    private int _id;
    private int begin;
    private int progress;
    private int end;

    private boolean isStop;
    private DownloadListener listener;
    private int index;
    private int retry;
    private static final int RETRY = 5;
    private int length;
    private boolean isNeedCallNext = true;

    public boolean isNeedCallNext() {
        return isNeedCallNext;
    }

    public void setNeedCallNext(boolean needCallNext) {
        isNeedCallNext = needCallNext;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int getProgress() {
        return progress;
    }

    public String getUrl(){
        return task.getUrl();
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public boolean isStop() {
        return isStop;
    }

    public void setStop(boolean stop) {
        isStop = stop;
    }

    public DownloadListener getListener() {
        return listener;
    }

    public void setListener(DownloadListener listener) {
        this.listener = listener;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public String toString() {
        return "DownloadExecutor{" +
                " _id=" + _id +
                ", begin=" + begin +
                ", progress=" + progress +
                ", end=" + end +
                ", index=" + index +
                '}';
    }

    @Override
    public void run() {
        download(task.getUrl());
    }

    private void download(String u){
        if(isStop){
            return;
        }
        if(retry > RETRY){
            return;
        }
        HttpURLConnection connection = null;
        try {
            URL url = new URL(u);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(100 * 1000);
            connection.setConnectTimeout(10 * 1000);
            connection.setRequestProperty("range", "bytes="+(begin + progress)+"-"+end);

            int code = connection.getResponseCode();
            if(200 == code){
                //不支持断点
                readFullContent(connection.getInputStream());
            }else if(302 == code){
                String lU = connection.getHeaderField("Location");
                retry++;
                download(lU);
            }else if(206 == code){
                //支持断点续传
                if(isNeedCallNext && index==0){
                    int next = index + 1;
                    isNeedCallNext = false;
                    task.startNext(getLength(), next);
                }
                readContent(connection.getInputStream());
            }else{
                retry++;
                download(u);
            }

        } catch (Exception e) {
            e.printStackTrace();
            retry++;
            download(u);
        }finally {
            if(null != connection){
                connection.disconnect();
            }
        }
    }

    private void readContent(InputStream inputStream) throws Exception {
        File f = new File(Downloader.getInstance().getDownloaderPath(), key.hashCode()+".apk");
        RandomAccessFile file = new RandomAccessFile(f, "rw");
        file.seek(progress + begin);
        try {
            byte[] buffer = getBuffer();
            int len = -1;
            int updateGate = 1024 * 20;
            int updateProgress = 0;
            while ((len = inputStream.read(buffer)) != -1){
                file.write(buffer, 0, len);
                updateProgress = len + updateProgress;
                progress = progress + len;

                if((len<buffer.length || updateProgress>updateGate)){
                    DownloadDAO.updateProgress(task.getKey(), progress, index);
                    if(null != listener){
                        listener.onProcess(updateProgress);
                        updateProgress = 0;
                    }
                }
                if(isStop){
                    break;
                }
            }
            if(!isStop){//下载完毕
                MLog.i("DOWN_SDK", "progress : " + progress + "   -- end-start : " + (end-begin));
                if(null != listener){
                    listener.onDownloadEnd(true);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            if(null != listener){
                listener.onDownloadEnd(false);
            }

        }finally {
            if(null != inputStream){
                inputStream.close();
            }
        }
    }

    private void readFullContent(InputStream inputStream)throws Exception {
        File file = new File(Downloader.getInstance().getDownloaderPath(), key+".apk");
        try {
            file.deleteOnExit();
            file.createNewFile();
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            byte[] buffer = new byte[2048];
            int len = -1;
            int updateGate = 1024 * 20;
            int updateProgress = 0;
            while ((len = inputStream.read(buffer)) != -1){
                out.write(buffer, 0, len);
                if(null != listener && (len<buffer.length || updateProgress>updateGate)){
                    listener.onProcess(len);
                    updateProgress = 0;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null != inputStream){
                inputStream.close();
            }
        }
    }

    private byte[] getBuffer(){
        String netType = NetUtils.getNetWorkType(Downloader.getInstance().getContext());
        if("WIFI".equalsIgnoreCase(netType)){
            return new byte[1024 * 20];
        }else if("4G".equalsIgnoreCase(netType)){
            return new byte[1024 * 10];
        }else{
            return new byte[1024 * 3];
        }
    }
}

package com.download.model;

import android.content.ContentValues;

import com.download.db.DownloadDAO;
import com.download.lib.DownloadListenerHolder;
import com.download.net.HttpUrlNetWork;
import com.download.net.INetWork;
import com.download.net.Request;
import com.download.net.Response;
import com.download.util.MLog;
import com.download.util.Utils;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author peiboning
 * @date 2018/3/13
 */

public class Task extends DownloadListener{
    private String key;
    private String url;
    private boolean isNeedMulti;
    private List<DownloadListener> listeners;
    private INetWork netWork;
    private int THREAD_NUM = 3;
    private Object lock;
    private int length;
    private AtomicInteger progress;
    private List<DownloadExecutor> executors;
    private int lastRealPercent;
    private AtomicInteger lastRealbyte;
    private AtomicBoolean result;
    private AtomicInteger executorSize;
    private AtomicLong lastTime;
    private String currentSpeed="0KB";

    public ContentValues contentValues(){
        ContentValues values = new ContentValues();

        return values;
    }

    public Task(){
        super();
        netWork = new HttpUrlNetWork();
        progress = new AtomicInteger(0);
        executors = new ArrayList<>(THREAD_NUM);
        result = new AtomicBoolean(true);
        executorSize = new AtomicInteger(0);
        lastRealbyte = new AtomicInteger(0);
        lastTime = new AtomicLong();
    }

    private static ThreadPoolExecutor mExecutor;
    static {
        mExecutor = new ThreadPoolExecutor(1,5,60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10), new CustomThreadFactory());
    }

    public String getKey() {
        return key;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public void onDownloadStart() {
        MLog.i("DOWN_SDK", "start download ");

        checkExecutors();

        if(null != listeners && listeners.size() > 0){
            for(DownloadListener listener : listeners){
                if(listener != null){
                    listener.onDownloadStart();
                }
            }
        }
    }

    private void checkExecutors() {
        executors = DownloadDAO.getExecutors(key);
        if(null != executors && executors.size()>0){
            for(DownloadExecutor executor : executors){
                executor.setTask(this);
                executor.setLength(length);
                executor.setListener(this);
            }
        }
    }

    @Override
    public void onProcess(int percent) {

        int num = progress.addAndGet(percent);
        int realPercent = (int) (( num * 1.0f / length) * 100);
        String speed = getSpeed(percent);
        if(realPercent > lastRealPercent){
            lastRealPercent = realPercent;
//            MLog.i("DOWN_SDK", Thread.currentThread().getId() + "  download progress is " +realPercent + "%");
            MLog.i("DOWN_SDK", "  download speed is " +speed );
            if(null != listeners && listeners.size() > 0){
                for(DownloadListener listener : listeners){
                    if(null != listener){
                        listener.onProcess(realPercent);
                    }
                }
            }
        }

    }

    private String getSpeed(int percent) {
        long gapTime = System.currentTimeMillis() - lastTime.get();
        lastRealbyte.getAndAdd(percent);
        if(gapTime > 2000){
            lastTime.set(System.currentTimeMillis());
            currentSpeed = Utils.formatByte((int) (lastRealbyte.get()/(gapTime/1000)));
            lastRealbyte.set(0);
        }
        return currentSpeed;
    }

    @Override
    public void onDownloadEnd(boolean isSuccess) {
        boolean flag = result.get();
        result.set(flag && isSuccess);
        if(executorSize.decrementAndGet() <= 0){
            MLog.i("DOWN_SDK", "download over , result is  " + result.get());
            if(null != listeners && listeners.size() > 0){
                for(DownloadListener listener : listeners){
                    if(null != listener){
                        listener.onDownloadEnd(result.get());
                    }
                }
            }
            if(result.get()){
                int deleteRes = DownloadDAO.deleteTask(getKey());
                int deleteExRes = DownloadDAO.deleteExecutor(getKey());
                DownloadListenerHolder.removeListener(getKey());
                MLog.i("DOWN_SDK", "download over , delete TaskRes  " + deleteRes + "   ExecuteRes " + deleteExRes);
            }
            synchronized (lock){
                lock.notify();
            }
        }
    }

    private static class CustomThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "DownloadExecutor");

        }
    }

    public void begin(){
        reset();
        Response response = netWork.perform(getRequest());
        if(null != response){
            MLog.i("DOWN_SDK", "reponse is " + response.toString());
            if(response.getHeader().responseCode == HttpURLConnection.HTTP_OK){
                length = response.getHeader().contentLength;
                onDownloadStart();
                if(null != executors && executors.size() > 0){
                    int percent = 0;
                    for(DownloadExecutor executor : executors){
                        percent = percent + executor.getProgress();
                        if(executor.getProgress() < (executor.getEnd() - executor.getBegin() + 1)){
                            executor.setNeedCallNext(false);
//                            mExecutor.execute(executor);
                            //FIXME 可以使用线程池
                            new Thread(executor).start();
                            executorSize.incrementAndGet();
                        }
                    }
                    onProcess(percent);
                    if(percent == length){
                        onDownloadEnd(true);
                    }
                }else{
                    startNext(length, 0);
                }
            }
        }else{
            MLog.i("DOWN_SDK", "reponse is null");
        }
    }

    private void reset() {
        progress.set(0);
        result.set(true);
        executorSize.set(0);
        lastRealPercent = 0;
        if(null == listeners || listeners.size()<=0){
            listeners = DownloadListenerHolder.getListener(getUrl());
        }
    }

    public void startNext(int lenght, int index){
       for(int i = index ;i<THREAD_NUM;i++){
           DownloadExecutor executor = getExecutor(lenght, i);
           if(null != executor){
               executorSize.incrementAndGet();
               DownloadDAO.insertExecutors(executor);
               executors.add(executor);
               //FIXME 可以使用线程池
               new Thread(executor).start();
//               mExecutor.execute(executor);
           }else{
               MLog.i("DOWN_SDK", "execute is null...");
           }
           if(i == 0){
               break;
           }
       }
    }

    public DownloadExecutor getExecutor(int lenght, int index){
        if(index > THREAD_NUM-1){
            return null;
        }else{
            DownloadExecutor executor = new DownloadExecutor();
            executor.setListener(this);
            executor.setTask(this);
            executor.setIndex(index);
            executor.setKey(getKey());
            executor.setLength(lenght);
            if(index == THREAD_NUM - 1){
                executor.setBegin(index * (lenght/THREAD_NUM));
                executor.setEnd(lenght - 1);
            }else{
                executor.setBegin(index * (lenght/THREAD_NUM));
                executor.setEnd((index+1) * (lenght/THREAD_NUM) - 1);
            }
            MLog.i("DOWN_SDK", Thread.currentThread().getId() + "  executor is " + executor.toString());
            return executor;
        }
    }

    private Request getRequest(){
        Request r = new Request(url, null);
        return r;
    }

    public void stopDownload(){
        if(executors.size() > 0){
            for(DownloadExecutor executor : executors){
                executor.setStop(true);
            }
            executors.clear();
        }
    }

    public void setKey(String key) {
        this.key = key;
//        setOwer(key);
    }

    public void setUrl(String url) {
        this.url = url;
        setOwer(url);
    }

    public boolean isNeedMulti() {
        return isNeedMulti;
    }

    public void setNeedMulti(boolean needMulti) {
        isNeedMulti = needMulti;
    }

    public List<DownloadListener> getListeners() {
        return listeners;
    }

    public void setListeners(List<DownloadListener>listeners) {
        this.listeners = listeners;
    }

    public void addListener(DownloadListener listener){
        if(null == listeners){
            listeners = new ArrayList<DownloadListener>();
        }
        listener.setOwer(getUrl());
        listeners.add(listener);
    }

    public INetWork getNetWork() {
        return netWork;
    }

    public void setNetWork(INetWork netWork) {
        this.netWork = netWork;
    }

    public Object getLock() {
        return lock;
    }

    public void setLock(Object lock) {
        this.lock = lock;
    }
}

package com.download.lib;

import android.os.Looper;
import android.text.TextUtils;

import com.download.db.DownloadDAO;
import com.download.model.DownloadListener;
import com.download.model.Task;
import com.download.model.TaskQueue;
import com.download.util.MLog;

/**
 *
 * @author peiboning
 * @date 2018/3/13
 */

public class DownloadLooper implements Runnable {
    private TaskQueue mQueue;
    private Object mLock;
    private Task mCurrentTask;
    private boolean mIsStop;
    DownloadLooper(Object lock){
        mQueue = new TaskQueue();
        mLock = lock;
        mIsStop = true;
    }
    @Override
    public void run() {
        while (true){
            if(mIsStop){
                dealStop();
                break;
            }
            mQueue.update();//尝试去更新原有的任务
            if(mQueue.getTaskSize() <=0){
                mIsStop = true;
                dealStop();
                MLog.i("DOWN_SDK", "wait queue is empty, looper is stop ");
                break;
            }
            mCurrentTask = mQueue.getTask();
            if(mCurrentTask != null){
                dealTask();
            }
            synchronized (mLock){
                try {
                    mLock.wait();
                    MLog.i("DOWN_SDK", "start next task ");
                } catch (InterruptedException e) {
                    mIsStop = true;
                    e.printStackTrace();
                }
                if(!mIsStop){//正常下载完毕
                    mQueue.removeTask(mCurrentTask);
                }
            }
        }
        MLog.i("DOWN_SDK", "download looper is stop ");
    }

    private void dealTask() {
        mCurrentTask.setLock(mLock);
        mCurrentTask.begin();
    }

    public void stop(){
        mIsStop = true;
        synchronized (mLock){
            mLock.notifyAll();
        }
    }

    public void resume(boolean isManual){
        if(!mIsStop){
            MLog.i("DOWN_SDK", "now is downloading , not need start");
            return;
        }
        mIsStop = false;
        new Thread(this).start();
        MLog.i("DOWN_SDK", "resume download , isManual:" + isManual);
    }

    private void dealStop() {
        MLog.i("DOWN_SDK", "deal with stop ");
        if(null != mCurrentTask){
            mCurrentTask.stopDownload();
            mCurrentTask = null;
        }
    }

    public void putTask(final Task t){
        if(null != t && null != mCurrentTask){
            if(t.getKey().equals(mCurrentTask.getKey())){
                return;
            }
        }
        if(Looper.myLooper() == Looper.getMainLooper()){
            new Thread(){
                @Override
                public void run() {
                    putTask2QueueAndResume(t);
                }
            }.start();
        }else{
            putTask2QueueAndResume(t);
        }
    }

    private void putTask2QueueAndResume(Task t){
        DownloadDAO.insertTask(t);
        if(mQueue.isUpdatefromDB()){
            mQueue.addTask(t);
        }
        if(mIsStop){
            DownloadLooper.this.resume(false);
        }
    }

    public void addListener(String url, DownloadListener listener){
        if(TextUtils.isEmpty(url)){
            MLog.i("DOWN_SDK", "download url is empty , add listener failed");
            return ;
        }
        if(null == listener){
            MLog.i("DOWN_SDK", "listener is null,add failed ");
        }
        if(null != mCurrentTask){
            if(url.equals(mCurrentTask.getUrl())){
                mCurrentTask.addListener(listener);
            }
        }else{
            mQueue.addListener2Task(url, listener);
        }

    }
}

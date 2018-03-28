package com.download.model;

import com.download.db.DownloadDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author peiboning
 * @date 2018/3/13
 */

public class TaskQueue {
    private List<Task> mQueue;
    private AtomicBoolean update;

    public TaskQueue(){
        mQueue = new ArrayList<Task>();
        update = new AtomicBoolean(false);
    }

    public void addTask(Task t){
        synchronized (mQueue){
            boolean flag = true;
            if(mQueue.size() > 0){
                for(Task task : mQueue){
                    if(task.getKey().equals(t.getKey())){
                        flag = false;
                        break;
                    }
                }
            }
            if(flag){
                mQueue.add(t);
            }
        }
    }

    public Task getTask(){
        synchronized (mQueue){
            if(getTaskSize() > 0){
                return mQueue.get(0);
            }
        }
        return null;
    }

    public void removeTask(Task t){
        if(null != t){
            synchronized (mQueue){
                if(getTaskSize() > 0){
                    mQueue.remove(t);
                }
            }
        }
    }

    public void update(){
        if(!update.get()){
            List<Task> list = DownloadDAO.getTaskList();
            if(null != list && list.size()>0){
                for(Task task : list){
                    addTask(task);
                }
            }
            update.set(true);
        }
    }

    public boolean isUpdatefromDB(){
        return update.get();
    }

    public void addListener2Task(String url, DownloadListener listener){
        synchronized (mQueue){
            if(getTaskSize() > 0){
                for(Task task : mQueue){
                    if(url.equals(task.getUrl())){
                        task.addListener(listener);
                        break;
                    }
                }
            }
        }
    }

    public int getTaskSize(){
        return mQueue.size();
    }
}

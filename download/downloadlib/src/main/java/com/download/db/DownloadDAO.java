package com.download.db;

import android.content.ContentValues;
import android.database.Cursor;

import com.download.model.DownloadExecutor;
import com.download.model.Task;
import com.download.util.MLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peiboning on 2018/3/14.
 */

public class DownloadDAO {
    public static List<DownloadExecutor> getExecutors(String key){
        String sql = "select * from " + SqliteDB.ExecutorTable.TABLE_NAME +" where key=?";
        Cursor cursor = DownDb.getInstance().getWriteableDb().rawQuery(sql, new String[]{key});

        if(null != cursor){
            List<DownloadExecutor> list = new ArrayList<>();
            DownloadExecutor executor = null;
            while (cursor.moveToNext()){
                executor = new DownloadExecutor();
                executor.setKey(key);
                executor.setEnd(cursor.getInt(cursor.getColumnIndex(SqliteDB.ExecutorTable.END)));
                executor.setBegin(cursor.getInt(cursor.getColumnIndex(SqliteDB.ExecutorTable.BEGIN)));
                executor.setProgress(cursor.getInt(cursor.getColumnIndex(SqliteDB.ExecutorTable.PROGRESS)));
                executor.setIndex(cursor.getInt(cursor.getColumnIndex(SqliteDB.ExecutorTable.INDEX)));
                list.add(executor);
            }
            cursor.close();
            DownDb.getInstance().closeDb();
            return list;
        }
        return null;
    }

    public static void insertExecutors(DownloadExecutor executor){
        List<DownloadExecutor> list = getExecutors(executor.getKey());
        boolean flag = true;
        if(null != list && list.size()>0){
            for(DownloadExecutor e : list){
                if(e.getKey().equals(executor.getKey()) && e.getIndex() == executor.getIndex()){
                    flag = false;
                    break;
                }
            }
        }
        if(flag){
            ContentValues values = new ContentValues();
            values.put(SqliteDB.ExecutorTable.KEY, executor.getKey());
            values.put(SqliteDB.ExecutorTable.BEGIN, executor.getBegin());
            values.put(SqliteDB.ExecutorTable.END, executor.getEnd());
            values.put(SqliteDB.ExecutorTable.INDEX, executor.getIndex());
            values.put(SqliteDB.ExecutorTable.PROGRESS, executor.getProgress());
            values.put(SqliteDB.ExecutorTable.URL, executor.getUrl());

            DownDb.getInstance().getWriteableDb().insert(SqliteDB.ExecutorTable.TABLE_NAME, null, values);
            MLog.i("DOWN_SDK", "insert executors is : " + executor.toString());
        }else{
            MLog.i("DOWN_SDK", "not need insert executors is : " + executor.toString());
        }
    }

    public static void updateProgress(String key, int progress, int index){
        String sql = "update " + SqliteDB.ExecutorTable.TABLE_NAME + " set " + SqliteDB.ExecutorTable.PROGRESS + "=" + progress
                + " where "+ SqliteDB.ExecutorTable.KEY+"='" + key + "' and "+ SqliteDB.ExecutorTable.INDEX+"=" + index;
        DownDb.getInstance().getWriteableDb().execSQL(sql);
        DownDb.getInstance().closeDb();
    }

    public static int deleteExecutor(String key){
        int res = DownDb.getInstance().getWriteableDb().delete(SqliteDB.ExecutorTable.TABLE_NAME, SqliteDB.ExecutorTable.KEY +"=?", new String[]{key});
        DownDb.getInstance().closeDb();
        return res;
    }
    public static int deleteTask(String key){
        int res = DownDb.getInstance().getWriteableDb().delete(SqliteDB.TaskTable.TABLE_NAME, SqliteDB.TaskTable.KEY +"=?", new String[]{key});
        DownDb.getInstance().closeDb();
        return res;
    }

    public static List<Task> getTaskList(){
        String sql = "select * from " + SqliteDB.TaskTable.TABLE_NAME;
        Cursor cursor = DownDb.getInstance().getWriteableDb().rawQuery(sql, null);

        if(null != cursor){
            List<Task> list = new ArrayList<>();
            Task task = null;
            while (cursor.moveToNext()){
                task = new Task();
                task.setKey(cursor.getString(cursor.getColumnIndex(SqliteDB.TaskTable.KEY)));
                task.setUrl(cursor.getString(cursor.getColumnIndex(SqliteDB.TaskTable.URL)));
                list.add(task);
            }
            cursor.close();
            DownDb.getInstance().closeDb();
            return list;
        }
        return null;
    }

    public static boolean insertTask(Task task){
        List<Task> list = getTaskList();
        boolean flag = true;
        if(null != list && list.size() > 0){
            for(Task t : list){
                if(t.getKey().equals(task.getKey())){
                    flag = false;
                    break;
                }
            }
        }
        if(flag){
            ContentValues values = new ContentValues();
            values.put(SqliteDB.TaskTable.KEY, task.getKey());
            values.put(SqliteDB.TaskTable.URL, task.getUrl());

            long res = DownDb.getInstance().getWriteableDb().insert(SqliteDB.TaskTable.TABLE_NAME, null, values);
            return res > -1;
        }
        return false;
    }

}

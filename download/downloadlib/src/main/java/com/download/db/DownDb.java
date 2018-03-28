package com.download.db;

import android.database.sqlite.SQLiteDatabase;

import com.download.lib.Downloader;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author peiboning
 * @date 2018/3/14
 */

public class DownDb {
    private static DownDb sInstance;
    private SqliteDB mDbHelper;
    private SQLiteDatabase mRealDb;
    private AtomicInteger mDbNums;

    public static DownDb getInstance(){
        if(null == sInstance){
            synchronized (DownDb.class){
                if(null == sInstance){
                    sInstance = new DownDb();
                }
            }
        }
        return sInstance;
    }

    private DownDb(){
        mDbHelper = new SqliteDB(Downloader.getInstance().getContext(), "");
        mDbNums = new AtomicInteger(0);
    }

    public synchronized SQLiteDatabase getWriteableDb(){
        if(mDbNums.get() == 0){
            mRealDb = mDbHelper.getWritableDatabase();
        }
        mDbNums.incrementAndGet();
        return mRealDb;
    }

    public synchronized void closeDb(){
        if(mDbNums.decrementAndGet() == 0){
            mRealDb.close();
            mRealDb = null;
        }
    }
}

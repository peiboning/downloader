package com.download.lib;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by peiboning on 2018/3/13.
 */

public class ThreadPoll {
    private static ExecutorService mSingleExecutor;

    static {
        mSingleExecutor = Executors.newSingleThreadExecutor();
    }

    public static void execute(Runnable runnable){
        mSingleExecutor.execute(runnable);
    }
}

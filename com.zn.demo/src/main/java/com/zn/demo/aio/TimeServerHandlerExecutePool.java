package com.zn.demo.aio;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * TimeServerHandler
 *
 * @author ggzhangna
 * @date 20/6/25
 */
public class TimeServerHandlerExecutePool {

    private ExecutorService executorService;

    public TimeServerHandlerExecutePool(int maxPoolSize, int queueSize) {
        executorService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors()
                , maxPoolSize
                ,120L
                , TimeUnit.SECONDS
                , new ArrayBlockingQueue<Runnable>(queueSize));
    }

    public void execute(java.lang.Runnable task){
        executorService.execute(task);
    }
}

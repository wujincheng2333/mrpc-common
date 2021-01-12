package com.wujincheng.mrpccommon.invoke;

import com.wujincheng.mrpccommon.common.Response;
import com.wujincheng.mrpccommon.support.HashedWheelTimer;
import com.wujincheng.mrpccommon.support.Timeout;
import com.wujincheng.mrpccommon.support.Timer;
import com.wujincheng.mrpccommon.support.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MyDefaultFuture extends CompletableFuture<Object> {

    private static final Logger logger = LoggerFactory.getLogger(MyDefaultFuture.class);

    private static final Map<Long, MyDefaultFuture> FUTURES = new ConcurrentHashMap<>();

    private static final AtomicInteger mThreadNum = new AtomicInteger(1);

    private Timeout timeoutCheckTask;

    private final int timeout;

    private final Long id;

    public static final Timer TIME_OUT_TIMER = new HashedWheelTimer(
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t=new Thread(r);
                    t.setDaemon(true);
                    t.setName("fyl-myDefaultFuture-timeout-"+mThreadNum.getAndIncrement());
                    return t;
                }
            },
            30,
            TimeUnit.MILLISECONDS);

    public MyDefaultFuture( Long id,int timeout) {
        this.timeout = timeout;
        this.id = id;
    }

    public int getTimeout() {
        return timeout;
    }

    public static MyDefaultFuture newFuture(Long id,int timeout) {
        final MyDefaultFuture future = new MyDefaultFuture(id,timeout);
        FUTURES.put(id, future);
        TimeoutCheckTask task = new TimeoutCheckTask(id);
        future.timeoutCheckTask = TIME_OUT_TIMER.newTimeout(task, future.getTimeout(), TimeUnit.MILLISECONDS);
        return future;
    }

    public static boolean containsFuture(Long id) {
        return FUTURES.containsKey(id);
    }

    public static void received(Response response) {
        MyDefaultFuture future = FUTURES.remove(response.getId());
        if(future==null){
            return;
        }
        Timeout t = future.timeoutCheckTask;
        t.cancel();
        future.doReceived(response);
    }

    private void doReceived(Response response) {
        this.complete(response);
    }

    private static class TimeoutCheckTask implements TimerTask {

        private final Long requestID;

        TimeoutCheckTask(Long requestID) {
            this.requestID = requestID;
        }

        @Override
        public void run(Timeout timeout) {
            MyDefaultFuture future = FUTURES.get(requestID);
            if (future == null || future.isDone()) {
                return;
            }
            Timeout t = future.timeoutCheckTask;
            t.cancel();
        }
    }
}
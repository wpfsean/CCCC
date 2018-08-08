package com.zkth.mst.client.utils;

import android.content.Context;

import com.zkth.mst.client.callbacks.SendHearToServerThread;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Root on 2018/7/23.
 */

public class TaskSendHb implements Runnable {

    private volatile static TaskSendHb instance = null;
    private ScheduledExecutorService mScheduledExecutorService;
    private long time;

    private TaskSendHb() {
        mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void run() {
        LogUtils.i("TAG",new Date().toString());
        SendHearToServerThread sendHearToServerThread = new SendHearToServerThread();
        sendHearToServerThread.start();
    }

    public static TaskSendHb getInstance() {
        if (instance == null) {
            synchronized (TaskSendHb.class) {
                if (instance == null) {
                    instance = new TaskSendHb();
                }
            }
        }
        return instance;
    }

    public void init(Context context, long time) {
        this.time = time;
    }

    public void start() {
        mScheduledExecutorService.scheduleWithFixedDelay(this, 0L, time, TimeUnit.MILLISECONDS);
    }

}
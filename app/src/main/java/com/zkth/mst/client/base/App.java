package com.zkth.mst.client.base;

import android.app.Application;
import android.content.Intent;
import android.os.Build;

import com.zkth.mst.client.BuildConfig;
import com.zkth.mst.client.callbacks.BatteryAndWifiService;
import com.zkth.mst.client.callbacks.SendHbService;
import com.zkth.mst.client.utils.FileUtils;
import com.zkth.mst.client.utils.LogUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ZHT on 2017/4/17.
 * 自定义Application
 */

public class App extends Application {

    public static ExecutorService fixedThreadPool = null;
    private static App mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        //设置是否打印日志
        LogUtils.setIsLog(BuildConfig.LOG_DEBUG);
        fixedThreadPool = Executors.newFixedThreadPool(5);

        //启动服务监听电量和wifi信息
        startService(new Intent(this, BatteryAndWifiService.class));

        //在6.0(M)版本下直接创建应用对应的文件夹
        //在6.0(M)版本以上的需要先进行权限申请
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            FileUtils.init(this);
        }
    }

    public static App getApplication() {
        return mContext;
    }

    public static ExecutorService getExecutorService() {
        return fixedThreadPool;
    }

}

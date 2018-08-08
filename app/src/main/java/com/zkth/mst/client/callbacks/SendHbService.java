package com.zkth.mst.client.callbacks;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.zkth.mst.client.base.App;
import com.zkth.mst.client.utils.TaskSendHb;

/**
 * Created by Root on 2018/8/6.
 */

public class SendHbService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TaskSendHb.getInstance().init(App.getApplication(),15*1000);
        TaskSendHb.getInstance().start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

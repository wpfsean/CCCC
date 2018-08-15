package com.zkth.mst.client.utils;

import android.app.Activity;
import android.app.Service;
import android.os.Vibrator;

/**
 * Created by Root on 2018/8/15.
 *
 * 手机震动工具类
 */

public class VibratorUtils {

    public static void Vibrate(final Activity activity, long milliseconds) {
        Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(milliseconds);
    }
}

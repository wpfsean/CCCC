package com.zkth.mst.client.utils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Root on 2018/8/16.
 *
 * 网络时间回调
 *方便用于本地时间的调校
 */

public class NetWorkTimeCallback implements Runnable {

    String time = "";

    TimeCallback listern;

    public NetWorkTimeCallback(TimeCallback listern){
        this.listern = listern;
    }

    @Override
    public void run() {
        try {
            URL url = new URL("http://www.baidu.com");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int code = con.getResponseCode();
            long ld = con.getDate(); // 取得网站日期时间
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(ld);
            String format = formatter.format(calendar.getTime());
            time = format;
        } catch (Exception e) {
            time = "";
        }

        if (listern != null){
            listern.getTime(time);
        }
    }
    public void start(){
        new Thread(this).start();
    }

    public interface  TimeCallback{
        public void getTime(String str);
    }
}

package com.zkth.mst.client.callbacks;

import android.text.TextUtils;


import com.zkth.mst.client.base.DbConfig;
import com.zkth.mst.client.db.Logutils;
import com.zkth.mst.client.utils.ByteUtils;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * 接收短信信息的回调(须去掉空的字符，否则会出现乱码情况)
 * Created by Root on 2018/4/19.
 */

public class ReceiveServerMess implements Runnable {

    GetSmsListern listern;

    public ReceiveServerMess(GetSmsListern listern) {
        this.listern = listern;
    }

    @Override
    public void run() {
        int port =2000;
        DatagramSocket ds = null;
        String dbPort = DbConfig.getInstance().getData(6);
        if (!TextUtils.isEmpty(dbPort)){
            port = Integer.parseInt(dbPort);
        }
        try {
            while (true) {
                ds = new DatagramSocket(port);
                byte bytes[] = new byte[1024];
                DatagramPacket dp = new DatagramPacket(bytes, bytes.length);
                ds.receive(dp);
                byte[] sms = dp.getData();
                int position = ByteUtils.getPosiotion(sms);
                String result = new String(sms, 0, position, "gb2312") + "\n" + dp.getAddress().getHostAddress() + "\n" + dp.getPort();
                ds.close();
                if (!TextUtils.isEmpty(result)) {
                    if (listern != null) {
                        listern.getSmsContent(result);
                    }
                }
            }
        } catch (Exception e) {
            Logutils.e("sms异常：" + e.getMessage());
        }
    }

    public interface GetSmsListern {
        void getSmsContent(String ms);
    }
}

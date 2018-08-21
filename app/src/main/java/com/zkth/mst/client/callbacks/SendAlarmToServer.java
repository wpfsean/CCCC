package com.zkth.mst.client.callbacks;


import com.zkth.mst.client.base.AppConfig;
import com.zkth.mst.client.base.DbConfig;
import com.zkth.mst.client.db.Logutils;
import com.zkth.mst.client.entity.VideoBen;
import com.zkth.mst.client.utils.ByteUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

/**
 * 发送报警报文 到服务器地址
 * Created by Root on 2018/4/24.
 */

public class SendAlarmToServer extends Thread {

    VideoBen videoBen;
    Callback callback;

    public SendAlarmToServer(VideoBen videoBen, Callback callback) {
        this.videoBen = videoBen;
        this.callback = callback;
    }

    @Override
    public void run() {
        super.run();

        String alermIp = DbConfig.getInstance().getData(11);
        String alermPort = DbConfig.getInstance().getData(6);


        byte[] requestBys = new byte[580];
        String fl = AppConfig.alarm_header_id;//数据头
        byte[] zd = fl.getBytes();
        System.arraycopy(zd, 0, requestBys, 0, 4);
        //sender ip
        byte[] id = new byte[32];
        byte[] ipByte = new byte[0];
        try {
            ipByte = videoBen.getIp().getBytes(AppConfig.dataFormat);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //id = ipByte;
        System.arraycopy(ipByte, 0, id, 0, ipByte.length);
        System.arraycopy(id, 0, requestBys, 4, 32);

        //报文内id
        byte[] id1 = new byte[48];
        byte[] id2 = videoBen.getId().getBytes();
        System.arraycopy(id2, 0, id1, 0, id2.length);
        System.arraycopy(id1, 0, requestBys, 40, 48);

        //报文内name
        byte[] name = new byte[128];
        byte[] name1 = new byte[0];
        try {
            name1 = videoBen.getName().getBytes(AppConfig.dataFormat);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.arraycopy(name1, 0, name, 0, name1.length);
        System.arraycopy(name, 0, requestBys, 88, 128);

        //报文内devicetype
        byte[] deviceType = new byte[16];
        byte[] deviceType1 = videoBen.getDevicetype().getBytes();
        System.arraycopy(deviceType1, 0, deviceType, 0, deviceType1.length);
        System.arraycopy(deviceType, 0, requestBys, 216, 16);

        //报文内iPAddress
        byte[] iPAddress = new byte[32];
        byte[] iPAddress1 = videoBen.getIp().getBytes();
        System.arraycopy(iPAddress1, 0, iPAddress, 0, iPAddress1.length);
        System.arraycopy(iPAddress, 0, requestBys, 232, 32);

        //报文内的port
        byte[] port = new byte[4];
        byte[] port1 = ByteUtils.toByteArray(80);
        System.arraycopy(port1, 0, port, 0, port1.length);
        System.arraycopy(port, 0, requestBys, 264, 4);

        //报文内的port
        byte[] channel = new byte[128];
        byte[] channel1 = videoBen.getPort().getBytes();
        System.arraycopy(channel1, 0, channel, 0, channel1.length);
        System.arraycopy(channel, 0, requestBys, 268, 128);

        //报文内的username
        byte[] username = new byte[32];
        byte[] username1 = videoBen.getUsername().getBytes();
        System.arraycopy(username1, 0, username, 0, username1.length);
        System.arraycopy(username, 0, requestBys, 396, 32);

        //报文内的password
        byte[] password = new byte[32];
        byte[] password1 = videoBen.getPassword().getBytes();
        System.arraycopy(password1, 0, password, 0, password1.length);
        System.arraycopy(password, 0, requestBys, 428, 32);


        // AlertType
        byte[] alertType = new byte[32];
        byte[] alertS = new byte[32];
        try {
            alertS = AppConfig.alertType.getBytes(AppConfig.dataFormat);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.arraycopy(alertS, 0, alertType, 0, alertS.length);
        System.arraycopy(alertType, 0, requestBys, 460, 32);
        //预留字节
        byte[] reserved = new byte[32];
        System.arraycopy(reserved, 0, requestBys, 492, 32);

        Socket socket = null;
        OutputStream os = null;
        try {
            socket = new Socket(alermIp,Integer.parseInt(alermPort));

            os = socket.getOutputStream();
            os.write(requestBys);
            os.flush();
            InputStream in = socket.getInputStream();
            byte[] headers = new byte[4];
            int read = in.read(headers);

            byte[] flag = new byte[4];
            for (int i = 0; i < 4; i++) {
                flag[i] = headers[i];
            }
            int status = ByteUtils.bytesToInt(flag, 0);
            Logutils.i(status + "-------------报警信息-----------");

            if (callback != null) {
                callback.getCallbackData("状态:" + status);
            }
        } catch (IOException e) {

            String err = e.getMessage();
            Logutils.i(err);
            if (callback != null) {
                callback.getCallbackData("状态error:" + err);
            }

            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public interface Callback {
        public void getCallbackData(String result);
    }
}

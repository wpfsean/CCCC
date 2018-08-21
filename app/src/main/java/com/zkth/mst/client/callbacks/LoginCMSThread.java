package com.zkth.mst.client.callbacks;


import com.zkth.mst.client.base.App;
import com.zkth.mst.client.base.AppConfig;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Root on 2018/8/5.
 *
 *用于验证cms服务登录 功能
 *
 * 判断返回的video的count是否大于0，
 * >0成功
 * =0 失败
 */

public class LoginCMSThread implements Runnable {

    String name;
    String pass;
    String nativeIp;
    String serverIp;
    LoginCallback callback;


    public LoginCMSThread(String name, String pass, String nativeIp, String serverIp, LoginCallback callback){
        this.name = name;
        this.pass = pass;
        this.nativeIp = nativeIp;
        this.serverIp = serverIp;
        this.callback = callback;

    }

    @Override
    public void run() {
        Socket socket = null;
        InputStream is = null;//读取输入流
        try {
            byte[] bys = new byte[140];
            String fl = AppConfig.video_header_id;
            byte[] zk = fl.getBytes();
            for (int i = 0; i < zk.length; i++) {
                bys[i] = zk[i];
            }
            //action 1（获取资源列表------需查看文档，根据实际的要求写入Action----------）
            bys[4] = 1;
            bys[5] = 0;
            bys[6] = 0;
            bys[7] = 0;
            //用户名列表
            String parameters = name+"/"+pass+"/"+nativeIp+"/0";
            byte[] na = parameters.getBytes(AppConfig.dataFormat);
            for (int i = 0; i < na.length; i++) {
                bys[i + 8] = na[i];
            }
            //socket请求
            socket = new Socket(serverIp, 2010);
            socket.setSoTimeout(6*1000);
            OutputStream os = socket.getOutputStream();
            os.write(bys);
            os.flush();
            is = socket.getInputStream();
            //获取前8个byte
            byte[] headers = new byte[188];
            int read = is.read(headers);
           if (callback != null){
               callback.getLoginStatus(headers[4]);
           }

        } catch (Exception e) {
            if (callback !=null){
                callback.getLoginStatus(0);
            }
        }
    }
    public void start(){
        App.getExecutorService().execute(this);
    }
    //回调
    public interface LoginCallback{
        public void  getLoginStatus(int count);
    }
}

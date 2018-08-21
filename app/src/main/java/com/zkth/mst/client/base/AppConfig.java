package com.zkth.mst.client.base;

/**
 * Created by Root on 2018/8/5.
 */

public class AppConfig {


    public AppConfig() {
        throw  new UnsupportedOperationException("不能被实例化");
    }

    //请求视频资源的数据头
    public static String video_header_id = "ZKTH";
    //请求数据的编码格式
    public static String dataFormat = "GB2312";
    //开箱申请的数据头
    public static  String ammo_header_id = "ReqB";
    //心跳协议标识 头
    public static  String heart_header_id = "ZDHB";
    //服务端口
    public static int server_port = 2010;

    //报警协议头
    public static  String alarm_header_id = "ATIF";

    //发送报文地ip和port
    public static String alarm_server_ip = "19.0.0.27";
    //心跳端口
    public static int alarm_server_port = 2000;

    //当前设备的guid
    public static String native_Guid = "{1ae41588-0a4e-4838-bef5-5980e322ef54}";
    //当前的设备名称
    public static String nativeDeviceName = "";
    //报警类型
    public static String alertType = "社会大哥打人了";

    public static double cpu = 0;
    public static double ram = 0;

    public static int battery = 0;
    public static int wifi = 0;

    public static double lat = 0;
    public static double log = 0;

    public static boolean isMainStream = false;
    public static boolean isVideoSound = false;


    public static String sipName = "";
    public static String sipNum = "";
    public static String sipPwd = "";
    public static String sipServer = "";


    public static String updateApkURl = "http://wesk.top/zhketech/auto_update/auto_update_apk.php";


}

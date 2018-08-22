package com.zkth.mst.client.entity;

import java.io.Serializable;

/**
 * Created by Root on 2018/8/5.
 *
 * 用于整个项目配置的数据
 *
 */

public class User implements Serializable {

    private String login_time;//登录的时间点
    private String name;//当前的登录者
    private String pass;//登录者的密码
    private String nativeip;//本机ip
    private String header_port;//心跳端口
    private String login_port;//登录端口
    private String alarm_port;//心跳端口
    private String sip_name;//sip名称
    private String sip_num;//sip号码
    private String sip_server;//sip服务器地址
    private String sip_pwd;//sip密码
    private String alarm_ip;//报警Ip
    private String serverip;//服务ip
    private String guid;//设备的guid
    private String deviceName;//当前的设备名称

    public User() {
    }

    @Override
    public String toString() {
        return "User{" +
                "login_time='" + login_time + '\'' +
                ", name='" + name + '\'' +
                ", pass='" + pass + '\'' +
                ", nativeip='" + nativeip + '\'' +
                ", header_port='" + header_port + '\'' +
                ", login_port='" + login_port + '\'' +
                ", alarm_port='" + alarm_port + '\'' +
                ", sip_name='" + sip_name + '\'' +
                ", sip_num='" + sip_num + '\'' +
                ", sip_server='" + sip_server + '\'' +
                ", sip_pwd='" + sip_pwd + '\'' +
                ", alarm_ip='" + alarm_ip + '\'' +
                ", serverip='" + serverip + '\'' +
                ", guid='" + guid + '\'' +
                ", deviceName='" + deviceName + '\'' +
                '}';
    }

    public String getLogin_time() {
        return login_time;
    }

    public void setLogin_time(String login_time) {
        this.login_time = login_time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getNativeip() {
        return nativeip;
    }

    public void setNativeip(String nativeip) {
        this.nativeip = nativeip;
    }

    public String getHeader_port() {
        return header_port;
    }

    public void setHeader_port(String header_port) {
        this.header_port = header_port;
    }

    public String getLogin_port() {
        return login_port;
    }

    public void setLogin_port(String login_port) {
        this.login_port = login_port;
    }

    public String getAlarm_port() {
        return alarm_port;
    }

    public void setAlarm_port(String alarm_port) {
        this.alarm_port = alarm_port;
    }

    public String getSip_name() {
        return sip_name;
    }

    public void setSip_name(String sip_name) {
        this.sip_name = sip_name;
    }

    public String getSip_num() {
        return sip_num;
    }

    public void setSip_num(String sip_num) {
        this.sip_num = sip_num;
    }

    public String getSip_server() {
        return sip_server;
    }

    public void setSip_server(String sip_server) {
        this.sip_server = sip_server;
    }

    public String getSip_pwd() {
        return sip_pwd;
    }

    public void setSip_pwd(String sip_pwd) {
        this.sip_pwd = sip_pwd;
    }

    public String getAlarm_ip() {
        return alarm_ip;
    }

    public void setAlarm_ip(String alarm_ip) {
        this.alarm_ip = alarm_ip;
    }

    public String getServerip() {
        return serverip;
    }

    public void setServerip(String serverip) {
        this.serverip = serverip;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}

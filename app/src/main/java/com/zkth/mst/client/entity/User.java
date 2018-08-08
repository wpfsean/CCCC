package com.zkth.mst.client.entity;

import java.io.Serializable;

/**
 * Created by Root on 2018/8/5.
 */

public class User implements Serializable {
//   + "login_time" + " TEXT,"
//           + "name" + " TEXT,"
//           + "pass" + " TEXT,"
//           + "nativeip" + " TEXT,"
//           + "header_port" + " TEXT,"
//           + "login_port" + " TEXT,"
//           + "alarm_port" + " TEXT,"
//           + "sip_name" + " TEXT,"
//           + "sip_num" + " TEXT,"
//           + "sip_server" + " TEXT,"
//           + "sip_pwd" + " TEXT,"
//           + "alarm_ip" + " TEXT,"
//           + "serverip" + " TEXT)";

    private String login_time;
    private String name;
    private String pass;
    private String nativeip;
    private String header_port;
    private String login_port;
    private String alarm_port;
    private String sip_name;
    private String sip_num;
    private String sip_server;
    private String sip_pwd;
    private String alarm_ip;
    private String serverip;


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
                '}';
    }

    public User() {
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
}

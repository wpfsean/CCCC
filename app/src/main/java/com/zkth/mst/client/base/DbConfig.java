package com.zkth.mst.client.base;

import android.database.Cursor;

import com.zkth.mst.client.db.DatabaseHelper;
import com.zkth.mst.client.utils.TimeDo;

import java.util.concurrent.Executors;

/**
 * Created by Root on 2018/8/8.
 *
 * 数据库封装类
 *
 *
 * <p>
 * Logutils.i(DbConfig.getInstance().getData(0)+"当前用户名");
 * Logutils.i(DbConfig.getInstance().getData(1)+"当前密码");
 * Logutils.i(DbConfig.getInstance().getData(2)+"用户登录时间");
 * Logutils.i(DbConfig.getInstance().getData(3)+"本机ip");
 * Logutils.i(DbConfig.getInstance().getData(4)+"心跳端口");
 * Logutils.i(DbConfig.getInstance().getData(5)+"登录端口");
 * Logutils.i(DbConfig.getInstance().getData(6)+"报警端口");
 * Logutils.i(DbConfig.getInstance().getData(7)+"当前sip名称");
 * Logutils.i(DbConfig.getInstance().getData(8)+"当前sip号码");
 * Logutils.i(DbConfig.getInstance().getData(9)+"sip服务器地址");
 * Logutils.i(DbConfig.getInstance().getData(10)+"sip密码");
 * Logutils.i(DbConfig.getInstance().getData(11)+"报警ip");
 * Logutils.i(DbConfig.getInstance().getData(12)+"服务器ip");
 */

public class DbConfig {
    static DatabaseHelper databaseHelper;
    private volatile static DbConfig instance = null;

    private DbConfig() {
    }

    public static DbConfig getInstance() {
        if (instance == null) {
            synchronized (DbConfig.class) {
                if (instance == null) {
                    instance = new DbConfig();
                    databaseHelper = new DatabaseHelper(App.getApplication());
                }
            }
        }
        return instance;
    }

    public String getData(int type) {
        try {
            Cursor cursor = databaseHelper.getUserCursor();
            String data = "";
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();

                    switch (type) {
                        case 0: //当前用户名
                            data = cursor.getString(cursor.getColumnIndex("name"));
                            break;
                        case 1://当前密码
                            data = cursor.getString(cursor.getColumnIndex("pass"));
                            break;
                        case 2://用户登录时间
                            data = cursor.getString(cursor.getColumnIndex("login_time"));
                            break;
                        case 3://本机ip
                            data = cursor.getString(cursor.getColumnIndex("nativeip"));
                            break;
                        case 4://心跳端口
                            data = cursor.getString(cursor.getColumnIndex("header_port"));
                            break;
                        case 5://登录端口
                            data = cursor.getString(cursor.getColumnIndex("login_port"));
                            break;
                        case 6://报警端口
                            data = cursor.getString(cursor.getColumnIndex("alarm_port"));
                            break;
                        case 7://当前sip名称
                            data = cursor.getString(cursor.getColumnIndex("sip_name"));
                            break;
                        case 8://当前sip号码
                            data = cursor.getString(cursor.getColumnIndex("sip_num"));
                            break;
                        case 9://sip服务器地址
                            data = cursor.getString(cursor.getColumnIndex("sip_server"));
                            break;
                        case 10://sip密码
                            data = cursor.getString(cursor.getColumnIndex("sip_pwd"));
                            break;
                        case 11://报警ip
                            data = cursor.getString(cursor.getColumnIndex("alarm_ip"));
                            break;
                        case 12://服务器ip
                            data = cursor.getString(cursor.getColumnIndex("serverip"));
                            break;
                    }
                } else {
                    return "";
                }
            }
            return  data;
        }catch (Exception e){

        }
        return "";
    }
}

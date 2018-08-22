package com.zkth.mst.client.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zkth.mst.client.entity.User;
import com.zkth.mst.client.utils.LogUtils;

/**
 * Created by Root on 2018/8/5.
 * <p>
 * User user = new User();
 * user.setLoginTime(new Date().toString());
 * user.setName("admin");
 * user.setPass("pass");
 * user.setNativeIp("19.0.0.79");
 * DatabaseHelper databaseHelper = new DatabaseHelper(LoginActivity.this);
 * databaseHelper.insertOneUser(user);
 * databaseHelper.getFirstUser();
 *
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    private SQLiteDatabase db;// 数据库

    public DatabaseHelper(Context context) {
        super(context, "zkth_p.db", null, 1);
        db = this.getWritableDatabase();
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql1 = "CREATE TABLE " + "users" + " (" + "_id"
                + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "login_time" + " TEXT,"
                + "name" + " TEXT,"
                + "pass" + " TEXT,"
                + "nativeip" + " TEXT,"
                + "header_port" + " TEXT,"
                + "login_port" + " TEXT,"
                + "alarm_port" + " TEXT,"
                + "sip_name" + " TEXT,"
                + "sip_num" + " TEXT,"
                + "sip_server" + " TEXT,"
                + "sip_pwd" + " TEXT,"
                + "alarm_ip" + " TEXT,"
                + "serverip" + " TEXT,"
                + "guid" + " TEXT,"
                + "device_name" + " TEXT)";
        sqLiteDatabase.execSQL(sql1);


        String sql2 = "CREATE TABLE " + "chat" + " (" + "_id"
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + "time" + " TEXT," +
                "fromuser" + " TEXT,"+ "message" + " TEXT,"
                + "touser" + " TEXT)";

        sqLiteDatabase.execSQL(sql2);
    }






    public void insertOneUser(User u) {
        Cursor cursor =db.query("users", null, "name =? and pass =? and serverip = ?", new String[]{u.getName(), u.getPass(), u.getServerip()}, null, null, null);
        if (cursor != null){
            if (cursor.getCount()>0){
                Logutils.i("已存在");
            }else {
                ContentValues contentValues = new ContentValues();
                contentValues.put("login_time",u.getLogin_time());
                contentValues.put("name",u.getName());
                contentValues.put("pass",u.getPass());
                contentValues.put("nativeip",u.getNativeip());
                contentValues.put("header_port",u.getHeader_port());
                contentValues.put("login_port",u.getLogin_port());
                contentValues.put("alarm_port",u.getAlarm_port());
                contentValues.put("sip_name",u.getSip_name());
                contentValues.put("sip_num",u.getSip_num());
                contentValues.put("sip_server",u.getSip_server());
                contentValues.put("sip_pwd",u.getSip_pwd());
                contentValues.put("alarm_ip",u.getAlarm_ip());
                contentValues.put("serverip",u.getServerip());
                contentValues.put("guid",u.getGuid());
                contentValues.put("device_name",u.getDeviceName());
                db.insert("users",null,contentValues);
                Logutils.i("插入成功");
            }
        }
    }



    public Cursor  getUserCursor(){
        Cursor cursor = db.query("users",null,null,null,null,null,null);
        if (cursor != null){
            if (cursor.getCount()>0){
                return cursor;
            }
        }
        return null;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


}

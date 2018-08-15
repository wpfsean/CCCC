package com.zkth.mst.client.ui.activity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.zkth.mst.client.R;
import com.zkth.mst.client.adapter.MyListAdapter;
import com.zkth.mst.client.adapter.MySubListAdapter;
import com.zkth.mst.client.base.BaseActivity;
import com.zkth.mst.client.base.DbConfig;
import com.zkth.mst.client.db.DatabaseHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingCenterActivity extends BaseActivity {


    private ListView listView;
    private ListView subListView;
    private MyListAdapter myAdapter;
    private MySubListAdapter subAdapter;
    SQLiteDatabase db;
    String sub_categories[][] = new String[][]{
            new String[]{"报警Ip:" + DbConfig.getInstance().getData(11), "报警端口:" + DbConfig.getInstance().getData(6), ""},
            new String[]{"心跳Ip:" + DbConfig.getInstance().getData(12), "心跳端口:" + DbConfig.getInstance().getData(4), ""},
            new String[]{"中心Ip:" + DbConfig.getInstance().getData(12), "中心端口:" + DbConfig.getInstance().getData(5), ""},
    };
    String categories[] = new String[]{"报警设置", "心跳设置", "中心设置"};

    @Override
    public void initView() {

    }

    @Override
    public void initData() {


        DatabaseHelper databaseHelper = new DatabaseHelper(SettingCenterActivity.this);
        db = databaseHelper.getWritableDatabase();


        listView = (ListView) findViewById(R.id.listView);
        subListView = (ListView) findViewById(R.id.subListView);
        myAdapter = new MyListAdapter(getApplicationContext(), categories);
        listView.setAdapter(myAdapter);
        setSubList(0);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                setSubList(position);
            }
        });

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_setting_center;
    }


    public void setSubList(int position) {
        final int location = position;
        myAdapter.setSelectedPosition(position);
        myAdapter.notifyDataSetInvalidated();
        subAdapter = new MySubListAdapter(getApplicationContext(), sub_categories,
                position);
        subListView.setAdapter(subAdapter);
        subListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                Toast.makeText(getApplicationContext(),
                        sub_categories[location][position], Toast.LENGTH_SHORT)
                        .show();
                settData(location, position);
            }
        });
    }


    public void settData(final int location, final int position) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingCenterActivity.this);
                final EditText editText = new EditText(SettingCenterActivity.this);
                if (location == 0) {
                    builder.setTitle("报警设置");
                    if (position == 0) {
                        builder.setView(editText).setPositiveButton("保存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String ip = editText.getText().toString().trim();
                                ContentValues contentValues = new ContentValues();
                                contentValues.put("alarm_ip", ip);
                                db.update("users", contentValues, "_id = ?", new String[]{"1"});
                                SettingCenterActivity.this.finish();
                                startActivity(new Intent(SettingCenterActivity.this,SettingCenterActivity.class));
                            }
                        }).create().show();
                    } else if (position == 1) {
                        builder.setView(editText).setPositiveButton("保存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String port = editText.getText().toString().trim();
                                ContentValues contentValues = new ContentValues();
                                contentValues.put("alarm_port", port);
                                db.update("users", contentValues, "_id = ?", new String[]{"1"});
                                SettingCenterActivity.this.finish();
                                startActivity(new Intent(SettingCenterActivity.this,SettingCenterActivity.class));
                            }
                        }).create().show();

                    }
                } else if (location == 1) {
                    builder.setTitle("心跳设置");
                    if (position == 0) {
                        builder.setView(editText).setPositiveButton("保存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String ip = editText.getText().toString().trim();
                                ContentValues contentValues = new ContentValues();
                                contentValues.put("serverip", ip);
                                db.update("users", contentValues, "_id = ?", new String[]{"1"});
                                SettingCenterActivity.this.finish();
                                startActivity(new Intent(SettingCenterActivity.this,SettingCenterActivity.class));
                            }
                        }).create().show();
                    } else if (position == 1) {
                        builder.setView(editText).setPositiveButton("保存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String port = editText.getText().toString().trim();
                                ContentValues contentValues = new ContentValues();
                                contentValues.put("header_port", port);
                                db.update("users", contentValues, "_id = ?", new String[]{"1"});
                                SettingCenterActivity.this.finish();
                                startActivity(new Intent(SettingCenterActivity.this,SettingCenterActivity.class));
                            }
                        }).create().show();

                    }
                } else if (location == 2) {
                    builder.setTitle("中心设置");
                    if (position == 0) {
                        builder.setView(editText).setPositiveButton("保存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String ip = editText.getText().toString().trim();
                                ContentValues contentValues = new ContentValues();
                                contentValues.put("serverip", ip);
                                db.update("users", contentValues, "_id = ?", new String[]{"1"});
                                SettingCenterActivity.this.finish();
                                startActivity(new Intent(SettingCenterActivity.this,SettingCenterActivity.class));
                            }
                        }).create().show();
                    } else if (position == 1) {
                        builder.setView(editText).setPositiveButton("保存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String port = editText.getText().toString().trim();
                                ContentValues contentValues = new ContentValues();
                                contentValues.put("login_port", port);
                                db.update("users", contentValues, "_id = ?", new String[]{"1"});
                                SettingCenterActivity.this.finish();
                                startActivity(new Intent(SettingCenterActivity.this,SettingCenterActivity.class));
                            }
                        }).create().show();

                    }
                }
            }
        });


    }

    public static boolean isboolIp(String ipAddress) {
        String ip = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pattern = Pattern.compile(ip);
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }
}

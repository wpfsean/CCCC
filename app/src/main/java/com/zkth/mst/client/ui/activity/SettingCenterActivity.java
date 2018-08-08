package com.zkth.mst.client.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.zkth.mst.client.R;
import com.zkth.mst.client.adapter.MyListAdapter;
import com.zkth.mst.client.adapter.MySubListAdapter;
import com.zkth.mst.client.base.BaseActivity;
import com.zkth.mst.client.base.DbConfig;

public class SettingCenterActivity extends BaseActivity {


    private ListView listView;
    private ListView subListView;
    private MyListAdapter myAdapter;
    private MySubListAdapter subAdapter;
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
            }
        });
    }
}

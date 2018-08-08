package com.zkth.mst.client.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zkth.mst.client.R;
import com.zkth.mst.client.adapter.MyPagerAdapter;
import com.zkth.mst.client.base.BaseActivity;
import com.zkth.mst.client.callbacks.ReceiveServerMess;
import com.zkth.mst.client.callbacks.ReceiverServerAlarm;
import com.zkth.mst.client.db.DatabaseHelper;
import com.zkth.mst.client.db.Logutils;
import com.zkth.mst.client.entity.AlarmBen;
import com.zkth.mst.client.ui.fragment.ChatListFragment;
import com.zkth.mst.client.ui.fragment.IntercomFragment;
import com.zkth.mst.client.ui.fragment.VoideoFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class MainFragmentActivity extends BaseActivity {

    //页面集合
    private List<Fragment> list = new ArrayList<>();
    //viewpager
    @BindView(R.id.main_view_pager)
    ViewPager mViewPager;
    int currentPager = 0;
    boolean threadIsRun = true;
    @BindView(R.id.bottom_intercom_btn)
    RadioButton bottom_intercom_btn;
    @BindView(R.id.bottom_chat_btn)
    RadioButton bottom_chat_btn;
    @BindView(R.id.bottom_video_btn)
    RadioButton bottom_video_btn;

    @BindView(R.id.no_network_layout)
    RelativeLayout no_network_layout;

    @BindView(R.id.current_fragment_name)
    TextView current_fragment_name;

    @BindView(R.id.mainfragment_time_layout)
    TextView mainfragment_time_layout;


    String[] title = new String[]{"Sip通话", "视频监控", "SIp通信"};

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1001) {
                long time = System.currentTimeMillis();
                Date date = new Date(time);
                SimpleDateFormat timeD = new SimpleDateFormat("HH:mm:ss");
                String currentTime = timeD.format(date).toString();
                if (!TextUtils.isEmpty(currentTime)) {
                    if (mainfragment_time_layout != null)
                        mainfragment_time_layout.setText(currentTime);
                }
            }
        }
    };

    @Override
    public void initView() {
        TimeThread timeThread = new TimeThread();
        new Thread(timeThread).start();
    }

    @Override
    public void initData() {

        list.add(new IntercomFragment());
        list.add(new VoideoFragment());
        list.add(new ChatListFragment());
        final MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager(), list);
        mViewPager.setAdapter(adapter);

        currentPager = getIntent().getIntExtra("current", 0);
        mViewPager.setCurrentItem(currentPager);
        Logutils.i("current:"+currentPager);
        if (currentPager == 0) {
            current_fragment_name.setText(title[0]);
            bottom_intercom_btn.setChecked(true);
            bottom_chat_btn.setChecked(false);
            bottom_video_btn.setChecked(false);
        } else if (currentPager == 1) {
            current_fragment_name.setText(title[1]);
            bottom_intercom_btn.setChecked(false);
            bottom_chat_btn.setChecked(true);
            bottom_video_btn.setChecked(false);
        } else if (currentPager == 2) {
            current_fragment_name.setText(title[2]);
            bottom_intercom_btn.setChecked(false);
            bottom_chat_btn.setChecked(false);
            bottom_video_btn.setChecked(true);
        }


        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {

                Logutils.i("position:"+position);
                if (position == 0) {
                    bottom_intercom_btn.setChecked(true);
                    bottom_chat_btn.setChecked(false);
                    bottom_video_btn.setChecked(false);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            current_fragment_name.setText(title[0]);
                        }
                    });
                } else if (position == 1) {
                    bottom_intercom_btn.setChecked(false);
                    bottom_chat_btn.setChecked(true);
                    bottom_video_btn.setChecked(false);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            current_fragment_name.setText(title[1]);
                        }
                    });
                } else if (position == 2) {
                    bottom_intercom_btn.setChecked(false);
                    bottom_chat_btn.setChecked(false);
                    bottom_video_btn.setChecked(true);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            current_fragment_name.setText(title[2]);
                        }
                    });
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_main_fragment;
    }


    @OnClick({R.id.bottom_intercom_btn, R.id.bottom_chat_btn, R.id.bottom_video_btn})
    public void radioClickEvent(View view) {
        if (view.getId() == R.id.bottom_intercom_btn) {
            mViewPager.setCurrentItem(0);
            bottom_intercom_btn.setChecked(true);
            bottom_chat_btn.setChecked(false);
            bottom_video_btn.setChecked(false);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    current_fragment_name.setText(title[0]);
                }
            });
        } else if (view.getId() == R.id.bottom_chat_btn) {
            mViewPager.setCurrentItem(1);
            bottom_intercom_btn.setChecked(false);
            bottom_chat_btn.setChecked(true);
            bottom_video_btn.setChecked(false);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    current_fragment_name.setText(title[1]);
                }
            });
        } else if (view.getId() == R.id.bottom_video_btn) {
            mViewPager.setCurrentItem(2);
            bottom_intercom_btn.setChecked(false);
            bottom_chat_btn.setChecked(false);
            bottom_video_btn.setChecked(true);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    current_fragment_name.setText(title[2]);
                }
            });
        }
    }

    @Override
    public void onNetChange(int state, String name) {
        if (state != 0 && state != 1) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    no_network_layout.setVisibility(View.VISIBLE);
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    no_network_layout.setVisibility(View.GONE);
                }
            });
        }
    }


    //显示时间的线程
    class TimeThread extends Thread {
        @Override
        public void run() {
            super.run();
            do {
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = 1001;
                    handler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (threadIsRun);
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MainFragmentActivity.this.finish();
    }
}

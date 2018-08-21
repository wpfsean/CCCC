package com.zkth.mst.client.ui.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zkth.mst.client.R;
import com.zkth.mst.client.base.AppConfig;
import com.zkth.mst.client.base.BaseFragment;
import com.zkth.mst.client.callbacks.OnMultiTouchListener;
import com.zkth.mst.client.callbacks.SendAlarmToServer;
import com.zkth.mst.client.db.Logutils;
import com.zkth.mst.client.entity.VideoBen;
import com.zkth.mst.client.onvif.Device;
import com.zkth.mst.client.utils.ControlPtzUtils;
import com.zkth.mst.client.utils.GsonUtils;
import com.zkth.mst.client.utils.PageModel;
import com.zkth.mst.client.utils.SharedPreferencesUtils;
import com.zkth.mst.client.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.nodemedia.NodePlayer;
import cn.nodemedia.NodePlayerDelegate;
import cn.nodemedia.NodePlayerView;


/**
 * @author Wpf
 * @version v1.0
 * @date：2016-5-4 下午5:14:58
 */
public class VoideoFragment extends BaseFragment implements NodePlayerDelegate, View.OnTouchListener {

    /**
     * 四分屏分页加载器
     */
    PageModel fourPm;

    /**
     * 单屏分页加载器
     */
    PageModel singlePm;

    /**
     * 记录当前的页码
     */
    int videoCurrentPage = 1;

    //四分屏按钮
    @BindView(R.id.four_screen_button_select)
    ImageButton four_screen_button_select;
    //单屏按钮
    @BindView(R.id.single_screen_button_selecte)
    ImageButton single_screen_button_selecte;
    //停止或重新播放按钮
    @BindView(R.id.paly_or_stop_button_select)
    ImageButton paly_or_stop_button_select;

    /**
     * 数据集合
     */
    List<Device> devicesList = new ArrayList<>();

    /**
     * 盛放当前四分屏数据的集合
     */
    List<Device> currentList = new ArrayList<>();

    /**
     * 盛放单屏数据的集合
     */
    List<Device> currentSingleList = new ArrayList<>();

    /**
     * 单屏播放的播放器
     */
    NodePlayer singlePlayer;

    /**
     * 四分屏的四个播放器
     */
    NodePlayer firstPalyer, secondPlayer, thirdPlayer, fourthPlayer;

    //单屏显示的player布局
    @BindView(R.id.single_player_layout)
    NodePlayerView single_player_layout;

    //第一个视频的view
    @BindView(R.id.first_player_layout)
    NodePlayerView firstPlayerView;

    //第一个加载进度条
    @BindView(R.id.first_pr_layout)
    ProgressBar first_pr_layout;

    //第一个视频 的loading
    @BindView(R.id.first_dispaly_loading_layout)
    TextView first_dispaly_loading_layout;

    //第一个视频所在的背景而
    @BindView(R.id.first_surfaceview_relativelayout)
    public RelativeLayout first_surfaceview_relativelayout;

    //第二个视频的view
    @BindView(R.id.second_player_layout)
    NodePlayerView secondPlayerView;

    //第二个视频所在的背景而
    @BindView(R.id.second_surfaceview_relativelayout)
    public RelativeLayout second_surfaceview_relativelayout;

    //第二个加载进度条
    @BindView(R.id.second_pr_layout)
    ProgressBar second_pr_layout;
    //第一个视频 的loading
    @BindView(R.id.seond_dispaly_loading_layout)
    TextView second_dispaly_loading_layout;

    //第三个视频的view
    @BindView(R.id.third_player_layout)
    NodePlayerView thirdPlayerView;

    //第三个视频所在的背景而
    @BindView(R.id.third_surfaceview_relativelayout)
    public RelativeLayout third_surfaceview_relativelayout;

    //第三个视频 的progressbar
    @BindView(R.id.third_pr_layout)
    ProgressBar third_pr_layout;

    //第三个视频 的loading
    @BindView(R.id.third_dispaly_loading_layout)
    TextView third_dispaly_loading_layout;

    //第四个视频的view
    @BindView(R.id.fourth_player_layout)
    NodePlayerView fourthPlayerView;

    //第四个视频所在的背景而
    @BindView(R.id.fourth_surfaceview_relativelayout)
    public RelativeLayout fourth_surfaceview_relativelayout;

    //每四个progressbar
    @BindView(R.id.fourth_pr_layout)
    ProgressBar fourth_pr_layout;

    //返回按钮
    @BindView(R.id.fourth_dispaly_loading_layout)
    TextView fourth_dispaly_loading_layout;

    //单屏播放时的progressbar
    @BindView(R.id.single_player_progressbar_layout)
    ProgressBar single_player_progressbar_layout;

    //单屏时显示 的Loading
    @BindView(R.id.dispaly_video_loading_layout)
    TextView dispaly_video_loading_layout;

    //显示视频信息的Textview
    @BindView(R.id.display_video_information_text_layout)
    TextView display_video_information_text_layout;

    //下键
    @BindView(R.id.video_ptz_up)
    ImageButton video_ptz_up;

    //上键
    @BindView(R.id.video_ptz_down)
    ImageButton video_ptz_down;

    //左键
    @BindView(R.id.video_ptz_left)
    ImageButton video_ptz_left;

    //右键
    @BindView(R.id.video_ptz_right)
    ImageButton video_ptz_right;

    //放大按钮
    @BindView(R.id.video_zoomout_button)
    ImageButton video_zoomout_button;

    //缩小按钮
    @BindView(R.id.video_zoombig_button)
    ImageButton video_zoombig_button;

    //listview所在的父布局
    @BindView(R.id.relativelayout_listview)
    RelativeLayout relativelayout_listview;

    //正文控制键所在的布局
    @BindView(R.id.show_relativelayout_all_button)
    RelativeLayout show_relativelayout_all_button;

    //方向键盘所在布局
    @BindView(R.id.relativelayout_control)
    RelativeLayout relativelayout_control;

    //展示视频数据的listview
    @BindView(R.id.show_listresources)
    public ListView show_listresources;

//
//    @BindView(R.id.bottom_sliding_recyclerview)
//    public RecyclerView bottomSlidingView;

    //四屏所在 的父布局
    @BindView(R.id.four_surfaceview_parent_relativelayout)
    RelativeLayout four_surfaceview_parent_relativelayout;

    //单屏所在的父布局
    @BindView(R.id.single_surfaceview_parent_relativelayout)
    RelativeLayout single_surfaceview_parent_relativelayout;

    /**
     * 当前 是否是四分屏状态
     */
    boolean isCurrentFourScreen = true;

    /**
     * 当前是否是单屏状态
     */
    boolean isCurrentSingleScreen = false;

    /**
     * 判断四个视频是否被选中
     */
    boolean firstViewSelect = false;
    boolean secondViewSelect = false;
    boolean thirdViewSelect = false;
    boolean fourthViewSelect = false;


    @Override
    protected int getLayoutId() {
        return R.layout.paly_video_layout;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //初始控件
        initView();
        //初始数据
        initData();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initData() {

        //取出事先解析好的数据
        String dataSources = (String) SharedPreferencesUtils.getObject(getActivity(), "result", "");
        if (TextUtils.isEmpty(dataSources)) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showEmptyView();
                }
            });
            return;
        }

        List<Device> mlist = GsonUtils.getGsonInstace().str2List(dataSources);
        if (mlist != null && mlist.size() > 0) {
            devicesList = mlist;
        }

        //实例分页加载器
        fourPm = new PageModel(devicesList, 4);
        singlePm = new PageModel(devicesList, 1);

        //获取要预览的数据
        Intent intent = getActivity().getIntent();
        List<Device> passingDataList = (List<Device>) intent.getSerializableExtra("previewdata");//获取list方式

        if (passingDataList != null) {
            currentList = passingDataList;
        } else {
            //初始页面显示的四屏数据
            currentList = fourPm.getObjects(videoCurrentPage);
            //初始页面单屏的数据
        }
        currentSingleList = singlePm.getObjects(videoCurrentPage);

        //播放视频
        initPlayer();

        //当前播放的视频中是否包含云台控制
        judgeHasPtzVideo();

        //当前视频单击或双击事件
        videoScreenClickEvent();
    }

    /**
     * 四分屏或单屏的单击或双击事件
     */
    private void videoScreenClickEvent() {

        //第一个播放器的点击或双击事件
        firstPlayerView.setOnTouchListener(new OnMultiTouchListener(new OnMultiTouchListener.MultiClickCallback() {
            @Override
            public void onDoubleClick() {
                String rtsp = "";
                if (!TextUtils.isEmpty(currentList.get(0).getRtspUrl())) {
                    rtsp = currentList.get(0).getRtspUrl();
                }
                initSinglePlayer(rtsp);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        single_screen_button_selecte.setBackgroundResource(R.mipmap.port_btn_single_selected);
                        four_screen_button_select.setBackgroundResource(R.mipmap.port_monitoring_btn_4splitscreen_normal);
                        isCurrentFourScreen = false;
                        isCurrentSingleScreen = true;
                        display_video_information_text_layout.setText(currentList.get(0).getVideoBen().getName());
                    }
                });
            }
        }, new OnMultiTouchListener.ClickCallback() {
            @Override
            public void onClick() {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        first_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_select_shape);
                        second_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                        third_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                        fourth_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                        firstViewSelect = true;
                        secondViewSelect = false;
                        thirdViewSelect = false;
                        fourthViewSelect = false;
                    }
                });
            }
        }));

        //第二个播放器的点击或双击事件
        secondPlayerView.setOnTouchListener(new OnMultiTouchListener(new OnMultiTouchListener.MultiClickCallback() {
            @Override
            public void onDoubleClick() {
                String rtsp = "";
                if (!TextUtils.isEmpty(currentList.get(1).getRtspUrl())) {
                    rtsp = currentList.get(1).getRtspUrl();
                }
                initSinglePlayer(rtsp);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        single_screen_button_selecte.setBackgroundResource(R.mipmap.port_btn_single_selected);
                        four_screen_button_select.setBackgroundResource(R.mipmap.port_monitoring_btn_4splitscreen_normal);
                        isCurrentFourScreen = false;
                        isCurrentSingleScreen = true;
                        display_video_information_text_layout.setText(currentList.get(1).getVideoBen().getName());

                    }
                });
            }
        }, new OnMultiTouchListener.ClickCallback() {
            @Override
            public void onClick() {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        first_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                        second_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_select_shape);
                        third_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                        fourth_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                        firstViewSelect = false;
                        secondViewSelect = true;
                        thirdViewSelect = false;
                        fourthViewSelect = false;
                    }
                });
            }
        }));

        //第三个播放器的点击或双击事件
        thirdPlayerView.setOnTouchListener(new OnMultiTouchListener(new OnMultiTouchListener.MultiClickCallback() {
            @Override
            public void onDoubleClick() {
                String rtsp = "";
                if (!TextUtils.isEmpty(currentList.get(2).getRtspUrl())) {
                    rtsp = currentList.get(2).getRtspUrl();
                }
                initSinglePlayer(rtsp);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        single_screen_button_selecte.setBackgroundResource(R.mipmap.port_btn_single_selected);
                        four_screen_button_select.setBackgroundResource(R.mipmap.port_monitoring_btn_4splitscreen_normal);
                        isCurrentFourScreen = false;
                        isCurrentSingleScreen = true;
                        display_video_information_text_layout.setText(currentList.get(2).getVideoBen().getName());
                    }
                });
            }
        }, new OnMultiTouchListener.ClickCallback() {
            @Override
            public void onClick() {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        first_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                        second_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                        third_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_select_shape);
                        fourth_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                        firstViewSelect = false;
                        secondViewSelect = false;
                        thirdViewSelect = true;
                        fourthViewSelect = false;
                    }
                });
            }
        }));

        //第四个播放器的点击或双击事件
        fourthPlayerView.setOnTouchListener(new OnMultiTouchListener(new OnMultiTouchListener.MultiClickCallback() {
            @Override
            public void onDoubleClick() {
                String rtsp = "";
                if (!TextUtils.isEmpty(currentList.get(3).getRtspUrl())) {
                    rtsp = currentList.get(3).getRtspUrl();
                }
                initSinglePlayer(rtsp);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        single_screen_button_selecte.setBackgroundResource(R.mipmap.port_btn_single_selected);
                        four_screen_button_select.setBackgroundResource(R.mipmap.port_monitoring_btn_4splitscreen_normal);
                        isCurrentFourScreen = false;
                        isCurrentSingleScreen = true;
                        display_video_information_text_layout.setText(currentList.get(3).getVideoBen().getName());

                    }
                });
            }
        }, new OnMultiTouchListener.ClickCallback() {
            @Override
            public void onClick() {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        first_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                        second_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                        third_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_shape);
                        fourth_surfaceview_relativelayout.setBackgroundResource(R.drawable.video_relativelayout_bg_select_shape);
                        firstViewSelect = false;
                        secondViewSelect = false;
                        thirdViewSelect = false;
                        fourthViewSelect = true;
                    }
                });
            }
        }));

        //单屏播放器的点击或双击事件
        single_player_layout.setOnTouchListener(new OnMultiTouchListener(new OnMultiTouchListener.MultiClickCallback() {
            @Override
            public void onDoubleClick() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        single_screen_button_selecte.setBackgroundResource(R.mipmap.port_btn_single_normal);
                        four_screen_button_select.setBackgroundResource(R.mipmap.port_monitoring_btn_4splitscreen_selected);
                        isCurrentSingleScreen = false;
                        isCurrentFourScreen = true;

                        if (singlePlayer != null && singlePlayer.isPlaying()) {
                            singlePlayer.pause();
                            singlePlayer.stop();
                        }
                        four_surfaceview_parent_relativelayout.setVisibility(View.VISIBLE);
                        single_surfaceview_parent_relativelayout.setVisibility(View.GONE);
                        single_player_layout.setVisibility(View.GONE);

                        firstPlayerView.setVisibility(View.VISIBLE);
                        secondPlayerView.setVisibility(View.VISIBLE);
                        thirdPlayerView.setVisibility(View.VISIBLE);
                        fourthPlayerView.setVisibility(View.VISIBLE);

                        firstPalyer.start();
                        secondPlayer.start();
                        thirdPlayer.start();
                        fourthPlayer.start();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                display_video_information_text_layout.setText("四分屏监控画面");
                            }
                        });
                    }
                });
            }
        }, null));
    }

    /**
     * 初始化控件
     */
    private void initView() {
        //方向键和放大缩小键的Touch事件
        video_ptz_up.setOnTouchListener(this);
        video_ptz_down.setOnTouchListener(this);
        video_ptz_left.setOnTouchListener(this);
        video_ptz_right.setOnTouchListener(this);
        video_zoomout_button.setOnTouchListener(this);
        video_zoombig_button.setOnTouchListener(this);

        //实现四个播放器
        firstPalyer = new NodePlayer(getActivity());

        //设置要播放的view
        firstPalyer.setPlayerView(firstPlayerView);

        //设置连接等待超时时长
        firstPalyer.setConnectWaitTimeout(3 * 1000);

        //设置不自动 重连
        firstPalyer.setAutoReconnectWaitTimeout(0);

        secondPlayer = new NodePlayer(getActivity());
        secondPlayer.setPlayerView(secondPlayerView);
        secondPlayer.setConnectWaitTimeout(3 * 1000);
        secondPlayer.setAutoReconnectWaitTimeout(0);

        thirdPlayer = new NodePlayer(getActivity());
        thirdPlayer.setPlayerView(thirdPlayerView);
        thirdPlayer.setConnectWaitTimeout(3 * 1000);
        thirdPlayer.setAutoReconnectWaitTimeout(0);
        fourthPlayer = new NodePlayer(getActivity());
        fourthPlayer.setPlayerView(fourthPlayerView);
        fourthPlayer.setConnectWaitTimeout(3 * 1000);
        fourthPlayer.setAutoReconnectWaitTimeout(0);
    }

    /**
     * 获取到的rtsp地址
     */
    String mRtsp = "";

    /**
     * 获取要播放的rtsp的token
     */
    String mToken = "";

    /**
     * 判断当前的页面是否有云台控制的视频
     */
    public void judgeHasPtzVideo() {

        if (currentList != null && currentList.size() > 0) {
            for (Device d : currentList) {
                String rtsp = d.getPtzUrl();
                if (!TextUtils.isEmpty(rtsp)) {
                    mRtsp = rtsp;
                    String token = d.getProfiles().get(0).getToken();
                    if (!TextUtils.isEmpty(token)) {
                        mToken = token;
                    }
                }
            }
        }
    }


    /**
     * 实例播放器
     */
    private void initPlayer() {
        single_screen_button_selecte.setBackgroundResource(R.mipmap.port_btn_single_normal);
        four_screen_button_select.setBackgroundResource(R.mipmap.port_monitoring_btn_4splitscreen_selected);
        if (currentList.size() == 4) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    first_pr_layout.setVisibility(View.VISIBLE);
//                    first_dispaly_loading_layout.setVisibility(View.VISIBLE);
//                    first_dispaly_loading_layout.setText("Loading...");
//
//                    second_pr_layout.setVisibility(View.VISIBLE);
//                    second_dispaly_loading_layout.setVisibility(View.VISIBLE);
//                    second_dispaly_loading_layout.setText("Loading...");
//
//                    third_pr_layout.setVisibility(View.VISIBLE);
//                    third_dispaly_loading_layout.setVisibility(View.VISIBLE);
//                    third_dispaly_loading_layout.setText("Loading...");
//
//                    fourth_pr_layout.setVisibility(View.VISIBLE);
//                    fourth_dispaly_loading_layout.setVisibility(View.VISIBLE);
//                    fourth_dispaly_loading_layout.setText("Loading...");
                }
            });

            String rtsp1 = "";
            if (!TextUtils.isEmpty(currentList.get(0).getRtspUrl())) {
                rtsp1 = currentList.get(0).getRtspUrl();
            } else {
                rtsp1 = "";
            }
            String rtsp2 = "";
            if (!TextUtils.isEmpty(currentList.get(1).getRtspUrl())) {
                rtsp2 = currentList.get(1).getRtspUrl();
            } else {
                rtsp2 = "";
            }
            String rtsp3 = "";
            if (!TextUtils.isEmpty(currentList.get(2).getRtspUrl())) {
                rtsp3 = currentList.get(2).getRtspUrl();
            } else {
                rtsp3 = "";
            }
            String rtsp4 = "";
            if (!TextUtils.isEmpty(currentList.get(3).getRtspUrl())) {
                rtsp4 = currentList.get(3).getRtspUrl();
            } else {
                rtsp4 = "";
            }

            Logutils.i("rtsp1:" + rtsp1 + "\n" + "rtsp2:" + rtsp2 + "\n" + "rtsp3:" + rtsp3 + "\n" + "rtsp4:" + rtsp4);

            if (firstPalyer != null && firstPalyer.isPlaying()) {
                firstPalyer.stop();
            }
            if (secondPlayer != null && secondPlayer.isPlaying()) {
                secondPlayer.stop();
            }
            if (thirdPlayer != null && thirdPlayer.isPlaying()) {
                thirdPlayer.stop();
            }
            if (fourthPlayer != null && fourthPlayer.isPlaying()) {
                fourthPlayer.stop();
            }
            //设置播放地址
            firstPalyer.setInputUrl(rtsp1);

            //设置播放器回调
            firstPalyer.setNodePlayerDelegate(this);

            //设置是否播放时有声音
            firstPalyer.setAudioEnable(AppConfig.isVideoSound);

            //视频是否开启视频
            firstPalyer.setVideoEnable(true);

            //开始播放
            firstPalyer.start();

            secondPlayer.setInputUrl(rtsp2);
            secondPlayer.setNodePlayerDelegate(this);
            secondPlayer.setAudioEnable(AppConfig.isVideoSound);
            secondPlayer.setVideoEnable(true);
            secondPlayer.start();

            thirdPlayer.setInputUrl(rtsp3);
            thirdPlayer.setNodePlayerDelegate(this);
            thirdPlayer.setAudioEnable(AppConfig.isVideoSound);
            thirdPlayer.setVideoEnable(true);
            thirdPlayer.start();

            fourthPlayer.setInputUrl(rtsp4);
            fourthPlayer.setNodePlayerDelegate(this);
            fourthPlayer.setAudioEnable(AppConfig.isVideoSound);
            fourthPlayer.setVideoEnable(true);
            fourthPlayer.start();
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {

            case R.id.video_ptz_up:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!TextUtils.isEmpty(mRtsp) && !TextUtils.isEmpty(mToken)) {
                        ControlPtzUtils controlPtz = new ControlPtzUtils(mRtsp, mToken, "top", 0.00, 0.03);
                        controlPtz.start();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ControlPtzUtils controlPtz = new ControlPtzUtils(mRtsp, mToken, "stop", 0.00, 0.00);
                    controlPtz.start();
                }

                break;

            case R.id.video_ptz_down:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!TextUtils.isEmpty(mRtsp) && !TextUtils.isEmpty(mToken)) {
                        ControlPtzUtils controlPtz = new ControlPtzUtils(mRtsp, mToken, "below", 0.00, -0.03);
                        controlPtz.start();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ControlPtzUtils controlPtz = new ControlPtzUtils(mRtsp, mToken, "stop", 0.00, 0.00);
                    controlPtz.start();
                }
                break;

            case R.id.video_ptz_left:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!TextUtils.isEmpty(mRtsp) && !TextUtils.isEmpty(mToken)) {
                        ControlPtzUtils controlPtz = new ControlPtzUtils(mRtsp, mToken, "left", 0.03, 0.00);
                        controlPtz.start();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ControlPtzUtils controlPtz = new ControlPtzUtils(mRtsp, mToken, "stop", 0.00, 0.00);
                    controlPtz.start();
                }
                break;

            case R.id.video_ptz_right:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!TextUtils.isEmpty(mRtsp) && !TextUtils.isEmpty(mToken)) {
                        ControlPtzUtils controlPtz = new ControlPtzUtils(mRtsp, mToken, "right", -0.03, 0.00);
                        controlPtz.start();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ControlPtzUtils controlPtz = new ControlPtzUtils(mRtsp, mToken, "stop", 0.00, 0.00);
                    controlPtz.start();
                }
                break;

            case R.id.video_zoomout_button:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!TextUtils.isEmpty(mRtsp) && !TextUtils.isEmpty(mToken)) {
                        ControlPtzUtils controlPtz = new ControlPtzUtils(mRtsp, mToken, "zoom_b", -0.3, -0.03);
                        controlPtz.start();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ControlPtzUtils controlPtz = new ControlPtzUtils(mRtsp, mToken, "stop", 0.00, 0.00);
                    controlPtz.start();
                }
                break;

            case R.id.video_zoombig_button:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!TextUtils.isEmpty(mRtsp) && !TextUtils.isEmpty(mToken)) {
                        ControlPtzUtils controlPtz = new ControlPtzUtils(mRtsp, mToken, "zoom_s", 0.3, 0.03);
                        controlPtz.start();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ControlPtzUtils controlPtz = new ControlPtzUtils(mRtsp, mToken, "stop", 0.00, 0.00);
                    controlPtz.start();
                }
                break;
        }
        return false;
    }

    /**
     * 播放器回调监听
     *
     * @param player
     * @param event
     * @param msg
     */
    @Override
    public void onEventCallback(NodePlayer player, final int event, final String msg) {
        if (firstPalyer == player) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (event == 1002 || event == 1003 || event == 1005 || event == 1006) {
                        first_pr_layout.setVisibility(View.GONE);
                        first_dispaly_loading_layout.setVisibility(View.VISIBLE);
                        first_dispaly_loading_layout.setText(msg);
                        return;
                    } else {
                        first_pr_layout.setVisibility(View.GONE);
                        first_dispaly_loading_layout.setVisibility(View.GONE);
                    }
                }
            });
        }
        if (secondPlayer == player) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (event == 1002 || event == 1003 || event == 1005 || event == 1006) {
                        second_pr_layout.setVisibility(View.GONE);
                        second_dispaly_loading_layout.setVisibility(View.VISIBLE);
                        second_dispaly_loading_layout.setText(msg);
                        return;
                    } else {
                        second_pr_layout.setVisibility(View.GONE);
                        second_dispaly_loading_layout.setVisibility(View.GONE);
                    }
                }
            });
        }
        if (thirdPlayer == player) {
            Logutils.i("third:" + event + msg);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (event == 1003) {
                        third_pr_layout.setVisibility(View.GONE);
                        third_dispaly_loading_layout.setVisibility(View.VISIBLE);
                        third_dispaly_loading_layout.setText(msg);
                        return;
                    } else {
                        third_pr_layout.setVisibility(View.GONE);
                        third_dispaly_loading_layout.setVisibility(View.GONE);
                    }
                }
            });
        }
        if (fourthPlayer == player) {
            Logutils.i("fourth:" + event + msg);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (event == 1003) {
                        fourth_pr_layout.setVisibility(View.GONE);
                        fourth_dispaly_loading_layout.setVisibility(View.VISIBLE);
                        fourth_dispaly_loading_layout.setText(msg);
                        return;
                    } else {
                        fourth_pr_layout.setVisibility(View.GONE);
                        fourth_dispaly_loading_layout.setVisibility(View.GONE);
                    }
                }
            });
        }
        if (singlePlayer == player) {
            Logutils.i("single:" + event + msg);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (event == 1002 || event == 1003 || event == 1005 || event == 1006) {
                        single_player_progressbar_layout.setVisibility(View.GONE);
                        dispaly_video_loading_layout.setVisibility(View.VISIBLE);
                        dispaly_video_loading_layout.setText(msg);
                        return;
                    } else {
                        dispaly_video_loading_layout.setVisibility(View.GONE);
                        single_player_progressbar_layout.setVisibility(View.GONE);
                    }
                }
            });
        }
    }


    /**
     * 向服务器发送报警
     */
    @OnClick(R.id.send_alarmtoServer_button)
    public void sendAlarmToServer(View view) {

        //判断当前是单屏状态
        if (isCurrentSingleScreen) {
            if (currentSingleList != null && currentSingleList.size() > 0) {
                VideoBen v = currentSingleList.get(0).getVideoBen();
                SendAlarmToServer sendAlarmToServer = new SendAlarmToServer(v, new SendAlarmToServer.Callback() {
                    @Override
                    public void getCallbackData(final String result) {
                        if (!TextUtils.isEmpty(result)) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setTitle("报警信息:").setMessage(result).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).create().show();
                                }
                            });
                        }
                    }
                });
                sendAlarmToServer.start();
            }
        }

        //判断当前 是四分屏状态
        if (isCurrentFourScreen) {
            if (firstViewSelect) {
                sendToAlarm(1);
            } else if (secondViewSelect) {
                sendToAlarm(2);
            } else if (thirdViewSelect) {
                sendToAlarm(3);
            } else if (fourthViewSelect) {
                sendToAlarm(4);
            } else {
                //未选 中窗口
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showShort("please select one window");
                    }
                });
            }
        }
    }

    /**
     * 根据选中的窗口报警
     * <p>
     * 1、第一个视频源
     * 2、第二个视频源
     * 3、第三个视频源
     * 4、第四个视频源
     */
    private void sendToAlarm(int tag) {
        VideoBen v = null;
        if (currentList != null && currentList.size() > 0) {
            if (tag == 1) {
                v = currentList.get(0).getVideoBen();
            } else if (tag == 2) {
                v = currentList.get(1).getVideoBen();
            } else if (tag == 3) {
                v = currentList.get(2).getVideoBen();
            } else if (tag == 4) {
                v = currentList.get(3).getVideoBen();
            }
            if (v == null) {
                return;
            }
            SendAlarmToServer sendAlarmToServer = new SendAlarmToServer(v, new SendAlarmToServer.Callback() {
                @Override
                public void getCallbackData(final String result) {
                    if (!TextUtils.isEmpty(result)) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("报警信息:").setMessage(result).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).create().show();
                            }
                        });
                    }
                }
            });
            sendAlarmToServer.start();
        }
    }


    /**
     * 单屏播放rtsp
     */
    private void initSinglePlayer(String rtsp) {

        //显示加载的进度条和加载数据的提示
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dispaly_video_loading_layout.setVisibility(View.VISIBLE);
                single_player_progressbar_layout.setVisibility(View.VISIBLE);
                dispaly_video_loading_layout.setText("Loading");
            }
        });

        //单屏的视频播放器的停止播放
        if (singlePlayer != null && singlePlayer.isPlaying()) {
            singlePlayer.pause();
            singlePlayer.stop();
            singlePlayer.release();
        }

        //第一个视频播放器的停止播放
        if (firstPalyer != null && firstPalyer.isPlaying()) {
            firstPalyer.pause();
            firstPalyer.stop();
        }

        //第二个视频播放器的停止播放
        if (secondPlayer != null && secondPlayer.isPlaying()) {
            secondPlayer.pause();
            secondPlayer.stop();
        }

        //第三个视频播放器的停止播放
        if (thirdPlayer != null && thirdPlayer.isPlaying()) {
            thirdPlayer.pause();
            thirdPlayer.stop();
        }

        //第四个视频播放器的停止播放
        if (fourthPlayer != null && fourthPlayer.isPlaying()) {
            fourthPlayer.pause();
            fourthPlayer.stop();
        }

        //设置四分屏的布局都 不可见
        firstPlayerView.setVisibility(View.GONE);
        secondPlayerView.setVisibility(View.GONE);
        thirdPlayerView.setVisibility(View.GONE);
        fourthPlayerView.setVisibility(View.GONE);
        four_surfaceview_parent_relativelayout.setVisibility(View.GONE);
        single_surfaceview_parent_relativelayout.setVisibility(View.VISIBLE);

        //单屏布局可见
        single_player_layout.setVisibility(View.VISIBLE);

        //实例单屏播放器并开始播放
        singlePlayer = new NodePlayer(getActivity());
        singlePlayer.setPlayerView(single_player_layout);
        singlePlayer.setInputUrl(rtsp);
        singlePlayer.setNodePlayerDelegate(this);
        singlePlayer.setAudioEnable(AppConfig.isVideoSound);
        singlePlayer.setVideoEnable(true);
        singlePlayer.start();

        //设置单屏的播放信息
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                display_video_information_text_layout.setText(currentSingleList.get(0).getVideoBen().getName());
            }
        });

    }

    /**
     * 下一页
     */
    @OnClick(R.id.video_nextpage_button)
    public void videoNextPage(View view) {

        videoCurrentPage++;

        //当前四分屏
        if (isCurrentFourScreen) {
            if (isCurrentFourScreen) {
                if (fourPm != null && fourPm.isHasNextPage()) {
                    currentList = fourPm.getObjects(videoCurrentPage);
                    initPlayer();
                }
            }
        }

        //当前单屏
        if (isCurrentSingleScreen) {
            if (singlePm != null && singlePm.isHasNextPage()) {
                currentSingleList = singlePm.getObjects(videoCurrentPage);
                String rtsp = "";
                if (!TextUtils.isEmpty(currentSingleList.get(0).getRtspUrl())) {
                    rtsp = currentSingleList.get(0).getRtspUrl();
                }
                initSinglePlayer(rtsp);
            }
        }

        //判断本页面内是否有云台控制的数据
        judgeHasPtzVideo();
    }

    /**
     * 上一页
     */
    @OnClick(R.id.video_previous_button)
    public void videoPreviousPage(View view) {

        videoCurrentPage--;

        //四分屏时的状态
        if (isCurrentFourScreen) {
            if (fourPm != null && fourPm.isHasPreviousPage()) {
                currentList = fourPm.getObjects(videoCurrentPage);
                initPlayer();
            } else {
                videoCurrentPage = 1;
            }
        }

        //单屏的状态
        if (isCurrentSingleScreen) {
            if (singlePm != null && singlePm.isHasPreviousPage()) {
                currentSingleList = singlePm.getObjects(videoCurrentPage);
                String rtsp = "";
                if (!TextUtils.isEmpty(currentSingleList.get(0).getRtspUrl())) {
                    rtsp = currentSingleList.get(0).getRtspUrl();
                }
                initSinglePlayer(rtsp);
            } else {
                videoCurrentPage = 1;
            }
        }
        judgeHasPtzVideo();
    }


    /**
     * 单屏播放rtsp
     */
    @OnClick(R.id.single_screen_button_selecte)
    public void singleScreenVideo(View view) {

        //  if (firstViewSelect || secondViewSelect || thirdViewSelect || fourthViewSelect) {
        single_screen_button_selecte.setBackgroundResource(R.mipmap.port_btn_single_selected);
        four_screen_button_select.setBackgroundResource(R.mipmap.port_monitoring_btn_4splitscreen_normal);
        isCurrentFourScreen = false;
        isCurrentSingleScreen = true;
        //  }

        //第一个窗口选中
        if (firstViewSelect) {
            String rtsp = "";
            if (!TextUtils.isEmpty(currentList.get(0).getRtspUrl())) {
                rtsp = currentList.get(0).getRtspUrl();
            }
            initSinglePlayer(rtsp);
        }

        //第二个窗口选中
        if (secondViewSelect) {
            String rtsp = "";
            if (!TextUtils.isEmpty(currentList.get(1).getRtspUrl())) {
                rtsp = currentList.get(1).getRtspUrl();
            }
            initSinglePlayer(rtsp);
        }

        //第三个窗口选中
        if (thirdViewSelect) {
            String rtsp = "";
            if (!TextUtils.isEmpty(currentList.get(2).getRtspUrl())) {
                rtsp = currentList.get(2).getRtspUrl();
            }
            initSinglePlayer(rtsp);
        }


        if (fourthViewSelect) {
            String rtsp = "";
            if (!TextUtils.isEmpty(currentList.get(3).getRtspUrl())) {
                rtsp = currentList.get(3).getRtspUrl();
            }
            initSinglePlayer(rtsp);
        }
    }


    /**
     * 四屏播放rtsp
     */
    @OnClick(R.id.four_screen_button_select)
    public void fourScreenVideo(View view) {
        single_screen_button_selecte.setBackgroundResource(R.mipmap.port_btn_single_normal);
        four_screen_button_select.setBackgroundResource(R.mipmap.port_monitoring_btn_4splitscreen_selected);
        isCurrentSingleScreen = false;
        isCurrentFourScreen = true;

        if (singlePlayer != null && singlePlayer.isPlaying()) {
            singlePlayer.pause();
            singlePlayer.stop();
        }
        four_surfaceview_parent_relativelayout.setVisibility(View.VISIBLE);
        single_surfaceview_parent_relativelayout.setVisibility(View.GONE);
        single_player_layout.setVisibility(View.GONE);

        firstPlayerView.setVisibility(View.VISIBLE);
        secondPlayerView.setVisibility(View.VISIBLE);
        thirdPlayerView.setVisibility(View.VISIBLE);
        fourthPlayerView.setVisibility(View.VISIBLE);

        firstPalyer.start();
        secondPlayer.start();
        thirdPlayer.start();
        fourthPlayer.start();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                display_video_information_text_layout.setText("四分屏监控画面");
            }
        });
    }

    boolean firstPlayerIsStop = false;

    /**
     * 播放器停止
     */
    @OnClick(R.id.paly_or_stop_button_select)
    public void stopOrRestartVideo(View view) {
        if (isCurrentFourScreen) {
            if (firstViewSelect) {
                if (firstPalyer != null) {
                    if (!firstPlayerIsStop) {
                        firstPalyer.stop();
                        firstPlayerIsStop = true;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                paly_or_stop_button_select.setBackgroundResource(R.mipmap.port_monitoring_icon_player_selected);
                            }
                        });
                    } else {
                        firstPlayerIsStop = false;
                        firstPalyer.start();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                paly_or_stop_button_select.setBackgroundResource(R.mipmap.port_monitoring_icon_stopplay_normal);
                            }
                        });
                    }

                }
            } else if (secondViewSelect) {
                if (secondPlayer != null) {
                    if (!firstPlayerIsStop) {
                        secondPlayer.stop();
                        firstPlayerIsStop = true;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                paly_or_stop_button_select.setBackgroundResource(R.mipmap.port_monitoring_icon_player_selected);
                            }
                        });
                    } else {
                        firstPlayerIsStop = false;
                        secondPlayer.start();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                paly_or_stop_button_select.setBackgroundResource(R.mipmap.port_monitoring_icon_stopplay_normal);
                            }
                        });
                    }

                }
            } else if (thirdViewSelect) {
                if (thirdPlayer != null) {
                    if (!firstPlayerIsStop) {
                        thirdPlayer.stop();
                        firstPlayerIsStop = true;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                paly_or_stop_button_select.setBackgroundResource(R.mipmap.port_monitoring_icon_player_selected);
                            }
                        });
                    } else {
                        firstPlayerIsStop = false;
                        thirdPlayer.start();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                paly_or_stop_button_select.setBackgroundResource(R.mipmap.port_monitoring_icon_stopplay_normal);
                            }
                        });
                    }

                }
            } else if (fourthViewSelect) {
                if (fourthPlayer != null) {
                    if (!firstPlayerIsStop) {
                        fourthPlayer.stop();
                        firstPlayerIsStop = true;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                paly_or_stop_button_select.setBackgroundResource(R.mipmap.port_monitoring_icon_player_selected);
                            }
                        });
                    } else {
                        firstPlayerIsStop = false;
                        fourthPlayer.start();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                paly_or_stop_button_select.setBackgroundResource(R.mipmap.port_monitoring_icon_stopplay_normal);
                            }
                        });
                    }

                }
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showShort("未选中窗口！！！");
                    }
                });
            }
        } else if (isCurrentSingleScreen) {
            if (singlePlayer != null) {
                if (!firstPlayerIsStop) {
                    singlePlayer.stop();
                    firstPlayerIsStop = true;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            paly_or_stop_button_select.setBackgroundResource(R.mipmap.port_monitoring_icon_player_selected);
                        }
                    });
                } else {
                    firstPlayerIsStop = false;
                    singlePlayer.start();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            paly_or_stop_button_select.setBackgroundResource(R.mipmap.port_monitoring_icon_stopplay_normal);
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Logutils.i(hidden + "///////////////////////hidden");
    }

    boolean currentPageVisible = false;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            currentPageVisible = true;
        } else {
            currentPageVisible = false;

            if (firstPalyer != null){
                firstPalyer.setNodePlayerDelegate(null);
            }
            if (secondPlayer != null){
                secondPlayer.setNodePlayerDelegate(null);
            }
            if (thirdPlayer != null){
                thirdPlayer.setNodePlayerDelegate(null);
            }
            if (fourthPlayer != null){
                fourthPlayer.setNodePlayerDelegate(null);
            }
        }
        Logutils.i(isVisibleToUser + "VoideoFragment:" + isVisibleToUser);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        firstPalyer.release();
        secondPlayer.release();
        thirdPlayer.release();
        fourthPlayer.release();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}

package com.zkth.mst.client.ui.activity;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.zkth.mst.client.R;
import com.zkth.mst.client.base.App;
import com.zkth.mst.client.base.AppConfig;
import com.zkth.mst.client.base.BaseActivity;
import com.zkth.mst.client.base.DbConfig;
import com.zkth.mst.client.callbacks.AmmoRequestCallBack;
import com.zkth.mst.client.callbacks.ReceiveServerMess;
import com.zkth.mst.client.callbacks.ReceiverServerAlarm;
import com.zkth.mst.client.callbacks.RequestSipSourcesThread;
import com.zkth.mst.client.callbacks.RequestVideoSourcesThread;
import com.zkth.mst.client.callbacks.SendHbService;
import com.zkth.mst.client.db.DatabaseHelper;
import com.zkth.mst.client.db.Logutils;
import com.zkth.mst.client.entity.AlarmBen;
import com.zkth.mst.client.entity.SipBean;
import com.zkth.mst.client.entity.VideoBen;
import com.zkth.mst.client.linphone.Linphone;
import com.zkth.mst.client.linphone.PhoneCallback;
import com.zkth.mst.client.linphone.RegistrationCallback;
import com.zkth.mst.client.linphone.SipService;
import com.zkth.mst.client.onvif.Device;
import com.zkth.mst.client.onvif.Onvif;
import com.zkth.mst.client.rtsp.RtspServer;
import com.zkth.mst.client.rtsp.media.VideoMediaCodec;
import com.zkth.mst.client.rtsp.record.Constant;
import com.zkth.mst.client.utils.CpuAndRam;
import com.zkth.mst.client.utils.LogUtils;
import com.zkth.mst.client.utils.NetworkUtils;
import com.zkth.mst.client.utils.PhoneUtils;
import com.zkth.mst.client.utils.SharedPreferencesUtils;
import com.zkth.mst.client.utils.ToastUtils;
import com.zkth.mst.client.utils.WriteLogToFile;

import org.linphone.core.LinphoneCall;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.nodemedia.NodePlayer;
import cn.nodemedia.NodePlayerView;

public class MainActivity extends BaseActivity implements Camera.PreviewCallback {

    private static final String TAG = "MainActivity";
    private Camera mCamera;
    SurfaceHolder surfaceHolder;
    private static int cameraId = 0;//默认后置摄像头
    private String RtspAddress;
    private VideoMediaCodec mVideoMediaCodec;
    private RtspServer mRtspServer;
    boolean isBandService = false;
    boolean isRecording;
    NodePlayer nodePlayer;
    TextView tv;
    boolean isCanCallPhone;
    int sipResourcesNum = 0;
    int videoResourcesNum = -1;


    //模拟值班室信息
    String duryName = "7002";//值班值号码信息
    String duryRtsp = "rtmp://live.hkstv.hk.lxdns.com/live/hks"; //值班室的画面信息

    @BindView(R.id.no_network_layout)
    RelativeLayout no_network_layout;
    @BindView(R.id.main_loading_layout)
    ImageView main_loading_layout;

    @BindView(R.id.main_loading_textview_layout)
    TextView main_loading_textview_layout;

    @BindView(R.id.main_incon_time)
    TextView main_incon_time;


    Animation mLoadingAnim;
    List<SipVideo> sipData = new ArrayList<>();
    List<Device> dataSources = new ArrayList<>();
    String ip = "";
    boolean threadIsRun = true;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                num++;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText(PhoneUtils.getTime(num) + "");
                    }
                });
            } else if (msg.what == 111) {
                Bundle bundle = msg.getData();
                SipVideo mSipVideo = (SipVideo) bundle.getSerializable("sipVideo");
                sipData.add(mSipVideo);
                if (sipData.size() == sipResourcesNum) {
                    Gson gson = new Gson();
                    String str = gson.toJson(sipData);
                    if (!TextUtils.isEmpty(str)) {
                        SharedPreferencesUtils.putObject(MainActivity.this, "sipresult", str);
                        Logutils.i("success sip ");
                    }
                }
            } else if (msg.what == 1000) {
                //onvif数据处理
                Bundle bundle = msg.getData();
                Device device = (Device) bundle.getSerializable("device");
                dataSources.add(device);
                if (dataSources.size() == videoResourcesNum) {
                    Gson gson = new Gson();
                    String str = gson.toJson(dataSources);
                    if (TextUtils.isEmpty(str)) {
                        return;
                    }
                    SharedPreferencesUtils.putObject(MainActivity.this, "result", str);
                    Logutils.i("success video ");
                }
            } else if (msg.what == 1001) {
                long time = System.currentTimeMillis();
                Date date = new Date(time);
                SimpleDateFormat timeD = new SimpleDateFormat("HH:mm:ss");
                String currentTime = timeD.format(date).toString();
                if (!TextUtils.isEmpty(currentTime)) {
                    if (main_incon_time != null)
                        main_incon_time.setText(currentTime);
                }
            }
        }
    };

    @Override
    public void initView() {

    }

    @Override
    public void initData() {
        //启动心跳服务15秒后开始发送心跳
        startService(new Intent(this, SendHbService.class));
        //时间显示
        TimeThread timeThread = new TimeThread();
        new Thread(timeThread).start();


        ip = NetworkUtils.getIPAddress(true);
        RtspAddress = "rtsp://" + "19.0.0.77" + ":" + RtspServer.DEFAULT_RTSP_PORT;
        mVideoMediaCodec = new VideoMediaCodec();
        if (RtspAddress != null) {
            Log.i("tag", "地址: " + RtspAddress);
        }
        mLoadingAnim = AnimationUtils.loadAnimation(this, R.anim.loading);
        CpuAndRam.getInstance().init(MainActivity.this, 5 * 1000);
        CpuAndRam.getInstance().start();

        //接收短消息和报警报文
        receiveMessageAndAlarm();
        //获取sip本机信息并注册到sip服务 器
        getNativeSipInformation();
        //获取所有的视频 资源并解析rtsp
        getAllVideoResoucesInformation();

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }


    @Override
    public void onNetworkViewRefresh() {

    }

    @OnClick({R.id.button_alarm, R.id.button_intercom, R.id.button_video, R.id.button_applyforplay, R.id.button_chat, R.id.button_setup})
    public void onclickEvent(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.button_intercom:
                LogUtils.i("TAG", AppConfig.cpu + "///");
                LogUtils.i("TAG", AppConfig.ram + "///");
                LogUtils.i("TAG", AppConfig.battery + "///");
                LogUtils.i("TAG", AppConfig.wifi + "///");
                LogUtils.i("TAG", AppConfig.lat + "///");
                LogUtils.i("TAG", AppConfig.log + "///");

                LogUtils.i("TAG", AppConfig.sipName + "///");
                LogUtils.i("TAG", AppConfig.sipNum + "///");
                LogUtils.i("TAG", AppConfig.sipPwd + "///");
                LogUtils.i("TAG", AppConfig.sipServer + "///");
                intent.setClass(MainActivity.this, MainFragmentActivity.class);
                intent.putExtra("current", 0);
                startActivity(intent);

                break;
            case R.id.button_video:
                intent.setClass(MainActivity.this, MainFragmentActivity.class);
                intent.putExtra("current", 1);
                startActivity(intent);


                break;
            case R.id.button_applyforplay:
                applyForUnpacking();
                break;
            case R.id.button_chat:
                intent.setClass(MainActivity.this, MainFragmentActivity.class);
                intent.putExtra("current", 2);
                startActivity(intent);

                break;
            case R.id.button_setup:
                intent.setClass(MainActivity.this, SettingCenterActivity.class);
                startActivity(intent);
                break;
            case R.id.button_alarm:
                sendEmergency();
                break;
        }
    }

    //应急报警
    private void sendEmergency() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.alarm_item_view, null);
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                final AlertDialog alarmWindow = builder.setView(view).create();
                alarmWindow.show();
                tv = (TextView) view.findViewById(R.id.show_emergency_call_time);
                NodePlayerView nodePlayerView = view.findViewById(R.id.show_emergency_layout);
                nodePlayer = new NodePlayer(MainActivity.this);
                nodePlayer.setPlayerView(nodePlayerView);
                nodePlayer.setInputUrl(duryRtsp);
                nodePlayer.start();
                if (SipService.isReady()) {
                    if (isCanCallPhone)
                        Linphone.callTo(duryName, false);
                    else
                        ToastUtils.showShort("sip未注册成功");
                }

                view.findViewById(R.id.custom_camera_layout).setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                    @Override
                    public void onClick(View view) {
                        if (cameraId == 0) {
                            cameraId = 1;
                        } else if (cameraId == 1) {
                            cameraId = 0;
                        }
                        initCamera();
                        try {
                            play();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                view.findViewById(R.id.close_layout).setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                    @Override
                    public void onClick(View view) {
                        if (null != mCamera) {
                            mCamera.setPreviewCallback(null);
                            mCamera.stopPreview();
                            mCamera.release();
                            mCamera = null;
                        }
                        alarmWindow.dismiss();
                        nodePlayer.stop();
                        nodePlayer.release();
                        Linphone.hangUp();
                        if (mRtspServer != null)
                            mRtspServer.removeCallbackListener(mRtspCallbackListener);
                        if (mRtspServiceConnection != null)
                            unbindService(mRtspServiceConnection);
                    }
                });

                SurfaceView surfaceView = (SurfaceView) view.findViewById(R.id.sur_view_layout);
                surfaceHolder = surfaceView.getHolder();
                surfaceHolder.setFixedSize(Constant.VIDEO_WIDTH, Constant.VIDEO_HEIGHT);
                surfaceHolder.setKeepScreenOn(true);
                surfaceHolder.addCallback(new SurfaceHolder.Callback() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                    @Override
                    public void surfaceCreated(SurfaceHolder surfaceHolder) {
                        LogUtils.i("TAG", "surfaceCreated");

                        initCamera();
                        try {
                            play();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                        LogUtils.i("TAG", "surfaceChanged");
                    }

                    @Override
                    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                        LogUtils.i("TAG", "surfaceDestroyed");
                        if (null != mCamera) {
                            mCamera.setPreviewCallback(null);
                            mCamera.stopPreview();
                            mCamera.release();
                            mCamera = null;
                        }
                    }
                });
            }
        });

    }

    //申请供弹
    private void applyForUnpacking() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                main_loading_layout.setVisibility(View.VISIBLE);
                main_loading_layout.startAnimation(mLoadingAnim);
                main_loading_textview_layout.setVisibility(View.VISIBLE);
                main_loading_textview_layout.setText("正在计算开锁码");
            }
        });
        AmmoRequestCallBack ammoRequestCallBack = new AmmoRequestCallBack(new AmmoRequestCallBack.GetDataListern() {
            @Override
            public void getDataInformation(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        main_loading_layout.setVisibility(View.GONE);
                        main_loading_layout.clearAnimation();
                        main_loading_textview_layout.setVisibility(View.GONE);
                        main_loading_textview_layout.setText("");
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("申请开箱状态:").setMessage(result).create().show();
                    }
                });
            }
        });
        ammoRequestCallBack.start();
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

    /**
     * 初始化相机参数
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)

    //初始化camera
    private void initCamera() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        mVideoMediaCodec.prepare();
        mVideoMediaCodec.isRun(true);
        try {
            mCamera = Camera.open(cameraId);
            mCamera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if (cameraId == 0)
            mCamera.setDisplayOrientation(90);
        else
            mCamera.setDisplayOrientation(90);
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFlashMode("off");
        parameters.setPreviewFormat(ImageFormat.NV21);
        parameters.setPreviewFrameRate(15);
        parameters.setPreviewSize(Constant.VIDEO_WIDTH, Constant.VIDEO_HEIGHT);
        try {
            mCamera.setParameters(parameters);
        } catch (Exception e) {
        }
        mCamera.setPreviewCallback(this);
    }

    //surfaceview预览
    private void play() throws IOException {
        mCamera.startPreview();
        if (RtspAddress != null && !RtspAddress.isEmpty()) {
            isRecording = true;
            Intent intent = new Intent(this, RtspServer.class);
            bindService(intent, mRtspServiceConnection, Context.BIND_AUTO_CREATE);
            isBandService = true;
        }
        new Thread() {
            @Override
            public void run() {
                mVideoMediaCodec.getBuffers();
            }
        }.start();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (cameraId == 0) {
            data = VideoMediaCodec.rotateYUVDegree90(data, Constant.VIDEO_WIDTH, Constant.VIDEO_HEIGHT);
        } else {
            data = VideoMediaCodec.rotateYUV420Degree270(data, Constant.VIDEO_WIDTH, Constant.VIDEO_HEIGHT);
        }
        VideoMediaCodec.putYUVData(data, data.length);
    }

    private RtspServer.CallbackListener mRtspCallbackListener = new RtspServer.CallbackListener() {
        @Override
        public void onError(RtspServer server, Exception e, int error) {
            // We alert the user that the port_icon is already used by another app.
            if (error == RtspServer.ERROR_BIND_FAILED) {
                new android.support.v7.app.AlertDialog.Builder(MainActivity.this)
                        .setTitle("Port already in use !")
                        .setMessage("You need to choose another port_icon for the RTSP server !")
                        .show();
            }
        }


        @Override
        public void onMessage(RtspServer server, int message) {
            if (message == RtspServer.MESSAGE_STREAMING_STARTED) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "RTSP STREAM STARTED", Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (message == RtspServer.MESSAGE_STREAMING_STOPPED) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "RTSP STREAM STOPPED", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    };

    private ServiceConnection mRtspServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mRtspServer = ((RtspServer.LocalBinder) service).getService();
            mRtspServer.addCallbackListener(mRtspCallbackListener);
            mRtspServer.start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        Linphone.addCallback(new RegistrationCallback() {
            @Override
            public void registrationProgress() {
                LogUtils.i("TAG", "registering");
            }

            @Override
            public void registrationOk() {
                LogUtils.i("TAG", "registrationOk");
                isCanCallPhone = true;
            }

            @Override
            public void registrationFailed() {
                LogUtils.i("TAG", "registrationFailed");
                isCanCallPhone = false;
            }
        }, new PhoneCallback() {
            @Override
            public void incomingCall(LinphoneCall linphoneCall) {

            }

            @Override
            public void outgoingInit() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText("正在拨号:" + duryName);
                    }
                });
            }

            @Override
            public void callConnected() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText("正在通话:" + duryName);
                        threadStart();

                    }
                });
            }

            @Override
            public void callEnd() {
                threadStop();
            }

            @Override
            public void callReleased() {

            }

            @Override
            public void error() {

            }
        });
    }

    Thread mThread = null;
    boolean mWorking = false;
    int num = 0;

    //计时线程开启
    public void threadStart() {
        mWorking = true;
        if (mThread != null && mThread.isAlive()) {
        } else {
            mThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (mWorking) {
                        try {
                            Thread.sleep(1 * 1000);
                            Message message = new Message();
                            message.what = 1;
                            handler.sendMessage(message);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });
            mThread.start();
        }
    }

    //计时线程停止
    public void threadStop() {
        if (mWorking) {
            if (mThread != null && mThread.isAlive()) {
                mThread.interrupt();
                mThread = null;
            }
            tv.setText("00:00");
            mWorking = false;
        }
    }

    //接收
    private void receiveMessageAndAlarm() {

        //接收短消息
        ReceiveServerMess receiveServerMess = new ReceiveServerMess(new ReceiveServerMess.GetSmsListern() {
            @Override
            public void getSmsContent(final String ms) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(MainActivity.this);
                        alert.setTitle("短信息").setMessage(ms).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                    }
                });

//                DatabaseHelper databaseHelper = new DatabaseHelper(MainFragmentActivity.this);
//                databaseHelper.insertMessData(new Date().toString(), "true", ms, "receivermess");
//                databaseHelper.close();

            }
        });
        new Thread(receiveServerMess).start();
        //接收報警報文
        ReceiverServerAlarm receiverServerAlarm = new ReceiverServerAlarm(new ReceiverServerAlarm.GetAlarmFromServerListern() {
            @Override
            public void getListern(final AlarmBen alarmBen, final String flage) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("公告信息").setMessage(alarmBen.getAlertType()).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                    }
                });
//                DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this);
//                databaseHelper.insertMessData(new Date().toString(), flage + "", alarmBen.toString(), "receiveralarm");
//                databaseHelper.close();
            }
        });
        new Thread(receiverServerAlarm).start();
    }


    private void getNativeSipInformation() {
        RequestSipSourcesThread sipThread = new RequestSipSourcesThread(MainActivity.this, "0", new RequestSipSourcesThread.SipListern() {
            @Override
            public void getDataListern(List<SipBean> mList) {
                Logutils.i("mlist:" + mList.size());
                if (mList != null && mList.size() > 0) {
                    if (!TextUtils.isEmpty(ip)) {
                        for (SipBean s : mList) {
                            if (s.getIp().equals(ip)) {
                                String sipName = s.getName();
                                String sipNum = s.getNumber();
                                String sipPwd = s.getSippass();
                                String sipServer = s.getSipserver();
                                if (!TextUtils.isEmpty(sipNum) && !TextUtils.isEmpty(sipPwd) && !TextUtils.isEmpty(sipServer)) {
                                    AppConfig.sipName = sipName;
                                    AppConfig.sipNum = sipNum;
                                    AppConfig.sipPwd = sipPwd;
                                    AppConfig.sipServer = sipServer;
                                    registerSipIntoServer(sipNum, sipPwd, sipServer);
                                    String db_sipName = DbConfig.getInstance().getData(7);
                                    String db_sipNum = DbConfig.getInstance().getData(8);
                                    String db_sipPwd = DbConfig.getInstance().getData(9);
                                    String db_sipServer = DbConfig.getInstance().getData(10);
                                    if (TextUtils.isEmpty(db_sipName) || TextUtils.isEmpty(db_sipNum) || TextUtils.isEmpty(db_sipPwd) || TextUtils.isEmpty(db_sipServer)) {
                                        DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this);
                                        SQLiteDatabase db = databaseHelper.getWritableDatabase();
                                        ContentValues contentValues = new ContentValues();
                                        contentValues.put("sip_name", sipName);
                                        contentValues.put("sip_num", sipNum);
                                        contentValues.put("sip_pwd", sipPwd);
                                        contentValues.put("sip_server", sipServer);
                                        db.update("users", contentValues, "_id = ?", new String[]{"1"});
                                    }
                                }
                                break;
                            }
                        }
                    }
                    for (int i = 0; i < mList.size(); i++) {
                        final SipVideo sipVideo = new SipVideo();
                        sipVideo.setNum(mList.get(i).getNumber());
                        String deviceType = mList.get(i).getVideoBen().getDevicetype();
                        if (!TextUtils.isEmpty(deviceType)) {
                            sipResourcesNum += 1;
                            if (deviceType.equals("ONVIF")) {
                                String ip = mList.get(i).getVideoBen().getIp();
                                final Device device = new Device();
                                device.setVideoBen(mList.get(i).getVideoBen());
                                device.setServiceUrl("http://" + ip + "/onvif/device_service");
                                Onvif onvif = new Onvif(device, new Onvif.GetRtspCallback() {
                                    @Override
                                    public void getDeviceInfoResult(String rtsp, boolean isSuccess, Device mDevice) {
                                        Message message = new Message();
                                        Bundle bundle = new Bundle();
                                        sipVideo.setRtsp(rtsp);
                                        bundle.putSerializable("sipVideo", sipVideo);
                                        message.setData(bundle);
                                        message.what = 111;
                                        handler.sendMessage(message);
                                    }
                                });
                                App.getExecutorService().execute(onvif);
                            } else if (deviceType.equals("RTSP")) {
                                String mRtsp = "rtsp://" + mList.get(i).getVideoBen().getUsername() + ":" + mList.get(i).getVideoBen().getPassword() + "@" + mList.get(i).getIp() + "/" + mList.get(i).getVideoBen().getChannel();
                                Message message = new Message();
                                Bundle bundle = new Bundle();
                                sipVideo.setRtsp(mRtsp);
                                bundle.putSerializable("sipVideo", sipVideo);
                                message.setData(bundle);
                                message.what = 111;
                                handler.sendMessage(message);
                            }
                        }
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("No Sip Infor").setMessage("未获取到sip信息，请检查本机ip是否配置正确~~").create().show();
                        }
                    });
                }
            }
        });
        App.getExecutorService().execute(sipThread);


    }

    //注册到sip服务器
    private void registerSipIntoServer(String sipNum, String sipPwd, String sipServer) {

        if (!SipService.isReady()) {
            Linphone.startService(this);
        }
        Linphone.setAccount(sipNum, sipPwd, sipServer);
        Linphone.login();
    }

    //把sip的num和要播放的rtsp相绑定
    class SipVideo implements Serializable {
        private String rtsp;
        private String num;

        public String getRtsp() {
            return rtsp;
        }

        public void setRtsp(String rtsp) {
            this.rtsp = rtsp;
        }

        public String getNum() {
            return num;
        }

        public void setNum(String num) {
            this.num = num;
        }

        public SipVideo() {
        }

        @Override
        public String toString() {
            return "SipVideo{" +
                    "rtsp='" + rtsp + '\'' +
                    ", num='" + num + '\'' +
                    '}';
        }
    }


    private void getAllVideoResoucesInformation() {
        RequestVideoSourcesThread requestVideoSourcesThread = new RequestVideoSourcesThread(MainActivity.this, new RequestVideoSourcesThread.GetDataListener() {
            @Override
            public void getResult(final List<VideoBen> mList) {

                if (mList != null && mList.size() > 0) {
                    Logutils.i("AAAAAAAAA:" + mList.size());
                    //总数据量
                    videoResourcesNum = mList.size();
                    for (int i = 0; i < mList.size(); i++) {
                        String deviceType = mList.get(i).getDevicetype();
                        if (deviceType.equals("ONVIF")) {
                            String ip = mList.get(i).getIp();
                            final Device device = new Device();
                            device.setVideoBen(mList.get(i));
                            device.setServiceUrl("http://" + ip + "/onvif/device_service");
                            Onvif onvif = new Onvif(device, new Onvif.GetRtspCallback() {
                                @Override
                                public void getDeviceInfoResult(String rtsp, boolean isSuccess, Device mDevice) {
                                    Message message = new Message();
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("device", mDevice);
                                    message.setData(bundle);
                                    message.what = 1000;
                                    handler.sendMessage(message);
                                }
                            });
                            App.getExecutorService().execute(onvif);
                        } else if (deviceType.equals("RTSP")) {
                            String mRtsp = "rtsp://" + mList.get(i).getUsername() + ":" + mList.get(i).getPassword() + "@" + mList.get(i).getIp() + "/" + mList.get(i).getChannel();
                            VideoBen v = mList.get(i);
                            v.setRtsp(mRtsp);
                            Device device = new Device();
                            device.setRtspUrl(mRtsp);
                            device.setVideoBen(v);
                            Message message = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("device", device);
                            message.setData(bundle);
                            message.what = 1000;
                            handler.sendMessage(message);
                        }
                    }
                } else {
                    Logutils.i("AAAAAAAA:<0");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("No Video Infor").setMessage("未获取到VideoResources，请检查本机网络是否连接正常~~").create().show();
                        }
                    });
                    WriteLogToFile.info("No get Video Resources Data !!!");
                }
            }
        });
        requestVideoSourcesThread.start();
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
}

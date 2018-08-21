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
import com.zkth.mst.client.utils.CpuAndRamUtils;
import com.zkth.mst.client.utils.LogUtils;
import com.zkth.mst.client.utils.NetworkUtils;
import com.zkth.mst.client.utils.PhoneUtils;
import com.zkth.mst.client.utils.SharedPreferencesUtils;
import com.zkth.mst.client.utils.SipHttpUtils;
import com.zkth.mst.client.utils.ToastUtils;
import com.zkth.mst.client.utils.VibratorUtils;
import com.zkth.mst.client.utils.WriteLogToFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

    /**
     * 相机对象
     */
    private Camera mCamera;

    /**
     * 显示摄像头图像 的surfaceviewHolder
     */
    SurfaceHolder surfaceHolder;

    /**
     * 当前是哪个摄像头
     * 0：后
     * 1：前
     */
    private static int cameraId = 0;

    /**
     * 远端显示本机采集图像的rtsp地址
     */
    private String RtspAddress;

    /**
     * h26编码
     */
    private VideoMediaCodec mVideoMediaCodec;

    /**
     * 传输rtsp的server
     */
    private RtspServer mRtspServer;

    /**
     * 是否已绑定service
     */
    boolean isBandService = false;

    /**
     * 是否正在录像
     */
    boolean isRecording;

    /**
     * 播放值班室视频的播放器
     */
    NodePlayer nodePlayer;

    /**
     * 显示当前 的通话时间
     */
    TextView tv;

    /**
     * 是否正在向外播打电话
     */
    boolean isCanCallPhone;

    /**
     * 记录sip资源的数量
     */
    int sipResourcesNum = 0;

    /**
     * 记录video资源的数据
     */
    int videoResourcesNum = -1;

    /**
     * 加载动画
     */
    Animation mLoadingAnim;

    /**
     * 模拟值班室名称
     */
    String duryName = "7002话机值班室";//值班值号码信息

    /**
     * 模拟值班室的号码
     */
    String duryNumber = "7002";

    /**
     * 模拟值班室的视频 地址
     */
    String duryRtsp = "rtsp://19.0.0.224:554/H264?ch=1&subtype=2&proto=Onvif";

    /**
     * 应急报警的弹窗
     */
    AlertDialog alarmWindow = null;


    //无网的提示
    @BindView(R.id.no_network_layout)
    RelativeLayout no_network_layout;

    //加载动画的布局
    @BindView(R.id.main_loading_layout)
    ImageView main_loading_layout;

    //加载提示信息的布局
    @BindView(R.id.main_loading_textview_layout)
    TextView main_loading_textview_layout;

    //时间显示 的布局
    @BindView(R.id.main_incon_time)
    TextView main_incon_time;

    /**
     * 存放 sip资源 的集合
     */
    List<SipVideo> sipData = new ArrayList<>();

    /**
     * 存放 video资源 的集合
     */
    List<Device> dataSources = new ArrayList<>();

    /**
     * 本机的ip
     */
    String ip = "";

    /**
     * 当前页面是否正在显示
     */
    private boolean isFront = false;

    /**
     * 显示时间的显示是否正在运行
     */
    boolean threadIsRun = true;

    /**
     * 电量信息
     */
    @BindView(R.id.icon_electritity_show)
    ImageView batteryIcon;

    /**
     * 信号强度
     */
    @BindView(R.id.icon_network)
    ImageView rssiIcon;

    /**
     * 连接状态
     */
    @BindView(R.id.icon_connection_show)
    ImageView connetIConb;







    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            //显示当前 的通话时间
            if (msg.what == 1) {
                num++;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText(PhoneUtils.getTime(num) + "");
                    }
                });
            } else if (msg.what == 111) {//把获取到的sip信息存放 到本地
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
            } else if (msg.what == 1000) {//把获取 的video资源信息存放到本地
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
            } else if (msg.what == 1001) {//显示当前 的系统时间
                long time = System.currentTimeMillis();
                Date date = new Date(time);
                SimpleDateFormat timeD = new SimpleDateFormat("HH:mm:ss");
                String currentTime = timeD.format(date).toString();
                if (!TextUtils.isEmpty(currentTime)) {
                    if (isFront == true)
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

        //获取本机的ip
        ip = NetworkUtils.getIPAddress(true);

        //本机图像传输的rtsp地址
        RtspAddress = "rtsp://" + ip + ":" + RtspServer.DEFAULT_RTSP_PORT;
        mVideoMediaCodec = new VideoMediaCodec();

        //加载动画
        mLoadingAnim = AnimationUtils.loadAnimation(this, R.anim.loading);

        //启动cpu和ram监测
        CpuAndRamUtils.getInstance().init(MainActivity.this, 5 * 1000);
        CpuAndRamUtils.getInstance().start();

        //接收短消息和报警报文
        receiveMessageAndAlarm();
        //获取sip本机信息并注册到sip服务 器
        getNativeSipInformation();
        //获取所有的视频 资源并解析rtsp
        getAllVideoResoucesInformation();

        //  if (TextUtils.isEmpty(duryName) || TextUtils.isEmpty(duryNumber) || TextUtils.isEmpty(duryRtsp))
        //获取值班室信息

//        if (NetworkUtils.isConnected())
//            requestDutyRoomInformation();
//        else
//            ToastUtils.showShort("无网络");

    }

    /**
     * 获取值班室信息
     */
    private void requestDutyRoomInformation() {

        SipHttpUtils request = new SipHttpUtils("http://19.0.0.28/zkth/dutyRoomData.php", new SipHttpUtils.GetHttpData() {
            @Override
            public void httpData(String result) {

                String re = result;
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String code = jsonObject.getString("code");
                    if (code.equals("1")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        JSONObject js = jsonArray.getJSONObject(0);
                        duryName = js.getString("name");
                        duryNumber = js.getString("number");
                        duryRtsp = js.getString("server");
                    } else {
                        duryName = "";
                        duryNumber = "";
                        duryRtsp = "";
                    }
                } catch (JSONException e) {
                    duryName = "";
                    duryNumber = "";
                    duryRtsp = "";
                }
            }
        });
        request.start();

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
            case R.id.button_intercom://sip通话
//                LogUtils.i("TAG", AppConfig.cpu + "///");
//                LogUtils.i("TAG", AppConfig.ram + "///");
//                LogUtils.i("TAG", AppConfig.battery + "///");
//                LogUtils.i("TAG", AppConfig.wifi + "///");
//                LogUtils.i("TAG", AppConfig.lat + "///");
//                LogUtils.i("TAG", AppConfig.log + "///");
//                LogUtils.i("TAG", AppConfig.sipName + "///");
//                LogUtils.i("TAG", AppConfig.sipNum + "///");
//                LogUtils.i("TAG", AppConfig.sipPwd + "///");
//                LogUtils.i("TAG", AppConfig.sipServer + "///");
                VibratorUtils.Vibrate(MainActivity.this, 500);
                intent.setClass(MainActivity.this, MainFragmentActivity.class);
                intent.putExtra("current", 0);
                startActivity(intent);

                break;
            case R.id.button_video://视频 监控
                VibratorUtils.Vibrate(MainActivity.this, 500);
                intent.setClass(MainActivity.this, MainFragmentActivity.class);
                intent.putExtra("current", 1);
                startActivity(intent);


                break;
            case R.id.button_applyforplay://申请开箱
                VibratorUtils.Vibrate(MainActivity.this, 500);
                applyForUnpacking();
                break;
            case R.id.button_chat://sip聊天
                VibratorUtils.Vibrate(MainActivity.this, 500);
                intent.setClass(MainActivity.this, MainFragmentActivity.class);
                intent.putExtra("current", 2);
                startActivity(intent);

                break;
            case R.id.button_setup://设置中心
                VibratorUtils.Vibrate(MainActivity.this, 500);
                intent.setClass(MainActivity.this, SettingCenterActivity.class);
                startActivity(intent);
                break;
            case R.id.button_alarm://应急报警
                VibratorUtils.Vibrate(MainActivity.this, 500);
                sendEmergency();
                break;
        }
    }



    //应急报警
    private void sendEmergency() {

        if (isCanCallPhone) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //应急报警的界面
                    View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.alarm_item_view, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    //创建窗口
                    alarmWindow = builder.setView(view).create();
                    alarmWindow.setCancelable(false);
                    alarmWindow.show();
                    tv = (TextView) view.findViewById(R.id.show_emergency_call_time);
                    NodePlayerView nodePlayerView = view.findViewById(R.id.show_emergency_layout);
                    nodePlayer = new NodePlayer(MainActivity.this);
                    nodePlayer.setPlayerView(nodePlayerView);
                    nodePlayer.setInputUrl(duryRtsp);
                    nodePlayer.start();
                    if (SipService.isReady()) {
                        Logutils.i("isCanCallPhone:" + isCanCallPhone);
                        if (!TextUtils.isEmpty(duryNumber)) {
                            Linphone.callTo(duryNumber, false);
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtils.showShort("未获取到值班室信息");
                                }
                            });
                        }
                    }
                    //切换摄像头
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
                            num = 0;
                            if (SipService.isReady())
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
        } else {
            ToastUtils.showShort("Sip未注册,请检查网络或重新登录");
        }

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
            mCamera.setDisplayOrientation(270);
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
        isFront = true;

        //linphone状态回调
        Linphone.addCallback(new RegistrationCallback() {
            @Override
            public void registrationProgress() {
                LogUtils.i("TAG", "registering");
            }

            @Override
            public void registrationOk() {
                LogUtils.i("TAG", "registrationOk");
                isCanCallPhone = true;
                updateUi(connetIConb,R.mipmap.icon_connection_normal);
            }

            @Override
            public void registrationFailed() {
                LogUtils.i("TAG", "registrationFailed");
                isCanCallPhone = false;
                updateUi(connetIConb,R.mipmap.icon_connection_disable);
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


        int level = AppConfig.battery;
        if (level >= 75 && level <= 100) {
            updateUi(batteryIcon, R.mipmap.icon_electricity_a);
        }
        if (level >= 50 && level < 75) {
            updateUi(batteryIcon, R.mipmap.icon_electricity_b);
        }
        if (level >= 25 && level < 50) {
            updateUi(batteryIcon, R.mipmap.icon_electricity_c);
        }
        if (level >= 0 && level < 25) {
            updateUi(batteryIcon, R.mipmap.icon_electricity_disable);
        }

        int rssi = AppConfig.wifi;
        if (rssi > -50 && rssi < 0) {
            updateUi(rssiIcon, R.mipmap.icon_network);
        } else if (rssi > -70 && rssi <= -50) {
            updateUi(rssiIcon, R.mipmap.icon_network_a);
        } else if (rssi < -70) {
            updateUi(rssiIcon, R.mipmap.icon_network_b);
        } else if (rssi == -200) {
            updateUi(rssiIcon, R.mipmap.icon_network_disable);
        }

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
                MainActivity.this.runOnUiThread(new Runnable() {
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

                MainActivity.this.runOnUiThread(new Runnable() {
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


    //获取CMS的视频数据并解析rtsp
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgressFail("未获取到CMS数据",3*1000);
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


    @Override
    protected void onPause() {
        super.onPause();
        isFront = false;
    }


    /**
     * 更新UI
     */
    public void updateUi(final ImageView imageView, final int n) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setBackgroundResource(n);
            }
        });
    }
}

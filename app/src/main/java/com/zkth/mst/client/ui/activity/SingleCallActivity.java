package com.zkth.mst.client.ui.activity;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zkth.mst.client.R;
import com.zkth.mst.client.base.AppConfig;
import com.zkth.mst.client.base.BaseActivity;
import com.zkth.mst.client.entity.SipBean;
import com.zkth.mst.client.linphone.Linphone;
import com.zkth.mst.client.linphone.PhoneCallback;
import com.zkth.mst.client.rtsp.RtspServer;
import com.zkth.mst.client.rtsp.media.VideoMediaCodec;
import com.zkth.mst.client.rtsp.record.Constant;
import com.zkth.mst.client.utils.PhoneUtils;

import org.linphone.core.LinphoneCall;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.nodemedia.NodePlayer;
import cn.nodemedia.NodePlayerDelegate;
import cn.nodemedia.NodePlayerView;

/**
 * Created by Root on 2018/7/16.
 */

public class SingleCallActivity extends BaseActivity implements View.OnClickListener, Camera.PreviewCallback, SurfaceHolder.Callback, NodePlayerDelegate {

    @BindView(R.id.secodary_surfacevie)
    public SurfaceView secodary_surfacevie;


    @BindView(R.id.show_call_time)
    public TextView show_call_time;

    @BindView(R.id.btn_handup_icon)
    public ImageButton hangupButton;

    @BindView(R.id.btn_mute)
    public ImageButton btn_mute;

    @BindView(R.id.btn_volumeadd)
    public ImageButton btn_volumeadd;

    @BindView(R.id.btn_camera)
    public ImageButton btn_camera;

    @BindView(R.id.btn_volumelow)
    public ImageButton btn_volumelow;


    @BindView(R.id.framelayout_bg_layout)
    public FrameLayout framelayout_bg_layout;

    @BindView(R.id.relativelayout_bg_layout)
    public RelativeLayout relativelayout_bg_layout;

    @BindView(R.id.image_bg_layout)
    public ImageView image_bg_layout;

    @BindView(R.id.text_who_is_calling_information)
    public TextView text_who_is_calling_information;

    @BindView(R.id.bottom_sliding_recyclerview)
    public RecyclerView bottomSlidingView;
    @BindView(R.id.single_sur_sow)
    TextView single_sur_sow;

    @BindView(R.id.main_view)
    public NodePlayerView np;
    ExecutorService fixedThreadPool = null;
    List<SipBean> sipListResources = new ArrayList<>();
    AudioManager mAudioManager = null;
    NodePlayer nodePlayer;
    boolean isCall = true;//来源是打电话还是接电话，true为打电话，false为接电话
    String userName = "wpf";
    boolean isVideo = false;
    String rtsp = "";//可视电话的视频地址
    boolean isSilent = false;//是否静音
    private Boolean isCallConnected = false;//是否已接通
    boolean mWorking = false;
    //计时的子线程
    Thread mThread = null;
    int num = 0;
    String native_name = "";


    private RtspServer mRtspServer;
    private String RtspAddress;
    private SurfaceHolder surfaceHolder;//小窗口
    private Camera mCamera;
    private VideoMediaCodec mVideoMediaCodec;
    // private AvcEncoder avcEncoder;
    private boolean isRecording = false;
    private static int cameraId = 0;//默认后置摄像头
    boolean isBandService = false;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    num++;

                    if (isCallConnected){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                show_call_time.setText(PhoneUtils.getTime(num) + "");
                            }
                        });
                    }

                    break;
            }
        }
    };


    @Override
    public void initView() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        nodePlayer = new NodePlayer(SingleCallActivity.this);
        np.setRenderType(NodePlayerView.RenderType.SURFACEVIEW);
        nodePlayer.setPlayerView(np);
        nodePlayer.setNodePlayerDelegate(this);

        hangupButton.setOnClickListener(this);
        btn_camera.setOnClickListener(this);
        btn_volumelow.setOnClickListener(this);
        secodary_surfacevie.setZOrderOnTop(true);
        btn_mute.setOnClickListener(this);
        btn_volumeadd.setOnClickListener(this);

        surfaceHolder = secodary_surfacevie.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setFixedSize(Constant.VIDEO_WIDTH, Constant.VIDEO_HEIGHT);
        surfaceHolder.setKeepScreenOn(true);

        RtspAddress = "rtsp://19.0.0.77:" + RtspServer.DEFAULT_RTSP_PORT;
        mVideoMediaCodec = new VideoMediaCodec();
        if (RtspAddress != null) {
            // SharedPreferencesUtils.putObject(mContext,AppConfig.NATIVE_RTSP,RtspAddress);
            Log.i("tag", "地址: " + RtspAddress);
        }

    }

    @Override
    public void initData() {
//        String sipResult = (String) SharedPreferencesUtils.getObject(SingleCallActivity.this, "sipresult", "");
//        if (!TextUtils.isEmpty(sipResult)) {
//            Gson gson = new Gson();
//            Type type = new TypeToken<List<MainActivity.SipVideo>>() {
//            }.getType();
//            List<MainActivity.SipVideo> alterSamples = new ArrayList<>();
//            alterSamples = gson.fromJson(sipResult, type);
//            if (alterSamples != null) {
//                sipData = alterSamples;
//            }
//        }

//        fixedThreadPool = Executors.newFixedThreadPool(5);
//        native_name = AppConfig.native_sip_name;
        isCall = this.getIntent().getBooleanExtra("isCall", true);//是打电话还是接电话
        userName = this.getIntent().getStringExtra("userName");//对方号码
        isVideo = this.getIntent().getBooleanExtra("isVideo", false);//是可视频电话，还是语音电话


//        sipListResources = new ArrayList<>();
//        Logutils.i("UserName:" + userName);
//        Logutils.i("native_name:" + native_name);
//        Logutils.i("isCall:" + isCall);
//        Logutils.i("isVideo:" + isVideo);
        //电话监听回调
        phoneCallback();

        //判断来电是否已接通
        boolean isConnet = this.getIntent().getBooleanExtra("isCallConnected", false);
        if (isConnet) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    show_call_time.setText("00:00");
                    hangupButton.setBackgroundResource(R.drawable.port_btn_hang_up_selected);
                    text_who_is_calling_information.setText("正在与" + userName + "通话");
                    threadStart();
                }
            });
        }
        //向外播放电话
        if (isCall) {
            Linphone.callTo(userName, false);
        }
        //视频电话向外播打电话
        if (isCall && isVideo) {
            text_who_is_calling_information.setVisibility(View.GONE);
//            main_player_framelayout.setVisibility(View.VISIBLE);
//            second_player_relativelayout.setVisibility(View.VISIBLE);
            framelayout_bg_layout.setVisibility(View.VISIBLE);
            relativelayout_bg_layout.setVisibility(View.VISIBLE);
            image_bg_layout.setVisibility(View.GONE);

//            for (MainActivity.SipVideo sipData : sipData) {
//                if (sipData.getNum().equals(userName)) {
//                    if (!TextUtils.isEmpty(sipData.getRtsp())) {
//                        nodePlayer.setInputUrl(sipData.getRtsp());
//                        nodePlayer.setAudioEnable(false);
//                        nodePlayer.start();
//                    } else {
//                    }
//                    break;
//                } else {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            single_sur_sow.setText("未获取到视频信息~");
//                        }
//                    });
//                }
//            }


        }
        initBottomData();

    }

    @Override
    protected int getLayoutId() {
        return R.layout.calling_activity;
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_handup_icon:
                Linphone.hangUp();
                num = 0;
                threadStop();

                break;
            case R.id.btn_mute:
                if (!isSilent) {
                    Linphone.toggleMicro(true);
                    btn_mute.setBackgroundResource(R.mipmap.port_btn_mute_selected);
                    isSilent = true;
                } else {
                    Linphone.toggleMicro(false);
                    btn_mute.setBackgroundResource(R.mipmap.port_btn_mute_normal);
                    isSilent = false;
                }
                break;
            //前后摄像头的转换
            case R.id.btn_camera:
                if (cameraId == 0) {
                    cameraId = 1;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            btn_camera.setBackgroundResource(R.mipmap.port_btn_custom_camera_normal);
                        }
                    });
                } else {
                    cameraId = 0;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            btn_camera.setBackgroundResource(R.mipmap.port_btn_custom_camera_presected);
                        }
                    });
                }
                initCamera();
                try {
                    play();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_volumeadd:
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                break;
            //音量减
            case R.id.btn_volumelow:
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                break;
        }
    }


    private void phoneCallback() {

        Linphone.addCallback(null, new PhoneCallback() {
            @Override
            public void incomingCall(LinphoneCall linphoneCall) {

            }

            @Override
            public void outgoingInit() {

                isCallConnected = true;
                if (isCallConnected) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            show_call_time.setText("00:00");
                            hangupButton.setBackgroundResource(R.drawable.port_btn_hang_up_selected);
                            text_who_is_calling_information.setText("正在响铃《  " + userName + "  》");
                        }
                    });
                }
            }

            @Override
            public void callConnected() {
                if (isCallConnected) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            show_call_time.setText("00:00");
                            hangupButton.setBackgroundResource(R.drawable.port_btn_hang_up_selected);
                            text_who_is_calling_information.setText("正在与" + userName + "通话");
                            threadStart();
                        }
                    });
                }

            }

            @Override
            public void callEnd() {

            }

            @Override
            public void callReleased() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        show_call_time.setText("00:00");
                        hangupButton.setBackgroundResource(R.drawable.port_btn_answer_selected);
                    }
                });
                if (nodePlayer != null) {
                    nodePlayer.pause();
                    nodePlayer.stop();
                    nodePlayer.release();
                    nodePlayer = null;
                }
                SingleCallActivity.this.finish();
            }

            @Override
            public void error() {

            }
        });
    }


    private void initBottomData() {
//
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(SingleCallActivity.this, 1);
//        gridLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//        bottomSlidingView.setLayoutManager(gridLayoutManager);
//        int images[] = new int[]{R.drawable.port_network_intercom_selected, R.drawable.port_instant_messaging_selected, R.drawable.port_video_surveillance_selected, R.drawable.port_alarm_btn_selected, R.drawable.port_bullet_btn_selected};
//        ButtomSlidingAdapter ada = new ButtomSlidingAdapter(SingleCallActivity.this, images, 0);
//        bottomSlidingView.setAdapter(ada);

    }

    /**
     * 计时线程开启
     */
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

    /**
     * 计时线程停止
     */
    public void threadStop() {
        if (mWorking) {
            if (mThread != null && mThread.isAlive()) {
                mThread.interrupt();
                mThread = null;
            }
            show_call_time.setText("00:00");
            mWorking = false;
        }
    }


    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        //前后摄像头的数据采集,根据前后进行相应的视频流旋转
//        Log.d("views","data:  "+data.length);
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
                new AlertDialog.Builder(SingleCallActivity.this)
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
                        Toast.makeText(SingleCallActivity.this, "RTSP STREAM STARTED", Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (message == RtspServer.MESSAGE_STREAMING_STOPPED) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(SingleCallActivity.this, "RTSP STREAM STOPPED", Toast.LENGTH_SHORT).show();
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


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        initCamera();
        try {
            play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }


    /**
     * 初始化相机参数
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
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
    protected void onPause() {
        super.onPause();

        if (isCall && isVideo) {
            if (mRtspServer != null)
                mRtspServer.removeCallbackListener(mRtspCallbackListener);
            if (mRtspServiceConnection != null)
                unbindService(mRtspServiceConnection);
        }

    }

    @Override
    public void onEventCallback(NodePlayer player, int event, final String msg) {
        if (event == 1003) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    single_sur_sow.setText(msg);
                }
            });
        }
    }
}

package com.zkth.mst.client.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.zkth.mst.client.R;
import com.zkth.mst.client.apk.UpdateManager;
import com.zkth.mst.client.base.AppConfig;
import com.zkth.mst.client.base.BaseActivity;
import com.zkth.mst.client.base.DbConfig;
import com.zkth.mst.client.callbacks.LoginCMSThread;
import com.zkth.mst.client.db.DatabaseHelper;
import com.zkth.mst.client.db.Logutils;
import com.zkth.mst.client.entity.User;
import com.zkth.mst.client.utils.ActivityUtils;
import com.zkth.mst.client.utils.NetworkUtils;
import com.zkth.mst.client.utils.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 登录首页面
 */

public class LoginActivity extends BaseActivity {

    /**
     * 无网络时提示
     */
    @BindView(R.id.no_network_layout)
    RelativeLayout noNetWorkShow;

    /**
     * 加载时的动画
     */
    Animation mLoadingAnim;

    /**
     * 本机iP
     */
    String nativeIP = "";

    /**
     * 数据库对象
     */
    DatabaseHelper databaseHelper;

    //进度动画
    @BindView(R.id.image_loading)
    ImageView image_loading;

    //登录错误信息提示
    @BindView(R.id.loin_error_infor_layout)
    TextView errorInfor;

    //登录按钮
    @BindView(R.id.userlogin_button_layout)
    Button userlogin_button_layout;

    //用户名
    @BindView(R.id.edit_username_layout)
    EditText userName;

    //密码
    @BindView(R.id.edit_userpass_layout)
    EditText userPwd;

    //记住密码Checkbox
    @BindView(R.id.remember_pass_layout)
    Checkable rememberPwd;

    //自动登录CheckBox
    @BindView(R.id.auto_login_layout)
    Checkable autoLoginCheckBox;

    //服务器
    @BindView(R.id.edit_serviceip_layout)
    EditText serverIp;

    //修改服务器的checkbox
    @BindView(R.id.remembe_serverip_layout)
    CheckBox updateServerIpCheckBox;

    /**
     * 整个项目需要申请的权限
     */
    String[] permissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.USE_SIP,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    /**
     * 存放未申请成功的权限
     */
    List<String> mPermissionList = new ArrayList<>();

    /**
     * 地图对象
     */
    public LocationClient mLocationClient = null;

    /**
     * 位置回调
     */
    private MyLocationListener myListener = new MyLocationListener();

    @Override
    public int getLayoutId() {
        return R.layout.activity_login_page;
    }

    @Override
    public void initView() {
        //选中修改地址的check时，使服务器地址可修改的样式，反之不可编辑状态
        updateServerIpCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    serverIp.setEnabled(true);
                } else {
                    serverIp.setEnabled(false);
                }
            }
        });
    }

    @Override
    public void initData() {

        UpdateManager updateManager = new UpdateManager(this);
        updateManager.checkUpdate();

        //百度地图获取定位信息
        initBDLocation();

        //申请权限
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            checkPermission();
//        }

        //初始化数据库对象
        databaseHelper = new DatabaseHelper(LoginActivity.this);

        //动画
        mLoadingAnim = AnimationUtils.loadAnimation(this, R.anim.loading);

        //获取本机ip
        if (NetworkUtils.isConnected()) {
            nativeIP = NetworkUtils.getIPAddress(true);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    errorInfor.setVisibility(View.VISIBLE);
                    errorInfor.setText("网络不可用");
                }
            });
        }

        //是否自动登录
        boolean isAutoLogin = (boolean) SharedPreferencesUtils.getObject(LoginActivity.this, "autologin", false);
        if (isAutoLogin == true) {
            loginSuccess();
        }
        //是否记住密码
        boolean isrePwd = (boolean) SharedPreferencesUtils.getObject(LoginActivity.this, "isremember", false);
        //如果是记住密码就从数据库中读取信息并显示
        if (isrePwd == true) {
            //
            rememberPwd.setChecked(true);
            String db_name = DbConfig.getInstance().getData(0);
            if (!TextUtils.isEmpty(db_name)) {
                userName.setText(db_name);
            }
            String db_pwd = DbConfig.getInstance().getData(1);
            if (!TextUtils.isEmpty(db_pwd)) {
                userPwd.setText(db_pwd);
            }

            String db_server = DbConfig.getInstance().getData(12);
            if (!TextUtils.isEmpty(db_server)) {
                serverIp.setText(db_server);
                serverIp.setEnabled(false);
            }
        }
    }

    private void initBDLocation() {
        mLocationClient = new LocationClient(getApplicationContext());
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(1000);
        option.setOpenGps(true);
        option.setLocationNotify(true);
        option.setIgnoreKillProcess(false);
        option.SetIgnoreCacheException(false);
        option.setWifiCacheTimeOut(5 * 60 * 1000);
        option.setEnableSimulateGps(false);
        mLocationClient.setLocOption(option);
        mLocationClient.registerLocationListener(myListener);
        mLocationClient.start();

    }

    String name = "";
    String pass = "";
    String server_IP = "";
    //是否记住密码
    boolean isRemember;
    //是否自动 登录
    boolean isAuto;


    @OnClick(R.id.userlogin_button_layout)
    public void loginCMS(View view) {
        errorInfor.setText("");
        //获取当前输入框内的内容
        name = userName.getText().toString().trim();
        pass = userPwd.getText().toString().trim();
        server_IP = serverIp.getText().toString().trim();
        //判断输入框是否为null
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(server_IP)) {
            //判断网络是否连接成功
            if (NetworkUtils.isConnected()) {
                //加载动画显示
                image_loading.setVisibility(View.VISIBLE);
                image_loading.startAnimation(mLoadingAnim);
                //正则判断服务器ip是否合法
                if (!TextUtils.isEmpty(server_IP)) {
                    if (!NetworkUtils.isboolIp(server_IP)) {
                        errorInfor.setVisibility(View.VISIBLE);
                        errorInfor.setText("服务器ip不合法,请重新输入");
                        image_loading.setVisibility(View.GONE);
                        image_loading.clearAnimation();
                    }
                }
                //判断是否获取了本机的ip信息
                if (TextUtils.isEmpty(nativeIP)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            errorInfor.setVisibility(View.VISIBLE);
                            errorInfor.setText("未获取到本机IP");
                            image_loading.setVisibility(View.GONE);
                            image_loading.clearAnimation();
                        }
                    });
                    return;
                }
                //子线程进行登陆并回调返回登录结果
                LoginCMSThread loginPassThread = new LoginCMSThread(name, pass, nativeIP, server_IP, new LoginCMSThread.LoginCallback() {
                    @Override
                    public void getLoginStatus(int count) {
                        //如果返回的结果大于0就说明登录 成功
                        if (count > 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    isRemember = rememberPwd.isChecked();
                                    isAuto = autoLoginCheckBox.isChecked();

                                    //判断当前是否记住密码，如果记住密码就把配置信息提前插入数据库
                                    if (isRemember == true) {
                                        if (isAuto == true) {
                                            SharedPreferencesUtils.putObject(LoginActivity.this, "autologin", isRemember);
                                        }
                                        //保存记住密码的状态
                                        SharedPreferencesUtils.putObject(LoginActivity.this, "isremember", isRemember);
                                    }
                                    User user = new User();
                                    user.setServerip(server_IP);
                                    user.setName(name);
                                    user.setPass(pass);
                                    user.setLogin_port("2010");
                                    user.setAlarm_ip("19.0.0.27");
                                    user.setAlarm_port("2000");
                                    user.setHeader_port("2020");
                                    user.setLogin_time(new Date().toString());
                                    user.setNativeip(nativeIP);
                                    //向数据库中的users配置表中插入数据
                                    databaseHelper.insertOneUser(user);
//                                    Logutils.i("insert success");
                                    //加载动画消失并提示登录成功
                                    loginSuccess();
                                }
                            });
                        } else {
                            //提示加载失败
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    image_loading.setVisibility(View.GONE);
                                    image_loading.clearAnimation();
                                    errorInfor.setVisibility(View.VISIBLE);
                                    errorInfor.setText("Login fail");
                                }
                            });
                        }
                    }
                });
                loginPassThread.start();
            } else {
                //显示没网界面
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        noNetWorkShow.setVisibility(View.VISIBLE);
                        errorInfor.setVisibility(View.VISIBLE);
                        errorInfor.setText("No Network");
                    }
                });
            }
        } else {
            //提示信息缺失
            image_loading.setVisibility(View.GONE);
            image_loading.clearAnimation();
            errorInfor.setVisibility(View.VISIBLE);
            errorInfor.setText("你少填写信息啦~");
        }
    }

    /**
     * 登录成功跳转
     */
    public void loginSuccess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                image_loading.setVisibility(View.GONE);
                image_loading.clearAnimation();
                errorInfor.setVisibility(View.VISIBLE);
                errorInfor.setText("");
            }
        });

        //跳转到主页面并finish本页面
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        LoginActivity.this.startActivity(intent);
        //overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
        // ActivityUtils.removeActivity(LoginPageActivity.this);
        LoginActivity.this.finish();
    }

    /**
     * 网络状态的时时监听
     *
     * @param state
     * @param name
     */
    @Override
    public void onNetChange(final int state, String name) {
        Logutils.i("//////" + state + "///" + name);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (state == 5 || state == -1) {
                    noNetWorkShow.setVisibility(View.VISIBLE);
                } else {
                    noNetWorkShow.setVisibility(View.GONE);
                    errorInfor.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * 按下返回键的状态(退出)
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityUtils.removeActivity(this);
        LoginActivity.this.finish();
    }

    /**
     * 按home键时保存当前的输入状态
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Logutils.i("按了home键，put信息");
        outState.putString("name", userName.getText().toString().trim());
        outState.putString("pass", userPwd.getText().toString().trim());
        outState.putString("serverip", serverIp.getText().toString().trim());
    }

    /**
     * 恢复刚才的输入状态
     *
     * @param savedInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Logutils.i("get信息");
        userName.setText(savedInstanceState.getString("name"));
        userPwd.setText(savedInstanceState.getString("pass"));
        serverIp.setText(savedInstanceState.getString("serverip"));
    }


    private void checkPermission() {
        mPermissionList.clear();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(LoginActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permission);
            }
        } /** * 判断存储委授予权限的集合是否为空 */
        if (!mPermissionList.isEmpty()) {
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            ActivityCompat.requestPermissions(LoginActivity.this, permissions, 1);
        } else {
            //未授予的权限为空，表示都授予了 // 后续操作...
            // delayEntryPage();
            initData();
        }

    }


    boolean mShowRequestPermission = true;//用户是否

    //权限回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        //判断是否勾选禁止后不再询问
                        boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissions[i]);
                        if (showRequestPermission) {//
                            checkPermission();//重新申请权限
                            return;
                        } else {
                            mShowRequestPermission = false;//已经禁止
                            String permisson = permissions[i];
                            Log.w("TAG", "permisson:" + permisson);
                        }
                    }
                }
                //  delayEntryPage();
                initData();
                break;
            default:
                break;
        }
    }

    //位置监听回调
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            double latitude = location.getLatitude();    //获取纬度信息
            double longitude = location.getLongitude();    //获取经度信息
            AppConfig.lat = latitude;
            AppConfig.log = longitude;
        }
    }
}

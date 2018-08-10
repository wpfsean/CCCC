package com.zkth.mst.client.ui.activity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.zkth.mst.client.R;
import com.zkth.mst.client.base.App;
import com.zkth.mst.client.base.AppConfig;
import com.zkth.mst.client.base.BaseActivity;
import com.zkth.mst.client.base.DbConfig;
import com.zkth.mst.client.callbacks.LoginPassThread;
import com.zkth.mst.client.db.DatabaseHelper;
import com.zkth.mst.client.db.Logutils;
import com.zkth.mst.client.entity.User;
import com.zkth.mst.client.utils.NetworkUtils;
import com.zkth.mst.client.utils.PhoneUtils;
import com.zkth.mst.client.utils.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class LoginActivity extends BaseActivity {

    /**
     * 加载时的动画
     */
    Animation mLoadingAnim;

    /**
     * 整个项目可能用到的权限
     */
    String[] permissions = new String[]{
            Manifest.permission.USE_SIP,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * 存放未授权的权限
     */
    List<String> mPermissionList = new ArrayList<>();

    /**
     * 地图监听
     */
    MyLocationListener mMyLocationListener;

    //断网提示
    @BindView(R.id.no_network_layout)
    RelativeLayout no_network_layout;

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
     * 地图对象
     */
    public LocationClient mLocationClient = null;

    /**
     * 本机iP
     */
    String nativeIP = "";

    /**
     * 数据库对象
     */
    DatabaseHelper databaseHelper;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    public void initView() {
        //验证权限
        verifyPermissions();
    }

    /**
     * 判断权限是否需要申请
     */
    private void verifyPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermission();
        } else {
            initThisPageData();
        }
    }

    /**
     * Init数据
     */
    private void initThisPageData() {

        //获取地理位置
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(1000);
        option.setIsNeedAddress(true);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.setLocOption(option);

        mMyLocationListener = new MyLocationListener();
        //声明LocationClient类
        mLocationClient.registerLocationListener(mMyLocationListener);
        mLocationClient.start();
    }

    /**
     * 申请权限
     */
    private void requestPermission() {
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
            initThisPageData();
        }
    }

    boolean mShowRequestPermission = true;//用户是否禁止权限

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
                            requestPermission();//重新申请权限
                            return;
                        } else {
                            mShowRequestPermission = false;//已经禁止
                            String permisson = permissions[i];
                            android.util.Log.w("TAG", "permisson:" + permisson);
                        }
                    }
                }
                initThisPageData();
                break;
            default:
                break;
        }
    }


    @Override
    public void initData() {

        Logutils.i(DbConfig.getInstance().getData(0) + "当前用户名");
        Logutils.i(DbConfig.getInstance().getData(1) + "当前密码");
        Logutils.i(DbConfig.getInstance().getData(2) + "用户登录时间");
        Logutils.i(DbConfig.getInstance().getData(3) + "本机ip");
        Logutils.i(DbConfig.getInstance().getData(4) + "心跳端口");
        Logutils.i(DbConfig.getInstance().getData(5) + "登录端口");
        Logutils.i(DbConfig.getInstance().getData(6) + "报警端口");
        Logutils.i(DbConfig.getInstance().getData(7) + "当前sip名称");
        Logutils.i(DbConfig.getInstance().getData(8) + "当前sip号码");
        Logutils.i(DbConfig.getInstance().getData(9) + "sip服务器地址");
        Logutils.i(DbConfig.getInstance().getData(10) + "sip密码");
        Logutils.i(DbConfig.getInstance().getData(11) + "报警ip");
        Logutils.i(DbConfig.getInstance().getData(12) + "服务器ip");

        //获取本机的Ip
        nativeIP = NetworkUtils.getIPAddress(true);

        //动画
        mLoadingAnim = AnimationUtils.loadAnimation(this, R.anim.loading);

        //初始化数据库对象
        databaseHelper = new DatabaseHelper(LoginActivity.this);

        //判断上次是否是记住密码
        boolean isrePwd = (boolean) SharedPreferencesUtils.getObject(LoginActivity.this, "isremember", false);

        //如果是记住密码就从数据库中读取信息并显示
        if (isrePwd == true) {
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
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        // mLocationClient.unRegisterLocationListener(mMyLocationListener);
    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            double latitude = location.getLatitude();    //获取纬度信息
            double longitude = location.getLongitude();    //获取经度信息
            AppConfig.lat = latitude;
            AppConfig.log = longitude;

            float radius = location.getRadius();    //获取定位精度，默认值为0.0f
            String coorType = location.getCoorType();
            int errorCode = location.getLocType();
            String addr = location.getAddrStr();    //获取详细地址信息
            String country = location.getCountry();    //获取国家
            String province = location.getProvince();    //获取省份
            String city = location.getCity();    //获取城市
            String district = location.getDistrict();    //获取区县
            String street = location.getStreet();    //获取街道信息
            //  LogUtils.i("TAG", latitude + "\n" + longitude + "\n" + radius + "\n" + coorType + "\n" + errorCode);
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

    @Override
    public void onNetworkViewRefresh() {
        showProgressDialogWithText("正在重新加载...");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dismissProgressDialog();
                showContentView();
            }
        }, 2 * 1000);
    }


    String name = "";
    String pass = "";
    String server_IP = "";
    //是否记住密码
    boolean isRemember;
    //是否自动 登录
    boolean isAuto;

    //登录到cms服务器
    @OnClick(R.id.userlogin_button_layout)
    public void loginCMS(View view) {
        errorInfor.setText("");

        name = userName.getText().toString().trim();
        pass = userPwd.getText().toString().trim();
        server_IP = serverIp.getText().toString().trim();

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(server_IP)) {
            if (NetworkUtils.isConnected()) {
                image_loading.setVisibility(View.VISIBLE);
                image_loading.startAnimation(mLoadingAnim);

                LoginPassThread loginPassThread = new LoginPassThread(name, pass, nativeIP, new LoginPassThread.LoginCallback() {
                    @Override
                    public void getLoginStatus(int count) {
                        if (count > 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    isRemember = rememberPwd.isChecked();
                                    isAuto = autoLoginCheckBox.isChecked();
                                    if (isRemember == true)
                                        SharedPreferencesUtils.putObject(LoginActivity.this, "isremember", isRemember);
                                    User user = new User();
                                    user.setServerip(server_IP);
                                    user.setName(name);
                                    user.setPass(pass);
                                    user.setLogin_port("2010");
                                    user.setAlarm_ip("19.0.0.27");
                                    user.setAlarm_port("2000");
                                    user.setHeader_port("2020");
                                    user.setLogin_time(new Date().toString());
                                    if (!TextUtils.isEmpty(nativeIP)) {
                                        user.setNativeip(nativeIP);
                                    } else {
                                        user.setNativeip("127.0.0.1");
                                    }
                                    databaseHelper.insertOneUser(user);
                                    Logutils.i("insert success");
                                    image_loading.setVisibility(View.GONE);
                                    image_loading.clearAnimation();
                                    errorInfor.setVisibility(View.VISIBLE);
                                    errorInfor.setText("");
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    LoginActivity.this.startActivity(intent);
                                    LoginActivity.this.finish();
                                }
                            });
                        } else {
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
                showNoNetworkView();
            }
        } else {
            image_loading.setVisibility(View.GONE);
            image_loading.clearAnimation();
            errorInfor.setVisibility(View.VISIBLE);
            errorInfor.setText("你少填写信息啦~");
        }
    }
}

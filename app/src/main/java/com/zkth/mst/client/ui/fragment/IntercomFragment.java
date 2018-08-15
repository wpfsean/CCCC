package com.zkth.mst.client.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.GridLayout;
import android.widget.Toast;

import com.zkth.mst.client.R;
import com.zkth.mst.client.adapter.SipGroupAdapter;
import com.zkth.mst.client.base.BaseFragment;
import com.zkth.mst.client.callbacks.SipGroupResourcesCallback;
import com.zkth.mst.client.entity.SipGroupBean;
import com.zkth.mst.client.linphone.SipService;
import com.zkth.mst.client.ui.activity.SingleCallActivity;
import com.zkth.mst.client.ui.activity.SipInforActivity;
import com.zkth.mst.client.utils.NetworkUtils;
import com.zkth.mst.client.utils.SipHttpUtils;
import com.zkth.mst.client.utils.ToastUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Root on 2018/8/6.
 */

public class IntercomFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    /**
     *  显示数据 的recyclerview
     */
    @BindView(R.id.sip_group_recyclearview)
    public RecyclerView recyclearview;

    /**
     * 下拉刷新布局
     */
    @BindView(R.id.sipgrou_intercom_refreshlayout)
    SwipeRefreshLayout refreshLayout;

    /**
     * 盛放数据的集合
     */
    List<SipGroupBean> mList = new ArrayList<>();

    /**
     * 模拟值班室名称
     */
    String duryName = "";//值班值号码信息

    /**
     * 模拟值班室的号码
     */
    String duryNumber = "";

    /**
     * 模拟值班室的视频 地址
     */
    String duryRtsp = "";

    @Override
    protected int getLayoutId() {
        return R.layout.sipgroup_intercom_layout;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        refreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        refreshLayout.setOnRefreshListener(this);

        getSipGroupResources();

        requestDutyRoomInformation();

    }



    /**
     * 获取值班室信息
     */
    private void requestDutyRoomInformation() {

        SipHttpUtils request = new SipHttpUtils("http://wesk.top/zhketech/dutyRoomData.php", new SipHttpUtils.GetHttpData() {
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


    /**
     * 获取要展示 的数据
     */
    private void getSipGroupResources() {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgressDialogWithText("正在加载数据...");
            }
        });
        //先清空集合内的数据
        if (mList != null && mList.size() > 0) {
            mList.clear();
        }
        //判断网络状态
        if (NetworkUtils.isConnected()) {
            //数据请求
            SipGroupResourcesCallback sipGroupResourcesCallback = new SipGroupResourcesCallback(new SipGroupResourcesCallback.SipGroupDataCallback() {
                @Override
                public void callbackSuccessData(final List<SipGroupBean> dataList) {
                    if (dataList != null && dataList.size() > 0) {
                        mList = dataList;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissProgressDialog();
                                SipGroupAdapter adapter = new SipGroupAdapter(getActivity(), mList);
                                recyclearview.setAdapter(adapter);
                                GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
                                gridLayoutManager.setReverseLayout(false);
                                gridLayoutManager.setOrientation(GridLayout.VERTICAL);
                                recyclearview.setLayoutManager(gridLayoutManager);
                                adapter.setItemClickListener(new SipGroupAdapter.MyItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, int position) {
                                        int group_id = mList.get(position).getGroup_id();
                                        Intent intent = new Intent();
                                        intent.putExtra("group_id", group_id);
                                        intent.setClass(getActivity(), SipInforActivity.class);
                                        startActivity(intent);
                                        getActivity().finish();
                                    }
                                });
                            }
                        });
                    } else {
                        //无数据提示
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showEmptyView();
                            }
                        });
                    }
                }
            });
            sipGroupResourcesCallback.start();
        } else {
            //没网提示
            showNoNetworkView();
        }
    }

    @Override
    public void onNetworkViewRefresh() {
        //重新加载数据
        showProgressDialogWithText("重新加载数据...");
        //重新获取 sip数据
        getSipGroupResources();
    }

    /**
     * 点击事件
     * @param view
     */
    @OnClick({R.id.sip_group_lastpage_layout, R.id.sip_group_nextpage_layout, R.id.video_calls_duty_room_intercom_layout, R.id.voice_calls_duty_room_intercom_layout})
    public void onclickEvent(View view) {
        switch (view.getId()) {
            case R.id.sip_group_lastpage_layout://上翻页
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showShort("No More Data!");
                    }
                });
                break;
            case R.id.sip_group_nextpage_layout://下翻页
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showShort("No More Data!");
                    }
                });
                break;
            case R.id.video_calls_duty_room_intercom_layout://视频电话
                call(1);
                break;
            case R.id.voice_calls_duty_room_intercom_layout://语音对讲
                call(0);
                break;
        }
    }
    /**
     * 向外打电话
     *
     * 1、视频电话
     * 2、语音对话
     */
    public void call(int type) {
        if (TextUtils.isEmpty(duryNumber)) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showShort("未获取到值班室信息!!!");
                }
            });
            return;
        }
        if (!SipService.isReady()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showShort("未获取到值班室信息!!!");
                }
            });
            return;
        }
        Intent intent = new Intent();
        intent.setClass(getActivity(), SingleCallActivity.class);
        intent.putExtra("userName", duryNumber);
        intent.putExtra("isCall", true);
        if (type == 0) {
            intent.putExtra("isVideo", true);
        }
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getSipGroupResources();
                refreshLayout.setRefreshing(false);
            }
        }, 2 * 1000);
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }
}

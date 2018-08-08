package com.zkth.mst.client.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.GridLayout;

import com.zkth.mst.client.R;
import com.zkth.mst.client.adapter.SipGroupAdapter;
import com.zkth.mst.client.base.BaseFragment;
import com.zkth.mst.client.callbacks.SipGroupResourcesCallback;
import com.zkth.mst.client.entity.SipGroupBean;
import com.zkth.mst.client.linphone.SipService;
import com.zkth.mst.client.ui.activity.SingleCallActivity;
import com.zkth.mst.client.ui.activity.SipInforActivity;
import com.zkth.mst.client.utils.NetworkUtils;
import com.zkth.mst.client.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Root on 2018/8/6.
 */

public class IntercomFragment extends BaseFragment {


    //sipgroup列表布局
    @BindView(R.id.sip_group_recyclearview)
    public RecyclerView recyclearview;

    List<SipGroupBean> mList = new ArrayList<>();

    String callNumber = "7002";

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

        getSipGroupResources();
    }

    private void getSipGroupResources() {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showLoadingView();
            }
        });
        if (mList != null && mList.size() > 0) {
            mList.clear();
        }
        if (NetworkUtils.isConnected()) {

            SipGroupResourcesCallback sipGroupResourcesCallback = new SipGroupResourcesCallback(new SipGroupResourcesCallback.SipGroupDataCallback() {
                @Override
                public void callbackSuccessData(final List<SipGroupBean> dataList) {
                    if (dataList != null && dataList.size() > 0) {
                        mList = dataList;
                        int s = dataList.size();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showContentView();
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
        showLoadingView();
        getSipGroupResources();
    }


    @OnClick({R.id.sip_group_lastpage_layout, R.id.sip_group_nextpage_layout, R.id.video_calls_duty_room_intercom_layout, R.id.voice_calls_duty_room_intercom_layout})
    public void onclickEvent(View view) {
        switch (view.getId()) {
            case R.id.sip_group_lastpage_layout:
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showShort("无数据");
                    }
                });
                break;
            case R.id.sip_group_nextpage_layout:
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showShort("无数据");
                    }
                });
                break;
            case R.id.loading_more_videosources_layout:
                getSipGroupResources();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showShort("已刷新");
                    }
                });
                break;
            case R.id.video_calls_duty_room_intercom_layout:
                call(1);
                break;
            case R.id.voice_calls_duty_room_intercom_layout:
                call(0);
                break;
        }
    }


    //打电话
    public void call(int type) {
        if (TextUtils.isEmpty(callNumber)) {
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
        intent.putExtra("userName", callNumber);
        intent.putExtra("isCall", true);
        if (type == 0) {
            intent.putExtra("isVideo", true);
        }
        startActivity(intent);
    }
}

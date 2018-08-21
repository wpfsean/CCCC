package com.zkth.mst.client.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.zkth.mst.client.R;
import com.zkth.mst.client.adapter.ChatListAdapter;
import com.zkth.mst.client.base.BaseFragment;
import com.zkth.mst.client.entity.SipClient;
import com.zkth.mst.client.linphone.MessageCallback;
import com.zkth.mst.client.linphone.SipService;
import com.zkth.mst.client.ui.activity.ChatActivity;
import com.zkth.mst.client.utils.NetworkUtils;
import com.zkth.mst.client.utils.SipHttpUtils;
import com.zkth.mst.client.views.SpaceItemDecoration;
import com.zkth.mst.client.views.WrapContentLinearLayoutManager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.linphone.core.LinphoneChatMessage;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;


/**
 * @author Wpf
 * @version v1.0
 * @date：2016-5-4 下午5:14:58
 */
public class ChatListFragment extends BaseFragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    //显示联系人列表的recyclearview
    @BindView(R.id.chat_contact_list_layout)
    RecyclerView chatList;
    //显示下拉刷新的SwipeRefreshLayout
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout sw;
    //数据适配器
    ChatListAdapter ada = null;

    //list展示数据
    List<SipClient> mList = new ArrayList<>();
    //list展示数据
    List<SipClient> newList = new ArrayList<>();

    //是否正在运行
    boolean threadIsRun = true;

    @Override
    protected int getLayoutId() {
        return R.layout.chat_fragment;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sw.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        sw.setOnRefreshListener(this);
        //设置recyclerview的布局及item间隔
        chatList.setLayoutManager(new WrapContentLinearLayoutManager(getActivity(), WrapContentLinearLayoutManager.VERTICAL, false));
        chatList.addItemDecoration(new SpaceItemDecoration(0, 30));
        chatList.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        getData();
    }

    @Override
    public void onClick(View v) {

    }

    private void getData() {

        if (NetworkUtils.isConnected()) {
            //获取sip数据并展示
            SipHttpUtils sipHttpUtils = new SipHttpUtils("http://19.0.0.60:8080/openapi/localuser/list?{%22syskey%22:%22123456%22}", new SipHttpUtils.GetHttpData() {
                @Override
                public void httpData(final String result) {
                    //判断是否正常的获取到数据
                    if (!TextUtils.isEmpty(result) && !result.contains("Execption")) {
                        //清除定时循环前的数据集合
                        if (mList != null || mList.size() > 0) {
                            mList.clear();
                        }
                        //解析json转成数据对象并添加到数据集合中
                        try {
                            JSONArray jsonArray = new JSONArray(result);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String username = jsonObject.getString("usrname");
                                String description = jsonObject.getString("description");
                                String dispname = jsonObject.getString("dispname");
                                String addr = jsonObject.getString("addr");
                                String state = jsonObject.getString("state");
                                String userAgent = jsonObject.getString("userAgent");
                                SipClient sipClient = new SipClient(username, description, dispname, addr, state, userAgent);
                                mList.add(sipClient);
                            }
                            //子线程无法更改主Ui
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dismissProgressDialog();
                                    ada = new ChatListAdapter(getActivity(), mList);
                                    chatList.setAdapter(ada);
                                    ada.setOnItemClickListener(new ChatListAdapter.OnItemClickListener() {
                                        @Override
                                        public void onClick(SipClient sipClient) {
                                            if (sipClient != null) {
                                                Intent intent = new Intent();
                                                intent.setClass(getActivity(), ChatActivity.class);
                                                Bundle bundle = new Bundle();
                                                bundle.putSerializable("sipclient", sipClient);
                                                intent.putExtras(bundle);
                                                getActivity().startActivity(intent);
                                            } else {
                                            }
                                        }
                                    });
                                }
                            });

                        } catch (Exception e) {
                        }
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showErrorView();
                            }
                        });
                    }
                }
            });
            sipHttpUtils.start();

        } else {
            showNoNetworkView();
        }
    }

    @Override
    public void onNetworkViewRefresh() {
        super.onNetworkViewRefresh();
        showProgressDialogWithText("正在努力加载中...");
        getData();
    }

    /**
     * 下拉刷新
     */
    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getData();
                sw.setRefreshing(false);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "No data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, 2 * 1000);
    }

    @Override
    public void onResume() {
        super.onResume();

        //回调,消息时时的刷新
        SipService.addMessageCallback(new MessageCallback() {
            @Override
            public void receiverMessage(LinphoneChatMessage linphoneChatMessage) {
                String from = linphoneChatMessage.getFrom().getUserName();
                int p = -1;
                for (int i = 0; i < mList.size(); i++) {
                    if (mList.get(i).getUsrname().equals(from)) {
                        p = i;
                        break;
                    }
                }
                ada.notifyItemChanged(p);
            }
        });
    }
}

package com.zkth.mst.client.ui.activity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.zkth.mst.client.R;
import com.zkth.mst.client.adapter.ChatMsgViewAdapter;
import com.zkth.mst.client.base.AppConfig;
import com.zkth.mst.client.base.BaseActivity;
import com.zkth.mst.client.base.DbConfig;
import com.zkth.mst.client.db.DatabaseHelper;
import com.zkth.mst.client.db.Logutils;
import com.zkth.mst.client.entity.ChatMsgEntity;
import com.zkth.mst.client.entity.SipClient;
import com.zkth.mst.client.linphone.Linphone;
import com.zkth.mst.client.linphone.MessageCallback;
import com.zkth.mst.client.linphone.SipManager;
import com.zkth.mst.client.linphone.SipService;
import com.zkth.mst.client.utils.TimeUtils;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Root on 2018/7/23.
 */

public class ChatActivity extends BaseActivity implements View.OnClickListener {

    //发送消息的按钮
    @BindView(R.id.send_message_btn_layout)
    TextView mBtnSend;
    //消息
    @BindView(R.id.sendmessage_layout)
    EditText mEditTextContent;
    //展示历史消息的ListView
    @BindView(R.id.message_listview_layout)
    ListView mListView;
    //当前的聊天室对象
    LinphoneChatRoom room = null;

    @BindView(R.id.current_fragment_name)
    TextView current_fragment_name;

    //和谁正在聊天
    String who = "";
    //Linphone聊天对象的地址
    LinphoneAddress linphoneAddress;
    //历史消息适配器
    private ChatMsgViewAdapter mAdapter;
    //盛放消息的集合容器
    private List<ChatMsgEntity> mDataArrays = new ArrayList<ChatMsgEntity>();
    SQLiteDatabase db;
    String sipNum = "";

    @Override
    public void initView() {
        ButterKnife.bind(this);
        mBtnSend.setOnClickListener(this);
    }

    @Override
    public void initData() {
        DatabaseHelper databaseHelper = new DatabaseHelper(ChatActivity.this);
        db = databaseHelper.getWritableDatabase();

        mDataArrays.clear();
        //获取本机的sip号码
        sipNum = DbConfig.getInstance().getData(8);


        //获取当前对话列表点击 的用户名
        SipClient sipClient = (SipClient) getIntent().getExtras().getSerializable("sipclient");
        String name = sipClient.getUsrname();
        if (!TextUtils.isEmpty(name)) {
            who = name;
            current_fragment_name.setText(who);
            Logutils.i("who:" + who);
            String sipserver = DbConfig.getInstance().getData(9);
            if (!TextUtils.isEmpty(sipserver)) {
                try {
                    linphoneAddress = LinphoneCoreFactory.instance().createLinphoneAddress("sip:" + who + "@" + sipserver);
                } catch (LinphoneCoreException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Logutils.e("No Get Chat Object!!!");
            return;
        }
        getAllHistory();
        //初始化适配器
        mAdapter = new ChatMsgViewAdapter(this, mDataArrays);
        mListView.setAdapter(mAdapter);
        mListView.setSelection(mListView.getCount());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.chat_activity;
    }

    /**
     * 取出所有的聊天记录
     */
    private void getAllHistory() {
        //根据条件查询聊天记录
        Cursor cursor = db.query("chat", null, "fromuser =? or touser = ?", new String[]{who, who}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String time = cursor.getString(cursor.getColumnIndex("time"));
                String fromuser = cursor.getString(cursor.getColumnIndex("fromuser"));
                String message = cursor.getString(cursor.getColumnIndex("message"));
                String toUser = cursor.getString(cursor.getColumnIndex("touser"));
                Logutils.i(TimeUtils.longTime2Short(time) + "\t" + fromuser + "\t" + toUser + "\t" + message);
                if (toUser.equals(who)) {
                    ChatMsgEntity mEntity = new ChatMsgEntity();
                    mEntity.setDate(TimeUtils.longTime2Short(time));
                    mEntity.setName(fromuser);
                    mEntity.setMsgType(false);
                    mEntity.setText(message);
                    mDataArrays.add(mEntity);
                } else if (fromuser.equals(who)) {
                    ChatMsgEntity tEntity = new ChatMsgEntity();
                    tEntity.setDate(TimeUtils.longTime2Short(time));
                    tEntity.setName(fromuser);
                    tEntity.setMsgType(true);
                    tEntity.setText(message);
                    mDataArrays.add(tEntity);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();


//        if (SipService.isReady()) {
//            LinphoneChatRoom[] rooms = SipManager.getLc().getChatRooms();
//            for (int i=0;i<rooms.length;i++){
//                String roomUser = rooms[i].getPeerAddress().getUserName();
//                Logutils.i("roomUser:"+roomUser);
//            }
//        }


    }

    @Override
    protected void onRestart() {
        super.onRestart();
        initMessReceiverCall();
    }


    //消息回调
    private void initMessReceiverCall() {
        SipService.addMessageCallback(new MessageCallback() {
            @Override
            public void receiverMessage(LinphoneChatMessage linphoneChatMessage) {
                ChatMsgEntity chatMsgEntity = new ChatMsgEntity();
                chatMsgEntity.setName(linphoneChatMessage.getFrom().getUserName());
                chatMsgEntity.setDate(TimeUtils.longTime2Short(new Date().toString()));
                chatMsgEntity.setMsgType(true);
                chatMsgEntity.setText(linphoneChatMessage.getText());
                mDataArrays.add(chatMsgEntity);
                mAdapter.notifyDataSetChanged();
                mEditTextContent.setText("");
                mListView.setSelection(mListView.getCount() - 1);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        initMessReceiverCall();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_message_btn_layout:
                sendMess();
                break;
        }
    }


    /**
     * 发消息
     */
    private void sendMess() {
        String chatMessage = mEditTextContent.getText().toString().trim();
        if (!TextUtils.isEmpty(chatMessage) && chatMessage.length() > 0) {
            //送消息的展示界面
            ChatMsgEntity entity = new ChatMsgEntity();
            entity.setText(chatMessage);
            entity.setMsgType(false);
            entity.setName("7007");
            entity.setDate(getDate());
            mDataArrays.add(entity);
            mAdapter.notifyDataSetChanged();
            mEditTextContent.setText("");
            mListView.setSelection(mListView.getCount() - 1);
            //（发送sip短消息到对方）
            if (SipService.isReady())
                Linphone.getLC().getChatRoom(linphoneAddress).sendMessage(chatMessage);

//            //把发的消息插入到数据库
            ContentValues contentValues = new ContentValues();
            contentValues.put("time", new Date().toString());
            contentValues.put("fromuser", sipNum);
            contentValues.put("message", chatMessage);
            contentValues.put("touser", who);
            db.insert("chat", null, contentValues);
        }
    }

    //时间
    private String getDate() {
        String time = new Date().toString();
        return TimeUtils.longTime2Short(time);
    }

}

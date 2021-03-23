package com.example.qingzhou.discussion;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qingzhou.Constant;
import com.example.qingzhou.R;
import com.example.qingzhou.activity.TabHostActivity;
import com.example.qingzhou.adapter.MessageAdapter;
import com.example.qingzhou.bean.DiscussionInfo;
import com.example.qingzhou.database.DiscussionRoomHelper;
import com.example.qingzhou.util.HttpUtil;
import com.example.qingzhou.util.MediaUtil;
import com.example.qingzhou.widget.RecordCreateDialog;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMChatRoomChangeListener;
import com.hyphenate.EMConferenceListener;
import com.hyphenate.EMMessageListener;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConferenceManager;
import com.hyphenate.chat.EMConferenceMember;
import com.hyphenate.chat.EMConferenceStream;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMStreamParam;
import com.hyphenate.chat.EMStreamStatistics;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.EMLog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GroupDiscussionActivity extends AppCompatActivity
        implements AudioRecorder.OnRecordFinishListener,View.OnClickListener{

    private static final int MESSAGE_VIEW_SEND_REFRESH=0;    //发送消息导致消息视图刷新
    private static final int MESSAGE_VIEW_RECV_REFRESH=1;    //收到消息导致消息视图刷新
    private static final int ROOM_INFO_REFRESH=2;   //消息类型：房间布局视图刷新（成员加入，退出）
    private static final int MIKE_BTN_REFRESH=3;    //开麦按钮换图
    private static final String TAG = "GroupDiscussionActivity";

    private RecordCreateDialog recordCreateDialog;
    private EMChatRoom emChatRoom;
    private Button btn_audioRecorder;
    private AudioRecorder audioRecorder;
    private String mRecordFilePath;
    private String roomId;      //记录从RoomAdapter返回的房间号
    private String roomTopic;
    private List<EMMessage> messages;   //存储从服务器获取的语音消息集合（线程安全）
    private boolean onclickSign = false;    //发言还没摁下去
    private boolean speak_bool = true;      //是否可以发言

    private DiscussionInfo currentDidscssionInfo;       //当前讨论房的类对象
    //messageCount用于计算第一阶段讨论时每个环节发送以及接收的消息总数，
    //方便启用或禁用当前用户的录音与发送按钮
    private int memberCount;
    private TextView room_topic;
    private TextView user1name;
    private TextView user2name;
    private TextView user3name;
    private TextView user4name;
    private List<String> memberList;

    private RecyclerView msgView;
    private MessageAdapter msgAdapter;

    private IntentFilter intentFilter;
    private LocalReceiver localReceiver;
    private LocalBroadcastManager localBroadcastManager;

    private discussProcess discussProcess;

    // Map<streamId, username>  会议里成员音频流
    private Map<String, String> streamMap = new HashMap<>();
    //会议管理器
    private EMConferenceManager conferenceManager;
    //当前用户名
    private String currentUsername;
    //推流ID,自己开启流的时候会有一个ID，这个用来记录
    private String publishId = null;
    //推送流参数设置
    private EMStreamParam normalParam;

    /**
     * 会议监听器
     */
    private EMConferenceListener conferenceListener = new EMConferenceListener() {
        //有人加入则重新发布一下自己的流
        @Override
        public void onMemberJoined(EMConferenceMember emConferenceMember) {
            unpublish(publishId);
            publish(false);
            Log.i("dopamine", "onMemberJoined: " + emConferenceMember.toString());
        }

        @Override
        public void onMemberExited(EMConferenceMember emConferenceMember) {
            Log.i("dopamine", "onMemberExited: 剩余人数"+currentDidscssionInfo.getConference().getMemberNum());
        }

        /**
         * 有音频流加入时调用
         * @param stream
         */
        @Override
        public void onStreamAdded(EMConferenceStream stream) {
            streamMap.put(stream.getStreamId(), stream.getUsername());      //把流加入到一个Map集合中
            Log.i("dopamine", "onStreamAdded: " + streamMap.toString());
            subscribe(stream);          //调用订阅接口
            //修改视图
            //。。。。。。。
        }

        /**
         * 有音频流退出时调用
         * @param stream
         */
        @Override
        public void onStreamRemoved(EMConferenceStream stream) {
            Log.i("dopamine", "onStreamRemoved: " + stream.getUsername());
            streamMap.remove(stream.getStreamId());
            //修改视图
//            final int existPosition = findExistPosition(stream.getUsername());
//            if (existPosition != -1) {
//                resetTalkerViewByPosition(existPosition);
//            }
        }

        /**
         * 流状态更新时调用
         * @param stream
         */
        @Override
        public void onStreamUpdate(EMConferenceStream stream) {
            Log.i("dopamine", "onStreamUpdate: ");
//            final int existPosition = findExistPosition(stream.getUsername());
        }

        /**
         * 会议销毁或者被踢出音视频会议
         * @param i
         * @param s
         */
        @Override
        public void onPassiveLeave(int i, String s) {
            Log.i("dopamine", "onPassiveLeave: " + i + " - " + s);

        }

        @Override
        public void onConferenceState(ConferenceState conferenceState) {

        }

        @Override
        public void onStreamStatistics(EMStreamStatistics emStreamStatistics) {

        }

        @Override
        public void onStreamSetup(String s) {

        }

        @Override
        public void onSpeakers(List<String> list) {

        }

        @Override
        public void onReceiveInvite(String s, String s1, String s2) {

        }

        @Override
        public void onRoleChanged(EMConferenceManager.EMConferenceRole emConferenceRole) {

        }
    };

    /**
     * 订阅指定成员的流stream，对于是音频流还是视频流在其他主播发布流的时候就已经设置好了
     */
    private void subscribe(EMConferenceStream stream){
        conferenceManager.subscribe(stream, null, new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                Log.i("dopamine", "Subscribe stream success");
            }

            @Override
            public void onError(int error, String errorMsg) {
                Log.e("dopamine", "Subscribe stream failed: " + error + " - " + errorMsg);
            }
        });
    }

    /**
     * 推送自己的音频流（推送视频流还是音频流看参数normalParam里的设置）
     * @param pauseVoice 是否开启视频推送
     */
    private void publish(boolean pauseVoice) {
        normalParam.setAudioOff(pauseVoice);
        conferenceManager.publish(normalParam, new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                Log.d("dopamine", "onSuccess: "+"推流成功");
                publishId = value;
                streamMap.put(publishId, currentUsername);
            }

            @Override
            public void onError(int error, String errorMsg) {
                EMLog.e("dopamine", "publish failed: error=" + error + ", msg=" + errorMsg);
            }
        });
    }

    /**
     * 停止推自己的数据
     */
    private void unpublish(final String publishId) {
        //使用工具类判断ID是否为空
        if (TextUtils.isEmpty(publishId)) {
            return;
        }

        conferenceManager.unpublish(publishId, new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                Log.i("dopamine", "unpublish success.");
            }

            @Override
            public void onError(int error, String errorMsg) {
                EMLog.e("dopamine", "unpublish failed: error=" + error + ", msg=" + errorMsg);
            }
        });
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.discuss);   //讨论界面
        audioRecorder=new AudioRecorder();
        audioRecorder.setOnRecordFinishListener(this);

        //初始化讨论吧场景的头部工具栏与讨论视图主体
        Intent intent=getIntent();
        initParam(intent);
        initToolBar();
        initDiscussScene(intent);
        initThread();

        //注册广播监听器
        localBroadcastManager=LocalBroadcastManager.getInstance(this);
        intentFilter=new IntentFilter();    //实例化广播过滤器
        intentFilter.addAction("com.example.junior.SEND_VOICE_MESSAGE");
        intentFilter.addAction("com.example.junior.CANCEL_VOICE_MESSAGE");
        localReceiver=new LocalReceiver();
        localBroadcastManager.registerReceiver(localReceiver,intentFilter);

        //启动当前讨论的接受信息后台服务
    //    Intent startMsgService=new Intent(this,MessageService.class);
    //    startService(startMsgService);

        //启动获取当前房间用户的子线程


        Log.i("dopamine", "剩余人数"+currentDidscssionInfo.getConference().getMemberNum());

    }

    @Override
    protected void onResume() {
        super.onResume();
        //注册消息监听来接收消息
        EMClient.getInstance().chatManager().addMessageListener(msgListener);
        //注册成员监听来追踪成员变化情况
        EMClient.getInstance().chatroomManager().addChatRoomChangeListener(roomChangeListener);
        initMessageView();      //重新加载当前的消息RecyclerView




    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //退出会议
        //退出房间，退出会议

        EMClient.getInstance().chatroomManager().leaveChatRoom(roomId);
        EMClient.getInstance().conferenceManager().exitConference(new EMValueCallBack() {
            @Override
            public void onSuccess(Object o) {
                Log.d("dopamine", "onSuccess: +讨论界面退出+"+"退出会议成功");
                Log.i("dopamine", "剩余人数"+currentDidscssionInfo.getConference().getMemberNum());
                if(currentDidscssionInfo.getConference().getMemberNum() == 1){          //这是最后一个人退出,删除数据库的记录
                    Log.d(TAG, "触发删除房间");
                    DiscussionRoomHelper.deleteByRoomID(roomId);
                }
            }

            @Override
            public void onError(int i, String s) {
                Log.d("dopamine", "onSuccess: "+"退出会议失败"+i+s);
            }
        });
        //记得在不需要的时候移除listener，如在activity的onDestroy()时
        EMClient.getInstance().chatManager().removeMessageListener(msgListener);    //移除消息的监听器
        EMClient.getInstance().chatroomManager().removeChatRoomListener(roomChangeListener);  //移除房间监听器
        localBroadcastManager.unregisterReceiver(localReceiver);
        mHandler.removeCallbacks(sendOut);
        //取消当前讨论的接受信息后台服务
    //    Intent stopMsgService=new Intent(this,MessageService.class);
    //    startService(stopMsgService);
    }

    /**
     * 初始化线程
     */
    private void initThread(){
        discussProcess = new discussProcess(roomId);                //讨论流程开始
        discussProcess.start();
        //实时检测是否可开启麦克风
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    speak_bool = discussProcess.getSpeak(EMClient.getInstance().getCurrentUser());      //根据流程类开放权限
                    if(speak_bool == false){                    //如果检测到是不能开麦的
                        closeMike();                                    //关麦
//                        Toast.makeText(GroupDiscussionActivity.this,"时间到了,已关闭麦克风",Toast.LENGTH_SHORT).show();
//                        HttpUtil.sendTxtmsg(EMClient.getInstance().getCurrentUser()+"时间到了,已关闭麦克风",EMClient.getInstance().getCurrentUser(),roomId,Constant.MSG_TYPE,Constant.DISCUSS_MSG);
                    }
                    //实时关闭语音开口
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    /**
     * 初始化参数用
     */
    private void initParam(Intent intent){
        //根据RoomAdapter中发来的动态广播包含的Intent中的房间号，来获取当前加入的聊天室详情
        roomId=intent.getStringExtra("chatroom_id");
        //得到当前房间类的信息
        currentDidscssionInfo = DiscussionRoomHelper.queryByRoomID(roomId);
        //会议管理类
        conferenceManager = EMClient.getInstance().conferenceManager();
//        conferenceManager.closeVoiceTransfer();          //闭麦
        conferenceManager.openVoiceTransfer();      //默认闭麦
        EMClient.getInstance().conferenceManager().addConferenceListener(conferenceListener);
        //设置Publish流的参数
        normalParam = new EMStreamParam();
        normalParam.setStreamType(EMConferenceStream.StreamType.NORMAL);
        //设置只发送音频流
        normalParam.setVideoOff(true);
        //设置只发布视频流
        normalParam.setAudioOff(false);
        //进入初始化的时候顺便推流，这里false意思是不开启视频推送
        publish(false);
    }

    //初始化页面工具栏
    private void initToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);   //获取当前Activity的工具栏
        toolbar.setTitle("讨论吧");
        setSupportActionBar(toolbar);                   //加载Toolbar控件
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {   //按返回键后退出到TabHostActivity
                Intent intent=new Intent(GroupDiscussionActivity.this, TabHostActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    //初始化讨论场景的视图与控件
    public void initDiscussScene(Intent intent){
        Log.d(TAG,"当前加入的房间ID为："+roomId);
        //获取所有的用户名TextView
        user1name=findViewById(R.id.user1_name);
        user2name=findViewById(R.id.user2_name);
        user3name=findViewById(R.id.user3_name);
        user4name=findViewById(R.id.user4_name);
        room_topic=findViewById(R.id.room_topic);
        memberList=new ArrayList<>();
        //刚进入讨论界面时在后台开启子线程获取成员列表与讨论
        memberList = HttpUtil.GetUserName(roomId);
        roomTopic = HttpUtil.GetRoomName(roomId);
        mHandler.sendEmptyMessage(ROOM_INFO_REFRESH);
        //为录音按钮注册监听器
        btn_audioRecorder=findViewById(R.id.btn_audio);
        btn_audioRecorder.setOnLongClickListener(new MyLongClickListener());
        btn_audioRecorder.setOnClickListener(this);
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_discuss_activity,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){    //Toolbar上的菜单按钮被点击时的事件
        if (item.getItemId()==R.id.menu_help){    //点击了“查看帮助”按钮

        }
        return true;
    }

    //加载进入房间时的消息视图（有可能是第一次进入，也有可能是将APP切换到后台再进入）
    private void initMessageView() {
        EMClient.getInstance().groupManager().loadAllGroups();
        EMClient.getInstance().chatManager().loadAllConversations();          //将聊天室的历史记录全部加载进来了
        messages=new ArrayList<>();
        //根据当前房间ID获取会话
        EMConversation conversation = EMClient.getInstance().chatManager()
                .getConversation(roomId, EMConversation.EMConversationType.ChatRoom,true);
        //获取此会话的所有消息
        Log.d(TAG, "当前会话的ID: "+conversation.conversationId());
//        messages = conversation.getAllMessages();
        msgView=findViewById(R.id.rv_msgview);  //获取到消息RecyclerView
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        msgView.setLayoutManager(layoutManager);
        msgAdapter=new MessageAdapter(messages);    //以之前获取到的会话消息初始化适配器
        msgView.setAdapter(msgAdapter);
    }

    // 音频录制一旦完成，就触发监听器的onRecordFinish方法
    @Override
    public void onRecordFinish() {
        Log.d(TAG, "onRecordFinish: 当前录音已完成");    //提示用户已经完成当前录音
    }



    //如果用户长按录音键，则弹出确认是否正式开始录音窗口
    private class MyLongClickListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            if (v.getId()==R.id.btn_audio){
                //动态获取录音权限
                if(ContextCompat.checkSelfPermission(GroupDiscussionActivity.this, Manifest.
                        permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(GroupDiscussionActivity.this,new
                            String[]{Manifest.permission.RECORD_AUDIO},1);
                }
                else {  //APP当前已获取录音权限
                    //弹窗，询问用户是否开始录音
                    AlertDialog.Builder builder=new AlertDialog.Builder(v.getContext());
                    builder.setTitle("确定要开始录音吗");
                    builder.setMessage("点击“确定”开始，“取消”离开");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //若用户已经同意APP获取录音权限，且想开始录音
                            //则弹出记录录音时长窗口，并在后台开启录音线程
                            startRecord();
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //用户不希望现在开始录音
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog=builder.create();
                    dialog.show();
                }
            }
            return false;
        }
    }

    private void startRecord() {
        mRecordFilePath=MediaUtil.getRecordFilePath(this
                ,"AudioRecord",".amr"); //先获取录音文件的保存路径
        recordCreateDialog=new RecordCreateDialog(GroupDiscussionActivity.this);
        recordCreateDialog.show();      //创建并显示计时面板
        new Thread(new Runnable() {
            @Override
            public void run() {     //在子线程中开始录音
                audioRecorder.start(mRecordFilePath);
            }
        }).start();
    }

    //对是否取得权限进行判断
    public void onRequestPermissionsResult(int requestCode,String[] permissions,
                                           int[] grantResults){
        switch (requestCode){
            case 1:
                if (grantResults.length>0&&grantResults[0]==PackageManager.
                        PERMISSION_GRANTED){    //用户之前已经同意了录音权限
                    //若用户已经同意APP获取录音权限，且想开始录音
                    //则弹出记录录音时长窗口，并在后台开启录音线程
                    recordCreateDialog=new RecordCreateDialog(GroupDiscussionActivity.this);
                    recordCreateDialog.show();
                }
                else {
                    Toast.makeText(this,"You denied record" +
                            " permission",Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what==MESSAGE_VIEW_SEND_REFRESH||msg.what==MESSAGE_VIEW_RECV_REFRESH){   //当接收到的消息标识为发送消息请求视图刷新
                Log.d(TAG, "接收到刷新消息视图的Message");
                //获取自己向服务器发送的消息
                EMMessage message= (EMMessage) msg.obj;
                //将新信息添加到原来的消息集合messages中（在原集合尾部添加）
                messages.add(message);
                //适配器新信息插入到适配器的位置（新信息也是在适配器尾部显示）
                msgAdapter.notifyItemInserted(messages.size());
                msgView.scrollToPosition(messages.size()-1);                //滚动到对应位置
                notifiCreate(message.getFrom());
            }
            else if (msg.what==ROOM_INFO_REFRESH){  //当接收到的消息标识为刷新房间视图时
                for(int i=0;i<memberList.size();i++){
                    if(i == 0){
                        user1name.setText(memberList.get(i));
                    }else if(i == 1){
                        user2name.setText(memberList.get(i));
                    }else if(i == 2){
                        user3name.setText(memberList.get(i));
                    }else if(i == 3){
                        user4name.setText(memberList.get(i));
                    }
                    room_topic.setText(roomTopic);
                    Log.d(TAG, "handleMessage: change");
                }
            }
            else if(msg.what==MIKE_BTN_REFRESH){
                if(onclickSign){                        //如果是摁下去的,则换图未松开
                    btn_audioRecorder.setBackgroundResource(R.drawable.microphone2);
                } else if(!onclickSign){
                    btn_audioRecorder.setBackgroundResource(R.drawable.microphone);
                }
            }
        }
    };
    private Runnable sendOut=new Runnable() {
        @Override
        public void run() {
            int length=audioRecorder.getRecordlength();
            //创建一个语音消息对象，其中第一个参数为语音文件路径，第二个参数为录音时间(秒)
            //注意第二个参数的取值，在AudioRecord类中获取到的为毫秒，需要先转换单位
            EMMessage message = EMMessage.createVoiceSendMessage(mRecordFilePath
                    ,length/1000,roomId);
            //这里为发送消息
            message.setAttribute(Constant.MSG_TYPE,Constant.DISCUSS_MSG);           //设置属性为讨论吧专属属性
            Log.d(TAG, "创建语音消息成功");
            //启动当前录音消息发送状态回调方法
            message.setMessageStatusCallback(new EMCallBack(){
                @Override
                public void onSuccess() {
                    Log.d(TAG,"消息发送成功");
                    Message message1=mHandler.obtainMessage();
                    message1.what=MESSAGE_VIEW_SEND_REFRESH;
                    message1.obj=message;   //消息携带着当前用户刚发送的信息
                    mHandler.sendMessage(message1);
                }

                @Override
                public void onError(int i, String s) {
                    Log.d(TAG,"消息发送失败："+i+";"+s);
                }

                @Override
                public void onProgress(int i, String s) {

                }
            });
            //设置消息发送的类型为群聊
            message.setChatType(EMMessage.ChatType.GroupChat);
            //将消息真正发送到当前群聊
            EMClient.getInstance().chatManager().sendMessage(message);
        }
    };

    //消息监听器实例
    EMMessageListener msgListener = new EMMessageListener() {

        @Override
        public void onMessageReceived(List<EMMessage> messages) {
            Log.d(TAG, "onMessageReceived: "+"收到消息");
            //向处理器Handler发送多条消息更新RecyclerView
            Message message=Message.obtain();   //获得一个默认的消息对象
            EMMessage emMessage = messages.get(messages.size()-1);      //得到最新的那一条消息
            try {
                if(emMessage.getIntAttribute(Constant.MSG_TYPE) == Constant.DISCUSS_MSG){               //如果收到的是讨论吧专属消息才会更新视图
                    Log.d(TAG, "onMessageReceived: "+"发送刷新通知");
                    message.what=MESSAGE_VIEW_RECV_REFRESH;
                    message.obj=emMessage;   //消息携带的数据为获取到的messages
                    mHandler.sendMessage(message);      //向主线程的Handler发送message
                } else if(emMessage.getIntAttribute(Constant.MSG_TYPE) == Constant.BACKSTAGE_STREAM){          //收到后台订阅流信息
                    EMTextMessageBody textMessageBody =(EMTextMessageBody)emMessage.getBody();        //强制转换为EMTextMessageBody
                    String subscribID = textMessageBody.getMessage();                                                   //得到内容

                }
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCmdMessageReceived(List<EMMessage> messages) {
            //收到透传消息
        }

        @Override
        public void onMessageRead(List<EMMessage> messages) {
            //收到已读回执
        }

        @Override
        public void onMessageDelivered(List<EMMessage> message) {
            //收到已送达回执
        }
        @Override
        public void onMessageRecalled(List<EMMessage> messages) {
            //消息被撤回
        }

        @Override
        public void onMessageChanged(EMMessage message, Object change) {
            //消息状态变动
        }
    };

    //房间监听器实例
    EMChatRoomChangeListener roomChangeListener=new EMChatRoomChangeListener() {
        @Override
        public void onChatRoomDestroyed(String s, String s1) {
            Log.d(TAG, "当前房间已被房主删除，退出到选择房间页面");
            //调用离开房间方法使当前用户退出房间
            EMClient.getInstance().chatroomManager().leaveChatRoom(roomId);
            //并返回到选择房间页面
            Intent intent=new Intent(GroupDiscussionActivity.this,TabHostActivity.class);
            startActivity(intent);
            finish();
        }

        @Override
        public void onMemberJoined(String s, String s1) {   //第2个参数s1为新加入用户的用户名
            Log.d(TAG, "当前房间有新用户加入");
            //通知Handler刷新房间UI（将用户名作为消息的传递对象）
            Message message=mHandler.obtainMessage();
            message.what=ROOM_INFO_REFRESH;
            message.obj=s1;
            mHandler.sendMessage(message);
        }

        @Override
        public void onMemberExited(String s, String s1, String s2) {    //第3个参数为退出成员的用户名
            Log.d(TAG, "当前房间有用户退出");
            //通知Handler刷新房间UI（将用户名作为消息的传递对象）
            Message message=mHandler.obtainMessage();
            message.what=ROOM_INFO_REFRESH;
            message.obj=s2;
            mHandler.sendMessage(message);
        }

        @Override
        public void onRemovedFromChatRoom(int i, String s, String s1, String s2) {

        }

        @Override
        public void onMuteListAdded(String s, List<String> list, long l) {

        }

        @Override
        public void onMuteListRemoved(String s, List<String> list) {

        }

        @Override
        public void onWhiteListAdded(String s, List<String> list) {

        }

        @Override
        public void onWhiteListRemoved(String s, List<String> list) {

        }

        @Override
        public void onAllMemberMuteStateChanged(String s, boolean b) {

        }

        @Override
        public void onAdminAdded(String s, String s1) {

        }

        @Override
        public void onAdminRemoved(String s, String s1) {

        }

        @Override
        public void onOwnerChanged(String s, String s1, String s2) {

        }

        @Override
        public void onAnnouncementChanged(String s, String s1) {

        }
    };

    /**
     *  本地广播监听器，监听来自RecordCreateDialog的广播
     */
    private class LocalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //接收来自RecordCreateDialog的确认发送录音信号
            String action=intent.getAction();
            if (action.equals("com.example.junior.SEND_VOICE_MESSAGE")){
                audioRecorder.stop();
                //启动录音发送任务
                mHandler.post(sendOut);
            }
            //接收来自RecordCreateDialog的取消发送录音信号
            else if (action.equals("com.example.junior.CANCEL_VOICE_MESSAGE")){
                audioRecorder.stop();
                File file=new File(mRecordFilePath);        //用户在没有第二次长按录音按钮前，上一次的录音文件路径不会改变
                if(file.delete()){
                    Toast.makeText(context,"录音文件删除成功",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(context,"录音文件删除失败",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * 每次收到消息时，都会在通知栏创建一个通知
     */
    private void notifiCreate(String string){
        Intent intent=new Intent(this,GroupDiscussionActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,0);
        NotificationManager manager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel=new NotificationChannel("1","message"
                    ,NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(notificationChannel);
        }
        Notification notification= new NotificationCompat.Builder(this,"1")
                .setContentTitle("讨论吧消息")       //设置通知标题
                .setContentText(string+"发出了一条消息")             //设置通知内容
                .setContentIntent(pendingIntent)        //给通知加上点击功能，点击跳转到讨论界面
                .setWhen(System.currentTimeMillis())    //在通知右侧显示时间
                .setSmallIcon(R.mipmap.ic_launcher)     //设置通知图标
                .setDefaults(NotificationCompat.DEFAULT_ALL)    //设置通知来时手机的震动和提示灯
                .setAutoCancel(true)        //设置通知被点击后自动消失
                .build();       //设置好各项参数后调用build建造当前通知
        manager.notify(1,notification);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_audio:
                if(!speak_bool){
                    Toast.makeText(GroupDiscussionActivity.this,"当前流程您不可开麦~",Toast.LENGTH_SHORT).show();
                    break;
                }
                if(onclickSign){            //摁下去了
                    closeMike();
                } else if(!onclickSign){                     //还没摁下去
                    openMike();
                }
                break;
        }
    }


    /**
     * 闭麦操作
     */
    private void closeMike(){
        Log.d("dopamine", "onClick: 闭麦");
        //换图
//                    conferenceManager.closeVoiceTransfer();          //闭麦
        unpublish(publishId);
        onclickSign = false;         //更改状态,松开
        //换图
        Message message=mHandler.obtainMessage();
        message.what=MIKE_BTN_REFRESH;
        mHandler.sendMessage(message);
    }

    /**
     * 开麦操作
     */
    private void openMike(){
        //换图
        Log.d("dopamine", "onClick: 开麦");
//                    conferenceManager.openVoiceTransfer();          //开麦
        publish(false);
        onclickSign = true;         //更改状态，摁下去
        //换图
        Message message=mHandler.obtainMessage();
        message.what=MIKE_BTN_REFRESH;
        mHandler.sendMessage(message);
    }

    /**
     * 该活动的统一入口
     */
    public static void actionStart(Context context, String ID){
        Intent intent = new Intent(context,GroupDiscussionActivity.class);
        intent.putExtra("chatroom_id",ID);
        context.startActivity(intent);
    }

}

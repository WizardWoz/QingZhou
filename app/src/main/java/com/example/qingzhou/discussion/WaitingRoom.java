package com.example.qingzhou.discussion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.qingzhou.Base.BaseActivity;
import com.example.qingzhou.Constant;
import com.example.qingzhou.R;
import com.example.qingzhou.adapter.MemberAdapter;
import com.example.qingzhou.bean.DiscussionInfo;
import com.example.qingzhou.bean.UserInfo;
import com.example.qingzhou.database.DiscussionRoomHelper;
import com.example.qingzhou.database.UserDBHelper2;
import com.example.qingzhou.util.HttpUtil;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMChatRoomChangeListener;
import com.hyphenate.EMMessageListener;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConference;
import com.hyphenate.chat.EMConferenceManager;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessageBody;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.exceptions.HyphenateException;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class WaitingRoom extends BaseActivity implements View.OnClickListener{

    private static final int MESSAGE_VIEW_SEND_REFRESH=0;    //发送消息导致消息视图刷新
    private static final int MESSAGE_VIEW_RECV_REFRESH=1;    //收到消息导致消息视图刷新
    private static final int MEMBER_INFO_REFRESH_JOIN=2;   //消息类型：房间布局视图刷新（成员加入，退出）
    private static final int MEMBER_INFO_REFRESH_EXIT=3;   //消息类型：房间布局视图刷新（成员加入，退出）
    private static final int TEXTVIEW_REFRESH_JOIN=4;        //聊天框刷新加入
    private static final int TEXTVIEW_REFRESH_SEND=5;        //聊天框刷新发送

    private static final String TAG = "WaitingRoom";

    private UserDBHelper2 userDBHelper2 = new UserDBHelper2();          //创建一个数据库管理器
    private String roomID;                          //房间ID
    private String ownerID;                         //房主ID
    private String roomTopic;                       //房间主题
    private List<UserInfo> memberInfoList = new ArrayList<>();            //list
    private MemberAdapter adapter;                  //滚动视图的adapter
    private RecyclerView userrecyclerView;                           //滚动视图
    private TextView waitingRoomTextView;                           //聊天框
    private EditText sendEditText;                           //文字框
    private Button sendBtn;                                     //发送按钮
    private TextView membernum;                             //人数信息
    private Button createBtn;                                //创建按钮
    private Button joinBtn;                                //加入按钮


    private SharedPreferences mShared;          //声明一个共享参数对象  记录用户手机号
    private String phone;                       //當前用戶手機號
    private UserInfo currentuser;               //当前用户类

    /**
     * 聊天室信息监听器，用来接收文字信息
     */
    private EMMessageListener messageListener = new EMMessageListener() {
        @Override
        public void onMessageReceived(List<EMMessage> list) {
            Log.d("dopamine", "onMessageReceived: "+"收到消息");
            Message message=Message.obtain();   //
            message.what=TEXTVIEW_REFRESH_JOIN;              //设置类型
            EMMessage emMessage = list.get(list.size()-1);     //得到最新的那一条消息

            try {
                if(emMessage.getIntAttribute(Constant.MSG_TYPE) == Constant.WAIT_MSG){          //如果是非讨论吧信息才显示在等候室
                    message.obj=emMessage;
                    mHandler.sendMessage(message);      //发送message
                }

            } catch (HyphenateException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onCmdMessageReceived(List<EMMessage> list) {
            //收到透传信息
        }

        @Override
        public void onMessageRead(List<EMMessage> list) {
            //收到已读回执
        }

        @Override
        public void onMessageDelivered(List<EMMessage> list) {
            //收到已送达回执
        }

        @Override
        public void onMessageRecalled(List<EMMessage> list) {
            //消息被撤回
        }

        @Override
        public void onMessageChanged(EMMessage emMessage, Object o) {
            //消息状态变动
        }
    };

    /**
     * 聊天室状态监听器，用来做对应变动
     */
    private EMChatRoomChangeListener emChatRoomChangeListener = new EMChatRoomChangeListener() {
        @Override
        public void onChatRoomDestroyed(String s, String s1) {

        }

        /**
         * 有成员加入时，改变对应视图，并在聊天框加入提示信息
         * 每次有人加入，普通成员调用一次，房主调用两次
         * eg:普通 有人加入+ID
         *    房主 有人加入+ID
         *        有人加入+系统管理员
         * 可以借此做一些调用
         * @param s
         * @param s1
         */
        @Override
        public void onMemberJoined(String s, String s1) {
            if(s1.matches("[0-9]+")){
//                Log.d("dopamine", "有人加入" + s1);
//                Log.d("dopamine", "onMemberJoined: "+s+s1+"join");
                UserInfo userInfo = userDBHelper2.queryByPhone(s1);     //得到加入用户的用户类

                //如果加入的人是自己，则发送系统提示信息
//                if(s1.equals(EMClient.getInstance().getCurrentUser())){
//
//                    HttpUtil.sendTxtmsg("[系统提示]:"+userInfo.getUserName() + "加入房间",s1,roomID,Constant.MSG_TYPE,Constant.WAIT_MSG);
//                }



                mHandler.sendEmptyMessage(MEMBER_INFO_REFRESH_JOIN);         //更改视图
            }
        }

        @Override
        public void onMemberExited(String s, String s1, String s2) {
//            Log.d("dopamine", "s="+s+",s1="+s1+",s2="+s2);
            if(s2.matches("[0-9]+")){
//                Log.d("dopamine", "有人离开" + s1);
//                Log.d("dopamine", "onMemberJoined: "+s+s1+"join");
                UserInfo userInfo = userDBHelper2.queryByPhone(s2);     //得到加入用户的用户类

                //如果离开的人是自己，则发送离开消息
                if(s2.equals(EMClient.getInstance().getCurrentUser())){
                    HttpUtil.sendTxtmsg("[系统提示]:"+userInfo.getUserName() + "离开房间",s2,roomID,Constant.MSG_TYPE,Constant.WAIT_MSG);       //发送信息
                }


                Message message=Message.obtain();   //
                message.what=MEMBER_INFO_REFRESH_EXIT;              //设置类型
                int remove_position = -1;                                //找出要删除的位置
                for(int i=0;i<memberInfoList.size();i++){                   //遍历，找出要删除的位置
                    if (memberInfoList.get(i).getPhone().equals(s2))
                        remove_position = i;
                }
                if(remove_position>=0){
                    message.arg1=remove_position;   //得到对应位置
                    mHandler.sendMessage(message);      //发送message,修改视图
                }

            }
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
     * 改变房间视图用的Handler
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler=new Handler(){
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                //收到刷新房间布局的通知
                case MEMBER_INFO_REFRESH_JOIN:
                    initList();         //重新刷新数据
                    adapter.notifyItemInserted(memberInfoList.size() - 1);          //刷新滚动视图
                    break;
                case MEMBER_INFO_REFRESH_EXIT:
                    initList();         //重新刷新数据
                    adapter.notifyItemRemoved(msg.arg1);
                    break;
                case TEXTVIEW_REFRESH_JOIN:
                    EMMessage message= (EMMessage) msg.obj;
                    EMTextMessageBody textMessageBody =(EMTextMessageBody)message.getBody();        //强制转换为EMTextMessageBody
                    String joinmsg = textMessageBody.getMessage();                                                   //得到内容
                    Log.d("dopamine", "handleMessage: "+"消息体内容:"+joinmsg);
                    waitingRoomTextView.setText(waitingRoomTextView.getText().toString()+"\n"+joinmsg);                              //加上一条新的信息
                    break;
                case TEXTVIEW_REFRESH_SEND:
                    String text = waitingRoomTextView.getText().toString();                     //获取未更新前textview的内容
                    waitingRoomTextView.setText(text+"\n"+sendEditText.getText().toString());   //加上一条新的信息
                    sendEditText.setText("");                                                   //输入框清空
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.waiting_room);
        initToolBar();              //顶部导航栏初始化
        initUI();                   //初始化UI界面
        initParam();                 //初始化一些数据，比如加入监听器等等
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EMClient.getInstance().chatManager().removeMessageListener(messageListener);
        EMClient.getInstance().chatroomManager().removeChatRoomListener(emChatRoomChangeListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //退出房间，退出会议
        EMClient.getInstance().chatroomManager().leaveChatRoom(roomID);
        EMClient.getInstance().conferenceManager().exitConference(new EMValueCallBack() {
            @Override
            public void onSuccess(Object o) {
                Log.d("dopamine", "onSuccess: "+"退出会议成功");
            }

            @Override
            public void onError(int i, String s) {
                Log.d("dopamine", "onSuccess: "+"退出会议失败"+i+s);
            }
        });
    }

    /**
     * 初始化数据
     */
    private void initParam(){
        EMClient.getInstance().chatroomManager().addChatRoomChangeListener(emChatRoomChangeListener);       //加入监听器
        EMClient.getInstance().chatManager().addMessageListener(messageListener);                           //加入信息监听器
        //打开共享参数编辑器
        mShared=getSharedPreferences("share_login",MODE_PRIVATE);
        //获得登录用户的手机号，它的设置相关代码可以在LoginActivity中找到
        phone=mShared.getString("user_phone","");
        currentuser = userDBHelper2.queryByPhone(phone);


        UserInfo userInfo = userDBHelper2.queryByPhone(EMClient.getInstance().getCurrentUser());     //得到加入用户的用户类
        HttpUtil.sendTxtmsg("[系统提示]:"+userInfo.getUserName() + "加入房间",EMClient.getInstance().getCurrentUser(),roomID,Constant.MSG_TYPE,Constant.WAIT_MSG);
    }

    /**
     * 初始化UI界面
     */
    private void initUI() {
        userrecyclerView = findViewById(R.id.menber_list);
        sendEditText = findViewById(R.id.sendtext);
        sendBtn = findViewById(R.id.sendbtn);
        createBtn = findViewById(R.id.create_conference);
        joinBtn = findViewById(R.id.join_conference);
        waitingRoomTextView = findViewById(R.id.waitRoomTextView);
        sendBtn.setOnClickListener(this);
        createBtn.setOnClickListener(this);
        joinBtn.setOnClickListener(this);
        //初始化recycleView
        initList();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        userrecyclerView.setLayoutManager(layoutManager);
        adapter = new MemberAdapter(memberInfoList);
        userrecyclerView.setAdapter(adapter);
        //初始化其他UI
    }

    /**
     * 防止代码过多，将RecycleList数据初始化分离出来
     */
    private void initList(){
        memberInfoList.clear();             //先清除一下List的内容
        List<String> membername = HttpUtil.GetUserName(roomID);     //向环信服务器发请求，获得聊天室内所有的用户手机号
        for (String s : membername) {
            memberInfoList.add(userDBHelper2.queryByPhone(s));        //根据手机号获取用户，并加入到集合中
        }
        Collections.reverse(memberInfoList);            //由于环信聊天室添加成员的特性，新添加的成员反而再最前面，因此需要倒叙一下才能放入滚动视图
    }

    /**
     * 初始化顶部导航栏
     */
    private void initToolBar(){
        Intent intent = getIntent();                        //获得启动该活动的intent
        roomID = intent.getStringExtra("ID");    //得到房间ID
        ownerID = EMClient.getInstance().chatroomManager().getChatRoom(roomID).getOwner();      //得到房主ID
//        Log.d("dopamine", "ID="+roomID);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    roomTopic = HttpUtil.GetRoomName(roomID); //得到房间名
//                } catch (IOException | JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
        Toolbar toolbar = findViewById(R.id.toolbar);       //获取当前Activity的工具栏
        toolbar.setTitle("等待");
        setSupportActionBar(toolbar);                       //加载Toolbar控件
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sendbtn:
                String sendtext = sendEditText.getText().toString();            //得到输入框内容

                HttpUtil.sendTxtmsg("["+currentuser.getUserName()+"]:"+sendtext,currentuser.getPhone(),roomID,Constant.MSG_TYPE,Constant.WAIT_MSG);

                sendEditText.setText("");   //清空输入框内容
                break;
            case R.id.create_conference:
                //如果是房主才可以点击，并且加入会议，发送会议密码到全部人
                //非房主则提示不是房主无法操作
                //判断是不是房主点击
                if(currentuser.getPhone().equals(EMClient.getInstance().chatroomManager().getChatRoom(roomID).getOwner())){
                    //如果是房主，则创建并且加入，并把密码发送到聊天室内
                    new AlertDialog.Builder(this)
                            .setTitle("提示")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setMessage("确定创建讨论吗")
                            .setPositiveButton("是的立刻马上", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AlertDialog alertDialog = new AlertDialog.Builder(v.getContext())
                                            .setTitle("提示")
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setMessage("请稍等")
                                            .setCancelable(false)
                                            .show();
                                    DiscussionInfo discussionInfo = DiscussionRoomHelper.queryByRoomID(roomID);         //如果已经有房间了
                                    if(discussionInfo != null){
                                        alertDialog.dismiss();
                                        new AlertDialog.Builder(v.getContext())
                                                .setTitle("错误")
                                                .setIcon(android.R.drawable.ic_dialog_alert)
                                                .setMessage("不能重复创建讨论1！")
                                                .setPositiveButton("好的", null)
                                                .show();
                                        return ;
                                    }
                                    //创建一个提示框，让稍等
                                    //创建随机4位数字密码
                                    StringBuilder conferencepwd = new StringBuilder();     //会议密码
                                    Random random = new Random();               //随机数类
                                    conferencepwd.append("1");
//                                    for(int i=0;i<4;i++){
//                                        conferencepwd.append(String.valueOf(random.nextInt(10)));     //每次添加一个0~9的数字
//                                    }
                                    Log.d("创建会议", "会议密码:"+conferencepwd.toString());
                                    //以房间ID为会议名称创建会议，并加入
                                    //会议会在没人的时候自动销毁
                                    EMClient.getInstance().conferenceManager().createAndJoinConference(EMConferenceManager.EMConferenceType.SmallCommunication,
                                            conferencepwd.toString(), new EMValueCallBack<EMConference>() {
                                                @Override
                                                public void onSuccess(EMConference emConference) {
                                                    alertDialog.dismiss();
                                                    //创建一次后就不能再创建了
                                                    DiscussionInfo discussionInfo = new DiscussionInfo(EMClient.getInstance().
                                                            chatroomManager().getChatRoom(roomID), emConference);
                                                    if (!DiscussionRoomHelper.insertRoomInfo(discussionInfo)){ //往服务器数据库插入信息
                                                        new AlertDialog.Builder(v.getContext())
                                                                .setTitle("错误")
                                                                .setIcon(android.R.drawable.ic_dialog_alert)
                                                                .setMessage("创建房间失败")
                                                                .setPositiveButton("好的", null)
                                                                .show();
                                                        return ;
                                                    }
                                                    Log.d("dopamine", "创建会议成功，密码:"+conferencepwd.toString()+",ID="+emConference.getConferenceId());
                                                    //发送密码信息
                                                    HttpUtil.sendTxtmsg("[系统提示]:讨论密码:"+emConference.getPassword() + " 请及时加入讨论!",currentuser.getPhone(),roomID,Constant.MSG_TYPE,Constant.WAIT_MSG);
                                                    GroupDiscussionActivity.actionStart(WaitingRoom.this,roomID);
//                                                    EMClient.getInstance().conferenceManager().exitConference(new EMValueCallBack() {
//                                                        @Override
//                                                        public void onSuccess(Object o) { }
//                                                        @Override
//                                                        public void onError(int i, String s) { }
//                                                    });
                                                }
                                                @Override
                                                public void onError(int i, String s) {
                                                    alertDialog.dismiss();
                                                    Log.d("dopamine", "创建会议失败,"+i+s);
                                                    new AlertDialog.Builder(v.getContext())
                                                            .setTitle("错误")
                                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                                            .setMessage("不能重复创建讨论2！")
                                                            .setPositiveButton("好的", null)
                                                            .show();
                                                }
                                            });

//                                    EMClient.getInstance().conferenceManager().joinRoom(roomID, conferencepwd.toString(),
//                                            EMConferenceManager.EMConferenceRole.Admin, new EMValueCallBack<EMConference>() {
//                                                @Override
//                                                public void onSuccess(EMConference emConference) {
//                                                    alertDialog.dismiss();
//                                                    //创建一次后就不能再创建了
//                                                    DiscussionInfo discussionInfo = new DiscussionInfo(EMClient.getInstance().
//                                                            chatroomManager().getChatRoom(roomID), emConference);
//                                                    if (!DiscussionRoomHelper.insertRoomInfo(discussionInfo)){ //往服务器数据库插入信息
//                                                        new AlertDialog.Builder(v.getContext())
//                                                                .setTitle("错误")
//                                                                .setIcon(android.R.drawable.ic_dialog_alert)
//                                                                .setMessage("不能重复创建讨论1！")
//                                                                .setPositiveButton("好的", null)
//                                                                .show();
//                                                        return ;
//                                                    }
//                                                    Log.d("dopamine", "创建会议成功，密码:"+conferencepwd.toString()+",ID="+emConference.getConferenceId());
//                                                    //发送密码信息
//                                                    HttpUtil.sendTxtmsg("[系统提示]:讨论密码:"+emConference.getPassword() + " 请及时加入讨论!",currentuser.getPhone(),roomID,Constant.MSG_TYPE,Constant.WAIT_MSG);
//                                                    GroupDiscussionActivity.actionStart(WaitingRoom.this,roomID);
//                                                    EMClient.getInstance().conferenceManager().exitConference(new EMValueCallBack() {
//                                                        @Override
//                                                        public void onSuccess(Object o) { }
//                                                        @Override
//                                                        public void onError(int i, String s) { }
//                                                    });
//                                                }
//                                                @Override
//                                                public void onError(int i, String s) {
//                                                    alertDialog.dismiss();
//                                                    Log.d("dopamine", "创建会议失败,"+i+s);
//                                                    new AlertDialog.Builder(v.getContext())
//                                                            .setTitle("错误")
//                                                            .setIcon(android.R.drawable.ic_dialog_alert)
//                                                            .setMessage("不能重复创建讨论2！")
//                                                            .setPositiveButton("好的", null)
//                                                            .show();
//                                                }
//                                            });
                                }
                            })
                            .setNegativeButton("算了再等等吧", null)
                            .show();
                } else{
                    // 如果非房主，则出现提示框
                    new AlertDialog.Builder(this)
                            .setTitle("没有权限！")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setMessage("你不是该房间的房主，无法创建讨论，请等待房主创建后再加入")
                            .setPositiveButton("好吧打扰了", null)
                            .show();
                }
                break;
            case R.id.join_conference:
                //输入密码加入会议
                //创建信息提示框，输入正确密码后加入会议并跳转活动
                final EditText edt = new EditText(this);
                edt.setMaxLines(1);
                new AlertDialog.Builder(this)
                        .setTitle("请输入本房间讨论密码")
                        .setMessage("提示:需房主创建讨论后才会有讨论密码")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(edt)
                        .setPositiveButton("加入", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {

                                //先在服务器获取看有没有记录，有的话说明创建了
                                DiscussionInfo conference = null;
                                conference = DiscussionRoomHelper.queryByRoomID(roomID);
                                if(conference == null){
                                    new AlertDialog.Builder(v.getContext())
                                            .setTitle("错误！")
                                            .setMessage("请等待房主创建房间后再加入")
                                            .setPositiveButton("过会再试", null)
                                            .show();
                                }
                                else{
                                    String psd = edt.getText().toString();
                                    //根据房间名加入会议
                                    Log.d(TAG, "加入的会议ID="+conference.getConfrID());
                                    EMClient.getInstance().conferenceManager().joinConference(conference.getConfrID(), psd,
                                            new EMValueCallBack<EMConference>() {
                                                @Override
                                                public void onSuccess(EMConference emConference) {
//                                                    new AlertDialog.Builder(v.getContext())
//                                                            .setTitle("提示")
//                                                            .setMessage("成功加入")
//                                                            .setPositiveButton("好的", null)
//                                                            .show();
                                                    GroupDiscussionActivity.actionStart(WaitingRoom.this,roomID);
                                                    Log.d("dopamine", "onSuccess: "+"加入会议成功,ID="+emConference.getConferenceId());
                                                }

                                                @Override
                                                public void onError(int i, String s) {
                                                    Log.d(TAG, "加入会议错误: "+i+s);
                                                    Toast.makeText(WaitingRoom.this,"失败:"+i+s,Toast.LENGTH_SHORT).show();
//                                                    new AlertDialog.Builder(v.getContext())
//                                                            .setTitle("错误！")
//                                                            .setMessage("奇怪的错误出现了:"+i+","+s)
//                                                            .setPositiveButton("过会再试", null)
//                                                            .show();
                                                }
                                            });
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
                break;
        }
    }

    /**
     * 该活动的统一入口
     */
    public static void actionStart(Context context,String ID){
        Intent intent = new Intent(context,WaitingRoom.class);
        intent.putExtra("ID",ID);
        context.startActivity(intent);
    }
}

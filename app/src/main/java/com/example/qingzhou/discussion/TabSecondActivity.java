package com.example.qingzhou.discussion;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qingzhou.R;
import com.example.qingzhou.adapter.RoomAdapter;
import com.example.qingzhou.widget.RoomCreateDialog;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import java.util.ArrayList;
import java.util.List;

public class TabSecondActivity extends AppCompatActivity {

    private static final String TAG = "TabSecondActivity";
    private static final int UPDATE_ROOM_VIEW=1;

    private List<EMChatRoom> roomList;
    private RecyclerView recyclerView;
    private RoomAdapter roomAdapter;

    private RoomCreateDialog roomCreateDialog;
    private GetOnlineRoomThread getOnlineRoomThread;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabsecond); //首先获取TabSecondActivity的布局
        initToolBar();      //初始化页面顶部的工具栏
        initRoomRecyclerView();  //初始化显示房间的RecyclerView视图
    }

    @Override
    protected void onResume() {
        super.onResume();
        EMClient.getInstance().groupManager().loadAllGroups();
        EMClient.getInstance().chatManager().loadAllConversations();

        getOnlineRoomThread=new GetOnlineRoomThread();
        getOnlineRoomThread.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        getOnlineRoomThread.setFlag(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getOnlineRoomThread.setFlag(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //退出当前APP时，同时把当前帐号退出环信服务器
        EMClient.getInstance().logout(true, new EMCallBack() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                Log.d(TAG,"退出环信服务器成功");
            }

            @Override
            public void onProgress(int progress, String status) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onError(int code, String message) {
                // TODO Auto-generated method stub
                Log.d(TAG,"退出环信服务器失败，失败代码"+code+"；失败信息："+message);
            }
        });
    }

    //只有当收到类型为UPDATE_ROOMVIEW的消息时
    //才更新一次RecyclerView的Adapter（有可能是新房间被创建/旧房间被删除）
    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what==UPDATE_ROOM_VIEW){
                roomAdapter.notifyItemRangeChanged(0,roomList.size());
                roomAdapter.notifyDataSetChanged();
            }
        }
    };

    //初始化页面工具栏Toolbar
    private void initToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);   //获取当前Activity的工具栏
        toolbar.setTitle("浏览房间");
        setSupportActionBar(toolbar);                   //加载Toolbar控件
    }

    //初始化讨论房间的RecyclerView
    private void initRoomRecyclerView() {
        roomList=new ArrayList<>();
        recyclerView=findViewById(R.id.rv_roomview);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        roomAdapter=new RoomAdapter(roomList);
        recyclerView.setAdapter(roomAdapter);
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_tab_second_activity,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){    //Toolbar上的菜单按钮被点击时的事件
        if (item.getItemId()==R.id.menu_create){    //点击了“创建房间”按钮
            if (roomCreateDialog==null){
                roomCreateDialog=new RoomCreateDialog(TabSecondActivity.this);    //在当前页面实例化对话框
            }
            roomCreateDialog.show();
        }
        return true;
    }

    //在子线程中不停获取最新的房间信息
    private class GetOnlineRoomThread extends Thread{
        private boolean flag=true;      //设置线程暂停与开启的标志位
        private List<EMChatRoom> roomRecord=new ArrayList<>();    //记录本次从服务器获取的房间列表

        public void setFlag(boolean flag) {
            this.flag = flag;
        }

        public void run() {
            while (flag){
                try {
                    roomRecord.clear(); //清空上一次线程运行时记录的房间数组列表
                    //重新从服务器获取新的房间列表
                    //第一个参数为每次取房间的个数10个，第二个参数为服务器后台的游标，首次传null即可
                    roomRecord.addAll(EMClient.getInstance().
                            chatroomManager().fetchPublicChatRoomsFromServer(10, null).getData());
                    if (!roomList.containsAll(roomRecord)){
                        //若当前记录的roomList房间列表没有完全包含重新获取的roomRecord的所有房间
                        roomRecord.removeAll(roomList);
                        roomList.addAll(roomList.size(),roomRecord);
                        mHandler.sendEmptyMessage(UPDATE_ROOM_VIEW);
                    }
                    else if (roomList.containsAll(roomRecord)&&roomList.size()!=roomRecord.size()){
                        //若当前记录的roomList房间列表完全包含了重新获取的roomRecord的所有房间
                        //但是两个列表长度不同
                        roomList.clear();
                        roomList.addAll(roomList.size(),roomRecord);
                        mHandler.sendEmptyMessage(UPDATE_ROOM_VIEW);
                    }
                    Thread.sleep(100);     //每100ms从服务器获取一次房间列表
                } catch (HyphenateException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

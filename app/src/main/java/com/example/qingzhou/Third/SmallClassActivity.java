package com.example.qingzhou.Third;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.example.qingzhou.Base.BaseActivity;
import com.example.qingzhou.R;
import com.example.qingzhou.adapter.MsgAdapter;

import java.util.ArrayList;
import java.util.List;

public class SmallClassActivity extends BaseActivity implements View.OnClickListener {
    private List<ThirdMsg> msgList = new ArrayList<>();
    private ImageButton logobtn;
    private RecyclerView msgRecyclerView;
    private MsgAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.small_class);
        initMsgs();     //初始化对话数据
        initUI();
        initRecyclerView();
    }

    /**
     * 初始化RecyclerView迭代器信息
     */
    private void initRecyclerView(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);
        adapter = new MsgAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);
    }

    /**
     * 初始化界面的UI信息
     */
    private void initUI(){
        Toolbar toolbar = findViewById(R.id.toolbar);   //获取当前Activity的工具栏
        setSupportActionBar(toolbar);
        setupBackAdUp("小课");
        logobtn = findViewById(R.id.logobtn);
        msgRecyclerView = findViewById(R.id.third_recycler_view);
        logobtn.setOnClickListener(this);
    }

    private void initMsgs(){
        //添加一个接收信息
        ThirdMsg msg1 = new ThirdMsg("Hello guys", ThirdMsg.TYPE_RECEIVED);         //构造方法1，普通对话
        msgList.add(msg1);

        //添加一个按钮信息
        List<String> mlist = new ArrayList<String>();
        mlist.add("语言是一个开放的系统");
        mlist.add("人们有选择的自由");
        ThirdMsg msg2 = new ThirdMsg(mlist,"关卡1","一种转变的哲学",ThirdMsg.TYPE_CHOOSE);       //构造方法2，按钮对话
        msgList.add(msg2);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.logobtn:
                ThirdMsg msg = new ThirdMsg("text",ThirdMsg.TYPE_SENT);
                msgList.add(msg);
                adapter.notifyItemChanged(msgList.size()-1);
                msgRecyclerView.scrollToPosition(msgList.size()-1);
                Log.d("dopamine_third", "logobtn click");
                break;
        }
    }

    /**
     * 其他活动调用此方法就可以启动该活动了
     * @param context
     * @param id 传进来的课程ID
     */
    public static void actionStart(Context context, int id){
        Intent intent = new Intent(context, SmallClassActivity.class);
        intent.putExtra("id",id);
        context.startActivity(intent);
    }
}

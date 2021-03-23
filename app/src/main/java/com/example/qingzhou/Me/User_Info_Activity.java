package com.example.qingzhou.Me;

import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.qingzhou.Base.BaseActivity;
import com.example.qingzhou.R;
import com.example.qingzhou.bean.UserInfo;
import com.example.qingzhou.database.UserDBHelper;
import com.example.qingzhou.database.UserDBHelper2;
import com.mob.wrappers.UMSSDKWrapper;

public class User_Info_Activity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "User_Info_Activity";

    private UserInfo info;                      //得到该用户的用户类
    private UserDBHelper mHelper;               //声明一个用户数据库帮助器对象
    private UserDBHelper2 mHelper2 = new UserDBHelper2();               //声明一个用户数据库帮助器对象
    private SharedPreferences mShared;          //声明一个共享参数对象  记录用户手机号
    private Intent intent;                      //构建意图

    private ImageView user_image;               //用户头像
    private TextView user_name;                 //用户昵称
    private TextView user_phone;                //用户手机号码(TextView)
    private String phone;                       //用户手机号码(String)
    private Button info_change_button;          //信息修改按钮
    private TextView user_gender;               //用户性别
    private TextView user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //打开共享参数编辑器
        mShared=getSharedPreferences("share_login",MODE_PRIVATE);
        //获得登录用户的手机号，它的设置相关代码可以在LoginActivity中找到
        phone=mShared.getString("user_phone","");
        //打开数据库链接,并获得实例
        mHelper=UserDBHelper.getInstance(this,2);
        mHelper.openWriteLink();
        //根据登录用户手机号，从数据库中获得该用户类
        info = mHelper2.queryByPhone(phone);
    //    info=mHelper.queryByPhone(phone);
        iniData();
        Log.d(TAG, "onResume: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //系统准备去启动其他活动时调用此函数，关闭数据库连接
        Log.d(TAG, "onPause: 数据库连接关闭");
        mHelper.closeLink();
    }

    private void iniData(){
        //修改标题栏相关信息
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //setupBackAdUp("信息");
        setupBackAdUp(getResources().getString(R.string.message));

        user_name=findViewById(R.id.user_name);                     //用户名实例
        user_phone=findViewById(R.id.user_phone);                   //收集号码实例
        info_change_button=findViewById(R.id.info_change_button);   //信息改变按钮实例
        user_gender=findViewById(R.id.user_gender);                 //用户性别实例
        user_id=findViewById(R.id.user_id);                         //用户id位置
        user_name.setText(info.getUserName());                      //用户名设置为当前登录用户的昵称
        user_phone.setText(info.getPhone());                        //手机号码设置为当前登录用户的手机号码
        user_gender.setText(info.getGender());                      //用户性别设置
        user_id.setText(String.valueOf(info.getId()));              //设置Id
        info_change_button.setOnClickListener(this);                //为修改按钮设置点击监听
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.info_change_button:
                intent=new Intent(User_Info_Activity.this, Info_Change_Activity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}

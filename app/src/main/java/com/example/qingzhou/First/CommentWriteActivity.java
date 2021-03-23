package com.example.qingzhou.First;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.qingzhou.Base.BaseActivity;
import com.example.qingzhou.R;
import com.example.qingzhou.bean.CommentInfo;
import com.example.qingzhou.bean.UserInfo;
import com.example.qingzhou.database.CommentHelper;
import com.example.qingzhou.database.UserDBHelper;
import com.example.qingzhou.database.UserDBHelper2;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommentWriteActivity extends BaseActivity implements View.OnClickListener{

    private UserInfo info;                      //得到该用户的用户类
    private UserDBHelper mHelper;               //声明一个用户数据库帮助器对象
    private UserDBHelper2 mHelper2 = new UserDBHelper2();               //声明一个用户数据库帮助器对象
    private SharedPreferences mShared;          //声明一个共享参数对象  记录用户手机号


    private EditText commentEditText;
    private Button submitBtn;
    private String place;
    private String people;

    private String phone;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment_write);
        iniData();
    }

    private void iniData(){
        //从上一个活动获取数据
        Intent intent = getIntent();
        place = intent.getStringExtra("place");
        people = intent.getStringExtra("people");
        //修改标题栏相关信息
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //setupBackAdUp("账号安全");
        setupBackAdUp(getResources().getString(R.string.commentWriteTitle));
        //绑定控件
        commentEditText = findViewById(R.id.comment_write_EditText);
        submitBtn = findViewById(R.id.submit_btn);
        submitBtn.setOnClickListener(this);
        //绑定Info
        //打开共享参数编辑器
        mShared=getSharedPreferences("share_login",MODE_PRIVATE);
        //获得登录用户的手机号，它的设置相关代码可以在LoginActivity中找到
        phone=mShared.getString("user_phone","");
        //打开数据库链接,并获得实例
        mHelper=UserDBHelper.getInstance(this,2);
        mHelper.openWriteLink();
        //根据登录用户手机号，从数据库中获得该用户类
        info=mHelper2.queryByPhone(phone);
    }

    //其他活动调用此方法就可以启动该活动了
    public static void actionStart(Context context, String place, String people){
        Intent intent = new Intent(context, CommentWriteActivity.class);
        intent.putExtra("place",place);
        intent.putExtra("people",people);
        context.startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.submit_btn:
                CommentInfo commentInfo = new CommentInfo(String.valueOf(info.getId()),info.getUserName(),place,people,
                        simpleDateFormat.format(new Date(System.currentTimeMillis())),commentEditText.getText().toString());
                if(CommentHelper.insertComment(commentInfo)){
                    Toast.makeText(this,getResources().getString(R.string.commentSuccessful),Toast.LENGTH_SHORT).show();
                    finish();
                }else{
                    Toast.makeText(this,getResources().getString(R.string.commentFailed),Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}

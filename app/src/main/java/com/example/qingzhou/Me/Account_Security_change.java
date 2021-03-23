package com.example.qingzhou.Me;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.qingzhou.Base.BaseActivity;
import com.example.qingzhou.R;
import com.example.qingzhou.bean.UserInfo;
import com.example.qingzhou.database.UserDBHelper;
import com.example.qingzhou.database.UserDBHelper2;

public class Account_Security_change extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "Account_Security_change";
    
    private String Choose;              //参数，根据参数选择显示什么界面
    private Intent intent;              //Intent意图，用来接收上一个活动传递过来的参数

    private UserInfo info;                      //得到该用户的用户类
    private UserDBHelper2 mHelper2 = new UserDBHelper2();               //声明一个用户数据库帮助器对象
    private UserDBHelper mHelper;               //声明一个用户数据库帮助器对象
    private SharedPreferences mShared;          //声明一个共享参数对象  记录用户手机号
    private String phone;
    private Button save_button;                 //保存按钮

    //以下为修改密码界面变量声明
    private EditText editText1;         //初始密码框
    private EditText editText2;         //新密码框
    private EditText editText3;         //再次确认框
    //修改密码界面变量声明结束

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent=getIntent();
        Choose=intent.getStringExtra("choose");
        assert Choose != null;
        switch (Choose){
            //如果是选择了修改密码，则加载修改密码界面布局
            case "password":
                setContentView(R.layout.password_change);
                break;
            default:
                setContentView(R.layout.activity_account_security_change);
                break;

        }
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
    }

    private void iniData(){
        //修改标题栏相关信息
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(Choose.equals("password")){
            //setupBackAdUp("修改密码");
            setupBackAdUp(getResources().getString(R.string.Password_Change));

        }else if(Choose.equals("phone")){

        }

        save_button=findViewById(R.id.save_button);                     //得到保存按钮实例
        //为保存按钮设计点击事件
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Choose.equals("password")){
                    editText1=findViewById(R.id.original_password);
                    editText2=findViewById(R.id.new_password);
                    editText3=findViewById(R.id.new_password_check);
                    //检查原密码输入是否正确
                    if(!info.getPassword().equals(editText1.getText().toString())){
                        new AlertDialog.Builder(Account_Security_change.this)
                                .setTitle(R.string.Error)
                                .setMessage(R.string.ErrorMessage1)
                                .setPositiveButton(R.string.confirm, null)
                                .show();
                        return ;
                    }
                    //确认新密码与确认新密码是否一致
                    if(!editText2.getText().toString().equals(editText3.getText().toString())){
                        new AlertDialog.Builder(Account_Security_change.this)
                                .setTitle(R.string.Error)
                                .setMessage(R.string.ErrorMessage2)
                                .setPositiveButton(R.string.confirm, null)
                                .show();
                        return ;
                    }
                    //新密码不能位空
                    if(editText2.getText().toString().equals("")){
                        new AlertDialog.Builder(Account_Security_change.this)
                                .setTitle(R.string.Error)
                                .setMessage(R.string.ErrorMessage3)
                                .setPositiveButton(R.string.confirm, null)
                                .show();
                        return ;
                    }
                    info.setPassword(editText2.getText().toString());
                    mHelper2.updateuser(info);
                    Log.d(TAG, "成功!!!!");
                    //修改成功，并提示
                    new AlertDialog.Builder(Account_Security_change.this)
                            .setTitle(R.string.Success)
                            .setMessage(R.string.SuccessMessage)
                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .show();
                }else if(Choose.equals("phone")){

                }
            }
        });
    }

    @Override
    public void onClick(View v) {

    }
}

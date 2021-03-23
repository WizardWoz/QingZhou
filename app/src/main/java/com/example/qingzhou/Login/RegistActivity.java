package com.example.qingzhou.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.qingzhou.R;
import com.example.qingzhou.database.UserDBHelper;
import com.example.qingzhou.bean.UserInfo;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class RegistActivity extends AppCompatActivity {

    private static final String TAG = "RegistActivity";
    private static final int GET_VERIFY_CODE_SUCCESS=0;
    private static final int SUBMIT_VERIFY_CODE_SUCCESS=1;

    private EditText et_enternewnum;    //手机号码编辑
    private EditText et_enternewname;   //用户名编辑
    private EditText et_enternewpsd;    //密码编辑
    private EditText et_enterconfirmpsd;//再次输入密码编辑
    private EditText et_verifycode_regist;  //输入验证码编辑框
    private Button btn_confirm_regist;  //“确定”按钮
    private Button btn_cancel_regist;   //“取消”按钮
    private Button btn_verifycode_regist;   //“获取验证码”按钮
    private UserDBHelper mUserDBHelper; //用户数据管理类
    private String userPhone;
    private String userName;
    private String userPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.regist);

        et_enternewnum = findViewById(R.id.et_enternewnum);
        et_enternewname = findViewById(R.id.et_enternewname);
        et_enternewpsd = findViewById(R.id.et_enternewpsd);
        et_enterconfirmpsd = findViewById(R.id.et_enterconfirmpsd);
        et_verifycode_regist=findViewById(R.id.et_verifycode_regist);
        btn_confirm_regist = findViewById(R.id.btn_confirm_regist);
        btn_cancel_regist = findViewById(R.id.btn_cancel_regist);
        btn_verifycode_regist=findViewById(R.id.btn_verifycode_regist);

        //注册界面“确认”与“取消”按钮的监听事件
        btn_confirm_regist.setOnClickListener(new MyOnClickListener_Regist());
        btn_cancel_regist.setOnClickListener(new MyOnClickListener_Regist());
        //“获取验证码”按钮
        btn_verifycode_regist.setOnClickListener(new MyOnClickListener_Regist());

        //建立本地数据库
        if (mUserDBHelper == null) {
            mUserDBHelper = UserDBHelper.getInstance(this,2);
            mUserDBHelper.openReadLink();
        }
        SMSSDK.registerEventHandler(eh);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterEventHandler(eh);
    }

    EventHandler eh=new EventHandler(){     //注册监听回调，后面执行发送验证的时候才能正常收到回调
        @Override
        public void afterEvent(int event, int result, Object data) {
            // TODO 此处不可直接处理UI线程，处理后续操作需传到主线程中操作
            if (result == SMSSDK.RESULT_COMPLETE) {     //回调完成
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    //提交验证码成功
                    mHandler.sendEmptyMessage(SUBMIT_VERIFY_CODE_SUCCESS);
                } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                    //获取验证码成功
                    mHandler.sendEmptyMessage(GET_VERIFY_CODE_SUCCESS);
                } else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {
                    //返回支持发送验证码的国家列表

                }
            } else {
                ((Throwable) data).printStackTrace();
            }
        }
    };

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what==GET_VERIFY_CODE_SUCCESS){     //获取验证码成功
                Toast.makeText(RegistActivity.this,"获取验证码成功",Toast.LENGTH_SHORT).show();
            }
            else if (msg.what==SUBMIT_VERIFY_CODE_SUCCESS){     //提交验证码成功
                //调用异步方法，创建一个EMClient实例
                signUp(userPhone,userName,userPwd);
            }
        }
    };

    class MyOnClickListener_Regist implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if(v.getId()==R.id.btn_confirm_regist) { //确认注册按钮的监听事件
                register_check();
            }
            else if (v.getId()==R.id.btn_cancel_regist){    //取消注册按钮的监听事件
                //创建提醒对话框的建造器
                AlertDialog.Builder builder = new AlertDialog.Builder(RegistActivity.this);
                //给建造器设置对话框的标题文本
                builder.setTitle(R.string.LoginM1);
                //给建造器设置对话框的信息文本
                builder.setMessage(R.string.LoginM2);
                //给建造器设置对话框的肯定按钮文本及其点击监听器
                builder.setPositiveButton(R.string.LoginM3, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent regist_to_mainframe=new Intent(RegistActivity.this, MainFrameActivity.class);
                        startActivity(regist_to_mainframe);
                        finish();
                    }
                });
                builder.setNegativeButton(R.string.LoginM4, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onRestart();
                    }
                });
                //根据建造器完成提醒对话框对象的构建
                AlertDialog alert=builder.create();
                //在界面上显示对话框
                alert.show();
            }
            else if (v.getId()==R.id.btn_verifycode_regist){    //获取验证码按钮
                // 请求验证码，其中country表示国家代码，如“86”；phone表示手机号码，如“13800138000”
                SMSSDK.getVerificationCode("86",et_enternewnum.getText().toString());
            }
        }
    }

    //确认按钮的监听事件
    public void register_check() {
        userPhone = et_enternewnum.getText().toString().trim();
        userName = et_enternewname.getText().toString().trim();
        userPwd = et_enternewpsd.getText().toString().trim();
        String userPwdCheck = et_enterconfirmpsd.getText().toString().trim();
        String verify_code=et_verifycode_regist.getText().toString();
        //通过手机号检查用户是否存在
        UserInfo info = mUserDBHelper.queryByPhone(userPhone);
        //用户已经存在时返回，给出提示文字
        if (info != null) {
            Toast.makeText(this, R.string.registerM1, Toast.LENGTH_SHORT).show();
        }
        //用户还不存在，新建用户
        //假如输入的手机号码不够11位
        if(userPhone.length()!=11){
            Toast.makeText(this, R.string.registerM2
                    , Toast.LENGTH_SHORT).show();
        }
        else if (userPwd.length()<6){
            Toast.makeText(this, "密码长度必须大于6"
                    , Toast.LENGTH_SHORT).show();
        }
        else if (userPwd.equals(userPwdCheck) == false) {     //两次密码输入不一样
            Toast.makeText(this, R.string.registerM3
                    , Toast.LENGTH_SHORT).show();
        }
        else if (verify_code.length()-1<3){
            Toast.makeText(this,R.string.registerM4,Toast.LENGTH_SHORT).show();
        }
        //检查用户输入的信息无误后，向MobSDK后台发送用户输入的验证码
        else {
            SMSSDK.submitVerificationCode("86",userPhone,verify_code);
            signUp(userPhone,userName,userPwd);
        }
    }

    //创建EMClient的异步方法
    private void signUp(String userPhone, String userName, String userPwd){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().createAccount(userPhone, userPwd);
                    Log.e("RegistActivity", "注册方法signUp(): 注册成功");
                    UserInfo mUser = new UserInfo(userPhone, userName, userPwd);
                    mUserDBHelper.openWriteLink();
                    long flag = mUserDBHelper.insertUserData(mUser); //新建用户信息
                    if (flag == -1) {
                        Log.d(TAG, "本地数据库注册失败，请再次检查所填信息");
                    }
                    else {
                        Log.d(TAG, "本地数据库注册成功 ");
                        Intent intent_Register_to_Login = new Intent(RegistActivity.this
                                , MainFrameActivity.class);    //切换User Activity至Login Activity
                        startActivity(intent_Register_to_Login);
                        finish();
                    }
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    Log.e("RegistActivity", "注册方法signUp(): 注册失败"+e.getErrorCode()+e.getMessage());
                }
            }
        }).start();
    }
}
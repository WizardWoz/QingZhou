package com.example.qingzhou.Login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qingzhou.R;

@SuppressLint("DefaultLocale")
public class ForgetPsdActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText et_enterphonenum;  //声明输入手机号的编辑框对象
    private EditText et_password_first; // 声明首次输入密码的编辑框对象
    private EditText et_password_second; // 声明验证密码的编辑框对象
    private EditText et_verifycode; // 声明填写验证码的编辑框对象
    private String mVerifyCode; // 验证码
    private String mPhone; // 手机号码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgetpsd);
        // 从布局文件中获取名叫et_password_first的编辑框
        et_password_first = findViewById(R.id.et_password_first);
        // 从布局文件中获取名叫et_password_second的编辑框
        et_password_second = findViewById(R.id.et_password_second);
        // 从布局文件中获取名叫et_verifycode的编辑框
        et_verifycode = findViewById(R.id.et_verifycode);
        //从布局文件中获取名叫et_enterphonenum的编辑框
        et_enterphonenum=findViewById(R.id.et_enterphonenum);

        //为获取验证码和确定按钮绑定单击监听器
        findViewById(R.id.btn_verifycode).setOnClickListener(this);
        findViewById(R.id.btn_confirm).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);

        // 从前一个页面获取要修改密码的手机号码，自动填写进手机号码框
        mPhone = getIntent().getStringExtra("phone");
        et_enterphonenum.setText(mPhone);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_verifycode) { // 点击了“获取验证码”按钮
            if (mPhone == null || mPhone.length() < 11) {
                Toast.makeText(this, R.string.toastM1 , Toast.LENGTH_SHORT).show();
                return;
            }
            // 生成六位随机数字的验证码
            mVerifyCode = String.format("%06d", (int) (Math.random() * 1000000 % 1000000));
            // 弹出提醒对话框，提示用户六位验证码数字
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.Forget5);
            builder.setMessage(getResources().getString(R.string.HintMessage1) + mPhone + getResources().getString(R.string.HintMessage2) + mVerifyCode + getResources().getString(R.string.HintMessage3) );
            builder.setPositiveButton(R.string.Ok, null);
            AlertDialog alert = builder.create();
            alert.show();
        }
        else if (v.getId() == R.id.btn_confirm) { // 点击了“确定”按钮
            String password_first = et_password_first.getText().toString();
            String password_second = et_password_second.getText().toString();
            if (password_first.length() < 6 || password_second.length() < 6) {
                Toast.makeText(this, R.string.toastM2 , Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password_first.equals(password_second)) {
                Toast.makeText(this, R.string.toastM3 , Toast.LENGTH_SHORT).show();
                return;
            }
            if (!et_verifycode.getText().toString().equals(mVerifyCode)) {
                Toast.makeText(this, R.string.toastM4 , Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, R.string.toastM5 , Toast.LENGTH_SHORT).show();
                // 把手机号，修改好的新密码返回给前一个页面
                Intent intent = new Intent();
                intent.putExtra("phone",mPhone);
                intent.putExtra("new_password", password_first);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }
        else if (v.getId()== R.id.btn_cancel){ //点击了“取消”按钮
            // 弹出提醒对话框，提示用户是否取消当前手机账号的密码找回
            //创建提醒对话框的建造器
            AlertDialog.Builder builder = new AlertDialog.Builder(ForgetPsdActivity.this);
            //给建造器设置对话框的标题文本
            builder.setTitle(R.string.LoginM1);
            //给建造器设置对话框的信息文本
            builder.setMessage(R.string.HintM5);
            //给建造器设置对话框的肯定按钮文本及其点击监听器
            builder.setPositiveButton(R.string.LoginM3, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent forgetpsd_to_login=new Intent(ForgetPsdActivity.this, MainFrameActivity.class);
                    startActivity(forgetpsd_to_login);
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
    }

}


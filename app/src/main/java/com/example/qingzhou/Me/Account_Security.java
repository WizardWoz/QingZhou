package com.example.qingzhou.Me;

import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.qingzhou.Base.BaseActivity;
import com.example.qingzhou.R;

public class Account_Security extends BaseActivity implements View.OnClickListener {

    private Button password_change_button;
    private Button phone_change_button;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_security);
        iniData();
    }

    private void iniData(){
        //修改标题栏相关信息
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //setupBackAdUp("账号安全");
        setupBackAdUp(getResources().getString(R.string.account_security));
        password_change_button=findViewById(R.id.password_change);
        phone_change_button=findViewById(R.id.phone_change);
        password_change_button.setOnClickListener(this);
        phone_change_button.setOnClickListener(this);
        intent=new Intent(Account_Security.this,Account_Security_change.class);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.password_change:
                intent.putExtra("choose","password");
                startActivity(intent);
                break;
            case R.id.phone_change:
                intent.putExtra("choose","phone");
                startActivity(intent);
                break;
        }
    }
}

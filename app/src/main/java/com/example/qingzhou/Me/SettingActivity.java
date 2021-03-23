package com.example.qingzhou.Me;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;

import com.example.qingzhou.Base.BaseActivity;
import com.example.qingzhou.Login.MainFrameActivity;
import com.example.qingzhou.R;

public class SettingActivity extends BaseActivity {

    private Configuration config;
    private Resources resources;
    private DisplayMetrics dm;
    private Button languageSetting;            //语言按钮
    private Toolbar toolbar;                    //标题栏

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        //初始化数据
        iniData();
    }

    private void iniData(){
        //修改标题栏相关信息
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupBackAdUp(getResources().getString(R.string.settings));
        resources = getResources();//获得res资源对象
        config = resources.getConfiguration();//获得设置对象
        dm = resources.getDisplayMetrics();//获得屏幕参数：主要是分辨率，像素等。
        languageSetting = findViewById(R.id.Language_setting);
        languageSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //多语言转换
                Intent intent = new Intent(SettingActivity.this,SetLanguageActivity.class);
                startActivity(intent);
            }
        });

    }
}

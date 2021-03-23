package com.example.qingzhou.Third;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qingzhou.Base.BaseActivity;
import com.example.qingzhou.R;
import com.example.qingzhou.adapter.NewsAdater;
import com.example.qingzhou.bean.News;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TabThirdActivity extends AppCompatActivity implements View.OnClickListener{

    private Button SmallClassBtn;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabthird);
        initToolBar();
        initUI();
    }

    //初始化页面工具栏Toolbar
    private void initToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);   //获取当前Activity的工具栏
        toolbar.setTitle("小课环节");
        setSupportActionBar(toolbar);                   //加载Toolbar控件
    }

    private void initUI(){
        //点击进入小课的按钮
        SmallClassBtn = findViewById(R.id.SmallClassEnterBtn);
        SmallClassBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.SmallClassEnterBtn:
                SmallClassActivity.actionStart(this,1);
                break;
        }
    }
}

package com.example.qingzhou.Me;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.Nullable;

import com.example.qingzhou.Base.BaseActivity;
import com.example.qingzhou.Login.MainFrameActivity;
import com.example.qingzhou.R;
import com.example.qingzhou.activity.TabHostActivity;
import com.example.qingzhou.util.LanguageType;
import com.example.qingzhou.util.MultiLanguageUtil;

public class SetLanguageActivity extends BaseActivity implements View.OnClickListener {

    private RelativeLayout rl_followSystem;
    private RelativeLayout rl_chinese;
    private RelativeLayout rl_english;
    private ImageView iv_english;
    private ImageView iv_followSystem;
    private ImageView iv_chinese;
    private int savedLanguageType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.language_settings);
        MultiLanguageUtil.init(this);
        initViews();
    }

    private void initViews(){
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //setupBackAdUp("多语言");
        setupBackAdUp(getResources().getString(R.string.Language));
        rl_followSystem = findViewById(R.id.rl_followSystem);
        rl_chinese = findViewById(R.id.rl_chinese);
        rl_english = findViewById(R.id.rl_english);
        iv_followSystem = findViewById(R.id.iv_followSystem);
        iv_chinese = findViewById(R.id.iv_chinese);
        iv_english = findViewById(R.id.iv_english);
        rl_followSystem.setOnClickListener(this);
        rl_chinese.setOnClickListener(this);
        rl_english.setOnClickListener(this);
        savedLanguageType = MultiLanguageUtil.getInstance().getLanguageType();
        if (savedLanguageType == LanguageType.LANGUAGE_FOLLOW_SYSTEM) {
            setFollowSytemVisible();
        } else if (savedLanguageType == LanguageType.LANGUAGE_CHINESE) {
            setSimplifiedVisible();
        } else if (savedLanguageType == LanguageType.LANGUAGE_EN) {
            setEnglishVisible();
        }  else {
            setSimplifiedVisible();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        int selectedLanguage = 0;
        switch (id) {
            case R.id.rl_followSystem:
                setFollowSytemVisible();
                selectedLanguage = LanguageType.LANGUAGE_FOLLOW_SYSTEM;
                break;
            case R.id.rl_chinese:
                setSimplifiedVisible();
                selectedLanguage = LanguageType.LANGUAGE_CHINESE;
                break;
            case R.id.rl_english:
                setEnglishVisible();
                selectedLanguage = LanguageType.LANGUAGE_EN;
                break;
        }
        MultiLanguageUtil.getInstance().updateLanguage(selectedLanguage);
        Intent intent = new Intent(SetLanguageActivity.this, TabHostActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void setFollowSytemVisible() {
        iv_followSystem.setVisibility(View.VISIBLE);
        iv_english.setVisibility(View.GONE);
        iv_chinese.setVisibility(View.GONE);
    }

    private void setSimplifiedVisible() {
        iv_followSystem.setVisibility(View.GONE);
        iv_english.setVisibility(View.GONE);
        iv_chinese.setVisibility(View.VISIBLE);
    }

    private void setEnglishVisible() {
        iv_followSystem.setVisibility(View.GONE);
        iv_english.setVisibility(View.VISIBLE);
        iv_chinese.setVisibility(View.GONE);
    }
}

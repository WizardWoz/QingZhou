package com.example.qingzhou.Third;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import com.example.qingzhou.Base.BaseActivity;
import com.example.qingzhou.R;
import com.google.android.material.appbar.CollapsingToolbarLayout;

public class NewsActicity extends BaseActivity {

    public static final String NEWS_TITLE = "titile";
    public static final String NEWS_PRECONTENT = "content";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_acticity);
        Intent intent = getIntent();
        String title = intent.getStringExtra(NEWS_TITLE);
        String content = intent.getStringExtra(NEWS_PRECONTENT);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout)
                findViewById(R.id.collapsing_toolbar);
        TextView TitleTextView = (TextView) findViewById(R.id.news_title_act);
        TextView NewsContentText = (TextView)findViewById(R.id.news_content_view);
        setSupportActionBar(toolbar);
        ActionBar actionBar  = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        collapsingToolbar.setTitle(" ");
        TitleTextView.setText(title);

        NewsContentText.setText(content);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

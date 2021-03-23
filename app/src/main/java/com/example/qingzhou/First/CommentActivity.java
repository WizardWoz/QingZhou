package com.example.qingzhou.First;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.util.LogTime;
import com.example.qingzhou.Base.BaseActivity;
import com.example.qingzhou.R;
import com.example.qingzhou.adapter.CommentAdapter;
import com.example.qingzhou.bean.CommentInfo;
import com.example.qingzhou.database.CommentHelper;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class CommentActivity extends BaseActivity implements View.OnClickListener{

    private static final String TAG = "CommentActivity";

    private List<CommentInfo> commentInfoList = null;

    private String place;
    private String people;
    private RecyclerView commentrecyclerView;
    private FloatingActionButton floatingActionButton;
    ImageView titleImage = null;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_comment);
    }

    @Override
    protected void onResume() {
        //从上一个活动获取数据
        Intent intent = getIntent();
        place = intent.getStringExtra("place");
        people = intent.getStringExtra("people");
        super.onResume();
        iniList();
        initData();
    }

    private void iniList(){
        commentInfoList = CommentHelper.queryByPlacePeople(place,people);
        if(commentInfoList == null){
            commentInfoList = new ArrayList<CommentInfo>();
            Log.d(TAG, "iniList: "+"遇到空值");
            CommentInfo tempcommentInfo = new CommentInfo(null,null,
                    null,null,null,"暂无评论~");
            commentInfoList.add(tempcommentInfo);
        }
     /*  for(int i=0;i<commentInfoList.size();i++){
            CommentInfo temp = commentInfoList.get(i);
            Log.d("获取评论", "click "
                    +temp.get_id()+" "
                    +temp.getComment_user_name()+" "
                    +temp.getPlace()+" "
                    +temp.getPeople()+" "
                    +temp.getComment_text()+" "
                    +temp.getComment_time());
        }*/
    }

    @SuppressLint("ResourceAsColor")
    private void initData(){
        //控件绑定
        titleImage = findViewById(R.id.first_image_view);
        titleImage.setImageResource(R.drawable.restaurant);
        commentrecyclerView = findViewById(R.id.comment_recyclerView);
        floatingActionButton = findViewById(R.id.comment_floatbtn);
        floatingActionButton.setOnClickListener(this);
        //修改标题栏相关信息
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(place + " " + people);
        //设置标题
        collapsingToolbarLayout.setCollapsedTitleTextColor(getResources().getColor(R.color.black));         //折叠后黑色
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(R.color.black));               //未折叠时黑色
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        //setupBackAdUp("修改信息");
//        setupBackAdUp(getResources().getString(R.string.ChangeInfor));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        commentrecyclerView.setLayoutManager(layoutManager);
        CommentAdapter commentAdapter = new CommentAdapter(commentInfoList);
        commentrecyclerView.setAdapter(commentAdapter);
    }

    //其他活动调用此方法就可以启动该活动了
    public static void actionStart(Context context, String place, String people){
        Intent intent = new Intent(context,CommentActivity.class);
        intent.putExtra("place",place);
        intent.putExtra("people",people);
        context.startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.comment_floatbtn:
             //   Toast.makeText(this,"you click floatbtn",Toast.LENGTH_SHORT).show();
                CommentWriteActivity.actionStart(this,place,people);
                break;
        }
    }
}

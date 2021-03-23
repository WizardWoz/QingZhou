package com.example.qingzhou.First;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.qingzhou.Base.BaseActivity;
import com.example.qingzhou.R;

import org.w3c.dom.Text;

public class FirstTest extends BaseActivity implements View.OnClickListener{

    private static final String TAG = "FirstTest";

    private String place;
    private String people;
    private ImageView dialogimg;
    private TextView[] textViewlist;                            //保存对话内容的数组
    private ImageView[] imglist;                                //保存对话框的数组
    private TextView test;          //先拿来测试下
    private TextView test2;         //试试
    private int count=0;              //试试

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_test);
        initData();
    }

    private void initData(){
        //从上一个活动获取数据
        Intent intent = getIntent();
        place = intent.getStringExtra("place");
        people = intent.getStringExtra("people");
        //修改标题栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);                                  //加载Toolbar控件
        setupBackAdUp(place+"+"+people);                                 //由于活动是继承了BaseActivity,里面有setupBackAdUp方法

        dialogimg = findViewById(R.id.dialogimg);
        dialogimg.setOnClickListener(this);
        //动态修改图片
        createDialogBox(1);                                              //动态创建对话框
        setDialog(1);                                                    //动态更改对话内容
        setImg();                                                       //动态更改图片
    //    Log.d(TAG, "initData: "+place+" "+people);
    }

    //其他活动调用此方法就可以启动该活动了
    public static void actionStart(Context context, String place, String people){
        Intent intent = new Intent(context,FirstTest.class);
        intent.putExtra("place",place);
        intent.putExtra("people",people);
        context.startActivity(intent);
    }

    /**
     *
     * @param boxnum
     */
    private void createDialogBox(int boxnum){

    }

    /**
     *
     * @param dialognum
     */
    //以后根据数据库保存的内容来更改，现在先固定
    private void setDialog(int dialognum){
//        textViewlist = new TextView[dialognum];
        test = findViewById(R.id.testtv);
        test2 = findViewById(R.id.explain);
    }

    //以后根据数据库保存的内容来更改，现在先固定
    private void setImg(){
        dialogimg.setImageResource(R.drawable.restaurant);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //点击后出现文字
            case R.id.dialogimg:
                if(count == 0){
                    test.setText("Let me spring for dinner.");
                    test.setAlpha(0f);
                    test.setVisibility(View.VISIBLE    );
                    test.animate()
                            .alpha(1f)
                            .setDuration(800)
                            .setListener(null);

                }else if(count == 1){
                    test2.setText("spring for 请客");
                    test2.setAlpha(0f);
                    test2.setVisibility(View.VISIBLE);
                    test2.animate()
                            .alpha(1f)
                            .setDuration(800)
                            .setListener(null);
                    test.setText(Html.fromHtml("Let me " + "<font color='red'><u>" + "spring for"+ "</u></font>" + " dinner."));
                }
                count++;
                break;
        }
    }
}

package com.example.qingzhou.First;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qingzhou.R;

import org.w3c.dom.Text;

import java.util.Random;
import java.util.Stack;

public class TabFirstActivity extends AppCompatActivity implements View.OnClickListener {

    private String[] strlist = {"餐厅","洗手间","电影院","家","学校","超市","公司","医院",
                                "老师","朋友","同学","陌生人","上属","店员","医生","服务员"};
    private Button[] buttonlist = new Button[16];                   //布局的按钮控件列表
    private Animation[] animlist = new Animation[16];               //保存浮动动画
    private Display display=null;
    private Rect[] rect = new Rect[16];                             //上面布局的Rect,用来判断是否重叠
    private RelativeLayout relativeLayout1;
    private RelativeLayout relativeLayout2;
    private TextView textView1;
    private TextView textView2;
    private Button TalkerShow;                                      //进入下一个界面按钮


    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabfirst);
        initData();
                                                        //这里的0 1 用来标识上下布局的
        setbtnlayout(relativeLayout1,0);
        setbtnlayout(relativeLayout2,1);

    }

    private void initData(){
        relativeLayout1 = findViewById(R.id.Re1);
        relativeLayout2 = findViewById(R.id.Re2);
        textView1 = findViewById(R.id.textview1);
        textView2 = findViewById(R.id.textview2);
        TalkerShow = findViewById(R.id.Talkerbtn);
        TalkerShow.setOnClickListener(this);                //注册监听
        //Rect初始化，因为如果为null的话后面判断会报错
        for(int i=0 ;i<rect.length; i++){
            rect[i] = new Rect(0,0,1,1);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setbtnlayout(RelativeLayout relativeLayout, int num){
        display = getWindowManager().getDefaultDisplay();                   //得到屏幕长宽度
        Log.d("No", "宽度"+display.getWidth()+"高度"+display.getHeight());

        //一次添加8个
        for(int i=0 ;i<8 ;i++){
            addfloatbtn(relativeLayout,num,i);                              //根据num和i动态添加一个按钮
        }
    }

    @Override
    public void onClick(View v) {
        //轮询16个按钮，值到遍历到我点击的
        if(v.getId() == R.id.Talkerbtn){
            CommentActivity.actionStart(TabFirstActivity.this,textView1.getText().toString(),textView2.getText().toString());
            //    FirstTest.actionStart(TabFirstActivity.this,textView1.getText().toString(),textView2.getText().toString());
        }else{
            for (int i = 0; i < buttonlist.length; i++) {
                if (v.getId() == buttonlist[i].getId()) {   //这里获取的id就不会错

                    final Button button = buttonlist[i];
                    button.setEnabled(false);               //设置不可重复点击
                    button.bringToFront();                  //设置按钮在最上的图层
                    //先把TextView的值修改
                    if(i<8){
                        textView1.setText(button.getText());
                        textView1.setTextColor(Color.parseColor("#000000"));
                    }else{
                        textView2.setText(button.getText());
                        textView2.setTextColor(Color.parseColor("#000000"));
                    }
                    //实现上浮
                    Animation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,Animation.RELATIVE_TO_SELF ,
                            Animation.RELATIVE_TO_SELF, -display.getHeight());//设置平移的起点和终点
                    translateAnimation.setDuration(2000);//动画持续的时间为2s
                    translateAnimation.setFillEnabled(true);//使其可以填充效果从而不回到原地
                    translateAnimation.setFillAfter(true);//不回到起始位置
                    translateAnimation.setInterpolator(new AccelerateInterpolator());

                    final Button finalBtn = buttonlist[i];                                  //把按钮传入内部类
                    final int finali = i;                                                   //把i传入内部类
                    //设置监听
                    translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                         //   Log.d("测试", "onAnimationEnd: ");
                            //删除结点并重新创建
                            RelativeLayout re;
                            int tepnum;
                            if(finali<8){                       //由于按钮保存在同一个数组1-16，但是它们又是分开的，所以只能这样判断要处理哪个布局
                                re = relativeLayout1;
                                tepnum=0;
                            }else{
                                re = relativeLayout2;
                                tepnum=1;
                            }
                            re.removeView(button);              //移除
                            buttonlist[finali]=null;                            //数组清空
                            rect[finali]=new Rect(0,0,1,1);     //rect清空
                            addfloatbtn(re,tepnum,finali%8);                //这里取余8是因为传过去要对应是该布局的第几个，而不是总数的第几个
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    button.startAnimation(translateAnimation);                                             //注册动画回调
                    break;
                }
            }
        }

    }

    private void addfloatbtn(RelativeLayout relativeLayout, int num,int i){
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(130, 130);
        Button tempbtn = new Button(this);
        int btnmarginleft=0;                            //按钮的位置marginleft
        int btnmargintop=0;                             //按钮的布局marginright
        boolean sign = true;
        while(sign){
            sign=false;                                     //一开始标记为可以跳出循环
            btnmarginleft = (int)(display.getWidth())*1/10+(int)(display.getWidth()*2/3 * Math.random());
            btnmargintop = (int)(display.getWidth())*1/10+(int)(display.getHeight()* 1/3  * Math.random());
            Rect temprect = new Rect(btnmarginleft-30,btnmargintop-30,btnmarginleft+140,btnmargintop+140);
            //根据num来遍历上下Rect
            for(int j = num*8; j<(num+1)*8 ;j++){
                //这里temprect一定要放在前面，因为如果方法返回false会改变调用此方法的元素的值为交点
                if(temprect.intersect(rect[j])){                  //如果碰到了重叠的状况，则设置为true，在进行循环
                    sign=true;
                    break;
                }
            }
            rect[i+num*8] = temprect;                               //将该rect存入数组
        }
        //经过了一个while循环，此时出来的btnmarginleft和top一定不会重叠了
        layoutParams.leftMargin = btnmarginleft;
        layoutParams.topMargin = btnmargintop;
        //给按钮设置相关参数
        tempbtn.setLayoutParams(layoutParams);
        tempbtn.setText(strlist[i+num*8]);
        tempbtn.setId(i+num*8);
        tempbtn.setBackground(getDrawable(R.drawable.bubble));
        tempbtn.setOnClickListener(this);                                       //注册监听
        //缩放动画
        Animation anim2 = new ScaleAnimation(0,1.0f,
                                        0,1.0f,
                                        Animation.RELATIVE_TO_SELF,0.5f,//以自身0.5宽度为轴缩放
                                        Animation.RELATIVE_TO_SELF,0.5f);
        anim2.setDuration(500);
        anim2.setFillAfter(true);
        //上浮动画
        Animation anim = new TranslateAnimation(0,0,(int)(-5-5*Math.random()),(int)(5+5*Math.random()));            //浮动动画Animation
        anim.setInterpolator(new AccelerateInterpolator());
        anim.setDuration((int)(2500+Math.random()*2500));  //浮动时间
        anim.setRepeatCount(Integer.MAX_VALUE);
        anim.setRepeatMode(Animation.REVERSE);

        AnimationSet set = new AnimationSet(true);      //动画集合，用来将多个动画合在一起
        set.addAnimation(anim);                         //将缩放动画和上浮动画合二为一，一起执行
    //    set.addAnimation(anim2);
        tempbtn.startAnimation(set);                       //实现浮动动画
    //    Log.d("调试", "set"+String.valueOf(i+1)+":"+btnmarginleft+":"+btnmargintop);
        relativeLayout.addView(tempbtn);
        buttonlist[i+num*8]=tempbtn;
    }
}

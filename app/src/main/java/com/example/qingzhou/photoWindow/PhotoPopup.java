package com.example.qingzhou.photoWindow;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;

import com.example.qingzhou.R;

public class PhotoPopup extends PopupWindow {
    private View view;
    private Context context;
    private View.OnClickListener selectListener;    //相册选择的监听器
    private View.OnClickListener captureListener;   //拍照这个动作的监听器

    public PhotoPopup(Activity context, View.OnClickListener selectListener, View.OnClickListener captureListener) {
        super(context);
        this.context = context;
        this.selectListener = selectListener;
        this.captureListener = captureListener;
        Init();
    }

    private void Init(){
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.change_item,null);
        Button btn_cemara = (Button) view.findViewById(R.id.camera_btn);
        Button btn_select = (Button) view.findViewById(R.id.select_btn);
        Button btn_cannels = (Button) view.findViewById(R.id.Bcannel_btn);;

        btn_cemara.setOnClickListener(captureListener);
        btn_select.setOnClickListener(selectListener);
        btn_cannels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        //导入布局
        this.setContentView(view);

        //设置动画效果
        this.setAnimationStyle(R.style.pop_anim_style);
        this.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        this.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        //设置可触
        this.setFocusable(true);

        ColorDrawable cd = new ColorDrawable(0x0000000);
        this.setBackgroundDrawable(cd);

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int height = view.findViewById(R.id.pop_item).getTop();
                int y = (int) motionEvent.getY();
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    if (y < height){
                        dismiss();
                    }
                }
                return true;
            }
        });
    }


}

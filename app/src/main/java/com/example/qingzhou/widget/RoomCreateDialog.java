package com.example.qingzhou.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.qingzhou.R;
import com.example.qingzhou.util.HttpUtil;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import org.json.JSONException;

import java.io.IOException;

public class RoomCreateDialog extends Dialog{

    private String[] themeArray={"主题1","主题2","主题3"};    //定义房间主题列表需要显示的文本数组
    private Integer[] capacityArray={2,3,4};    //定义房间人数列表需要显示的文本数组
    private String roomTheme;       //记录房间主题
    private int roomCapacity;       //记录房间容量

    private Spinner sp_roomtheme,sp_roomcapacity;   //声明房间主题和容量下拉框对象
    private Button btn_confirmroom;     //“确定”按钮
    private Button btn_cancleroom;      //“取消”按钮

    public RoomCreateDialog(Context context){
        super(context,R.style.RoomCreateDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_dialog);
        setCanceledOnTouchOutside(false);       //设置点击对话框外部区域不会使对话框消失
        initView();
    }

    private void initView(){
        sp_roomtheme=findViewById(R.id.sp_roomtheme);
        sp_roomcapacity=findViewById(R.id.sp_roomcapacity);
        btn_confirmroom=findViewById(R.id.btn_confirmroom);
        btn_cancleroom=findViewById(R.id.btn_cancleroom);
        //初始化房间主题下拉列表
        initThemeSpinner();
        //初始化房间人数下拉列表
        initCapacitySpinner();
        //“确定”按钮与“取消”按钮
        initButtons();
    }

    private void initButtons() {
        //“确定”按钮的点击事件
        btn_confirmroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                HttpUtil.createChatRoom(roomTheme,roomCapacity);
                dismiss();      //关闭创建房间对话框
            }
        });
        //“取消”按钮的点击事件
        btn_cancleroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();     //关闭创建房间对话框，房间RecyclerView不更新
            }
        });
    }

    private void initThemeSpinner() {
        //声明一个下拉列表的数组适配器
        ArrayAdapter<String> themeAdapter=new ArrayAdapter<String>(getContext(),R.layout.item_select,
                themeArray);
        //设置下拉框的数组适配器
        sp_roomtheme.setAdapter(themeAdapter);
        //设置下拉框默认显示第一项
        sp_roomtheme.setSelection(0);
        //给下拉框设置选择监听器，一旦用户选中某一项，就触发监听器的OnItemSelected方法
        sp_roomtheme.setOnItemSelectedListener(new ThemeSelectedListener());
    }

    //初始化房间人数下拉列表
    private void initCapacitySpinner() {
        //声明一个下拉列表的数组适配器
        ArrayAdapter<Integer> capacityAdapter=new ArrayAdapter<Integer>(getContext(),R.layout.item_select,
                capacityArray);
        //设置下拉框的数组适配器
        sp_roomcapacity.setAdapter(capacityAdapter);
        //设置下拉框默认显示第一项
        sp_roomcapacity.setSelection(0);
        //给下拉框设置选择监听器，一旦用户选中某一项，就触发监听器的OnItemSelected方法
        sp_roomcapacity.setOnItemSelectedListener(new CapacitySelectedListener());
    }

    private class ThemeSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        //选中了某一个主题时的处理方法
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            //记录下当前主题
            roomTheme=themeArray[position];
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private class CapacitySelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            //记录下当前容量
            roomCapacity=capacityArray[position];
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }
}

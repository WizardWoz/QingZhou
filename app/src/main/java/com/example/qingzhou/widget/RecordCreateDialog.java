package com.example.qingzhou.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.qingzhou.R;
import com.example.qingzhou.discussion.AudioRecorder;

public class RecordCreateDialog extends Dialog {

    private static final String TAG = "RecordCreateDialog";
    private int timerCount;
    private Button btn_confirm_record;
    private Button btn_cancel_record;
    private TextView current_length;    //随时间不断更新的文本计时器
    private ProgressBar progressBar;
    private LocalBroadcastManager localBroadcastManager;

    public RecordCreateDialog(Context context) {
        super(context, R.style.RecordCreateDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_dialog);
        setCanceledOnTouchOutside(false);       //设置点击对话框外部区域不会使对话框消失
        initView();     //设置计时器窗口的显示内容
    }

    private void initView() {
        current_length=findViewById(R.id.tv_current_length);
        progressBar=findViewById(R.id.pb_current_pro);
        DialogRefreshTask refreshTask=new DialogRefreshTask();
        refreshTask.execute();     //启动刷新计时对话框UI的异步任务
        //“确定”按钮与“取消”按钮
        btn_confirm_record = findViewById(R.id.btn_confirm_record);
        btn_cancel_record = findViewById(R.id.btn_cancel_record);

        //“确定”按钮的点击事件
        btn_confirm_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                localBroadcastManager=LocalBroadcastManager.getInstance(v.getContext());
                //向GroupDiscussionActivity类发送“确认发送当前语音消息”的广播
                Intent intent=new Intent("com.example.junior.SEND_VOICE_MESSAGE");
                localBroadcastManager.sendBroadcast(intent);
                refreshTask.cancel(true);
                //mHandler.removeCallbacks(mCounter); //取消计时任务
                dismiss();  //关闭当前对话框
            }
        });
        //“取消”按钮的点击事件
        btn_cancel_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                localBroadcastManager=LocalBroadcastManager.getInstance(v.getContext());
                //向GroupDiscussionActivity类发送“取消发送当前语音消息”的广播
                Intent intent=new Intent("com.example.junior.CANCEL_VOICE_MESSAGE");
                localBroadcastManager.sendBroadcast(intent);    //通过本地广播管理器发送本地广播
                refreshTask.cancel(true);
                //mHandler.removeCallbacks(mCounter); //取消计时任务
                dismiss();  //关闭当前对话框
            }
        });
        //mHandler.post(mCounter);    //立即开始计时任务
    }

    //当录制时长超过100s时，调用ViewChange方法改变当前对话框的文本
    private void ViewChange(){

    }

//    private Handler mHandler = new Handler();  //声明一个处理器对象
//    private Runnable mCounter=new Runnable() {  //定义一个计数任务
//        @Override
//        public void run() {
//            //应先将int类型的timerCount变量转为String类型再setText()
//            String string=timerCount+"s";
//            dynamic_length.setText(string);
//            timerCount++;
//            mHandler.postDelayed(this,1000);    //每隔一秒计时变量+1
//        }
//    };

    class DialogRefreshTask extends AsyncTask<Void,Integer,Boolean>{

        private int progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            current_length.setText("0:00");     //初始化当前文字进度
            progressBar.setProgress(0);     //初始化进度条进度
        }

        @Override
        protected Boolean doInBackground(Void... params) {      //在子线程中执行该方法，相当于在后台进行计时任务
            progress=0;   //记录当前进度条的进度
            try {
                while (progress<=100) {      //该处也可写成for循环的形式
                    Thread.sleep(1000);
                    progress += 1;      //ProgressBar的长度每秒+1（因为最大的录音时长为100s）
                    publishProgress(progress);      //需要更新UI，反馈当前的计时进度
                    if (isCancelled()) {
                        Log.d(TAG, "当前计时任务被手动取消");
                        break;      //当用户点击“发送”或“取消”按钮后，跳出刷新UI循环
                    }
                }
                if (progress==100){
                    Log.d(TAG, "当前录音已经达到了100s，自动取消");
                }
            } catch (Exception e){
                return false;       //若Dialog在更新途中出现异常，需要捕获并且返回false
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {    //更新当前录音进度
            super.onProgressUpdate(values);
            StringBuilder builder=new StringBuilder();
            int minutes=values[0]/60;
            int seconds=values[0]%60;
            if (seconds<10){
                builder.append(minutes+":0"+seconds);
            }
            else {
                builder.append(minutes+":"+seconds);
            }
            current_length.setText(builder.toString());
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {      //后台任务执行完毕的收尾工作
            super.onPostExecute(result);
            progress=0;
        }

    }
}

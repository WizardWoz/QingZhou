package com.example.qingzhou.discussion;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioEncoder;
import android.media.MediaRecorder.AudioSource;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.media.MediaRecorder.OutputFormat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

public class AudioRecorder implements OnErrorListener, OnInfoListener{
    private static final String TAG = "AudioRecorder";
    private int recordlength;   //记录录音时长
    private Context mContext; // 声明一个上下文对象
    private MediaRecorder mMediaRecorder; // 声明一个媒体录制器对象
    private MediaPlayer mediaPlayer;    //声明一个媒体播放器对象
    private String mRecordFilePath; // 录制文件的保存路径

    // 开始录制
    public void start(String RecordFilePath) {
        mRecordFilePath=RecordFilePath;     //保存至GroupDiscussion传过来的路径
        //初始化录制操作
        mMediaRecorder = new MediaRecorder(); // 创建一个媒体录制器
        mMediaRecorder.setOnErrorListener(this); // 设置媒体录制器的错误监听器
        mMediaRecorder.setOnInfoListener(this); // 设置媒体录制器的信息监听器
        mMediaRecorder.setAudioSource(AudioSource.MIC); // 设置音频源为麦克风
        mMediaRecorder.setOutputFormat(OutputFormat.AMR_NB); // 设置媒体的输出格式
        mMediaRecorder.setAudioEncoder(AudioEncoder.AMR_NB); // 设置媒体的音频编码器
        //mMediaRecorder.setAudioSamplingRate(8); // 设置媒体的音频采样率。可选
        // mMediaRecorder.setAudioChannels(2); // 设置媒体的音频声道数。可选
        mMediaRecorder.setAudioEncodingBitRate(4096); // 设置音频每秒录制的字节数。可选
        mMediaRecorder.setMaxDuration(100 * 1000); // 设置媒体的最大录制时长（目前为100s）
        // mMediaRecorder.setMaxFileSize(1024*1024*10); // 设置媒体的最大文件大小
        // setMaxFileSize与setMaxDuration设置其一即可
        mMediaRecorder.setOutputFile(mRecordFilePath); // 设置媒体文件的保存路径
        try {
            mMediaRecorder.prepare(); // 媒体录制器准备就绪
            mMediaRecorder.start(); // 媒体录制器开始录制
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 停止录制
    public void stop() {
        //当录制完成监听器不为空时，调用onRecordFinish()方法
        if (mOnRecordFinishListener != null) {
            mOnRecordFinishListener.onRecordFinish();
        }

        if (mMediaRecorder != null) {
            mMediaRecorder.setOnErrorListener(null); // 错误监听器置空
            mMediaRecorder.setPreviewDisplay(null); // 预览界面置空
            try {
                mMediaRecorder.stop(); // 媒体录制器停止录制
            } catch (Exception e) {
                e.printStackTrace();
            }
            mMediaRecorder.release(); // 媒体录制器释放资源
            mMediaRecorder = null;
        }
    }

    //获取当前录音文件时长操作
    public int getRecordlength() {
        mediaPlayer=new MediaPlayer();  //通过MediaPlayer对象来获取
        Log.d(TAG, "当前录音的文件路径为："+mRecordFilePath);
        try {
            mediaPlayer.setDataSource(mRecordFilePath);  //获取当前录音的路径
            mediaPlayer.prepare();
            recordlength=mediaPlayer.getDuration();  //获取录音时长
            Log.d(TAG, "录音时长为："+recordlength);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return recordlength;
    }

    private OnRecordFinishListener mOnRecordFinishListener; // 声明一个录制完成监听器对象
    // 定义一个录制完成监听器接口
    public interface OnRecordFinishListener {
        void onRecordFinish();
    }

    // 设置录制完成监听器
    public void setOnRecordFinishListener(OnRecordFinishListener listener) {
        mOnRecordFinishListener = listener;
    }

    // 在录制发生错误时触发（发生异常/未知错误）
    public void onError(MediaRecorder mr, int what, int extra) {
        if (mr != null) {
            mr.reset(); // 重置媒体录制器
        }
    }

    //在录制遇到状况时触发（达到录制时长/大小）
    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {

    }
}

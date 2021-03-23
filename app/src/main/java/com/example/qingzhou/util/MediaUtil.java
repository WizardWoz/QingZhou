package com.example.qingzhou.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class MediaUtil {
    private final static String TAG = "MediaUtil";

    // 获得音视频文件的存储路径
    public static String getRecordFilePath(Context context, String dir_name, String extend_name) {
        String path = "";
        //通过context调用存储路径:内部存储/Android/data/com.example.junior/files/Download/dir_name
        //从GroupDiscussionActivity传过来的dir_name为AudioRecord
        File recordDir = new File(context.getExternalFilesDir(Environment
                .DIRECTORY_DOWNLOADS).toString() + "/" + dir_name + "/");
        if (!recordDir.exists()) {  //若当前的文件路径未存在，则先创建
            recordDir.mkdirs();
        }
        try {   //若当前文件路径已存在
            //在recordDir路径中创建临时文件
            //生成后的临时文件名字将会是:内部存储/Android/data/com.example.junior/files/Download/dir_name
            //第1个参数为临时文件前缀，第2个参数为临时文件后缀，第3个参数为文件保存路径
            File recordFile = File.createTempFile(DateUtil.getNowDateTime(),extend_name,recordDir);
            path = recordFile.getAbsolutePath();    //获取当前录音文件的绝对路径
            Log.d(TAG, "dir_name=" + dir_name + ", extend_name=" + extend_name + ", path=" + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }

    //删除指定路径下的所有文件
    private static void deleteFiles(String path){
        File file=new File(path);
        //如果当前的路径为文件夹
        File[] files=file.listFiles();  //则创建文件数组存放当前路径下的所有文件
        for (int i=0;i<file.length();i++){  //递归删除文件数组中存储的所有文件（包括二级文件夹）
            deleteFiles(files[i].getAbsolutePath());
        }
    }
}

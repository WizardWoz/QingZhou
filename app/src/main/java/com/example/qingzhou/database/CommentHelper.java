package com.example.qingzhou.database;

import android.text.TextUtils;
import android.util.Log;

import com.example.qingzhou.bean.CommentInfo;
import com.example.qingzhou.util.StreamTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class CommentHelper {
    protected static final int ERROR = 2;
    protected static final int SUCCESS = -1;
    private static HttpURLConnection conn = null;
    private CommentInfo commentInfo = null;
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public static boolean insertComment(final CommentInfo commentInfo){
        Log.d("评论测试", ""+commentInfo.get_id()+","+commentInfo.getComment_text()+","+commentInfo.getPeople()+
                ","+commentInfo.getPlace());
        final boolean[] insertresult = new boolean[1];
        Thread insertThread;
        insertThread = new Thread() {
            @Override
            public void run() {
                try {
                    String path = "http://47.112.197.9/comment.php?type=insert";
                    URL url = new URL(path);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    JSONObject databody = new JSONObject();
                    databody.put("_id", commentInfo.get_id());
                    databody.put("username", commentInfo.getComment_user_name());
                    databody.put("place", commentInfo.getPlace());
                    databody.put("people", commentInfo.getPeople());
                    databody.put("text", commentInfo.getComment_text());
                    databody.put("time", commentInfo.getComment_time());

                    String data = String.valueOf(databody);
                    byte[] bytes = data.getBytes("UTF-8");
                    conn.getOutputStream().write(bytes);
                    int code = conn.getResponseCode();
                    Log.d("评论测试", "run: code="+code);
                    if (code == 200) {
                        //得到服务器返回的结果，这里应该是成功获失败
                        InputStream is = conn.getInputStream();
                        String result = StreamTools.readStream(is);
                        Log.d("评论测试", "insert: " + result);
                        if (result.equals("y")) {
                            insertresult[0] = true;
                        } else if (result.equals("n")) {
                            insertresult[0] = false;
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        insertThread.start();
        try {
            insertThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d("测试", "insertuser: "+insertresult[0]);
        return insertresult[0];
    }

    /**
     * 根据phone选择用户,传入phone
     * @param place
     * @param people
     * @return null
     */
    public static List<CommentInfo> queryByPlacePeople(final String place, final String people){
        final List<CommentInfo>[] list = new List[]{new ArrayList<CommentInfo>()};
        Thread loginThread;
        loginThread=new Thread(){
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try{
                    String path = "http://47.112.197.9/comment.php?type=query";
                    URL url = new URL(path);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    JSONObject databody = new JSONObject();
                    databody.put("place",place);
                    databody.put("people",people);
                    String data = String.valueOf(databody);
                    byte[] bytes =  data.getBytes("UTF-8");
                    conn.getOutputStream().write(bytes);
                    int code = conn.getResponseCode();
                    Log.d("获取评论", "run:"+code);
                    if(code == 200){
                        //得到服务器返回的结果，这里应该是成功获失败
                        InputStream is = conn.getInputStream();
                        String result = StreamTools.readStream(is);
                        //解析返回的Json结果
                        Log.d("获取评论", "run:"+result);
                        //存在记录，登录成功，返回整个user类
                        if(!result.equals("null")){
                            //解析Json数据
                            list[0] =analyzeJson(result);

                        }else{
                            Log.d("终极测试", "返回null");
                            list[0] = null;
                        }
                        is.close();
                    }else{
                        Log.d("终极测试", "返回null");
                        list[0] = null;
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }finally {
                    //关闭连接
                    conn.disconnect();
                }
            }
        };

        //线程开始，读取数据
        loginThread.start();
        try {
            //使用join方法，让线程执行完之后才返回数据
            loginThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return list[0];
    }

    /**
     * json解析
     * @param str
     */
    private static List<CommentInfo> analyzeJson(final String str){
        List<CommentInfo> list = new ArrayList<CommentInfo>();
        try{
            //带有{}用JSONObject
            //带有[]用JSONArray
            //JSONObject 用来解析带有{}的，去掉中括号，并指定键值，得到对应的JSON数组
            JSONObject jsonObjectALL = new JSONObject(str);
            String info = jsonObjectALL.optString("commentinfo",null);     //去掉一个中括号

            if(!TextUtils.isEmpty(info)){
                //JSON数组，用来保存JSONObject 根据键值得到的数组
                JSONArray jsonArray = new JSONArray(info);
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject tempJSON = (JSONObject) jsonArray.get(i);
                    CommentInfo tempComment = new CommentInfo();
                    tempComment.set_id(tempJSON.optString("_id"));
                    tempComment.setComment_user_name(tempJSON.optString("username"));
                    tempComment.setPlace(decode(tempJSON.optString("username")));
                    tempComment.setPeople(decode(tempJSON.optString("people")));
                    tempComment.setComment_text(tempJSON.optString("text"));
                    tempComment.setComment_time(tempJSON.optString("time"));
                    list.add(tempComment);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }


    /**
     * unicode解码
     * @param unicodeStr
     * @return
     */
    public static String decode(String unicodeStr) {
        if (unicodeStr == null) {
            return null;
        }
        StringBuffer retBuf = new StringBuffer();
        int maxLoop = unicodeStr.length();
        for (int i = 0; i < maxLoop; i++) {
            if (unicodeStr.charAt(i) == '\\') {
                if ((i < maxLoop - 5) && ((unicodeStr.charAt(i + 1) == 'u') || (unicodeStr.charAt(i + 1) == 'U')))
                    try {
                        retBuf.append((char) Integer.parseInt(unicodeStr.substring(i + 2, i + 6), 16));
                        i += 5;
                    } catch (NumberFormatException localNumberFormatException) {
                        retBuf.append(unicodeStr.charAt(i));
                    }
                else
                    retBuf.append(unicodeStr.charAt(i));
            } else {
                retBuf.append(unicodeStr.charAt(i));
            }
        }
        return retBuf.toString();
    }
}

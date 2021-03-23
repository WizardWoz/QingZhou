package com.example.qingzhou.database;

import android.text.TextUtils;
import android.util.Log;

import com.example.qingzhou.bean.UserInfo;
import com.example.qingzhou.util.StreamTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UserDBHelper2 {

    protected static final int ERROR = 2;
    protected static final int SUCCESS = -1;
    private HttpURLConnection conn = null;
    private UserInfo userInfo = null;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public boolean insertuser(final UserInfo user){
        final boolean[] insertresult = new boolean[1];
        Thread insertThread;
        insertThread = new Thread() {
            @Override
            public void run() {
                try {
                    String path = "http://47.112.197.9/insert.php";
                    URL url = new URL(path);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    JSONObject databody = new JSONObject();
                    databody.put("_id", user.getId());
                    databody.put("phone", user.getPhone());
                    databody.put("psw", user.getPassword());
                    databody.put("username", user.getUserName());
                    databody.put("update_time", simpleDateFormat.format(new Date(System.currentTimeMillis())));
                    databody.put("sex", user.getGender());
                    String data = String.valueOf(databody);
                    byte[] bytes = data.getBytes("UTF-8");
                    conn.getOutputStream().write(bytes);
                    int code = conn.getResponseCode();
                    if (code == 200) {
                        //得到服务器返回的结果，这里应该是成功获失败
                        InputStream is = conn.getInputStream();
                        String result = StreamTools.readStream(is);
                        Log.d("测试", "insert: " + result);
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
     * 更新用户数据，传入一个完整的user
     * @param user
     * @return boolean
     */
    public boolean updateuser(final UserInfo user){
        final boolean[] updateresult = new boolean[1];
        Thread updateThread;
        updateThread = new Thread(){
            @Override
            public void run() {
                try{
                    String path = "http://47.112.197.9/update.php";
                    URL url = new URL(path);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    JSONObject databody = new JSONObject();
                    databody.put("_id",user.getId());
                    databody.put("phone",user.getPhone());
                    databody.put("psw",user.getPassword());
                    databody.put("username",user.getUserName());
                    databody.put("update_time",simpleDateFormat.format(new Date(System.currentTimeMillis())));
                    databody.put("sex",user.getGender());
                    String data = String.valueOf(databody);
                    byte[] bytes =  data.getBytes("UTF-8");
                    conn.getOutputStream().write(bytes);
                    int code = conn.getResponseCode();
                    if(code == 200){
                        //得到服务器返回的结果，这里应该是成功获失败
                        InputStream is = conn.getInputStream();
                        String result = StreamTools.readStream(is);
                        Log.d("测试", "update: "+result);
                        if(result.equals("y")){
                            updateresult[0] = true;
                        }else if(result.equals("n")){
                            updateresult[0] = false;
                        }
                    }
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        updateThread.start();
        try {
            updateThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return updateresult[0];
    }


    /**
     * 根据phone选择用户,传入phone
     * @param phone
     * @return UserInfo
     */
    public UserInfo queryByPhone(final String phone){
        userInfo = null;
        Thread loginThread;
        loginThread=new Thread(){
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try{
                    String path = "http://47.112.197.9/query.php";
                    URL url = new URL(path);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    JSONObject databody = new JSONObject();
                    databody.put("phone",phone);
                    String data = String.valueOf(databody);
                    byte[] bytes =  data.getBytes("UTF-8");
                    conn.getOutputStream().write(bytes);
                    int code = conn.getResponseCode();
                    Log.d("测试登录", "run: "+code);
                    if(code == 200){
                        //得到服务器返回的结果，这里应该是成功获失败
                        InputStream is = conn.getInputStream();
                        String result = StreamTools.readStream(is);
                        //解析返回的Json结果
                        Log.d("测试", "run: "+result);
                        //存在记录，登录成功，返回整个user类
                        if(result!=null){
                            //解析Json数据
                            analyzeJson(result);
                        }else{
                            userInfo = null;
                        }
                        is.close();
                    }else{
                        userInfo = null;
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

        //因为线程原因这里还没分析完就已经返回了，可放到线程里分析
        return userInfo;
    }

    /**
     * json解析
     * @param str
     */
    private void analyzeJson(final String str){
        try{
            //带有{}用JSONObject
            //带有[]用JSONArray
            //JSONObject 用来解析带有{}的，去掉中括号，并指定键值，得到对应的JSON数组
            JSONObject jsonObjectALL = new JSONObject(str);
            String info = jsonObjectALL.optString("userinfo",null);     //去掉一个中括号
            if(!TextUtils.isEmpty(info)){
                //JSON数组，用来保存JSONObject 根据键值得到的数组
                JSONArray jsonArray = new JSONArray(info);
                //取出第一个元素作为新的JSONObject
                //因为根据数组可知第一个元素最外面用{}包起来的，相当于是一个JSONObject
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                //为userinfo赋值
                userInfo = new UserInfo();
                userInfo.setId(jsonObject.optLong("_id",0));
                userInfo.setPhone(jsonObject.optString("phone",null));
                userInfo.setPassword(jsonObject.optString("psw",null));
                userInfo.setUserName(decode(jsonObject.optString("username",null)));        //这里由于php传输过来的是unicode编码，所以要解码
                userInfo.setUpdate_Time(jsonObject.optString("update_time",null));
                userInfo.setGender(decode(jsonObject.optString("sex",null)));
                Log.d("测试", "analyzeJson: "+userInfo.getId()+" "
                                            +userInfo.getPhone()+" "
                                            +userInfo.getPassword()+" "
                                            +userInfo.getUserName()+" "
                                            +userInfo.getUpdate_Time());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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




package com.example.qingzhou.util;

import android.util.Log;

import com.example.qingzhou.util.StreamTools;
import com.hyphenate.chat.EMClient;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class HttpUtil {
    private static HttpURLConnection conn = null;          //用于创建聊天室用的http连接
    private static String org_name = "1127200416065957";
    private static String app_name = "qingzhou";
    //    private static String apiURL = "https://a1.easemob.com/{org_name}/{app_name}/token";
    private static String TokenUrlStr = "https://a1.easemob.com/"+org_name+"/"+app_name+"/token";           //获取token用的url
    private static String CreateRoomUrlStr = "https://a1.easemob.com/"+org_name+"/"+app_name+"/chatrooms";  //创建房间用的url
    private static String PreGetChatRoomInfo = "http://a1.easemob.com/"+org_name+"/"+app_name+"/chatrooms/";  //获取房间信息用的url,使用时后面要补上房间ID
    private static String SendTxtMsgUrl = "http://a1.easemob.com/"+org_name+"/"+app_name+"/messages";    //发送rest消息用

    private static String client_id = "YXA6UXQ2eSirSvS6NcJXwiD8bw";           //去环信控制台查看
    private static String client_secret = "YXA6kSxh0aAsctnJmhNRMqAI8wJW1ho";


    //通过向环信服务器发送请求来创建聊天室
    //首先要获取管理员的token,用于制作请求头
    //这里根据org_name和app_name获得请求URL

    /**
     *
     * @return Token字符串
     * @throws IOException
     * @throws JSONException
     */
    public static String GetToken() throws IOException, JSONException {
        String mtoken = null;

        URL url = new URL(TokenUrlStr);
        conn = (HttpURLConnection) url.openConnection();            //得到链接
        conn.setRequestMethod("POST");          //POST方式
        conn.setRequestProperty("Content-Type", "application/json");    //json格式
        conn.setDoOutput(true);
        JSONObject tokenParam = new JSONObject();        //用于请求token的Json数据
        tokenParam.put("grant_type","client_credentials");
        tokenParam.put("client_id", client_id);
        tokenParam.put("client_secret", client_secret);
        String data = String.valueOf(tokenParam);
        byte[] bytes = data.getBytes("UTF-8");
        conn.getOutputStream().write(bytes);
        int code = conn.getResponseCode();              //得到返回结果码
        Log.d("获取token", "code="+code);
        if(code == 200){
            //得到服务器返回的结果，这里应该是成功获失败
            InputStream is = conn.getInputStream();
            String result = StreamTools.readStream(is);
            JSONObject resultJson = new JSONObject(result);
            mtoken = resultJson.optString("access_token");
            Log.d("获取token", "response" + result);
            Log.d("获取token", "token: " + mtoken);
        }else{
            Log.d("获取token", "失败");
        }
        return mtoken;
    }

    /**
     *
     * @param topic     房间主题
     * @param num       房间人数
     * @return          如果成功返回创建房间的ID号，否则返回null
     * @throws IOException
     * @throws JSONException
     */
    public static String createChatRoom(String topic,int num){
        final String[] roomID = {null};
        Thread createThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String accessToken = GetToken();
                    URL url = new URL(CreateRoomUrlStr);
                    conn = (HttpURLConnection) url.openConnection();            //得到链接
                    conn.setRequestMethod("POST");          //POST方式
                    conn.setRequestProperty("Content-Type", "application/json");    //json格式
                    //利用token设置请求头
                    conn.setRequestProperty("Authorization", "Bearer "+accessToken);
                    conn.setDoOutput(true);
                    JSONObject tokenParam = new JSONObject();        //用于请求token的Json数据
                    tokenParam.put("name",topic);
                    tokenParam.put("description","just_test");
                    tokenParam.put("maxusers",num);
                    tokenParam.put("owner", EMClient.getInstance().getCurrentUser());
                    //    tokenParam.put("members","test1");            //可选项，这里选择没有成员添加
                    String data = String.valueOf(tokenParam);
                    byte[] bytes = data.getBytes("UTF-8");
                    conn.getOutputStream().write(bytes);
                    int code = conn.getResponseCode();              //得到返回结果码
                    Log.d("创建房间", "code="+code);
                    if(code==200){
                        //得到服务器返回的结果，这里应该是成功获失败
                        InputStream is = conn.getInputStream();
                        String result = StreamTools.readStream(is);
                        JSONObject resultJson = new JSONObject(result);
                        roomID[0] = new JSONObject(resultJson.optString("data")).optString("id");

                    }else{
                        Log.d("创建房间", "失败!");
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
        createThread.start();
        try {
            createThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return roomID[0];
    }

    /**
     *
     * @param chatRoomID 要获取信息的房间ID
     * @return 成功返回房间信息（包括请求状态等）的JSONObject,失败返回NULL
     * @throws IOException
     * @throws JSONException
     */
    public static JSONObject GetChatRoomInfo(String chatRoomID) throws IOException, JSONException {
        String accessToken = GetToken();
//        Log.d("dopamine", "GetChatRoomInfo: "+PreGetChatRoomInfo+chatRoomID);
        URL url = new URL(PreGetChatRoomInfo+chatRoomID);

        //    URL url = new URL(CreateRoomUrlStr);
        conn = (HttpURLConnection) url.openConnection();            //得到链接
        conn.setRequestMethod("GET");          //GET方式
        conn.setRequestProperty("Content-Type", "application/json");    //json格式
        //利用token设置请求头
        conn.setRequestProperty("Authorization", "Bearer "+accessToken);
        conn.setDoOutput(false);                //用GET方法，这里要改为false

        int code = conn.getResponseCode();              //得到返回结果码
//        Log.d("获取房间信息", "GetChatRoomInfo: " + code);
        JSONObject resultJson = null;
        if(code==200) {
            //得到服务器返回的结果，这里应该是成功获失败
            InputStream is = conn.getInputStream();
            String result = StreamTools.readStream(is);
            Log.d("获取房间信息", "成功!GetChatRoomInfo: "+result);
            resultJson = new JSONObject(result);
        }else{
            Log.d("获取房间信息","失败!");
        }
        return resultJson;
    }


    /**
     * 获得的对应是环信控制台中的ID
     * @param chatRoomID 房间号
     * @return 返回一个用户名称的List
     */
    public static List<String> GetUserName(String chatRoomID){
        List<String> userList = new ArrayList<String>();
        Thread getUserNameThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //总信息，包括请求的状态等
                    JSONObject roominfo_sum = GetChatRoomInfo(chatRoomID);
                    //获得房间信息，包括ID，成员等
                    JSONObject roominfo_data = roominfo_sum.getJSONArray("data").getJSONObject(0);
                    //获得成员json数组
                    JSONArray roomusers = roominfo_data.getJSONArray("affiliations");
                    Log.d("获取房间信息", "GetUserName: " + roomusers.toString());
                    //从成员Json数组中获取成员信息，添加到List
                    //先获取房主，他的键是独立的
                    //   userList.add(roominfo_data.getString("owner"));
                    //然后获取成员
                    for(int i=0; i<roomusers.length(); i++){
                        JSONObject temp = (JSONObject) roomusers.get(i);
                        Iterator<String> iterator = temp.keys();
                        while(iterator.hasNext()){
                            String key = iterator.next();
                            String member = temp.optString(key);
                            userList.add(member);
                        }

                    }
//                    for(int i=0;i<userList.size();i++){
//                        Log.d("获取房间成员", "GetUserName: " + userList.get(i));
//                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
        getUserNameThread.start();
        try {
            getUserNameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        Log.d("获取房间成员", "GetUserName: " + owner);
        return userList;
    }


    /**
     * @param chatRoomID
     * @return 房间名称
     * @throws IOException
     * @throws JSONException
     */
    public static String GetRoomName(String chatRoomID){
        final String[] roomName = {""};
        Thread getRoomThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //总信息，包括请求的状态等
                    JSONObject roominfo_sum = GetChatRoomInfo(chatRoomID);
                    //获得房间信息，包括ID，成员等
                    JSONObject roominfo_data = roominfo_sum.getJSONArray("data").getJSONObject(0);
                    //获取房间名称
                    roomName[0] = roominfo_data.optString("name");
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
        getRoomThread.start();
        try {
            getRoomThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return roomName[0];
    }

    /**
     * 非多线程
     * @param content  消息内容
     * @param fromID 谁发出的
     * @param toID 谁接受
     * @param attrName 消息的参数名
     * @param attrValue 消息的参数值
     * @return 发送结果
     */
    public synchronized static boolean sendTxtmsg(String content,String fromID,String toID,String attrName,int attrValue){
        final String[] sendresult = {null};
        Thread sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String accessToken = GetToken();            //得到token
                    URL url = new URL(SendTxtMsgUrl);
                    conn = (HttpURLConnection) url.openConnection();            //得到链接
                    conn.setRequestMethod("POST");          //POST方式
                    conn.setRequestProperty("Content-Type", "application/json");    //json格式
                    //利用token设置请求头
                    conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                    conn.setDoOutput(true);
                    JSONObject tokenParam = new JSONObject();        //用于请求token的Json数据
                    tokenParam.put("target_type", "chatrooms");
                    tokenParam.put("target", toID);
                    JSONObject msgJson = new JSONObject();
                    msgJson.put("type", "txt");
                    msgJson.put("msg", content);
                    tokenParam.put("msg", msgJson);
                    tokenParam.put("from", fromID);
                    JSONObject attrJson = new JSONObject();         //额外标记参数
                    attrJson.put(attrName,attrValue);
                    tokenParam.put("ext",attrJson);
                    Log.d("发送消息", "sendTxtmsg: "+tokenParam);
                    String data = String.valueOf(tokenParam);
                    byte[] bytes = data.getBytes("UTF-8");
                    conn.getOutputStream().write(bytes);
                    int code = conn.getResponseCode();              //得到返回结果码
                    Log.d("发送消息", "code=" + code);
                    if (code == 200) {
                        //得到服务器返回的结果，这里应该是成功获失败
                        InputStream is = conn.getInputStream();
                        String result = StreamTools.readStream(is);
                        Log.d("发送消息", "结果:"+result);
                        JSONObject resultJson = new JSONObject(result);         //得到服务器返回的结果
                        JSONObject tempjson = new JSONObject(resultJson.optString("data"));     //取出里面的结果值
                        Iterator<String> iterator = tempjson.keys();
                        if(iterator.hasNext()){
                            String key = iterator.next();
                            sendresult[0] = tempjson.optString(key);
                        }
                        Log.d("发送消息", "结果:"+ sendresult[0]);
                    } else {
                        Log.d("发送消息", "失败!");
                    }
                    assert sendresult[0] != null;
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        sendThread.start();
        try {
            sendThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return sendresult[0].equals("success");
    }


}

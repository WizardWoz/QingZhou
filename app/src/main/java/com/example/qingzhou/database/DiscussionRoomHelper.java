package com.example.qingzhou.database;

import android.text.TextUtils;
import android.util.Log;

import com.example.qingzhou.Constant;
import com.example.qingzhou.bean.DiscussionInfo;
import com.example.qingzhou.bean.UserInfo;
import com.example.qingzhou.util.StreamTools;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConference;
import com.hyphenate.chat.adapter.EMABase;

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

public class DiscussionRoomHelper {
    private static final String TAG = "DiscussionRoomHelper";

    protected static final int ERROR = 2;
    protected static final int SUCCESS = -1;
    private static HttpURLConnection conn = null;

    public synchronized static boolean insertRoomInfo(final DiscussionInfo discussionInfo){
        final boolean[] insertresult = new boolean[1];
        Thread insertThread;
        insertThread = new Thread(() -> {
            try {
                String path = "http://47.112.197.9/room.php?type=insert";
                URL url = new URL(path);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                JSONObject databody  = new JSONObject();
                databody.put("roomID",discussionInfo.getRoomID());
                databody.put("conferenceID",discussionInfo.getConfrID());
                databody.put("pwd",discussionInfo.getConfrPsd());
                databody.put("owner",discussionInfo.getOwnerName());
                databody.put("confrAudienceLimit",discussionInfo.getConfrAudienceLimit());
                databody.put("startTime",discussionInfo.getStartTime());
                Log.d("房间保存", "时间="+discussionInfo.getStartTime());
                databody.put("endTime","1970-01-01-23-59-59");
                databody.put("start",Constant.PREPARE_STAGE);                //默认未开始，阶段0
                String data = String.valueOf(databody);
                Log.d("房间保存", "data="+data);
                byte[] bytes = data.getBytes("UTF-8");
                conn.getOutputStream().write(bytes);
                int code = conn.getResponseCode();
                Log.d("房间保存", "run: code="+code);
                if (code == 200) {
                    //得到服务器返回的结果，这里应该是成功获失败
                    InputStream is = conn.getInputStream();
                    String result = StreamTools.readStream(is);
                    Log.d("房间保存", "insert: " + result);
                    if (result.equals("y")) {
                        insertresult[0] = true;
                    } else if (result.equals("n")) {
                        insertresult[0] = false;
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        });
        insertThread.start();
        try {
            insertThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d("插入房间", "insertRoomInfo: " + insertresult[0]);
        return insertresult[0];
    }

    /**
     * 更新数据库房间信息
     * 多线程
     * @param discussionInfo 房间信息
     */
    public static void updateRoom(final DiscussionInfo discussionInfo){
        final boolean[] updateresult = new boolean[1];
        Thread updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String path = "http://47.112.197.9/room.php?type=update";
                    URL url = new URL(path);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    JSONObject databody = new JSONObject();
                    databody.put("owner",discussionInfo.getOwnerName());
                    databody.put("endTime",discussionInfo.getEndTime());
                    databody.put("start",discussionInfo.getStart());
                    databody.put("roomID",discussionInfo.getRoomID());
                    String data = String.valueOf(databody);
                    byte[] bytes =  data.getBytes("UTF-8");
                    conn.getOutputStream().write(bytes);
                    int code = conn.getResponseCode();
                    Log.d("dopamine", "更新房间信息"+code);
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
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        updateThread.start();
    }

    /**
     * 多线程
     * 根据房间ID删除数据库对应记录
     * @param roomID 房间ID
     */
    public synchronized static void deleteByRoomID(final String roomID){
        Log.d(TAG, "进入房间删除");
        Thread deleteThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    String path = "http://47.112.197.9/room.php?type=delete";
                    URL url = new URL(path);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    JSONObject databody = new JSONObject();
                    databody.put("roomID",roomID);
                    String data = String.valueOf(databody);
                    byte[] bytes =  data.getBytes("UTF-8");
                    conn.getOutputStream().write(bytes);
                    int code = conn.getResponseCode();
                    Log.d("删除房间", "run:"+code);
                    if(code == 200){
                        //得到服务器返回的结果，这里应该是成功获失败
                        InputStream is = conn.getInputStream();
                        String result = StreamTools.readStream(is);
                        Log.d("删除房间", "update: "+result);
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
        deleteThread.start();
    }

    /**
     * 获取服务器数据库房间信息
     * 非多线程
     * @param roomID
     * @return
     */
    public synchronized static DiscussionInfo queryByRoomID(String roomID){
        final DiscussionInfo[] discussionInfo = {null};
        Thread QueryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String path = "http://47.112.197.9/room.php?type=query";
                    URL url = new URL(path);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    JSONObject databody = new JSONObject();
                    databody.put("roomID",roomID);
                    String data = String.valueOf(databody);
                    byte[] bytes =  data.getBytes("UTF-8");
                    conn.getOutputStream().write(bytes);
                    int code = conn.getResponseCode();
                    Log.d("获取数据库房间", "run:"+code);
                    if(code == 200){
                        //得到服务器返回的结果，这里应该是成功获失败
                        InputStream is = conn.getInputStream();
                        String result = StreamTools.readStream(is);
                        //解析返回的Json结果
                        Log.d("获取数据库房间", "run: "+result);
                        //存在记录，登录成功，返回整个user类
                        if(!result.equals("null")){
                            //解析Json数据
                            discussionInfo[0] = analyzeJson(result);
                        }else{
                            discussionInfo[0] = null;
                        }
                        is.close();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        QueryThread.start();
        try {
            QueryThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d("获取数据库房间", "return " + discussionInfo[0]);
        return discussionInfo[0];
    }

    /**
     * json解析
     * @param str
     */
    private static DiscussionInfo analyzeJson(final String str){
        final boolean[] getsign = {false};
        DiscussionInfo analyzeInfo = null;
        try{
            //带有{}用JSONObject
            //带有[]用JSONArray
            //JSONObject 用来解析带有{}的，去掉中括号，并指定键值，得到对应的JSON数组
            JSONObject jsonObjectALL = new JSONObject(str);
            String info = jsonObjectALL.optString("roominfo",null);     //去掉一个中括号
            if(!TextUtils.isEmpty(info)){
                //JSON数组，用来保存JSONObject 根据键值得到的数组
                JSONArray jsonArray = new JSONArray(info);
                //取出第一个元素作为新的JSONObject
                //因为根据数组可知第一个元素最外面用{}包起来的，相当于是一个JSONObject
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                //为analyzeInfo赋值
                String roomID = jsonObject.optString("roomID","");
                String conferenceID = jsonObject.optString("conferenceID","");
                String pwd = jsonObject.optString("pwd","");
                int start = jsonObject.optInt("start", Constant.PREPARE_STAGE);
                EMChatRoom emChatRoom = EMClient.getInstance().chatroomManager().getChatRoom(roomID);
                final EMConference[] conference = {null};
                Log.d(TAG, "获取会议"+conferenceID+","+pwd);
                EMClient.getInstance().conferenceManager().getConferenceInfo(conferenceID, pwd, new EMValueCallBack<EMConference>() {
                    @Override
                    public void onSuccess(EMConference emConference) {
                        Log.d(TAG, "onSuccess: 获取会议成功");
                        conference[0] = emConference;
                        getsign[0] = true;
                    }

                    @Override
                    public void onError(int i, String s) {
                        getsign[0] = true;
                        Log.d(TAG, "onError: 获取会议失败"+i+s);
                    }
                });
                while(conference[0]==null){                             //这里由于环信多线程，只能这么改
                    Thread.sleep(10);
                }
                //解析数据赋值
                analyzeInfo = new DiscussionInfo();
                analyzeInfo.setRoomID(roomID);
                analyzeInfo.setConfrID(conferenceID);
                analyzeInfo.setConfrPsd(pwd);
                analyzeInfo.setChatRoom(emChatRoom);
                analyzeInfo.setRoomName(emChatRoom.getName());
                analyzeInfo.setOwnerName(emChatRoom.getOwner());
                analyzeInfo.setStartTime(jsonObject.optString("startTime",""));
                analyzeInfo.setEndTime(jsonObject.optString("endTime",""));
                analyzeInfo.setConfrAudienceLimit(jsonObject.optInt("confrAudienceLimit",400));
                analyzeInfo.setStart(start);                            //哪个阶段
                analyzeInfo.setConference(conference[0]);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return analyzeInfo;
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

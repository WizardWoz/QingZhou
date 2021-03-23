package com.example.qingzhou.discussion;

import android.util.Log;

import com.example.qingzhou.Constant;
import com.example.qingzhou.bean.DiscussionInfo;
import com.example.qingzhou.database.DiscussionRoomHelper;
import com.example.qingzhou.util.DateUtil;
import com.example.qingzhou.util.HttpUtil;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConference;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class discussProcess {
    private static final String TAG = "discussProcess";

    private DiscussionInfo roomInfo;           //房间信息，用于发送信息用
    private String roomID;                      //房间号
    private String ownerID;                     //房主ID
    private List<String> memberlist;            //成员列表
    private Map<String,Boolean> speakmap = new HashMap<>();            //开麦权限Map
    private long startTime;                     //讨论开始时间，1970-1-1-到现在的时间,单位毫秒
    private long nowTime;                       //现在的时间，1970-1-1-到现在的时间，单位毫秒
    private long passedTime;                     //已经经过的时间
    private int stage;                           //第几阶段

    /**
     * 构造方法
     * @param RoomID 房间号
     */
    public discussProcess(String RoomID){
        roomInfo = DiscussionRoomHelper.queryByRoomID(RoomID);
        startTime = DateUtil.getDate(roomInfo.getStartTime()).getTime();
        roomID = roomInfo.getRoomID();
        ownerID = roomInfo.getChatRoom().getOwner();
        memberlist = HttpUtil.GetUserName(roomID);            //得到成员列表
        stage = roomInfo.getStart();
        //初始化开麦权限
        for(String userID:memberlist){
            speakmap.put(userID,true);                      //一开始设置全部可以开麦
        }
        Log.d(TAG, "discussProcess: "+startTime+","+roomID+","+ownerID+","+memberlist.size());
//
//        String[] test = roomInfo.getConference().getAdmins();
//        for(String x:test){
//            Log.d(TAG, "discussProcess: "+x);
//        }
    }

    /**
     * 讨论流程开始方法
     * 思路：
     * 先在服务器中得到讨论开始时间，然后再得到现在的时间，
     * 两个时间只差就得到了会议已经经过的时间，根据这个经过时间判断已经到达了哪个环节
     * 由于这是个线程，会不断执行，并且每次提示信息只由一个人发出和不能重复发出
     * 因此采取了stage这个变量来记录当前的会议阶段，当会议经过时间吻合那个阶段的时间并且刚好stage变量吻合，则由房主发出信息，并且调整stage
     * 到下一个换届，避免一直重复发送提示信息
     */
    public void start(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    //刷新成员列表
                    memberlist = HttpUtil.GetUserName(roomID);
                    if(memberlist.size() == 0){                 //如果成员列表没人，则表示房间人全走了，整个流程摧毁
                        break;
                    }
                    //获取当前时间，并减去开始时间得到对应阶段
                    nowTime = System.currentTimeMillis();           //当前时间
                    passedTime = nowTime/1000 - startTime/1000;               //经过时间，除以1000得到秒
                    roomInfo = DiscussionRoomHelper.queryByRoomID(roomID);      //得到服务器数据库的房间信息
//                    stage = roomInfo.getStart();                                //得到服务器记录的阶段
                    if(roomInfo == null){                                       //如果已经被删除房间了，则说明解散，流程中止
                        break;
                    }


//                    Log.d(TAG, "discussProcess2: "+startTime+","+roomID+","+ownerID+","+memberlist.size());
                    Log.d(TAG, "time: "+passedTime);
                    //分析是哪个阶段，并由列表的第一个人发送提示信息

                     //第一步，根据会议经过的时间判断，是否需要发信息
                    //第一，准备阶段，时间大于起点，小于准备阶段时长
                    if(passedTime>=Constant.PREPARE_STAGE_TIME && passedTime < Constant.SEQUENTIAL_STAGE_TIME && stage == Constant.PREPARE_STAGE){    //准备阶段
                        Log.d(TAG, "run: "+"准备阶段");
                        //发送信息到聊天室,只由列表中的第一个人发出
                        if(EMClient.getInstance().getCurrentUser().equals(memberlist.get(0))){
                            Log.d(TAG, "roominfo:"+roomInfo.getStart());
                            roomInfo.setStart(stage);
                            DiscussionRoomHelper.updateRoom(roomInfo);          //修改服务器信息
                            stage = Constant.SEQUENTIAL_STAGE;
//                            roomInfo.setStart(stage);

                            HttpUtil.sendTxtmsg("准备阶段\n请各位做好讨论开始的准备",null,roomID,Constant.MSG_TYPE,Constant.DISCUSS_MSG);
                        }
                    }   //第二，按顺序发言阶段，时间大于准备时长，小于人数乘以人均发言时长
                    else if(passedTime >= Constant.SEQUENTIAL_STAGE_TIME && passedTime < Constant.SEQUENTIAL_STAGE_TIME+Constant.PRE_SPEAK_TIME*memberlist.size() && stage == Constant.SEQUENTIAL_STAGE){    //第一阶段
                        Log.d(TAG, "run: "+"阶段1");
                        //发送信息到聊天室
                        if(EMClient.getInstance().getCurrentUser().equals(memberlist.get(0))) {
                            roomInfo.setStart(stage);
                            DiscussionRoomHelper.updateRoom(roomInfo);          //修改服务器信息
                            stage = Constant.FREETALK_STAGE;
//                            roomInfo.setStart(stage);
                            HttpUtil.sendTxtmsg("第一阶段，按顺序发表观点", null, roomID, Constant.MSG_TYPE, Constant.DISCUSS_MSG);
                        }
                    }
//                    else if(passedTime >= Constant.SEQUENTIAL_STAGE_TIME && passedTime < Constant.FREETALK_STAGE_TIME && stage == Constant.SEQUENTIAL_STAGE){    //第一阶段
//                        Log.d(TAG, "run: "+"阶段1");
//                        //发送信息到聊天室
//                        if(EMClient.getInstance().getCurrentUser().equals(memberlist.get(0))) {
//                            DiscussionRoomHelper.updateRoom(roomInfo);          //修改服务器信息
//                            stage = Constant.FREETALK_STAGE;
//                            roomInfo.setStart(stage);
//                            HttpUtil.sendTxtmsg("第一阶段，按顺序发表观点", null, roomID, Constant.MSG_TYPE, Constant.DISCUSS_MSG);
//                        }
//                    }
                    //第三阶段，自由发言阶段，时间大于第二阶段结束时间，小于讨论结束时间
                    else if(passedTime >= Constant.SEQUENTIAL_STAGE_TIME+Constant.PRE_SPEAK_TIME*memberlist.size() &&  passedTime < Constant.END_STAGE_TIME && stage == Constant.FREETALK_STAGE){         //第二阶段
                        Log.d(TAG, "run: "+"阶段2");
                        //发送信息到聊天室
                        if(EMClient.getInstance().getCurrentUser().equals(memberlist.get(0))){
                            roomInfo.setStart(stage);
                            DiscussionRoomHelper.updateRoom(roomInfo);          //修改服务器信息
                            stage = Constant.END_STAGE;
//                            roomInfo.setStart(stage);
                            HttpUtil.sendTxtmsg("第二阶段，自由发言",null,roomID,Constant.MSG_TYPE,Constant.DISCUSS_MSG);
                        }
                    } //第四阶段，时间到了，讨论结束
                    else if(passedTime >= Constant.END_STAGE_TIME && stage == Constant.END_STAGE){            //结束阶段
                        if(EMClient.getInstance().getCurrentUser().equals(memberlist.get(0))){
                            roomInfo.setStart(stage);
                            roomInfo.setEndTime( DateUtil.getNowDateTime());
                            DiscussionRoomHelper.updateRoom(roomInfo);          //修改服务器数据库信息
                            HttpUtil.sendTxtmsg("讨论结束！",null,roomID,Constant.MSG_TYPE,Constant.DISCUSS_MSG);
                        }
                        Log.d(TAG, "run: "+"阶段4结束");
                    }

                    //第二步，根据阶段开放相应权限，做响应的处理
                    //这里常量类用不了switch

                    roomInfo = DiscussionRoomHelper.queryByRoomID(roomID);      //得到最新
                    if(roomInfo == null){                                       //如果已经被删除房间了，则说明解散，流程中止
                        break;
                    }
                    int current_stage = roomInfo.getStart();  //首先获取当前阶段
                    Log.d(TAG, "current_stage="+current_stage);
                    if(current_stage == Constant.PREPARE_STAGE){                //阶段1，准备阶段，调试麦克风等
                        for(String userID:memberlist){
                            speakmap.put(userID,true);                  //全部人允许开麦
                        }
                    } else if(current_stage == Constant.SEQUENTIAL_STAGE){      //阶段2，按顺序发言阶段
                        if((passedTime - Constant.SEQUENTIAL_STAGE_TIME)%Constant.PRE_SPEAK_TIME == 10){
                            if(EMClient.getInstance().getCurrentUser().equals(memberlist.get(0))){
                                HttpUtil.sendTxtmsg("还剩10秒",null,roomID,Constant.MSG_TYPE,Constant.DISCUSS_MSG);
                            }
                        }
                        if((passedTime - Constant.SEQUENTIAL_STAGE_TIME)%Constant.PRE_SPEAK_TIME == 0){     //到达修改麦克风权限的时间点，就是每隔一个发言时间段就执行一次权限修改
                            for(String userID:memberlist){
                                speakmap.put(userID,false);                  //先关闭所有权限，再根据时间点开权限
                            }
                            //得到当前应该开放第几个说话，num即为memberlist的下标
                            int num = (int) ((passedTime - Constant.SEQUENTIAL_STAGE_TIME)/Constant.PRE_SPEAK_TIME);
                            Log.d(TAG, "index_num="+num);
                            if(num<memberlist.size()){
                                speakmap.put(memberlist.get(num),true);             //开放这个人的权限
                                if(EMClient.getInstance().getCurrentUser().equals(memberlist.get(0))){
                                    HttpUtil.sendTxtmsg(memberlist.get(num)+"讲话",null,roomID,Constant.MSG_TYPE,Constant.DISCUSS_MSG);
                                }
                            }

//                            try {
//                                Thread.sleep(Constant.PRE_SPEAK_TIME);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }


//                            if(passedTime == Constant.SEQUENTIAL_STAGE_TIME){     //如果第一个说话
//
//                            } else if(passedTime == (Constant.SEQUENTIAL_STAGE_TIME+Constant.PRE_SPEAK_TIME)){     //如果第二个说话
//
//                            } else if(passedTime == (Constant.SEQUENTIAL_STAGE_TIME+2*Constant.PRE_SPEAK_TIME)){     //如果第一个说话
//
//                            } else if(passedTime == (Constant.SEQUENTIAL_STAGE_TIME+3*Constant.PRE_SPEAK_TIME)){     //如果第一个说话
//
//                            }
                        }

                    } else if(current_stage == Constant.FREETALK_STAGE){
                        for(String userID:memberlist){
                            speakmap.put(userID,true);                  //全部人允许开麦
                        }
                    } else if(current_stage == Constant.END_STAGE){
                        try {
                            Thread.sleep(1000);

                            if(EMClient.getInstance().getCurrentUser().equals(memberlist.get(0))){
                            HttpUtil.sendTxtmsg("请离场",null,roomID,Constant.MSG_TYPE,Constant.DISCUSS_MSG);
                        }

                        for(String userID:memberlist){
                            speakmap.put(userID,false);                  //关闭所有麦克风
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                        break;          //结束线程
                    }
                }

                try {
                    Thread.sleep(500);               //线程休息1秒
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 得到是否可以讲话
     * @param userID
     * @return 对应ID是否讲话
     */
    public boolean getSpeak(String userID){
        if(speakmap.get(userID)!=null){
            return speakmap.get(userID);
        } else{
            return false;
        }
    }

    /**
     * 得到当前阶段
     * @return 当前阶段
     */
    public int getStage(){
        return roomInfo.getStart();
    }
}

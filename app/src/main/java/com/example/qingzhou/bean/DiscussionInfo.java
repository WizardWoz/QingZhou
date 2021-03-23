package com.example.qingzhou.bean;

import com.example.qingzhou.Constant;
import com.example.qingzhou.util.DateUtil;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConference;

import java.util.Date;

/**
 * 讨论房间信息，聊天室和会议的结合体
 */

public class DiscussionInfo {

    public static final int WAIT_DISCUSSION = 1;                //讨论还没开始
    public static final int START_DISCUSSION = 2;               //讨论正在进行
    public static final int END_DISCUSSION = 3;                 //讨论已经结束


    private EMChatRoom chatRoom;
    private EMConference conference;
    private String roomID;      //聊天室ID
    private String roomName;    //聊天室名
    private String ownerName;   //房主名字
    private String confrID;     //会议ID
    private String confrPsd; // 会议密码
    private int confrAudienceLimit; // 观众上限
    private int confrTalkerLimit; // 主播上限
    private boolean allowAudienceTalk; // 是否允许除房主外上麦发言
    private String startTime;           //开始时间
    private String endTime;             //结果时间
    private int start;                  //哪个阶段

    public DiscussionInfo(EMChatRoom emChatRoom,EMConference emConference){
        chatRoom = emChatRoom;
        conference = emConference;
        roomID = emChatRoom.getId();
        roomName = emChatRoom.getName();
        ownerName = emChatRoom.getOwner();
        confrID = emConference.getConferenceId();
        confrPsd = emConference.getPassword();
        confrAudienceLimit = emChatRoom.getMaxUsers();
        confrTalkerLimit = emConference.getMemberNum();
        startTime = DateUtil.getNowDateTime();
        start = Constant.PREPARE_STAGE;                                      //默认未开始
    }

    public DiscussionInfo() {

    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public EMChatRoom getChatRoom() {
        return chatRoom;
    }

    public void setChatRoom(EMChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    public EMConference getConference() {
        return conference;
    }

    public void setConference(EMConference conference) {
        this.conference = conference;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getConfrID() {
        return confrID;
    }

    public void setConfrID(String confrID) {
        this.confrID = confrID;
    }

    public String getConfrPsd() {
        return confrPsd;
    }

    public void setConfrPsd(String confrPsd) {
        this.confrPsd = confrPsd;
    }

    public int getConfrAudienceLimit() {
        return confrAudienceLimit;
    }

    public void setConfrAudienceLimit(int confrAudienceLimit) {
        this.confrAudienceLimit = confrAudienceLimit;
    }

    public int getConfrTalkerLimit() {
        return confrTalkerLimit;
    }

    public void setConfrTalkerLimit(int confrTalkerLimit) {
        this.confrTalkerLimit = confrTalkerLimit;
    }

    public boolean isAllowAudienceTalk() {
        return allowAudienceTalk;
    }

    public void setAllowAudienceTalk(boolean allowAudienceTalk) {
        this.allowAudienceTalk = allowAudienceTalk;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }
}

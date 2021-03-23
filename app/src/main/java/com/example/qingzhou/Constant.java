package com.example.qingzhou;

public class Constant {

    //聊天室消息额外属性类型
    public static String MSG_TYPE = "type";
    public static int WAIT_MSG = 10;            //等候室专属
    public static int DISCUSS_MSG = 11;         //讨论吧专属
    public static int BACKSTAGE_STREAM = 12;           //后台消息
    //讨论流程阶段
    public static int PREPARE_STAGE = 1;
    public static int SEQUENTIAL_STAGE = 2;
    public static int FREETALK_STAGE = 3;
    public static int END_STAGE = 4;
    //讨论流程的时间点
    /**
     * 第一阶段 0~10
     * 第二阶段 10~20*memberlist.size()
     * 第三阶段 20*memberlist.size()~150
     * 第四阶段 150
     */
    public static long PREPARE_STAGE_TIME = 0;
    public static long SEQUENTIAL_STAGE_TIME = 10;         //第一阶段按顺 序发言阶段开始时间
    public static long FREETALK_STAGE_TIME = 20;           //第二阶段自由发言阶段开始时间
    public static long END_STAGE_TIME = 100;                //结束时间
    public static long PRE_SPEAK_TIME = 20;                 //按顺序讨论环节每个人可以说话的时间

}

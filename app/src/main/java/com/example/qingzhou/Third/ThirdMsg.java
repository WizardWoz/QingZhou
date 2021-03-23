package com.example.qingzhou.Third;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

public class ThirdMsg {
        public static final int TYPE_RECEIVED = 0;
        public static final int TYPE_SENT = 1;
        //这里的按钮应该分开为一个独立的信息类
        public static final int TYPE_CHOOSE = 2;

        @NotNull
        private Date time;
        @NotNull
        private String content;
        @NotNull
        private int type;
        @NotNull
        private List<String> btnList;
        @NotNull
        private String title;
        @NotNull
        private String describe;

        //构造类型1，普通对话
        public ThirdMsg(@NotNull String content, int type) {
            this.content = content;
            this.type = type;
        }

        //构造类型2，按钮对话
        public ThirdMsg(@NotNull List<String> btnList,String title,String describe,int type){
            this.btnList = btnList;
            this.title = title;
            this.describe = describe;
            this.type = type;
        }

        //构造类型3，无参数
        public ThirdMsg() {
        }

        /**
         * 得到当前按钮对象的下一句应该是什么
         * @return 下一句String
         */
        public String getNextText(){
            return null;
        }


        @NotNull
        public Date getTime() {
            return time;
        }

        public void setTime(@NotNull Date time) {
            this.time = time;
        }

        @NotNull
        public String getContent() {
            return content;
        }

        public void setContent(@NotNull String content) {
            this.content = content;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        @NotNull
        public List<String> getBtnList() {
            return btnList;
        }

        public void setBtnList(@NotNull List<String> btnList) {
            this.btnList = btnList;
        }

        @NotNull
        public String getTitle() {
            return title;
        }

        public void setTitle(@NotNull String title) {
            this.title = title;
        }

        @NotNull
        public String getDescribe() {
            return describe;
        }

        public void setDescribe(@NotNull String describe) {
            this.describe = describe;
        }

        @Override
        public String toString() {
            return "Msg{" +
                    ", content='" + content + '\'' +
                    ", type=" + type + '\'' +
                    '}';
        }


}

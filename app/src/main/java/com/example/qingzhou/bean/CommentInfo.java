package com.example.qingzhou.bean;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CommentInfo {
    private String _id;
    private String comment_user_name;
    private String place;
    private String people;
    private String comment_time;
    private String comment_text;

    public CommentInfo(){
        _id = null;
        comment_user_name = null;
        place = null;
        people = null;
        comment_time = null;
        comment_text = null;
    }

    public CommentInfo(String myid,String mycomment_user_name,String myplace,String mypeople,String mycomment_time,String mycomment_text){
        _id = myid;
        comment_user_name = mycomment_user_name;
        place = myplace;
        people = mypeople;
        comment_time = mycomment_time;
        comment_text = mycomment_text;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public void setComment_user_name(String comment_user_name) {
        this.comment_user_name = comment_user_name;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public void setPeople(String people) {
        this.people = people;
    }

    public void setComment_time(String comment_time) {
        this.comment_time = comment_time;
    }

    public void setComment_text(String comment_text) {
        this.comment_text = comment_text;
    }

    public String get_id() {
        return _id;
    }

    public String getComment_user_name() {
        return comment_user_name;
    }

    public String getPlace() {
        return place;
    }

    public String getPeople() {
        return people;
    }

    public String getComment_time() {
        return comment_time;
    }

    public String getComment_text() {
        return comment_text;
    }
}

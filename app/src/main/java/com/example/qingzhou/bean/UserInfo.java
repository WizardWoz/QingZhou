/*用户的注册信息类*/
package com.example.qingzhou.bean;

public class UserInfo {
    private long id;
    private String username;
    private String update_time;
    private String phone;
    private String password;
    private String gender;

    public UserInfo(long id,String username,String update_time,String phone,String password){
        this.id=id;
        this.username=username;
        this.update_time=update_time;
        this.phone=phone;
        this.password=password;
        gender="男";
    }

    public UserInfo(String phone,String username,String password){
        this.phone=phone;
        this.username=username;
        this.password=password;
    }


    public UserInfo() {

    }

    public long getId(){
        return id;
    }

    public void setId(long id){
        this.id=id;
    }

    public String getUserName(){
        return username;
    }

    public void setUserName(String username){
        this.username=username;
    }

    public String getUpdate_Time(){
        return update_time;
    }

    public void setUpdate_Time(String update_time){
        this.update_time=update_time;
    }

    public String getPhone(){
        return phone;
    }

    public void setPhone(String phone){
        this.phone=phone;
    }

    public String getPassword(){
        return password;
    }

    public void setPassword(String password){
        this.password=password;
    }

    public String getGender(){
        return gender;
    }

    public void setGender(String gender){
        this.gender=gender;
    }
}

package com.example.qingzhou.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.qingzhou.bean.UserInfo;
import com.example.qingzhou.util.DateUtil;

import java.util.ArrayList;

public class UserDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "UserDBHelper";
    private static final String DB_NAME="user.db";  //数据库名称
    private static final int DB_VERSION=1;  //数据库版本号
    private static UserDBHelper mHelper=null;   //数据库帮助器实例
    private SQLiteDatabase mDB=null;    //数据库实例
    public static final String TABLE_NAME="user_info";  //表的名称

    private UserDBHelper(Context context){
        super(context,DB_NAME,null,DB_VERSION);
    }

    private UserDBHelper(Context context,int version){
        super(context,DB_NAME,null,version);
    }

    //利用单例模式获取数据库帮助器的唯一实例
    public static UserDBHelper getInstance(Context context,int version){
        if(version>0&&mHelper==null){
            mHelper=new UserDBHelper(context,version);
        }
        else if(mHelper==null){
            mHelper=new UserDBHelper(context);
        }
        return mHelper;
    }

    //打开数据库的读连接
    public SQLiteDatabase openReadLink(){
        if(mDB==null||!mDB.isOpen()){
            mDB=mHelper.getReadableDatabase();
        }
        return mDB;
    }

    //打开数据库的写连接
    public SQLiteDatabase openWriteLink(){
        if(mDB==null||!mDB.isOpen()){
            mDB=mHelper.getWritableDatabase();
        }
        return mDB;
    }

    //关闭数据库连接
    public void closeLink(){
        if(mDB!=null&&mDB.isOpen()){
            mDB.close();
            mDB=null;
        }
    }

    //创建数据库，执行建表语句
    @Override
    public void onCreate(SQLiteDatabase db) {
        String drop_sql="DROP TABLE IF EXISTS "+TABLE_NAME+";";
        db.execSQL(drop_sql);
        String create_sql="CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"("
                +"_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                +"username VARCHAR NOT NULL," +"update_time VARCHAR NOT NULL,"
                +"phone VARCHAR,"+"password VARCHAR"+");";
        db.execSQL(create_sql);
    }

    //修改数据库，执行表结构变更语句
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    //根据指定条件删除表记录
    public int delete(String condition){
        //执行删除记录操作，该语句返回删除记录的数目
        return mDB.delete(TABLE_NAME,condition,null);
    }

    //添加新用户，即注册
    public long insertUserData(UserInfo userData) {
        String phoneNum=userData.getPhone().trim();
        String userName=userData.getUserName().trim();
        String userPwd=userData.getPassword().trim();
        String currentTime= DateUtil.getNowDateTime();
        ContentValues values = new ContentValues();
        values.put("phone",phoneNum);
        values.put("username",userName);
        values.put("password",userPwd);
        values.put("update_time",currentTime);
        return mDB.insert(TABLE_NAME,"",values);    //返回插入记录的行号
    }

    //根据条件更新指定的表记录，即登录时更新
    public int updateUserData(UserInfo info,String condition){
        ContentValues cv=new ContentValues();
        cv.put("phone",info.getPhone().trim());
        cv.put("password",info.getPassword().trim());
        cv.put("update_time",info.getUpdate_Time().trim());
        //执行更新记录操作，该语句返回记录更新的数目
        return mDB.update(TABLE_NAME,cv,condition,null);
    }

    //根据指定条件查询记录，并返回结果数据队列
    public ArrayList<UserInfo> query(String condition){
        String sql=String.format("select _id,username,update_time,"+
                "phone,password from %s where %s;",TABLE_NAME,condition);
        ArrayList<UserInfo> infoArray=new ArrayList<UserInfo>();    //新的数据队列
        //执行记录查询操作，该语句返回结果集的游标
        Cursor cursor=mDB.rawQuery(sql,null);
        //循环取出游标指向的每条记录
        while(cursor.moveToNext()){
            UserInfo info=new UserInfo();
            info.setId(cursor.getLong(0));
            info.setUserName(cursor.getString(1));
            info.setUpdate_Time(cursor.getString(2));
            info.setPhone(cursor.getString(3));
            info.setPassword(cursor.getString(4));
            infoArray.add(info);
        }
        cursor.close();     //查询完毕，关闭游标
        return infoArray;
    }

    //根据手机号码查询指定记录
    public UserInfo queryByPhone(String phone){
        UserInfo info=null;
        ArrayList<UserInfo> infoArray=query(String.format("phone='%s'",phone));
        if (infoArray.size()>0){
            info=infoArray.get(0);
        }
        return info;
    }
}

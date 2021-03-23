package com.example.qingzhou.Me;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.qingzhou.Base.BaseActivity;
import com.example.qingzhou.R;
import com.example.qingzhou.bean.UserInfo;
import com.example.qingzhou.database.UserDBHelper;
import com.example.qingzhou.database.UserDBHelper2;
import com.example.qingzhou.photoWindow.PhotoPopup;

import java.io.File;
import java.io.FileOutputStream;

public class Info_Change_Activity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "Info_Change_Activity";

    //常量
    private static final int REQUEST_IMAGE_GET = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_SMALL_IMAGE_CUTTING = 2;
    private static final int REQUEST_BIG_IMAGE_CUTTING = 3;
    private static final String IMAGE_FILE_MAME = "icon.jpg";

    private PhotoPopup photoPopup;              //声明一个相册选择对象
    private UserInfo info;                      //得到该用户的用户类
    private UserDBHelper mHelper;               //声明一个用户数据库帮助器对象
    private UserDBHelper2 mHelper2 = new UserDBHelper2();               //声明一个用户数据库帮助器对象
    private SharedPreferences mShared;          //声明一个共享参数对象  记录用户手机号

    private ImageView user_img_edit;
    private EditText user_name_edit;            //用户名编辑框
    private TextView user_gender;               //性别选择框
    private Button save_button;

    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_change);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //打开共享参数编辑器
        mShared=getSharedPreferences("share_login",MODE_PRIVATE);
        //获得登录用户的手机号，它的设置相关代码可以在LoginActivity中找到
        phone=mShared.getString("user_phone","");
        //打开数据库链接,并获得实例
        mHelper=UserDBHelper.getInstance(this,2);
        mHelper.openWriteLink();
        //根据登录用户手机号，从数据库中获得该用户类
        info = mHelper2.queryByPhone(phone);
    //    info=mHelper.queryByPhone(phone);
        iniData();
    }

    private void iniData(){
        //修改标题栏相关信息
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupBackAdUp("修改信息");
        user_name_edit=findViewById(R.id.user_name_edit);
        user_gender=findViewById(R.id.user_gender_edit);
        user_img_edit=findViewById(R.id.img_edit);
        save_button=findViewById(R.id.save_button);
        user_gender.setOnClickListener(this);                       //性别设置点击监听
        user_img_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photoPopup = new PhotoPopup(Info_Change_Activity.this, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //文件申请
                        //文件申请
                        if (ContextCompat.checkSelfPermission(Info_Change_Activity.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            //没有权限，进行申请操作
                            ActivityCompat.requestPermissions(Info_Change_Activity.this,
                                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
                        } else {
                            photoPopup.dismiss();
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivityForResult(intent, REQUEST_IMAGE_GET);
                            } else {
                                Toast.makeText(Info_Change_Activity.this, "未找到图片查看器", Toast.LENGTH_SHORT).show();
                            }
                        }


                        if (ContextCompat.checkSelfPermission(Info_Change_Activity.this,
                                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                                || ContextCompat.checkSelfPermission(Info_Change_Activity.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            //没有权限，进行申请操作
                            ActivityCompat.requestPermissions(Info_Change_Activity.this,
                                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 300);
                        } else {
                            photoPopup.dismiss();
                            imageCature();
                        }
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (ContextCompat.checkSelfPermission(Info_Change_Activity.this,
                                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                                || ContextCompat.checkSelfPermission(Info_Change_Activity.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            //没有权限，进行申请操作
                            ActivityCompat.requestPermissions(Info_Change_Activity.this,
                                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 300);
                        } else {
                            photoPopup.dismiss();
                            imageCature();
                        }
                    }
                });
                View rootView = LayoutInflater.from(Info_Change_Activity.this)
                        .inflate(R.layout.activity_info_change, null);
                photoPopup.showAtLocation(rootView, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
            }
        });                                                         //头像设置点击监听
        save_button.setOnClickListener(this);                       //保存按钮设置监听
        //初始化信息
        user_name_edit.setText(info.getUserName());
        user_gender.setText(info.getGender());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //昵称本身就是一个EditView，不用设置点击监听
            //点击性别弹出选项框
            case R.id.user_gender_edit:
                new AlertDialog.Builder(this)
                        .setTitle("性别")
                        .setSingleChoiceItems(new String[]{"男", "女"}, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case 0:
                                        user_gender.setText("男");
                                        info.setGender("男");
                                        break;
                                    case 1:
                                        user_gender.setText("女");
                                        info.setGender("女");
                                        break;
                                    default:
                                        break;
                                }
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("取消",null)
                        .show();
                break;
            //修改确定按钮
            case R.id.save_button:
                info.setUserName(user_name_edit.getText().toString());
                info.setGender(user_gender.getText().toString());
                mHelper2.updateuser(info);
            //    mHelper.updateUserData(info,info.getUpdate_Time());
                Toast.makeText(Info_Change_Activity.this,"保存成功",Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                //小图切割
                case REQUEST_SMALL_IMAGE_CUTTING:
                    if (data != null) {
                        setPicToView(data);
                    }
                    break;

                //相册选取
                case REQUEST_IMAGE_GET:
                    try {
                        startSmallPhotoZoom(data.getData());
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    break;

                //拍照
                case REQUEST_IMAGE_CAPTURE:
                    File temp = new File(Environment.getExternalStorageDirectory() + "/" + IMAGE_FILE_MAME);
                    startSmallPhotoZoom(Uri.fromFile(temp));
                    break;
            }

        }
    }

    /**
     *  小图切割
     */
    public void startSmallPhotoZoom(Uri uri){
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri,"image/*");
        intent.putExtra("crop","true");
        intent.putExtra("aspectX","1");
        intent.putExtra("aspectY","1");
        intent.putExtra("outputX","300");
        intent.putExtra("outputY","300");
        intent.putExtra("scale","true");
        intent.putExtra("return-data",true);
        startActivityForResult(intent,REQUEST_SMALL_IMAGE_CUTTING);
    }

    /**
     * 小图模式中，保存图片后，设置到视图中
     */
    private void setPicToView(Intent data){
        Bundle extras = data.getExtras();
        if (extras != null){
            Bitmap photo = extras.getParcelable("data");
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                String storage = Environment.getExternalStorageDirectory().getPath();
                File dirFile = new File(storage + "/smallIcon");
                if (!dirFile.exists()){
                    if (!dirFile.mkdirs()){
                        Log.e("TAG","文件夹创新失败");
                    }else{
                        Log.e("TAG","文件创建成功");
                    }
                }
                File file = new File(dirFile,System.currentTimeMillis() + ".jpg");
                FileOutputStream outputStream = null;
                try{
                    outputStream = new FileOutputStream(file);
                    photo.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
                    outputStream.flush();
                    outputStream.close();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            user_img_edit.setImageBitmap(photo);
        }
    }

    /**
     * 大图剪切
     */
    public void startBigPhotoZoom(Uri uri){
        Uri imageUri = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            String storage = Environment.getExternalStorageDirectory().getPath();
            File dirFile = new File(storage + "/bigIcon");
            if (!dirFile.exists()){
                if (!dirFile.mkdirs()){
                    Log.e("TAG","文件夹创建失败");
                }else{
                    Log.e("TAG","文件夹创建成功");
                }
            }
            File file = new File(dirFile,System.currentTimeMillis() + ".jpg");
            imageUri = Uri.fromFile(file);
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri,"image/*");
        intent.putExtra("crop","true");
        intent.putExtra("aspectX","1");
        intent.putExtra("aspectY","1");
        intent.putExtra("outputX","600");
        intent.putExtra("outputY","600");
        intent.putExtra("return-data","false");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        startActivityForResult(intent,REQUEST_BIG_IMAGE_CUTTING);
    }

    /**
     * 判断系统及拍照操作
     */
    private void imageCature(){
        Intent intent;
        Uri pictureUri;
        File pictureFile = new File(Environment.getExternalStorageDirectory(),IMAGE_FILE_MAME);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pictureUri = FileProvider.getUriForFile(this,
                    "com.example.junior.fileProvider",pictureFile);
        }else{
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            pictureUri = Uri.fromFile(pictureFile);
        }
        // 开始切割
        Intent intent1 = new Intent("com.android.camera.action.CROP");
        intent1.setDataAndType(FileProvider.getUriForFile(this,
                "com.example.junior.fileProvider", pictureFile), "image/*");
        intent1.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        // ......
        intent1.putExtra("return-data", false); // 不直接返回数据
        intent1.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri); // 返回一个文件
        intent1.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        startActivityForResult(intent1, REQUEST_BIG_IMAGE_CUTTING);
    }

    /**
     * 处理回调结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 200:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    photoPopup.dismiss();
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, REQUEST_IMAGE_GET);
                    } else {
                        Toast.makeText(Info_Change_Activity.this, "未找到图片查看器", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    photoPopup.dismiss();
                }
                break;
            case 300:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    photoPopup.dismiss();
                    imageCature();
                } else {
                    photoPopup.dismiss();
                }
                break;
        }
    }
}

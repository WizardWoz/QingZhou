package com.example.qingzhou.Login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qingzhou.R;
import com.example.qingzhou.activity.LaunchSimpleActivity;
import com.example.qingzhou.activity.TabHostActivity;
import com.example.qingzhou.bean.UserInfo;
import com.example.qingzhou.database.UserDBHelper;
import com.example.qingzhou.database.UserDBHelper2;
import com.example.qingzhou.util.DateUtil;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;


public class MainFrameActivity extends AppCompatActivity implements View.OnClickListener,View.OnFocusChangeListener {
    private static final String TAG = "MainFrameActivity";
    private String temp="";                        //暂存字符串,暂时保存输入框的内容
    private Button login_click;
    private Button regist_click;

    private Button get_verifycode;  //获取验证码按钮
    private RadioGroup rg_login; // 声明一个单选组对象
    private RadioButton rb_password; // 声明一个单选按钮对象
    private RadioButton rb_verifycode; // 声明一个单选按钮对象
    private EditText et_phone; // 声明一个编辑框对象
    private EditText et_password; // 声明一个编辑框对象
    private Button btn_forget; // 声明一个按钮控件对象
    private CheckBox ck_remember; // 声明一个复选框对象
    private UserDBHelper mHelper;   //声明一个用户数据库帮助器对象
    private UserDBHelper2 mHelper2 = new UserDBHelper2();   //声明一个用户数据库帮助器对象
    private SharedPreferences mShared;  //声明一个共享参数对象（用于记录用户是否第一次使用该APP） 记录用户手机号
    private SharedPreferences.Editor editor;    //共享参数编辑器对象
    private Display display=null;

    private int mRequestCode = 0; // 跳转页面时的请求代码
    private boolean bRemember = false; // 是否记住密码，默认为不记住
    private boolean isFirstLogin=true;  //是否为第一次成功登录该APP，默认为是
    private String mPassword="111111";  //默认密码是111111
    private String mVerifyCode="111111"; // 验证码
    private String last_login_phone;        //最后一个登录的用户的手机号码

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainframe);
        //调用findViewSetListener()设置按钮控件
        findViewSetListener();
        display=getWindowManager().getDefaultDisplay();
        //初始化参数函数
        initData();

    }

    private void initData(){
        mHelper=UserDBHelper.getInstance(this,2);                   //获得用户数据库帮助器的一个实例
        mHelper.openWriteLink();                                                    //恢复页面，则打开数据库连接*/
        mShared=getSharedPreferences("share_login",MODE_PRIVATE);            //从share_login.xml中获取共享参数对象
        editor=mShared.edit();                                                      //获取共享参数中保存的登录成功信息
        isFirstLogin=mShared.getBoolean("first_login",true);        //获取共享参数中是否保存密码
        bRemember=mShared.getBoolean("bRemember",false);            //读取，如果用户先前选择了记住密码，则打勾
        last_login_phone=mShared.getString("last_login_phone","");  //读取本机最后一位登录的用户的手机号码
        get_verifycode.setVisibility(View.INVISIBLE);                               //默认是密码登录，获取验证码按钮不显示
        if(!last_login_phone.equals("")){                                           //如果非空,则自动填入手机号码
            et_phone.setText(last_login_phone);
        }
        if(bRemember){                                                              //如果之前已经有选择过记录密码，则记住密码复选框打勾,并且如果记住最后一次登录用户的密码
            ck_remember.setChecked(true);
            String phone = et_phone.getText().toString();                           //获得手机号码
            Log.d(TAG, "initData: "+phone);
         //   UserInfo info=mHelper.queryByPhone(phone);                              //获取用户
            UserInfo info=mHelper2.queryByPhone(phone);
            if(info!=null){                                                         //如何非空
                et_password.setText(info.getPassword());                            //自动填入密码
                temp=et_password.getText().toString().trim();                       //暂存字符
            }
        }else{
            ck_remember.setChecked(false);
        }
//        while(true){
//            Log.d(TAG,""+display.getWidth()+";"+display.getHeight());
//        }
    }

    private void findViewSetListener(){
        get_verifycode=findViewById(R.id.get_verifycode);
        //从名叫activity_main的XML布局文件中获取名叫login_click的按钮控件
        login_click = findViewById(R.id.login_click);
        //从名叫activity_main的XML布局文件中获取名叫regist_click的按钮控件
        regist_click = findViewById(R.id.regist_click);
        //从名叫activity_main的XML布局文件中获取名叫rg_login的单选组控件
        rg_login = findViewById(R.id.rg_login);
        //从名叫activity_main的XML布局文件中获取名叫rb_password的单选按钮控件
        rb_password=findViewById(R.id.rb_password);
        //从名叫activity_main的XML布局文件中获取名叫rb_verifycode的单选按钮控件
        rb_verifycode=findViewById(R.id.rb_verifycode);
        //从名叫activity_main的XML布局文件中获取名叫ck_remember的复选框控件
        ck_remember=findViewById(R.id.ck_remember);
        //从名叫activity_main的XML布局文件中获取名叫btn_forget的按钮控件
        btn_forget=findViewById(R.id.btn_forget);
        //从名叫activity_main的XML布局文件中获取名叫et_phone的编辑框控件
        et_phone=findViewById(R.id.et_phone);
        //从名叫activity_main的XML布局文件中获取名叫et_password的编辑框控件
        et_password=findViewById(R.id.et_password);


        // 给rg_login设置单选监听器
        rg_login.setOnCheckedChangeListener(new MainFrameActivity.RadioListener());
        // 给ck_remember设置勾选监听器
        ck_remember.setOnCheckedChangeListener(new MainFrameActivity.CheckListener());
        // 给获取验证码按钮设置监听器
        get_verifycode.setOnClickListener(this);
        //给btn_forget添加点击监听器
        btn_forget.setOnClickListener(this);
        //设置下划线
        btn_forget.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        //给login_click设置点击监听器，一旦用户点击按钮，就触发监听器的onClick方法
        login_click.setOnClickListener(this);
        //给regist_click设置点击监听器，一旦用户点击按钮，就触发监听器的onClick方法
        regist_click.setOnClickListener(this);
    }

    // 定义登录方式的单选监听器
    private class RadioListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.rb_password) { // 选择了密码登录
                et_password.setText(temp);
                et_password.setHint(R.string.InputPassward);
                et_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                et_password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ck_remember.setVisibility(View.VISIBLE);
                btn_forget.setVisibility(View.VISIBLE);
                get_verifycode.setVisibility(View.INVISIBLE);       //隐藏获取验证码按钮
            } else if (checkedId == R.id.rb_verifycode) {           // 选择了验证码登录
                et_password.setHint(R.string.InputCode);
                et_password.setInputType(InputType.TYPE_CLASS_NUMBER);
                et_password.setText("");
                get_verifycode.setVisibility(View.VISIBLE);         //显示获取验证码按钮
                ck_remember.setVisibility(View.INVISIBLE);          //隐藏记住密码复选框
                btn_forget.setVisibility(View.INVISIBLE);           //隐藏忘记密码复选框
            }
        }
    }

    // 定义是否记住密码的勾选监听器
    private class CheckListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView.getId() == R.id.ck_remember) {
                bRemember = isChecked;
            }
        }
    }

    //各个按钮的点击事件
    @Override
    public void onClick(View v) {
        String phone = et_phone.getText().toString();   //取得用户输入的手机号
        if (phone.length() != 11) { // 手机号码不是11位
            Toast.makeText(this, R.string.HintPhone1, Toast.LENGTH_SHORT).show();
        }
        // 点击了“忘记密码”按钮
        if (v.getId() == R.id.btn_forget) {
            if (rb_password.isChecked()) { // 选择了密码方式校验，此时要跳到找回密码页面
                Intent intent = new Intent(this, ForgetPsdActivity.class);
                // 携带手机号码跳转到找回密码页面
                intent.putExtra("phone", phone);
                startActivityForResult(intent, mRequestCode);
            }
        }
        // 点击了“登录”按钮
        else if (v.getId() == R.id.login_click) {
            UserInfo info = mHelper2.queryByPhone(phone);
        //    UserInfo info=mHelper.queryByPhone(phone);  //根据手机号搜索用户
            if(info==null){ //由于手机号未注册，所以找不到用户
                Toast.makeText(this,R.string.HintPhone2,Toast.LENGTH_SHORT).show();
            }
            else {   //手机号已注册的情况
                if (rb_password.isChecked()) { // 密码方式校验
                    Log.d("logincheck", ""+info.getPassword());
                    if (!et_password.getText().toString().equals(info.getPassword())) {
                        Toast.makeText(this, R.string.HintPhone3, Toast.LENGTH_SHORT).show();
                    }
                    else { // 密码校验通过
                        signIn(info.getPhone(),info.getPassword());   //把EMClient连接到聊天服务器
                        // 弹出提醒文字浮窗，提示用户登录成功
                        Toast.makeText(this,"登录成功",Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "登录成功1");
//                        loginSuccess();
                    }
                }
                else if (rb_verifycode.isChecked()) { // 验证码方式校验
                    if (!et_password.getText().toString().equals(mVerifyCode)) {
                        Toast.makeText(this, R.string.HintCode , Toast.LENGTH_SHORT).show();
                    }
                    else { // 验证码校验通过
                        Log.d(TAG, "登录成功2");
                        loginSuccess(); // 提示用户登录成功
                    }
                }
            }
        }
        //点击注册按钮
        else if (v.getId() == R.id.regist_click) {
            Intent intent_main_to_regist = new Intent(MainFrameActivity.this, RegistActivity.class);
            startActivity(intent_main_to_regist);
        }   //获取验证码
        else if(v.getId() == R.id.get_verifycode){
            Toast.makeText(this,"getVerifycode",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        String phone=et_phone.getText().toString();
        //判断是否是密码编辑框发生焦点变化
        if(v.getId()==R.id.et_password){
            //用户已输入手机号码，且密码框获得焦点，单选按钮选择的是以密码方式登录
            if(phone.length()>0&&phone.length()==11&&hasFocus&&rb_password.isChecked()){
                //根据手机号码到数据库中查询用户记录
                UserInfo info = mHelper2.queryByPhone(phone);
            //    UserInfo info=mHelper.queryByPhone(phone);
                if(info!=null){
                    //找到用户记录，则自动在密码框中填写该用户的密码
                    et_password.setText(info.getPassword());
                }
            }
        }
    }

    // 校验通过，登录成功
    private void loginSuccess() {
        Log.d(TAG, "loginSuccess: 函数进入");
        //如果此时记住密码勾选，则记录
        if(ck_remember.isChecked()){
            editor.putBoolean("bRemember",true);
        }else{
            editor.putBoolean("bRemember",false);
        }
        //记录这个手机号码为本机最后一个登录的用户的手机号码
        editor.putString("last_login_phone",et_phone.getText().toString().trim());
        //如果勾选了“记住密码”，则把手机号码和密码，还有登录时间保存为数据库的用户表记录
        if(bRemember){
            //创建一个用户信息实体类
            UserInfo info=new UserInfo();
            info.setPhone(et_phone.getText().toString());
            info.setPassword(et_password.getText().toString());
            info.setUpdate_Time(DateUtil.getNowDateTime());
            //用户数据库更新登录成功的用户信息（包含手机号码、密码、登录时间）
            mHelper2.updateuser(info);
        //    mHelper.updateUserData(info,info.getUpdate_Time());
        }
        //更改共享参数的值
        editor.putBoolean("first_login",false); //添加名叫first_login的布尔值
        editor.commit();    //提交编辑器的修改
        //控制显示引导页
        showLaunchPage();
    }

    //将EMClient实例连接至环信聊天服务器
    private void signIn(String userPhone,String password){
        EMClient.getInstance().logout(false);
        EMClient.getInstance().login(userPhone, password, new EMCallBack() {
            @Override
            public void onSuccess() {
                loginSuccess(); // 提示用户登录成功
                //为了保证进入主页面后本地会话和群组都 load 完毕
                EMClient.getInstance().groupManager().loadAllGroups();
                EMClient.getInstance().chatManager().loadAllConversations();
                Log.e(TAG, "登录方法signIn()：登录成功");
            }

            @Override
            public void onError(int i, String s) {
                Log.e(TAG, "登录方法signIn()：登录失败"+i+","+s);
            }

            @Override
            public void onProgress(int i, String s) {

            }
        });
    }

    //负责控制是否显示第一次成功登录的引导页
    private void showLaunchPage(){
        //能到达这里说明这个手机号是存在的
        //将手机号储存起来
        editor.putString("user_phone", et_phone.getText().toString()); //添加登录用户的手机号
        editor.commit();    //提交编辑器的修改

        if(isFirstLogin==true){
            //如果是首次成功登录，就前往引导页面
            Intent login_to_launch=new Intent(MainFrameActivity.this, LaunchSimpleActivity.class);
            startActivity(login_to_launch);
        }
        else {
            //非首次登录用户将会去到功能页面
            Intent login_to_tabhost=new Intent(MainFrameActivity.this, TabHostActivity.class);
            startActivity(login_to_tabhost);
        }
    }

    protected void onResume(){
        super.onResume();
        //获得用户数据库帮助器的一个实例
        mHelper=UserDBHelper.getInstance(this,2);
        //恢复页面，则打开数据库连接
        mHelper.openWriteLink();
    }

    protected void onPause(){
        super.onPause();
        //暂停页面，则关闭数据库连接
        mHelper.closeLink();
    }

    // 从修改密码页面返回登录页面，要清空密码的输入框
    @Override
    protected void onRestart() {
        et_password.setText("");
        super.onRestart();
    }

    // 从后一个页面携带参数返回当前页面时触发
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == mRequestCode && data != null) {
            // 得到手机号码，用户密码已改为新密码，故更新密码变量
            String phone=data.getStringExtra("phone");
            mPassword = data.getStringExtra("new_password");
            et_phone.setText(phone);
            //更新数据库中该手机号的用户信息
            UserInfo info=mHelper2.queryByPhone(phone);
        //    UserInfo info=mHelper.queryByPhone(phone);
            info.setUpdate_Time(DateUtil.getNowDateTime());
            mHelper.updateUserData(info,info.getUpdate_Time());
        }
    }
}







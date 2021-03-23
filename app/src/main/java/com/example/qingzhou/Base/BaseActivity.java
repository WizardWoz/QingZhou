package com.example.qingzhou.Base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qingzhou.R;
import com.example.qingzhou.util.MultiLanguageUtil;

public class BaseActivity extends AppCompatActivity {

    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MultiLanguageUtil.attachBaseContext(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver,intentFilter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
    }

    protected void setupBackAdUp(String title){
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //为标题栏设置标题，即给ActionBar设置标题。
            actionBar.setTitle(title);
            //ActionBar加一个返回图标
            actionBar.setDisplayHomeAsUpEnabled(true);
            //不显示当前程序的图标。
            actionBar.setDisplayShowHomeEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            //点击HOME ICON finish当前Activity
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class NetworkChangeReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if(networkInfo!=null && networkInfo.isAvailable()){
            //    Toast.makeText(context,getResources().getString(R.string.networksuccess),Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(context,getResources().getString(R.string.networkloss),Toast.LENGTH_SHORT).show();
            }
        }
    }
}

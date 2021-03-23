package com.example.qingzhou.util;

import android.view.Menu;
import android.view.Window;

import java.lang.reflect.Method;

public class MenuUtil {
    public static void setOverflowIconVisible(int featureId, Menu menu){
        //ActionBar的featureId是8，Toolbar的featureId是108
        if(featureId%100==Window.FEATURE_ACTION_BAR&&menu!=null){
            if(menu.getClass().getSimpleName().equals("MenuBuilder")){
                try{
                    //setOptionalIconsVisible是个隐藏方法，需要通过反射方法调用
                    Method m=menu.getClass().getDeclaredMethod("setOptionalIconsVisible",Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu,true);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}

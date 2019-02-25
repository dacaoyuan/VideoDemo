package com.wode369.videodemo;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Created by ypk on 2019/2/23 0023  10:15
 * <p>
 * Description:
 */
public class InitApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    //突破方法数限制
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}

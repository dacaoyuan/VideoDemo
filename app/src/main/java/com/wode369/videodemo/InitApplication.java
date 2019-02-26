package com.wode369.videodemo;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import iknow.android.utils.BaseUtils;
import nl.bravobit.ffmpeg.FFmpeg;

/**
 * Created by ypk on 2019/2/23 0023  10:15
 * <p>
 * Description:
 */
public class InitApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        BaseUtils.init(this);
        initFFmpegBinary(this);



    }

    private void initFFmpegBinary(Context context) {
        if (!FFmpeg.getInstance(context).isSupported()) {
            Log.e("InitApplication","Android cup arch not supported!");
        }
    }

    //突破方法数限制
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);



    }

}

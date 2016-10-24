package com.example.wanghui.mweibo;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by wanghui on 16-10-20.
 */

public class WeiboApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // the following line is important
        Fresco.initialize(getApplicationContext());
    }
}

package com.example.wanghui.weibo_sdk;

import com.android.volley.RequestQueue;

/**
 * Created by wanghui on 16-12-14.
 */

public abstract class AbsAPI {
    public RequestQueue mQueue;

    public AbsAPI(RequestQueue mQueue) {
        this.mQueue = mQueue;
    }
}

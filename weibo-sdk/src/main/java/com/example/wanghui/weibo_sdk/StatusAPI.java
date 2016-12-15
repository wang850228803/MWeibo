package com.example.wanghui.weibo_sdk;

import android.util.Log;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

/**
 * Created by wanghui on 16-12-14.
 */

public class StatusAPI extends AbsAPI {
    private static final String HOME_TIMELINE = "https://api.weibo.com/2/statuses/home_timeline.json";
    private static final String TAG = "StatusAPI";

    public StatusAPI(RequestQueue mQueue) {
        super(mQueue);
    }

    /**
     * 获取当前登录用户及其所关注用户的最新微博。
     *
     * @param since_id    若指定此参数，则返回ID比since_id大的微博（即比since_id时间晚的微博），默认为0
     * @param max_id      若指定此参数，则返回ID小于或等于max_id的微博，默认为0。
     * @param count       单页返回的记录条数，默认为50。
     * @param page        返回结果的页码，默认为1。
     * @param base_app    是否只获取当前应用的数据。0为否（所有数据），1为是（仅当前应用），默认为0。
     * @param feature 过滤类型ID，0：全部、1：原创、2：图片、3：视频、4：音乐，默认为0。
     * @param trim_user   返回值中user字段开关，0：返回完整user字段、1：user字段仅返回user_id，默认为0。
     * @param listener    异步请求回调接口
     */
    public void homeTimeline(String access_token, long max_id, long since_id, int count, int page, int base_app,
                             int feature, int trim_user, Response.Listener<String> listener,
                             Response.ErrorListener errorListener) {
        //api参数描述中since_id max_id顺序反了，url中是按参数顺序来取值的
        String url = HOME_TIMELINE + "?access_token=" + access_token + "&max_id=" + max_id + "&since_id=" + since_id
                + "&count=" + count + "&page=" + page + "&base_app=" + base_app + "&feature=" + feature + "&trim_user=" + trim_user;
        Log.i(TAG, "url=" + url);
        StringRequest request = new StringRequest(Method.GET, url, listener, errorListener);
                /*new Listener<String>() {

            @Override
            public void onResponse(String arg0) {
                Toast.makeText(getApplicationContext(), arg0, Toast.LENGTH_LONG).show();
                Log.d("onResponse", arg0);
            }
        }, new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError arg0) {
                Toast.makeText(getApplicationContext(), arg0.toString(), Toast.LENGTH_LONG).show();
                Log.d("onErrorResponse", arg0.toString());
            }
        });*/
        mQueue.add(request);
    }
}

package com.example.wanghui.weibo_sdk;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import static org.junit.Assert.*;

/**
 * Created by wanghui on 16-12-14.
 */
@RunWith(AndroidJUnit4.class)
public class StatusAPITest {
    final CountDownLatch signal = new CountDownLatch(1);
    @Test
    public void testHomeTimeline() throws Exception {
        final Context appContext = InstrumentationRegistry.getTargetContext();
        RequestQueue mQueue = Volley.newRequestQueue(appContext);
        StatusAPI sAPI = new StatusAPI(mQueue);

        Response.Listener<String> mReListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String arg0) {
                Log.d("onResponse", arg0);
                assertTrue(arg0.startsWith("{\"statuses\""));
                signal.countDown();
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                Log.d("onErrorResponse", arg0.toString());
            }
        };
        sAPI.homeTimeline("2.00Apyo2CbNyxqB4937806e1c5gunTB", 0, 0, 2, 1, 0, 0, 0, mReListener,errorListener);
        signal.await();
    }
}

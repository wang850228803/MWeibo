package com.wanghui.image;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wanghui on 16-7-6.
 */
public class AsyncImageLoader {
    Map<String, SoftReference<Drawable>> cache;

    public AsyncImageLoader() {
        cache = new HashMap<String, SoftReference<Drawable>>();
    }

    public Drawable loadImage(final String  url, final ILoadedListener listener) {
        Drawable img = null;
        SoftReference<Drawable> obj = cache.get(url);
        if (obj != null)
            img = obj.get();
        if (img != null)
            return img;

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Drawable image = (Drawable) msg.obj;
                listener.onImageLoaded(url, image);
            }
        };

        new Thread() {
            URL imageUrl;
            InputStream i;
            @Override
            public void run() {
                super.run();
                try {
                    imageUrl = new URL(url);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                try {
                    i = (InputStream) imageUrl.getContent();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Drawable draw = Drawable.createFromStream(i, "src");
                handler.obtainMessage(0, draw).sendToTarget();
            }
        }.start();
        return null;
    }

    public interface ILoadedListener {
        void onImageLoaded(String url, Drawable image);
    }
}

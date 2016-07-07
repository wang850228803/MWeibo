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
    boolean allowLoad = true;
    Object lock = new Object();
    int start;
    int end;

    public AsyncImageLoader() {
        cache = new HashMap<String, SoftReference<Drawable>>();
    }

    public void lock() {
        allowLoad = false;
    }

    public void unlock(int start, int end) {
        allowLoad = true;
        synchronized (lock) {
            lock.notifyAll();
        }
        this.start = start;
        this.end = end;
    }

    public Drawable loadImage(final int position, final String  url, final ILoadedListener listener) {

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
                cache.put(url, new SoftReference<Drawable>(image));
                listener.onImageLoaded(url, image);
            }
        };

        new Thread() {
            URL imageUrl;
            InputStream i;
            @Override
            public void run() {

                if (!allowLoad)
                    synchronized (lock) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (position > end || position < start)
                            return;
                    }

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

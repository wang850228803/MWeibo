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
        this.start = start;
        this.end = end;
        synchronized (lock) {
            lock.notifyAll();
        }

    }

    public Drawable loadImage(final int position, final String  url, final ILoadedListener listener) {
        //Log.i("wh", "enter loadimage,position:" + position);

        Drawable img = null;
        SoftReference<Drawable> obj = cache.get(url);
        if (obj != null)
            img = obj.get();
        if (img != null)
            return img;

        //Log.i("wh", "enter2 loadimage,position:" + position);

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Drawable image = (Drawable) msg.obj;
                cache.put(url, new SoftReference<Drawable>(image));
                listener.onImageLoaded(position, url, image);
            }
        };

        //Log.i("wh", "enter3 loadimage,position:" + position);
        new Thread() {
            URL imageUrl;
            InputStream i;
            @Override
            public void run() {

                if (!allowLoad)
                    synchronized (lock) {
                        try {
                            //Log.i("wh", "position:" + position);
                            //Log.i("wh", "before lock");
                            lock.wait();
                            //Log.i("wh", "after lock");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (position > end || position < start) {
                            return;
                        }

                }

                //Log.i("wh", "enter4 loadimage,position:" + position);
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
        void onImageLoaded(int position, String url, Drawable image);
    }
}

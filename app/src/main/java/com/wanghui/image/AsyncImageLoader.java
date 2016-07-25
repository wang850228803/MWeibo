package com.wanghui.image;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wanghui on 16-7-6.
 */
public class AsyncImageLoader {
    //一级缓存
    Map<String, SoftReference<Drawable>> cache;
    //二级缓存
    FileCache fileCache;
    //线程池
    ExecutorService executorService;

    boolean allowLoad = true;
    Object lock = new Object();
    int start;
    int end;

    ILoadedListener listener;

    public AsyncImageLoader(Context cxt) {
        cache = new HashMap<String, SoftReference<Drawable>>();
        fileCache = new FileCache(cxt, Environment.getExternalStorageState(), "weibo_images");
        executorService = Executors.newFixedThreadPool(9);
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

    public Drawable loadImage(int position, String  url, ILoadedListener listener) {

        Drawable img = null;
        SoftReference<Drawable> obj = cache.get(url);
        if (obj != null)
            img = obj.get();
        if (img != null) {
            return img;

        }

        this.listener = listener;
        executorService.execute(new LoadFromFileOrWeb(position, url));
        return null;
    }

    public interface ILoadedListener {
        void onImageLoaded(int position, String url, Drawable image);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Drawable image = (Drawable) msg.obj;
            Bundle bundle = msg.getData();
            int position = bundle.getInt("pos");
            String url = bundle.getString("url");
            listener.onImageLoaded(position, url, image);
        }
    };

    private class LoadFromFileOrWeb implements Runnable{
        private int position;
        private String url;
        URL imageUrl;
        InputStream i;
        Drawable draw;

        public LoadFromFileOrWeb (int position, String  url) {
            this.position = position;
            this.url = url;
        }

        @Override
        public void run() {
            if (!allowLoad)
                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (position > end || position < start) {
                        return;
                    }

                }

            File file = fileCache.getFileCacheImage(url);
            try {
                if (file.exists()) {
                    InputStream is = new FileInputStream(file);
                    draw = Drawable.createFromStream(is, "src");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if (draw == null) {
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
                draw = Drawable.createFromStream(i, "src");
                cache.put(url, new SoftReference<Drawable>(draw));
                fileCache.cacheImage(url, i);
            }

            Message mes = handler.obtainMessage(0, draw);
            Bundle bundle = new Bundle();
            bundle.putInt("pos", position);
            bundle.putString("url", url);
            mes.setData(bundle);
            mes.sendToTarget();
        }

    }

    public void clear() {
        fileCache.clear();
        cache.clear();
    }
}

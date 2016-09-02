package com.wanghui.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

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
    int start = 0;
    int end = 10;

    ILoadedListener listener;

    String TAG = "AsyncImageLoader";

    public AsyncImageLoader(Context cxt) {
        cache = new HashMap<String, SoftReference<Drawable>>();
        fileCache = new FileCache(cxt, Environment.getExternalStorageState(), "weibo_images");
        //reduce the thread pool number to avoid OOM.
        executorService = Executors.newFixedThreadPool(3);
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
        Drawable draw = null;

        public LoadFromFileOrWeb (int position, String  url) {
            this.position = position;
            this.url = url;
        }

        @Override
        public void run() {
            if (!allowLoad) {
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
            }

            //Because the thread in thread pool is 9, so if the thread num is large than 9, the thread will not be blocked.
            if (position > end || position < start) {
                return;
            }

            File file = fileCache.getFileCacheImage(url);
            try {
                if (file.exists()) {
                    InputStream is = new FileInputStream(file);
                    draw = Drawable.createFromStream(is, "src");
                    is.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (draw == null) {
                Log.i(TAG, "start to load from url:"+url+"start:" + start+"end:"+end+"position:"+position);
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

                //resolve the exception: Bitmap too large to be uploaded into a texture exception
                int w = draw.getIntrinsicWidth();
                int h = draw.getIntrinsicHeight();
                if (h > 2 * w) {
                    Bitmap bm = ((BitmapDrawable)draw).getBitmap();
                    Bitmap bms = Bitmap.createBitmap(bm, 0, (h - w) / 2, w, w, null, false);
                    draw = new BitmapDrawable(bms);
                    bm.recycle();
                }

                cache.put(url, new SoftReference<Drawable>(draw));
                fileCache.cacheImage(url, i);
                try {
                    i.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

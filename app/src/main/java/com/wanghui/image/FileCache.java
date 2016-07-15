package com.wanghui.image;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by wanghui on 16-7-14.
 */
public class FileCache {
    File mCacheDir;

    public FileCache(Context cxt, String cacheDir, String dir) {
        if (Environment.getExternalStorageDirectory().equals(Environment.MEDIA_MOUNTED)) {
            mCacheDir = new File(cacheDir, dir);
        } else {
            mCacheDir = cxt.getCacheDir();
        }
    }

    public File getFileCacheImage(String url) {
        File file = null;
        try {
            String filename = URLEncoder.encode(url, "utf-8");
            file = new File(mCacheDir, filename);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return file;
    }

    public void cacheImage(String url, InputStream is) {
        byte[] bytes = new byte[1024];
        OutputStream os = null;
        File file = getFileCacheImage(url);
        try {
            os = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for(;;) {
            try {
                int count = is.read(bytes, 0, 1024);
                if (count == -1)
                    break;
                else
                    os.write(bytes, 0, count);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void clear() {
        File[] files = mCacheDir.listFiles();
        for (File file:files) {
            file.delete();
        }
    }
}

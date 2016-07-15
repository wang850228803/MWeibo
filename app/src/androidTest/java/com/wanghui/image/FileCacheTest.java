package com.wanghui.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.wanghui.mweibo.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Created by wanghui on 16-7-14.
 */
public class FileCacheTest extends AndroidTestCase{
    Context cxt;
    FileCache fileCache;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        cxt = getContext();
        fileCache = new FileCache(cxt, Environment.getExternalStorageState(), "dir");
    }

    public void testCacheAndCleanImage() {
        Drawable draw = cxt.getResources().getDrawable(R.drawable.ic_launcher);
        fileCache.cacheImage("image1", Bitmap2InputStream(drawable2Bitmap(draw)));
        Log.i("----------------------", "" + fileCache.getFileCacheImage("image1"));
        assertTrue(fileCache.getFileCacheImage("image1").exists());
        fileCache.clear();
        assertFalse(fileCache.getFileCacheImage("image1").exists());
    }

    // 将Bitmap转换成InputStream
    public InputStream Bitmap2InputStream(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        return is;
    }

    // Drawable转换成Bitmap
    public Bitmap drawable2Bitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap
                .createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}

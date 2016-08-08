package com.example.wanghui.mweibo;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.lhh.ptrrv.library.PullToRefreshRecyclerView;
import com.wanghui.image.AsyncImageLoader;

import java.util.ArrayList;

/**
 * Created by wanghui on 16-7-6.
 */
public class WeiboImageAdapter extends BaseAdapter {
    ArrayList<String> mUrls;
    Context mCxt;
    LayoutInflater mInflater;
    AsyncImageLoader mLoader;
    ViewHolder mHolder;

    int pos;
    PullToRefreshRecyclerView lv;
    String TAG = "WeiboImageAdapter";
    ArrayList<String> mLargeUrls = new ArrayList<String>();

    public WeiboImageAdapter(Context cxt, PullToRefreshRecyclerView lv, int position, ArrayList<String> urls, AsyncImageLoader loader) {
        this.mUrls = urls;
        this.mCxt = cxt;
        this.mLoader = loader;
        this.pos = position;
        this.lv = lv;
        mInflater = LayoutInflater.from(cxt);
        getLargeUrls(urls, mLargeUrls);
    }

    private void getLargeUrls(ArrayList<String> urls, ArrayList<String> lus) {
        for (String s : urls) {
            lus.add(s.replace("/thumbnail/", "/large/"));
        }
    }

    @Override
    public int getCount() {
        return mUrls.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.image_item, null);
            mHolder = new ViewHolder();
            mHolder.imageView = (ImageView) convertView.findViewById(R.id.itemimage);
            convertView.setTag(mHolder);
        }
        String url = mUrls.get(position).replace("/thumbnail/", "/bmiddle/");
        mHolder = (ViewHolder) convertView.getTag();
        mHolder.imageView.setTag(url);
        mHolder.imageView.setBackgroundColor(mCxt.getResources().getColor(R.color.gradviewBack));
        mHolder.imageView.setImageDrawable(mLoader.loadImage(pos, url, new AsyncImageLoader.ILoadedListener() {
            @Override
            public void onImageLoaded(int listPos, String url, Drawable image) {
                ImageView iv = (ImageView) lv.findViewWithTag(url);
                if (iv != null)
                    iv.setImageDrawable(image);
                Log.i(TAG, "iv:" + iv + "url:" + url);
            }
        }));
        mHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mCxt, TouchGallery.class);
                intent.putStringArrayListExtra("lUrls", mLargeUrls);
                intent.putExtra("pos", position);
                mCxt.startActivity(intent);
            }
        });
        return convertView;
    }

    public class ViewHolder {
        ImageView imageView;
    }
}

package com.example.wanghui.mweibo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.wanghui.image.AsyncImageLoader;

import java.util.ArrayList;

/**
 * Created by wanghui on 16-7-6.
 */
public class WeiboImageAdapter extends BaseAdapter {
    ArrayList<String> urls;
    Context cxt;
    LayoutInflater mInflater;
    AsyncImageLoader mLoader;
    ViewHolder holder;
    int pos;
    ListView lv;

    public WeiboImageAdapter(Context cxt, ListView lv, int positon, ArrayList<String> urls, AsyncImageLoader loader) {
        this.urls = urls;
        this.cxt = cxt;
        this.mLoader = loader;
        this.pos = positon;
        this.lv = lv;
        mInflater = LayoutInflater.from(cxt);
        mLoader = new AsyncImageLoader();
    }

    @Override
    public int getCount() {
        return urls.size();
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
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.itemimage);
            convertView.setTag(holder);
        }
        holder = (ViewHolder) convertView.getTag();
        holder.imageView.setTag(urls.get(position));
        /*holder.imageView.setImageDrawable(mLoader.loadImage(urls.get(position), new AsyncImageLoader.ILoadedListener() {
            @Override
            public void onImageLoaded(String url, Drawable image) {
                NoScrollGradView gv = (NoScrollGradView) lv.findViewWithTag(pos);
            }
        }));*/
        return convertView;
    }

    public class ViewHolder {
        ImageView imageView;
    }
}

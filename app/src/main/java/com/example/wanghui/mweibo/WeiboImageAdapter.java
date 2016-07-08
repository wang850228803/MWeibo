package com.example.wanghui.mweibo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.wanghui.image.AsyncImageLoader;
import com.wanghui.image.NoScrollGradView;

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

    public WeiboImageAdapter(Context cxt, ListView lv, int position, ArrayList<String> urls, AsyncImageLoader loader) {
        this.urls = urls;
        this.cxt = cxt;
        this.mLoader = loader;
        this.pos = position;
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
        holder.imageView.setBackgroundColor(cxt.getResources().getColor(R.color.gradviewBack));
        holder.imageView.setImageDrawable(mLoader.loadImage(pos, urls.get(position), new AsyncImageLoader.ILoadedListener() {
            @Override
            public void onImageLoaded(int listPos, String url, Drawable image) {
                NoScrollGradView gv = (NoScrollGradView) lv.findViewWithTag(listPos);
                //Log.i("wanghui", "lisp:" + listPos + "-------------------gv" + gv);
                ImageView iv = (ImageView) lv.findViewWithTag(url);
                //Log.i("wanghui", "-------------------iv" + iv);
                if (iv != null)
                    iv.setImageDrawable(image);
            }
        }));
        return convertView;
    }

    public class ViewHolder {
        ImageView imageView;
    }
}

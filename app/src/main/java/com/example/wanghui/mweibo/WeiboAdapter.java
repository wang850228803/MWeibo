package com.example.wanghui.mweibo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.sina.weibo.sdk.openapi.models.Status;
import com.wanghui.image.AsyncImageLoader;
import com.wanghui.image.NoScrollGradView;

import java.util.List;

/**
 * Created by wanghui on 16-7-6.
 */
public class WeiboAdapter extends BaseAdapter {
    List<Status> statusList;
    LayoutInflater inflater;
    MViewHolder holder;
    AsyncImageLoader loader;
    ListView lv;
    Context cxt;

    public WeiboAdapter(Context cxt, ListView lv) {
        inflater = LayoutInflater.from(cxt);
        loader = new AsyncImageLoader();
        this.lv = lv;
        this.cxt = cxt;
        lv.setOnScrollListener(mScrollListener);
    }

    public void addStatus(List<Status> list) {
        this.statusList = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item, null);
            holder = new MViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.imageView);
            holder.username = (TextView) convertView.findViewById(R.id.name);
            holder.created_at = (TextView) convertView.findViewById(R.id.time);
            holder.text = (TextView) convertView.findViewById(R.id.content);
            holder.reposts_count = (TextView) convertView.findViewById(R.id.repost);
            holder.comments_count = (TextView) convertView.findViewById(R.id.comment);
            holder.gridView = (NoScrollGradView) convertView.findViewById(R.id.gridview);
            convertView.setTag(holder);
        } else {
            holder = (MViewHolder) convertView.getTag();
        }

        Status status = statusList.get(position);
        if (status.user != null) {
            if (status.user.profile_image_url != null && !status.user.profile_image_url.equals("")) {
                holder.icon.setTag(status.user.profile_image_url);
                holder.icon.setImageDrawable(loader.loadImage(position, status.user.profile_image_url, new AsyncImageLoader.ILoadedListener() {
                    @Override
                    public void onImageLoaded(int pos, String url, Drawable image) {
                        ImageView iv = (ImageView) lv.findViewWithTag(url);
                        if (iv != null)
                            iv.setImageDrawable(image);
                    }
                }));
            }

            holder.username.setText(status.user.screen_name);
        }

        if (status.pic_urls != null && status.pic_urls.size() != 0) {
            holder.gridView.setTag(position);
            holder.gridView.setAdapter(new WeiboImageAdapter(cxt, lv, position, status.pic_urls, loader));
        } else {
            holder.gridView.setAdapter(null);
        }
        holder.created_at.setText(status.created_at);
        holder.text.setText(status.text);
        holder.reposts_count.setText("转发(" + status.reposts_count + ")");
        holder.comments_count.setText("评论(" + status.comments_count + ")");
        return convertView;
    }

    @Override
    public int getCount() {
        if (statusList != null)
            return statusList.size();
        else
            return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public final class MViewHolder {
        public ImageView icon;
        /** 微博作者的用户信息字段 */
        public TextView username;
        public TextView created_at;
        /** 微博信息内容 */
        public TextView text;
        /** 转发数 */
        public TextView reposts_count;
        /** 评论数 */
        public TextView comments_count;
        /** 图片*/
        public NoScrollGradView gridView;
    }

    AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    loader.lock();
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                    loader.unlock(lv.getFirstVisiblePosition(), lv.getLastVisiblePosition());
                    break;
                default:
                    break;

            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        }
    };
}

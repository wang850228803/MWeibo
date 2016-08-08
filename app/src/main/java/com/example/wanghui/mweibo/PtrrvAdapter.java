package com.example.wanghui.mweibo;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.lhh.ptrrv.library.PullToRefreshRecyclerView;
import com.sina.weibo.sdk.openapi.models.Status;
import com.wanghui.image.AsyncImageLoader;
import com.wanghui.image.NoScrollGradView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wanghui on 16-7-19.
 */
public class PtrrvAdapter extends RecyclerView.Adapter<PtrrvAdapter.MViewHolder> {
    public List<Status> statusList = new ArrayList<Status>();
    AsyncImageLoader mLoader;
    PullToRefreshRecyclerView mPtrrv;
    MainActivity mActivity;
    ImageView mIV;

    public PtrrvAdapter(MainActivity activity, PullToRefreshRecyclerView ptrrv, ImageView iv) {
        this.mPtrrv = ptrrv;
        this.mActivity = activity;
        this.mIV = iv;
        mLoader = new AsyncImageLoader(activity);
        ptrrv.addOnScrollListener(mScrollListener);
    }

    public void addStatus(List<Status> list) {
        statusList.addAll(list);
    }

    @Override
    public MViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MViewHolder holder = new MViewHolder(LayoutInflater.from(mActivity).inflate(R.layout.item, null));
        return holder;
    }

    @Override
    public void onBindViewHolder(MViewHolder holder, int position) {
        Status status = statusList.get(position);
        if (status.user != null) {
            if (status.user.profile_image_url != null && !status.user.profile_image_url.equals("")) {
                holder.icon.setTag(status.user.profile_image_url);
                holder.icon.setImageDrawable(mLoader.loadImage(position, status.user.profile_image_url, new AsyncImageLoader.ILoadedListener() {
                    @Override
                    public void onImageLoaded(int pos, String url, Drawable image) {
                        ImageView iv = (ImageView) mPtrrv.findViewWithTag(url);
                        if (iv != null)
                            iv.setImageDrawable(image);
                    }
                }));
            }

            holder.username.setText(status.user.screen_name);
        }

        if (status.pic_urls != null && status.pic_urls.size() != 0) {
            holder.gridView.setAdapter(new WeiboImageAdapter(mActivity, mPtrrv, position, status.pic_urls, mLoader));
        } else {
            holder.gridView.setAdapter(null);
        }
        holder.created_at.setText(status.created_at);
        holder.text.setText(status.text);
        holder.reposts_count.setText("转发(" + status.reposts_count + ")");
        holder.comments_count.setText("评论(" + status.comments_count + ")");
    }

    @Override
    public int getItemCount() {
        if (statusList != null)
            return statusList.size();
        else
            return 0;
    }

    public class MViewHolder extends RecyclerView.ViewHolder{
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

        public MViewHolder(View view) {
            super(view);
            icon = (ImageView) view.findViewById(R.id.imageView);
            username = (TextView) view.findViewById(R.id.name);
            created_at = (TextView) view.findViewById(R.id.time);
            text = (TextView) view.findViewById(R.id.content);
            reposts_count = (TextView) view.findViewById(R.id.repost);
            comments_count = (TextView) view.findViewById(R.id.comment);
            gridView = (NoScrollGradView) view.findViewById(R.id.gridview);
        }
    }

    private boolean mScrollFlag = false;
    private int lastVisibleItemPosition = 0;

    PullToRefreshRecyclerView.OnScrollListener mScrollListener = new PullToRefreshRecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            switch (newState) {
                case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                    mLoader.lock();
                    mScrollFlag = false;
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    mLoader.lock();
                    mScrollFlag = true;
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                    mLoader.unlock(mPtrrv.findFirstVisibleItemPosition(), mPtrrv.findLastVisibleItemPosition());
                    mScrollFlag = false;
                    if (mPtrrv.findLastVisibleItemPosition() == (statusList.size() - 1)) {
                        mIV.setVisibility(View.VISIBLE);
                    }
                    if (mPtrrv.findFirstVisibleItemPosition() == 0) {
                        mIV.setVisibility(View.GONE);
                    }
                    break;
                default:
                    break;

            }

        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

        }

        @Override
        public void onScroll(RecyclerView recyclerView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (mScrollFlag) {
                if (firstVisibleItem > lastVisibleItemPosition)
                    mIV.setVisibility(View.GONE);
                else if (firstVisibleItem < lastVisibleItemPosition)
                    mIV.setVisibility(View.VISIBLE);
                else
                    return;
                lastVisibleItemPosition = firstVisibleItem;
            }
        }
    };

    public void clear() {
        mLoader.clear();
    }
}

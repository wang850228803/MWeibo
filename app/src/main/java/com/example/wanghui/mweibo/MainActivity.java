package com.example.wanghui.mweibo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.StatusList;
import com.sina.weibo.sdk.utils.LogUtil;
import com.wanghui.image.AsyncImageLoader;
import com.wanghui.weibo.util.AccessTokenKeeper;
import com.wanghui.weibo.util.Constants;

import java.util.List;

public class MainActivity extends Activity {

    private SsoHandler mSsoHandler;
    private AuthInfo mAuthInfo;
    /** 显示认证后的信息，如 AccessToken */

    /** 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能  */
    private Oauth2AccessToken mAccessToken;

    private Button btn;
    private ListView lv;

    private String TAG = "MainActivity";

    StatusList statuses;
    private WeiboAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_main);
        btn = (Button)findViewById(R.id.login);
        btn.setOnClickListener(mListener);
        lv = (ListView) findViewById(R.id.listview);
        mAdapter = new WeiboAdapter(this);
        lv.setAdapter(mAdapter);

        if (AccessTokenKeeper.isTokenExist(this)) {
            btn.setVisibility(View.GONE);
            mAccessToken = AccessTokenKeeper.readAccessToken(this);
            loadWeibo();
        } else {
            authorize();
        }
    }

    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            authorize();
        }
    };

    private void authorize() {
        mAuthInfo = new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
        mSsoHandler = new SsoHandler(this, mAuthInfo);
        mSsoHandler.authorize(new AuthListener());
    }

    private void loadWeibo() {
        StatusesAPI mStatusesAPI = new StatusesAPI(this, Constants.APP_KEY, mAccessToken);
        if (mAccessToken != null && mAccessToken.isSessionValid())
            mStatusesAPI.friendsTimeline(0L, 0L, 100, 1, false, 0, false, mReListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    /**
     * 微博认证授权回调类。
     * 1. SSO 授权时，需要在 {@link #onActivityResult} 中调用 {@link SsoHandler#authorizeCallBack} 后，
     *    该回调才会被执行。
     * 2. 非 SSO 授权时，当授权结束后，该回调就会被执行。
     * 当授权成功后，请保存该 access_token、expires_in、uid 等信息到 SharedPreferences 中。
     */
    class AuthListener implements WeiboAuthListener {

        @Override
        public void onComplete(Bundle values) {
            // 从 Bundle 中解析 Token
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            if (mAccessToken.isSessionValid()) {
                // 保存 Token 到 SharedPreferences
                AccessTokenKeeper.writeAccessToken(MainActivity.this, mAccessToken);
                Toast.makeText(MainActivity.this,
                        R.string.weibosdk_toast_auth_success, Toast.LENGTH_SHORT).show();
                btn.setVisibility(View.GONE);
                loadWeibo();
            } else {
                // 以下几种情况，您会收到 Code：
                // 1. 当您未在平台上注册的应用程序的包名与签名时；
                // 2. 当您注册的应用程序包名与签名不正确时；
                // 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
                String code = values.getString("code");
                String message = getString(R.string.weibosdk_toast_auth_failed);
                if (!TextUtils.isEmpty(code)) {
                    message = message + "\nObtained the code: " + code;
                }
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onCancel() {
            Toast.makeText(MainActivity.this,
                    R.string.weibosdk_toast_auth_canceled, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(MainActivity.this,
                    "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private RequestListener mReListener = new RequestListener() {
        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                LogUtil.i(TAG, response);
                if (response.startsWith("{\"statuses\"")) {
                    // 调用 StatusList#parse 解析字符串成微博列表对象
                    statuses = StatusList.parse(response);
                    if (statuses != null && statuses.total_number > 0) {
                        Toast.makeText(MainActivity.this,
                                "获取微博信息流成功, 条数: " + statuses.statusList.size(),
                                Toast.LENGTH_LONG).show();
                    }
                    mAdapter.addStatus(statuses.statusList);
                    mAdapter.notifyDataSetChanged();
                } else if (response.startsWith("{\"created_at\"")) {
                    /*// 调用 Status#parse 解析字符串成微博对象
                    Status status = Status.parse(response);
                    Toast.makeText(WBStatusAPIActivity.this,
                            "发送一送微博成功, id = " + status.id,
                            Toast.LENGTH_LONG).show();*/
                } else {
                    /*Toast.makeText(WBStatusAPIActivity.this, response, Toast.LENGTH_LONG).show();*/
                }
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            LogUtil.e(TAG, e.getMessage());
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Toast.makeText(MainActivity.this, info.toString(), Toast.LENGTH_LONG).show();
        }
    };

    private class WeiboAdapter extends BaseAdapter {
        List<Status> statusList;
        LayoutInflater inflater;
        MViewHolder holder;
        AsyncImageLoader loader;

        public WeiboAdapter(Context cxt) {
            inflater = LayoutInflater.from(cxt);
            loader = new AsyncImageLoader();
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
                convertView.setTag(holder);
            } else {
                holder = (MViewHolder) convertView.getTag();
            }

            Status status = statusList.get(position);
            if (status.user != null) {
                if (status.user.url != null && !status.user.profile_image_url.equals(""))
                    holder.icon.setTag(status.user.profile_image_url);
                    holder.icon.setImageDrawable(loader.loadImage(status.user.profile_image_url, new AsyncImageLoader.ILoadedListener() {
                        @Override
                        public void onImageLoaded(String url, Drawable image) {
                            ImageView iv = (ImageView) lv.findViewWithTag(url);
                            if (iv != null)
                                iv.setImageDrawable(image);
                        }
                    }));

                holder.username.setText(status.user.screen_name);
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
    }

}


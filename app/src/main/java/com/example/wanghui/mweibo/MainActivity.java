package com.example.wanghui.mweibo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.lhh.ptrrv.library.PullToRefreshRecyclerView;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.StatusList;
import com.sina.weibo.sdk.utils.LogUtil;
import com.wanghui.weibo.util.AccessTokenKeeper;
import com.wanghui.weibo.util.Constants;

public class MainActivity extends Activity {

    private SsoHandler mSsoHandler;
    private AuthInfo mAuthInfo;
    /** 显示认证后的信息，如 AccessToken */

    /** 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能  */
    private Oauth2AccessToken mAccessToken;

    private Button btn;
    private ListView lv;
    private PullToRefreshRecyclerView mPtrrv;

    private String TAG = "MainActivity";

    StatusList statuses;
    private PtrrvAdapter mAdapter;

    private long since_id = 0L;
    private long max_id = 0L;
    private StatusesAPI mStatusesAPI;
    private boolean loadNow = true;
    private boolean refreshNow = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_main);
        btn = (Button)findViewById(R.id.login);
        btn.setOnClickListener(mListener);
        //lv = (ListView) findViewById(R.id.listview);
        mPtrrv = (PullToRefreshRecyclerView) this.findViewById(R.id.ptrrv);
        //改用ptrrv
        //mAdapter = new WeiboAdapter(this, lv);
        //lv.setAdapter(mAdapter);
        // custom own load-more-view and add it into ptrrv
        LoadMoreView loadMoreView = new LoadMoreView(this, mPtrrv.getRecyclerView());
        loadMoreView.setLoadmoreString(getString(R.string.demo_loadmore));
        loadMoreView.setLoadMorePadding(100);

        mPtrrv.setLoadMoreFooter(loadMoreView);
        //remove header
        //mPtrrv.removeHeader();

        // set true to open swipe(pull to refresh, default is true)
        mPtrrv.setSwipeEnable(true);

        // set the layoutManager which to use
        mPtrrv.setLayoutManager(new LinearLayoutManager(this));

        // set PagingableListener
        mPtrrv.setPagingableListener(new PullToRefreshRecyclerView.PagingableListener() {
            @Override
            public void onLoadMoreItems() {
                //do loadmore here
                refreshNow = false;
                loadNow = true;
                loadWeibo(0, max_id);
            }
        });

        // set OnRefreshListener
        mPtrrv.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // do refresh here
                refreshNow = true;
                loadNow = false;
                loadWeibo(since_id, 0);
            }
        });

        // add item divider to recyclerView
        mPtrrv.getRecyclerView().addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));

        // add headerView
        //mPtrrv.addHeaderView(View.inflate(this, R.layout.header, null));

        //set EmptyVIEW
        mPtrrv.setEmptyView(View.inflate(this,R.layout.empty_view, null));

        // set loadmore enable, onFinishLoading(can load more? , select before item)
        mPtrrv.onFinishLoading(true, false);
        // Finally: Set the adapter which extends RecyclerView.Adpater

        mAdapter = new PtrrvAdapter(this, mPtrrv);
        mPtrrv.setAdapter(mAdapter);

        if (AccessTokenKeeper.isTokenExist(this)) {
            btn.setVisibility(View.GONE);
            mAccessToken = AccessTokenKeeper.readAccessToken(this);
            Log.i("------------", mAccessToken + "");
            mStatusesAPI = new StatusesAPI(MainActivity.this, Constants.APP_KEY, mAccessToken);
            loadWeibo(since_id, max_id);
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

    public void loadWeibo(long startId, long endId) {
        if (mAccessToken != null && mAccessToken.isSessionValid())
            mStatusesAPI.friendsTimeline(startId, endId, 100, 1, false, 0, false, mReListener);
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
                mStatusesAPI = new StatusesAPI(MainActivity.this, Constants.APP_KEY, mAccessToken);
                loadWeibo(since_id, max_id);
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
                    if (statuses != null && statuses.statusList != null && statuses.statusList.size() > 0) {
                        if (refreshNow) {
                            since_id = Long.parseLong(statuses.statusList.get(0).id);

                        }
                        if (loadNow)
                            max_id = Long.parseLong(statuses.statusList.get(statuses.statusList.size() - 1).id) - 1;
                        if (!refreshNow)
                            mAdapter.addStatus(statuses.statusList);
                        else
                            mAdapter.statusList.addAll(0, statuses.statusList);

                        //mAdapter.setCount(DEFAULT_ITEM_SIZE + ITEM_SIZE_OFFSET);
                        mAdapter.notifyDataSetChanged();
                        mPtrrv.onFinishLoading(true, false);
                    }
                    if (refreshNow)
                        mPtrrv.setOnRefreshComplete();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("this", "remove the cache");
        mAdapter.clear();
    }
}


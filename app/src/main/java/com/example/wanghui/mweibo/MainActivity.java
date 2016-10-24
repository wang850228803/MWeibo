package com.example.wanghui.mweibo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
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
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.StatusList;
import com.util.AccessTokenKeeper;
import com.util.Constants;

import java.io.File;

public class MainActivity extends Activity {

    private SsoHandler mSsoHandler;
    private AuthInfo mAuthInfo;
    /** 显示认证后的信息，如 AccessToken */

    /** 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能  */
    private Oauth2AccessToken mAccessToken;

    private ImageView mToTop;
    private PullToRefreshRecyclerView mPtrrv;

    private String TAG = "MainActivity";

    StatusList statuses;
    private PtrrvAdapter mAdapter;

    private long since_id = 0L;
    private long max_id = 0L;
    private StatusesAPI mStatusesAPI;
    private boolean loadNow = true;
    private boolean refreshNow = true;

    private PopupWindow mPw;
    ImageView mSettingsMenu;
    private ImageView mAddMenu;

    public static final int REQUEST_FOR_ADD = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_main);
        mToTop = (ImageView) findViewById(R.id.toTop);
        mToTop.setOnClickListener(mListener);
        mPtrrv = (PullToRefreshRecyclerView) this.findViewById(R.id.ptrrv);
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

        mAdapter = new PtrrvAdapter(this, mPtrrv, mToTop);
        mPtrrv.setAdapter(mAdapter);

        if (AccessTokenKeeper.isTokenValid(this)) {
            mAccessToken = AccessTokenKeeper.readAccessToken(this);
            Log.i("------------", mAccessToken + "");
            mStatusesAPI = new StatusesAPI(MainActivity.this, Constants.APP_KEY, mAccessToken);
            loadWeibo(since_id, max_id);
        } else {
            authorize();
        }
        prepareMenu();
    }

    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.toTop:
                    mPtrrv.scrollToPosition(0);
                    mAdapter.reset();
                    mPtrrv.setRefreshing(true);
                    refreshNow = true;
                    loadNow = false;
                    loadWeibo(since_id, 0);
                    break;
                default:
                    break;
            }
        }
    };

    private void authorize() {
        mAuthInfo = new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
        mSsoHandler = new SsoHandler(this, mAuthInfo);
        mSsoHandler.authorize(new AuthListener());
    }

    public void loadWeibo(long startId, long endId) {
        if (mAccessToken != null && mAccessToken.isSessionValid()) {
            Log.i(TAG, "load weibo...startID="+startId+" endID:" + endId);
            mStatusesAPI.friendsTimeline(startId, endId, 100, 1, false, 0, false, mReListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = null;
        if (requestCode == REQUEST_FOR_ADD) {
            if (resultCode == RESULT_OK) {
                String filePath = data.getStringExtra("image");
                File imageFile = new File(filePath);
                if (imageFile.exists()) {
                    bitmap = BitmapFactory.decodeFile(filePath);
                    //mStatusesAPI.upload(data.getStringExtra("text"), bitmap, "0", "0", mReListener);     //上传时oom
                }
            }
            return;
        }
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
            MainActivity.this.finish();
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
                Log.i(TAG, response);
                if (response.startsWith("{\"statuses\"")) {
                    // 调用 StatusList#parse 解析字符串成微博列表对象
                    statuses = StatusList.parse(response);
                    if (statuses != null && statuses.statusList != null && statuses.statusList.size() > 0) {
                        if (refreshNow) {
                            since_id = Long.parseLong(statuses.statusList.get(0).id);
                        }
                        if (loadNow)
                            max_id = Long.parseLong(statuses.statusList.get(statuses.statusList.size() - 1).id);
                        if (!refreshNow) {
                            mAdapter.statusList.remove(mAdapter.statusList.size() - 1);
                            mAdapter.addStatus(statuses.statusList);
                        }
                        else {
                            mAdapter.statusList.addAll(0, statuses.statusList);
                            mPtrrv.setOnRefreshComplete();
                        }

                        //mAdapter.setCount(DEFAULT_ITEM_SIZE + ITEM_SIZE_OFFSET);
                        mAdapter.notifyDataSetChanged();
                        mPtrrv.onFinishLoading(true, false);
                    } else {
                        //Once the statuses.statusList.size() = 0, it will always be 0.
                        if (loadNow)
                            mPtrrv.setOnLoadMoreComplete();
                        if (refreshNow)
                            mPtrrv.setOnRefreshComplete();
                    }

                } else if (response.startsWith("{\"created_at\"")) {
                    // 调用 Status#parse 解析字符串成微博对象
                    Status status = Status.parse(response);
                    Toast.makeText(MainActivity.this,
                            "发送一送微博成功, id = " + status.id,
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, response, Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            mPtrrv.onFinishLoading(true, true);
            mPtrrv.setOnRefreshComplete();
            Log.e(TAG, e.getMessage());
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Toast.makeText(MainActivity.this, info.toString(), Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        menuState = 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(this + "", "remove the cache");
        mAdapter.clear();
    }

    private int menuState = 0;

    private void prepareMenu() {
        View view = getLayoutInflater().inflate(R.layout.layout_menu, null);
        mPw = new PopupWindow(view, LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
        mSettingsMenu = (ImageView) view.findViewById(R.id.settings);
        mSettingsMenu.setOnClickListener(mMenuListener);
        mAddMenu = (ImageView) view.findViewById(R.id.add);
        mAddMenu.setOnClickListener(mMenuListener);
    }

    View.OnClickListener mMenuListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.add:
                    startActivityForResult(new Intent(MainActivity.this, AddNewWeibo.class), REQUEST_FOR_ADD);
                    mPw.dismiss();
                    break;
                case R.id.settings:
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    mPw.dismiss();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (menuState == 0) {
                mPw.showAtLocation(findViewById(R.id.content), Gravity.CENTER, 0, this.getWindowManager().getDefaultDisplay().getHeight() - 100);
                menuState = 1;
            } else {
                mPw.dismiss();
                menuState = 0;
            }
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_BACK && menuState == 1) {
            mPw.dismiss();
            menuState = 0;
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

/*    //This leads to misbehavior.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //menu.add(0, Menu.FIRST, 0, "test");//This should be added, otherwise the first menu close will be failed.
        View view = getLayoutInflater().inflate(R.layout.layout_menu, null);
        mPw = new PopupWindow(view, LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
        mSettingsMenu = (ImageView) view.findViewById(R.id.settings);
        mSettingsMenu.setOnClickListener(mMenuListener);
        mAddMenu = (ImageView) view.findViewById(R.id.add);
        mAddMenu.setOnClickListener(mMenuListener);
        //return super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mPw.showAtLocation(findViewById(R.id.content), Gravity.CENTER, 0, this.getWindowManager().getDefaultDisplay().getHeight() - 100);
        //return super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        mPw.dismiss();
        //super.onOptionsMenuClosed(menu);
    }*/

}



package com.youqude.storyflow.ui;

import com.tencent.weibo.oauthv2.OAuthV2;
import com.tencent.weibo.webview.OAuthV2AuthorizeWebView;
import com.youqude.storyflow.R;
import com.youqude.storyflow.StoryFlowApp;
import com.youqude.storyflow.StoryFlowEventHandler;
import com.youqude.storyflow.adapter.StoryFlowDescriptionAdapter;
import com.youqude.storyflow.adapter.StoryFlowHomeDescriptionAdapter;
import com.youqude.storyflow.domain.AlbumInfo;
import com.youqude.storyflow.domain.MemoryImageCache;
import com.youqude.storyflow.domain.StoryInfo;
import com.youqude.storyflow.domain.UserInfo;
import com.youqude.storyflow.net.QqDownloadAsyncTask;
import com.youqude.storyflow.net.StoryDesHeadBitmapLoadAsyncTask;
import com.youqude.storyflow.net.StoryHomeDesHeadBitmapLoadAsyncTask;
import com.youqude.storyflow.utils.Constants;
import com.youqude.storyflow.utils.DesEncrypt;
import com.youqude.storyflow.utils.StoryLogger;
import com.youqude.storyflow.utils.Utility;

import android.app.AlertDialog;
import android.app.LocalActivityManager;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.TabHost.TabSpec;

import java.util.ArrayList;

public class StoryHomeActivity extends BaseActivity implements StoryFlowEventHandler,
        OnClickListener, OnScrollListener {

    private static final String TAG = StoryHomeActivity.class.getSimpleName();

    private TabHost tabHost;
    private LocalActivityManager mlam;

    RelativeLayout mBottomLayout;
    Button mButton_Register;
    Button mButton_Login;

    static int REQUEST_CODE = 0;

    boolean isLogin;

    View homeView;
    View homeDesView;
    boolean isDescriptionView;

    Button mButtonBack;
    TextView mTextViewStoryTitle;
    Button mButtonCamera;
    ListView mListView;
    StoryFlowHomeDescriptionAdapter mDescriptionAdapter;

    int pageNum = -1;
    int pageSize = 15;
    int recordSize;
    int totalPage;
    int mLastSavedTotalCount = -1;

    private MemoryImageCache mImageCache;
    private StoryHomeDesHeadBitmapLoadAsyncTask headBitmapLoadAsyncTask;
    private int mStartIndex;
    private int mEndIndex;

    private String mCurrentStoryId;

    private ArrayList<AlbumInfo> mOriginalData;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(Constants.CHANGE_STORY_HOME_DES_CONTENT_VIEW_ACTION)) {
                StoryLogger.e(TAG, TAG + "--->" + action);

                setContentView(homeDesView);
                isDescriptionView = true;

                Bundle extras = intent.getExtras();
                if (extras != null) {
                    @SuppressWarnings("unchecked")
                    ArrayList<AlbumInfo> data = (ArrayList<AlbumInfo>) extras
                            .getSerializable("data");
                    String mStoryId = extras.getString("mStoryId");
                    mCurrentStoryId = mStoryId;
                    int index = extras.getInt("index");
                    StoryLogger.e(TAG, TAG + ":" + mStoryId);
                    if (!TextUtils.isEmpty(mStoryId)) {

                        refreshHead();
                        mDescriptionAdapter.setData(data);
                        mDescriptionAdapter.notifyDataSetChanged();
                        mListView.setSelection(index);
                        mOriginalData = data;
                        
                        pageNum = 1;
                        
                        if (!TextUtils.isEmpty(StoryFlowApp.getInstance().userInfo.sessionId)) {
                            /**
                             * 异步调用，获取喜欢状态列表
                             */
                            mService.loadLikedState(StoryHomeActivity.this, mCurrentStoryId);
                        }

                        /*
                         * mService.getStoryPicById(StoryHomeActivity.this,
                         * mStoryId, pageNum, pageSize);
                         */
                        mService.getStoryTitleByStoryId(StoryHomeActivity.this, mStoryId);
                    }
                }

            }

        }
    };
    
    
    private BroadcastReceiver mLoginReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            
            String action = intent.getAction();
            if (action.equals(Constants.LOGIN_SUCCESS_ACTION)) {
                mButton_Login.setVisibility(View.GONE);
                mButton_Register.setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        homeView = inflater.inflate(R.layout.story_home, null);
        homeDesView = inflater.inflate(R.layout.story_flow_description, null);

        setContentView(homeView);

        mImageCache = new MemoryImageCache();

        mListView = (ListView) homeDesView.findViewById(R.id.mListView);
        mDescriptionAdapter = new StoryFlowHomeDescriptionAdapter(this, mImageCache, mListView, false);
        mListView.setAdapter(mDescriptionAdapter);
//        mListView.setOnScrollListener(this);

        mButtonBack = (Button) homeDesView.findViewById(R.id.btnBack);
        mTextViewStoryTitle = (TextView) homeDesView.findViewById(R.id.tvStoryTitle);
        mButtonCamera = (Button) homeDesView.findViewById(R.id.btnCamera);
        mButtonBack.setOnClickListener(this);
        mButtonCamera.setOnClickListener(this);

        mlam = new LocalActivityManager(this, false);
        tabHost = (TabHost) homeView.findViewById(R.id.tabhost);
        // tabHost = this.getTabHost();
        mlam.dispatchCreate(savedInstanceState);
        tabHost.setup(mlam);

        TabSpec tab1 = tabHost.newTabSpec("today_hottest");
        tab1.setIndicator(createTabView(R.layout.story_left_subtab,
                getString(R.string.story_home_tab1)));
        tab1.setContent(new Intent(this, TodayHotActivity.class));
        tabHost.addTab(tab1);

        TabSpec tab2 = tabHost.newTabSpec("week_hottest");
        tab2.setIndicator(createTabView(R.layout.story_middle_subtab,
                getString(R.string.story_home_tab2)));
        tab2.setContent(new Intent(this, WeekHotActivity.class));
        tabHost.addTab(tab2);

        TabSpec tab3 = tabHost.newTabSpec("Latest stories");
        tab3.setIndicator(createTabView(R.layout.story_right_subtab,
                getString(R.string.story_home_tab3)));
        tab3.setContent(new Intent(this, LatestStoryActivity.class));
        tabHost.addTab(tab3);

        Bundle extras = getIntent().getExtras();

        if (null != extras) {
            isLogin = extras.getBoolean("isLogin");
        }

        /*
         * mBottomLayout = (RelativeLayout) findViewById(R.id.bottomLayout); if
         * (isLogin) { mBottomLayout.setVisibility(View.GONE); }
         */

        mButton_Register = (Button) homeView.findViewById(R.id.btn_register);
        mButton_Login = (Button) homeView.findViewById(R.id.btn_login);

        if (isLogin) {
            mButton_Register.setVisibility(View.GONE);
            mButton_Login.setVisibility(View.GONE);
        }

        mButton_Register.setOnClickListener(this);
        mButton_Login.setOnClickListener(this);

        IntentFilter filter = new IntentFilter(Constants.CHANGE_STORY_HOME_DES_CONTENT_VIEW_ACTION);
        registerReceiver(mReceiver, filter);
        
        IntentFilter loginFilter = new IntentFilter(Constants.LOGIN_SUCCESS_ACTION);
        registerReceiver(mLoginReceiver, loginFilter);
        
        

    }

    private View createTabView(int id, String title) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(id, null);
        TextView tv = (TextView) view.findViewById(R.id.tab_textview_title);
        tv.setText(title);
        return view;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {

            if (isDescriptionView) {
                setContentView(homeView);
                isDescriptionView = false;
            } else {
                exit();
            }

            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onPause() {
        mlam.dispatchPause(isFinishing());
        super.onPause();
    }

    @Override
    protected void onResume() {
        mlam.dispatchResume();
        StoryLogger.e("onResume", TAG + "onResume");
        validate(StoryHomeActivity.this);
        super.onResume();
    }

    public void validate(Context context) {

        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String phone_imei = telephonyManager.getDeviceId();
        DesEncrypt des = new DesEncrypt(phone_imei);

        SharedPreferences prefs = context.getSharedPreferences(Constants.SESSION_PREFS, 0);
        String sessionId = des.getDesString(prefs.getString(Constants.SESSION_ID, ""));
        String appType = des.getDesString(prefs.getString(Constants.APP_TYPE, ""));
        String openid = des.getDesString(prefs.getString(Constants.OPEN_ID, ""));
        String openkey = des.getDesString(prefs.getString(Constants.OPEN_KEY, ""));
        String sessionKey = des.getDesString(prefs.getString(Constants.SESSION_KEY, ""));
        String sessionSecret = des.getDesString(prefs.getString(Constants.SESSION_SECRET, ""));
        String token = des.getDesString(prefs.getString(Constants.TOKEN, ""));
        String uid = des.getDesString(prefs.getString(Constants.UID, ""));
        String expire_in = des.getDesString(prefs.getString(Constants.EXPIRE_IN, ""));
        String imei = des.getDesString(prefs.getString(Constants.DEVICE_ID, ""));
        String validity_time = des.getDesString(prefs.getString(Constants.VALIDITY_TIME, ""));

        UserInfo userInfo = new UserInfo();
        userInfo.sessionId = sessionId;
        userInfo.appType = appType;
        userInfo.openid = openid;
        userInfo.openkey = openkey;
        userInfo.sessionKey = sessionKey;
        userInfo.sessionSecret = sessionSecret;
        userInfo.token = token;
        userInfo.uid = uid;

        if (imei.equals(phone_imei) && !TextUtils.isEmpty(userInfo.sessionId)
                && !TextUtils.isEmpty(token) && !TextUtils.isEmpty(userInfo.uid)) {

            if (Utility.isSessionValid(validity_time)) {
                StoryFlowApp.getInstance().userInfo = userInfo;
                mButton_Login.setVisibility(View.GONE);
                mButton_Register.setVisibility(View.GONE);

            } else {
                mButton_Register.setVisibility(View.VISIBLE);
                mButton_Login.setVisibility(View.VISIBLE);
            }
        } else {
            mButton_Register.setVisibility(View.VISIBLE);
            mButton_Login.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);
        unregisterReceiver(mLoginReceiver);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_register:
                startActivityForResult(new Intent(StoryHomeActivity.this, LoginActivity.class),
                        REQUEST_CODE);
                break;
            case R.id.btn_login:
                startActivityForResult(new Intent(StoryHomeActivity.this, LoginActivity.class),
                        REQUEST_CODE);
                break;
            case R.id.btnBack:
                if (isDescriptionView) {
                    setContentView(homeView);
                    isDescriptionView = false;
                }
                break;
            case R.id.btnCamera:
                /**
                 * 拍照
                 */
                if (TextUtils.isEmpty(StoryFlowApp.getInstance().userInfo.sessionId)) {
                    Intent intent = new Intent(StoryHomeActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(StoryHomeActivity.this, StoryCameraActivity.class);
                    intent.putExtra("storyTitle", mTextViewStoryTitle.getText().toString());
                    intent.putExtra("storyId", mCurrentStoryId);
                    startActivity(intent);
                }

                break;

            default:
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // mBottomLayout.setVisibility(View.GONE);
                mButton_Login.setVisibility(View.GONE);
                mButton_Register.setVisibility(View.GONE);
            }
        } else if (requestCode == 2) {
            if (resultCode == OAuthV2AuthorizeWebView.RESULT_CODE) {
                Bundle bundle = data.getExtras();
                if (bundle !=null) {
                    OAuthV2 qOAuthV2 = (OAuthV2) bundle.getSerializable("oauth");
                    String downloadURL = StoryFlowHomeDescriptionAdapter.mdownloadURL;
                    StoryLogger.e(TAG, TAG+"---------->"+downloadURL);
                    AlbumInfo mAlbumInfo = StoryFlowHomeDescriptionAdapter.mShareAlbumInfo;
                    new QqDownloadAsyncTask(StoryHomeActivity.this, qOAuthV2, mAlbumInfo)
                    .execute(new Object[] {
                            downloadURL
                    });
                    StoryFlowHomeDescriptionAdapter.mdownloadURL = "";
                    StoryFlowHomeDescriptionAdapter.mShareAlbumInfo = null;
                    /**
                     * 更新本地保存值
                     */
                    Utility.updateQQOAuthPrefs(StoryHomeActivity.this, qOAuthV2);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void exit() {

        AlertDialog.Builder builder = new AlertDialog.Builder(StoryHomeActivity.this);
        builder.setCancelable(true);
        builder.setTitle(R.string.confirm_exit);

        builder.setPositiveButton(R.string.ok_label,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        finish();
                    }
                });
        builder.setNegativeButton(R.string.cancel_label,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(true);
        try {
            alert.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void afterServiceConnected() {

    }

    @Override
    public void handleSeviceResult(String err_msg, int eventId, Object rlt) {

        switch (eventId) {
            case Constants.STORY_PIC_LIST_SUCCESS: {

                Object[] obj = (Object[]) rlt;
                ArrayList<AlbumInfo> data = (ArrayList<AlbumInfo>) obj[0];
                StoryLogger.e(TAG, TAG + ":size" + data.size());

                mDescriptionAdapter.setData(data);
                mDescriptionAdapter.notifyDataSetChanged();

                /**
                 * 加载大图
                 */
                /*
                 * if (task != null && task.getStatus() ==
                 * AsyncTask.Status.RUNNING) { task.cancel(true); } if
                 * (mDescriptionAdapter.getCount() < 1) { task = new
                 * StoryDesBitmapLoadAsyncTask(StorySelfActivity.this,
                 * mDescriptionAdapter, 0, mDescriptionAdapter.getCount(),
                 * mImageCache); } else { task = new
                 * StoryDesBitmapLoadAsyncTask(StorySelfActivity.this,
                 * mDescriptionAdapter, 0, 1, mImageCache); } task.execute();
                 */

                /**
                 * 加载头像
                 */

                break;
            }
            case Constants.STORY_PIC_LIST_FAILED: {

                break;
            }
            case Constants.STORY_PIC_LIST_NET_INTERRUPT: {

                break;
            }

            case Constants.STORY_DETAIL_INFO_SUCCESS: {

                Object[] obj = (Object[]) rlt;
                StoryInfo info = (StoryInfo) obj[0];
                mTextViewStoryTitle.setText(info.title);
                StoryFlowApp.getInstance().storyTitle = info.title;

                break;
            }
            case Constants.STORY_DETAIL_INFO_FAILED: {

                break;
            }
            case Constants.STORY_DETAIL_INFO_NET_INTERRUPT: {

                break;
            }

            case Constants.STORY_LIKED_INFO_LIST_SUCCESS: {

                /**
                 * 用于判断该图片是否已经喜欢
                 */
                Object[] obj = (Object[]) rlt;
                ArrayList<String> mPicIdList = (ArrayList<String>) obj[0];
                AlbumInfo albumInfo = null;
                for (int i = 0; i < mOriginalData.size(); i++) {
                    albumInfo = mOriginalData.get(i);
                    if (mPicIdList.contains(albumInfo.id)) {
                        albumInfo.isLiked = true;
                    } else {
                        albumInfo.isLiked = false;
                    }
                }

                mDescriptionAdapter.notifyDataSetChanged();

                break;
            }
            case Constants.STORY_LIKED_INFO_LIST_FAILED: {

                break;
            }
            case Constants.STORY_LIKED_INFO_LIST_NET_INTERRUPT: {

                break;
            }

            default:
                break;
        }

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            if (headBitmapLoadAsyncTask != null
                    && headBitmapLoadAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                headBitmapLoadAsyncTask.cancel(true);
            }
            headBitmapLoadAsyncTask = new StoryHomeDesHeadBitmapLoadAsyncTask(
                    StoryHomeActivity.this,
                    mDescriptionAdapter, mStartIndex,
                    mEndIndex, mImageCache);
            headBitmapLoadAsyncTask.execute();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        mStartIndex = firstVisibleItem;
        mEndIndex = firstVisibleItem + visibleItemCount;
        if (mEndIndex >= totalItemCount) {
            mEndIndex = totalItemCount;
        }

    }

    public void refreshHead() {
        if (headBitmapLoadAsyncTask != null
                && headBitmapLoadAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            headBitmapLoadAsyncTask.cancel(true);
        }
        if (mDescriptionAdapter.getCount() < 1) {
            headBitmapLoadAsyncTask = new StoryHomeDesHeadBitmapLoadAsyncTask(
                    StoryHomeActivity.this, mDescriptionAdapter, 0,
                    mDescriptionAdapter.getCount(), mImageCache);
        } else {
            headBitmapLoadAsyncTask = new StoryHomeDesHeadBitmapLoadAsyncTask(
                    StoryHomeActivity.this, mDescriptionAdapter, 0, 1,
                    mImageCache);
        }
        headBitmapLoadAsyncTask.execute();
    }

}

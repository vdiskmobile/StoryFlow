package com.youqude.storyflow.ui;

import com.tencent.weibo.oauthv2.OAuthV2;
import com.tencent.weibo.webview.OAuthV2AuthorizeWebView;
import com.youqude.storyflow.R;
import com.youqude.storyflow.StoryFlowApp;
import com.youqude.storyflow.StoryFlowEventHandler;
import com.youqude.storyflow.StoryAPI.PlatformType;
import com.youqude.storyflow.adapter.StoryFlowDescriptionAdapter;
import com.youqude.storyflow.adapter.StoryFlowHomeDescriptionAdapter;
import com.youqude.storyflow.domain.AlbumInfo;
import com.youqude.storyflow.domain.MemoryImageCache;
import com.youqude.storyflow.domain.StoryInfo;
import com.youqude.storyflow.domain.StoryUserProfileInfo;
import com.youqude.storyflow.domain.StoryUserTabCountInfo;
import com.youqude.storyflow.net.BitmapLoadAsyncTask;
import com.youqude.storyflow.net.QqDownloadAsyncTask;
import com.youqude.storyflow.net.StoryDesBitmapLoadAsyncTask;
import com.youqude.storyflow.net.StoryDesHeadBitmapLoadAsyncTask;
import com.youqude.storyflow.net.StoryHomeDesHeadBitmapLoadAsyncTask;
import com.youqude.storyflow.utils.Constants;
import com.youqude.storyflow.utils.StoryLogger;
import com.youqude.storyflow.utils.Utility;

import android.app.AlertDialog;
import android.app.LocalActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;

public class StorySelfActivity extends BaseActivity implements StoryFlowEventHandler,
        OnClickListener , OnScrollListener{

    
    private static final String TAG = StorySelfActivity.class.getSimpleName();
   
    
    TextView mTextViewNickName;
    ImageView mImageViewHead;
    Button mButton_Setting;
    Button mButton_Self;
    
    
    Button mButtonBack;
    TextView mTextViewStoryTitle;
    Button mButtonCamera;
    ListView mListView;
    StoryFlowDescriptionAdapter mDescriptionAdapter;
    private ArrayList<AlbumInfo> mOriginalData;
    
    private MemoryImageCache mImageCache;
    private StoryDesHeadBitmapLoadAsyncTask headBitmapLoadAsyncTask;
    private int mStartIndex;
    private int mEndIndex;
    
    
    private StoryHomeDesHeadBitmapLoadAsyncTask headFlowBitmapLoadAsyncTask;
    StoryFlowHomeDescriptionAdapter mFlowDescriptionAdapter;
    
    
    View view1;
    View view2;
    boolean isDescriptionView;
    
    
    private TabHost tabHost;
    private LocalActivityManager mlam;
    
    
    TextView tvPic;
    TextView tvStory;
    TextView tvLove;

    String userId;
    
    String storyId;
    
    int pageNum = 1;
    int pageSize = 50;
    
    private String mCurrentStoryId;
    
    private static final int LOAD_HEAD_IMAGE_SUCCESS = 0x0;
    private static final int LOAD_HEAD_IMAGE_FAILED = 0x1;
    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            
            switch (msg.what) {
                case LOAD_HEAD_IMAGE_SUCCESS:
                    
                    /**
                     * 设置头像
                     */
                    Bitmap bitmap = (Bitmap) msg.obj;
                    bitmap = Utility.toRoundCorner(bitmap, 90);
                    mImageViewHead.setImageBitmap(bitmap);
                    break;
                case LOAD_HEAD_IMAGE_FAILED:
                    /**
                     * 设置默认头像
                     */
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };
    
    
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(Constants.CHANGE_STORY_CONTENT_VIEW_ACTION)) {
                setContentView(view2);
                isDescriptionView = true;
                
                Bundle extras = intent.getExtras();
                if (extras !=null) {
//                    ArrayList<AlbumInfo> data = (ArrayList<AlbumInfo>) extras.getSerializable("data");
                    String mStoryId = extras.getString("mStoryId");
                    StoryLogger.e(TAG, TAG+":"+mStoryId);
                    mCurrentStoryId = mStoryId;
                    if (!TextUtils.isEmpty(mStoryId)) {
                        mService.getStoryPicById(StorySelfActivity.this, mStoryId, pageNum, pageSize);
                        mService.getStoryTitleByStoryId(StorySelfActivity.this, mStoryId);
                    }
                }
            }
        }
    };
    
    private BroadcastReceiver mFlowReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(Constants.CHANGE_STORY_SELF_DES_CONTENT_VIEW_ACTION)) {
                StoryLogger.e(TAG, TAG + "--->" + action);

                setContentView(view2);
                isDescriptionView = true;

                Bundle extras = intent.getExtras();
                if (extras != null) {
                    ArrayList<AlbumInfo> data = (ArrayList<AlbumInfo>) extras
                            .getSerializable("data");
                    String mStoryId = extras.getString("mStoryId");
                    int index = extras.getInt("index");
                    StoryLogger.e(TAG, TAG + ":" + mStoryId);
                    if (!TextUtils.isEmpty(mStoryId)) {

//                        mCurrentStoryId = mStoryId;
                        
                        refreshHead();
                        mListView.setAdapter(mFlowDescriptionAdapter);
                        mFlowDescriptionAdapter.setData(data);
                        mFlowDescriptionAdapter.notifyChanged(userId);
                        mListView.setSelection(index);
                        /*
                         * mService.getStoryPicById(StoryHomeActivity.this,
                         * mStoryId, pageNum, pageSize);
                         */
                        mService.getStoryTitleByStoryId(StorySelfActivity.this, mStoryId);
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
                Bundle extras = intent.getExtras();
                if (extras !=null) {
                    
                    String uid = extras.getString("userId");
                    
                    userId = uid;
                    
                    mService.loadUserProfile(StorySelfActivity.this, uid);
                }
            }
            
        }
    };
    
    
    private BroadcastReceiver mChangeUserReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(Constants.CHANGE_USER_ACTION)) {
                /**
                 * 看他人信息
                 */
                Bundle bundle = intent.getExtras();
                if (bundle !=null) {
                    userId = bundle.getString("mUserId");
                    StoryLogger.e(TAG, TAG+"---->receiver:"+ userId);
                    
                    if (userId.equals(StoryFlowApp.getInstance().userInfo.uid)) {
                        mButton_Self.setVisibility(View.GONE);
                        mButton_Setting.setVisibility(View.VISIBLE);
                    } else {
                        mButton_Setting.setVisibility(View.GONE);
                        mButton_Self.setVisibility(View.VISIBLE);
                    }
                    
                    mBaseHandler.sendEmptyMessage(START_PROGRESS_DIALOG);
                    mService.loadUserProfile(StorySelfActivity.this, userId);
                }
            }
            
        }
    };
    private BroadcastReceiver mEnterOthersReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            
            String action = intent.getAction();
            if (action.equals(Constants.ENTER_OTHERS_BY_NICKNAME_ACTION)) {
                
                setContentView(view1);
                isDescriptionView = false;
                //清数据
                mDescriptionAdapter.clearAdapter();
                mFlowDescriptionAdapter.clearAdapter();
                StoryFlowApp.getInstance().storyTitle = "";
                
                /**
                 * 看他人信息
                 */
                Bundle bundle = intent.getExtras();
                if (bundle !=null) {
                    userId = bundle.getString("currentUserId");
                    StoryLogger.e(TAG, TAG+"---->receiver:"+ userId);
                    
                    if (userId.equals(StoryFlowApp.getInstance().userInfo.uid)) {
                        mButton_Self.setVisibility(View.GONE);
                        mButton_Setting.setVisibility(View.VISIBLE);
                    } else {
                        mButton_Setting.setVisibility(View.GONE);
                        mButton_Self.setVisibility(View.VISIBLE);
                    }
                    
                    mBaseHandler.sendEmptyMessage(START_PROGRESS_DIALOG);
                    mService.loadUserProfile(StorySelfActivity.this, userId);
                }
            }
            
        }
    };
    private BroadcastReceiver mEnterOthersByPicReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            
            String action = intent.getAction();
            if (action.equals(Constants.ENTER_OTHERS_BY_NICKNAME_PIC_ACTION)) {
                
                setContentView(view1);
                isDescriptionView = false;
                //清数据
                mDescriptionAdapter.clearAdapter();
                mFlowDescriptionAdapter.clearAdapter();
                StoryFlowApp.getInstance().storyTitle = "";
                
                /**
                 * 看他人信息
                 */
                Bundle bundle = intent.getExtras();
                if (bundle !=null) {
                    userId = bundle.getString("mCurrentUserId");
                    StoryLogger.e(TAG, TAG+"---->receiver:"+ userId);
                    
                    if (userId.equals(StoryFlowApp.getInstance().userInfo.uid)) {
                        mButton_Self.setVisibility(View.GONE);
                        mButton_Setting.setVisibility(View.VISIBLE);
                    } else {
                        mButton_Setting.setVisibility(View.GONE);
                        mButton_Self.setVisibility(View.VISIBLE);
                    }
                    
                    mBaseHandler.sendEmptyMessage(START_PROGRESS_DIALOG);
                    mService.loadUserProfile(StorySelfActivity.this, userId);
                }
            }
            
        }
    };
    
    private BroadcastReceiver mdeleteDataEmptyReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(Constants.USER_DELETE_DATA_EMPTY_ACTION)) {
                setContentView(view1);
                isDescriptionView = false;
                //清数据
                mDescriptionAdapter.clearAdapter();
                mFlowDescriptionAdapter.clearAdapter();
                StoryFlowApp.getInstance().storyTitle = "";
            }
            
        }
    };
    
    private boolean isCreate;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        
        view1 = inflater.inflate(R.layout.story_self, null);
        view2 = inflater.inflate(R.layout.story_self_description, null);
        setContentView(view1);
        
        mImageCache = new MemoryImageCache();
        
        
        mListView = (ListView) view2.findViewById(R.id.mListView);
        mDescriptionAdapter = new StoryFlowDescriptionAdapter(this, mImageCache, mListView);
        mFlowDescriptionAdapter = new StoryFlowHomeDescriptionAdapter(this, mImageCache, mListView, true);
        
        
        mButtonBack = (Button) view2.findViewById(R.id.btnBack);
        mTextViewStoryTitle = (TextView) view2.findViewById(R.id.tvStoryTitle);
        mButtonCamera = (Button) view2.findViewById(R.id.btnCamera);
        mButtonBack.setOnClickListener(this);
        mButtonCamera.setOnClickListener(this);
        
        
        IntentFilter intentFilter = new IntentFilter(Constants.CHANGE_STORY_CONTENT_VIEW_ACTION);
        registerReceiver(mReceiver, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter(Constants.CHANGE_STORY_SELF_DES_CONTENT_VIEW_ACTION);
        registerReceiver(mFlowReceiver, intentFilter2);
        
        if (!TextUtils.isEmpty(StoryFlowApp.getInstance().mChangedUserId)) {
            userId = StoryFlowApp.getInstance().mChangedUserId;
        } else {
            Bundle extras = getIntent().getExtras();
            if (extras !=null) {
                userId = extras.getString("mUserId");
            } else {
                userId = StoryFlowApp.getInstance().userInfo.uid;
            }
        }
        
        mImageViewHead = (ImageView) findViewById(R.id.iv_head);
        mTextViewNickName = (TextView) findViewById(R.id.tv_nick_name);
        mButton_Setting = (Button) findViewById(R.id.btnSetting);
        mButton_Setting.setOnClickListener(this);
        mButton_Self = (Button) findViewById(R.id.btnSelf);
        mButton_Self.setOnClickListener(this);
        

        mlam = new LocalActivityManager(this, false);
        tabHost = (TabHost) view1.findViewById(R.id.tabhost);
        mlam.dispatchCreate(savedInstanceState);
        tabHost.setup(mlam);

        TabSpec tab1 = tabHost.newTabSpec("self_pic");
        LayoutInflater inflater2 = LayoutInflater.from(this);
        View viewLeft = inflater2.inflate(R.layout.self_left_subtab, null);
        tvPic = (TextView) viewLeft.findViewById(R.id.tab_textview_title);
        tvPic.setText(getString(R.string.tab_pic_text));
        tab1.setIndicator(viewLeft);
        Intent intent = new Intent(this, SelfPicActivity.class);
        intent.putExtra("userId", userId);
        tab1.setContent(intent);
        tabHost.addTab(tab1);

        TabSpec tab2 = tabHost.newTabSpec("self_story_flow");
        LayoutInflater inflater3 = LayoutInflater.from(this);
        View view2 = inflater3.inflate(R.layout.self_middle_subtab, null);
        tvStory = (TextView) view2.findViewById(R.id.tab_textview_title);
        tvStory.setText(getString(R.string.tab_story_flow_text));
//        tab2.setIndicator(createTabView(getString(R.string.self_story_flow_tab2)));
        tab2.setIndicator(view2);
        Intent intent2 = new Intent(this, SelfStoryFlowActivity.class);
        intent2.putExtra("userId", userId);
        tab2.setContent(intent2);
        tabHost.addTab(tab2);

        TabSpec tab3 = tabHost.newTabSpec("self_love");
        LayoutInflater inflater4 = LayoutInflater.from(this);
        View view3 = inflater4.inflate(R.layout.self_right_subtab, null);
        tvLove = (TextView) view3.findViewById(R.id.tab_textview_title);
        tvLove.setText(getString(R.string.tab_love_text));
//        tab3.setIndicator(createTabView(getString(R.string.self_love_tab3)));
        tab3.setIndicator(view3);
        Intent intent3 = new Intent(this, SelfLoveActivity.class);
        intent3.putExtra("userId", userId);
        tab3.setContent(intent3);
        tabHost.addTab(tab3);

        
        IntentFilter filter = new IntentFilter(Constants.LOGIN_SUCCESS_ACTION);
        registerReceiver(mLoginReceiver, filter);
        
        IntentFilter changeUserfilter = new IntentFilter(Constants.CHANGE_USER_ACTION);
        registerReceiver(mChangeUserReceiver, changeUserfilter);
        
        IntentFilter mEnterOthersFilter = new IntentFilter(Constants.ENTER_OTHERS_BY_NICKNAME_ACTION);
        registerReceiver(mEnterOthersReceiver, mEnterOthersFilter);
        IntentFilter mEnterOthersByPicFilter = new IntentFilter(Constants.ENTER_OTHERS_BY_NICKNAME_PIC_ACTION);
        registerReceiver(mEnterOthersByPicReceiver, mEnterOthersByPicFilter);
        
        IntentFilter deleteFilter = new IntentFilter(Constants.USER_DELETE_DATA_EMPTY_ACTION);
        registerReceiver(mdeleteDataEmptyReceiver, deleteFilter);
        
        StoryLogger.e(TAG, TAG+"onCreate");
        isCreate = true;
    }

    
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            
            StoryLogger.e(TAG, TAG+":"+isDescriptionView);
            
            if (isDescriptionView) {
                setContentView(view1);
                isDescriptionView = false;
                //清数据
                mDescriptionAdapter.clearAdapter();
                mFlowDescriptionAdapter.clearAdapter();
                StoryFlowApp.getInstance().storyTitle = "";
            } else {
                exit();
            }
           
            
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
    
    private View createTabView(String title) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.subtab, null);
        TextView tv = (TextView) view.findViewById(R.id.tab_textview_title);
        tv.setText(title);
        return view;
    }
    
    
    @Override
    protected void onPause() {
        mlam.dispatchPause(isFinishing());
        super.onPause();
    }
    
    @Override
    protected void onResume() {
        mlam.dispatchResume();
        StoryLogger.e("onResume", TAG+"onResume");
        
        /**
         * 处理如果userId不是当前登录账号,则设置按钮改为"我的主页"显示,否则，显示"设置"按钮
         */
       StoryLogger.e(TAG, TAG+"----------userId:"+ StoryFlowApp.getInstance().userInfo.uid);
       StoryLogger.e(TAG, TAG+"----------userId:"+ userId);
       
       if (userId.equals(StoryFlowApp.getInstance().userInfo.uid)) {
           mButton_Self.setVisibility(View.GONE);
           mButton_Setting.setVisibility(View.VISIBLE);
       } else {
           mButton_Setting.setVisibility(View.GONE);
           mButton_Self.setVisibility(View.VISIBLE);
       }
       
        
        super.onResume();
    }
    
    @Override
    protected void onDestroy() {
        
        unregisterReceiver(mReceiver);
        unregisterReceiver(mFlowReceiver);
        unregisterReceiver(mLoginReceiver);
        unregisterReceiver(mChangeUserReceiver);
        unregisterReceiver(mdeleteDataEmptyReceiver);
        unregisterReceiver(mEnterOthersReceiver);
        unregisterReceiver(mEnterOthersByPicReceiver);
        
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        if (requestCode == 2) {
            if (resultCode == OAuthV2AuthorizeWebView.RESULT_CODE) {
                Bundle bundle = data.getExtras();
                if (bundle !=null) {
                    OAuthV2 qOAuthV2 = (OAuthV2) bundle.getSerializable("oauth");
                    
                    String downloadURL = null;
                    AlbumInfo mAlbumInfo = null;
                    if (StoryFlowDescriptionAdapter.mFromSelfFlow) {
                        downloadURL = StoryFlowHomeDescriptionAdapter.mdownloadURL;
                        StoryLogger.e(TAG, TAG+"-----FLOW----->"+downloadURL);
                        mAlbumInfo = StoryFlowHomeDescriptionAdapter.mShareAlbumInfo;
                    } else {
                        downloadURL = StoryFlowDescriptionAdapter.mdownloadURL;
                        StoryLogger.e(TAG, TAG+"---------->"+downloadURL);
                        mAlbumInfo = StoryFlowDescriptionAdapter.mShareAlbumInfo;
                    }
                    
                    
                    new QqDownloadAsyncTask(StorySelfActivity.this, qOAuthV2, mAlbumInfo)
                    .execute(new Object[] {
                            downloadURL
                    });
                    StoryFlowHomeDescriptionAdapter.mdownloadURL = "";
                    StoryFlowHomeDescriptionAdapter.mShareAlbumInfo = null;
                    StoryFlowDescriptionAdapter.mFromSelfFlow = true;
                    /**
                     * 更新本地保存值
                     */
                    Utility.updateQQOAuthPrefs(StorySelfActivity.this, qOAuthV2);
                }
            }
        }
        
        
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    protected void afterServiceConnected() {

        StoryLogger.e(TAG, TAG+"afterServiceConnected");
        
        if (Utility.isNetworkAvailable(StorySelfActivity.this)
                && !TextUtils.isEmpty(StoryFlowApp.getInstance().userInfo.sessionId)) {

            mBaseHandler.sendEmptyMessage(START_PROGRESS_DIALOG);
            mService.loadUserProfile(StorySelfActivity.this, userId);
            // mService.loadSelfCount(StorySelfActivity.this,userId);
        }
        
        
    }
    
    @Override
    public void handleSeviceResult(String err_msg, int eventId, Object rlt) {
        mBaseHandler.sendEmptyMessage(END_PROGRESS_DIALOG);
        switch (eventId) {
            case Constants.STORY_USER_PROFILE_SUCCESS:{
                
                Object[] obj = (Object[]) rlt;
                final StoryUserProfileInfo info = (StoryUserProfileInfo) obj[0];
                StoryLogger.e(TAG, TAG+":"+info.nickName+"--->"+info.userAvatar);
                
                mTextViewNickName.setText(info.nickName);
                
                final String appType = StoryFlowApp.getInstance().userInfo.appType;
                    /**
                     * 异步调用，加载头像
                     */
                    new Thread(new Runnable() {
                        
                        @Override
                        public void run() {
                            
                            try {
                                if (appType.equals(PlatformType.SINA.toString())) {
                                    info.userAvatar = info.userAvatar.replace("/50/", "/180/");
                                }
                               
                                InputStream inputStream = Utility
                                        .getInputStream(info.userAvatar);
                                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                Message msg = new Message();
                                if (bitmap !=null) {
                                    msg.what = LOAD_HEAD_IMAGE_SUCCESS;
                                    msg.obj = bitmap;
                                    mHandler.sendMessage(msg);
                                } else {
                                    msg.what = LOAD_HEAD_IMAGE_FAILED;
                                    mHandler.sendMessage(msg);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            
                        }
                    }).start();
                break;
            }
            case Constants.STORY_USER_PROFILE_FAILED:{
                Toast.makeText(StorySelfActivity.this,
                        getResources().getString(R.string.data_load_failed_toast),
                        Toast.LENGTH_SHORT).show();
                break;
            }
            case Constants.STORY_USER_PROFILE_NET_INTERRUPT:{
                Toast.makeText(StorySelfActivity.this,
                        getResources().getString(R.string.no_network_connection_toast),
                        Toast.LENGTH_SHORT).show();
                break;
            }
            
            case Constants.STORY_PIC_LIST_SUCCESS:{
                
                Object[] obj = (Object[]) rlt;
                ArrayList<AlbumInfo> data = (ArrayList<AlbumInfo>) obj[0];
                StoryLogger.e(TAG, TAG+":size"+data.size());
                mListView.setAdapter(mDescriptionAdapter);
                mDescriptionAdapter.setData(data);
                mDescriptionAdapter.notifyChanged(userId);
                
                mOriginalData = data;
                
                /**
                 * 异步调用，获取喜欢状态列表
                 */
                mService.loadLikedState(StorySelfActivity.this, mCurrentStoryId);
                
                /**
                 * 加载大图
                 */
               /* if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
                    task.cancel(true);
                }
                if (mDescriptionAdapter.getCount() < 1) {
                    task = new StoryDesBitmapLoadAsyncTask(StorySelfActivity.this, mDescriptionAdapter, 0,
                            mDescriptionAdapter.getCount(), mImageCache);
                } else {
                    task = new StoryDesBitmapLoadAsyncTask(StorySelfActivity.this, mDescriptionAdapter, 0, 1,
                            mImageCache);
                }
                task.execute();*/
                
                
                /**
                 * 加载头像
                 */
                
                try {
                    if (headBitmapLoadAsyncTask != null && headBitmapLoadAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                        headBitmapLoadAsyncTask.cancel(true);
                    }
                    if (mDescriptionAdapter.getCount() < 1) {
                        headBitmapLoadAsyncTask = new StoryDesHeadBitmapLoadAsyncTask(StorySelfActivity.this, mDescriptionAdapter, 0,
                                mDescriptionAdapter.getCount(), mImageCache);
                    } else {
                        headBitmapLoadAsyncTask = new StoryDesHeadBitmapLoadAsyncTask(StorySelfActivity.this, mDescriptionAdapter, 0, 1,
                                mImageCache);
                    }
                    headBitmapLoadAsyncTask.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                
                break;
            }
            case Constants.STORY_PIC_LIST_FAILED:{
                
                break;
            }
            case Constants.STORY_PIC_LIST_NET_INTERRUPT:{
                
                break;
            }
            
            case Constants.STORY_USER_TAB_COUNT_SUCCESS:{
                
                Object[] obj = (Object[]) rlt;
                StoryUserTabCountInfo userTabCountInfo = (StoryUserTabCountInfo) obj[0];
                
              /*  tvPic.setText(String.format(getString(R.string.self_pic_tab1), userTabCountInfo.picCount));
                tvStory.setText(String.format(getString(R.string.self_story_flow_tab2), userTabCountInfo.storyCount));
                tvLove.setText(String.format(getString(R.string.self_love_tab3), userTabCountInfo.likeCount));*/
                
                break;
            }
            case Constants.STORY_USER_TAB_COUNT_FAILED:{
                
                break;
            }
            case Constants.STORY_USER_TAB_COUNT_NET_INTERRUPT:{
                
                break;
            }
            
            case Constants.STORY_DETAIL_INFO_SUCCESS:{
                
                Object[] obj = (Object[]) rlt;
                StoryInfo info = (StoryInfo) obj[0];
                mTextViewStoryTitle.setText(info.title);
                StoryFlowApp.getInstance().storyTitle = info.title;
                
                break;
            }
            case Constants.STORY_DETAIL_INFO_FAILED:{
                
                break;
            }
            case Constants.STORY_DETAIL_INFO_NET_INTERRUPT:{
                
                break;
            }
            case Constants.STORY_LIKED_INFO_LIST_SUCCESS:{
                
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
                
                mDescriptionAdapter.notifyChanged(userId);
                
                break;
            }
            case Constants.STORY_LIKED_INFO_LIST_FAILED:{
                
                break;
            }
            case Constants.STORY_LIKED_INFO_LIST_NET_INTERRUPT:{
                
                break;
            }
            
            default:
                break;
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnBack:
                if (isDescriptionView) {
                    setContentView(view1);
                    isDescriptionView = false;
                    //清数据
                    mDescriptionAdapter.clearAdapter();
                    mFlowDescriptionAdapter.clearAdapter();
                    StoryFlowApp.getInstance().storyTitle = "";
                } 
                break;
            case R.id.btnCamera:
                /**
                 * 拍照
                 */
                Intent intent = new Intent(StorySelfActivity.this, StoryCameraActivity.class);
                intent.putExtra("storyTitle", mTextViewStoryTitle.getText().toString());
                intent.putExtra("storyId", mCurrentStoryId);
                startActivity(intent);
                
                
                break;
            case R.id.btnSetting:
                
                startActivity(new Intent(StorySelfActivity.this, StorySelfSettingActivity.class));
                
                break;
            case R.id.btnSelf:
                
                userId = StoryFlowApp.getInstance().userInfo.uid;
                Intent intent2 = new Intent(Constants.CHANGE_USER_ACTION);
                intent2.putExtra("mUserId", userId);
                sendBroadcast(intent2);
                
                break;
            default:
                break;
        }
        
    }
    
    
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            try {
                if (headBitmapLoadAsyncTask != null && headBitmapLoadAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                    headBitmapLoadAsyncTask.cancel(true);
                }
                headBitmapLoadAsyncTask = new StoryDesHeadBitmapLoadAsyncTask(StorySelfActivity.this, mDescriptionAdapter, mStartIndex,
                        mEndIndex, mImageCache);
                headBitmapLoadAsyncTask.execute();
                
                
                if (headBitmapLoadAsyncTask != null
                        && headBitmapLoadAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                    headBitmapLoadAsyncTask.cancel(true);
                }
                headFlowBitmapLoadAsyncTask = new StoryHomeDesHeadBitmapLoadAsyncTask(
                        StorySelfActivity.this,
                        mFlowDescriptionAdapter, mStartIndex,
                        mEndIndex, mImageCache);
                headFlowBitmapLoadAsyncTask.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
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
    
    public void refreshHead(){
        try {
            if (headFlowBitmapLoadAsyncTask != null
                    && headFlowBitmapLoadAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                headFlowBitmapLoadAsyncTask.cancel(true);
            }
            if (mFlowDescriptionAdapter.getCount() < 1) {
                headFlowBitmapLoadAsyncTask = new StoryHomeDesHeadBitmapLoadAsyncTask(
                        StorySelfActivity.this, mFlowDescriptionAdapter, 0,
                        mFlowDescriptionAdapter.getCount(), mImageCache);
            } else {
                headFlowBitmapLoadAsyncTask = new StoryHomeDesHeadBitmapLoadAsyncTask(
                        StorySelfActivity.this, mFlowDescriptionAdapter, 0, 1,
                        mImageCache);
            }
            headFlowBitmapLoadAsyncTask.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void exit(){

        AlertDialog.Builder builder = new AlertDialog.Builder(StorySelfActivity.this);
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


}

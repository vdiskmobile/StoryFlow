
package com.youqude.storyflow.ui;

import com.youqude.storyflow.R;
import com.youqude.storyflow.StoryFlowApp;
import com.youqude.storyflow.StoryFlowEventHandler;
import com.youqude.storyflow.adapter.ShowStorySelfLoveAdapter;
import com.youqude.storyflow.domain.LikeInfo;
import com.youqude.storyflow.domain.MemoryImageCache;
import com.youqude.storyflow.net.BitmapLoadAsyncTask;
import com.youqude.storyflow.net.BitmapLoveLoadAsyncTask;
import com.youqude.storyflow.utils.Constants;
import com.youqude.storyflow.utils.StoryLogger;
import com.youqude.storyflow.utils.Utility;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;

public class SelfLoveActivity extends BaseActivity implements StoryFlowEventHandler,
        OnScrollListener {

    private static final String TAG = SelfLoveActivity.class.getSimpleName();

    String userId;
    
    int pageNum = -1;
    int pageSize = 10;
    int recordSize;
    int totalPage;
    int mLastSavedTotalCount = -1;

    GridView mGridPicView;
    ShowStorySelfLoveAdapter mAdapter;
    ArrayList<LikeInfo> mData = new ArrayList<LikeInfo>();
    ArrayList<LikeInfo> data = new ArrayList<LikeInfo>();
    private MemoryImageCache mImageCache;

    BitmapLoveLoadAsyncTask task;
    private int mStartIndex;
    private int mEndIndex;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(Constants.RELEASE_STORY_SUCCESS_ACTION)) {

                if (Utility.isNetworkAvailable(SelfLoveActivity.this)) {
                    pageNum = 1;
                    mService.loadSelfLovePicList(SelfLoveActivity.this, userId, pageNum, pageSize);
                } else {
                    Toast.makeText(SelfLoveActivity.this,
                            getResources().getString(R.string.no_network_connection_toast),
                            Toast.LENGTH_SHORT).show();
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
                if (extras != null) {
                    pageNum = 1;
                    String uid = extras.getString("userId");
                    userId = uid;
                    mService.loadSelfLovePicList(SelfLoveActivity.this, uid, pageNum, pageSize);
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
                if (bundle != null) {
                    userId = bundle.getString("mUserId");
                    showLoadingDialog();
                    pageNum = 1;
                    mService.loadSelfLovePicList(SelfLoveActivity.this, userId, pageNum, pageSize);
                }
            }

        }
    };

    private BroadcastReceiver mdoLikeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(Constants.USER_DO_LIKE_SUCCESS_ACTION)) {
                if (Utility.isNetworkAvailable(SelfLoveActivity.this)) {
                    pageNum = 1;
                    mService.loadSelfLovePicList(SelfLoveActivity.this, userId, pageNum, pageSize);
                } else {
                    Toast.makeText(SelfLoveActivity.this,
                            getResources().getString(R.string.no_network_connection_toast),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private BroadcastReceiver mdeleteSuccessReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(Constants.USER_DELETE_SUCCESS_ACTION)) {
                pageNum = 1;
                mService.loadSelfLovePicList(SelfLoveActivity.this, userId, pageNum, pageSize);
            }
        }
    };

    private BroadcastReceiver mEnterReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(Constants.ENTER_OTHERS_BY_NICKNAME_ACTION)) {

                Bundle extras = intent.getExtras();
                if (extras != null) {
                    pageNum = 1;
                    userId = extras.getString("currentUserId");
                    mService.loadSelfLovePicList(SelfLoveActivity.this, userId, pageNum, pageSize);
                }
            }

        }
    };
    
    private BroadcastReceiver mEnterOthersByPicReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(Constants.ENTER_OTHERS_BY_NICKNAME_PIC_ACTION)) {

                Bundle extras = intent.getExtras();
                if (extras != null) {
                    userId = extras.getString("mCurrentUserId");
                    pageNum = 1;
                    mService.loadSelfLovePicList(SelfLoveActivity.this, userId, pageNum, pageSize);
                }
            }

        }
    };

    public ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.self_love);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getString("userId");
        } else {
            userId = StoryFlowApp.getInstance().userInfo.uid;
        }

        mImageCache = new MemoryImageCache();

        mGridPicView = (GridView) findViewById(R.id.mGridLoveView);
        mAdapter = new ShowStorySelfLoveAdapter(SelfLoveActivity.this, mImageCache);
        mGridPicView.setAdapter(mAdapter);

        mGridPicView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                StoryLogger.e(TAG, TAG + ":" + mData.get(position).id + "--->"
                        + mData.get(position).picPath);

                Intent intent = new Intent(Constants.CHANGE_STORY_CONTENT_VIEW_ACTION);
                intent.putExtra("mStoryId", mData.get(position).storyId);
                sendBroadcast(intent);

            }
        });
        
        mGridPicView.setOnScrollListener(this);

        IntentFilter intentFilter = new IntentFilter(Constants.RELEASE_STORY_SUCCESS_ACTION);
        registerReceiver(mReceiver, intentFilter);

        IntentFilter filter = new IntentFilter(Constants.LOGIN_SUCCESS_ACTION);
        registerReceiver(mLoginReceiver, filter);

        mProgressDialog = new ProgressDialog(this.getParent());
        mProgressDialog.setTitle(null);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(true);

        IntentFilter changeUserfilter = new IntentFilter(Constants.CHANGE_USER_ACTION);
        registerReceiver(mChangeUserReceiver, changeUserfilter);

        IntentFilter dolikeFilter = new IntentFilter(Constants.USER_DO_LIKE_SUCCESS_ACTION);
        registerReceiver(mdoLikeReceiver, dolikeFilter);

        IntentFilter deleteSuccessFilter = new IntentFilter(Constants.USER_DELETE_SUCCESS_ACTION);
        registerReceiver(mdeleteSuccessReceiver, deleteSuccessFilter);

        IntentFilter enterFilter = new IntentFilter(Constants.ENTER_OTHERS_BY_NICKNAME_ACTION);
        registerReceiver(mEnterReceiver, enterFilter);
        
        IntentFilter enterOtherByPicFilter = new IntentFilter(Constants.ENTER_OTHERS_BY_NICKNAME_PIC_ACTION);
        registerReceiver(mEnterOthersByPicReceiver, enterOtherByPicFilter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);
        unregisterReceiver(mLoginReceiver);
        unregisterReceiver(mChangeUserReceiver);
        unregisterReceiver(mdoLikeReceiver);
        unregisterReceiver(mdeleteSuccessReceiver);
        unregisterReceiver(mEnterReceiver);
        unregisterReceiver(mEnterOthersByPicReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        StoryLogger.e(TAG, TAG+"onResume");
        
    }
    
    @Override
    protected void afterServiceConnected() {

        if (Utility.isNetworkAvailable(SelfLoveActivity.this)) {
            
            StoryLogger.e(TAG, TAG+"-------->"+userId);
            
            if (TextUtils.isEmpty(userId)) {
                userId = StoryFlowApp.getInstance().userInfo.uid;
            }
            
            if (!TextUtils.isEmpty(userId)) {
                showLoadingDialog();
                pageNum = 1;
                mService.loadSelfLovePicList(SelfLoveActivity.this, userId, pageNum, pageSize);
            }
        } else {
            Toast.makeText(SelfLoveActivity.this,
                    getResources().getString(R.string.no_network_connection_toast),
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void handleSeviceResult(String err_msg, int eventId, Object rlt) {

        switch (eventId) {
            case Constants.STORY_PIC_LIST_SUCCESS: {

                /**
                 * 初始化图片对象数据源
                 */
                Object[] obj = (Object[]) rlt;
                data = (ArrayList<LikeInfo>) obj[0];
                mAdapter.setData(data);
                mAdapter.notifyChanged();
                break;
            }
            case Constants.STORY_PIC_LIST_FAILED: {
                Toast.makeText(SelfLoveActivity.this,
                        getResources().getString(R.string.data_load_failed_toast),
                        Toast.LENGTH_SHORT).show();
                break;
            }
            case Constants.STORY_PIC_LIST_NET_INTERRUPT: {
                Toast.makeText(SelfLoveActivity.this,
                        getResources().getString(R.string.no_network_connection_toast),
                        Toast.LENGTH_SHORT).show();
                break;
            }
            case Constants.STORY_LIKE_LIST_SUCCESS: {

                dismissLoadingDialog();

                /**
                 * 初始化图片对象数据源
                 */
                Object[] obj = (Object[]) rlt;
                ArrayList<LikeInfo> mArrayList = (ArrayList<LikeInfo>) obj[0];
                
                if (pageNum == 1) {
                    if (mData!=null && !mData.isEmpty()) {
                        mData.clear();
                    }
                }
                
                
                mData.addAll(mArrayList);

                recordSize = (Integer) obj[1];

                totalPage = recordSize / pageSize;
                
                mAdapter.setData(mData);
                mAdapter.notifyChanged();

                /*
                 * if (task != null && task.getStatus() ==
                 * AsyncTask.Status.RUNNING) { task.cancel(true); } if
                 * (mAdapter.getCount() < 18) { task = new
                 * BitmapLoveLoadAsyncTask(SelfLoveActivity.this, mAdapter, 0,
                 * mAdapter.getCount(), mImageCache); } else { task = new
                 * BitmapLoveLoadAsyncTask(SelfLoveActivity.this, mAdapter, 0,
                 * 18, mImageCache); } task.execute();
                 */

                break;
            }
            case Constants.STORY_LIKE_LIST_FAILED: {
                dismissLoadingDialog();
                Toast.makeText(SelfLoveActivity.this,
                        getString(R.string.no_network_connection_toast), Toast.LENGTH_SHORT).show();
                break;
            }
            case Constants.STORY_LIKE_LIST_NET_INTERRUPT: {
                dismissLoadingDialog();
                Toast.makeText(SelfLoveActivity.this,
                        getString(R.string.no_network_connection_toast), Toast.LENGTH_SHORT).show();
                break;
            }
            default:
                break;
        }
    }

    private void showLoadingDialog() {
        if (mProgressDialog != null && !mProgressDialog.isShowing()) {
            mProgressDialog.setMessage(getString(R.string.loading_waiting));
            try {
                mProgressDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void dismissLoadingDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            try {
                mProgressDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            /*
             * if (task != null && task.getStatus() == AsyncTask.Status.RUNNING)
             * { task.cancel(true); } task = new
             * BitmapLoveLoadAsyncTask(SelfLoveActivity.this, mAdapter,
             * mStartIndex, mEndIndex, mImageCache); task.execute();
             */
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
       /* mStartIndex = firstVisibleItem;
        mEndIndex = firstVisibleItem + visibleItemCount;
        if (mEndIndex >= totalItemCount) {
            mEndIndex = totalItemCount;
        }*/
        
        if (visibleItemCount > 0
                && (firstVisibleItem + visibleItemCount == totalItemCount)) {
            if (totalItemCount != mLastSavedTotalCount) {
                mLastSavedTotalCount = totalItemCount;
                if (pageNum < totalPage) {
                    // 加载下一页
                    pageNum++;
//                    showLoadingDialog();
                    Toast.makeText(SelfLoveActivity.this, getString(R.string.next_page_loading), Toast.LENGTH_SHORT).show();
                    mService.loadSelfLovePicList(SelfLoveActivity.this, userId, pageNum, pageSize);
                } else {
                    if (recordSize%pageSize !=0 && pageNum ==totalPage) {
                        pageNum++;
//                        showLoadingDialog();
                        Toast.makeText(SelfLoveActivity.this, getString(R.string.last_page_loading), Toast.LENGTH_SHORT).show();
                        mService.loadSelfLovePicList(SelfLoveActivity.this, userId, pageNum, pageSize);
                    }
                }
            }
        }

    }

}

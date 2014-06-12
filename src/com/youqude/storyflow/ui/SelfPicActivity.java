
package com.youqude.storyflow.ui;

import com.youqude.storyflow.R;
import com.youqude.storyflow.StoryFlowApp;
import com.youqude.storyflow.StoryFlowEventHandler;
import com.youqude.storyflow.adapter.ShowStoryPicAdapter;
import com.youqude.storyflow.domain.AlbumInfo;
import com.youqude.storyflow.domain.MemoryImageCache;
import com.youqude.storyflow.net.BitmapLoadAsyncTask;
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
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import java.util.ArrayList;

public class SelfPicActivity extends BaseActivity implements StoryFlowEventHandler,
        OnScrollListener{

    private static final String TAG = SelfPicActivity.class.getSimpleName();

    String userId;
    int pageNum = -1;
    int pageSize = 10;
    int recordSize;
    int totalPage;
    int mLastSavedTotalCount = -1;

    GridView mGridPicView;
    ShowStoryPicAdapter mAdapter;
    ArrayList<AlbumInfo> data = new ArrayList<AlbumInfo>();

    private MemoryImageCache mImageCache;
    private BitmapLoadAsyncTask task;
    private int mStartIndex;
    private int mEndIndex;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(Constants.RELEASE_STORY_SUCCESS_ACTION)) {

                if (Utility.isNetworkAvailable(SelfPicActivity.this)) {
                    pageNum = 1;
                    mService.loadSelfReleasePicList(SelfPicActivity.this, userId, pageNum, pageSize);
                } else {
                    Toast.makeText(SelfPicActivity.this,
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
                    mService.loadSelfReleasePicList(SelfPicActivity.this, uid, pageNum, pageSize);
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
                    mService.loadSelfReleasePicList(SelfPicActivity.this, userId, pageNum, pageSize);
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
                mService.loadSelfReleasePicList(SelfPicActivity.this, userId, pageNum, pageSize);
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
                    mService.loadSelfReleasePicList(SelfPicActivity.this, userId, pageNum, pageSize);
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
                    pageNum = 1;
                    userId = extras.getString("mCurrentUserId");
                    mService.loadSelfReleasePicList(SelfPicActivity.this, userId, pageNum, pageSize);
                }
            }

        }
    };

    public ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.self_pic);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getString("userId");
        }else {
            userId = StoryFlowApp.getInstance().userInfo.uid;
        }

        mImageCache = new MemoryImageCache();

        mGridPicView = (GridView) findViewById(R.id.mGridPicView);
        mAdapter = new ShowStoryPicAdapter(SelfPicActivity.this, mImageCache);
        mGridPicView.setAdapter(mAdapter);
        mGridPicView.setOnScrollListener(this);

        mGridPicView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                StoryLogger.e(TAG, TAG + ":" + data.get(position).id + "---->"
                        + data.get(position).picPath);

                Intent intent = new Intent(Constants.CHANGE_STORY_CONTENT_VIEW_ACTION);
                // intent.putExtra("data", data);
                StoryLogger.e(TAG, TAG + ":------>" + data.get(position).storyId);
                intent.putExtra("mStoryId", data.get(position).storyId);
                sendBroadcast(intent);
            }

        });

        IntentFilter intentFilter = new IntentFilter(Constants.RELEASE_STORY_SUCCESS_ACTION);
        registerReceiver(mReceiver, intentFilter);

        IntentFilter filter = new IntentFilter(Constants.LOGIN_SUCCESS_ACTION);
        registerReceiver(mLoginReceiver, filter);

        IntentFilter enterFilter = new IntentFilter(Constants.ENTER_OTHERS_BY_NICKNAME_ACTION);
        registerReceiver(mEnterReceiver, enterFilter);
        
        IntentFilter enterOthersByPicFilter = new IntentFilter(Constants.ENTER_OTHERS_BY_NICKNAME_PIC_ACTION);
        registerReceiver(mEnterOthersByPicReceiver, enterOthersByPicFilter);

        IntentFilter changeUserfilter = new IntentFilter(Constants.CHANGE_USER_ACTION);
        registerReceiver(mChangeUserReceiver, changeUserfilter);

        IntentFilter deleteSuccessFilter = new IntentFilter(Constants.USER_DELETE_SUCCESS_ACTION);
        registerReceiver(mdeleteSuccessReceiver, deleteSuccessFilter);

        mProgressDialog = new ProgressDialog(this.getParent());
        mProgressDialog.setTitle(null);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);
        unregisterReceiver(mLoginReceiver);
        unregisterReceiver(mChangeUserReceiver);
        unregisterReceiver(mdeleteSuccessReceiver);
        unregisterReceiver(mEnterReceiver);
        unregisterReceiver(mEnterOthersByPicReceiver);
    }

    @Override
    protected void afterServiceConnected() {

        if (Utility.isNetworkAvailable(SelfPicActivity.this)) {
            StoryLogger.e(TAG, TAG+"=====>"+userId);
            if (!TextUtils.isEmpty(userId)) {
                pageNum = 1;
                mService.loadSelfReleasePicList(SelfPicActivity.this, userId, pageNum, pageSize);
            }
        } else {
            Toast.makeText(SelfPicActivity.this,
                    getResources().getString(R.string.no_network_connection_toast),
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void handleSeviceResult(String err_msg, int eventId, Object rlt) {

        dismissLoadingDialog();

        switch (eventId) {
            case Constants.STORY_PIC_LIST_SUCCESS: {

                /**
                 * 初始化图片对象数据源
                 */
                Object[] obj = (Object[]) rlt;
                ArrayList<AlbumInfo> mArrayList = (ArrayList<AlbumInfo>) obj[0];
                
                if (pageNum == 1) {
                    if (data!=null && !data.isEmpty()) {
                        data.clear();
                    }
                }
                
                
                data.addAll(mArrayList);

                recordSize = (Integer) obj[1];

                totalPage = recordSize / pageSize;
                
                mAdapter.setData(data);
                mAdapter.notifyChanged();

                /*
                 * if (task != null && task.getStatus() ==
                 * AsyncTask.Status.RUNNING) { task.cancel(true); } if
                 * (mAdapter.getCount() < 18) { task = new
                 * BitmapLoadAsyncTask(SelfPicActivity.this, mAdapter, 0,
                 * mAdapter.getCount(), mImageCache); } else { task = new
                 * BitmapLoadAsyncTask(SelfPicActivity.this, mAdapter, 0, 18,
                 * mImageCache); } task.execute();
                 */

                break;
            }
            case Constants.STORY_PIC_LIST_FAILED: {
                Toast.makeText(SelfPicActivity.this,
                        getResources().getString(R.string.data_load_failed_toast),
                        Toast.LENGTH_SHORT).show();
                break;
            }
            case Constants.STORY_PIC_LIST_NET_INTERRUPT: {
                Toast.makeText(SelfPicActivity.this,
                        getResources().getString(R.string.no_network_connection_toast),
                        Toast.LENGTH_SHORT).show();
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
           /* if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
                task.cancel(true);
            }
            task = new BitmapLoadAsyncTask(SelfPicActivity.this, mAdapter,
                    mStartIndex, mEndIndex, mImageCache);*/
            // task.execute();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        
        if (visibleItemCount > 0
                && (firstVisibleItem + visibleItemCount == totalItemCount)) {
            if (totalItemCount != mLastSavedTotalCount) {
                mLastSavedTotalCount = totalItemCount;
                if (pageNum < totalPage) {
                    // 加载下一页
                    pageNum++;
//                    showLoadingDialog();
                    Toast.makeText(SelfPicActivity.this, getString(R.string.next_page_loading), Toast.LENGTH_SHORT).show();
                    mService.loadSelfReleasePicList(SelfPicActivity.this, userId, pageNum, pageSize);
                } else {
                    if (recordSize%pageSize !=0 && pageNum ==totalPage) {
                        pageNum++;
//                        showLoadingDialog();
                        Toast.makeText(SelfPicActivity.this, getString(R.string.last_page_loading), Toast.LENGTH_SHORT).show();
                        mService.loadSelfReleasePicList(SelfPicActivity.this, userId, pageNum, pageSize);
                    }
                }
            }
        }
        
        
        /*mStartIndex = firstVisibleItem;
        mEndIndex = firstVisibleItem + visibleItemCount;
        if (mEndIndex >= totalItemCount) {
            mEndIndex = totalItemCount;
        }*/

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

}

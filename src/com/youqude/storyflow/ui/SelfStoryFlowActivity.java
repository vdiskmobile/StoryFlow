
package com.youqude.storyflow.ui;

import com.youqude.storyflow.R;
import com.youqude.storyflow.StoryFlowApp;
import com.youqude.storyflow.StoryFlowEventHandler;
import com.youqude.storyflow.adapter.StoryFlowAdapter;
import com.youqude.storyflow.domain.AlbumInfo;
import com.youqude.storyflow.domain.StoryInfo;
import com.youqude.storyflow.utils.Constants;
import com.youqude.storyflow.utils.StoryLogger;
import com.youqude.storyflow.utils.Utility;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class SelfStoryFlowActivity extends BaseActivity implements StoryFlowEventHandler , OnScrollListener{

    private static final String TAG = SelfStoryFlowActivity.class.getSimpleName();

    String userId;
    
    int pageNum = -1;
    int pageSize = 10;
    int recordSize;
    int totalPage;
    int mLastSavedTotalCount = -1;

    ListView mListView;
    StoryFlowAdapter mAdapter;
    ArrayList<StoryInfo> mData = new ArrayList<StoryInfo>();

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(Constants.RELEASE_STORY_SUCCESS_ACTION)) {

                if (Utility.isNetworkAvailable(SelfStoryFlowActivity.this)) {
                    pageNum = 1;
                    mService.loadSelfStoryFlowList(SelfStoryFlowActivity.this, userId, pageNum,
                            pageSize);
                } else {
                    Toast.makeText(SelfStoryFlowActivity.this,
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
                    String uid = extras.getString("userId");
                    pageNum = 1;
                    userId = uid;
                    mService.loadSelfStoryFlowList(SelfStoryFlowActivity.this, uid, pageNum,
                            pageSize);
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
                    pageNum = 1;
                    userId = bundle.getString("mUserId");
                    mService.loadSelfStoryFlowList(SelfStoryFlowActivity.this, userId, pageNum,
                            pageSize);
                }
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
                    mService.loadSelfStoryFlowList(SelfStoryFlowActivity.this, userId, pageNum,
                            pageSize);
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
                    StoryLogger.e(TAG, TAG+"----------------------ssssssssssssssssss");
                    pageNum = 1;
                    userId = extras.getString("mCurrentUserId");
                    mService.loadSelfStoryFlowList(SelfStoryFlowActivity.this, userId, pageNum,
                            pageSize);
                }
            }

        }
    };

    public ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.self_flow_story);
        
        
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getString("userId");
        } else {
            userId = StoryFlowApp.getInstance().userInfo.uid;
        }
        

        mListView = (ListView) findViewById(R.id.self_flow_listview);
        mAdapter = new StoryFlowAdapter(this, mListView, "self");
        mListView.setAdapter(mAdapter);
        
        mListView.setOnScrollListener(this);
        

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

        IntentFilter enterFilter = new IntentFilter(Constants.ENTER_OTHERS_BY_NICKNAME_ACTION);
        registerReceiver(mEnterReceiver, enterFilter);
        
        IntentFilter enterOthersByPicFilter = new IntentFilter(Constants.ENTER_OTHERS_BY_NICKNAME_PIC_ACTION);
        registerReceiver(mEnterOthersByPicReceiver, enterOthersByPicFilter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);
        unregisterReceiver(mLoginReceiver);
        unregisterReceiver(mChangeUserReceiver);
        unregisterReceiver(mEnterReceiver);
        unregisterReceiver(mEnterOthersByPicReceiver);
    }

    @Override
    protected void afterServiceConnected() {

        if (Utility.isNetworkAvailable(SelfStoryFlowActivity.this)) {
            
            if (TextUtils.isEmpty(userId)) {
                userId = StoryFlowApp.getInstance().userInfo.uid;
            }
            
            if (!TextUtils.isEmpty(userId)) {
                showLoadingDialog();
                pageNum = 1;
                mService.loadSelfStoryFlowList(SelfStoryFlowActivity.this, userId, pageNum,
                        pageSize);
            }
        } else {
            Toast.makeText(SelfStoryFlowActivity.this,
                    getString(R.string.no_network_connection_toast), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void handleSeviceResult(String err_msg, int eventId, Object rlt) {

        switch (eventId) {
            case Constants.STORY_INFO_LIST_SUCCESS: {

                Object[] obj = (Object[]) rlt;

                ArrayList<StoryInfo> mArrayList = (ArrayList<StoryInfo>) obj[0];
                
                if (pageNum == 1) {
                    if (mData!=null && !mData.isEmpty()) {
                        mData.clear();
                    }
                }
                
                
                mData.addAll(mArrayList);

                recordSize = (Integer) obj[1];

                totalPage = recordSize / pageSize;
                
                
                
                mAdapter.setData(mData);
                mAdapter.notifyChange();
                if (mData != null && !mData.isEmpty()) {
                    mService.loadStoryItemInfo(SelfStoryFlowActivity.this, mData,
                            StoryFlowApp.getInstance().HorizontialPageNum,
                            StoryFlowApp.getInstance().HorizontialPageSize
                            );
                } else {
                    dismissLoadingDialog();
                }
                break;
            }
            case Constants.STORY_INFO_LIST_FAILED: {
                dismissLoadingDialog();
                Toast.makeText(SelfStoryFlowActivity.this,
                        getString(R.string.no_network_connection_toast), Toast.LENGTH_SHORT).show();
                break;
            }
            case Constants.STORY_INFO_LIST_NET_INTERRUPT: {
                dismissLoadingDialog();
                Toast.makeText(SelfStoryFlowActivity.this,
                        getString(R.string.no_network_connection_toast), Toast.LENGTH_SHORT).show();
                break;
            }

            case Constants.STORY_PIC_LIST_SUCCESS: {

                dismissLoadingDialog();
                Object[] obj = (Object[]) rlt;
                StoryFlowApp.getInstance().mHashMap = (HashMap<String, ArrayList<AlbumInfo>>) obj[0];

                StoryLogger.e(TAG,
                        TAG + ":mHashMap-->" + StoryFlowApp.getInstance().mHashMap.size());

                mAdapter.notifyChange();

                break;
            }
            case Constants.STORY_PIC_LIST_FAILED: {
                dismissLoadingDialog();
                Toast.makeText(SelfStoryFlowActivity.this,
                        getString(R.string.no_network_connection_toast), Toast.LENGTH_SHORT).show();
                break;
            }
            case Constants.STORY_PIC_LIST_NET_INTERRUPT: {
                dismissLoadingDialog();
                Toast.makeText(SelfStoryFlowActivity.this,
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
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {

        if (visibleItemCount > 0
                && (firstVisibleItem + visibleItemCount == totalItemCount)) {
            if (totalItemCount != mLastSavedTotalCount) {
                mLastSavedTotalCount = totalItemCount;
                if (pageNum < totalPage) {
                    // 加载下一页
                    pageNum++;
//                    showLoadingDialog();
                    Toast.makeText(SelfStoryFlowActivity.this, getString(R.string.next_page_loading), Toast.LENGTH_SHORT).show();
                    mService.loadSelfStoryFlowList(SelfStoryFlowActivity.this, userId, pageNum,
                            pageSize);
                } else {
                    if (recordSize%pageSize !=0 && pageNum ==totalPage) {
                        pageNum++;
//                        showLoadingDialog();
                        Toast.makeText(SelfStoryFlowActivity.this, getString(R.string.last_page_loading), Toast.LENGTH_SHORT).show();
                        mService.loadSelfStoryFlowList(SelfStoryFlowActivity.this, userId, pageNum,
                                pageSize);
                    }
                }
            }
        }
        
    }

}


package com.youqude.storyflow.ui;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
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
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class LatestStoryActivity extends BaseActivity implements StoryFlowEventHandler , OnRefreshListener, OnScrollListener{

    private static final String TAG = LatestStoryActivity.class.getSimpleName();

    int pageNum = -1;
    int pageSize = 20;
    int recordSize;
    int totalPage;
    int mLastSavedTotalCount = -1;

    
    PullToRefreshListView mPullToRefreshListView;
    ListView mListView;
    StoryFlowAdapter mAdapter;

    ArrayList<StoryInfo> mData = new ArrayList<StoryInfo>();

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(Constants.RELEASE_STORY_SUCCESS_ACTION)) {

                if (Utility.isNetworkAvailable(LatestStoryActivity.this)) {
                    pageNum = 1;
                    mService.getLatestHotStory(LatestStoryActivity.this, pageNum, pageSize);
                } else {
                    Toast.makeText(LatestStoryActivity.this,
                            getResources().getString(R.string.no_network_connection_toast),
                            Toast.LENGTH_SHORT).show();
                }
            }

        }
    };
    public ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.latest_hot_story);

        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.latest_listview);
        mListView = mPullToRefreshListView.getRefreshableView();
        mAdapter = new StoryFlowAdapter(this, mListView, "home");
        mListView.setAdapter(mAdapter);
        
        
        mPullToRefreshListView.setOnRefreshListener(this);
        mListView.setOnScrollListener(this);
        
        IntentFilter intentFilter = new IntentFilter(Constants.RELEASE_STORY_SUCCESS_ACTION);
        registerReceiver(mReceiver, intentFilter);
        
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
    }

    @Override
    protected void afterServiceConnected() {

        if (Utility.isNetworkAvailable(LatestStoryActivity.this)) {
            showLoadingDialog();
            pageNum = 1;
            mService.getLatestHotStory(LatestStoryActivity.this, pageNum, pageSize);
        } else {
            Toast.makeText(LatestStoryActivity.this,
                    getResources().getString(R.string.no_network_connection_toast),
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void handleSeviceResult(String err_msg, int eventId, Object rlt) {

        mPullToRefreshListView.onRefreshComplete();
        
        switch (eventId) {
            case Constants.STORY_INFO_LIST_SUCCESS: {

                Object[] obj = (Object[]) rlt;

                ArrayList<StoryInfo> mArrayList = (ArrayList<StoryInfo>) obj[0];
                
                recordSize = (Integer) obj[1];

                if (pageNum == 1) {
                    if (mData!=null && !mData.isEmpty()) {
                        mData.clear();
                    }
                }
                
                mData.addAll(mArrayList);
                
                StoryLogger.e(TAG, TAG+":recordSize:"+recordSize);
                
                totalPage = recordSize/pageSize;
                
                /*if (pageNum >= recordSize / pageSize) {
                    // 已经是最后一页，隐藏dialog
                    dismissLoadingDialog();
                }*/
                
                
                mAdapter.setData(mData);
                if (mData != null && !mData.isEmpty()) {
                    mService.loadStoryItemInfo(LatestStoryActivity.this, mData,
                            StoryFlowApp.getInstance().HorizontialPageNum,
                            StoryFlowApp.getInstance().HorizontialPageSize
                            );
                } else {
                    Toast.makeText(LatestStoryActivity.this, getString(R.string.data_is_null), Toast.LENGTH_SHORT).show();
                    dismissLoadingDialog();
                }

                break;
            }
            case Constants.STORY_INFO_LIST_FAILED: {
                dismissLoadingDialog();
                Toast.makeText(LatestStoryActivity.this, getString(R.string.no_network_connection_toast), Toast.LENGTH_SHORT).show();
                break;
            }
            case Constants.STORY_INFO_LIST_NET_INTERRUPT: {
                dismissLoadingDialog();
                Toast.makeText(LatestStoryActivity.this, getString(R.string.no_network_connection_toast), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(LatestStoryActivity.this, getString(R.string.no_network_connection_toast), Toast.LENGTH_SHORT).show();
                break;
            }
            case Constants.STORY_PIC_LIST_NET_INTERRUPT: {
                dismissLoadingDialog();
                Toast.makeText(LatestStoryActivity.this, getString(R.string.no_network_connection_toast), Toast.LENGTH_SHORT).show();
                break;
            }

            default:
                break;
        }
    }

    private void showLoadingDialog(){
        if (mProgressDialog !=null && !mProgressDialog.isShowing()) {
            mProgressDialog.setMessage(getString(R.string.loading_waiting));
            try {
                mProgressDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void dismissLoadingDialog(){
        if (mProgressDialog !=null && mProgressDialog.isShowing()) {
            try {
                mProgressDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public void onRefresh() {
        
        if (Utility.isNetworkAvailable(LatestStoryActivity.this)) {
            pageNum = 1;
            mService.getLatestHotStory(LatestStoryActivity.this, pageNum, pageSize);
        } else {
            mPullToRefreshListView.onRefreshComplete();
            Toast.makeText(LatestStoryActivity.this,
                    getResources().getString(R.string.no_network_connection_toast),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        
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
                    Toast.makeText(LatestStoryActivity.this, getString(R.string.next_page_loading), Toast.LENGTH_SHORT).show();
                    mService.getLatestHotStory(LatestStoryActivity.this, pageNum, pageSize);
                } else {
                    if (recordSize%pageSize !=0 && pageNum ==totalPage) {
                        pageNum++;
//                        showLoadingDialog();
                        Toast.makeText(LatestStoryActivity.this, getString(R.string.last_page_loading), Toast.LENGTH_SHORT).show();
                        mService.getLatestHotStory(LatestStoryActivity.this, pageNum, pageSize);
                    }
                }
                
            }
        }
        
    }

}

package com.youqude.storyflow.ui;

import com.youqude.storyflow.R;
import com.youqude.storyflow.StoryFlowEventHandler;
import com.youqude.storyflow.adapter.ShowStoryPicAdapter;
import com.youqude.storyflow.domain.AlbumInfo;
import com.youqude.storyflow.domain.MemoryImageCache;
import com.youqude.storyflow.net.BitmapLoadAsyncTask;
import com.youqude.storyflow.utils.Constants;
import com.youqude.storyflow.utils.StoryLogger;
import com.youqude.storyflow.utils.Utility;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ShowStoryPicActivity extends BaseActivity implements StoryFlowEventHandler , OnClickListener, OnScrollListener{

   private static final String TAG = ShowStoryPicActivity.class.getSimpleName();

   
   int pageNum = -1;
   int pageSize = 10;
   int recordSize;
   int totalPage;
   int mLastSavedTotalCount = -1;
   
   
    Button mButtonBack;
    TextView mTextViewTitle;
    GridView mGridView;
    ShowStoryPicAdapter mAdapter;
    
    String storyId;
    String title;
    
    
    ArrayList<AlbumInfo> data = new ArrayList<AlbumInfo>();
    
    
    private MemoryImageCache mImageCache;
    private BitmapLoadAsyncTask task;
    private int mStartIndex;
    private int mEndIndex;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.show_story_pic);
        
        mImageCache = new MemoryImageCache();
        
        mButtonBack = (Button) findViewById(R.id.btnBack);
        mButtonBack.setOnClickListener(this);
        
        mTextViewTitle = (TextView) findViewById(R.id.tvStoryTitle);
        
        mGridView = (GridView) findViewById(R.id.mGridPicView);
        mAdapter = new ShowStoryPicAdapter(ShowStoryPicActivity.this, mImageCache);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnScrollListener(this);
        
        Bundle extras = getIntent().getExtras();
        if (extras !=null) {
            storyId = extras.getString("storyId");
            title = extras.getString("title");
            mTextViewTitle.setText(title);
            StoryLogger.e(TAG, TAG+":"+storyId);
        }
        
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void afterServiceConnected() {

        StoryLogger.e(TAG, TAG+":afterServiceConnected");
        
        if (Utility.isNetworkAvailable(ShowStoryPicActivity.this)) {
            if (!TextUtils.isEmpty(storyId)) {
                mBaseHandler.sendEmptyMessage(START_PROGRESS_DIALOG);
                pageNum = 1;
                mService.getStoryPicById(ShowStoryPicActivity.this, storyId, pageNum, pageSize);
            }
        } else {
            Toast.makeText(ShowStoryPicActivity.this,
                    getResources().getString(R.string.no_network_connection_toast),
                    Toast.LENGTH_SHORT).show();
        }
        
    }
    
    @Override
    public void handleSeviceResult(String err_msg, int eventId, Object rlt) {

        mBaseHandler.sendEmptyMessage(END_PROGRESS_DIALOG);
        switch (eventId) {
            case Constants.STORY_PIC_LIST_SUCCESS:{
                
                /**
                 * 初始化图片对象数据源
                 */
                Object[] obj = (Object[]) rlt;
                ArrayList<AlbumInfo> mArrayList = (ArrayList<AlbumInfo>) obj[0];
                
                data.addAll(mArrayList);
                
                recordSize = (Integer) obj[1];
                
                totalPage = recordSize / pageSize;
                
                
                
                mAdapter.setData(data);
                mAdapter.notifyChanged();
                
                /*if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
                    task.cancel(true);
                }
                if (mAdapter.getCount() < 18) {
                    task = new BitmapLoadAsyncTask(ShowStoryPicActivity.this, mAdapter, 0,
                            mAdapter.getCount(), mImageCache);
                } else {
                    task = new BitmapLoadAsyncTask(ShowStoryPicActivity.this, mAdapter, 0, 18,
                            mImageCache);
                }
                task.execute();*/
                
                
                break;
            }
            case Constants.STORY_PIC_LIST_FAILED:{
                Toast.makeText(ShowStoryPicActivity.this,
                        getResources().getString(R.string.data_load_failed_toast),
                        Toast.LENGTH_SHORT).show();
                break;
            }
            case Constants.STORY_PIC_LIST_NET_INTERRUPT:{
                Toast.makeText(ShowStoryPicActivity.this,
                        getResources().getString(R.string.no_network_connection_toast),
                        Toast.LENGTH_SHORT).show();
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
                finish();
                break;

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
            task = new BitmapLoadAsyncTask(ShowStoryPicActivity.this, mAdapter,
                    mStartIndex, mEndIndex, mImageCache);*/
//            task.execute();
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
                    Toast.makeText(ShowStoryPicActivity.this, getString(R.string.next_page_loading), Toast.LENGTH_SHORT).show();
                    mService.getStoryPicById(ShowStoryPicActivity.this, storyId, pageNum, pageSize);                   
                } else {
                    if (recordSize%pageSize !=0 && pageNum ==totalPage) {
                        pageNum++;
//                        showLoadingDialog();
                        Toast.makeText(ShowStoryPicActivity.this, getString(R.string.last_page_loading), Toast.LENGTH_SHORT).show();
                        mService.getStoryPicById(ShowStoryPicActivity.this, storyId, pageNum, pageSize);
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

}

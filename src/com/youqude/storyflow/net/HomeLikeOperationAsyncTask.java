package com.youqude.storyflow.net;

import com.youqude.storyflow.R;
import com.youqude.storyflow.StoryAPI;
import com.youqude.storyflow.adapter.StoryFlowDescriptionAdapter;
import com.youqude.storyflow.adapter.StoryFlowHomeDescriptionAdapter;
import com.youqude.storyflow.domain.AlbumInfo;
import com.youqude.storyflow.exception.StatusCodeException;
import com.youqude.storyflow.exception.StoryFlowException;
import com.youqude.storyflow.utils.Constants;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.AsyncTask;
import android.widget.Toast;

public class HomeLikeOperationAsyncTask extends AsyncTask<Object[], Void, Boolean> {

    
    StoryFlowHomeDescriptionAdapter mAdapter;
    
    /*
     * 
     * 用于异步处理喜欢与取消喜欢操作
     */
    Context mContext;
    boolean isCancel;//是否取消喜欢
    
    String picId;
    AlbumInfo mAlbumInfo;
    
    private ProgressDialog mProgressDialog;
    
    public HomeLikeOperationAsyncTask(Context ctx, boolean isCancel, StoryFlowHomeDescriptionAdapter adapter, AlbumInfo albumInfo ){
        this.mContext = ctx;
        this.isCancel = isCancel;
        this.mAdapter = adapter;
        this.mAlbumInfo = albumInfo;
    }
    
    @Override
    protected void onPreExecute() {
        
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle(null);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(mContext.getString(R.string.operation_loading));
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(false);  
        try {
            mProgressDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        
        if (mProgressDialog !=null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        
        if (!result) {
            Toast.makeText(mContext, mContext.getString(R.string.operation_failed), Toast.LENGTH_SHORT).show();
        } else {
            if (isCancel) {
                Toast.makeText(mContext, mContext.getString(R.string.unlike_success), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, mContext.getString(R.string.like_success), Toast.LENGTH_SHORT).show();
            }
            
            //通知喜欢列表进行刷新
            Intent intent = new Intent(Constants.USER_DO_LIKE_SUCCESS_ACTION);
            mContext.sendBroadcast(intent);
        }
        
        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        
        mAdapter.notifyDataSetChanged();
        
        super.onProgressUpdate(values);
    }

    @Override
    protected Boolean doInBackground(Object[]... params) {
        
        picId = (String) params[0][0];
        
        boolean isOperationSuccess = false;
        try {
            if (isCancel) {
                isOperationSuccess = StoryAPI.cancelStoryFlowLikeAction(mContext,picId);
                if (isOperationSuccess) {
                    mAlbumInfo.isLiked = false;
                    publishProgress();
                }
            } else {
                isOperationSuccess = StoryAPI.startStoryFlowLikeAction(mContext,picId);
                if (isOperationSuccess) {
                    mAlbumInfo.isLiked = true;
                    publishProgress();
                }
            }
           
        } catch (StatusCodeException e) {
            e.printStackTrace();
        } catch (StoryFlowException e) {
            e.printStackTrace();
        }
        
        return isOperationSuccess;
    }

}

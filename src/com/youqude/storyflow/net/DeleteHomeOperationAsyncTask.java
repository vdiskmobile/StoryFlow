
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
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.ArrayList;

public class DeleteHomeOperationAsyncTask extends AsyncTask<Object[], Void, Boolean> {

    StoryFlowHomeDescriptionAdapter mAdapter;

    /*
     * 用于异步处理喜欢与取消喜欢操作
     */
    Context mContext;

    String picId;

    ArrayList<AlbumInfo> mAlbumInfos;
    AlbumInfo mAlbumInfo;

    private ProgressDialog mProgressDialog;

    public DeleteHomeOperationAsyncTask(Context ctx, StoryFlowHomeDescriptionAdapter adapter,
            ArrayList<AlbumInfo> data,
            AlbumInfo albumInfo) {
        this.mContext = ctx;
        this.mAdapter = adapter;
        this.mAlbumInfos = data;
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

        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }

        if (!result) {
            Toast.makeText(mContext, mContext.getString(R.string.operation_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(Constants.USER_DELETE_SUCCESS_ACTION);
            mContext.sendBroadcast(intent);
            Toast.makeText(mContext, mContext.getString(R.string.delete_success),
                    Toast.LENGTH_SHORT).show();
        }

        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(Void... values) {

        mAdapter.notifyDataSetChanged();

        if (mAlbumInfos.isEmpty()) {
            //当前操作界面消失
            Intent intent = new Intent(Constants.USER_DELETE_DATA_EMPTY_ACTION);
            mContext.sendBroadcast(intent);
        }
        
        super.onProgressUpdate(values);
    }

    @Override
    protected Boolean doInBackground(Object[]... params) {

        picId = (String) params[0][0];

        boolean isOperationSuccess = false;
        try {
            isOperationSuccess = StoryAPI.deleteStoryFlowLikeAction(mContext, picId);
            if (isOperationSuccess) {
                mAlbumInfos.remove(mAlbumInfo);
                publishProgress();
            }

        } catch (StatusCodeException e) {
            e.printStackTrace();
        } catch (StoryFlowException e) {
            e.printStackTrace();
        }

        return isOperationSuccess;
    }

}

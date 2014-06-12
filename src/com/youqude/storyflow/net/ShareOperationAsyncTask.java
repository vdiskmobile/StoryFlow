
package com.youqude.storyflow.net;

import com.youqude.storyflow.R;
import com.youqude.storyflow.StoryAPI;
import com.youqude.storyflow.StoryFlowApp;
import com.youqude.storyflow.adapter.StoryFlowDescriptionAdapter;
import com.youqude.storyflow.domain.AlbumInfo;
import com.youqude.storyflow.exception.StatusCodeException;
import com.youqude.storyflow.exception.StoryFlowException;
import com.youqude.storyflow.utils.Constants;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.ArrayList;

public class ShareOperationAsyncTask extends AsyncTask<Object[], Void, Boolean> {


    Context mContext;

    AlbumInfo mAlbumInfo;
    
    String access_token;
    String picPath;

    private ProgressDialog mProgressDialog;

    public ShareOperationAsyncTask(Context ctx, AlbumInfo albumInfo) {
        this.mContext = ctx;
        this.mAlbumInfo = albumInfo;
    }

    @Override
    protected void onPreExecute() {

        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle(null);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(mContext.getString(R.string.share_loading));
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
            try {
                mProgressDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!result) {
            Toast.makeText(mContext, mContext.getString(R.string.share_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.share_success),
                    Toast.LENGTH_SHORT).show();
        }

        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(Void... values) {

        super.onProgressUpdate(values);
    }

    @Override
    protected Boolean doInBackground(Object[]... params) {

        access_token = (String) params[0][0];
        picPath = (String) params[0][1];
        
        boolean isShareSuccess = false;

        try {
            
            
            String linkedURL = "http://www.gushiliu.com/story/show?pid="+mAlbumInfo.id+"&_t="+System.currentTimeMillis();
            String storyTitle = StoryFlowApp.getInstance().storyTitle;
            JSONObject jsonObject = StoryAPI.updateWithPicViaSina(mContext, access_token,
                    String.format(mContext.getString(R.string.sina_status_text), storyTitle , linkedURL), picPath);
            if (jsonObject != null) {
                String created_at = jsonObject.optString("created_at", null);
                if (created_at != null) {
                    isShareSuccess = true;
                } 
            } 
            
        } catch (StatusCodeException e) {
            e.printStackTrace();
        } catch (StoryFlowException e) {
            e.printStackTrace();
        } 

        return isShareSuccess;
    }

}

package com.youqude.storyflow.ui;

import com.youqude.storyflow.R;
import com.youqude.storyflow.StoryFlowDataService;
import com.youqude.storyflow.utils.StoryLogger;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

public abstract class BaseActivity extends Activity {

    
    private static String TAG = BaseActivity.class.getSimpleName();
    
    protected StoryFlowDataService mService;
    private Intent mFDServiceInent;
    public Context parentCtx;
    
    private ProgressDialog mProgressDialog;
    
    protected static final int START_PROGRESS_DIALOG  = 0x0;
    protected static final int END_PROGRESS_DIALOG  = 0x1;
    
    protected Handler mBaseHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            
            switch (msg.what) {
                case START_PROGRESS_DIALOG:
                    
                    if (mProgressDialog !=null && !mProgressDialog.isShowing()) {
                        mProgressDialog.setMessage(getString(R.string.loading_waiting));
                        try {
                            mProgressDialog.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    
                    break;
                case END_PROGRESS_DIALOG:
                    
                    if (mProgressDialog !=null && mProgressDialog.isShowing()) {
                        try {
                            mProgressDialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    
                    break;

                default:
                    break;
            }
            
            super.handleMessage(msg);
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        parentCtx = getParent() == null ? BaseActivity.this : getParent();
        mFDServiceInent = new Intent(BaseActivity.this, StoryFlowDataService.class);
        this.getApplicationContext().bindService(mFDServiceInent, mConnection, Context.BIND_AUTO_CREATE);
        
        
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(null);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        
        super.onCreate(savedInstanceState);
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((StoryFlowDataService.StoryFlowFetchDataBinder)service).getService();

            afterServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };
    
    protected abstract void afterServiceConnected();
    
    @Override
    protected void onDestroy() {
        if (null != mConnection) {
            this.getApplicationContext().unbindService(mConnection);
            StoryLogger.e(TAG, TAG+"mConnection is null");
        }
        super.onDestroy();
    }
    
}

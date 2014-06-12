
package com.youqude.storyflow.pcs;

import com.youqude.storyflow.R;
import com.youqude.storyflow.pcs.exception.PcsException;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class Uploader extends AsyncTask<Void, Long, Boolean> {
    
    private final static String TAG = Uploader.class.getSimpleName();
    private Context instance;
    private final ProgressDialog mDialog;
    private PcsClient pcsClient;

    private String localPath;
    private String remoteDir;
    private String remoteName;

    public Uploader(Context context, PcsClient pcsClient, String localPath, String remoteDir,
            String remoteName) {
        instance = context;
        this.pcsClient = pcsClient;
        this.localPath = localPath;
        this.remoteDir = remoteDir;
        this.remoteName = remoteName;
        mDialog = new ProgressDialog(context);
        mDialog.setMessage(instance.getString(R.string.save_loading));
        mDialog.show();
    }

    String errmsg = "";

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            if (null != localPath) {
                Log.i(TAG, "Upload " + localPath + " to " + remoteDir + "/" + remoteName);
                pcsClient.uploadFile(localPath, remoteDir, remoteName);
                return true;
            } else {
                Log.i(TAG, "mkdir " + remoteName);
                pcsClient.mkdir(remoteName);
                return true;
            }
        } catch (PcsException e) {
            e.printStackTrace();
            errmsg = "error on upload " + e.toString();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        try {
            mDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!result) {
            Toast.makeText(instance, errmsg, Toast.LENGTH_LONG).show();
            return;
        } else {
            Toast.makeText(instance, instance.getString(R.string.save_success), Toast.LENGTH_LONG)
                    .show();
        }
    }
}

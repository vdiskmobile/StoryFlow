
package com.youqude.storyflow.net;

import com.tencent.weibo.oauthv2.OAuthV2;
import com.youqude.storyflow.R;
import com.youqude.storyflow.StoryAPI;
import com.youqude.storyflow.StoryFlowApp;
import com.youqude.storyflow.domain.AlbumInfo;
import com.youqude.storyflow.exception.StatusCodeException;
import com.youqude.storyflow.exception.StoryFlowException;
import com.youqude.storyflow.pcs.PcsClient;
import com.youqude.storyflow.pcs.Uploader;
import com.youqude.storyflow.utils.Constants;
import com.youqude.storyflow.utils.StoryLogger;
import com.youqude.storyflow.utils.Utility;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class QqDownloadAsyncTask extends AsyncTask<Object[], Object[], Void> {

    /**
     * 执行文件保存预处理操作:下载
     */
    private static final String TAG = QqDownloadAsyncTask.class.getSimpleName();

    private static Context ctx;
    private String downloadURL;

    private File downloadFile;
    private long mContentLength;

    private ProgressDialog mProgressDialog;

    private OAuthV2 qAuthV2;

    private String storyTitle;
    
    private AlbumInfo mAlbumInfo;
    
    public QqDownloadAsyncTask(Context ctx, OAuthV2 qAuthV2, AlbumInfo albumInfo) {
        super();
        this.ctx = ctx;
        this.qAuthV2 = qAuthV2;
        this.storyTitle = storyTitle;
        this.mAlbumInfo = albumInfo;
    }

    @Override
    protected void onPreExecute() {

        mProgressDialog = new ProgressDialog(ctx);
        mProgressDialog.setTitle(null);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(ctx.getString(R.string.operation_loading));
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
    protected void onPostExecute(Void result) {

        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            try {
                mProgressDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (mContentLength == downloadFile.length()) {

            String tempName = downloadFile.getName();
            File parent = downloadFile.getParentFile();
            if (tempName.endsWith(Constants.DOWNLOAD_TEMP_FILE_SUFFIX)) {
                String filename = tempName.replace(
                        Constants.DOWNLOAD_TEMP_FILE_SUFFIX, "");
                File newFile = new File(parent, filename);
                downloadFile.renameTo(newFile);

                /**
                 * 执行保存操作:百度网盘上传
                 */
                StoryLogger.e(TAG,
                        TAG + "=======>" + newFile.getPath() + "---->" + newFile.getName());

                new QqShareOperationAsyncTask(ctx, mAlbumInfo).execute(new Object[]{
                      qAuthV2 , newFile.getPath()
                });
            } else {
                Toast.makeText(ctx, ctx.getString(R.string.operation_failed), Toast.LENGTH_LONG)
                        .show();
            }

        } else {
            Toast.makeText(ctx, ctx.getString(R.string.operation_failed), Toast.LENGTH_LONG)
                    .show();
        }
        super.onPostExecute(result);
    }

    @Override
    protected Void doInBackground(Object[]... params) {

        downloadURL = (String) params[0][0];

        InputStream is = null;
        FileOutputStream fos = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {

            URL uri = new URL(downloadURL);
            HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
            conn.setReadTimeout(100000);
            conn.setConnectTimeout(20000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty(
                    "User-Agent",
                    Constants.USER_AGENT_PREFIX
                            + Utility.getVerName(ctx)
                            + " "
                            + ("Android;" + Build.MODEL + ";" + Build.VERSION.RELEASE + ";" + "zh_CN"));

            File root = Environment.getExternalStorageDirectory();
            String expName = downloadURL.substring(downloadURL.lastIndexOf("/") + 1,
                    downloadURL.length());
            String cachePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/"+ctx.getString(R.string.app_name)+"/"+ctx.getString(R.string.downloaded)+"/" + expName + Constants.DOWNLOAD_TEMP_FILE_SUFFIX;

            downloadFile = Utility.createDirFile(cachePath);

            String contentLength = conn.getHeaderField("Content-Length");
            mContentLength = Long.parseLong(contentLength);
            int statusCode = conn.getResponseCode();

            is = conn.getInputStream();
            fos = new FileOutputStream(downloadFile, true);
            bis = new BufferedInputStream(is);
            bos = new BufferedOutputStream(fos);

            int bufferSize = 16384;
            if (mContentLength > 256 * 1024) {
                bufferSize = 256 * 1024;
            } else if (mContentLength > 65536) {
                bufferSize = 65536;
            }

            byte[] buffer = new byte[bufferSize];
            int count = 0;
            int downloadCount = 0;
            try {
                while (true) {

                    if (Utility.isNetworkAvailable(ctx)) {
                        try {
                            count = bis.read(buffer, 0, buffer.length);
                        } catch (IOException e) {
                            e.printStackTrace();
                            break;
                        }

                        if (count < 0) {
                            break;
                        }
                        try {
                            bos.write(buffer, 0, count);
                            bos.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (bis != null) {
                    bis.close();
                }

                if (fos != null) {
                    fos.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e2) {
            }
        }
        return null;
    }
}

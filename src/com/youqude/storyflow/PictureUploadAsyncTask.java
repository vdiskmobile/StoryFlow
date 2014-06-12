
package com.youqude.storyflow;

import com.youqude.storyflow.utils.Constants;
import com.youqude.storyflow.utils.StoryLogger;
import com.youqude.storyflow.utils.Utility;

import org.apache.http.HttpStatus;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PictureUploadAsyncTask extends AsyncTask<Object[], Integer, Integer> {

    private static final String TAG = PictureUploadAsyncTask.class.getSimpleName();

    private Context ctx;

    private String BOUNDARY;
    private String PREFIX;
    private String LINEND;
    private String MULTIPART_FROM_DATA;
    private String CHARSET;

    private String uploadPicFilePath;
    private String uploadPicFileName;

    private int res;// responseCode
    private String resultBody;

    public PictureUploadAsyncTask(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    protected void onPreExecute() {

        BOUNDARY = java.util.UUID.randomUUID().toString();
        PREFIX = "--";
        LINEND = "\r\n";
        MULTIPART_FROM_DATA = "multipart/form-data";
        CHARSET = HTTP.UTF_8;

        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(Object[]... params) {

        if (Utility.isNetworkAvailable(ctx)) {
            return doBackground(params);
        } else {
            return 0;
        }
    }

    private Integer doBackground(Object[]... params) {

        uploadPicFilePath = (String) params[0][0];
        uploadPicFileName = (String) params[0][1];

        InputStream is = null;
        BufferedInputStream bis = null;
        OutputStream outStream = null;
        HttpURLConnection conn = null;

        int temp = 0;
        
        try {

            File picFile = new File(uploadPicFilePath);
            
            StringBuilder sb = new StringBuilder();
            sb.append(PREFIX);
            sb.append(BOUNDARY);
            sb.append(LINEND);
            sb.append("Content-Disposition: form-data; name=\"uploadedFile\"; filename=\""
                    + uploadPicFileName + "\"" + LINEND);
            sb.append("Content-Type: application/octet-stream; charset=" + CHARSET
                    + LINEND);
            sb.append(LINEND);

            byte[] before = sb.toString().getBytes("UTF-8");
            byte[] after = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("UTF-8");

            URL uri = new URL(Constants.STORY_PIC_UPLOAD_URL);
            conn = (HttpURLConnection) uri.openConnection();
            conn.setReadTimeout(100000);
            conn.setConnectTimeout(30000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
           /* conn.setFixedLengthStreamingMode((int) (before.length +
                    picFile.length() + after.length));*/
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Charsert", "UTF-8");
            conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);

//            conn.connect();

            StoryLogger.e("UPLOAD", picFile.length()+"");
            
            is = new FileInputStream(picFile);
            bis = new BufferedInputStream(is);
            outStream = conn.getOutputStream();
            outStream.write(before);

            byte[] buffer = new byte[8192];
            int len = 0;
            int total = 0;
            int uploadCount = 0;
            /**
             * output
             */
            while (true) {
                if (Utility.isNetworkAvailable(ctx)) {
                    try {
                        len = bis.read(buffer, 0, buffer.length);
                    } catch (IOException e) {
                        res = 0;
                        break;
                    }
                    if (len < 0) {
                        break;
                    }
                    total += len;
                    try {
                        outStream.write(buffer, 0, len);
                        outStream.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    /*if ((uploadCount == 0)
                            || (long) (total * 100 / picFile.length()) - 1 > uploadCount) {

                        uploadCount += 1;
                        double progressD = ((double) total / (double) picFile.length())
                                * (double) 100;
                        int progress = (int) Math.round(progressD);

                        if (temp != progress) {
                            StoryLogger.e("TAG", "TAG:" + temp + "===>" + progress);
                            temp = progress;
                        }
                    }*/
                    
                    
                    
                } else {
                    res = 0;
                    break;
                }
            }

            /**
             * input
             */
            if (Utility.isNetworkAvailable(ctx)) {
                // 请求结束标志
                outStream.write(after);

                try {
                    res = conn.getResponseCode();

                    StoryLogger.e(TAG, TAG+":---->"+res);
                    
                    if (res == HttpStatus.SC_OK) {

                        /**
                         * 获取body
                         */
                        InputStream inputStream = conn.getInputStream();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer2 = new byte[1024];
                        int count = 0;
                        while ((count = inputStream.read(buffer)) != -1) {
                            baos.write(buffer, 0, count);
                        }
                        resultBody = new String(baos.toByteArray());

                        baos.close();
                        inputStream.close();

                    } else {
                        /**
                         * 上传失败
                         */
                        res = 0;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                res = 0;
            }

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (is != null) {
                    is.close();
                }
                if (outStream != null) {
                    outStream.close();
                }

                if (conn != null) {
                    conn.disconnect();
                }

            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return res;
    }

    @Override
    protected void onPostExecute(Integer resCode) {

        if (res == HttpStatus.SC_OK) {
            /**
             * 说明上传成功
             */
            try {
                
                StoryLogger.e(TAG, TAG+"---->"+resultBody);
                
                JSONObject jobj = new JSONObject(resultBody);
                JSONObject jsonObject = jobj.getJSONObject("resp");
                int infoCode = jsonObject.getInt("infocode");
                if (infoCode == 200) {
                    String relativePath = jsonObject.getString("relativePath");
                    Intent intent = new Intent(Constants.UPLOAD_STORY_PIC_ACTION);
                    intent.putExtra("relativePath", relativePath);
                    ctx.sendBroadcast(intent);
                } else {
                    Intent intent = new Intent(Constants.UPLOAD_STORY_PIC_FAILED_ACTION);
                    ctx.sendBroadcast(intent);
                }
                
            } catch (JSONException e) {
                e.printStackTrace();
                /**
                 * 上传失败,或上传过程中网络中断,直接给予提示
                 */
                Intent intent = new Intent(Constants.UPLOAD_STORY_PIC_FAILED_ACTION);
                ctx.sendBroadcast(intent);
            }
        } else {
            /**
             * 上传失败,或上传过程中网络中断,直接给予提示
             */
            Intent intent = new Intent(Constants.UPLOAD_STORY_PIC_FAILED_ACTION);
            ctx.sendBroadcast(intent);
        }
        
        
        super.onPostExecute(resCode);
    }

}

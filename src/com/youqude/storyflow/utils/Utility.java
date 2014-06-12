
package com.youqude.storyflow.utils;

import com.tencent.weibo.oauthv2.OAuthV2;
import com.youqude.storyflow.StoryFlowApp;
import com.youqude.storyflow.adapter.HorizontialListViewAdapter;
import com.youqude.storyflow.domain.UserInfo;
import com.youqude.storyflow.ui.HorizontialListView;
import com.youqude.storyflow.weibo.AccessToken;
import com.youqude.storyflow.weibo.Weibo;
import com.youqude.storyflow.weibo.WeiboException;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpUriRequest;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.ViewGroup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Utility {

    
    public static final String BOUNDARY = "7cd4a6d158c";
    public static final String MP_BOUNDARY = "--" + BOUNDARY;
    public static final String END_MP_BOUNDARY = "--" + BOUNDARY + "--";
    
    
    /**
     * @param context
     * @return
     */
    public static String getVerName(Context context) {
        String verName = "";
        try {
            verName = context.getPackageManager().getPackageInfo(
                    Constants.PACKAGE_NAME, 0).versionName;
        } catch (Exception e) {

        }
        return verName;
    }

    /**
     * @param context
     * @return
     */
    public static int getVerCode(Context context) {
        int verCode = -1;
        try {
            verCode = context.getPackageManager().getPackageInfo(
                    Constants.PACKAGE_NAME, 0).versionCode;
        } catch (Exception e) {

        }
        return verCode;
    }

    /**
     * @param ctx
     * @param request
     */
    public static void sign(Context ctx, HttpUriRequest request) {
        request.setHeader("User-Agent", Constants.USER_AGENT_PREFIX +
                Utility.getVerName(ctx) + " " + " " + ("Android;" +
                        Build.MODEL + ";" + Build.VERSION.RELEASE + ";" + "zh_CN"));
    }

    /**
     * @param params
     * @return
     */
    static public String urlencode(String[] params) {
        if (params.length % 2 != 0) {
            throw new IllegalArgumentException("Params must have an even number of elements.");
        }
        String result = "";
        try {
            boolean firstTime = true;
            for (int i = 0; i < params.length; i += 2) {
                if (params[i + 1] != null) {
                    if (firstTime) {
                        firstTime = false;
                    } else {
                        result += "&";
                    }
                    result += URLEncoder.encode(params[i], "UTF-8") + "="
                            + URLEncoder.encode(params[i + 1], "UTF-8");
                }
            }
            result.replace("*", "%2A");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        return result;
    }

    /**
     * @param filepath
     * @return
     */
    public static File createDirFile(String filepath) {
        int pos = filepath.lastIndexOf("/");
        String dirpath = filepath.substring(0, pos + 1);
        if (!dirpath.startsWith("/"))
            dirpath = "/" + dirpath;
        File f = new File(dirpath);
        if (!f.exists())
            f.mkdirs();
        return new File(filepath);
    }

    /**
     * 保存当前登录的用户所有信息
     */
    public static void updateLoginPreference(Context context, UserInfo userInfo, String expire_in) {

        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();
        DesEncrypt encrypt = new DesEncrypt(imei);
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.SESSION_PREFS, 0);
        Editor edit = prefs.edit();
        edit.putString(Constants.SESSION_ID,
                encrypt.getEncString(userInfo.sessionId));
        edit.putString(Constants.APP_TYPE,
                encrypt.getEncString(userInfo.appType));
        edit.putString(Constants.OPEN_ID,
                encrypt.getEncString(userInfo.openid));
        edit.putString(Constants.OPEN_KEY,
                encrypt.getEncString(userInfo.openkey));
        edit.putString(Constants.SESSION_KEY,
                encrypt.getEncString(userInfo.sessionKey));
        edit.putString(Constants.SESSION_SECRET,
                encrypt.getEncString(userInfo.sessionSecret));
        edit.putString(Constants.TOKEN,
                encrypt.getEncString(userInfo.token));
        edit.putString(Constants.UID,
                encrypt.getEncString(userInfo.uid));
        edit.putString(Constants.EXPIRE_IN,
                encrypt.getEncString(expire_in));
        edit.putString(Constants.DEVICE_ID,
                encrypt.getEncString(imei));
        edit.putString(Constants.VALIDITY_TIME,
                encrypt.getEncString(String.valueOf(System.currentTimeMillis())));
        edit.commit();
    }

    
    /**
     * 清空当前登录信息
     * @param ctx
     */
    public static void clearLoginPreference(Context ctx){
        
        SharedPreferences prefs = ctx.getSharedPreferences(
                Constants.SESSION_PREFS, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
        
    }
    
    
    /**
     * 判断sessionId是否有效
     * 
     * @param validityTime
     * @return
     */
    public static boolean isSessionValid(String validityTime) {
        if (!TextUtils.isEmpty(validityTime)) {
            try {
                long mCurrentTime = System.currentTimeMillis();
                long mSubTime = mCurrentTime - Long.parseLong(validityTime);
                StoryLogger.e("time", mCurrentTime + "====---------->");
                StoryLogger.e("time", validityTime + "====>");
                StoryLogger.e("time", mSubTime + "");
                if (mSubTime / 1000 <= Constants.VALIDITY_TIME_END) {
                    return true;
                }
                return false;
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return false;
            }

        }
        return false;
    }

    /**
     * @param ctx
     * @return
     */
    public static boolean isNetworkAvailable(Context ctx) {
        Context context = ctx;
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            // Error
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 获取头像
     * 
     * @param headURL
     * @return
     * @throws Exception
     */
    public static InputStream getInputStream(String headURL) throws Exception
    {
        URL url = new URL(headURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        InputStream inStream = conn.getInputStream();
        return inStream;
    }

    /**
     * 
     * @param path
     * @param id
     * @return
     * @throws Exception
     */
    public static byte[] getImage(String path, String id) throws Exception
    {
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setUseCaches(false);
        // conn.setConnectTimeout(6 * 1000);
        InputStream inStream = conn.getInputStream();
        return StreamTools.readInputStream(inStream, id);
    }

    /**
     * 重新分配listview的高度
     * @param listView
     */
    public static void setListViewHeightBasedOnChildren(HorizontialListView listView) {
        HorizontialListViewAdapter listAdapter = (HorizontialListViewAdapter) listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        /*
         * int totalHeight = 0; for (int i = 0; i < listAdapter.getCount(); i++)
         * { View listItem = listAdapter.getView(i, null, listView);
         * listItem.measure(0, 0); totalHeight += listItem.getMeasuredHeight();
         * }
         */
        int desityDpi = StoryFlowApp.getInstance().mScreenDensityDpi;
        
        
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        
        if (desityDpi<=120) {
            params.height = 60;
        }else if (desityDpi>120&&desityDpi<=160) {
            params.height = 80;
        }else if (desityDpi>120&&desityDpi<=240) {
            params.height = 120;
        }
        
        // params.height = totalHeight + (2* (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    /**
     * 绘制圆形图片
     * @param bitmap
     * @param pixels
     * @return
     */
    public static Bitmap toRoundCorner(Bitmap bitmap, int pixels) {

        Bitmap output = Bitmap
                .createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
    
    /**
     * 保存QQ OAuth信息
     * @param context
     * @param qOAuth
     */
    public static void updateQQOAuthPrefs(Context context, OAuthV2 qOAuth) {

        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();
        DesEncrypt encrypt = new DesEncrypt(imei);
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.QQ_OAUTH_PREFS, 0);
        Editor edit = prefs.edit();
        edit.putString(Constants.TOKEN,
                encrypt.getEncString(qOAuth.getAccessToken()));
        edit.putString(Constants.EXPIRE_IN,
                encrypt.getEncString(qOAuth.getExpiresIn()));
        edit.putString(Constants.OPEN_ID,
                encrypt.getEncString(qOAuth.getOpenid()));
        edit.putString(Constants.OPEN_KEY,
                encrypt.getEncString(qOAuth.getOpenkey()));
        edit.putString(Constants.DEVICE_ID,
                encrypt.getEncString(imei));
        edit.putLong(Constants.VALIDITY_TIME,
                System.currentTimeMillis());
        edit.commit();
    }
    
    
    /**
     * 保存sina weibo授权成功后信息
     * @param context
     * @param accessToken
     */
    public static void updateSINAOAuthPrefs(Context context, AccessToken accessToken) {
        
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();
        DesEncrypt encrypt = new DesEncrypt(imei);
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.SINA_OAUTH_PREFS, 0);
        Editor edit = prefs.edit();
        edit.putString(Constants.TOKEN,
                encrypt.getEncString(accessToken.getToken()));
        edit.putString(Constants.EXPIRE_IN,
                encrypt.getEncString(String.valueOf(accessToken.getExpiresIn())));
        edit.putString(Constants.DEVICE_ID,
                encrypt.getEncString(imei));
        edit.putLong(Constants.VALIDITY_TIME,
                System.currentTimeMillis());
        edit.commit();
    }
    
    /**
     * 保存百度登录信息
     * @param context
     * @param access_token
     * @param exprie_in
     * @param sessionKey
     * @param sessionSecret
     */
    public static void updateBaiduOAuthPrefs(Context context, String access_token,
            String exprie_in, String sessionKey, String sessionSecret) {

        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();
        DesEncrypt encrypt = new DesEncrypt(imei);
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.BAIDU_OAUTH_PREFS, 0);
        Editor edit = prefs.edit();
        edit.putString(Constants.TOKEN,
                encrypt.getEncString(access_token));
        edit.putString(Constants.EXPIRE_IN,
                encrypt.getEncString(String.valueOf(exprie_in)));
        edit.putString(Constants.SESSION_KEY,
                encrypt.getEncString(sessionKey));
        edit.putString(Constants.SESSION_SECRET,
                encrypt.getEncString(sessionSecret));
        edit.putString(Constants.DEVICE_ID,
                encrypt.getEncString(imei));
        edit.putLong(Constants.VALIDITY_TIME,
                System.currentTimeMillis());
        edit.commit();
    }
    
    /**
     * 获取SINA WEIBO OAuth信息
     * @param context
     * @param qOAuth
     */
    
    public static AccessToken getSinaOAuth2Prefs(Context context) {
        
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();
        DesEncrypt encrypt = new DesEncrypt(imei);
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.SINA_OAUTH_PREFS, 0);
        String access_token=encrypt.getDesString(prefs.getString(Constants.TOKEN, ""));
        String expire_in=encrypt.getDesString(prefs.getString(Constants.EXPIRE_IN, ""));
        String deviceId=encrypt.getDesString(prefs.getString(Constants.DEVICE_ID, ""));
        long validity_time=prefs.getLong(Constants.VALIDITY_TIME, 0);
        
        /**
         * 判断是否在有效期内
         */
        try {
            long mCurrentTime = System.currentTimeMillis();
            long mSubTime = mCurrentTime - validity_time;
            if (imei.equals(deviceId) && mSubTime / 1000 < Constants.SINA_VALIDITY_TIME_END) {
                
                StoryLogger.e("ACCESS_TOKEN", "-------------->0");
                
                AccessToken accessToken = new AccessToken(access_token, Constants.WEIBO_CONSUMER_SECRET);
                accessToken.setExpiresIn(expire_in);
                Weibo.getInstance().setAccessToken(accessToken);
                return accessToken;
            }
        } catch (Exception e) {
            e.printStackTrace();
            StoryLogger.e("ACCESS_TOKEN", "-------------->1");
            return null;
        }
        
        StoryLogger.e("ACCESS_TOKEN", "-------------->2");
        return null;
    }
    
    /**
     * 获取百度登陆状态
     * @param context
     * @return
     */
    
    public static String getBaiduOAuth2Prefs(Context context) {
        
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();
        DesEncrypt encrypt = new DesEncrypt(imei);
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.BAIDU_OAUTH_PREFS, 0);
        String access_token=encrypt.getDesString(prefs.getString(Constants.TOKEN, ""));
        String expire_in=encrypt.getDesString(prefs.getString(Constants.EXPIRE_IN, ""));
        String sessionKey=encrypt.getDesString(prefs.getString(Constants.SESSION_KEY, ""));
        String sessionSecret=encrypt.getDesString(prefs.getString(Constants.SESSION_SECRET, ""));
        String deviceId=encrypt.getDesString(prefs.getString(Constants.DEVICE_ID, ""));
        long validity_time=prefs.getLong(Constants.VALIDITY_TIME, 0);
        
        /**
         * 判断是否在有效期内
         */
        try {
            long mCurrentTime = System.currentTimeMillis();
            long mSubTime = mCurrentTime - validity_time;
            if (imei.equals(deviceId) && mSubTime / 1000 < Constants.BAIDU_VALIDITY_TIME_END) {
                return access_token;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
        return null;
    }
    
    
    /**
     * 获取QQ 登录信息
     * @param context
     * @return
     */
    public static OAuthV2 getQQOAuthPrefs(Context context) {
        
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();
        DesEncrypt encrypt = new DesEncrypt(imei);
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.QQ_OAUTH_PREFS, 0);
        String access_token=encrypt.getDesString(prefs.getString(Constants.TOKEN, ""));
        String expire_in=encrypt.getDesString(prefs.getString(Constants.EXPIRE_IN, ""));
        String openid=encrypt.getDesString(prefs.getString(Constants.OPEN_ID, ""));
        String openkey=encrypt.getDesString(prefs.getString(Constants.OPEN_KEY, ""));
        String deviceId=encrypt.getDesString(prefs.getString(Constants.DEVICE_ID, ""));
        long validity_time=prefs.getLong(Constants.VALIDITY_TIME, 0);
        
        /**
         * 判断是否在有效期内
         */
        try {
            long mCurrentTime = System.currentTimeMillis();
            long mSubTime = mCurrentTime - validity_time;
            if (imei.equals(deviceId) && mSubTime / 1000 < Constants.QQ_VALIDITY_TIME_END) {
                OAuthV2 qAuthV2 = new OAuthV2(Constants.QQ_REDIRECT_URI);
                qAuthV2.setClientId(Constants.QQ_CLIENT_ID);
                qAuthV2.setClientSecret(Constants.QQ_CLIENT_SECRET);
                qAuthV2.setAccessToken(access_token);
                qAuthV2.setExpiresIn(expire_in);
                qAuthV2.setOpenid(openid);
                qAuthV2.setOpenkey(openkey);
                return qAuthV2;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
        return null;
    }

    
    /**
     * post 相关参数
     * @param baos
     * @param params
     * @throws WeiboException
     */
    public static void paramToUpload(OutputStream baos, ArrayList<NameValuePair> params)
            throws WeiboException {
        String key = "";
        for (int loc = 0; loc < params.size(); loc++) {
            key = params.get(loc).getName();
            StringBuilder temp = new StringBuilder(10);
            temp.setLength(0);
            temp.append(MP_BOUNDARY).append("\r\n");
            temp.append("content-disposition: form-data; name=\"").append(key).append("\"\r\n\r\n");
            temp.append(params.get(loc).getValue()).append("\r\n");
            byte[] res = temp.toString().getBytes();
            try {
                baos.write(res);
            } catch (IOException e) {
                throw new WeiboException(e);
            }
        }
    }
    
    public static String Md5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(s.getBytes());
            byte[] output = md.digest();
            char hexDigit[] = {
                    '0', '1', '2', '3', '4', '5', '6', '7', '8',
                    '9', 'a', 'b', 'c', 'd', 'e', 'f'
            };
            StringBuffer buf = new StringBuffer();
            for (int j = 0; j < output.length; j++) {
                buf.append(hexDigit[(output[j] >> 4) & 0x0f]);
                buf.append(hexDigit[output[j] & 0x0f]);
            }
            return buf.toString();
        } catch (NoSuchAlgorithmException e) {
        }
        return "";
    }
    
    
    public static int computeSampleSize(BitmapFactory.Options options,
            int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);
     
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
     
        return roundedSize;
    }
    
    
    private static int computeInitialSampleSize(BitmapFactory.Options options,
            int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
     
        int lowerBound = (maxNumOfPixels == -1) ? 1 :
                (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 :
                (int) Math.min(Math.floor(w / minSideLength),
                Math.floor(h / minSideLength));
     
        if (upperBound < lowerBound) {
            return lowerBound;
        }
     
        if ((maxNumOfPixels == -1) &&
                (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }   
    
}

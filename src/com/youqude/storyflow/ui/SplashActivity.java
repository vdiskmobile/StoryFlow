package com.youqude.storyflow.ui;

import com.youqude.storyflow.R;
import com.youqude.storyflow.StoryFlowApp;
import com.youqude.storyflow.StoryAPI.PlatformType;
import com.youqude.storyflow.domain.UserInfo;
import com.youqude.storyflow.utils.Constants;
import com.youqude.storyflow.utils.DesEncrypt;
import com.youqude.storyflow.utils.StoryLogger;
import com.youqude.storyflow.utils.Utility;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;

public class SplashActivity extends BaseActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();
    
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.splash);
        
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                userValidate();
            }

        }, 1000);
    }

    
    private void userValidate() {
        
        
        TelephonyManager telephonyManager=(TelephonyManager) SplashActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
        String phone_imei=telephonyManager.getDeviceId();
        DesEncrypt des = new DesEncrypt(phone_imei);
        
        SharedPreferences prefs = getSharedPreferences(Constants.SESSION_PREFS, 0);
        String sessionId=des.getDesString(prefs.getString(Constants.SESSION_ID, ""));
        String appType = des.getDesString(prefs.getString(Constants.APP_TYPE, ""));
        String openid = des.getDesString(prefs.getString(Constants.OPEN_ID, ""));
        String openkey = des.getDesString(prefs.getString(Constants.OPEN_KEY, ""));
        String sessionKey = des.getDesString(prefs.getString(Constants.SESSION_KEY, ""));
        String sessionSecret = des.getDesString(prefs.getString(Constants.SESSION_SECRET, ""));
        String token = des.getDesString(prefs.getString(Constants.TOKEN, ""));
        String uid = des.getDesString(prefs.getString(Constants.UID, ""));
        String expire_in = des.getDesString(prefs.getString(Constants.EXPIRE_IN, ""));
        String imei=des.getDesString(prefs.getString(Constants.DEVICE_ID, ""));
        String validity_time=des.getDesString(prefs.getString(Constants.VALIDITY_TIME, ""));
        

        UserInfo userInfo = new UserInfo();
        userInfo.sessionId  = sessionId;
        userInfo.appType  = appType;
        userInfo.openid  = openid;
        userInfo.openkey  = openkey;
        userInfo.sessionKey  = sessionKey;
        userInfo.sessionSecret  = sessionSecret;
        userInfo.token  = token;
        userInfo.uid = uid;
        
        
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        if (imei.equals(phone_imei) && !TextUtils.isEmpty(userInfo.sessionId)
                && !TextUtils.isEmpty(token) && !TextUtils.isEmpty(userInfo.uid)) {

            StoryLogger.e(TAG, TAG+"--------->0");
            
            if (!Utility.isSessionValid(validity_time)) {
                intent.putExtra("isLogin", false);
                StoryLogger.e(TAG, TAG+"--------->1");
            } else {
                intent.putExtra("isLogin", true);
                StoryFlowApp.getInstance().userInfo = userInfo;
               
            }
        } else{
            StoryLogger.e(TAG, TAG+"--------->2");
            intent.putExtra("isLogin", false);
        }
        
        
        /**
         * 判断本地是否存在，验证用户是否登录
         */
        startActivity(intent);
        finish();
        
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void afterServiceConnected() {

    }

}


package com.youqude.storyflow.ui;

import com.tencent.weibo.api.UserAPI;
import com.tencent.weibo.constants.OAuthConstants;
import com.tencent.weibo.oauthv2.OAuthV2;
import com.tencent.weibo.oauthv2.OAuthV2Client;
import com.tencent.weibo.webview.OAuthV2AuthorizeWebView;
import com.youqude.storyflow.R;
import com.youqude.storyflow.StoryFlowApp;
import com.youqude.storyflow.StoryAPI.PlatformType;
import com.youqude.storyflow.StoryFlowEventHandler;
import com.youqude.storyflow.baidu.BaiduDialogListener;
import com.youqude.storyflow.baidu.BaiduOAuth;
import com.youqude.storyflow.baidu.BaiduOAuthViaDialog;
import com.youqude.storyflow.domain.BaiduUserInfo;
import com.youqude.storyflow.domain.QUserInfo;
import com.youqude.storyflow.domain.UserInfo;
import com.youqude.storyflow.utils.Constants;
import com.youqude.storyflow.utils.StoryLogger;
import com.youqude.storyflow.utils.Utility;
import com.youqude.storyflow.weibo.AccessToken;
import com.youqude.storyflow.weibo.DialogError;
import com.youqude.storyflow.weibo.Weibo;
import com.youqude.storyflow.weibo.WeiboDialogListener;
import com.youqude.storyflow.weibo.WeiboException;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class LoginActivity extends BaseActivity implements OnClickListener, WeiboDialogListener,
        StoryFlowEventHandler {

    private static String TAG = LoginActivity.class.getSimpleName();
    /*
     * (non-Javadoc)
     * @see com.youqude.storyflow.BaseActivity#onCreate(android.os.Bundle)
     */

    LinearLayout sinaLayout;
    LinearLayout qqLayout;
    LinearLayout baiduLayout;

    /**
     * qq
     */
    private OAuthV2 QAuth;

    /**
     * baidu
     */
    private BaiduOAuth mBaiduOAuth;
    private BaiduUserInfo baiduUserInfo;

    /**
     * sina
     */
    private String sina_uid;
    
    private String expire_in;//可能是qq,也可能是sina,也可能是百度的access_token有效期
    
    
    Button mButton_Back;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.login);

        sinaLayout = (LinearLayout) findViewById(R.id.sinalayout);
        qqLayout = (LinearLayout) findViewById(R.id.qqlayout);
        baiduLayout = (LinearLayout) findViewById(R.id.baidulayout);

        
        mButton_Back = (Button) findViewById(R.id.btnBack);
        
        sinaLayout.setOnClickListener(this);
        qqLayout.setOnClickListener(this);
        baiduLayout.setOnClickListener(this);
        mButton_Back.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.sinalayout:

                Weibo weibo = Weibo.getInstance();
                weibo.setupConsumerConfig(Constants.WEIBO_CONSUMER_KEY,
                        Constants.WEIBO_CONSUMER_SECRET);
                weibo.setRedirectUrl(Constants.WEIBO_REDIRECT_URI);
                weibo.authorize(LoginActivity.this,
                        LoginActivity.this);

                break;
            case R.id.qqlayout:

                QAuth = new OAuthV2(Constants.QQ_REDIRECT_URI);
                QAuth.setClientId(Constants.QQ_CLIENT_ID);
                QAuth.setClientSecret(Constants.QQ_CLIENT_SECRET);
                // 关闭OAuthV2Client中的默认开启的QHttpClient。
                OAuthV2Client.getQHttpClient().shutdownConnection();
                // 创建Intent，使用WebView让用户授权
                Intent intent = new Intent(LoginActivity.this, OAuthV2AuthorizeWebView.class);
                intent.putExtra("oauth", QAuth);
                startActivityForResult(intent, 2);

                break;
            case R.id.baidulayout:
                mBaiduOAuth = new BaiduOAuthViaDialog(Constants.API_KEY);
                mBaiduOAuth.startDialogAuth(LoginActivity.this, new String[] {
                        "basic", "netdisk"
                }, new BaiduDialogListener() {

                    @Override
                    public void onCancel() {
                        Toast.makeText(getApplicationContext(), "取消授权登录",
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onComplete(Bundle values) {
                        StoryLogger.e(TAG, "access_token:" + values.getString("access_token")
                                + "\nsession_key:" + values.getString("session_key")
                                + "\nsession_secret:" + values.getString("session_secret"));
                        StoryLogger.e(TAG, "expire_in----->"+values.getString("expires_in"));
                        expire_in = values.getString("expires_in");
                        
                        Utility.updateBaiduOAuthPrefs(LoginActivity.this, values.getString("access_token"), 
                                expire_in, values.getString("session_key"), values.getString("session_secret"));
                        
                        mService.getBaiduLoggedInUser(LoginActivity.this,
                                mBaiduOAuth.getAccessToken());
                    }

                    @Override
                    public void onException(String msg) {
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                    }
                });

                break;
            case R.id.btnBack:
                
                finish();
                
                break;
            default:
                break;
        }

    }

    /*
     * 通过读取OAuthV2AuthorizeWebView返回的Intent，获取用户授权信息
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            if (resultCode == OAuthV2AuthorizeWebView.RESULT_CODE) {
                QAuth = (OAuthV2) data.getExtras().getSerializable("oauth");
                if (QAuth.getStatus() == 0)
                    /*Toast.makeText(
                            getApplicationContext(),
                            "登陆成功:acccess_token:" + QAuth.getAccessToken()
                                    + ":expire_in:" + QAuth.getExpiresIn() + ":refresh_token:"
                                    + QAuth.getRefreshToken()
                                    + ":openid:" + QAuth.getOpenid() + ":openkey:"
                                    + QAuth.getOpenkey(),
                            Toast.LENGTH_SHORT).show();*/
                
                expire_in = QAuth.getExpiresIn();
                
                Utility.updateQQOAuthPrefs(LoginActivity.this, QAuth);
                
                try {
                    mService.qqLogin(LoginActivity.this, QAuth.getAccessToken(), QAuth.getOpenid(),
                            QAuth.getOpenkey());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onComplete(Bundle values) {
        String token = values.getString("access_token");
        String expires_in = values.getString("expires_in");
        AccessToken accessToken = new AccessToken(token, Constants.WEIBO_CONSUMER_SECRET);
        accessToken.setExpiresIn(expires_in);
        Weibo.getInstance().setAccessToken(accessToken);

        expire_in = expires_in;
        
        Utility.updateSINAOAuthPrefs(LoginActivity.this, accessToken);
        
//        Toast.makeText(LoginActivity.this, "finish weibo oauth", 0).show();

        mService.getSinaUid(LoginActivity.this, token);
    }

    @Override
    public void onWeiboException(WeiboException e) {
        Toast.makeText(getApplicationContext(),
                "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public void onError(DialogError e) {
        Toast.makeText(getApplicationContext(),
                "取消授权登录  : " + e.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCancel() {
        Toast.makeText(getApplicationContext(), "取消授权登录",
                Toast.LENGTH_LONG).show();
    }

    @Override
    protected void afterServiceConnected() {
        if (null == mService) {
            StoryLogger.e(TAG, TAG + ":mService is null");
        } else {
            StoryLogger.e(TAG, TAG + ":mService is not null");
        }

    }

    /**
     * 处理异步线程回调
     */
    @Override
    public void handleSeviceResult(String err_msg, int eventId, Object rlt) {

        switch (eventId) {
            case Constants.EVENT_CODE_SUCCESS: {
                StoryLogger.e(TAG, TAG + ":get sessionId success");
                Object[] obj = (Object[]) rlt;
                UserInfo userInfo = (UserInfo) obj[0];
                StoryLogger.e(TAG, userInfo.sessionId);
                StoryLogger.e(TAG, userInfo.uid);
                StoryLogger.e(TAG, userInfo.appType);

                StoryFlowApp.getInstance().userInfo = userInfo;
                StoryLogger.e(TAG, TAG+ "application-------->"+StoryFlowApp.getInstance().userInfo.sessionId);
                
                Intent intent = new Intent(Constants.LOGIN_SUCCESS_ACTION);
                intent.putExtra("userId", userInfo.uid);
                sendBroadcast(intent);
                
                
                /**
                 * 更新本地shared_prefs文件
                 */
                StoryLogger.e(TAG, TAG+":"+expire_in);
                
                Utility.updateLoginPreference(LoginActivity.this, userInfo, expire_in);
                
                
                if (userInfo.firstLogin) {
                    if (userInfo.appType.equals(PlatformType.SINA.toString())) {
                        if (!TextUtils.isEmpty(sina_uid) && userInfo.token != null
                                && !TextUtils.isEmpty(userInfo.token)) {
                            mService.addSinaBaseUserProfile(LoginActivity.this, sina_uid, userInfo);
                        }
                    } else if (userInfo.appType.equals(PlatformType.QQ.toString())) {
                        mService.addQQBaseUserProfile(LoginActivity.this, QAuth, userInfo);
                    } else if (userInfo.appType.equals(PlatformType.BAIDU.toString())) {

                        mService.addBaiduBaseUserProfile(LoginActivity.this, baiduUserInfo,
                                userInfo);
                    }
                } else {
                    
                    Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_SHORT)
                    .show();
                    setResult(RESULT_OK);
                    finish();
                   
                }
                break;
            }
            case Constants.BAIDU_UID_EVENT_CODE_SUCCESS: {
                Object[] obj = (Object[]) rlt;
                baiduUserInfo = (BaiduUserInfo) obj[0];
                StoryLogger.e(TAG, baiduUserInfo.uid);
                mService.baiduLogin(LoginActivity.this, baiduUserInfo.uid,
                        mBaiduOAuth.getAccessToken(), mBaiduOAuth.getSessionKey(),
                        mBaiduOAuth.getSessionSecret());
                break;
            }
            case Constants.SINA_UID_EVENT_CODE_SUCCESS: {
                Object[] obj = (Object[]) rlt;
                sina_uid = (String) obj[0];
                StoryLogger.e(TAG, sina_uid);
                mService.sinaLogin(LoginActivity.this, sina_uid, Weibo.getInstance()
                        .getAccessToken().getToken());
                break;
            }
            case Constants.QQ_BASE_PROFILE_SUCCESS: {
                StoryLogger.e(TAG, TAG + ":qq add profile success");
                Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_SHORT)
                .show();
                
                setResult(RESULT_OK);
                finish();
                break;
            }
            case Constants.SINA_BASE_PROFILE_SUCCESS: {
                StoryLogger.e(TAG, TAG + ":sina add profile success");
                Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_SHORT)
                .show();
                setResult(RESULT_OK);
                finish();
                break;
            }
            case Constants.BAIDU_BASE_PROFILE_SUCCESS: {
                StoryLogger.e(TAG, TAG + ":baidu add profile success");
                Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_SHORT)
                .show();
                setResult(RESULT_OK);
                finish();
                break;
            }
            case Constants.EVENT_CODE_FAILED: {
                StoryLogger.e(TAG, TAG + ":qq failed");
                break;
            }

            case Constants.EVENT_CODE_NET_INTERRUPT: {
                StoryLogger.e(TAG, TAG + ":qq net interrupt");
                Toast.makeText(LoginActivity.this,
                        getResources().getString(R.string.no_network_connection_toast),
                        Toast.LENGTH_SHORT).show();
                break;
            }
            default:
                break;
        }
    }

}

package com.youqude.storyflow.ui;

import com.tencent.weibo.oauthv2.OAuthV2;
import com.tencent.weibo.oauthv2.OAuthV2Client;
import com.tencent.weibo.webview.OAuthV2AuthorizeWebView;
import com.youqude.storyflow.R;
import com.youqude.storyflow.StoryFlowEventHandler;
import com.youqude.storyflow.utils.Constants;
import com.youqude.storyflow.utils.Utility;
import com.youqude.storyflow.weibo.AccessToken;
import com.youqude.storyflow.weibo.DialogError;
import com.youqude.storyflow.weibo.Weibo;
import com.youqude.storyflow.weibo.WeiboDialogListener;
import com.youqude.storyflow.weibo.WeiboException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class StorySelfSettingActivity extends BaseActivity implements StoryFlowEventHandler , OnClickListener , WeiboDialogListener{

    private static final String TAG = StorySelfSettingActivity.class.getSimpleName();
    
    
    Button mButton_finished;
    Button mButton_exit;
    TextView mTextView_Version;
    TextView mTextView_Copyright;
    
    
    LinearLayout sinaLayout;
    LinearLayout qqLayout;
    
    TextView mTextView_sina_bind;
    TextView mTextView_qq_bind;
    ImageView mImageView_Sina;
    ImageView mImageView_Qq;
    
    /**
     * 分享设置
     */
    boolean mQQTokenCanUse;
    OAuthV2 qOAuthV2;
    AccessToken mSinaAccessToken;
    boolean mSinaTokenCanUse;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.self_setting);
        
        mButton_finished = (Button) findViewById(R.id.btnFinish);
        mButton_exit = (Button) findViewById(R.id.btn_exit);
        mButton_finished.setOnClickListener(this);
        mButton_exit.setOnClickListener(this);
        
        mTextView_Version = (TextView) findViewById(R.id.tvVersion);
        mTextView_Version.setText(getString(R.string.app_name)+" "+Utility.getVerName(this));
        mTextView_Copyright = (TextView) findViewById(R.id.tvCopyright);
        mTextView_Copyright.setText("@"+getString(R.string.copyright_text));
        
        
        sinaLayout = (LinearLayout) findViewById(R.id.shareToplayout);
        qqLayout = (LinearLayout) findViewById(R.id.shareBottomlayout);
        mTextView_sina_bind = (TextView) findViewById(R.id.sina_bind);
        mTextView_qq_bind = (TextView) findViewById(R.id.qq_bind);
        mImageView_Sina = (ImageView) findViewById(R.id.iv_sina);
        mImageView_Qq = (ImageView) findViewById(R.id.iv_qq);
        
        sinaLayout.setOnClickListener(this);
        qqLayout.setOnClickListener(this);
        
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        /**
         * 初始化分享方式，验证是否需要重新授权
         */
        
      //SINA
        mSinaAccessToken = Utility.getSinaOAuth2Prefs(this); 
        
        if (mSinaAccessToken !=null) {
            //按钮高亮
            mImageView_Sina.setImageResource(R.drawable.icon_sina);
            mSinaTokenCanUse = true;
            mTextView_sina_bind.setText(getString(R.string.share_bind));
        } else {
            //按钮变灰
            mImageView_Sina.setImageResource(R.drawable.icon_sina_gray);
            mSinaTokenCanUse = false;
            mTextView_sina_bind.setText(getString(R.string.share_unbind));
        }
        
        
        //QQ
        qOAuthV2 = Utility.getQQOAuthPrefs(this); 
        
        if (qOAuthV2 !=null) {
            //按钮高亮
            mImageView_Qq.setImageResource(R.drawable.icon_qq);
            mQQTokenCanUse = true;
            mTextView_qq_bind.setText(getString(R.string.share_bind));
        } else {
            //按钮变灰
            mImageView_Qq.setImageResource(R.drawable.icon_qq_gray);
            mQQTokenCanUse = false;
            mTextView_qq_bind.setText(getString(R.string.share_unbind));
        }
        
        
        
        
    }
    
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

       if (requestCode == 2) {
            if (resultCode == OAuthV2AuthorizeWebView.RESULT_CODE) {
                qOAuthV2 = (OAuthV2) data.getExtras().getSerializable("oauth");
                /**
                 * 更新本地保存值
                 */
                Utility.updateQQOAuthPrefs(StorySelfSettingActivity.this, qOAuthV2);
                
                /**
                 * 初始化分享方式，验证是否需要重新授权
                 */
                if (qOAuthV2 !=null) {
                    //按钮高亮
                    mImageView_Qq.setImageResource(R.drawable.icon_qq);
                    mQQTokenCanUse = true;
                    mTextView_qq_bind.setText(getString(R.string.share_bind));
                } else {
                    //按钮变灰
                    mImageView_Qq.setImageResource(R.drawable.icon_qq_gray);
                    mQQTokenCanUse = false;
                    mTextView_qq_bind.setText(getString(R.string.share_unbind));
                }
                
            }
        }
        
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    
    @Override
    protected void afterServiceConnected() {

    }

    @Override
    public void handleSeviceResult(String err_msg, int eventId, Object rlt) {

    }

    @Override
    public void onClick(View v) {
        
        switch (v.getId()) {
            case R.id.btnFinish:
                finish();
                break;
            case R.id.btn_exit:
                
                logout();
                break;
            case R.id.shareToplayout:
                /**
                 * 判断是否需要登录授权
                 */
                if (!mSinaTokenCanUse) {
                    // 登录sina weibo授权页面

                    Weibo weibo = Weibo.getInstance();
                    weibo.setupConsumerConfig(Constants.WEIBO_CONSUMER_KEY,
                            Constants.WEIBO_CONSUMER_SECRET);
                    weibo.setRedirectUrl(Constants.WEIBO_REDIRECT_URI);
                    weibo.authorize(StorySelfSettingActivity.this,
                            StorySelfSettingActivity.this);

                }
                break;
            case R.id.shareBottomlayout:
                
                /**
                 * 判断是否需要登录授权
                 */
                if (!mQQTokenCanUse) {
                    // 登录qq授权页面

                    qOAuthV2 = new OAuthV2(Constants.QQ_REDIRECT_URI);
                    qOAuthV2.setClientId(Constants.QQ_CLIENT_ID);
                    qOAuthV2.setClientSecret(Constants.QQ_CLIENT_SECRET);
                    // 关闭OAuthV2Client中的默认开启的QHttpClient。
                    OAuthV2Client.getQHttpClient().shutdownConnection();
                    // 创建Intent，使用WebView让用户授权
                    Intent intent2 = new Intent(StorySelfSettingActivity.this,
                            OAuthV2AuthorizeWebView.class);
                    intent2.putExtra("oauth", qOAuthV2);
                    startActivityForResult(intent2, 2);

                }
                
                
                break;
                
            default:
                break;
        }
        
    }
    
    @SuppressWarnings("unused")
    @Override
    public void onComplete(Bundle values) {

        String token = values.getString("access_token");
        String expires_in = values.getString("expires_in");
        AccessToken accessToken = new AccessToken(token, Constants.WEIBO_CONSUMER_SECRET);
        accessToken.setExpiresIn(expires_in);
        Weibo.getInstance().setAccessToken(accessToken);

        /**
         * 初始化分享方式，验证是否需要重新授权
         */
        if (accessToken !=null) {
            //按钮高亮
            /**
             * 更新本地保存值
             */
            Utility.updateSINAOAuthPrefs(StorySelfSettingActivity.this, accessToken);
            
            mImageView_Sina.setImageResource(R.drawable.icon_sina);
            mSinaTokenCanUse = true;
            mTextView_sina_bind.setText(getString(R.string.share_bind));
        } else {
            //按钮变灰
            mImageView_Sina.setImageResource(R.drawable.icon_sina_gray);
            mSinaTokenCanUse = false;
            mTextView_sina_bind.setText(getString(R.string.share_unbind));
        }
        
        
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
                "Auth error : " + e.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCancel() {
        Toast.makeText(getApplicationContext(), "Auth cancel",
                Toast.LENGTH_LONG).show();        
    }
    
    
    
    private void logout(){

        AlertDialog.Builder builder = new AlertDialog.Builder(StorySelfSettingActivity.this);
        builder.setCancelable(true);
        builder.setTitle(R.string.confirm_logout);

        builder.setPositiveButton(R.string.ok_label,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        /**
                         * 清空当前账户的prefs文件内容，跳转到主页面
                         */
                        Utility.clearLoginPreference(StorySelfSettingActivity.this);
                        
                        Intent intent = new Intent(Constants.EXIT_CURRENT_SESSION_ACTION);
                        sendBroadcast(intent);
                        
                        finish();
                    }
                });
        builder.setNegativeButton(R.string.cancel_label,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(true);
        try {
            alert.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    
    }
    
    
}

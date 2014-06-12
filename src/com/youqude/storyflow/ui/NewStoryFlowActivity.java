
package com.youqude.storyflow.ui;

import com.tencent.weibo.oauthv2.OAuthV2;
import com.tencent.weibo.oauthv2.OAuthV2Client;
import com.tencent.weibo.webview.OAuthV2AuthorizeWebView;
import com.youqude.storyflow.PictureUploadAsyncTask;
import com.youqude.storyflow.R;
import com.youqude.storyflow.StoryFlowEventHandler;
import com.youqude.storyflow.utils.Constants;
import com.youqude.storyflow.utils.StoryLogger;
import com.youqude.storyflow.utils.Utility;
import com.youqude.storyflow.weibo.AccessToken;
import com.youqude.storyflow.weibo.DialogError;
import com.youqude.storyflow.weibo.Weibo;
import com.youqude.storyflow.weibo.WeiboDialogListener;
import com.youqude.storyflow.weibo.WeiboException;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class NewStoryFlowActivity extends BaseActivity implements StoryFlowEventHandler,
        OnClickListener ,  WeiboDialogListener{

    private static final String TAG = NewStoryFlowActivity.class.getSimpleName();

    Button mButton_Cancel;
    Button mButton_Release;

    ImageView mImageView_thumbnail;
    EditText mEditText_title;
    EditText mEditText_description;

    String imageFilePath;
    long imageFileSize;
    String storyTitle;
    String mBundleStoryId;
    Bitmap bitmap;

    static int REQUEST_CODE = 0;
    String storyId = "";
    
    ImageView mButton_sina;
    ImageView mButton_qq;
    boolean mQQTokenCanUse;
    OAuthV2 qOAuthV2;
    AccessToken mSinaAccessToken;
    boolean mSinaTokenCanUse;
    
    ProgressDialog pd;
    
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(Constants.UPLOAD_STORY_PIC_ACTION)) {
                /**
                 * 执行发布故事信息的逻辑
                 */
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    String picPath = extras.getString("relativePath");
                    StoryLogger.e(TAG, picPath);
                    if (TextUtils.isEmpty(storyId)) {
                        mService.sendStoryFlow(NewStoryFlowActivity.this, mEditText_title.getText()
                                .toString(), mEditText_description.getText().toString(),
                                picPath);
                    } else {
                        /**
                         * 发布一张图片到某个故事流
                         */
                        StoryLogger.e(TAG, TAG+":"+storyId);
                        mService.sendStoryFlowWithStoryId(NewStoryFlowActivity.this, storyId,
                                mEditText_description.getText().toString(),
                                picPath);
                    }
                   
                }
            } else if (action.equals(Constants.UPLOAD_STORY_PIC_FAILED_ACTION)) {
                
                if (pd !=null && pd.isShowing()) {
                    pd.dismiss();
                }
                
                Toast.makeText(NewStoryFlowActivity.this,
                        getResources().getString(R.string.release_story_failed),
                        Toast.LENGTH_SHORT).show();
                
            }
            
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.new_story_flow);

        Bundle extras = getIntent().getExtras();
        if (extras !=null) {
            imageFilePath = extras.getString("imageFile");
            imageFileSize = extras.getLong("imageFileSize");
            storyTitle = extras.getString("storyTitle");
            mBundleStoryId = extras.getString("storyId");
            storyId = mBundleStoryId;
        }
        

        mImageView_thumbnail = (ImageView) findViewById(R.id.share_img);
        mEditText_title = (EditText) findViewById(R.id.story_title);
        
        if (!TextUtils.isEmpty(storyTitle)) {
            mEditText_title.setText(storyTitle);
        }
        
        mEditText_description = (EditText) findViewById(R.id.story_description);

        BitmapFactory.Options ops = new BitmapFactory.Options();
        ops.inSampleSize = 4;
        bitmap = BitmapFactory.decodeFile(imageFilePath, ops);
        mImageView_thumbnail.setImageBitmap(bitmap);
        bitmap = null;

        mButton_Cancel = (Button) findViewById(R.id.btnCancel);
        mButton_Release = (Button) findViewById(R.id.btnRelease);
        
        
        mButton_sina = (ImageView) findViewById(R.id.btn_share_sina);
        mButton_qq = (ImageView) findViewById(R.id.btn_share_qq);
        
        /**
         * 需添加处理分享按钮的高亮的逻辑
         */
        
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.UPLOAD_STORY_PIC_ACTION);
        intentFilter.addAction(Constants.UPLOAD_STORY_PIC_FAILED_ACTION);
        registerReceiver(mReceiver, intentFilter);
        
        
        mButton_sina.setOnClickListener(this);
        mButton_qq.setOnClickListener(this);
        mButton_Cancel.setOnClickListener(this);
        mButton_Release.setOnClickListener(this);
        mEditText_title.setOnClickListener(this);
        
    }

    @Override
    protected void onDestroy() {

        unregisterReceiver(mReceiver);
        if (bitmap != null) {
            bitmap = null;
        }
        System.gc();
        

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        
        /**
         * 初始化分享方式，验证是否需要重新授权
         */
        //QQ
        qOAuthV2 = Utility.getQQOAuthPrefs(this); 
        
        if (qOAuthV2 !=null) {
            //按钮高亮
            mButton_qq.setImageResource(R.drawable.icon_qq);
            mQQTokenCanUse = true;
        } else {
            //按钮变灰
            mButton_qq.setImageResource(R.drawable.icon_qq_gray);
            mQQTokenCanUse = false;
        }
        //SINA
        mSinaAccessToken = Utility.getSinaOAuth2Prefs(this); 
        
        if (mSinaAccessToken !=null) {
            //按钮高亮
            mButton_sina.setImageResource(R.drawable.icon_sina);
            mSinaTokenCanUse = true;
        } else {
            //按钮变灰
            mButton_sina.setImageResource(R.drawable.icon_sina_gray);
            mSinaTokenCanUse = false;
        }
        
    }
    
    
    @Override
    protected void afterServiceConnected() {

    }

    @Override
    public void handleSeviceResult(String err_msg, int eventId, Object rlt) {

        if (pd !=null && pd.isShowing()) {
            pd.dismiss();
        }
        
        switch (eventId) {
            case Constants.STORY_RELEASE_SUCCESS:
                Toast.makeText(NewStoryFlowActivity.this,
                        getResources().getString(R.string.release_story_success),
                        Toast.LENGTH_SHORT).show();
                
                Intent intent = new Intent(Constants.RELEASE_STORY_SUCCESS_ACTION);
                sendBroadcast(intent);
                
                Intent intent2 = new Intent(NewStoryFlowActivity.this, MainActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent2);
                finish();
                break;
            case Constants.STORY_RELEASE_FAILED:
                Toast.makeText(NewStoryFlowActivity.this,
                        getResources().getString(R.string.release_story_failed),
                        Toast.LENGTH_SHORT).show();
                break;

            default:
                break;
        }
        
        
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCancel:
                finish();
                break;
            case R.id.btnRelease:

                if (Utility.isNetworkAvailable(NewStoryFlowActivity.this)) {
                    /**
                     * 第一步，上传图片，成功后进行第二步操作，发送相关故事信息
                     */

                    if (TextUtils.isEmpty(mEditText_title.getText().toString())) {
                        Toast.makeText(NewStoryFlowActivity.this,
                                getResources().getString(R.string.release_story_title_is_null),
                                Toast.LENGTH_SHORT).show();
                    } else if(imageFileSize > 2*1024*1024){
                        Toast.makeText(NewStoryFlowActivity.this,
                                getResources().getString(R.string.release_story_pic_exceed),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        
                        if (qOAuthV2 != null) {
                            String status = String.format(getString(R.string.qq_status_text), mEditText_title.getText().toString()
                                    ,"http://www.gushiliu.com/?_t="+System.currentTimeMillis());
                            mService.sendQQWeiboWithPic(NewStoryFlowActivity.this, qOAuthV2,
                                    status, imageFilePath);
                        }

                        if (mSinaAccessToken != null) {
                            
                            String status = String.format(getString(R.string.sina_status_text), mEditText_title.getText().toString()
                                    ,"http://www.gushiliu.com/?_t="+System.currentTimeMillis());
                            
                            mService.sendSinaWeiboWithPic(NewStoryFlowActivity.this,
                                    mSinaAccessToken.getToken(), status,
                                    imageFilePath);
                        }
                        
                        
                        pd = new ProgressDialog(NewStoryFlowActivity.this);
                        pd.setMessage(getResources().getString(R.string.upload_pic_loading));
                        pd.setCanceledOnTouchOutside(false);
                        pd.show();

                        String picName = imageFilePath.substring(
                                imageFilePath.lastIndexOf("/") + 1, imageFilePath.length());

                        new PictureUploadAsyncTask(NewStoryFlowActivity.this).execute(new Object[] {
                                imageFilePath, picName
                        });
                    }

                } else {
                    Toast.makeText(NewStoryFlowActivity.this,
                            getResources().getString(R.string.no_network_connection_toast),
                            Toast.LENGTH_SHORT).show();
                }
                
                
                break;
            case R.id.story_title:
                Intent intent = new Intent(NewStoryFlowActivity.this,
                        SearchStoryTitleActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            case R.id.btn_share_qq:
                
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
                    Intent intent2 = new Intent(NewStoryFlowActivity.this,
                            OAuthV2AuthorizeWebView.class);
                    intent2.putExtra("oauth", qOAuthV2);
                    startActivityForResult(intent2, 2);

                }
                
                break;
                
            case R.id.btn_share_sina:
                
                /**
                 * 判断是否需要登录授权
                 */
                if (!mSinaTokenCanUse) {
                    // 登录sina weibo授权页面

                    Weibo weibo = Weibo.getInstance();
                    weibo.setupConsumerConfig(Constants.WEIBO_CONSUMER_KEY,
                            Constants.WEIBO_CONSUMER_SECRET);
                    weibo.setRedirectUrl(Constants.WEIBO_REDIRECT_URI);
                    weibo.authorize(NewStoryFlowActivity.this,
                            NewStoryFlowActivity.this);

                }
                
                break;
            default:
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String storyTitle = data.getStringExtra("storyTitle");
                
                if (TextUtils.isEmpty(mBundleStoryId)) {
                    storyId = data.getStringExtra("storyId");
                }
                mEditText_title.setText(storyTitle);
            }
        } else if (requestCode == 2) {
            if (resultCode == OAuthV2AuthorizeWebView.RESULT_CODE) {
                qOAuthV2 = (OAuthV2) data.getExtras().getSerializable("oauth");
                /**
                 * 更新本地保存值
                 */
                Utility.updateQQOAuthPrefs(NewStoryFlowActivity.this, qOAuthV2);
                
                /**
                 * 初始化分享方式，验证是否需要重新授权
                 */
                if (qOAuthV2 !=null) {
                    //按钮高亮
                    mButton_qq.setImageResource(R.drawable.icon_qq);
                    mQQTokenCanUse = true;
                } else {
                    //按钮变灰
                    mButton_qq.setImageResource(R.drawable.icon_qq_gray);
                    mQQTokenCanUse = false;
                }
                
            }
        }
        
        super.onActivityResult(requestCode, resultCode, data);
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
            Utility.updateSINAOAuthPrefs(NewStoryFlowActivity.this, accessToken);
            
            mButton_sina.setImageResource(R.drawable.icon_sina);
            mSinaTokenCanUse = true;
        } else {
            //按钮变灰
            mButton_sina.setImageResource(R.drawable.icon_sina_gray);
            mSinaTokenCanUse = false;
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
}

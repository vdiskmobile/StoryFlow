
package com.youqude.storyflow.adapter;

import com.tencent.weibo.oauthv2.OAuthV2;
import com.tencent.weibo.oauthv2.OAuthV2Client;
import com.tencent.weibo.webview.OAuthV2AuthorizeWebView;
import com.youqude.storyflow.R;
import com.youqude.storyflow.StoryAPI;
import com.youqude.storyflow.StoryFlowApp;
import com.youqude.storyflow.StoryFlowDataService;
import com.youqude.storyflow.baidu.BaiduDialogListener;
import com.youqude.storyflow.baidu.BaiduOAuthViaDialog;
import com.youqude.storyflow.domain.AlbumInfo;
import com.youqude.storyflow.domain.ImageCache;
import com.youqude.storyflow.exception.StatusCodeException;
import com.youqude.storyflow.exception.StoryFlowException;
import com.youqude.storyflow.net.DeleteOperationAsyncTask;
import com.youqude.storyflow.net.DownloadAsyncTask;
import com.youqude.storyflow.net.ImageDownloader;
import com.youqude.storyflow.net.LikeOperationAsyncTask;
import com.youqude.storyflow.net.QqDownloadAsyncTask;
import com.youqude.storyflow.ui.LoginActivity;
import com.youqude.storyflow.utils.Constants;
import com.youqude.storyflow.utils.StoryLogger;
import com.youqude.storyflow.utils.Utility;
import com.youqude.storyflow.weibo.AccessToken;
import com.youqude.storyflow.weibo.DialogError;
import com.youqude.storyflow.weibo.Weibo;
import com.youqude.storyflow.weibo.WeiboDialogListener;
import com.youqude.storyflow.weibo.WeiboException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class StoryFlowDescriptionAdapter extends BaseAdapter {

    private static final String TAG = StoryFlowDescriptionAdapter.class.getSimpleName();

    Activity mContext;

    public static String mdownloadURL = "";
    public static AlbumInfo mShareAlbumInfo = null;
    public static boolean mFromSelfFlow = true;
    
    ArrayList<AlbumInfo> data;

    private ImageCache mImageCache;

    private ListView mListView;
    
    
    
    ViewDesHolder holder;
    ImageDownloader downloader;
    
    private boolean flag = false;//判断是否喜欢成功
    private String userId;

    public StoryFlowDescriptionAdapter(Activity context, ImageCache mImageCache, ListView mListView) {
        this.mContext = context;
        this.mImageCache = mImageCache;
        this.mListView = mListView;
        
        downloader = new ImageDownloader();
    }

    @Override
    public int getCount() {

        if (data != null) {
            return data.size();
        }

        return 0;

    }

    public void setData(ArrayList<AlbumInfo> data) {
        this.data = data;
    }

    @Override
    public AlbumInfo getItem(int position) {
        return this.data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void notifyChanged(String userId) {
        notifyDataSetChanged();
        this.userId = userId;
    }
    
    public void clearAdapter(){
        
        if (this.data != null) {
            this.data.clear();
            notifyDataSetChanged();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.story_self_description_item, null);
            holder = new ViewDesHolder();
            holder.mImageView_Header = (ImageView) convertView.findViewById(R.id.ivHeader);
            holder.mTextView_NickName = (TextView) convertView.findViewById(R.id.tvNickName);
//            holder.mTextView_Title = (TextView) convertView.findViewById(R.id.tvTitle);
            holder.mImageView_Pic = (ImageView) convertView.findViewById(R.id.ivPic);
            holder.mTextView_Description = (TextView) convertView.findViewById(R.id.tvDescription);
            holder.mButton_like = (Button) convertView.findViewById(R.id.btnLike);
            holder.mButton_save = (Button) convertView.findViewById(R.id.btnSave);

            convertView.setTag(holder);
        }

        holder = (ViewDesHolder) convertView.getTag();
        holder.mTextView_NickName.setText(data.get(position).nickname);
        holder.mTextView_Description.setText(data.get(position).description);
//        holder.mTextView_Title.setText(StoryFlowApp.getInstance().storyTitle);

        if (data.get(position).isLiked) {
            holder.mButton_like.setBackgroundResource(R.drawable.btn_unlike);
            holder.mButton_like.setText(mContext.getString(R.string.btn_liked));
        } else {
            holder.mButton_like.setBackgroundResource(R.drawable.btn_like);
            holder.mButton_like.setText(mContext.getString(R.string.btn_unlike));
        }
        
        
        
        holder.mButton_like.setOnClickListener(mOnClickListener);
        holder.mButton_save.setOnClickListener(mOnClickListener);
        holder.mImageView_Header.setOnClickListener(mOnClickListener);
        holder.mTextView_NickName.setOnClickListener(mOnClickListener);

       /* Bitmap bitmap = mImageCache.get(data.get(position).picPath);
        if (bitmap == null) {
            *//**
             * 设置默认图片
             *//*
            
             * image.setImageBitmap(Utils.getFileIcon(ctx,
             * R.drawable.picture_icon));
             
        } else {
            holder.mImageView_Pic.setImageBitmap(bitmap);
        }*/
       
        if (data !=null && !data.isEmpty()) {
            String url = data.get(position).picPath;
            try {
                downloader.download(url, holder.mImageView_Pic, mContext);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        Bitmap bitmap2 = mImageCache.get(data.get(position).userAvatar);
        if (bitmap2 != null) {
            bitmap2 = Utility.toRoundCorner(bitmap2, 90);
            holder.mImageView_Header.setImageBitmap(bitmap2);
        } 

        return convertView;
    }

    class ViewDesHolder {
        public ImageView mImageView_Header;
        public TextView mTextView_NickName;
        public TextView mTextView_Title;
        public ImageView mImageView_Pic;
        public TextView mTextView_Description;
        public Button mButton_like;
        public Button mButton_save;

    }

    OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            final int position = mListView.getPositionForView((View) v.getParent());

            switch (v.getId()) {
                case R.id.btnLike:{
                    
                    StoryLogger.e(TAG, TAG + ":" + position);
                    
                    AlbumInfo albumInfo = data.get(position);
                    
                    if (albumInfo.isLiked) {
                        //执行取消喜欢逻辑
                        new LikeOperationAsyncTask(mContext, true, StoryFlowDescriptionAdapter.this, albumInfo).execute(new Object[]{
                                albumInfo.id
                        });
                    } else {
                        //执行喜欢逻辑
                        new LikeOperationAsyncTask(mContext, false, StoryFlowDescriptionAdapter.this, albumInfo).execute(new Object[]{
                                albumInfo.id
                        });;
                    }
                   
                    break;
                }
                case R.id.btnSave:
                    showDialogItems(position);
                    break;
                case R.id.ivHeader:{
                    StoryLogger.e(TAG, TAG+"------>++++++++++++++++++++++");
                    String userId = data.get(position).userId;
                    Intent intent = new Intent(Constants.ENTER_OTHERS_BY_NICKNAME_PIC_ACTION);
                    intent.putExtra("mCurrentUserId", userId);
                    mContext.sendBroadcast(intent);
                   
                    break;
                }
                case R.id.tvNickname:{
                    StoryLogger.e(TAG, TAG+"------>++++++++++++++++++++++");
                    String userId = data.get(position).userId;
                    Intent intent = new Intent(Constants.ENTER_OTHERS_BY_NICKNAME_PIC_ACTION);
                    intent.putExtra("mCurrentUserId", userId);
                    mContext.sendBroadcast(intent);
                    break;
                }
                default:
                    break;
            }

        }
    };

    public void showDialogItems(int pos) {

        final ArrayList<String> items = new ArrayList<String>();
        final int position = pos;

        final AlbumInfo mAlbumInfo = data.get(position);
        
        /**
         * 判断userId是否是当前登录用户,是,具备删除操作,否,不具备删除操作
         */
       /* if (userId.equals(StoryFlowApp.getInstance().userInfo.uid)) {
            items.add(mContext.getString(R.string.btn_delete));
        }*/
        items.add(mContext.getString(R.string.btn_share));
        items.add(mContext.getString(R.string.btn_share_qq));
        items.add(mContext.getString(R.string.btn_baidu_save));
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                
//                if (!userId.equals(StoryFlowApp.getInstance().userInfo.uid)) {
                    

                    switch (item) {
                        case 0:
                            shareBySina(position, mAlbumInfo);
                            break;
                        case 1:
                            shareQq(position, mAlbumInfo);
                            break;
                        case 2:
                            //保存至百度网盘
                            String access_token = Utility.getBaiduOAuth2Prefs(mContext);
                            if (TextUtils.isEmpty(access_token)) {
                                //登陆百度
                                loginBaidu(position,mAlbumInfo);
                            } else {
                                //执行下载操作和保存操作
                                String downloadURL = mAlbumInfo.picPath;
                                new DownloadAsyncTask(mContext , access_token, false, mAlbumInfo).execute(new Object[]{
                                        downloadURL
                                });
                            }
                            break;
                        default:
                            break;
                    }
                
                    
//                } else {

                    /*switch (item) {
                        case 0:
                            //删除操作
                            new DeleteOperationAsyncTask(mContext, StoryFlowDescriptionAdapter.this, data, mAlbumInfo)
                            .execute(new Object[]{
                                    mAlbumInfo.id
                            });
                            break;
                        case 1:
                            shareBySina(position);
                            break;
                        case 2:
                            //保存至百度网盘
                            String access_token = Utility.getBaiduOAuth2Prefs(mContext);
                            if (TextUtils.isEmpty(access_token)) {
                                //登陆百度
                                loginBaidu(position);
                            } else {
                                //执行下载操作和保存操作
                                String downloadURL = data.get(position).picPath;
                                new DownloadAsyncTask(mContext , access_token, false).execute(new Object[]{
                                        downloadURL
                                });
                            }
                            break;
                        default:
                            break;
                    }*/
                
//                }
                
            }

        };
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setItems(items.toArray(new CharSequence[0]), listener);
        final AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(true);
        Window mWindow = alert.getWindow();
        try {
            alert.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    /**
     * 登录百度授权，并进行下载保存操作
     * @param position
     */
    private void loginBaidu(final int position, final AlbumInfo mAlbumInfo){
        BaiduOAuthViaDialog mBaiduOAuth = new BaiduOAuthViaDialog(Constants.API_KEY);
        mBaiduOAuth.startDialogAuth(mContext, new String[] {
                "basic", "netdisk"
        }, new BaiduDialogListener() {

            @Override
            public void onCancel() {
                Toast.makeText(mContext, "取消授权登录 ",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onComplete(Bundle values) {
                
                String access_token = values.getString("access_token");
                
                Utility.updateBaiduOAuthPrefs(mContext, access_token, 
                        values.getString("expires_in"), values.getString("session_key"), values.getString("session_secret"));
                //执行下载操作和保存操作
                String downloadURL = data.get(position).picPath;
                new DownloadAsyncTask(mContext , access_token, false, mAlbumInfo).execute(new Object[]{
                        downloadURL
                });
            }

            @Override
            public void onException(String msg) {
                Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void shareBySina(final int position, final AlbumInfo mAlbumInfo){
      //分享操作
        
        AccessToken accessToken = Utility.getSinaOAuth2Prefs(mContext);
        
        if (accessToken!=null && !TextUtils.isEmpty(accessToken.getToken())) {
            //先下载再分享
            String downloadURL = data.get(position).picPath;
            new DownloadAsyncTask(mContext , accessToken.getToken(), true, mAlbumInfo).execute(new Object[]{
                    downloadURL
            });
        } else {
            Weibo weibo = Weibo.getInstance();
            weibo.setupConsumerConfig(Constants.WEIBO_CONSUMER_KEY,
                    Constants.WEIBO_CONSUMER_SECRET);
            weibo.setRedirectUrl(Constants.WEIBO_REDIRECT_URI);
            weibo.authorize(mContext,
                    new WeiboDialogListener() {
                        
                        @Override
                        public void onWeiboException(WeiboException e) {

                            Toast.makeText(mContext.getApplicationContext(),
                                    "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG)
                                    .show();
                        }
                        
                        @Override
                        public void onError(DialogError e) {
                            Toast.makeText(mContext.getApplicationContext(),
                                    "取消授权登录  : " + e.getMessage(), Toast.LENGTH_LONG).show();                                        
                        }
                        
                        @Override
                        public void onComplete(Bundle values) {

                            String token = values.getString("access_token");
                            String expires_in = values.getString("expires_in");
                            AccessToken accessToken = new AccessToken(token, Constants.WEIBO_CONSUMER_SECRET);
                            accessToken.setExpiresIn(expires_in);
                            Weibo.getInstance().setAccessToken(accessToken);
                            
                            Utility.updateSINAOAuthPrefs(mContext, accessToken);
                            
                            //先下载再分享
                            String downloadURL = data.get(position).picPath;
                            new DownloadAsyncTask(mContext , accessToken.getToken(), true, mAlbumInfo).execute(new Object[]{
                                    downloadURL
                            });
                        }
                        
                        @Override
                        public void onCancel() {
                            Toast.makeText(mContext.getApplicationContext(), "取消授权登录 ",
                                    Toast.LENGTH_LONG).show();                                        
                        }
                    });
        }
    }

    
    private void shareQq(final int position, final AlbumInfo mAlbumInfo){
        // qq t 分享操作
        OAuthV2 qOAuthV2 = Utility.getQQOAuthPrefs(mContext); 
        String downloadURL = data.get(position).picPath;
        if (qOAuthV2 != null) {
            // 先下载再分享
            new QqDownloadAsyncTask(mContext, qOAuthV2, mAlbumInfo)
            .execute(new Object[] {
                    downloadURL
            });
        } else {
            qOAuthV2 = new OAuthV2(Constants.QQ_REDIRECT_URI);
            qOAuthV2.setClientId(Constants.QQ_CLIENT_ID);
            qOAuthV2.setClientSecret(Constants.QQ_CLIENT_SECRET);
            // 关闭OAuthV2Client中的默认开启的QHttpClient。
            OAuthV2Client.getQHttpClient().shutdownConnection();
            // 创建Intent，使用WebView让用户授权
            Intent intent2 = new Intent(mContext,
                    OAuthV2AuthorizeWebView.class);
            intent2.putExtra("oauth", qOAuthV2);
//            intent2.putExtra("downloadURL", downloadURL);
//            intent2.putExtra("mAlbumInfo", mAlbumInfo);
            mdownloadURL = downloadURL;
            mShareAlbumInfo = mAlbumInfo;
            mFromSelfFlow = false;
            mContext.startActivityForResult(intent2, 2);
        }
    }
    
}


package com.youqude.storyflow;

import com.youqude.storyflow.domain.BaiduUserInfo;
import com.youqude.storyflow.domain.QUserInfo;
import com.youqude.storyflow.domain.SinaUserInfo;
import com.youqude.storyflow.domain.UserInfo;
import com.youqude.storyflow.exception.StatusCodeException;
import com.youqude.storyflow.exception.StoryFlowException;
import com.youqude.storyflow.net.GzipDecompressingEntity;
import com.youqude.storyflow.net.HttpManager;
import com.youqude.storyflow.utils.BASE64Encoder;
import com.youqude.storyflow.utils.Constants;
import com.youqude.storyflow.utils.StoryLogger;
import com.youqude.storyflow.utils.Utility;
import com.youqude.storyflow.weibo.WeiboParameters;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

public class StoryAPI {

    private static String TAG = StoryAPI.class.getSimpleName();

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";

    
    public enum PlatformType {
        SINA("sina"), QQ("qq"), BAIDU("baidu");

        private final String type;

        private PlatformType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }
    
    
    /**
     * 获取使用qq登录后的相关信息
     * 
     * @param ctx
     * @param token
     * @param openid
     * @param openkey
     * @return
     */
    public static JSONObject getQLoginInfo(Context ctx, String token, String openid, String openkey)
            throws StatusCodeException, StoryFlowException{
        
        StoryLogger.e(TAG, TAG+":openid-->"+openid);
        StoryLogger.e(TAG, TAG+":token-->"+token);
        StoryLogger.e(TAG, TAG+":openkey-->"+openkey);
        
        ArrayList<NameValuePair> form = new ArrayList<NameValuePair>();
        form.add(new BasicNameValuePair("thirdUid", openid));
        form.add(new BasicNameValuePair("token", token));
        form.add(new BasicNameValuePair("openid", openid));
        form.add(new BasicNameValuePair("openkey", openkey));
        String result = requestData(METHOD_GET, form,
                Constants.Q_LOGIN_URL, ctx);

        JSONObject jo = null;
        
        try {
            jo = new JSONObject(result);
        } catch (JSONException e) {
            return null;
        }
         
        return jo;
    }

    /**
     * 
     * @param ctx
     * @param thirdUid
     * @param token
     * @param openid
     * @param openkey
     * @return
     * @throws StatusCodeException
     * @throws StoryFlowException
     */
    static public JSONObject getBaiduLoginInfo(Context ctx, String thirdUid, String token,
            String sessionKey, String sessionSecret)
            throws StatusCodeException, StoryFlowException {
        ArrayList<NameValuePair> form = new ArrayList<NameValuePair>();
        form.add(new BasicNameValuePair("thirdUid", thirdUid));
        form.add(new BasicNameValuePair("token", token));
        BASE64Encoder encoder = new BASE64Encoder();
        form.add(new BasicNameValuePair("sessionKey", encoder.encode(sessionKey.getBytes())));
        form.add(new BasicNameValuePair("sessionSecret", sessionSecret));
        String result = requestData(METHOD_GET, form,
                Constants.BAIDU_LOGIN_URL, ctx);

        JSONObject jo = null;

        try {
            jo = new JSONObject(result);
        } catch (JSONException e) {
            return null;
        }

        return jo;
    }
    
    /**
     * 
     * @param ctx
     * @param thirdUid
     * @param token
     * @return
     * @throws StatusCodeException
     * @throws StoryFlowException
     */
    static public JSONObject getSinaLoginInfo(Context ctx, String thirdUid, String token)
                    throws StatusCodeException, StoryFlowException {
        ArrayList<NameValuePair> form = new ArrayList<NameValuePair>();
        form.add(new BasicNameValuePair("thirdUid", thirdUid));
        form.add(new BasicNameValuePair("token", token));
        String result = requestData(METHOD_GET, form,
                Constants.SINA_LOGIN_URL, ctx);
        
        JSONObject jo = null;
        
        try {
            jo = new JSONObject(result);
        } catch (JSONException e) {
            return null;
        }
        
        return jo;
    }
    
    /**
     * sina user show
     * @param ctx
     * @param sina_uid
     * @param userInfo
     * @return
     * @throws StatusCodeException
     * @throws StoryFlowException
     */
    static public JSONObject getSinaUserShow(Context ctx, String sina_uid, UserInfo userInfo)
            throws StatusCodeException, StoryFlowException {

        ArrayList<NameValuePair> form = new ArrayList<NameValuePair>();
        form.add(new BasicNameValuePair("uid", sina_uid));
        form.add(new BasicNameValuePair("access_token", userInfo.token));
        String result = requestOpenData(METHOD_GET, form,
                Constants.SINA_USER_SHOW_URL, ctx, null);

        JSONObject jo = null;

        try {
            jo = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return jo;

    }
    /**
     * 通过关键字查询故事流列表
     * @param ctx
     * @param keywords
     * @return
     * @throws StatusCodeException
     * @throws StoryFlowException
     */
    
    static public JSONObject searchStoryTitleByKeywords(Context ctx, String keywords)
            throws StatusCodeException, StoryFlowException {
        
        String requestData = String.format("<req>" +
                    "<keywords>%s</keywords>" +
                    "</req>", keywords);
        
        String result = requestData(METHOD_POST, null,
                Constants.STORY_TITLE_LIST_SEARCH_URL, ctx, requestData);
        
        JSONObject jo = null;
        
        try {
            jo = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        
        return jo;
        
    }
    
    /**
     * 根据相应storyId获取所有图片数据
     * @param ctx
     * @param storyId
     * @param pageNum
     * @param pageSize
     * @return
     * @throws StatusCodeException
     * @throws StoryFlowException
     */
    static public JSONObject searchStoryPicById(Context ctx, String storyId, int pageNum, int pageSize)
            throws StatusCodeException, StoryFlowException {
        
        ArrayList<NameValuePair> form = new ArrayList<NameValuePair>();
        form.add(new BasicNameValuePair("storyId", storyId));
        form.add(new BasicNameValuePair("pageNum", String.valueOf(pageNum)));
        form.add(new BasicNameValuePair("pageSize", String.valueOf(pageSize)));
        String result = requestData(METHOD_GET, form,
                Constants.STORY_PIC_LIST_SEARCH_URL, ctx);

        JSONObject jo = null;

        try {
            jo = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        
        return jo;
        
    }
    /**
     * 根据storyId获取story detail
     * @param ctx
     * @param storyId
     * @return
     * @throws StatusCodeException
     * @throws StoryFlowException
     */
    static public JSONObject getStoryDetailById(Context ctx, String storyId)
            throws StatusCodeException, StoryFlowException {
        
        ArrayList<NameValuePair> form = new ArrayList<NameValuePair>();
        form.add(new BasicNameValuePair("storyId", storyId));
        String result = requestData(METHOD_GET, form,
                Constants.STORY_DETAIL_BY_STORY_ID_URL, ctx);
        
        JSONObject jo = null;
        
        try {
            jo = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        
        return jo;
        
    }
    
    /**
     * 获取今日最热的故事
     * @param ctx
     * @param pageNum
     * @param pageSize
     * @return
     * @throws StatusCodeException
     * @throws StoryFlowException
     */
    
    static public JSONObject getTodayHotStoryList(Context ctx, int pageNum, int pageSize)
            throws StatusCodeException, StoryFlowException {
        
        ArrayList<NameValuePair> form = new ArrayList<NameValuePair>();
        form.add(new BasicNameValuePair("pageNum", String.valueOf(pageNum)));
        form.add(new BasicNameValuePair("pageSize", String.valueOf(pageSize)));
        String result = requestData(METHOD_GET, form,
                Constants.STORY_HOT_TODAY_LIST_URL, ctx);
        
        JSONObject jo = null;
        
        try {
            jo = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        
        return jo;
        
    }
    /**
     * 获取本周最热故事
     * @param ctx
     * @param pageNum
     * @param pageSize
     * @return
     * @throws StatusCodeException
     * @throws StoryFlowException
     */
    static public JSONObject getWeekHotStoryList(Context ctx, int pageNum, int pageSize)
            throws StatusCodeException, StoryFlowException {
        
        ArrayList<NameValuePair> form = new ArrayList<NameValuePair>();
        form.add(new BasicNameValuePair("pageNum", String.valueOf(pageNum)));
        form.add(new BasicNameValuePair("pageSize", String.valueOf(pageSize)));
        String result = requestData(METHOD_GET, form,
                Constants.STORY_HOT_WEEK_LIST_URL, ctx);
        
        JSONObject jo = null;
        
        try {
            jo = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        
        return jo;
        
    }
    /**
     * 获取最热的故事
     * @param ctx
     * @param pageNum
     * @param pageSize
     * @return
     * @throws StatusCodeException
     * @throws StoryFlowException
     */
    static public JSONObject getLatestHotStoryList(Context ctx, int pageNum, int pageSize)
            throws StatusCodeException, StoryFlowException {
        
        ArrayList<NameValuePair> form = new ArrayList<NameValuePair>();
        form.add(new BasicNameValuePair("pageNum", String.valueOf(pageNum)));
        form.add(new BasicNameValuePair("pageSize", String.valueOf(pageSize)));
        String result = requestData(METHOD_GET, form,
                Constants.STORY_HOT_LATEST_LIST_URL, ctx);
        
        JSONObject jo = null;
        
        try {
            jo = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        
        return jo;
        
    }
    /**
     *  获取用户基本信息，用于显示昵称和头像等
     * @param ctx
     * @param userId
     * @return
     * @throws StatusCodeException
     * @throws StoryFlowException
     */
    static public JSONObject loadUserProfileAction(Context ctx, String userId)
            throws StatusCodeException, StoryFlowException {
        
        ArrayList<NameValuePair> form = new ArrayList<NameValuePair>();
        form.add(new BasicNameValuePair("userId", userId));
        String result = requestData(METHOD_GET, form,
                Constants.STORY_USER_PROFILE_URL, ctx);
        
        JSONObject jo = null;
        
        try {
            jo = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        
        return jo;
        
    }
    
    /**
     * 获取用户tab标签数量
     * @param ctx
     * @param userId
     * @return
     * @throws StatusCodeException
     * @throws StoryFlowException
     */
    static public JSONObject loadUserTabCountAction(Context ctx, String userId)
            throws StatusCodeException, StoryFlowException {
        
        ArrayList<NameValuePair> form = new ArrayList<NameValuePair>();
        form.add(new BasicNameValuePair("userId", userId));
        String result = requestData(METHOD_GET, form,
                Constants.USER_TAB_COUNT_URL, ctx);
        
        JSONObject jo = null;
        
        try {
            jo = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        
        return jo;
        
    }
    
    /**
     * 执行喜欢操作
     * @param ctx
     * @param picid
     * @return
     * @throws StatusCodeException
     * @throws StoryFlowException
     */
    static public boolean startStoryFlowLikeAction(Context ctx, String picid)
            throws StatusCodeException, StoryFlowException {
        
        ArrayList<NameValuePair> form = new ArrayList<NameValuePair>();
        form.add(new BasicNameValuePair("sessionid", StoryFlowApp.getInstance().userInfo.sessionId));
        form.add(new BasicNameValuePair("picid", picid));
        String result = requestData(METHOD_GET, form,
                Constants.USER_LIKE_ACTION_URL, ctx);
        
        JSONObject jo = null;
        String rlt;
        
        try {
            jo = new JSONObject(result);
            JSONObject jsonObject = jo.optJSONObject("resp");
            if (jsonObject !=null) {
                rlt = jsonObject.optString("infocode", null);
                
                if (rlt !=null && rlt.equals("200")) {
                    
                    return true;
                }
            }
            
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        
        return false;
        
    }
    
    /**
     * 取消喜欢
     * @param ctx
     * @param picid
     * @return
     * @throws StatusCodeException
     * @throws StoryFlowException
     */
    static public boolean cancelStoryFlowLikeAction(Context ctx, String picid)
            throws StatusCodeException, StoryFlowException {
        
        ArrayList<NameValuePair> form = new ArrayList<NameValuePair>();
        form.add(new BasicNameValuePair("sessionid", StoryFlowApp.getInstance().userInfo.sessionId));
        form.add(new BasicNameValuePair("picid", picid));
        String result = requestData(METHOD_GET, form,
                Constants.USER_UNLIKE_ACTION_URL, ctx);
        
        JSONObject jo = null;
        String rlt;
        
        try {
            jo = new JSONObject(result);
            JSONObject jsonObject = jo.optJSONObject("resp");
            if (jsonObject !=null) {
                rlt = jsonObject.optString("infocode", null);
                
                if (rlt !=null && rlt.equals("200")) {
                    
                    return true;
                }
            }
            
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        
        return false;
        
    }
    
    /**
     * 删除用户自己图片
     * @param ctx
     * @param picid
     * @return
     * @throws StatusCodeException
     * @throws StoryFlowException
     */
    static public boolean deleteStoryFlowLikeAction(Context ctx, String picid)
            throws StatusCodeException, StoryFlowException {
        
        ArrayList<NameValuePair> form = new ArrayList<NameValuePair>();
        form.add(new BasicNameValuePair("sessionid", StoryFlowApp.getInstance().userInfo.sessionId));
        form.add(new BasicNameValuePair("picid", picid));
        String result = requestData(METHOD_GET, form,
                Constants.USER_DELETE_ACTION_URL, ctx);
        
        JSONObject jo = null;
        String rlt;
        
        try {
            jo = new JSONObject(result);
            JSONObject jsonObject = jo.optJSONObject("resp");
            if (jsonObject !=null) {
                rlt = jsonObject.optString("infocode", null);
                
                if (rlt !=null && rlt.equals("200")) {
                    
                    return true;
                }
            }
            
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        
        return false;
        
    }
    /**
     * 判断用户喜欢状态
     * @param ctx
     * @param picid
     * @return
     * @throws StatusCodeException
     * @throws StoryFlowException
     */
    static public String loadUserLikeStateAction(Context ctx, String picid)
            throws StatusCodeException, StoryFlowException {
        
        ArrayList<NameValuePair> form = new ArrayList<NameValuePair>();
        form.add(new BasicNameValuePair("sessionid", StoryFlowApp.getInstance().userInfo.sessionId));
        form.add(new BasicNameValuePair("picid", picid));
        String result = requestData(METHOD_GET, form,
                Constants.USER_LIKE_STATE_URL, ctx);
        
        JSONObject jo = null;
        String state;
        try {
            jo = new JSONObject(result);
            JSONObject jsonObject = jo.optJSONObject("resp");
            if (jsonObject !=null) {
                state = jsonObject.optString("likeStatus", null);
                return state;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        
        return null;
        
    }
    /**
     * 获取用户发布的图片列表
     * @param ctx
     * @param userId
     * @return
     * @throws StatusCodeException
     * @throws StoryFlowException
     */
    static public JSONObject loadUserReleasePicAction(Context ctx, String userId, int pageNum, int pageSize)
            throws StatusCodeException, StoryFlowException {
        
        ArrayList<NameValuePair> form = new ArrayList<NameValuePair>();
        form.add(new BasicNameValuePair("userId", userId));
        form.add(new BasicNameValuePair("pageNum", String.valueOf(pageNum)));
        form.add(new BasicNameValuePair("pageSize", String.valueOf(pageSize)));
        String result = requestData(METHOD_GET, form,
                Constants.USER_RELEASE_PIC_LIST_URL, ctx);
        
        JSONObject jo = null;
        
        try {
            jo = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        
        return jo;
        
    }
    /**
     * 获取用户喜欢的图片列表
     * @param ctx
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     * @throws StatusCodeException
     * @throws StoryFlowException
     */
    static public JSONObject loadUserLovePicAction(Context ctx, String userId, int pageNum, int pageSize)
            throws StatusCodeException, StoryFlowException {
        
        ArrayList<NameValuePair> form = new ArrayList<NameValuePair>();
        form.add(new BasicNameValuePair("userId", userId));
        form.add(new BasicNameValuePair("pageNum", String.valueOf(pageNum)));
        form.add(new BasicNameValuePair("pageSize", String.valueOf(pageSize)));
        String result = requestData(METHOD_GET, form,
                Constants.USER_LOVE_PIC_LIST_URL, ctx);
        
        JSONObject jo = null;
        
        try {
            jo = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        
        return jo;
        
    }
    /**
     * 获取用户参与过的故事
     * @param ctx
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     * @throws StatusCodeException
     * @throws StoryFlowException
     */
    static public JSONObject loadUserParticipateStoryFlowAction(Context ctx, String userId, int pageNum, int pageSize)
            throws StatusCodeException, StoryFlowException {
        
        ArrayList<NameValuePair> form = new ArrayList<NameValuePair>();
        form.add(new BasicNameValuePair("userId", userId));
        form.add(new BasicNameValuePair("pageNum", String.valueOf(pageNum)));
        form.add(new BasicNameValuePair("pageSize", String.valueOf(pageSize)));
        String result = requestData(METHOD_GET, form,
                Constants.USER_PART_STORY_FLOW_LIST_URL, ctx);
        
        JSONObject jo = null;
        
        try {
            jo = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        
        return jo;
        
    }
    
    /**
     * 查询当前用户喜欢状态列表
     * @param ctx
     * @param storyId
     * @param pageNum
     * @param pageSize
     * @return
     * @throws StatusCodeException
     * @throws StoryFlowException
     */
    static public JSONObject loadLikedStateStoryFlowAction(Context ctx, String storyId)
            throws StatusCodeException, StoryFlowException {
        
        ArrayList<NameValuePair> form = new ArrayList<NameValuePair>();
       /* form.add(new BasicNameValuePair("sessionId", "C71EB21ADA09A4161215B46A77248A42"));
        form.add(new BasicNameValuePair("storyId", "1"));*/
        form.add(new BasicNameValuePair("sessionId", StoryFlowApp.getInstance().userInfo.sessionId));
        form.add(new BasicNameValuePair("storyId", storyId));
        String result = requestData(METHOD_GET, form,
                Constants.USER_LIKE_STATE_ACTION_URL, ctx);
        
        JSONObject jo = null;
        
        try {
            jo = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        
        return jo;
        
    }
    
    /**
     * 
     * @param ctx
     * @param object
     * @param appType
     * @param sessionId
     * @return
     * @throws StatusCodeException
     * @throws StoryFlowException
     */
    static public JSONObject addBaseProfile(Context ctx, Object object, String appType,
            String sessionId)
            throws StatusCodeException, StoryFlowException {

        String session = "";
        if (appType.equals(PlatformType.SINA.toString())) {
            SinaUserInfo sinaUserInfo = (SinaUserInfo) object;
            session = String.format("<req>" +
                    "<sessionId>%s</sessionId>" +
                    "<nickName>%s</nickName>" +
                    "<location>%s</location>" +
                    "<userAvatar>%s</userAvatar>" +
                    "</req>", sessionId, sinaUserInfo.screen_name, sinaUserInfo.location,
                    sinaUserInfo.profile_image_url);
        } else if (appType.equals(PlatformType.QQ.toString())) {
            QUserInfo qUserInfo = (QUserInfo) object;
            session = String.format("<req>" +
                    "<sessionId>%s</sessionId>" +
                    "<nickName>%s</nickName>" +
                    "<location>%s</location>" +
                    "<userAvatar>%s</userAvatar>" +
                    "</req>", sessionId, qUserInfo.nick, qUserInfo.location, qUserInfo.head);
        } else if (appType.equals(PlatformType.BAIDU.toString())) {
            BaiduUserInfo baiduUserInfo = (BaiduUserInfo) object;
            session = String.format("<req>" +
                    "<sessionId>%s</sessionId>" +
                    "<nickName>%s</nickName>" +
                    "<location>%s</location>" +
                    "<userAvatar>%s</userAvatar>" +
                    "</req>", sessionId, baiduUserInfo.uname, baiduUserInfo.location,
                    baiduUserInfo.portrait);
        }

        StoryLogger.e(TAG, "ADD SESSION");
        StoryLogger.e(TAG, session);

        String result = requestData(METHOD_POST, null,
                Constants.ADD_BASE_PROFILE, ctx, session);

        JSONObject jo = null;

        try {
            jo = new JSONObject(result);
        } catch (JSONException e) {
            return null;
        }

        return jo;
    }
    
    /**
     * release story flow
     * @param ctx
     * @param title
     * @param description
     * @param picPath
     * @return
     * @throws StatusCodeException
     * @throws StoryFlowException
     */
    
    static public JSONObject sendStoryFlowAction(Context ctx, String title, String description,
            String picPath) throws StatusCodeException, StoryFlowException {
                    
        
        JSONObject jo = null;
        
        if (StoryFlowApp.getInstance().userInfo == null) {
            StoryLogger.e(TAG, TAG+"------------------------>");
        } else {
            String sessionId = StoryFlowApp.getInstance().userInfo.sessionId;
            String requestData = String.format("<req>" +
                        "<sessionId>%s</sessionId>" +
                        "<title>%s</title>" +
                        "<description>%s</description>" +
                        "<picPath>%s</picPath>" +
                        "</req>", sessionId, title, description,
                        picPath);
            
            StoryLogger.e(TAG, "RELEASE STORY FLOW");
            StoryLogger.e(TAG, requestData);
            
            String result = requestData(METHOD_POST, null,
                    Constants.STORY_PUBLISH_URL, ctx, requestData);
            
            
            try {
                jo = new JSONObject(result);
            } catch (JSONException e) {
                return null;
            }
        }
        
        
        
        return jo;
    }
    
    
    
    static public JSONObject sendStoryFlowWithStoryIdAction(Context ctx, String storyId, String description,
            String picPath) throws StatusCodeException, StoryFlowException {
        
        
        JSONObject jo = null;
        
        if (StoryFlowApp.getInstance().userInfo == null) {
            StoryLogger.e(TAG, TAG+"------------------------>");
        } else {
            String sessionId = StoryFlowApp.getInstance().userInfo.sessionId;
            String requestData = String.format("<req>" +
                    "<sessionId>%s</sessionId>" +
                    "<storyId>%s</storyId>" +
                    "<description>%s</description>" +
                    "<picPath>%s</picPath>" +
                    "</req>", sessionId, storyId, description,
                    picPath);
            
            StoryLogger.e(TAG, "RELEASE STORY FLOW");
            StoryLogger.e(TAG, requestData);
            
            String result = requestData(METHOD_POST, null,
                    Constants.STORY_PUBLISH_WITH_STORY_ID_URL, ctx, requestData);
            
            
            try {
                jo = new JSONObject(result);
            } catch (JSONException e) {
                return null;
            }
        }
        
        
        
        return jo;
    }
    
    
    
    
    /**
     * 
     * @param ctx
     * @param access_token
     * @return
     * @throws StatusCodeException
     * @throws StoryFlowException
     */
    static public JSONObject getBaiduUserInfo(Context ctx, String access_token)
            throws StatusCodeException, StoryFlowException{
        ArrayList<NameValuePair> form = new ArrayList<NameValuePair>();
        form.add(new BasicNameValuePair("access_token", access_token));
        String result = requestOpenData(METHOD_GET, form,
                Constants.BAIDU_USER_INFO_URL, ctx, null);
        
        JSONObject jo = null;
        
        try {
            jo = new JSONObject(result);
        } catch (JSONException e) {
            return null;
        }
        
        return jo;
    }
    /**
     * 
     * @param ctx
     * @param access_token
     * @return
     * @throws StatusCodeException
     * @throws StoryFlowException
     */
    static public JSONObject getSinaUidByToken(Context ctx, String access_token)
            throws StatusCodeException, StoryFlowException{
        ArrayList<NameValuePair> form = new ArrayList<NameValuePair>();
        form.add(new BasicNameValuePair("access_token", access_token));
        String result = requestOpenData(METHOD_GET, form,
                Constants.SINA_UID_URL, ctx, null);
        
        JSONObject jo = null;
        
        try {
            jo = new JSONObject(result);
        } catch (JSONException e) {
            return null;
        }
        
        return jo;
    }
    
    /**
     * 发布带图片的微博 sina
     * @param ctx
     * @param access_token
     * @param status
     * @param picPath
     * @return
     * @throws StatusCodeException
     * @throws StoryFlowException
     */
    static public JSONObject updateWithPicViaSina(Context ctx, String access_token, String status, String picPath)
            throws StatusCodeException, StoryFlowException{
        ArrayList<NameValuePair> form = new ArrayList<NameValuePair>();
        form.add(new BasicNameValuePair("access_token", access_token));
        form.add(new BasicNameValuePair("status", status));
        String result = requestOpenData(METHOD_POST, form,
                Constants.SINA_STATUS_UPDATE_URL, ctx, picPath);
        
        JSONObject jo = null;
        
        try {
            jo = new JSONObject(result);
        } catch (JSONException e) {
            return null;
        }
        
        return jo;
    }

    /**
     * 统一进行网络请求处理
     * @param httpMethod
     * @param params
     * @param url
     * @param ctx
     * @return
     * @throws StatusCodeException
     * @throws StoryFlowException
     */
    private static String requestData(String httpMethod,
            ArrayList<NameValuePair> params, String url, Context ctx)
            throws StatusCodeException, StoryFlowException {
        String rlt = "";
        HttpUriRequest method = null;

        String getUrlParams = "";
        if (params != null) {

            StringBuffer sb = new StringBuffer();
            for (NameValuePair nameValuePair : params) {
                if (httpMethod.equals(METHOD_GET)) {
                    try {
                        sb.append(URLEncoder.encode(nameValuePair.getValue(), HTTP.UTF_8) + "/");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    sb.append(nameValuePair.getValue() + "/");
                }
            }
            String content = sb.toString();
            if (!TextUtils.isEmpty(content)) {
                content = content.substring(0, content.length() - 1);
            }
            getUrlParams = content;
        }

        try {
            if (METHOD_POST.equals(httpMethod)) {
                url = url + ".json";
                HttpPost post = new HttpPost(url);

                if (params != null) {
                    post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                    post.setHeader("Accept-Encoding", "gzip");
                }

                method = post;
            } else if (METHOD_GET.equals(httpMethod)) {

                if (params != null) {
                    url = url + getUrlParams + ".json";
                }
                StoryLogger.e(TAG, TAG + ":" + url);
                method = new HttpGet(url);
                method.setHeader("Accept-Encoding", "gzip");
            }
            HttpClient client = HttpManager.sClient;
            
            Utility.sign(ctx, method);
            
            HttpResponse httpResponse = client.execute(method);
            StatusLine status = httpResponse.getStatusLine();
            int statusCode = status.getStatusCode();
            StoryLogger.e(TAG, TAG + ":" + statusCode);
            if (statusCode != HttpStatus.SC_OK) {
                throw new StatusCodeException(url, statusCode);
            } else {
                HttpEntity entity = httpResponse.getEntity();

                InputStream is = null;

                // 判断是否支持GZIP

                Header[] allHeaders = httpResponse.getAllHeaders();
                for (int i = 0; i < allHeaders.length; i++) {

                    if (allHeaders[i].getValue().equalsIgnoreCase("gzip")) {
                        StoryLogger.e(TAG, TAG + ":gzip");
                        StoryLogger.e(TAG, TAG + ":" + entity.getContentLength());
                        GzipDecompressingEntity decompressingEntity = new GzipDecompressingEntity(
                                entity);
                        is = decompressingEntity.getContent();
                    }
                }

                if (is == null) {
                    is = entity.getContent();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int count = 0;
                while ((count = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, count);
                }
                rlt = new String(baos.toByteArray());
                StoryLogger.e("content--->", "content-->" + url + ":" + rlt);
            }
        } catch (ConnectTimeoutException e) {
            throw new StoryFlowException("request data faild", e);
        } catch (SocketTimeoutException e) {
            // 上报日志
            throw new StoryFlowException("request data faild", e);
        } catch (StatusCodeException e) {
            throw new StatusCodeException(e.getUrl(), e.getStatusCode());
        } catch (Exception e) {
            throw new StoryFlowException("request data faild", e);
        }

        return rlt;
    }
    private static String requestData(String httpMethod,
            ArrayList<NameValuePair> params, String url, Context ctx, String session)
                    throws StatusCodeException, StoryFlowException {
        String rlt = "";
        HttpUriRequest method = null;
        
        String getUrlParams = "";
        
        try {
            if (METHOD_POST.equals(httpMethod)) {
                url = url + ".json";
                HttpPost post = new HttpPost(url);
                
                if (params != null) {
                    post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                    post.setHeader("Accept-Encoding", "gzip");
                }
                
                byte[] data = null;
//                post.setHeader("Content-Type", "application/x-www-form-urlencoded");
                post.setHeader("Content-Type", "application/xml");
                data = session.getBytes("UTF-8");
                ByteArrayEntity formEntity = new ByteArrayEntity(data);
                post.setEntity(formEntity);
                method = post;
                
            } else if (METHOD_GET.equals(httpMethod)) {
                
                if (params != null) {
                    url = url + getUrlParams + ".json?";
                }
                StoryLogger.e(TAG, TAG + ":" + url);
                method = new HttpGet(url);
                method.setHeader("Accept-Encoding", "gzip");
            }
            HttpClient client = HttpManager.sClient;
            
            Utility.sign(ctx, method);
            
            HttpResponse httpResponse = client.execute(method);
            StatusLine status = httpResponse.getStatusLine();
            int statusCode = status.getStatusCode();
            StoryLogger.e(TAG, TAG + ":" + statusCode);
            if (statusCode != HttpStatus.SC_OK) {
                throw new StatusCodeException(url, statusCode);
            } else {
                HttpEntity entity = httpResponse.getEntity();
                
                InputStream is = null;
                
                // 判断是否支持GZIP
                
                Header[] allHeaders = httpResponse.getAllHeaders();
                for (int i = 0; i < allHeaders.length; i++) {
                    
                    if (allHeaders[i].getValue().equalsIgnoreCase("gzip")) {
                        StoryLogger.e(TAG, TAG + ":gzip");
                        StoryLogger.e(TAG, TAG + ":" + entity.getContentLength());
                        GzipDecompressingEntity decompressingEntity = new GzipDecompressingEntity(
                                entity);
                        is = decompressingEntity.getContent();
                    }
                }
                
                if (is == null) {
                    is = entity.getContent();
                }
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int count = 0;
                while ((count = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, count);
                }
                rlt = new String(baos.toByteArray());
                StoryLogger.e("content--->", "content-->" + url + ":" + rlt);
            }
        } catch (ConnectTimeoutException e) {
            throw new StoryFlowException("request data faild", e);
        } catch (SocketTimeoutException e) {
            // 上报日志
            throw new StoryFlowException("request data faild", e);
        } catch (StatusCodeException e) {
            throw new StatusCodeException(e.getUrl(), e.getStatusCode());
        } catch (Exception e) {
            throw new StoryFlowException("request data faild", e);
        }
        
        return rlt;
    }
    /**
     * 针对开放平台做处理
     * @param httpMethod
     * @param params
     * @param url
     * @param ctx
     * @return
     * @throws StatusCodeException
     * @throws StoryFlowException
     */
    private static String requestOpenData(String httpMethod,
            ArrayList<NameValuePair> params, String url, Context ctx, String file)
                    throws StatusCodeException, StoryFlowException {
        String rlt = "";
        HttpUriRequest method = null;
        
        String getUrlParams = "";
        if (params != null) {
            
            StringBuffer sb = new StringBuffer();
            for (NameValuePair nameValuePair : params) {
                sb.append(nameValuePair.getName()+"=");
                sb.append(nameValuePair.getValue()+"&");
            }
            String content = sb.toString();
            if (!TextUtils.isEmpty(content)) {
                content = content.substring(0, content.length() - 1);
            }
            getUrlParams = content;
            
            if(!url.contains("?")) {
                getUrlParams = "?" + getUrlParams;
            }else {
                getUrlParams = "&" + getUrlParams;
            }
        }
        
        try {
            if (METHOD_POST.equals(httpMethod)) {
                HttpPost post = new HttpPost(url);
                if (params != null) {
                    
                    if (!TextUtils.isEmpty(file)) {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024 * 50);
                        Utility.paramToUpload(bos, params);
                        post.setHeader("Content-Type", "multipart/form-data" + "; boundary=" + "7cd4a6d158c");
                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inSampleSize = 4;
                        Bitmap bf = BitmapFactory.decodeFile(file, opts);
                        com.youqude.storyflow.weibo.Utility.imageContentToUpload(bos, bf);
                        byte[] data = bos.toByteArray();
                        bos.close();
                        ByteArrayEntity formEntity = new ByteArrayEntity(data);
                        post.setEntity(formEntity);
                    } else {
                        post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                        post.setHeader("Accept-Encoding", "gzip");
                    }
                }
                
                method = post;
            } else if (METHOD_GET.equals(httpMethod)) {
                
                if (params != null) {
                    url = url + getUrlParams ;
                }
                StoryLogger.e(TAG, TAG + ":" + url);
                method = new HttpGet(url);
                method.setHeader("Accept-Encoding", "gzip");
            }
            HttpClient client = HttpManager.sClient;
            
            Utility.sign(ctx, method);
            
            HttpResponse httpResponse = client.execute(method);
            StatusLine status = httpResponse.getStatusLine();
            int statusCode = status.getStatusCode();
            StoryLogger.e(TAG, TAG+":statusCode--->"+statusCode);
            if (statusCode != HttpStatus.SC_OK) {
                
                HttpEntity entity = httpResponse.getEntity();
                InputStream inputStream = entity.getContent();
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int count = 0;
                while ((count = inputStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, count);
                }
                rlt = new String(baos.toByteArray());
                StoryLogger.e("content--->", "content-->" + url + ":" + rlt);
                
                throw new StatusCodeException(url, statusCode);
            } else {
                HttpEntity entity = httpResponse.getEntity();
                
                InputStream is = null;
                
                // 判断是否支持GZIP
                
                Header[] allHeaders = httpResponse.getAllHeaders();
                for (int i = 0; i < allHeaders.length; i++) {
                    
                    if (allHeaders[i].getValue().equalsIgnoreCase("gzip")) {
                        StoryLogger.e(TAG, TAG + ":gzip");
                        GzipDecompressingEntity decompressingEntity = new GzipDecompressingEntity(
                                entity);
                        is = decompressingEntity.getContent();
                    }
                }
                
                if (is == null) {
                    is = entity.getContent();
                }
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int count = 0;
                while ((count = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, count);
                }
                rlt = new String(baos.toByteArray());
                StoryLogger.e("content--->", "content-->" + url + ":" + rlt);
            }
        } catch (ConnectTimeoutException e) {
            throw new StoryFlowException("request data faild", e);
        } catch (SocketTimeoutException e) {
            // 上报日志
            throw new StoryFlowException("request data faild", e);
        } catch (StatusCodeException e) {
            throw new StatusCodeException(e.getUrl(), e.getStatusCode());
        } catch (Exception e) {
            throw new StoryFlowException("request data faild", e);
        }
        
        return rlt;
    }

    
    public static void shutdownConnection(){
        try { 
            HttpManager.sClient.getConnectionManager().shutdown(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
}

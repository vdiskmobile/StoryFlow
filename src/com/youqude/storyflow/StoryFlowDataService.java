
package com.youqude.storyflow;

import com.tencent.weibo.api.TAPI;
import com.tencent.weibo.api.UserAPI;
import com.tencent.weibo.constants.OAuthConstants;
import com.tencent.weibo.oauthv2.OAuthV2;
import com.youqude.storyflow.domain.AlbumInfo;
import com.youqude.storyflow.domain.BaiduUserInfo;
import com.youqude.storyflow.domain.Event;
import com.youqude.storyflow.domain.LikeInfo;
import com.youqude.storyflow.domain.PicIdInfo;
import com.youqude.storyflow.domain.QUserInfo;
import com.youqude.storyflow.domain.SinaUserInfo;
import com.youqude.storyflow.domain.StoryInfo;
import com.youqude.storyflow.domain.StoryUserProfileInfo;
import com.youqude.storyflow.domain.StoryUserTabCountInfo;
import com.youqude.storyflow.domain.UserInfo;
import com.youqude.storyflow.exception.StatusCodeException;
import com.youqude.storyflow.exception.StoryFlowException;
import com.youqude.storyflow.ui.NewStoryFlowActivity;
import com.youqude.storyflow.utils.Constants;
import com.youqude.storyflow.utils.StoryLogger;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class StoryFlowDataService extends Service {

    private static final String TAG = StoryFlowDataService.class.getSimpleName();
    private Context mContext = this;

    public final IBinder iBinder = new StoryFlowFetchDataBinder();

    public class StoryFlowFetchDataBinder extends Binder {
        public StoryFlowDataService getService() {
            return StoryFlowDataService.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return iBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        StoryLogger.e(TAG, TAG + "serviceonCreate");
        super.onCreate();
    }

    /**
     * qq login
     * 
     * @param handler
     * @param token
     * @param openid
     * @param openkey
     * @return
     */
    public boolean qqLogin(StoryFlowEventHandler handler, String token,
            String openid, String openkey) {
        new QLoginTask().execute(new Object[] {
                handler, token, openid,
                openkey
        });
        return true;
    }

    /**
     * qq login task
     */
    private class QLoginTask extends AsyncTask<Object[], Void, Event> {

        StoryFlowEventHandler handler;

        String token = "";
        String openid = "";
        String openkey = "";

        Event ev = new Event();

        @Override
        protected Event doInBackground(Object[]... params) {
            handler = (StoryFlowEventHandler) params[0][0];
            token = (String) params[0][1];
            openid = (String) params[0][2];
            openkey = (String) params[0][3];

            JSONObject jobj = null;
            try {
                StoryLogger.e(TAG, TAG+":openid-->"+openid);
                StoryLogger.e(TAG, TAG+":token-->"+token);
                StoryLogger.e(TAG, TAG+":openkey-->"+openkey);
                jobj = StoryAPI.getQLoginInfo(mContext, token, openid, openkey);
                if (null != jobj) {
                    UserInfo userInfo = UserInfo.create(jobj);
                    if (null != userInfo) {
                        ev.eventId = Constants.EVENT_CODE_SUCCESS;
                        ev.rlt = new Object[] {
                                userInfo
                        };
                        return ev;
                    }
                }
                ev.eventId = Constants.EVENT_CODE_FAILED;

            } catch (StatusCodeException e) {
                e.printStackTrace();
                ev.eventId = Constants.EVENT_CODE_NET_INTERRUPT;
            } catch (StoryFlowException e) {
                e.printStackTrace();
                ev.eventId = Constants.EVENT_CODE_NET_INTERRUPT;
            } catch (JSONException e) {
                e.printStackTrace();
                ev.eventId = Constants.EVENT_CODE_FAILED;
            }
            return ev;
        }

        @Override
        protected void onPostExecute(Event event) {
            handler.handleSeviceResult(event.msg, event.eventId, event.rlt);
        }
    }

    /**
     * 获取百度用户信息
     * 
     * @param handler
     * @param access_token
     * @return
     */
    public boolean getBaiduLoggedInUser(StoryFlowEventHandler handler, String access_token
            ) {
        new BaiduUserInfoTask().execute(new Object[] {
                handler, access_token
        });
        return true;
    }

    private class BaiduUserInfoTask extends AsyncTask<Object[], Void, Event> {

        StoryFlowEventHandler handler;

        String access_token = "";

        Event ev = new Event();

        @Override
        protected Event doInBackground(Object[]... params) {
            handler = (StoryFlowEventHandler) params[0][0];
            access_token = (String) params[0][1];

            JSONObject jobj = null;
            try {
                jobj = StoryAPI.getBaiduUserInfo(mContext, access_token);
                if (null != jobj) {
                    BaiduUserInfo baiduUserInfo = BaiduUserInfo.create(jobj);
                    if (null != baiduUserInfo) {
                        ev.eventId = Constants.BAIDU_UID_EVENT_CODE_SUCCESS;
                        ev.rlt = new Object[] {
                                baiduUserInfo
                        };
                        return ev;
                    }
                }
                ev.eventId = Constants.EVENT_CODE_FAILED;

            } catch (StatusCodeException e) {
                e.printStackTrace();
                ev.eventId = Constants.EVENT_CODE_NET_INTERRUPT;
            } catch (StoryFlowException e) {
                e.printStackTrace();
                ev.eventId = Constants.EVENT_CODE_NET_INTERRUPT;
            }
            return ev;
        }

        @Override
        protected void onPostExecute(Event event) {
            handler.handleSeviceResult(event.msg, event.eventId, event.rlt);
        }
    }

    /**8
     * 
     * @param handler
     * @param access_token
     * @return
     */
    public boolean getSinaUid(StoryFlowEventHandler handler, String access_token
            ) {
        new SinaUidTask().execute(new Object[] {
                handler, access_token
        });
        return true;
    }

    private class SinaUidTask extends AsyncTask<Object[], Void, Event> {

        StoryFlowEventHandler handler;

        String access_token = "";

        Event ev = new Event();

        @Override
        protected Event doInBackground(Object[]... params) {
            handler = (StoryFlowEventHandler) params[0][0];
            access_token = (String) params[0][1];

            JSONObject jobj = null;
            try {
                jobj = StoryAPI.getSinaUidByToken(mContext, access_token);
                if (null != jobj) {
                    String rlt = jobj.optString("uid", null);
                    if (null != rlt) {
                        ev.eventId = Constants.SINA_UID_EVENT_CODE_SUCCESS;
                        ev.rlt = new Object[] {
                                rlt
                        };
                        return ev;
                    }
                }
                ev.eventId = Constants.EVENT_CODE_FAILED;

            } catch (StatusCodeException e) {
                e.printStackTrace();
                ev.eventId = Constants.EVENT_CODE_NET_INTERRUPT;
            } catch (StoryFlowException e) {
                e.printStackTrace();
                ev.eventId = Constants.EVENT_CODE_NET_INTERRUPT;
            }
            return ev;
        }

        @Override
        protected void onPostExecute(Event event) {
            handler.handleSeviceResult(event.msg, event.eventId, event.rlt);
        }
    }

    /**
     * @param handler
     * @param uid
     * @param token
     * @param sessionKey
     * @param sessionSecret
     * @return
     */
    public boolean baiduLogin(StoryFlowEventHandler handler, String uid, String token,
            String sessionKey, String sessionSecret) {
        new BaiduLoginTask().execute(new Object[] {
                handler, uid, token, sessionKey,
                sessionSecret
        });
        return true;
    }

    /**
     * baidu login task
     */
    private class BaiduLoginTask extends AsyncTask<Object[], Void, Event> {

        StoryFlowEventHandler handler;

        String uid = "";
        String token = "";
        String sessionKey = "";
        String sessionSecret = "";

        Event ev = new Event();

        @Override
        protected Event doInBackground(Object[]... params) {
            handler = (StoryFlowEventHandler) params[0][0];
            uid = (String) params[0][1];
            token = (String) params[0][2];
            sessionKey = (String) params[0][3];
            sessionSecret = (String) params[0][4];

            JSONObject jobj = null;
            try {
                jobj = StoryAPI.getBaiduLoginInfo(mContext, uid, token, sessionKey, sessionSecret);
                if (null != jobj) {
                    UserInfo userInfo = UserInfo.create(jobj);
                    if (null != userInfo) {
                        ev.eventId = Constants.EVENT_CODE_SUCCESS;
                        ev.rlt = new Object[] {
                                userInfo
                        };
                        return ev;
                    }
                }
                ev.eventId = Constants.EVENT_CODE_FAILED;

            } catch (StatusCodeException e) {
                e.printStackTrace();
                ev.eventId = Constants.EVENT_CODE_NET_INTERRUPT;
            } catch (StoryFlowException e) {
                e.printStackTrace();
                ev.eventId = Constants.EVENT_CODE_NET_INTERRUPT;
            } catch (JSONException e) {
                e.printStackTrace();
                ev.eventId = Constants.EVENT_CODE_FAILED;
            }
            return ev;
        }

        @Override
        protected void onPostExecute(Event event) {
            handler.handleSeviceResult(event.msg, event.eventId, event.rlt);
        }
    }

    /**
     * @param handler
     * @param uid
     * @param token
     * @return
     */
    public boolean sinaLogin(StoryFlowEventHandler handler, String uid, String token) {
        new SinaLoginTask().execute(new Object[] {
                handler, uid, token
        });
        return true;
    }

    /**
     * sina login task
     */
    private class SinaLoginTask extends AsyncTask<Object[], Void, Event> {

        StoryFlowEventHandler handler;

        String uid = "";
        String token = "";

        Event ev = new Event();

        @Override
        protected Event doInBackground(Object[]... params) {
            handler = (StoryFlowEventHandler) params[0][0];
            uid = (String) params[0][1];
            token = (String) params[0][2];

            JSONObject jobj = null;
            try {
                jobj = StoryAPI.getSinaLoginInfo(mContext, uid, token);
                if (null != jobj) {
                    UserInfo userInfo = UserInfo.create(jobj);
                    if (null != userInfo) {
                        ev.eventId = Constants.EVENT_CODE_SUCCESS;
                        ev.rlt = new Object[] {
                                userInfo
                        };
                        return ev;
                    }
                }
                ev.eventId = Constants.EVENT_CODE_FAILED;

            } catch (StatusCodeException e) {
                e.printStackTrace();
                ev.eventId = Constants.EVENT_CODE_NET_INTERRUPT;
            } catch (StoryFlowException e) {
                e.printStackTrace();
                ev.eventId = Constants.EVENT_CODE_NET_INTERRUPT;
            } catch (JSONException e) {
                e.printStackTrace();
                ev.eventId = Constants.EVENT_CODE_FAILED;
            }
            return ev;
        }

        @Override
        protected void onPostExecute(Event event) {
            handler.handleSeviceResult(event.msg, event.eventId, event.rlt);
        }
    }

    /**
     * 
     * @param handler
     * @param object
     * @param sessionId
     * @param appType
     * @return
     */
    public boolean saveUserProfile(StoryFlowEventHandler handler, Object object, String sessionId, String appType) {
        new SaveSessionTask().execute(new Object[] {
                handler, object, sessionId, appType
        });
        return true;
    }

    private class SaveSessionTask extends AsyncTask<Object[], Void, Event> {

        StoryFlowEventHandler handler;

        Object obj = null;

        String sessionId = "";
        String appType = "";
        
        Event ev = new Event();

        @Override
        protected Event doInBackground(Object[]... params) {
            handler = (StoryFlowEventHandler) params[0][0];
            obj = params[0][1];
            sessionId = (String) params[0][2];
            appType = (String) params[0][3];
            
            JSONObject jobj = null;
            try {
                jobj = StoryAPI.addBaseProfile(mContext, obj, appType, sessionId);
              
                ev.eventId = Constants.EVENT_CODE_FAILED;

            } catch (StatusCodeException e) {
                e.printStackTrace();
                ev.eventId = Constants.EVENT_CODE_NET_INTERRUPT;
            } catch (StoryFlowException e) {
                e.printStackTrace();
                ev.eventId = Constants.EVENT_CODE_NET_INTERRUPT;
            } 
           /* catch (JSONException e) {
                e.printStackTrace();
                ev.eventId = Constants.EVENT_CODE_FAILED;
            }*/
            return ev;
        }

        @Override
        protected void onPostExecute(Event event) {
            handler.handleSeviceResult(event.msg, event.eventId, event.rlt);
        }
    }
    
    /**
     * 
     * @param handler
     * @param qOAuth
     * @param userInfo
     * @return
     */
    public boolean addQQBaseUserProfile(StoryFlowEventHandler handler, OAuthV2 qOAuth, UserInfo userInfo) {
        new QQBaseUserProfileCreateTask().execute(new Object[] {
                handler, qOAuth, userInfo
        });
        return true;
    }
    
    private class QQBaseUserProfileCreateTask extends AsyncTask<Object[], Void, Event> {
        
        StoryFlowEventHandler handler;
        
        OAuthV2 qOAuth;
        UserInfo userInfo;      
        
        Event ev = new Event();
        
        @Override
        protected Event doInBackground(Object[]... params) {
            handler = (StoryFlowEventHandler) params[0][0];
            qOAuth = (OAuthV2) params[0][1];
            userInfo =  (UserInfo) params[0][2];
            
            JSONObject jobj = null;
            UserAPI userAPI = null;
            try {
               
                // 获取qq用户的其他信息
                userAPI = new UserAPI(OAuthConstants.OAUTH_VERSION_2_A);
                String response = userAPI.info(qOAuth, "json");
                StoryLogger.e(TAG, TAG + "----------->"+response);
                QUserInfo qUserInfo = QUserInfo.create(response);
                if (null != qUserInfo) {
                    jobj = StoryAPI.addBaseProfile(mContext, qUserInfo, userInfo.appType,
                            userInfo.sessionId);
                    // 判断是否添加成功

                    if (null != jobj) {
                        ev.eventId = Constants.QQ_BASE_PROFILE_SUCCESS;
                        return ev;
                    }

                }
                ev.eventId = Constants.EVENT_CODE_FAILED;
                
            } catch (StatusCodeException e) {
                e.printStackTrace();
                ev.eventId = Constants.EVENT_CODE_NET_INTERRUPT;
            } catch (StoryFlowException e) {
                e.printStackTrace();
                ev.eventId = Constants.EVENT_CODE_NET_INTERRUPT;
            } catch (JSONException e) {
                e.printStackTrace();
                ev.eventId = Constants.EVENT_CODE_FAILED;
            } catch (Exception e) {
                e.printStackTrace();
                ev.eventId = Constants.EVENT_CODE_NET_INTERRUPT;
            }
            
            if (null != userAPI) {
                userAPI.shutdownConnection();
            }
            return ev;
        }
        
        @Override
        protected void onPostExecute(Event event) {
            handler.handleSeviceResult(event.msg, event.eventId, event.rlt);
        }
    }
    
    /**
     * 
     * @param handler
     * @param sina_uid
     * @param userInfo
     * @return
     */
    public boolean addSinaBaseUserProfile(StoryFlowEventHandler handler, String sina_uid, UserInfo userInfo) {
        new SinaBaseUserProfileCreateTask().execute(new Object[] {
                handler, sina_uid, userInfo
        });
        return true;
    }
    
    private class SinaBaseUserProfileCreateTask extends AsyncTask<Object[], Void, Event> {
        
        StoryFlowEventHandler handler;
        
        String sina_uid;
        UserInfo userInfo;      
        
        Event ev = new Event();
        
        @Override
        protected Event doInBackground(Object[]... params) {
            handler = (StoryFlowEventHandler) params[0][0];
            sina_uid = (String) params[0][1];
            userInfo =  (UserInfo) params[0][2];
            
            JSONObject jobj = null;
            try {
                
                // 获取sina用户的其他信息
                
                    jobj = StoryAPI.getSinaUserShow(mContext, sina_uid, userInfo);
                
                    if (null != jobj) {
                        SinaUserInfo sinaUserInfo = SinaUserInfo.create(jobj);
                    if (null != sinaUserInfo) {
                        jobj = StoryAPI.addBaseProfile(mContext, sinaUserInfo, userInfo.appType,
                                userInfo.sessionId);
                        // 判断是否添加成功
                        if (null != jobj) {
                            ev.eventId = Constants.SINA_BASE_PROFILE_SUCCESS;
                            return ev;
                        }
                    }
                    }
                    
                ev.eventId = Constants.EVENT_CODE_FAILED;
                
            } catch (StatusCodeException e) {
                e.printStackTrace();
                ev.eventId = Constants.EVENT_CODE_NET_INTERRUPT;
            } catch (StoryFlowException e) {
                e.printStackTrace();
                ev.eventId = Constants.EVENT_CODE_NET_INTERRUPT;
            } 
            return ev;
        }
        
        @Override
        protected void onPostExecute(Event event) {
            handler.handleSeviceResult(event.msg, event.eventId, event.rlt);
        }
    }
    /**
     * 
     * @param handler
     * @param baiduUserInfo
     * @param userInfo
     * @return
     */
    public boolean addBaiduBaseUserProfile(StoryFlowEventHandler handler, BaiduUserInfo baiduUserInfo, UserInfo userInfo) {
        new BaiduBaseUserProfileCreateTask().execute(new Object[] {
                handler, baiduUserInfo, userInfo
        });
        return true;
    }
    
    private class BaiduBaseUserProfileCreateTask extends AsyncTask<Object[], Void, Event> {
        
        StoryFlowEventHandler handler;
        
        BaiduUserInfo baiduUserInfo;
        UserInfo userInfo;      
        
        Event ev = new Event();
        
        @Override
        protected Event doInBackground(Object[]... params) {
            handler = (StoryFlowEventHandler) params[0][0];
            baiduUserInfo =(BaiduUserInfo) params[0][1];
            userInfo =  (UserInfo) params[0][2];
            
            JSONObject jobj = null;
            try {
                
                jobj = StoryAPI.addBaseProfile(mContext, baiduUserInfo, userInfo.appType,
                        userInfo.sessionId);
                // 判断是否添加成功
                if (null != jobj) {
                    ev.eventId = Constants.BAIDU_BASE_PROFILE_SUCCESS;
                    return ev;
                }
                
                ev.eventId = Constants.EVENT_CODE_FAILED;
                
            } catch (StatusCodeException e) {
                e.printStackTrace();
                ev.eventId = Constants.EVENT_CODE_NET_INTERRUPT;
            } catch (StoryFlowException e) {
                e.printStackTrace();
                ev.eventId = Constants.EVENT_CODE_NET_INTERRUPT;
            } 
            return ev;
        }
        
        @Override
        protected void onPostExecute(Event event) {
            handler.handleSeviceResult(event.msg, event.eventId, event.rlt);
        }
    }
    /**
     * 发布一个新的故事流
     * @param handler
     * @param title
     * @param description
     * @param picPath
     * @return
     */
    public boolean sendStoryFlow(StoryFlowEventHandler handler, String title, String description ,
                                 String picPath) {
        new StoryFlowCreateTask().execute(new Object[] {
                handler, title, description, picPath
        });
        return true;
    }
    
    private class StoryFlowCreateTask extends AsyncTask<Object[], Void, Event> {
        
        StoryFlowEventHandler handler;
        
        String title;
        String description;
        String picPath;
        
        
        Event ev = new Event();
        
        @Override
        protected Event doInBackground(Object[]... params) {
            handler = (StoryFlowEventHandler) params[0][0];
            title = (String) params[0][1];
            description =  (String) params[0][2];
            picPath =  (String) params[0][3];
            
            JSONObject jobj = null;
            try {
                
                jobj = StoryAPI.sendStoryFlowAction(mContext, title, description,
                        picPath);
                // 判断是否发布成功
                if (null != jobj) {
                    ev.eventId = Constants.STORY_RELEASE_SUCCESS;
                    return ev;
                }
                
                ev.eventId = Constants.STORY_RELEASE_FAILED;
                
            } catch (StatusCodeException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_RELEASE_NET_INTERRUPT;
            } catch (StoryFlowException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_RELEASE_NET_INTERRUPT;
            } 
            return ev;
        }
        
        @Override
        protected void onPostExecute(Event event) {
            handler.handleSeviceResult(event.msg, event.eventId, event.rlt);
        }
    }
    
    /**
     * 发布一张图片到某个故事流
     * @param handler
     * @param description
     * @param picPath
     * @return
     */
    
    public boolean sendStoryFlowWithStoryId(StoryFlowEventHandler handler, String storyId, String description ,
            String picPath) {
        new StoryFlowCreateWithStoryIdTask().execute(new Object[] {
                handler, storyId, description, picPath
        });
        return true;
    }
    
    private class StoryFlowCreateWithStoryIdTask extends AsyncTask<Object[], Void, Event> {
        
        StoryFlowEventHandler handler;
        String storyId;
        String description;
        String picPath;
        
        
        Event ev = new Event();
        
        @Override
        protected Event doInBackground(Object[]... params) {
            handler = (StoryFlowEventHandler) params[0][0];
            storyId =  (String) params[0][1];
            description =  (String) params[0][2];
            picPath =  (String) params[0][3];
            
            JSONObject jobj = null;
            try {
                
                jobj = StoryAPI.sendStoryFlowWithStoryIdAction(mContext, storyId, description,
                        picPath);
                // 判断是否发布成功
                if (null != jobj) {
                    ev.eventId = Constants.STORY_RELEASE_SUCCESS;
                    return ev;
                }
                
                ev.eventId = Constants.STORY_RELEASE_FAILED;
                
            } catch (StatusCodeException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_RELEASE_NET_INTERRUPT;
            } catch (StoryFlowException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_RELEASE_NET_INTERRUPT;
            } 
            return ev;
        }
        
        @Override
        protected void onPostExecute(Event event) {
            handler.handleSeviceResult(event.msg, event.eventId, event.rlt);
        }
    }
    /**
     * 根据关键字查询故事流标题列表
     * @param handler
     * @param keywords
     * @return
     */
    public boolean searchStoryTitleListByKeywords(StoryFlowEventHandler handler, String keywords) {
        new StoryTitleSearchListByKeywordsTask().execute(new Object[] {
                handler, keywords
        });
        return true;
    }
    
    private class StoryTitleSearchListByKeywordsTask extends AsyncTask<Object[], Void, Event> {
        
        StoryFlowEventHandler handler;
        
        String keywords;
        
        Event ev = new Event();
        
        @Override
        protected Event doInBackground(Object[]... params) {
            handler = (StoryFlowEventHandler) params[0][0];
            keywords = (String) params[0][1];
            
            JSONObject jobj = null;
            
            ArrayList<StoryInfo> data;
            
            try {
                
                jobj = StoryAPI.searchStoryTitleByKeywords(mContext, keywords);
                // 判断故事主题列表
                if (null != jobj) {
                    data = StoryInfo.create(jobj);
                    if (data !=null) {
                        ev.eventId = Constants.STORY_TITLE_LIST_SUCCESS;
                        ev.rlt = new Object[]{
                          data      
                        };
                        return ev;
                    } 
                }
                
                ev.eventId = Constants.STORY_TITLE_LIST_FAILED;
                
            } catch (StatusCodeException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_TITLE_LIST_NET_INTERRUPT;
            } catch (StoryFlowException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_TITLE_LIST_NET_INTERRUPT;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return ev;
        }
        
        @Override
        protected void onPostExecute(Event event) {
            handler.handleSeviceResult(event.msg, event.eventId, event.rlt);
        }
    }
    
    /**
     * 根据storyId获取图片列表
     * @param handler
     * @param storyId
     * @param pageNum
     * @return
     */
    
    public boolean getStoryPicById(StoryFlowEventHandler handler, String storyId, int pageNum, int pageSize) {
        new StoryPicDataByIdTask().execute(new Object[] {
                handler, storyId, pageNum,pageSize
        });
        return true;
    }
    
    private class StoryPicDataByIdTask extends AsyncTask<Object[], Void, Event> {
        
        StoryFlowEventHandler handler;
        
        String storyId;
        int pageNum;
        int pageSize;
        
        Event ev = new Event();
        
        @Override
        protected Event doInBackground(Object[]... params) {
            handler = (StoryFlowEventHandler) params[0][0];
            storyId = (String) params[0][1];
            pageNum = (Integer) params[0][2];
            pageSize = (Integer) params[0][3];
            
            JSONObject jobj = null;
            
            ArrayList<AlbumInfo> data;
            
            try {
                
                jobj = StoryAPI.searchStoryPicById(mContext, storyId, pageNum, pageSize);
                if (null != jobj) {
                    
                    JSONObject jDataObj = jobj.optJSONObject("resp");
                    if (jDataObj!=null) {
                        int recordSize = jDataObj.getInt("recordSize");
                        
                        data = AlbumInfo.create(jobj);
                        if (data !=null) {
                            ev.eventId = Constants.STORY_PIC_LIST_SUCCESS;
                            ev.rlt = new Object[]{
                              data , recordSize     
                            };
                            return ev;
                        } 
                    }
                }
                
                ev.eventId = Constants.STORY_PIC_LIST_FAILED;
                
            } catch (StatusCodeException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_PIC_LIST_NET_INTERRUPT;
            } catch (StoryFlowException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_PIC_LIST_NET_INTERRUPT;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return ev;
        }
        
        @Override
        protected void onPostExecute(Event event) {
            handler.handleSeviceResult(event.msg, event.eventId, event.rlt);
        }
    }
    
    /**
     * 用于初始化故事流列表图片时调用
     * @param handler
     * @param mArrayList
     * @return
     */
    public boolean loadStoryItemInfo(StoryFlowEventHandler handler, ArrayList<StoryInfo> mArrayList, int pageNum, int pageSize) {
        new StoryFlowItemLoadTask().execute(new Object[] {
                handler, mArrayList, pageNum, pageSize
        });
        return true;
    }
    
    private class StoryFlowItemLoadTask extends AsyncTask<Object[], Void, Event> {
        
        StoryFlowEventHandler handler;
        
        ArrayList<StoryInfo> mArrayList;
        
        int pageNum;
        int pageSize;
        
        
        Event ev = new Event();
        
        @Override
        protected Event doInBackground(Object[]... params) {
            handler = (StoryFlowEventHandler) params[0][0];
            mArrayList =  (ArrayList<StoryInfo>) params[0][1];
            pageNum = (Integer) params[0][2];
            pageSize = (Integer) params[0][3];
            
            
            JSONObject jobj = null;
            ArrayList<AlbumInfo> data = null;
            
            HashMap<String, ArrayList<AlbumInfo>> mHashMap = new HashMap<String, ArrayList<AlbumInfo>>();
            
            try {
                for (int i = 0; i < mArrayList.size(); i++) {

                    jobj = StoryAPI.searchStoryPicById(mContext, mArrayList.get(i).storyId,
                            pageNum, pageSize);
                    if (null != jobj) {
                        data = AlbumInfo.create(jobj);
                        if (data != null) {
                            try {
                                mHashMap.put(mArrayList.get(i).storyId, data);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            continue;
                        }
                    }
                }
                
                ev.eventId = Constants.STORY_PIC_LIST_SUCCESS;
                ev.rlt = new Object[]{
                        mHashMap      
                };
                return ev;
            } catch (StatusCodeException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_PIC_LIST_NET_INTERRUPT;
                return ev;
            } catch (StoryFlowException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_PIC_LIST_NET_INTERRUPT;
                return ev;
            } catch (JSONException e) {
                e.printStackTrace();
            }
          
            ev.eventId = Constants.STORY_PIC_LIST_FAILED;
            
            return ev;
        }
        
        @Override
        protected void onPostExecute(Event event) {
            handler.handleSeviceResult(event.msg, event.eventId, event.rlt);
        }
    }
    
    /**
     * 根据storyId获取story detail
     * @param handler
     * @param storyId
     * @return
     */
    public boolean getStoryTitleByStoryId(StoryFlowEventHandler handler, String storyId) {
        new StoryDetailDataByIdTask().execute(new Object[] {
                handler, storyId
        });
        return true;
    }
    
    private class StoryDetailDataByIdTask extends AsyncTask<Object[], Void, Event> {
        
        StoryFlowEventHandler handler;
        
        String storyId;
        
        Event ev = new Event();
        
        @Override
        protected Event doInBackground(Object[]... params) {
            handler = (StoryFlowEventHandler) params[0][0];
            storyId = (String) params[0][1];
            
            JSONObject jobj = null;
            
            StoryInfo info;
            
            try {
                
                jobj = StoryAPI.getStoryDetailById(mContext, storyId);
                if (null != jobj) {
                    info = StoryInfo.createStoryInfo(jobj);
                    if (info !=null) {
                        ev.eventId = Constants.STORY_DETAIL_INFO_SUCCESS;
                        ev.rlt = new Object[]{
                                info      
                        };
                        return ev;
                    } 
                }
                
                ev.eventId = Constants.STORY_DETAIL_INFO_FAILED;
                
            } catch (StatusCodeException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_DETAIL_INFO_NET_INTERRUPT;
            } catch (StoryFlowException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_DETAIL_INFO_NET_INTERRUPT;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return ev;
        }
        
        @Override
        protected void onPostExecute(Event event) {
            handler.handleSeviceResult(event.msg, event.eventId, event.rlt);
        }
    }
    
    /**
     * 获取今日最热的故事
     * @param handler
     * @param pageNum
     * @param pageSize
     * @return
     */
    
    public boolean getTodayHotStory(StoryFlowEventHandler handler, int pageNum, int pageSize) {
        new TodayHotStoryFetchTask().execute(new Object[] {
                handler, pageNum, pageSize
        });
        return true;
    }
    
    private class TodayHotStoryFetchTask extends AsyncTask<Object[], Void, Event> {
        
        StoryFlowEventHandler handler;
        
        int pageNum;
        int pageSize;
        
        Event ev = new Event();
        
        @Override
        protected Event doInBackground(Object[]... params) {
            handler = (StoryFlowEventHandler) params[0][0];
            pageNum = (Integer) params[0][1];
            pageSize = (Integer) params[0][2];
            
            JSONObject jobj = null;
            
            ArrayList<StoryInfo> data;
            try {
                
                jobj = StoryAPI.getTodayHotStoryList(mContext, pageNum, pageSize);
             // 判断故事主题列表
                if (null != jobj) {
                    
                    JSONObject jDataObj = jobj.optJSONObject("resp");
                    if (jDataObj!=null) {
                        int recordSize = jDataObj.getInt("recordSize");
                        data = StoryInfo.create(jobj);
                        if (data !=null) {
                            ev.eventId = Constants.STORY_INFO_LIST_SUCCESS;
                            ev.rlt = new Object[]{
                              data , recordSize     
                            };
                            return ev;
                        } 
                    }
                    
                }
                
                ev.eventId = Constants.STORY_INFO_LIST_FAILED;
                
            } catch (StatusCodeException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_INFO_LIST_NET_INTERRUPT;
            } catch (StoryFlowException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_INFO_LIST_NET_INTERRUPT;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return ev;
        }
        
        @Override
        protected void onPostExecute(Event event) {
            handler.handleSeviceResult(event.msg, event.eventId, event.rlt);
        }
    }
    
    
    /**
     * 获取本周最热故事
     * @param handler
     * @param pageNum
     * @param pageSize
     * @return
     */
    public boolean getWeekHotStory(StoryFlowEventHandler handler, int pageNum, int pageSize) {
        new WeekHotStoryFetchTask().execute(new Object[] {
                handler, pageNum, pageSize
        });
        return true;
    }
    
    private class WeekHotStoryFetchTask extends AsyncTask<Object[], Void, Event> {
        
        StoryFlowEventHandler handler;
        
        int pageNum;
        int pageSize;
        
        Event ev = new Event();
        
        @Override
        protected Event doInBackground(Object[]... params) {
            handler = (StoryFlowEventHandler) params[0][0];
            pageNum = (Integer) params[0][1];
            pageSize = (Integer) params[0][2];
            
            JSONObject jobj = null;
            ArrayList<StoryInfo> data;
            try {
                
                jobj = StoryAPI.getWeekHotStoryList(mContext, pageNum, pageSize);
                // 判断故事主题列表
                if (null != jobj) {
                    JSONObject jDataObj = jobj.optJSONObject("resp");
                    if (jDataObj!=null) {
                        int recordSize = jDataObj.getInt("recordSize");
                        data = StoryInfo.create(jobj);
                        if (data !=null) {
                            ev.eventId = Constants.STORY_INFO_LIST_SUCCESS;
                            ev.rlt = new Object[]{
                              data , recordSize    
                            };
                            return ev;
                        } 
                    }
                }
                
                ev.eventId = Constants.STORY_INFO_LIST_FAILED;
                
            } catch (StatusCodeException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_INFO_LIST_NET_INTERRUPT;
            } catch (StoryFlowException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_INFO_LIST_NET_INTERRUPT;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return ev;
        }
        
        @Override
        protected void onPostExecute(Event event) {
            handler.handleSeviceResult(event.msg, event.eventId, event.rlt);
        }
    }
    /**
     * 获取最热的故事
     * @param handler
     * @param pageNum
     * @param pageSize
     * @return
     */
    public boolean getLatestHotStory(StoryFlowEventHandler handler, int pageNum, int pageSize) {
        new LatestHotStoryFetchTask().execute(new Object[] {
                handler, pageNum, pageSize
        });
        return true;
    }
    
    private class LatestHotStoryFetchTask extends AsyncTask<Object[], Void, Event> {
        
        StoryFlowEventHandler handler;
        
        int pageNum;
        int pageSize;
        
        Event ev = new Event();
        
        @Override
        protected Event doInBackground(Object[]... params) {
            handler = (StoryFlowEventHandler) params[0][0];
            pageNum = (Integer) params[0][1];
            pageSize = (Integer) params[0][2];
            
            JSONObject jobj = null;
            ArrayList<StoryInfo> data;
            try {
                
                jobj = StoryAPI.getLatestHotStoryList(mContext, pageNum, pageSize);
                // 判断故事主题列表
                if (null != jobj) {
                    JSONObject jDataObj = jobj.optJSONObject("resp");
                    if (jDataObj!=null) {
                        int recordSize = jDataObj.getInt("recordSize");
                        data = StoryInfo.create(jobj);
                        if (data !=null) {
                            ev.eventId = Constants.STORY_INFO_LIST_SUCCESS;
                            ev.rlt = new Object[]{
                              data , recordSize     
                            };
                            return ev;
                        } 
                    }
                }
                
                ev.eventId = Constants.STORY_INFO_LIST_FAILED;
                
            } catch (StatusCodeException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_INFO_LIST_NET_INTERRUPT;
            } catch (StoryFlowException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_INFO_LIST_NET_INTERRUPT;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return ev;
        }
        
        @Override
        protected void onPostExecute(Event event) {
            handler.handleSeviceResult(event.msg, event.eventId, event.rlt);
        }
    }
    
    /**
     * 获取用户基本信息，用于显示昵称和头像等
     * @param handler
     * @param userId
     * @return
     */
    public boolean loadUserProfile(StoryFlowEventHandler handler, String userId) {
        new StoryUserProfileFetchTask().execute(new Object[] {
                handler, userId
        });
        return true;
    }
    
    private class StoryUserProfileFetchTask extends AsyncTask<Object[], Void, Event> {
        
        StoryFlowEventHandler handler;
        
        String userId;
        
        Event ev = new Event();
        
        @Override
        protected Event doInBackground(Object[]... params) {
            handler = (StoryFlowEventHandler) params[0][0];
            userId = (String) params[0][1];
            
            JSONObject jobj = null;
            try {
                
                jobj = StoryAPI.loadUserProfileAction(mContext, userId);
                if (jobj !=null) {
                    StoryUserProfileInfo info = StoryUserProfileInfo.create(jobj);
                    if (info !=null) {
                        ev.eventId = Constants.STORY_USER_PROFILE_SUCCESS;
                        ev.rlt = new Object[]{
                                info
                        };
                        return ev;
                    }
                }
                
                ev.eventId = Constants.STORY_USER_PROFILE_FAILED;
                
            } catch (StatusCodeException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_USER_PROFILE_NET_INTERRUPT;
            } catch (StoryFlowException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_USER_PROFILE_NET_INTERRUPT;
            } catch (JSONException e) {
                e.printStackTrace();
            } 
            return ev;
        }
        
        @Override
        protected void onPostExecute(Event event) {
            handler.handleSeviceResult(event.msg, event.eventId, event.rlt);
        }
    }
    
    /**
     * 获取主页"我"的tab标签数量
     * @param handler
     * @param userId
     * @return
     */
    
    public boolean loadSelfCount(StoryFlowEventHandler handler, String userId) {
        new StoryUserCountFetchTask().execute(new Object[] {
                handler, userId
        });
        return true;
    }
    
    private class StoryUserCountFetchTask extends AsyncTask<Object[], Void, Event> {
        
        StoryFlowEventHandler handler;
        
        String userId;
        
        Event ev = new Event();
        
        @Override
        protected Event doInBackground(Object[]... params) {
            handler = (StoryFlowEventHandler) params[0][0];
            userId = (String) params[0][1];
            
            JSONObject jobj = null;
            try {
                
                jobj = StoryAPI.loadUserTabCountAction(mContext, userId);
                if (jobj !=null) {
                    
                    StoryUserTabCountInfo info = StoryUserTabCountInfo.create(jobj);
                    if (info !=null) {
                        ev.eventId = Constants.STORY_USER_TAB_COUNT_SUCCESS;
                        ev.rlt = new Object[]{
                                info
                        };
                        return ev;
                    }
                }
                
                ev.eventId = Constants.STORY_USER_TAB_COUNT_FAILED;
                
            } catch (StatusCodeException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_USER_TAB_COUNT_NET_INTERRUPT;
            } catch (StoryFlowException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_USER_TAB_COUNT_NET_INTERRUPT;
            } catch (JSONException e) {
                e.printStackTrace();
            } 
            return ev;
        }
        
        @Override
        protected void onPostExecute(Event event) {
            handler.handleSeviceResult(event.msg, event.eventId, event.rlt);
        }
    }
    
    /**
     * 获取用户发布的图片列表
     * @param handler
     * @param userId
     * @return
     */
    
    public boolean loadSelfReleasePicList(StoryFlowEventHandler handler, String userId, int pageNum, int pageSize) {
        new UserReleasePicFetchTask().execute(new Object[] {
                handler, userId, pageNum, pageSize
        });
        return true;
    }
    
    private class UserReleasePicFetchTask extends AsyncTask<Object[], Void, Event> {
        
        StoryFlowEventHandler handler;
        
        String userId;
        int pageNum;
        int pageSize;
        
        Event ev = new Event();
        
        @Override
        protected Event doInBackground(Object[]... params) {
            handler = (StoryFlowEventHandler) params[0][0];
            userId = (String) params[0][1];
            pageNum = (Integer) params[0][2];
            pageSize = (Integer) params[0][3];
            
            JSONObject jobj = null;
            
            ArrayList<AlbumInfo> data;
            
            try {
                
                jobj = StoryAPI.loadUserReleasePicAction(mContext, userId, pageNum, pageSize);
                if (null != jobj) {
                    
                    JSONObject jDataObj = jobj.optJSONObject("resp");
                    if (jDataObj!=null) {
                        int recordSize = jDataObj.getInt("recordSize");
                        
                        data = AlbumInfo.create(jobj);
                        if (data !=null) {
                            ev.eventId = Constants.STORY_PIC_LIST_SUCCESS;
                            ev.rlt = new Object[]{
                              data ,  recordSize    
                            };
                            return ev;
                        } 
                        
                    }
                    
                }
                
                ev.eventId = Constants.STORY_PIC_LIST_FAILED;
                
            } catch (StatusCodeException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_PIC_LIST_NET_INTERRUPT;
            } catch (StoryFlowException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_PIC_LIST_NET_INTERRUPT;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return ev;
        }
        
        @Override
        protected void onPostExecute(Event event) {
            handler.handleSeviceResult(event.msg, event.eventId, event.rlt);
        }
    }
    
    /**
     * 获取用户喜欢的图片列表
     * @param handler
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    
    public boolean loadSelfLovePicList(StoryFlowEventHandler handler, String userId, int pageNum, int pageSize) {
        new UserLovePicFetchTask().execute(new Object[] {
                handler, userId, pageNum, pageSize
        });
        return true;
    }
    
    private class UserLovePicFetchTask extends AsyncTask<Object[], Void, Event> {
        
        StoryFlowEventHandler handler;
        
        String userId;
        int pageNum;
        int pageSize;
        
        Event ev = new Event();
        
        @Override
        protected Event doInBackground(Object[]... params) {
            handler = (StoryFlowEventHandler) params[0][0];
            userId = (String) params[0][1];
            pageNum = (Integer) params[0][2];
            pageSize = (Integer) params[0][3];
            
            JSONObject jobj = null;
            
            ArrayList<LikeInfo> data;
            
            try {
                
                jobj = StoryAPI.loadUserLovePicAction(mContext, userId, pageNum, pageSize);
                if (null != jobj) {
                    
                    JSONObject jDataObj = jobj.optJSONObject("resp");
                    if (jDataObj!=null) {
                        int recordSize = jDataObj.getInt("recordSize");
                        
                        data = LikeInfo.create(jobj);
                        if (data !=null) {
                            ev.eventId = Constants.STORY_LIKE_LIST_SUCCESS;
                            ev.rlt = new Object[]{
                                    data , recordSize     
                            };
                            return ev;
                        } 
                    }
                }
                
                ev.eventId = Constants.STORY_LIKE_LIST_FAILED;
                
            } catch (StatusCodeException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_LIKE_LIST_NET_INTERRUPT;
            } catch (StoryFlowException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_LIKE_LIST_NET_INTERRUPT;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return ev;
        }
        
        @Override
        protected void onPostExecute(Event event) {
            handler.handleSeviceResult(event.msg, event.eventId, event.rlt);
        }
    }
    
    /**
     * 获取用户参与过得故事
     * @param handler
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public boolean loadSelfStoryFlowList(StoryFlowEventHandler handler, String userId, int pageNum, int pageSize) {
        new UserParticipateStoryFlowFetchTask().execute(new Object[] {
                handler, userId, pageNum, pageSize
        });
        return true;
    }
    
    private class UserParticipateStoryFlowFetchTask extends AsyncTask<Object[], Void, Event> {
        
        StoryFlowEventHandler handler;
        
        String userId;
        int pageNum;
        int pageSize;
        
        Event ev = new Event();
        
        @Override
        protected Event doInBackground(Object[]... params) {
            handler = (StoryFlowEventHandler) params[0][0];
            userId = (String) params[0][1];
            pageNum = (Integer) params[0][2];
            pageSize = (Integer) params[0][3];
            
            JSONObject jobj = null;
            
            ArrayList<StoryInfo> data;
            try {
                
                jobj = StoryAPI.loadUserParticipateStoryFlowAction(mContext, userId, pageNum, pageSize);
             // 判断故事主题列表
                if (null != jobj) {
                    
                    JSONObject jDataObj = jobj.optJSONObject("resp");
                    if (jDataObj!=null) {
                        int recordSize = jDataObj.getInt("recordSize");
                        
                        data = StoryInfo.create(jobj);
                        if (data !=null) {
                            ev.eventId = Constants.STORY_INFO_LIST_SUCCESS;
                            ev.rlt = new Object[]{
                              data  , recordSize    
                            };
                            return ev;
                        } 
                    }
                }
                
                ev.eventId = Constants.STORY_INFO_LIST_FAILED;
                
            } catch (StatusCodeException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_INFO_LIST_NET_INTERRUPT;
            } catch (StoryFlowException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_INFO_LIST_NET_INTERRUPT;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return ev;
        }
        
        @Override
        protected void onPostExecute(Event event) {
            handler.handleSeviceResult(event.msg, event.eventId, event.rlt);
        }
    }
    
    /**
     * 根据storyId获取当前用户所有喜欢状态
     * @param handler
     * @param storyId
     * @return
     */
    public boolean loadLikedState(StoryFlowEventHandler handler, String storyId) {
        new LikedStateStoryFlowFetchTask().execute(new Object[] {
                handler, storyId
        });
        return true;
    }
    
    private class LikedStateStoryFlowFetchTask extends AsyncTask<Object[], Void, Event> {
        
        StoryFlowEventHandler handler;
        
        String storyId;
        
        Event ev = new Event();
        
        @Override
        protected Event doInBackground(Object[]... params) {
            handler = (StoryFlowEventHandler) params[0][0];
            storyId = (String) params[0][1];
            
            JSONObject jobj = null;
            
            ArrayList<String> data;
            try {
                
                jobj = StoryAPI.loadLikedStateStoryFlowAction(mContext, storyId);
                // 获取喜欢列表
                if (null != jobj) {
                    data = PicIdInfo.create(jobj);
                    if (data !=null) {
                        StoryLogger.e(TAG, TAG+":--->"+data.toString());
                        ev.eventId = Constants.STORY_LIKED_INFO_LIST_SUCCESS;
                        ev.rlt = new Object[]{
                                data      
                        };
                        return ev;
                    } 
                }
                
                ev.eventId = Constants.STORY_LIKED_INFO_LIST_FAILED;
                
            } catch (StatusCodeException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_LIKED_INFO_LIST_NET_INTERRUPT;
            } catch (StoryFlowException e) {
                e.printStackTrace();
                ev.eventId = Constants.STORY_LIKED_INFO_LIST_NET_INTERRUPT;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return ev;
        }
        
        @Override
        protected void onPostExecute(Event event) {
            handler.handleSeviceResult(event.msg, event.eventId, event.rlt);
        }
    }
    
    /**
     * 使用qq发送带图片的微博
     * @param handler
     * @param qAuthV2
     * @return
     */
    public boolean sendQQWeiboWithPic(StoryFlowEventHandler handler, OAuthV2 qAuthV2, String status, String picPath) {
        new QWeiBoWithPicTask().execute(new Object[] {
                handler, qAuthV2, status, picPath
        });
        return true;
    }
    
    private class QWeiBoWithPicTask extends AsyncTask<Object[], Void, Event> {
        
        StoryFlowEventHandler handler;
        
        
        OAuthV2 qAuthV2;
        String status = "";
        String picPath = "";
        
        @Override
        protected Event doInBackground(Object[]... params) {
            handler = (StoryFlowEventHandler) params[0][0];
            qAuthV2 = (OAuthV2) params[0][1];
            status = (String) params[0][2];
            picPath = (String) params[0][3];
            
            TAPI tAPI = new TAPI(OAuthConstants.OAUTH_VERSION_2_A);
            try {
               
                String response = tAPI.addPic(qAuthV2, "json", status, "127.0.0.1",
                        picPath);
                StoryLogger.e(TAG, response);
            } catch (Exception e) {
                e.printStackTrace();
            }
            tAPI.shutdownConnection();
                
            return null;
        }
        
        @Override
        protected void onPostExecute(Event event) {
            if (event !=null) {
                handler.handleSeviceResult(event.msg, event.eventId, event.rlt);
            }
        }
    }
    
    
    /**
     * 使用sina weibo发送带图片的微博
     * @param handler
     * @param accessToken
     * @param status
     * @param picPath
     * @return
     */
    public boolean sendSinaWeiboWithPic(StoryFlowEventHandler handler, String accessToken, String status, String picPath) {
        new SinaWeiBoWithPicTask().execute(new Object[] {
                handler, accessToken, status, picPath
        });
        return true;
    }
    
    private class SinaWeiBoWithPicTask extends AsyncTask<Object[], Void, Event> {
        
        StoryFlowEventHandler handler;
        
        
        String accessToken;
        String status = "";
        String picPath = "";
        
        @Override
        protected Event doInBackground(Object[]... params) {
            handler = (StoryFlowEventHandler) params[0][0];
            accessToken = (String) params[0][1];
            status = (String) params[0][2];
            picPath = (String) params[0][3];
            
            try {
                StoryAPI.updateWithPicViaSina(mContext, accessToken, status, picPath);
            } catch (StatusCodeException e) {
                e.printStackTrace();
            } catch (StoryFlowException e) {
                e.printStackTrace();
            }
            
            return null;
        }
        
        @Override
        protected void onPostExecute(Event event) {
            if (event !=null) {
                handler.handleSeviceResult(event.msg, event.eventId, event.rlt);
            }
        }
    }

}

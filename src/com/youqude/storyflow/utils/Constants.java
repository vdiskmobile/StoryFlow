
package com.youqude.storyflow.utils;

public class Constants {

//    public static final String API_SERVER = "http://svn.uqude.com:8081/uqude_gushiliu_war";
//    public static final String API_SERVER = "http://211.103.158.248:8082/uqude_gushiliu_war";
    public static final String API_SERVER = "http://rest.gushiliu.com";
    

    public static final String PACKAGE_NAME = "com.youqude.storyflow";
    public static final String USER_AGENT_PREFIX = "STORY_FLOW";

    public static final long VALIDITY_TIME_END = 3*30*24*60*60;//3个月
    public static final long QQ_VALIDITY_TIME_END = 3*24*60*60;//3天
    public static final long SINA_VALIDITY_TIME_END = 1*24*60*60;//1天
    public static final long BAIDU_VALIDITY_TIME_END = 25*24*60*60;//1天
    
    public static String DOWNLOAD_TEMP_FILE_SUFFIX = ".story.tmp";
    
    /**
     * prefs
     */
    public static final String SESSION_PREFS = "prefs";
    public static final String QQ_OAUTH_PREFS = "qq_t_prefs";
    public static final String SINA_OAUTH_PREFS = "sina_weibo_prefs";
    public static final String BAIDU_OAUTH_PREFS = "baidu_weibo_prefs";

    public static final String SESSION_ID = "sessionId";
    public static final String APP_TYPE = "appType";
    public static final String OPEN_ID = "openid";
    public static final String OPEN_KEY = "openkey";
    public static final String SESSION_KEY = "sessionKey";
    public static final String SESSION_SECRET = "sessionSecret";
    public static final String TOKEN = "token";
    public static final String UID = "uid";
    public static final String EXPIRE_IN = "expire_in";
    public static final String DEVICE_ID = "imei";
    public static final String VALIDITY_TIME = "validity_time";
    
    /**
     * sina
     */
    public static final String WEIBO_CONSUMER_KEY = "4236539202";
    public static final String WEIBO_CONSUMER_SECRET = "c8fe084df43acea1f60bc401e14ab3e2";
    public static final String WEIBO_REDIRECT_URI = "http://www.gushiliu.com/user/callbacksina";

    /**
     * qq
     */
    // !!!请根据您的实际情况修改!!! 认证成功后浏览器会被重定向到这个url中 必须与注册时填写的一致
    public static final String QQ_REDIRECT_URI = "http://www.gushiliu.com";//qq sdk demo自带http://www.tencent.com/zh-cn/index.shtml
    // !!!请根据您的实际情况修改!!! 换为您为自己的应用申请到的APP KEY
    public static final String QQ_CLIENT_ID = "801176312";//qq sdk demo自带801115505
    // !!!请根据您的实际情况修改!!! 换为您为自己的应用申请到的APP SECRET
    public static final String QQ_CLIENT_SECRET = "e85634a393d361e56c1adc2b6c22ffa7";//qq sdk demo自带be1dd1410434a9f7d5a2586bab7a6829

    /**
     * baidu
     */
    public static final String API_KEY = "gGOGNEOh5gV8aWp4YpYR77W0";
    public static final int BAIDU_UID_EVENT_CODE_SUCCESS = 100;
    public static final int BAIDU_BASE_PROFILE_SUCCESS = 101;

    /**
     * sina
     */
    public static final int SINA_UID_EVENT_CODE_SUCCESS = 200;
    public static final int SINA_BASE_PROFILE_SUCCESS = 201;

    /**
     * qq
     */
    public static final int QQ_BASE_PROFILE_SUCCESS = 300;

    /**
     * uqude eventId
     */
    public static final int STORY_RELEASE_SUCCESS = 400;
    public static final int STORY_RELEASE_NET_INTERRUPT = 401;
    public static final int STORY_RELEASE_FAILED = 402;
    public static final int STORY_TITLE_LIST_SUCCESS= 403;
    public static final int STORY_TITLE_LIST_FAILED= 404;
    public static final int STORY_TITLE_LIST_NET_INTERRUPT= 405;
    public static final int STORY_PIC_LIST_SUCCESS= 406;
    public static final int STORY_PIC_LIST_FAILED= 407;
    public static final int STORY_PIC_LIST_NET_INTERRUPT= 408;
    public static final int STORY_USER_PROFILE_SUCCESS= 409;
    public static final int STORY_USER_PROFILE_FAILED= 410;
    public static final int STORY_USER_PROFILE_NET_INTERRUPT= 411;
    public static final int STORY_USER_TAB_COUNT_SUCCESS= 412;
    public static final int STORY_USER_TAB_COUNT_FAILED= 413;
    public static final int STORY_USER_TAB_COUNT_NET_INTERRUPT= 414;
    public static final int STORY_DETAIL_INFO_SUCCESS= 415;
    public static final int STORY_DETAIL_INFO_FAILED= 416;
    public static final int STORY_DETAIL_INFO_NET_INTERRUPT= 417;
    public static final int STORY_LIKE_LIST_SUCCESS= 418;
    public static final int STORY_LIKE_LIST_FAILED= 419;
    public static final int STORY_LIKE_LIST_NET_INTERRUPT= 420;
    public static final int STORY_INFO_LIST_SUCCESS= 421;
    public static final int STORY_INFO_LIST_FAILED= 422;
    public static final int STORY_INFO_LIST_NET_INTERRUPT= 423;
    public static final int STORY_LIKED_INFO_LIST_SUCCESS= 424;
    public static final int STORY_LIKED_INFO_LIST_FAILED= 425;
    public static final int STORY_LIKED_INFO_LIST_NET_INTERRUPT= 426;
    
    
    /**
     * login eventId
     */
    public static final int EVENT_CODE_SUCCESS = 0x0;
    public static final int EVENT_CODE_FAILED = 0xffffffff;
    public static final int EVENT_CODE_NET_INTERRUPT = 0xfffffffe;

    /**
     * LOGIN URL
     */
    public static final String Q_LOGIN_URL = API_SERVER + "/user/loginqq/";
    public static final String BAIDU_LOGIN_URL = API_SERVER + "/user/loginbaidu/";
    public static final String SINA_LOGIN_URL = API_SERVER + "/user/loginsina/";

    /**
     * STORY PIC UPLOAD URL
     */
    public static final String STORY_PIC_UPLOAD_URL = API_SERVER + "/story/upload.json";
    
    /**
     * STORY CONTENT URL
     */
    public static final String STORY_PUBLISH_URL = API_SERVER + "/story/publish";
    public static final String STORY_PUBLISH_WITH_STORY_ID_URL = API_SERVER + "/story/publishbystory";
    public static final String STORY_TITLE_LIST_SEARCH_URL = API_SERVER + "/story/getstorylistbykeywords/";
    public static final String STORY_PIC_LIST_SEARCH_URL = API_SERVER + "/story/getstoryalbumbyId/";
    public static final String STORY_DETAIL_BY_STORY_ID_URL = API_SERVER + "/story/getstorydetailbyid/";
    public static final String STORY_HOT_TODAY_LIST_URL = API_SERVER + "/story/gethotstorybytoday/";
    public static final String STORY_HOT_WEEK_LIST_URL = API_SERVER + "/story/gethotstorybyweek/";
    public static final String STORY_HOT_LATEST_LIST_URL = API_SERVER + "/story/getnewstory/";
    
    public static final String USER_RELEASE_PIC_LIST_URL = API_SERVER + "/story/getpiclist/";
    public static final String USER_TAB_COUNT_URL = API_SERVER + "/story/gethomecountbyuser/";
    public static final String USER_PART_STORY_FLOW_LIST_URL = API_SERVER + "/story/getstorybyuid/";
    
    /**
     * STORY LIKE URL
     */
    public static final String USER_LOVE_PIC_LIST_URL = API_SERVER + "/like/getlikebyuser/";
    public static final String USER_LIKE_STATE_URL = API_SERVER + "/like/getislike/";
    public static final String USER_LIKE_ACTION_URL = API_SERVER + "/like/dolike/";
    public static final String USER_UNLIKE_ACTION_URL = API_SERVER + "/like/undolike/";
    public static final String USER_DELETE_ACTION_URL = API_SERVER + "/story/removepicbyuser/";
    public static final String USER_LIKE_STATE_ACTION_URL = API_SERVER + "/like/getislikebystory/";
    
    
    /**
     * STORY USER PROFILE URL
     */
    public static final String STORY_USER_PROFILE_URL = API_SERVER + "/user/getuser/";
    
    /**
     * Session ID URL
     */
    public static final String ADD_BASE_PROFILE = API_SERVER + "/user/addbaseprofile";

    /**
     * BAIDU USER INFO URL
     */
    public static final String BAIDU_USER_INFO_URL = "https://openapi.baidu.com/rest/2.0/passport/users/getLoggedInUser?";

    /**
     * SINA UID URL
     */
    public static final String SINA_UID_URL = "https://api.weibo.com/2/account/get_uid.json";

    /**
     * SINA USER SHOW URL
     */
    public static final String SINA_USER_SHOW_URL = "https://api.weibo.com/2/users/show.json";
    
    
    public static final String SINA_STATUS_UPDATE_URL = "https://upload.api.weibo.com/2/statuses/upload.json";

    /**
     * ACTION
     */
    public static final String UPLOAD_STORY_PIC_ACTION = "com.youqude.storyflow.UPLOAD_STORY_PIC_ACTION";
    public static final String UPLOAD_STORY_PIC_FAILED_ACTION = "com.youqude.storyflow.UPLOAD_STORY_PIC_FAILED_ACTION";
    public static final String CHANGE_STORY_CONTENT_VIEW_ACTION = "com.youqude.storyflow.CHANGE_STORY_CONTENT_VIEW_ACTION";
    public static final String CHANGE_STORY_SELF_DES_CONTENT_VIEW_ACTION = "com.youqude.storyflow.CHANGE_STORY_SELF_DES_CONTENT_VIEW_ACTION";
    public static final String CHANGE_STORY_HOME_DES_CONTENT_VIEW_ACTION = "com.youqude.storyflow.CHANGE_STORY_HOME_DES_CONTENT_VIEW_ACTION";
    public static final String RELEASE_STORY_SUCCESS_ACTION = "com.youqude.storyflow.RELEASE_STORY_SUCCESS_ACTION";
    public static final String EXIT_CURRENT_SESSION_ACTION = "com.youqude.storyflow.EXIT_CURRENT_SESSION_ACTION";
    public static final String LOGIN_SUCCESS_ACTION = "com.youqude.storyflow.LOGIN_SUCCESS_ACTION";
    public static final String CHANGE_USER_ACTION = "com.youqude.storyflow.CHANGE_USER_ACTION";
    public static final String USER_DO_LIKE_SUCCESS_ACTION = "com.youqude.storyflow.USER_DO_LIKE_SUCCESS_ACTION";
    public static final String USER_DELETE_DATA_EMPTY_ACTION = "com.youqude.storyflow.USER_DELETE_DATA_EMPTY_ACTION";
    public static final String USER_DELETE_SUCCESS_ACTION = "com.youqude.storyflow.USER_DELETE_SUCCESS_ACTION";
    public static final String ENTER_OTHERS_BY_NICKNAME_ACTION = "com.youqude.storyflow.ENTER_OTHERS_BY_NICKNAME_ACTIONs";
    public static final String ENTER_OTHERS_BY_NICKNAME_PIC_ACTION = "com.youqude.storyflow.ENTER_OTHERS_BY_NICKNAME_PIC_ACTION";
}

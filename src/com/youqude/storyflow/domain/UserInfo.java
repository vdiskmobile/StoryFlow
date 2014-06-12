package com.youqude.storyflow.domain;

import org.json.JSONException;
import org.json.JSONObject;

public class UserInfo extends BaseInfo{

    
    public static String infoCode;
    public static String appType;
    public static boolean firstLogin;
    public static String uid;
    public static String openid;
    public static String openkey;
    public static String sessionId;
    public static String sessionKey;
    public static String sessionSecret;
    public static String thirdUid;
    public static String token;
    
    public static UserInfo create(JSONObject jobj) throws JSONException {
        UserInfo info = new UserInfo();
        
        JSONObject jDataObj = jobj.optJSONObject("resp");
        if (jDataObj != null) {
            info.infoCode =jDataObj.getString("infocode"); 
            info.appType =jDataObj.getString("appType"); 
            info.firstLogin =jDataObj.getBoolean("firstLogin");
            info.uid =jDataObj.getString("id"); 
            info.openid =jDataObj.optString("openid", null); 
            if (null ==info.openid) {
                info.sessionKey =jDataObj.optString("sessionKey", null);
                if (null != info.sessionKey) {
                    info.sessionSecret =jDataObj.getString("sessionSecret"); 
                }
            } else {
                info.openkey =jDataObj.getString("openkey"); 
            }
            info.sessionId =jDataObj.getString("sessionId"); 
            info.thirdUid =jDataObj.getString("thirdUid"); 
            info.token = jDataObj.getString("token");
            return info;
        }
        return null;
    }
}
